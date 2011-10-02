package org.yaxim.androidclient.preferences;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import org.yaxim.androidclient.R;
import org.yaxim.androidclient.util.PreferenceConstants;

public class MainPrefs extends PreferenceActivity{
	  public void onCreate(Bundle savedInstanceState) {
			String theme = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceConstants.THEME, "dark");
			if (theme.equals("light")) {
				setTheme(R.style.LightTheme);
			} else {
				setTheme(R.style.DarkTheme);
			}
	        super.onCreate(savedInstanceState);
	        addPreferencesFromResource(R.xml.mainprefs);
	    }
	  
}
