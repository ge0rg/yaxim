package org.yaxim.androidclient.service;

import org.jivesoftware.smack.packet.Message;

public interface XMPPServiceCallback {
	void newMessage(String[] from, String messageBody, 
			Message.Type msgType, boolean isCarbon, String ownNick);
	void messageError(String[] from, String errorBody, boolean silent_notification);
	void connectionStateChanged();
	void rosterChanged(); // TODO: remove that!
	void mucInvitationReceived(String room, String body);
}
