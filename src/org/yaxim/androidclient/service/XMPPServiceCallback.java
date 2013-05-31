package org.yaxim.androidclient.service;

import org.jivesoftware.smack.packet.Message;

public interface XMPPServiceCallback {
	void newMessage(String fromJID, String messageBody, Message.Type msgType, boolean isCarbon);
	void rosterChanged();
	void disconnectOnError();
	void mucInvitationReceived(String room, String body);
}
