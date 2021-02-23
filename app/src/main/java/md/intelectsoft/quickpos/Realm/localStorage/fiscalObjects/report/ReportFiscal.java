package md.intelectsoft.quickpos.Realm.localStorage.fiscalObjects.report;

import io.realm.RealmList;
import io.realm.RealmObject;

public class ReportFiscal extends RealmObject {
    private int reportType;
    private int shiftNumber; // nr_report
    private long dateTime;
    private RealmList<ReportCommerceService> reportService;

    public int getReportType() {
        return reportType;
    }

    public void setReportType(int reportType) {
        this.reportType = reportType;
    }

    public int getShiftNumber() {
        return shiftNumber;
    }

    public void setShiftNumber(int shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public RealmList<ReportCommerceService> getReportService() {
        return reportService;
    }

    public void setReportService(RealmList<ReportCommerceService> reportService) {
        this.reportService = reportService;
    }
}
