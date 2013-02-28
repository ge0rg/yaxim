package org.yaxim.androidclient.util.crypto;

/*
 * Inspired by k9
 * https://github.com/k9mail/
*/

public class PgpData {
    protected long mEncryptionKeyIds[] = null;
    protected long mSignatureKeyId = 0;
    protected String mSignatureUserId = null;
    public enum SignatureStatus {
        failed, success, unknown };
    protected SignatureStatus mSignatureStatus = SignatureStatus.unknown;
    protected String mDecryptedData = null;
    protected String mEncryptedData = null;

    public void setSignatureKeyId(long keyId) {
        mSignatureKeyId = keyId;
    }

    public long getSignatureKeyId() {
        return mSignatureKeyId;
    }

    public void setEncryptionKeys(long keyIds[]) {
        mEncryptionKeyIds = keyIds;
    }

    public long[] getEncryptionKeys() {
        return mEncryptionKeyIds;
    }

    public boolean hasSignatureKey() {
        return mSignatureKeyId != 0;
    }

    public boolean hasEncryptionKeys() {
        return (mEncryptionKeyIds != null) && (mEncryptionKeyIds.length > 0);
    }

    public String getEncryptedData() {
        return mEncryptedData;
    }

    public void setEncryptedData(String data) {
        mEncryptedData = data;
    }

    public String getDecryptedData() {
        return mDecryptedData;
    }

    public void setDecryptedData(String data) {
        mDecryptedData = data;
    }

    public void setSignatureUserId(String userId) {
        mSignatureUserId = userId;
    }

    public String getSignatureUserId() {
        return mSignatureUserId;
    }

    public SignatureStatus getSignatureStatus() {
        return mSignatureStatus;
    }

    public void setSignatureStatus(SignatureStatus status) {
    	mSignatureStatus = status;
    }

}