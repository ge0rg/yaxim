package org.yaxim.androidclient.chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.yaxim.androidclient.MainWindow;
import org.yaxim.androidclient.R;
import org.yaxim.androidclient.XMPPRosterServiceAdapter;
import org.yaxim.androidclient.data.RosterItem;
import org.yaxim.androidclient.data.ChatProvider;
import org.yaxim.androidclient.data.ChatProvider.ChatConstants;
import org.yaxim.androidclient.service.IXMPPChatService;
import org.yaxim.androidclient.service.IXMPPRosterService;
import org.yaxim.androidclient.service.XMPPService;
import org.yaxim.androidclient.util.PreferenceConstants;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ChatWindow extends ListActivity implements OnKeyListener,
		TextWatcher {

	public static final String INTENT_EXTRA_USERNAME = ChatWindow.class.getName() + ".username";
	
	private static final String TAG = "ChatWindow";
	private static final String[] PROJECTION_FROM = new String[] {
			ChatProvider.ChatConstants._ID, ChatProvider.ChatConstants.DATE,
			ChatProvider.ChatConstants.FROM_ME, ChatProvider.ChatConstants.JID,
			ChatProvider.ChatConstants.MESSAGE, ChatProvider.ChatConstants.HAS_BEEN_READ };

	private static final int[] PROJECTION_TO = new int[] { R.id.chat_date,
			R.id.chat_from, R.id.chat_message };

	private Button mSendButton = null;
	private EditText mChatInput = null;
	private String mWithJabberID = null;
	private String mUserScreenName = null;
	private Intent mServiceIntent;
	private ServiceConnection mServiceConnection;
	private XMPPChatServiceAdapter mServiceAdapter;
	private XMPPRosterServiceAdapter mRosterServiceAdapter = null;
	private AlertDialog mChooser;
	private Intent mRosterServiceIntent;
	private ServiceConnection mRosterServiceConnection = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		String theme = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceConstants.THEME, "dark");
		if (theme.equals("light")) {
			setTheme(R.style.LightTheme_NoTitle);
		} else {
			setTheme(R.style.DarkTheme_NoTitle);
		}
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mainchat);

		registerForContextMenu(getListView());
		setContactFromUri();
		registerXMPPService();
		registerXMPPRosterService();
		setUserInput();
		setSendButton();
		
		String titleUserid;
		if (mUserScreenName != null) {
			titleUserid = mUserScreenName;
		} else {
			titleUserid = mWithJabberID;
		}

		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle(titleUserid);
		actionBar.setHomeAction(new IntentAction(this, MainWindow
				.createIntent(this), R.drawable.ic_action_appicon));

		setChatWindowAdapter();
	}

	private void setChatWindowAdapter() {
		String selection = ChatConstants.JID + "='" + mWithJabberID + "'";
		Cursor cursor = managedQuery(ChatProvider.CONTENT_URI, PROJECTION_FROM,
				selection, null, null);
		ListAdapter adapter = new ChatWindowAdapter(cursor, PROJECTION_FROM,
				PROJECTION_TO, mWithJabberID, mUserScreenName);

		setListAdapter(adapter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unbindXMPPService();
	}

	@Override
	protected void onResume() {
		super.onResume();
		bindXMPPService();
		mChatInput.requestFocus();
	}

	private void registerXMPPRosterService() {
		Log.i(TAG, "called startXMPPRosterService()");
		Intent i = getIntent();

		String action = i.getAction();
		if (!((action != null) && (action.equals(Intent.ACTION_SEND)))) return;
		mRosterServiceIntent = new Intent(this, XMPPService.class);
		mRosterServiceIntent.setAction("org.yaxim.androidclient.XMPPSERVICE");
		
		mRosterServiceConnection = new ServiceConnection() {
		
			public void onServiceConnected(ComponentName name, final IBinder service) {
				Log.i(TAG, "called onServiceConnected() Roster");
				mRosterServiceAdapter = new XMPPRosterServiceAdapter(IXMPPRosterService.Stub.asInterface(service));
						
				Intent i = getIntent();
						
				String action = i.getAction();
				if ((action != null) && (action.equals(Intent.ACTION_SEND))) {
					final String text = i.getStringExtra(Intent.EXTRA_TEXT);
							
					List<String> rosterGroups = null;
					rosterGroups = mRosterServiceAdapter.getRosterGroups();
					if (rosterGroups == null) return;
					int itemCount = 0;
					for (String group : rosterGroups) {
						List<RosterItem> rosterItems = mRosterServiceAdapter.getGroupItems(group);
						itemCount += rosterItems.size();
					}
							
					final CharSequence[] screenNames = new CharSequence[itemCount];
					final CharSequence[] jabberIDs = new CharSequence[itemCount];
					itemCount = 0;
							
					for (String group : rosterGroups) {
						List<RosterItem> rosterItems = mRosterServiceAdapter.getGroupItems(group);
						for (RosterItem item : rosterItems) {
							if (item.screenName.length() > 0) {
								screenNames[itemCount] = item.screenName;
							} else {
								screenNames[itemCount] = item.jabberID;
							}
							jabberIDs[itemCount] = item.jabberID;
							itemCount++;
						}
					}
			
					AlertDialog.Builder builder = new AlertDialog.Builder(ChatWindow.this);
					builder.setTitle(getText(R.string.chooseContact))
					       .setCancelable(true)
					       .setItems(screenNames, new DialogInterface.OnClickListener() {
							    public void onClick(DialogInterface dialog, int item) {
									mWithJabberID = new String(jabberIDs[item].toString());
									mUserScreenName = new String(screenNames[item].toString());
									
									String titleUserid = mUserScreenName;

									ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
									actionBar.setTitle(titleUserid);
									actionBar.setHomeAction(new IntentAction(ChatWindow.this, MainWindow
											.createIntent(ChatWindow.this), R.drawable.ic_action_appicon));

									setChatWindowAdapter();
									mChatInput.setText(text);
									unbindService(mRosterServiceConnection);

									mRosterServiceConnection = null;
									registerXMPPService();
									bindXMPPService();
							    }
					       }).setOnCancelListener(new DialogInterface.OnCancelListener() {
								public void onCancel(DialogInterface dialog) {
									unbindService(mRosterServiceConnection);
									finish();
								}
						});
				
					mChooser = builder.create();
					mChooser.show();
				} 
			}
		
			public void onServiceDisconnected(ComponentName name) {
				Log.i(TAG, "called onServiceDisconnected() Roster");
			}
		};
	}

	private void registerXMPPService() {
		Log.i(TAG, "called startXMPPService()");
		if (mWithJabberID.length() == 0) {
			return;
		}
		mServiceIntent = new Intent(this, XMPPService.class);
		Uri chatURI = Uri.parse(mWithJabberID);
		mServiceIntent.setData(chatURI);
		mServiceIntent.setAction("org.yaxim.androidclient.XMPPSERVICE");

		mServiceConnection = new ServiceConnection() {

			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(TAG, "called onServiceConnected()");
				mServiceAdapter = new XMPPChatServiceAdapter(
						IXMPPChatService.Stub.asInterface(service),
						mWithJabberID);
				
				mServiceAdapter.clearNotifications(mWithJabberID);
			}

			public void onServiceDisconnected(ComponentName name) {
				Log.i(TAG, "called onServiceDisconnected()");
			}

		};
	}

	private void unbindXMPPService() {
		try {
			if (mServiceConnection != null) unbindService(mServiceConnection);
			if (mRosterServiceConnection != null) unbindService(mRosterServiceConnection);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Service wasn't bound!");
		}
	}

	private void bindXMPPService() {
		if ((mRosterServiceIntent != null) && (mRosterServiceConnection != null)) {
			bindService(mRosterServiceIntent, mRosterServiceConnection, BIND_AUTO_CREATE);
		}
		if ((mServiceIntent != null) && (mServiceConnection != null)) {
			bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		}
	}

	private void setSendButton() {
		mSendButton = (Button) findViewById(R.id.Chat_SendButton);
		View.OnClickListener onSend = getOnSetListener();
		mSendButton.setOnClickListener(onSend);
		mSendButton.setEnabled(false);
	}

	private void setUserInput() {
		mChatInput = (EditText) findViewById(R.id.Chat_UserInput);
		mChatInput.addTextChangedListener(this);
	}

	private void setContactFromUri() {
		Intent i = getIntent();

		String action = i.getAction();
		if ((action != null) && (action.equals(Intent.ACTION_SEND))) {
			mWithJabberID = "";
		} else {
			mWithJabberID = i.getDataString().toLowerCase();
		}
		
		if (i.hasExtra(INTENT_EXTRA_USERNAME)) {
			mUserScreenName = i.getExtras().getString(INTENT_EXTRA_USERNAME);
		} else {
			mUserScreenName = mWithJabberID;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.chat_contextmenu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		View target = ((AdapterContextMenuInfo)item.getMenuInfo()).targetView;
		switch (item.getItemId()) {
		case R.id.chat_contextmenu_copy_text:
			TextView message = (TextView)target.findViewById(R.id.chat_message);
			ClipboardManager cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
			cm.setText(message.getText());
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	

	private View.OnClickListener getOnSetListener() {
		return new View.OnClickListener() {

			public void onClick(View v) {
				sendMessageIfNotNull();
			}
		};
	}

	private void sendMessageIfNotNull() {
		if (mChatInput.getText().length() >= 1) {
			sendMessage(mChatInput.getText().toString());
			if (!mServiceAdapter.isServiceAuthenticated())
				showToastNotification(R.string.toast_stored_offline);
		}
	}

	private void sendMessage(String message) {
		mChatInput.setText(null);
		mSendButton.setEnabled(false);
		mServiceAdapter.sendMessage(mWithJabberID, message);
	}

	private void markAsRead(int id) {
		Uri rowuri = Uri.parse("content://" + ChatProvider.AUTHORITY
			+ "/" + ChatProvider.TABLE_NAME + "/" + id);
		Log.d(TAG, "markAsRead: " + rowuri);
		ContentValues values = new ContentValues();
		values.put(ChatConstants.HAS_BEEN_READ, true);
		getContentResolver().update(rowuri, values, null, null);
	}

	class ChatWindowAdapter extends SimpleCursorAdapter {
		String mScreenName, mJID;

		ChatWindowAdapter(Cursor cursor, String[] from, int[] to,
				String JID, String screenName) {
			super(ChatWindow.this, android.R.layout.simple_list_item_1, cursor,
					from, to);
			mScreenName = screenName;
			mJID = JID;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			ChatItemWrapper wrapper = null;
			Cursor cursor = this.getCursor();
			cursor.moveToPosition(position);

			long dateMilliseconds = cursor.getLong(cursor
					.getColumnIndex(ChatProvider.ChatConstants.DATE));

			int _id = cursor.getInt(cursor
					.getColumnIndex(ChatProvider.ChatConstants._ID));
			String date = getDateString(dateMilliseconds);
			String message = cursor.getString(cursor
					.getColumnIndex(ChatProvider.ChatConstants.MESSAGE));
			int from_me = cursor.getInt(cursor
					.getColumnIndex(ChatProvider.ChatConstants.FROM_ME));
			String jid = cursor.getString(cursor
					.getColumnIndex(ChatProvider.ChatConstants.JID));
			int has_been_read = cursor.getInt(cursor
					.getColumnIndex(ChatProvider.ChatConstants.HAS_BEEN_READ));

			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.chatrow, null);
				wrapper = new ChatItemWrapper(row);
				row.setTag(wrapper);
			} else {
				wrapper = (ChatItemWrapper) row.getTag();
			}

			if (from_me == 0 && has_been_read == 0) {
				markAsRead(_id);
			}

			String from = jid;
			if (jid.equals(mJID))
				from = mScreenName;
			wrapper.populateFrom(date, from_me != 0, from, message, has_been_read != 0);

			return row;
		}
	}

	private String getDateString(long milliSeconds) {
		SimpleDateFormat dateFormater = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		Date date = new Date(milliSeconds);
		return dateFormater.format(date);
	}

	public class ChatItemWrapper {
		private TextView mDateView = null;
		private TextView mFromView = null;
		private TextView mMessageView = null;

		private final View mRowView;

		ChatItemWrapper(View row) {
			this.mRowView = row;
		}

		void populateFrom(String date, boolean from_me, String from, String message,
				boolean has_been_read) {
//			Log.i(TAG, "populateFrom(" + from_me + ", " + from + ", " + message + ")");
			getDateView().setText(date);
			TypedValue tv = new TypedValue();
			if (from_me) {
				getTheme().resolveAttribute(R.attr.ChatMsgHeaderMeColor, tv, true);
				getDateView().setTextColor(tv.data);
				getFromView().setText(getString(R.string.chat_from_me));
				getFromView().setTextColor(tv.data);
			} else {
				getTheme().resolveAttribute(R.attr.ChatMsgHeaderYouColor, tv, true);
				getDateView().setTextColor(tv.data);
				getFromView().setText(from + ":");
				getFromView().setTextColor(tv.data);
			}
			if (!has_been_read) {
				ColorDrawable layers[] = new ColorDrawable[2];
				layers[0] = new ColorDrawable(0xff404040);
				if (from_me) {
					layers[1] = new ColorDrawable(0x60404040);
				} else {
					layers[1] = new ColorDrawable(0x00000000);
				}
				TransitionDrawable backgroundColorAnimation = new
					TransitionDrawable(layers);
				mRowView.setBackgroundDrawable(backgroundColorAnimation);
				backgroundColorAnimation.setCrossFadeEnabled(true);
				backgroundColorAnimation.startTransition(2000);
			}
			getMessageView().setText(message);
		}
        
		
		TextView getDateView() {
			if (mDateView == null) {
				mDateView = (TextView) mRowView.findViewById(R.id.chat_date);
			}
			return mDateView;
		}

		TextView getFromView() {
			if (mFromView == null) {
				mFromView = (TextView) mRowView.findViewById(R.id.chat_from);
			}
			return mFromView;
		}

		TextView getMessageView() {
			if (mMessageView == null) {
				mMessageView = (TextView) mRowView
						.findViewById(R.id.chat_message);
			}
			return mMessageView;
		}

	}

	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_ENTER) {
			sendMessageIfNotNull();
			return true;
		}
		return false;

	}

	public void afterTextChanged(Editable s) {
		if (mChatInput.getText().length() >= 1) {
			mChatInput.setOnKeyListener(this);
			mSendButton.setEnabled(true);
		}
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	private void showToastNotification(int message) {
		Toast toastNotification = Toast.makeText(this, message,
				Toast.LENGTH_SHORT);
		toastNotification.show();
	}

}
