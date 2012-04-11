package org.yaxim.androidclient.service;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;
import org.yaxim.androidclient.data.VCardAvatarUpdateExtension;

public class VCardAvatarUpdateProvider implements PacketExtensionProvider {
	@Override
	public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
		VCardAvatarUpdateExtension updateExtension = new VCardAvatarUpdateExtension();
		boolean done = false;
		StringBuilder hash = null;

		while (!done) {
			switch (parser.next()) {
			case XmlPullParser.START_TAG:
				if (parser.getName().equals("photo"))
					hash = new StringBuilder();
				break;
			case XmlPullParser.TEXT:
				if (hash != null)
					hash.append(parser.getText());
				break;
			case XmlPullParser.END_TAG:
				if (parser.getName().equals("photo"))
					updateExtension.setAvatarHash(hash.toString());
				else if (parser.getName().equals(updateExtension.getElementName()))
					done = true;
				break;
			}
		}
		return updateExtension;
	}
}
