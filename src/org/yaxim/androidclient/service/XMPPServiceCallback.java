package org.yaxim.androidclient.service;

public interface XMPPServiceCallback {
	void newMessage(String from, String messageBody, String newID);
	void rosterChanged();
	boolean isBoundTo(String jabberID);
}
