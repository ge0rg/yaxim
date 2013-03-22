package org.yaxim.androidclient.util.crypto;

import org.yaxim.androidclient.data.RosterProvider;
import org.yaxim.androidclient.data.RosterProvider.RosterConstants;

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
	private Context context;
	private static final String[] SIGNATURE_QUERY = new String[] {
		RosterConstants._ID,
		RosterConstants.JID,
		RosterConstants.STATUS_X_SIGNATURE,
		RosterConstants.PGPSIGNATURE,
		RosterConstants.STATUS_MESSAGE
	};
	
	
	public SignatureChecker(Context con) {
		context = con;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader cl = new CursorLoader(context, RosterProvider.CONTENT_URI, SIGNATURE_QUERY,
				null, null, null);
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.d("SignatureChecker", "onLoadFinished:" + data.toString());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d("SignatureChecker", "onLoaderReset:");
	}

}
