package org.yaxim.androidclient.util;

import org.yaxim.androidclient.R;

public enum StatusSigned {
	signed_not(R.string.signed_not, 0),
	signed_nokey(R.string.signed_nokey, R.drawable.ic_signed_nokey),
	signed_valid(R.string.signed_valid, R.drawable.ic_signed_valid),
	signed_trusted(R.string.signed_trusted, R.drawable.ic_signed_trusted),
	signed_invalid(R.string.signed_invalid, 0);

	public final int textId;
	public final int drawableId;

	StatusSigned(int textId, int drawableId) {
		this.textId = textId;
		this.drawableId = drawableId;
	}
}
