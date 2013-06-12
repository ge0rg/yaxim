package org.yaxim.androidclient.service;

import org.jivesoftware.smack.packet.Message;

public interface XMPPServiceCallback {
	void newMessage(String[] from, String messageBody, Message.Type msgType);
	void rosterChanged();
	void disconnectOnError();
	void mucInvitationReceived(String room, String body);
}
