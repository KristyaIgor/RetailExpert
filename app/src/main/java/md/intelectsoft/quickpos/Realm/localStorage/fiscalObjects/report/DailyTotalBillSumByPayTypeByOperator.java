package md.intelectsoft.quickpos.Realm.localStorage.fiscalObjects.report;

import io.realm.RealmObject;

public class DailyTotalBillSumByPayTypeByOperator extends RealmObject {
    private int paymentType;
    private int paymentCode;
    private double totalSum;

    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }

    public int getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(int paymentCode) {
        this.paymentCode = paymentCode;
    }

    public double getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(double totalSum) {
        this.totalSum = totalSum;
    }
}
