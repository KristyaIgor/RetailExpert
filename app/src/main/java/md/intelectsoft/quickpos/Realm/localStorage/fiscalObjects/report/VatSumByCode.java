package md.intelectsoft.quickpos.Realm.localStorage.fiscalObjects.report;

import io.realm.RealmObject;

public class VatSumByCode extends RealmObject {
    private int vatCode;
    private int vatValue;
    private double sum;

    public int getVatCode() {
        return vatCode;
    }

    public void setVatCode(int vatCode) {
        this.vatCode = vatCode;
    }

    public int getVatValue() {
        return vatValue;
    }

    public void setVatValue(int vatValue) {
        this.vatValue = vatValue;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }
}
