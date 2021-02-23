package md.intelectsoft.quickpos.Realm.localStorage.fiscalObjects.report;

import io.realm.RealmList;
import io.realm.RealmObject;

public class ReportByOperator extends RealmObject {
    private int userCode; // operator code
    private double dailyTotalBillsSum;
    private double totalSumService;
    private double soldCashDrawer;
    RealmList<VatSumByCode> vatSumByCodes;
    RealmList<ServiceOperationByOperator> serviceOperationByOperators;
    RealmList<DailyTotalBillSumByPayTypeByOperator> dailyTotalBillSumByPayTypeByOperators;

    public int getUserCode() {
        return userCode;
    }

    public void setUserCode(int userCode) {
        this.userCode = userCode;
    }

    public double getDailyTotalBillsSum() {
        return dailyTotalBillsSum;
    }

    public void setDailyTotalBillsSum(double dailyTotalBillsSum) {
        this.dailyTotalBillsSum = dailyTotalBillsSum;
    }

    public double getTotalSumService() {
        return totalSumService;
    }

    public void setTotalSumService(double totalSumService) {
        this.totalSumService = totalSumService;
    }

    public double getSoldCashDrawer() {
        return soldCashDrawer;
    }

    public void setSoldCashDrawer(double soldCashDrawer) {
        this.soldCashDrawer = soldCashDrawer;
    }

    public RealmList<VatSumByCode> getVatSumByCodes() {
        return vatSumByCodes;
    }

    public void setVatSumByCodes(RealmList<VatSumByCode> vatSumByCodes) {
        this.vatSumByCodes = vatSumByCodes;
    }

    public RealmList<ServiceOperationByOperator> getServiceOperationByOperators() {
        return serviceOperationByOperators;
    }

    public void setServiceOperationByOperators(RealmList<ServiceOperationByOperator> serviceOperationByOperators) {
        this.serviceOperationByOperators = serviceOperationByOperators;
    }

    public RealmList<DailyTotalBillSumByPayTypeByOperator> getDailyTotalBillSumByPayTypeByOperators() {
        return dailyTotalBillSumByPayTypeByOperators;
    }

    public void setDailyTotalBillSumByPayTypeByOperators(RealmList<DailyTotalBillSumByPayTypeByOperator> dailyTotalBillSumByPayTypeByOperators) {
        this.dailyTotalBillSumByPayTypeByOperators = dailyTotalBillSumByPayTypeByOperators;
    }
}
