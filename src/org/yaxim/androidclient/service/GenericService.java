package org.yaxim.androidclient.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.yaxim.androidclient.chat.ChatWindow;
import org.yaxim.androidclient.data.YaximConfiguration;
import org.yaxim.androidclient.util.LogConstants;
import org.yaxim.androidclient.util.Log;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.widget.Toast;
import org.yaxim.androidclient.R;

public abstract class GenericService extends Service {

	private static final String TAG = "yaxim.Service";
	private static final String APP_NAME = "yaxim";
	private static final int MAX_TICKER_MSG_LEN = 50;
	private static final int DELAYED_NOTIFICATION_TIMEOUT = 30000;

	private NotificationManager mNotificationMGR;
	private Notification mNotification;
	private ArrayList<DelayedNotification> mPendingNotifications = new ArrayList<DelayedNotification>();
	private Vibrator mVibrator;
	private Intent mNotificationIntent;
	protected WakeLock mWakeLock;
	//private int mNotificationCounter = 0;
	
	private Map<String, Integer> notificationCount = new HashMap<String, Integer>(2);
	private Map<String, Integer> notificationId = new HashMap<String, Integer>(2);
	protected static int SERVICE_NOTIFICATION = 1;
	private int lastNotificationId = 2;

	protected YaximConfiguration mConfig;

	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "called onBind()");
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "called onUnbind()");
		return super.onUnbind(intent);
	}

	@Override
	public void onRebind(Intent intent) {
		Log.i(TAG, "called onRebind()");
		super.onRebind(intent);
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "called onCreate()");
		super.onCreate();
		mConfig = new YaximConfiguration(PreferenceManager
				.getDefaultSharedPreferences(this));
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mWakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE))
				.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, APP_NAME);
		addNotificationMGR();
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "called onDestroy()");
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "called onStartCommand()");
		return START_STICKY;
	}

	private void addNotificationMGR() {
		mNotificationMGR = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationIntent = new Intent(this, ChatWindow.class);
	}

	protected void notifyClient(String fromJid, String fromUserName, String message,
			boolean showNotification, boolean deferrable) {
		if (!showNotification) {
			if (deferrable) {
				new DelayedNotification(this, fromJid, fromUserName, message,
						DELAYED_NOTIFICATION_TIMEOUT);
			} else {
				// only play sound and return
				RingtoneManager.getRingtone(getApplicationContext(), mConfig.notifySound).play();
			}
			return;
		}
		mWakeLock.acquire();
		setNotification(fromJid, fromUserName, message);
		setLEDNotification();
		mNotification.sound = mConfig.notifySound;
		
		int notifyId = 0;
		if (notificationId.containsKey(fromJid)) {
			notifyId = notificationId.get(fromJid);
		} else {
			lastNotificationId++;
			notifyId = lastNotificationId;
			notificationId.put(fromJid, Integer.valueOf(notifyId));
		}

		// If vibration is set to "system default", add the vibration flag to the 
		// notification and let the system decide.
		if("SYSTEM".equals(mConfig.vibraNotify)) {
			mNotification.defaults |= Notification.DEFAULT_VIBRATE;
		}
		mNotificationMGR.notify(notifyId, mNotification);
		
		// If vibration is forced, vibrate now.
		if("ALWAYS".equals(mConfig.vibraNotify)) {
			mVibrator.vibrate(400);
		}
		mWakeLock.release();
	}
	
	private void setNotification(String fromJid, String fromUserId, String message) {
		
		int mNotificationCounter = 0;
		if (notificationCount.containsKey(fromJid)) {
			mNotificationCounter = notificationCount.get(fromJid);
		}
		mNotificationCounter++;
		notificationCount.put(fromJid, mNotificationCounter);
		String author;
		if (null == fromUserId || fromUserId.length() == 0) {
			author = fromJid;
		} else {
			author = fromUserId;
		}
		String title = getString(R.string.notification_message, author);
		String ticker;
		if (mConfig.ticker) {
			int newline = message.indexOf('\n');
			int limit = 0;
			String messageSummary = message;
			if (newline >= 0)
				limit = newline;
			if (limit > MAX_TICKER_MSG_LEN || message.length() > MAX_TICKER_MSG_LEN)
				limit = MAX_TICKER_MSG_LEN;
			if (limit > 0)
				messageSummary = message.substring(0, limit) + " [...]";
			ticker = title + ":\n" + messageSummary;
		} else
			ticker = getString(R.string.notification_anonymous_message, author);
		mNotification = new Notification(R.drawable.sb_message, ticker,
				System.currentTimeMillis());
		Uri userNameUri = Uri.parse(fromJid);
		mNotificationIntent.setData(userNameUri);
		mNotificationIntent.putExtra(ChatWindow.INTENT_EXTRA_USERNAME, fromUserId);
		mNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		//need to set flag FLAG_UPDATE_CURRENT to get extras transferred
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				mNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		mNotification.setLatestEventInfo(this, title, message, pendingIntent);
		if (mNotificationCounter > 1)
			mNotification.number = mNotificationCounter;
		mNotification.flags = Notification.FLAG_AUTO_CANCEL;
	}

	private void setLEDNotification() {
		if (mConfig.isLEDNotify) {
			mNotification.ledARGB = Color.MAGENTA;
			mNotification.ledOnMS = 300;
			mNotification.ledOffMS = 1000;
			mNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
		}
	}

	protected void shortToastNotify(String msg) {
		Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		toast.show();
	}

	public void resetNotificationCounter(String userJid) {
		notificationCount.remove(userJid);
	}

	protected void logError(String data) {
		if (LogConstants.LOG_ERROR) {
			Log.e(TAG, data);
		}
	}

	protected void logInfo(String data) {
		if (LogConstants.LOG_INFO) {
			Log.i(TAG, data);
		}
	}

	public void clearNotification(String Jid) {
		int notifyId = 0;
		if (notificationId.containsKey(Jid)) {
			notifyId = notificationId.get(Jid);
			mNotificationMGR.cancel(notifyId);
		}
		while (!mPendingNotifications.isEmpty()) {
			mPendingNotifications.get(0).cancel();
		}
	}

	public void flushNotifications() {
		while (!mPendingNotifications.isEmpty()) {
			mPendingNotifications.get(0).deliver();
		}
	}

	private class DelayedNotification extends BroadcastReceiver {
		private static final String DELAYED_NOTIFICATION_ALARM =
				"org.yaxim.androidclient.DELAYED_NOTIFICATION_ALARM";

		private String mFromJid;
		private String mFromUserName;
		private String mMessage;
		private Intent mAlarmIntent = new Intent(DELAYED_NOTIFICATION_ALARM);
		private PendingIntent mPAlarmIntent;

		public DelayedNotification(Context ctx, String fromJid, String fromUserName,
				String message, int delay) {
			mFromJid = fromJid;
			mFromUserName = fromUserName;
			mMessage = message;

			mPAlarmIntent = PendingIntent.getBroadcast(ctx, 0, mAlarmIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			registerReceiver(this, new IntentFilter(DELAYED_NOTIFICATION_ALARM));

			mPendingNotifications.add(this);

			((AlarmManager)getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis() + delay, mPAlarmIntent);
		}

		public synchronized void cancel() {
			if (mPAlarmIntent == null)
				return;
			logInfo("Cancel delayed notification.");
			((AlarmManager)getSystemService(Context.ALARM_SERVICE)).cancel(mPAlarmIntent);
			unregisterReceiver(this);
			mPAlarmIntent = null;
			mPendingNotifications.remove(this);
		}

		private synchronized void doDeliver(boolean fullNotification) {
			if (mPAlarmIntent == null)
				return;
			logInfo("Delayed notification.");
			unregisterReceiver(this);
			mPAlarmIntent = null;
			mPendingNotifications.remove(this);
			notifyClient(mFromJid, mFromUserName, mMessage, fullNotification, false);
		}

		public void deliver() {
			doDeliver(true);
		}

		public void onReceive(Context ctx, Intent i) {
			doDeliver(false);
		}
	}
}
