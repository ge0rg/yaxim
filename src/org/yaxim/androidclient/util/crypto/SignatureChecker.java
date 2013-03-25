package org.yaxim.androidclient.util.crypto;

import org.yaxim.androidclient.data.RosterProvider;
import org.yaxim.androidclient.data.RosterProvider.RosterConstants;

import android.app.Activity;
import android.app.Application;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


//return "-----BEGIN PGP SIGNED MESSAGE-----\n" + "Hash: SHA256\n\n"
//+ s + "\n-----BEGIN PGP SIGNATURE-----\n"
//+ "Version: APG v1.0.8\n\n" + ((PGPSignature) xs).signature
//+ "\n-----END PGP SIGNATURE-----";			
//if (apgAvailable) {
//mPgpData = new PgpData();
//Apg.getInstance().decrypt(MainWindow.this, statusString, mPgpData);

public class SignatureChecker implements
	LoaderCallbacks<Cursor> {
	private Activity activity;
	private static final String[] SIGNATURE_QUERY = new String[] {
		RosterConstants._ID,
		RosterConstants.JID,
		RosterConstants.STATUS_X_SIGNATURE,
		RosterConstants.PGPSIGNATURE,
		RosterConstants.STATUS_MESSAGE
	};
	
	
	public SignatureChecker(Activity act) {
		activity = act;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(activity.getApplicationContext(),
				RosterProvider.CONTENT_URI, SIGNATURE_QUERY,
				RosterConstants.PGPSIGNATURE + " = '" + StatusSigned.unknown.name() + "'",
				null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int jidIdx = cursor.getColumnIndex(RosterConstants.JID);
		int pgpSigIxd = cursor.getColumnIndex(RosterConstants.PGPSIGNATURE);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			StatusSigned statSig = StatusSigned.valueOf( cursor.getString(pgpSigIxd) );
			String jid = cursor.getString(jidIdx);
			Log.d("SignatureChecker", jid + ":" + statSig.name());
			cursor.moveToNext();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d("SignatureChecker", "onLoaderReset:");
	}

}
