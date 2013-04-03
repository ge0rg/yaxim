package org.yaxim.androidclient.util.crypto;

import org.yaxim.androidclient.R;

public enum StatusSigned {
	unknown(R.string.signed_unknown, R.drawable.rel_signed_unknown),
	not(R.string.signed_not, 0),
	nokey(R.string.signed_nokey, R.drawable.rel_signed_nokey),
	/** signature succesfully validated */
	valid(R.string.signed_valid, R.drawable.rel_signed_valid),
	/** signature succesfully validated and we trust the key*/
	trusted(R.string.signed_trusted, R.drawable.rel_signed_trusted),
	invalid(R.string.signed_invalid, R.drawable.rel_signed_invalid);

	public final int textId;
	public final int drawableId;

	StatusSigned(int textId, int drawableId) {
		this.textId = textId;
		this.drawableId = drawableId;
	}
}
