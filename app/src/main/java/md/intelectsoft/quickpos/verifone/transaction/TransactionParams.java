package md.intelectsoft.quickpos.verifone.transaction;
/*
 *  author: Derrick
 *  Time: 2019/5/27 16:38
 */

public class TransactionParams {

    public static final int IMPORT_APP_SELECT = 1;
    public static final int IMPORT_CARD_CONFIRM_RESULT = 2;
    public static final int IMPORT_PIN = 3;
    public static final int IMPORT_CERT_CONFIRM_RESULT = 4;
    public static final int INPUT_ONLINE_RESULT = 5;


    private static TransactionParams instance;
    private String transactionAmount;
    private String transactionType;
    private String pan;
    private String expiredDate;

    private boolean isOnlinePin;
    private int retryTimes;

    private String date;
    private String referNum;

    private String esignData;
    private String esignWidth;
    private String esignHeight;
    private String conditionCode;
    private boolean esignUploadFlag;

    //QR data
    private String QRData = "QRData";

    public void setQRData(String QRData) {
        this.QRData = QRData;
    }

    public String getQRData() {
        return QRData;
    }

    public String getEsignData() {
        return esignData;
    }

    public void setEsignData(String esignData) {
        this.esignData = esignData;
    }

    public String getEsignWidth() {
        return esignWidth;
    }

    public void setEsignWidth(String esignWidth) {
        this.esignWidth = esignWidth;
    }

    public String getEsignHeight() {
        return esignHeight;
    }

    public void setEsignHeight(String esignHeight) {
        this.esignHeight = esignHeight;
    }

    public String getConditionCode() {
        return conditionCode;
    }

    public void setConditionCode(String conditionCode) {
        this.conditionCode = conditionCode;
    }

    public boolean isEsignUploadFlag() {
        return esignUploadFlag;
    }

    public void setEsignUploadFlag(boolean esignUploadFlag) {
        this.esignUploadFlag = esignUploadFlag;
    }

    private TransactionParams(){
    }

    public static TransactionParams getInstance(){
        if ( null == instance){
            instance = new TransactionParams();
        }
        return instance;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setReferNum(String referNum) {
        this.referNum = referNum;
    }

    public String getReferNum() {
        return referNum;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }
    public String getTransactionAmount() {
        return transactionAmount;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getPan() {
        return pan;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public boolean isOnlinePin() {
        return isOnlinePin;
    }

    public void setOnlinePin(boolean onlinePin) {
        isOnlinePin = onlinePin;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(String expiredDate) {
        this.expiredDate = expiredDate;
    }
}
