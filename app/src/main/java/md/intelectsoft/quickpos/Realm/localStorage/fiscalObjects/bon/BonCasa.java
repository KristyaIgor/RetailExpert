package md.intelectsoft.quickpos.Realm.localStorage.fiscalObjects.bon;

import io.realm.RealmList;
import io.realm.RealmObject;

public class BonCasa extends RealmObject {
    private String id;
    private int operationType;                          // vinzare / suplinire / extragere
    private int globalReceiptNumberFiscalMemory;        // global number receip in fiscal memory  / nr_bon
    private int shiftNumber;                            // z report number
    private int operatorCode;
    private int artNumber;
    private long dateTime;
    private int paymentCode;
    private double sum;
    private double discount;
    private RealmList<BonCasaItem> bonCasaItems;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOperationType() {
        return operationType;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
    }

    public int getGlobalReceiptNumberFiscalMemory() {
        return globalReceiptNumberFiscalMemory;
    }

    public void setGlobalReceiptNumberFiscalMemory(int globalReceiptNumberFiscalMemory) {
        this.globalReceiptNumberFiscalMemory = globalReceiptNumberFiscalMemory;
    }

    public int getShiftNumber() {
        return shiftNumber;
    }

    public void setShiftNumber(int shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    public int getOperatorCode() {
        return operatorCode;
    }

    public void setOperatorCode(int operatorCode) {
        this.operatorCode = operatorCode;
    }

    public int getArtNumber() {
        return artNumber;
    }

    public void setArtNumber(int artNumber) {
        this.artNumber = artNumber;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public int getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(int paymentCode) {
        this.paymentCode = paymentCode;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public RealmList<BonCasaItem> getBonCasaItems() {
        return bonCasaItems;
    }

    public void setBonCasaItems(RealmList<BonCasaItem> bonCasaItems) {
        this.bonCasaItems = bonCasaItems;
    }
}
