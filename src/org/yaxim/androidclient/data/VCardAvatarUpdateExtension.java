package org.yaxim.androidclient.data;

import org.jivesoftware.smack.packet.PacketExtension;

public class VCardAvatarUpdateExtension implements PacketExtension {
	String avatarHash = "";

	public String getAvatarHash() {
		return avatarHash;
	}

	public void setAvatarHash(String hash) {
		avatarHash = hash;
	}

	@Override
	public String getElementName() {
		return "x";
	}

	@Override
	public String getNamespace() {
		return "vcard-temp:x:update";
	}

	@Override
	public String toXML() {
		StringBuilder xml = new StringBuilder("<x xmlns='vcard-temp:x:update'><photo>");
		xml.append(avatarHash);
		xml.append("</photo></x>");
		return xml.toString();
	}
}
