package org.yaxim.androidclient.service;

import org.yaxim.androidclient.exceptions.YaximXMPPException;


public interface Smackable {
	boolean doConnect(boolean create_account) throws YaximXMPPException;
	boolean isAuthenticated();

	void addRosterItem(String user, String alias, String group) throws YaximXMPPException;
	void removeRosterItem(String user) throws YaximXMPPException;
	void renameRosterItem(String user, String newName) throws YaximXMPPException;
	void moveRosterItemToGroup(String user, String group) throws YaximXMPPException;
	void renameRosterGroup(String group, String newGroup);
	void requestAuthorizationForRosterItem(String user);
	void addRosterGroup(String group);
	
	void setStatusFromConfig();
	void sendMessage(String user, String message);
	
	void registerCallback(XMPPServiceCallback callBack);
	void unRegisterCallback();
	
	void mucTest();
	void sendMucMessage(String room, String message);
	void syncDbRooms();
	public boolean addRoom(String jid, String password, String nickname);
	public boolean removeRoom(String jid);
	public boolean createAndJoinRoom(String jid, String password, String nickname);
	public String[] getRooms();
	public boolean isRoom(String jid);
	
	String getNameForJID(String jid);

}
