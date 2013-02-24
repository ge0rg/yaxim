package org.yaxim.androidclient.util;

import org.yaxim.androidclient.R;

public enum StatusSigned {
	signed_not(R.string.signed_not, R.drawable.ic_signed_not),
	signed_nokey(R.string.signed_nokey, R.drawable.ic_signed_nokey),
	signed_valid(R.string.signed_valid, R.drawable.ic_signed_valid),
	signed_trusted(R.string.signed_trusted, R.drawable.ic_signed_trusted),
	signed_invalid(R.string.signed_invalid, R.drawable.ic_signed_invalid);

	private final int textId;
	private final int drawableId;

	StatusSigned(int textId, int drawableId) {
		this.textId = textId;
		this.drawableId = drawableId;
	}

	public int getTextId() {
		return textId;
	}

	public int getDrawableId() {
		return drawableId;
	}

	public String toString() {
		return name();
	}

	public static StatusMode fromString(String status) {
		return StatusMode.valueOf(status);
	}

}
