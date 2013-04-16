package org.yaxim.androidclient.preferences;

import org.yaxim.androidclient.util.crypto.OpenPGP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

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
		OpenPGP.selectSecretKey((Activity)getContext());
	}
    /**
     * Saves the id to the {@link SharedPreferences}.
     * 
     * @param id The id to save
     */
    public void setId(long id) {
        final boolean wasBlocking = shouldDisableDependents();
		if (id == 0) return;
        mId = id;
        persistLong(id);
        final boolean isBlocking = shouldDisableDependents(); 
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
        setSummaryToID(id);
    }
    /**
     * Gets the id from the {@link SharedPreferences}.
     * 
     * @return The current preference value.
     */
    public long getId() {
        return mId;
    }    
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		setSummaryToID(getPersistedLong(0));
	}
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setId(restoreValue ? getPersistedLong(mId) : 0);
    }	
	@SuppressLint("DefaultLocale") // just converting a hex num
	private void setSummaryToID(long id) {
		if (id != 0) setSummary(Integer.toHexString((int)id).toUpperCase());
	}

}
