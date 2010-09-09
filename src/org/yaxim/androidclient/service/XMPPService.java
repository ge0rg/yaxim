package org.yaxim.androidclient.service;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.yaxim.androidclient.IXMPPRosterCallback;
import org.yaxim.androidclient.R;
import org.yaxim.androidclient.data.RosterItem;
import org.yaxim.androidclient.exceptions.YaximXMPPException;
import org.yaxim.androidclient.util.ConnectionState;
import org.yaxim.androidclient.util.StatusMode;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

public class XMPPService extends GenericService {

	private AtomicBoolean mIsConnected = new AtomicBoolean(false);
	private AtomicBoolean mConnectionDemanded = new AtomicBoolean(false); // should we try to reconnect?
	private static final int RECONNECT_AFTER = 5;
	private static final int RECONNECT_MAXIMUM = 10*60;
	private int mReconnectTimeout = RECONNECT_AFTER;
	private String mLastConnectionError = null;
	private String mReconnectInfo = "";

	private Thread mConnectingThread;

	private Smackable mSmackable;
	private IXMPPRosterService.Stub mService2RosterConnection;
	private IXMPPChatService.Stub mServiceChatConnection;

	private RemoteCallbackList<IXMPPRosterCallback> mRosterCallbacks = new RemoteCallbackList<IXMPPRosterCallback>();
	private HashSet<String> mIsBoundTo = new HashSet<String>();
	private Handler mMainHandler = new Handler();

	@Override
	public IBinder onBind(Intent intent) {
		super.onBind(intent);
		String chatPartner = intent.getDataString();
		if ((chatPartner != null)) {
			resetNotificationCounter(chatPartner);
			mIsBoundTo.add(chatPartner);
			return mServiceChatConnection;
		}

		return mService2RosterConnection;
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		String chatPartner = intent.getDataString();
		if ((chatPartner != null)) {
			mIsBoundTo.add(chatPartner);
			resetNotificationCounter(chatPartner);
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		String chatPartner = intent.getDataString();
		if ((chatPartner != null)) {
			mIsBoundTo.remove(chatPartner);
		}
		return true;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		createServiceRosterStub();
		createServiceChatStub();

		// for the initial connection, check if autoConnect is set
		mConnectionDemanded.set(mConfig.autoConnect);

		if (mConfig.autoConnect) {
			/*
			 * start our own service so it remains in background even when
			 * unbound
			 */
			Intent xmppServiceIntent = new Intent(this, XMPPService.class);
			xmppServiceIntent.setAction("org.yaxim.androidclient.XMPPSERVICE");
			startService(xmppServiceIntent);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mRosterCallbacks.kill();
		doDisconnect();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		doConnect();
	}

	private void createServiceChatStub() {
		mServiceChatConnection = new IXMPPChatService.Stub() {

			public void sendMessage(String user, String message)
					throws RemoteException {
				mSmackable.sendMessage(user, message);
			}

			public boolean isAuthenticated() throws RemoteException {
				if (mSmackable != null) {
					return mSmackable.isAuthenticated();
				}

				return false;
			}
		};
	}

	private void createServiceRosterStub() {
		mService2RosterConnection = new IXMPPRosterService.Stub() {

			public void registerRosterCallback(IXMPPRosterCallback callback)
					throws RemoteException {
				if (callback != null)
					mRosterCallbacks.register(callback);
			}

			public void unregisterRosterCallback(IXMPPRosterCallback callback)
					throws RemoteException {
				if (callback != null)
					mRosterCallbacks.unregister(callback);
			}

			public int getConnectionState() throws RemoteException {
				if (mSmackable != null && mSmackable.isAuthenticated()) {
					return ConnectionState.AUTHENTICATED;
				} else if (mConnectionDemanded.get()) {
					return ConnectionState.CONNECTING;
				} else {
					return ConnectionState.OFFLINE;
				}
			}

			public String getConnectionStateString() throws RemoteException {
				if (mLastConnectionError != null)
					return mLastConnectionError + mReconnectInfo;
				else
					return null;
			}


			public void setStatus(String status, String statusMsg)
					throws RemoteException {
				if (status.equals("offline")) {
					doDisconnect();
					return;
				}
				mSmackable.setStatus(StatusMode.valueOf(status), statusMsg);
			}

			public void addRosterItem(String user, String alias, String group)
					throws RemoteException {
				try {
					mSmackable.addRosterItem(user, alias, group);
				} catch (YaximXMPPException e) {
					shortToastNotify(e.getMessage());
					logError("exception in addRosterItem(): " + e.getMessage());
				}
			}

			public void addRosterGroup(String group) throws RemoteException {
				mSmackable.addRosterGroup(group);
			}

			public void removeRosterItem(String user) throws RemoteException {
				try {
					mSmackable.removeRosterItem(user);
				} catch (YaximXMPPException e) {
					shortToastNotify(e.getMessage());
					logError("exception in removeRosterItem(): "
							+ e.getMessage());
				}
			}

			public void moveRosterItemToGroup(String user, String group)
					throws RemoteException {
				try {
					mSmackable.moveRosterItemToGroup(user, group);
				} catch (YaximXMPPException e) {
					shortToastNotify(e.getMessage());
					logError("exception in moveRosterItemToGroup(): "
							+ e.getMessage());
				}
			}

			public void renameRosterItem(String user, String newName)
					throws RemoteException {
				try {
					mSmackable.renameRosterItem(user, newName);
				} catch (YaximXMPPException e) {
					shortToastNotify(e.getMessage());
					logError("exception in renameRosterItem(): "
							+ e.getMessage());
				}
			}

			public List<String> getRosterGroups() throws RemoteException {
				return mSmackable.getRosterGroups();
			}

			public List<RosterItem> getRosterEntriesByGroup(String group)
					throws RemoteException {
				return mSmackable.getRosterEntriesByGroup(group);
			}

			public void renameRosterGroup(String group, String newGroup)
					throws RemoteException {
				mSmackable.renameRosterGroup(group, newGroup);
			}

			public void disconnect() throws RemoteException {
				doDisconnect();
			}

			public void connect() throws RemoteException {
				doConnect();
			}

			public void requestAuthorizationForRosterItem(String user)
					throws RemoteException {
				mSmackable.requestAuthorizationForRosterItem(user);
			}
		};
	}

	private void doConnect() {
		if (mConnectingThread != null) {
			// a connection is still goign on!
			return;
		}

		setForeground(true);
		if (mSmackable == null) {
			createAdapter();
			registerAdapterCallback();
		}

		mConnectingThread = new Thread() {

			public void run() {
				try {
					if (!mSmackable.doConnect()) {
						postConnectionFailed("Inconsistency in Smackable.doConnect()");
					}
				} catch (YaximXMPPException e) {
					String message = e.getLocalizedMessage();
					if (e.getCause() != null)
						message += "\n" + e.getCause().getLocalizedMessage();
					postConnectionFailed(message);
					logError("YaximXMPPException in doConnect(): " + e);
				} finally {
					mConnectingThread = null;
				}
			}

		};
		mConnectingThread.start();
	}

	private void postConnectionFailed(final String reason) {
		mMainHandler.post(new Runnable() {
			public void run() {
				connectionFailed(reason);
			}
		});
	}

	private void postConnectionEstablished() {
		mMainHandler.post(new Runnable() {
			public void run() {
				connectionEstablished();
			}
		});
	}

	private void postRosterChanged() {
		mMainHandler.post(new Runnable() {
			public void run() {
				rosterChanged();
			}
		});
	}

	private void connectionFailed(String reason) {
		logInfo("connectionFailed: " + reason);
		mLastConnectionError = reason;
		mIsConnected.set(false);
		final int broadCastItems = mRosterCallbacks.beginBroadcast();
		for (int i = 0; i < broadCastItems; i++) {
			try {
				mRosterCallbacks.getBroadcastItem(i).connectionFailed(mConnectionDemanded.get());
			} catch (RemoteException e) {
				logError("caught RemoteException: " + e.getMessage());
			}
		}
		mRosterCallbacks.finishBroadcast();
		// post reconnection
		if (mConnectionDemanded.get()) {
			mReconnectInfo = getString(R.string.conn_reconnect, mReconnectTimeout);
			logInfo("connectionFailed(): registering reconnect in " + mReconnectTimeout + "s");
			mMainHandler.postDelayed(new Runnable() {
				public void run() {
					if (!mConnectionDemanded.get()) {
						return;
					}
					if (mIsConnected.get()) {
						logError("Reconnect attempt aborted: we are connected again!");
						return;
					}
					doConnect();
				}
			}, mReconnectTimeout * 1000);
			mReconnectTimeout = mReconnectTimeout * 2;
			if (mReconnectTimeout > RECONNECT_MAXIMUM)
				mReconnectTimeout = RECONNECT_MAXIMUM;
		} else
			mReconnectInfo = "";

	}

	private void connectionEstablished() {
		// once we are connected, use autoReconnect to determine reconnections
		mConnectionDemanded.set(mConfig.autoReconnect);
		mLastConnectionError = null;
		mIsConnected.set(true);
		mReconnectTimeout = RECONNECT_AFTER;
		final int broadCastItems = mRosterCallbacks.beginBroadcast();
		for (int i = 0; i < broadCastItems; i++) {
			try {
				mRosterCallbacks.getBroadcastItem(i).connectionSuccessful();
			} catch (RemoteException e) {
				logError("caught RemoteException: " + e.getMessage());
			}
		}
		mRosterCallbacks.finishBroadcast();
	}

	private void rosterChanged() {
		if (!mIsConnected.get() && mSmackable.isAuthenticated()) {
			// We get a roster changed update, but we are not connected,
			// that means we just got connected and need to notify the Activity.
			logInfo("rosterChanged(): we just got connected");
			connectionEstablished();
		}
		if (mIsConnected.get()) {
			final int broadCastItems = mRosterCallbacks.beginBroadcast();

			for (int i = 0; i < broadCastItems; ++i) {
				try {
					mRosterCallbacks.getBroadcastItem(i).rosterChanged();
				} catch (RemoteException e) {
					logError("caught RemoteException: " + e.getMessage());
				}
			}
			mRosterCallbacks.finishBroadcast();
		}
		if (mIsConnected.get() && mSmackable != null && !mSmackable.isAuthenticated()) {
			logInfo("rosterChanged(): disconnected without warning");
			connectionFailed("Connection closed");
		}
	}

	public void doDisconnect() {
		mConnectionDemanded.set(false);
		if (mSmackable != null) {
			mSmackable.unRegisterCallback();
		}
		mSmackable = null;
		setForeground(false);
	}

	private void createAdapter() {
		System.setProperty("smack.debugEnabled", "" + mConfig.smackdebug);
		try {
			mSmackable = new SmackableImp(mConfig, getContentResolver());
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

  private class MyXMPPServiceCallback implements XMPPServiceCallback {
    private String lastID = "";
    public void newMessage(String from, String message, String newID) {
      // Called from SmackableImp.processMessage
      // duplicate-anti-dupe-till-it-works-somewhere-trick
      if (newID == lastID) {
        logInfo("ignoring duplicate ID: " + newID); 
        return;
      }
      lastID = newID;

      if (!mIsBoundTo.contains(from)) {
        logInfo("notification: " + from);
        notifyClient(from, mSmackable.getNameForJID(from), message);
      }
    }

    public void rosterChanged() {
      postRosterChanged();
    }

    public boolean isBoundTo(String jabberID) {
      return mIsBoundTo.contains(jabberID);
    }
  }

  private XMPPServiceCallback myXMPPCallback = null;

	private void registerAdapterCallback() {
    if (myXMPPCallback == null)
      myXMPPCallback = new MyXMPPServiceCallback();
		mSmackable.registerCallback(myXMPPCallback);
	}
}

