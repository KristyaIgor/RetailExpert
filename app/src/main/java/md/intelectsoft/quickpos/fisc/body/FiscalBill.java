package md.intelectsoft.quickpos.fisc.body;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FiscalBill {
    @SerializedName("Address")
    @Expose
    private String address;
    @SerializedName("BillItems")
    @Expose
    private List<BillItem> billItems = null;
    @SerializedName("Client")
    @Expose
    private String client;
    @SerializedName("Change")
    @Expose
    private double change;
    @SerializedName("Date")
    @Expose
    private String date;
    @SerializedName("Discount")
    @Expose
    private Double discount;
    @SerializedName("FiscalNumber")
    @Expose
    private String fiscalNumber;
    @SerializedName("FreeTextFooter")
    @Expose
    private String freeTextFooter;
    @SerializedName("FreeTextHeader")
    @Expose
    private String freeTextHeader;
    @SerializedName("ID")
    @Expose
    private String iD;
    @SerializedName("IDNO")
    @Expose
    private String iDNO;
    @SerializedName("Number")
    @Expose
    private String number;
    @SerializedName("OperationType")
    @Expose
    private Integer operationType;
    @SerializedName("OperatorCode")
    @Expose
    private String operatorCode;
    @SerializedName("PaymantCode")
    @Expose
    private Integer paymantCode;
    @SerializedName("PaymantType")
    @Expose
    private String paymantType;
    @SerializedName("ShiftNumber")
    @Expose
    private String shiftNumber;
    @SerializedName("Summ")
    @Expose
    private Double summ;
    @SerializedName("TotalArticle")
    @Expose
    private Integer totalArticle;
    @SerializedName("User")
    @Expose
    private String user;
    @SerializedName("Workplace")
    @Expose
    private String workplace;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<BillItem> getBillItems() {
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getFiscalNumber() {
        return fiscalNumber;
    }

    public void setFiscalNumber(String fiscalNumber) {
        this.fiscalNumber = fiscalNumber;
    }

    public String getFreeTextFooter() {
        return freeTextFooter;
    }

    public void setFreeTextFooter(String freeTextFooter) {
        this.freeTextFooter = freeTextFooter;
    }

    public String getFreeTextHeader() {
        return freeTextHeader;
    }

    public void setFreeTextHeader(String freeTextHeader) {
        this.freeTextHeader = freeTextHeader;
    }

    public String getID() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
    }

    public String getIDNO() {
        return iDNO;
    }

    public void setIDNO(String iDNO) {
        this.iDNO = iDNO;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Integer getOperationType() {
        return operationType;
    }

    public void setOperationType(Integer operationType) {
        this.operationType = operationType;
    }

    public String getOperatorCode() {
        return operatorCode;
    }

    public void setOperatorCode(String operatorCode) {
        this.operatorCode = operatorCode;
    }

    public Integer getPaymantCode() {
        return paymantCode;
    }

    public void setPaymantCode(Integer paymantCode) {
        this.paymantCode = paymantCode;
    }

    public String getPaymantType() {
        return paymantType;
    }

    public void setPaymantType(String paymantType) {
        this.paymantType = paymantType;
    }

    public String getShiftNumber() {
        return shiftNumber;
    }

    public void setShiftNumber(String shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    public Double getSumm() {
        return summ;
    }

    public void setSumm(Double summ) {
        this.summ = summ;
    }

    public Integer getTotalArticle() {
        return totalArticle;
    }

    public void setTotalArticle(Integer totalArticle) {
        this.totalArticle = totalArticle;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }
}
