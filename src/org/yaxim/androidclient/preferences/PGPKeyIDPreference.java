package org.yaxim.androidclient.preferences;

import org.yaxim.androidclient.R;
import org.yaxim.androidclient.util.crypto.OpenPGP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class PGPKeyIDPreference extends Preference {
	private long mId;

	public PGPKeyIDPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PGPKeyIDPreference(Context context) {
		super(context);
	}

	/* (non-Javadoc)
	 * @see android.preference.Preference#onClick()
	 */
	@Override
	protected void onClick() {
		super.onClick();
		if (hasOpenPGP())
			OpenPGP.selectSecretKey((Activity)getContext());
		else
			OpenPGP.installOpenPGP((Activity)getContext());
	}
    /**
     * Saves the id to the {@link SharedPreferences}.
     * 
     * @param id The id to save
     */
	@SuppressLint("DefaultLocale") // just converting a hex num
    public void setId(long id) {
        final boolean wasBlocking = shouldDisableDependents();
		if (id == 0) {
			if (hasOpenPGP()) 
				setSummary(R.string.account_pgp_no_key);
			return;
		}
        mId = id;
        persistLong(id);
        final boolean isBlocking = shouldDisableDependents(); 
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
        setSummary(Integer.toHexString((int)id).toUpperCase());
    }
    /* (non-Javadoc)
	 * @see android.preference.Preference#onCreateView(android.view.ViewGroup)
	 */
	@Override
	protected View onCreateView(ViewGroup parent) {
		setId(getPersistedLong(0));
		return super.onCreateView(parent);
	}

	/**
     * Gets the id from the {@link SharedPreferences}.
     * 
     * @return The current preference value.
     */
    public long getId() {
        return mId;
    }    
	private boolean hasOpenPGP() {
		return OpenPGP.isAvailable(getContext());
	}
}
