package org.yaxim.androidclient.service;

public interface XMPPServiceCallback {
	void newMessage(String from, String messageBody, boolean isCarbon);
	void rosterChanged();
	void disconnectOnError();
}
