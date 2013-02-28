package org.yaxim.androidclient.util.crypto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class NoCrypto extends CryptoProvider {
    public static final String NAME = "";
    

    public static NoCrypto createInstance() {
        return new NoCrypto();
    }

    @Override
    public boolean isAvailable(Context context) {
        return false;
    }

    @Override
    public boolean selectSecretKey(Activity activity, PgpData pgpData) {
        return false;
    }

    @Override
    public boolean selectEncryptionKeys(Activity activity, String emails, PgpData pgpData) {
        return false;
    }

    @Override
    public long[] getSecretKeyIdsFromEmail(Context context, String email) {
        return null;
    }

    @Override
    public long[] getPublicKeyIdsFromEmail(Context context, String email) {
        return null;
    }

    @Override
    public boolean hasSecretKeyForEmail(Context context, String email) {
        return false;
    }

    @Override
    public boolean hasPublicKeyForEmail(Context context, String email) {
        return false;
    }

    @Override
    public String getUserId(Context context, long keyId) {
        return null;
    }

    @Override
    public boolean onActivityResult(Activity activity, int requestCode, int resultCode,
                                    android.content.Intent data, PgpData pgpData) {
        return false;
    }

    @Override
    public boolean onDecryptActivityResult(CryptoDecryptCallback callback, int requestCode,
            int resultCode, Intent data, PgpData pgpData) {
        return false;
    }

    @Override
    public boolean encrypt(Activity activity, String data, PgpData pgpData) {
        return false;
    }

    @Override
    public boolean decrypt(Fragment fragment, String data, PgpData pgpData) {
        return false;
    }

    @Override
    public boolean isEncrypted(String message) {
        return false;
    }

    @Override
    public boolean isSigned(String message) {
        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean test(Context context) {
        return true;
    }
}
