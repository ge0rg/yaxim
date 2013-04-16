package org.yaxim.androidclient.util.crypto;
/*
 * Inspired by k9
 * https://github.com/k9mail/
*/
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yaxim.androidclient.R;
import org.yaxim.androidclient.data.RosterProvider;
import org.yaxim.androidclient.data.RosterProvider.RosterConstants;
import org.yaxim.androidclient.preferences.AccountPrefs;
import org.yaxim.androidclient.preferences.PGPKeyIDPreference;
import org.yaxim.androidclient.util.ArrayUtils;
import org.yaxim.androidclient.util.PreferenceConstants;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;


/**
 * APG integration.
 */
public class OpenPGP  {
    public interface CryptoDecryptCallback {
        void onDecryptDone(PgpData pgpData);
    }
	private OpenPGP() {}
    private static final String PGP_PACKAGE_NAME = "org.sufficientlysecure.keychain";
    /** Check for unknown Version in isAvailable */
    private static final int MIN_REQUIRED_VERSION = Integer.MIN_VALUE;

    public static final String AUTHORITY = PGP_PACKAGE_NAME;
    public static final Uri CONTENT_URI_SECRET_KEY_RING_BY_KEY_ID =
        Uri.parse("content://" + AUTHORITY + "/key_rings/secret/key_id/");
    public static final Uri CONTENT_URI_SECRET_KEY_RING_BY_EMAILS =
        Uri.parse("content://" + AUTHORITY + "/key_rings/secret/emails/");

    public static final Uri CONTENT_URI_PUBLIC_KEY_RING_BY_KEY_ID =
        Uri.parse("content://" + AUTHORITY + "/key_rings/public/key_id/");
    public static final Uri CONTENT_URI_PUBLIC_KEY_RING_BY_EMAILS =
        Uri.parse("content://" + AUTHORITY + "/key_rings/public/emails/");

    public static class IntentNames {
    	/** OpenPGP intent name */
        public static final String DECRYPT_AND_RETURN = PGP_PACKAGE_NAME + ".action.DECRYPT_AND_RETURN";
        public static final String SELECT_SECRET_KEY = PGP_PACKAGE_NAME + ".action.SELECT_SECRET_KEYRING";
        
        public static final String DECRYPT = PGP_PACKAGE_NAME + ".intent.DECRYPT";
        public static final String ENCRYPT = PGP_PACKAGE_NAME + ".intent.ENCRYPT";
        public static final String DECRYPT_FILE = PGP_PACKAGE_NAME + ".intent.DECRYPT_FILE";
        public static final String ENCRYPT_FILE = PGP_PACKAGE_NAME + ".intent.ENCRYPT_FILE";
        public static final String ENCRYPT_AND_RETURN = PGP_PACKAGE_NAME + ".intent.ENCRYPT_AND_RETURN";
        public static final String SELECT_PUBLIC_KEYS = PGP_PACKAGE_NAME + ".intent.SELECT_PUBLIC_KEYS";
    }

    public static final String EXTRA_TEXT = "text";
    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_ERROR = "error";
    public static final String EXTRA_DECRYPTED_MESSAGE = "decryptedMessage";
    public static final String EXTRA_ENCRYPTED_MESSAGE = "encryptedMessage";
    public static final String EXTRA_SIGNATURE = "signature";
    public static final String EXTRA_SIGNATURE_KEY_ID = "signatureKeyId";
    public static final String EXTRA_SIGNATURE_USER_ID = "signatureUserId";
    public static final String EXTRA_SIGNATURE_SUCCESS = "signatureSuccess";
    public static final String EXTRA_SIGNATURE_UNKNOWN = "signatureUnknown";
    public static final String EXTRA_USER_ID = "userId";
    public static final String EXTRA_KEY_ID = "keyId";
    public static final String EXTRA_MASTER_KEY_ID = "masterKeyId";
    public static final String EXTRA_ENCRYPTION_KEY_IDS = "encryptionKeyIds";
    public static final String EXTRA_SELECTION = "selection";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_INTENT_VERSION = "intentVersion";

    

    public static final String INTENT_VERSION = "1";

    // Note: The support package only allows us to use the lower 16 bits of a request code.
    public static final int DECRYPT_MESSAGE = 0x0000A001;
    public static final int ENCRYPT_MESSAGE = 0x0000A002;
    public static final int SELECT_PUBLIC_KEYS = 0x0000A003;

    public static Pattern PGP_MESSAGE =
        Pattern.compile(".*?(-----BEGIN PGP MESSAGE-----.*?-----END PGP MESSAGE-----).*",
                        Pattern.DOTALL);

    public static Pattern PGP_SIGNED_MESSAGE =
        Pattern.compile(".*?(-----BEGIN PGP SIGNED MESSAGE-----.*?-----BEGIN PGP SIGNATURE-----.*?-----END PGP SIGNATURE-----).*",
                        Pattern.DOTALL);

    private enum CallType{
    	/** indicates a signature check */
    	checkSig,
    	/** indicates, that the original call was an encryption request */
    	encryptMsg,
    	/** indicates, that the original call was an decryption request */
    	decryptMsg,
    	/** indicates a request to select private key */
    	selectPrivKey
    }
    /**
     * Check whether APG is installed and at a high enough version.
     *
     * @param context
     * @return whether a suitable version of APG was found
     */
    public static boolean isAvailable(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(PGP_PACKAGE_NAME, 0);
            if (pi.versionCode >= MIN_REQUIRED_VERSION) {
                return true;
            }
			Toast.makeText(context, R.string.error_pgp_version_not_supported, Toast.LENGTH_SHORT).show();
        } catch (NameNotFoundException e) {
            // not found
        }
        return false;
    }

    /**
     * Start the decrypt activity.
     * @param activity 
     * @param jId The jabberId - an e-mail
     *
     * @return success or failure
     */
    public static boolean checkStatusSignature(Activity activity, String jId) {
    	// get from database
    	Cursor cursor = activity.getContentResolver().query(
    					RosterProvider.CONTENT_URI, 
    					new String[] {RosterConstants.STATUS_MESSAGE, RosterConstants.STATUS_X_SIGNATURE}, 
    					RosterConstants.JID + "=?",
    					new String[] {jId},
    					null);
    	if (cursor.getCount() != 1) {
    		setStatus(activity, jId, StatusSigned.invalid);
    		return false;
    	}
    	cursor.moveToFirst();
    	String statusMessage = cursor.getString(0);
    	String sig = cursor.getString(1);    	
    	// pgp string bauen
    	String pgpData = createPgpData(statusMessage, sig);
    	// define call id
    	int callId = createCallId(CallType.checkSig, jId);
    	// call decrypt
    	return decrypt(activity, pgpData, callId);
    }

    private static class Pair<First,Second>{
    	
		public final First first;
		public final Second second;

		public Pair(First a, Second b) {
			first = a;
			second = b;
		}
    }
    
    private static AtomicInteger callCounter = new AtomicInteger(DECRYPT_MESSAGE);
    private static Map<Integer, Pair<CallType, String>> callCache = 
    				Collections.synchronizedMap(new HashMap<Integer, Pair<CallType, String>>());
    
    /********************************************************************************
	 * created by kurella at 04.04.2013 <br>
	 * 
     * @param callType
     * @param data
	 * 
	 * @return
	 *******************************************************************************/
	private static int createCallId(CallType callType, String data) {
		int id = callCounter.getAndIncrement();
		callCache.put(Integer.valueOf(id), new Pair<CallType, String>(callType, data));
		return id;
	}

	/********************************************************************************
	 * created by kurella at 04.04.2013 <br>
	 * 
	 * @param pgpMessage
	 * @param sig
	 * @return
	 *******************************************************************************/
	private static String createPgpData(String pgpMessage, String sig) {
		if (pgpMessage == null) pgpMessage = "";
		return "-----BEGIN PGP SIGNED MESSAGE-----\n"
						+ "Hash: SHA256\n\n"
						+ pgpMessage
						+ "\n-----BEGIN PGP SIGNATURE-----\n"
						+ "Version: APG v1.0.8\n\n"
						+ sig
						+ "\n-----END PGP SIGNATURE-----";
	}

	/********************************************************************************
	 * created by kurella at 03.04.2013 <br>
	 * @param activity 
	 * @param jId 
	 * @param signedStatus
	 *******************************************************************************/
	private static void setStatus(ContextWrapper activity, String jId, StatusSigned signedStatus) {
		// update database
		ContentValues values = new ContentValues();
		values.put(RosterConstants.PGPSIGNATURE, signedStatus.name());
		// ignore double update
		activity.getContentResolver().update(RosterProvider.CONTENT_URI, values, RosterConstants.JID + " = ?", new String[] { jId });
	}

	/**
     * Select the signature key.
     *
     * @param activity
     * @param pgpData
     * @return success or failure
     */
    public static boolean selectSecretKey(Activity activity) {
    	int pCallId = createCallId(CallType.selectPrivKey, null);

    	Intent intent = new Intent(IntentNames.SELECT_SECRET_KEY);
        intent.putExtra(EXTRA_INTENT_VERSION, INTENT_VERSION);
        try {
            activity.startActivityForResult(intent, pCallId);
            return true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity,
                           R.string.error_activity_not_found,
                           Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Select encryption keys.
     *
     * @param activity
     * @param emails The emails that should be used for preselection.
     * @param pgpData
     * @return success or failure
     */
    public static boolean selectEncryptionKeys(Activity activity, String emails, PgpData pgpData) {
        Intent intent = new Intent(IntentNames.SELECT_PUBLIC_KEYS);
        intent.putExtra(EXTRA_INTENT_VERSION, INTENT_VERSION);
        long[] initialKeyIds = null;
        if (!pgpData.hasEncryptionKeys()) {
            List<Long> keyIds = new ArrayList<Long>();
            if (pgpData.hasSignatureKey()) {
                keyIds.add(pgpData.getSignatureKeyId());
            }

            try {
                Uri contentUri = Uri.withAppendedPath(
                                     OpenPGP.CONTENT_URI_PUBLIC_KEY_RING_BY_EMAILS,
                                     emails);
                Cursor c = activity.getContentResolver().query(contentUri,
                           new String[] { "master_key_id" },
                           null, null, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        keyIds.add(c.getLong(0));
                    }
                }

                if (c != null) {
                    c.close();
                }
            } catch (SecurityException e) {
                Toast.makeText(activity,
                               activity.getResources().getString(R.string.insufficient_pgp_permissions),
                               Toast.LENGTH_LONG).show();
            }
            if (!keyIds.isEmpty()) {
                initialKeyIds = new long[keyIds.size()];
                for (int i = 0, size = keyIds.size(); i < size; ++i) {
                    initialKeyIds[i] = keyIds.get(i);
                }
            }
        } else {
            initialKeyIds = pgpData.getEncryptionKeys();
        }
        intent.putExtra(OpenPGP.EXTRA_SELECTION, initialKeyIds);
        try {
            activity.startActivityForResult(intent, OpenPGP.SELECT_PUBLIC_KEYS);
            return true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity,
                           R.string.error_activity_not_found,
                           Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Get secret key ids based on a given email.
     *
     * @param context
     * @param email The email in question.
     * @return key ids
     */
    public static long[] getSecretKeyIdsFromEmail(Context context, String email) {
        long ids[] = null;
        try {
            Uri contentUri = Uri.withAppendedPath(OpenPGP.CONTENT_URI_SECRET_KEY_RING_BY_EMAILS,
                                                  email);
            Cursor c = context.getContentResolver().query(contentUri,
                       new String[] { "master_key_id" },
                       null, null, null);
            if (c != null && c.getCount() > 0) {
                ids = new long[c.getCount()];
                while (c.moveToNext()) {
                    ids[c.getPosition()] = c.getLong(0);
                }
            }

            if (c != null) {
                c.close();
            }
        } catch (SecurityException e) {
            Toast.makeText(context,
                           context.getResources().getString(R.string.insufficient_pgp_permissions),
                           Toast.LENGTH_LONG).show();
        }

        return ids;
    }

    /**
     * Get public key ids based on a given email.
     *
     * @param context
     * @param email The email in question.
     * @return key ids
     */
    public static long[] getPublicKeyIdsFromEmail(Context context, String email) {
        long ids[] = null;
        try {
            Uri contentUri = Uri.withAppendedPath(OpenPGP.CONTENT_URI_PUBLIC_KEY_RING_BY_EMAILS, email);
            Cursor c = context.getContentResolver().query(contentUri,
                       new String[] { "master_key_id" }, null, null, null);
            if (c == null) {
                Toast.makeText(context,
                        context.getResources().getString(R.string.pgp_error),
                        Toast.LENGTH_LONG).show();
            }
            if (c.getCount() > 0) {
                ids = new long[c.getCount()];
                while (c.moveToNext()) {
                    ids[c.getPosition()] = c.getLong(0);
                }
            }
            if (c != null) {
                c.close();
            }
        } catch (SecurityException e) {
            Toast.makeText(context,
                           context.getResources().getString(R.string.insufficient_pgp_permissions),
                           Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            Toast.makeText(context,
                    e.toString(),
                    Toast.LENGTH_LONG).show();
        }
        return ids;
    }

    /**
     * Find out if a given email has a secret key.
     *
     * @param context
     * @param email The email in question.
     * @return true if there is a secret key for this email.
     */
    public static boolean hasSecretKeyForEmail(Context context, String email) {
        try {
            Uri contentUri = Uri.withAppendedPath(OpenPGP.CONTENT_URI_SECRET_KEY_RING_BY_EMAILS, email);
            Cursor c = context.getContentResolver().query(contentUri,
                    new String[] { "master_key_id" }, null, null, null);
            if (c != null && c.getCount() > 0) {
                c.close();
                return true;
            }
            if (c != null) {
                c.close();
            }
        } catch (SecurityException e) {
            Toast.makeText(context,
                    context.getResources().getString(R.string.insufficient_pgp_permissions),
                    Toast.LENGTH_LONG).show();
        }
        return false;
    }

    /**
     * Find out if a given email has a public key.
     *
     * @param context
     * @param email The email in question.
     * @return true if there is a public key for this email.
     */
    public static boolean hasPublicKeyForEmail(Context context, String email) {
        try {
            Uri contentUri = Uri.withAppendedPath(OpenPGP.CONTENT_URI_PUBLIC_KEY_RING_BY_EMAILS, email);
            Cursor c = context.getContentResolver().query(contentUri,
                    new String[] { "master_key_id" }, null, null, null);
            if (c != null && c.getCount() > 0) {
                c.close();
                return true;
            }
            if (c != null) {
                c.close();
            }
        } catch (SecurityException e) {
            Toast.makeText(context,
                    context.getResources().getString(R.string.insufficient_pgp_permissions),
                    Toast.LENGTH_LONG).show();
        }
        return false;
    }

    /**
     * Get the user id based on the key id.
     *
     * @param context
     * @param keyId
     * @return user id
     */
    public static String getUserId(Context context, long keyId) {
        String userId = null;
        try {
            Uri contentUri = ContentUris.withAppendedId(
                                 OpenPGP.CONTENT_URI_SECRET_KEY_RING_BY_KEY_ID,
                                 keyId);
            Cursor c = context.getContentResolver().query(contentUri,
                       new String[] { "user_id" },
                       null, null, null);
            if (c != null && c.moveToFirst()) {
                userId = c.getString(0);
            }

            if (c != null) {
                c.close();
            }
        } catch (SecurityException e) {
            Toast.makeText(context,
                           context.getResources().getString(R.string.insufficient_pgp_permissions),
                           Toast.LENGTH_LONG).show();
        }

        if (userId == null) {
            userId = context.getString(R.string.unknown_crypto_signature_user_id);
        }
        return userId;
    }

//    /**
//     * Handle the activity results that concern us.
//     *
//     * @param activity
//     * @param requestCode
//     * @param resultCode
//     * @param data
//     * @return handled or not
//     */
//    public static boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
//    	PgpData pgpData = new PgpData();
//        switch (requestCode) {
//        case Apg.SELECT_SECRET_KEY:
//            if (resultCode != Activity.RESULT_OK || data == null) {
//                break;
//            }
//            pgpData.setSignatureKeyId(data.getLongExtra(Apg.EXTRA_KEY_ID, 0));
//            pgpData.setSignatureUserId(data.getStringExtra(Apg.EXTRA_USER_ID));
////            ((MessageCompose) activity).updateEncryptLayout();
//            break;
//
//        case Apg.SELECT_PUBLIC_KEYS:
//            if (resultCode != Activity.RESULT_OK || data == null) {
//                pgpData.setEncryptionKeys(null);
////                ((MessageCompose) activity).onEncryptionKeySelectionDone();
//                break;
//            }
//            pgpData.setEncryptionKeys(data.getLongArrayExtra(Apg.EXTRA_SELECTION));
////            ((MessageCompose) activity).onEncryptionKeySelectionDone();
//            break;
//
//        case Apg.ENCRYPT_MESSAGE:
//            if (resultCode != Activity.RESULT_OK || data == null) {
//                pgpData.setEncryptionKeys(null);
////                ((MessageCompose) activity).onEncryptDone();
//                break;
//            }
//            pgpData.setEncryptedData(data.getStringExtra(Apg.EXTRA_ENCRYPTED_MESSAGE));
//            // this was a stupid bug in an earlier version, just gonna leave this in for an APG
//            // version or two
//            if (pgpData.getEncryptedData() == null) {
//                pgpData.setEncryptedData(data.getStringExtra(Apg.EXTRA_DECRYPTED_MESSAGE));
//            }
//            if (pgpData.getEncryptedData() != null) {
////                ((MessageCompose) activity).onEncryptDone();
//            }
//            break;
//
//        default:
//            return false;
//        }
//        return true;
//    }

    public static boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		Pair<CallType, String> reqData = callCache.get(Integer.valueOf(requestCode));
		if (reqData == null)
			return false;
		if (resultCode != Activity.RESULT_OK || data == null) {
			setStatus(activity, reqData.second, StatusSigned.invalid);
			return true;
		}
		switch (reqData.first) {
			case checkSig:
				long userId = data.getLongExtra(OpenPGP.EXTRA_SIGNATURE_KEY_ID, 0);
				long[] publicKeys = getPublicKeyIdsFromEmail(activity, reqData.second);
				if (!ArrayUtils.contains(publicKeys, userId)) {
					setStatus(activity, reqData.second, StatusSigned.nokey);
					return true;
				}
				if (data.getBooleanExtra(
						OpenPGP.EXTRA_SIGNATURE_SUCCESS, false))
					setStatus(activity, reqData.second, StatusSigned.valid);
				// TODO for more granular status
				// else if (data.getBooleanExtra(Apg.EXTRA_SIGNATURE_UNKNOWN,
				// false))
				// setStatus(activity, reqData.second, StatusSigned.invalid);
				else
					setStatus(activity, reqData.second, StatusSigned.invalid);
				break;
			case decryptMsg:
//                pgpData.setSignatureUserId(data.getStringExtra(Apg.EXTRA_SIGNATURE_USER_ID));
//                pgpData.setSignatureKeyId(data.getLongExtra(Apg.EXTRA_SIGNATURE_KEY_ID, 0));
//                pgpData.setDecryptedData(data.getStringExtra(Apg.EXTRA_DECRYPTED_MESSAGE));
				break;
			case encryptMsg:
				break;
			case selectPrivKey:
				long keyID = data.getLongExtra(EXTRA_MASTER_KEY_ID, 0);
				((AccountPrefs)activity).setPGPKeyID(keyID);
				return true;
		}
        return true;
    }
    
    /**
     * Start the encrypt activity.
     *
     * @param activity
     * @param data
     * @param pgpData
     * @return success or failure
     */
    public static boolean encrypt(Activity activity, String data, PgpData pgpData) {
        Intent intent = new Intent(IntentNames.ENCRYPT_AND_RETURN);
        intent.putExtra(EXTRA_INTENT_VERSION, INTENT_VERSION);
        intent.setType("text/plain");
        intent.putExtra(OpenPGP.EXTRA_TEXT, data);
        intent.putExtra(OpenPGP.EXTRA_ENCRYPTION_KEY_IDS, pgpData.getEncryptionKeys());
        intent.putExtra(OpenPGP.EXTRA_SIGNATURE_KEY_ID, pgpData.getSignatureKeyId());
        try {
            activity.startActivityForResult(intent, OpenPGP.ENCRYPT_MESSAGE);
            return true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity,
                           R.string.error_activity_not_found,
                           Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Start the decrypt activity.
     * @param data the ready to send pgp data
     * @param pCallId pregenetated original request identifier
     *
     * @return success or failure
     */
    public static boolean decrypt(Activity activity, String data, int pCallId) {
    	if (data == null) {
    		return false;
    	}
        Intent intent = new Intent(IntentNames.DECRYPT_AND_RETURN);
        intent.putExtra(EXTRA_INTENT_VERSION, INTENT_VERSION);
        intent.setType("text/plain");
        try {
            intent.putExtra(EXTRA_TEXT, data);
            activity.startActivityForResult(intent, pCallId);
            return true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.error_activity_not_found, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean isEncrypted(String message) {
        Matcher matcher = PGP_MESSAGE.matcher(message);
        return matcher.matches();
    }

    public static boolean isSigned(String message) {
        Matcher matcher = PGP_SIGNED_MESSAGE.matcher(message);
        return matcher.matches();
    }

    /**
     * Test the APG installation.
     *
     * @return success or failure
     */
    public static boolean test(Context context) {
        if (!isAvailable(context)) {
            return false;
        }

        try {
            // try out one content provider to check permissions
            Uri contentUri = ContentUris.withAppendedId(
                                 OpenPGP.CONTENT_URI_SECRET_KEY_RING_BY_KEY_ID,
                                 12345);
            Cursor c = context.getContentResolver().query(contentUri,
                       new String[] { "user_id" },
                       null, null, null);
            if (c != null) {
                c.close();
            }
        } catch (SecurityException e) {
            // if there was a problem, then let the user know, this will not stop Yaxim/APG from
            // working, but some features won't be available, so we can still return "true"
            Toast.makeText(context,
                           context.getResources().getString(R.string.insufficient_pgp_permissions),
                           Toast.LENGTH_LONG).show();
        }

        return true;
    }

	public static void installOpenPGP(Activity context) {
		final String appName = "org.sufficientlysecure.keychain";
		try {
		    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appName)));
		} catch (android.content.ActivityNotFoundException anfe) {
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="+appName)));
		}	}
}