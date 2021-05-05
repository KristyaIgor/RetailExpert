package md.intelectsoft.quickpos.fisc.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FiscalBillInfo {
    @SerializedName("Date")
    @Expose
    private String date;
    @SerializedName("Discount")
    @Expose
    private Double discount;
    @SerializedName("ID")
    @Expose
    private String iD;
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
    private String paymantCode;
    @SerializedName("ShiftNumber")
    @Expose
    private String shiftNumber;
    @SerializedName("Summ")
    @Expose
    private Double summ;
    @SerializedName("TotalArticle")
    @Expose
    private Integer totalArticle;

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

    public String getID() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
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

    public String getPaymantCode() {
        return paymantCode;
    }

    public void setPaymantCode(String paymantCode) {
        this.paymantCode = paymantCode;
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
}
