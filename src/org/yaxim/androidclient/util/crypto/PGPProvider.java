package org.yaxim.androidclient.util.crypto;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class PGPProvider implements PacketExtensionProvider {

	@Override
	public PacketExtension parseExtension(XmlPullParser xpp) throws Exception {
        int eventType = xpp.getEventType();
        Boolean inTag = false;
        while (eventType != XmlPullParser.END_DOCUMENT) {
         if (eventType == XmlPullParser.START_TAG 
        		&& "x".equals( xpp.getName() ) 
          		&& PGPSignature.NAMESPACE.equals(xpp.getNamespace()) ) {
          			inTag = true;
         } else if(inTag && eventType == XmlPullParser.TEXT) {
        	return new PGPSignature( xpp.getText() );
         } else {
         	inTag = false;
         }
         eventType = xpp.next();
        }
		return null;
	}

}
