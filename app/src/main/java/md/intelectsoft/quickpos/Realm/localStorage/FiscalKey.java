package md.intelectsoft.quickpos.Realm.localStorage;

import io.realm.RealmObject;

public class FiscalKey extends RealmObject {
    private byte[] key;

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }
}
