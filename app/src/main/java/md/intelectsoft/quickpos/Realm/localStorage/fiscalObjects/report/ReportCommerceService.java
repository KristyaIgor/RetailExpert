package md.intelectsoft.quickpos.Realm.localStorage.fiscalObjects.report;

import io.realm.RealmList;
import io.realm.RealmObject;

public class ReportCommerceService extends RealmObject {
    private int dailyBillCounter; //nr_documents
    private double dailyTotalBillsSum; //total_operation
    private double totalSumService;
    private double soldCashDrawer;
    private RealmList<DailyTotalBillSumByPayType> dailyTotalBillSumByPayTypes;
    private RealmList<ServiceOperation> serviceOperations;
    private RealmList<ReportByOperator> reportByOperators;
    private RealmList<ReportByVat> reportByVats;


    public int getDailyBillCounter() {
        return dailyBillCounter;
    }

    public void setDailyBillCounter(int dailyBillCounter) {
        this.dailyBillCounter = dailyBillCounter;
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

    public RealmList<DailyTotalBillSumByPayType> getDailyTotalBillSumByPayTypes() {
        return dailyTotalBillSumByPayTypes;
    }

    public void setDailyTotalBillSumByPayTypes(RealmList<DailyTotalBillSumByPayType> dailyTotalBillSumByPayTypes) {
        this.dailyTotalBillSumByPayTypes = dailyTotalBillSumByPayTypes;
    }

    public RealmList<ServiceOperation> getServiceOperations() {
        return serviceOperations;
    }

    public void setServiceOperations(RealmList<ServiceOperation> serviceOperations) {
        this.serviceOperations = serviceOperations;
    }

    public RealmList<ReportByOperator> getReportByOperators() {
        return reportByOperators;
    }

    public void setReportByOperators(RealmList<ReportByOperator> reportByOperators) {
        this.reportByOperators = reportByOperators;
    }

    public RealmList<ReportByVat> getReportByVats() {
        return reportByVats;
    }

    public void setReportByVats(RealmList<ReportByVat> reportByVats) {
        this.reportByVats = reportByVats;
    }
}
