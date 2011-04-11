package org.yaxim.androidclient.service;

public interface XMPPServiceCallback {
	void newMessage(String from, String messageBody);
	void rosterChanged();
	boolean isBoundTo(String jabberID);
	public void presenceChanged(String jid, boolean available);
}
