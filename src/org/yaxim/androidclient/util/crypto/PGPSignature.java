package org.yaxim.androidclient.util.crypto;

import org.jivesoftware.smack.packet.PacketExtension;
import android.util.Log;

public class PGPSignature implements PacketExtension {
	public static final String NAMESPACE = "jabber:x:signed";
	public final String signature;

	private PGPSignature(String stringToSign, long keyid) {
		super();
		this.signature = OpenPGP.getSignature(stringToSign, keyid);
	}

	private PGPSignature(String sig) {
		super();
		this.signature = sig;
	}
	
	public static PGPSignature signString(String stringToSign, long keyid) {
		return new PGPSignature(stringToSign, keyid);
	}
	
	public static PGPSignature extensionFromSignature(String sig) {
		return new PGPSignature(sig);
	}

	@Override
	public String getElementName() {
		return "x";
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String toXML() {
		return "<x xmlns='" + PGPSignature.NAMESPACE + "'>" + signature + "</x>";
	}

}
