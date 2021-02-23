package md.intelectsoft.quickpos.Realm.localStorage;
import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Igor on 02.10.2019
 */

public class Bill extends RealmObject {
    private String id;
    private String shiftId;
    private int operationType;
    private int shiftReceiptNumSoftware;
    private int shiftReceiptFiscalDevice;
    private int receiptNumFiscalMemory;
    private String discountCardId;
    private String discountCardNumber;
    private String deviceId;
    private long createDate;
    private int state;                      // 0 - opened , 1 - closed
    private boolean isDeleted;
    private String currentSoftwareVersion;
    private String userId;
    private String userName;
    private int userCode;                //OperatorCode
    private double totalDiscount;
    private double totalSum;
    private RealmList<BillString> billStrings;
    private RealmList<BillPaymentType> billPaymentTypes;
    private String closedBy;
    private String closedByName;
    private long closeDate;
    private String lastEditAuthor;
    private long lastEditDate;
    private int inProcessOfSync;            //0 - este sincronizat cu succes , 1 - e in proces , 2 - nu este sincronizat
    private boolean expanded;
    private boolean isSynchronized;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public int getShiftReceiptNumSoftware() {
        return shiftReceiptNumSoftware;
    }

    public void setShiftReceiptNumSoftware(int shiftReceiptNumSoftware) {
        this.shiftReceiptNumSoftware = shiftReceiptNumSoftware;
    }

    public int getReceiptNumFiscalMemory() {
        return receiptNumFiscalMemory;
    }

    public void setReceiptNumFiscalMemory(int receiptNumFiscalMemory) {
        this.receiptNumFiscalMemory = receiptNumFiscalMemory;
    }

    public String getDiscountCardId() {
        return discountCardId;
    }

    public void setDiscountCardId(String discountCardId) {
        this.discountCardId = discountCardId;
    }

    public String getDiscountCardNumber() {
        return discountCardNumber;
    }

    public void setDiscountCardNumber(String discountCardNumber) {
        this.discountCardNumber = discountCardNumber;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(long closeDate) {
        this.closeDate = closeDate;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getCurrentSoftwareVersion() {
        return currentSoftwareVersion;
    }

    public void setCurrentSoftwareVersion(String currentSoftwareVersion) {
        this.currentSoftwareVersion = currentSoftwareVersion;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public double getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(double totalSum) {
        this.totalSum = totalSum;
    }

    public boolean isSynchronized() {
        return isSynchronized;
    }

    public void setSynchronized(boolean aSynchronized) {
        isSynchronized = aSynchronized;
    }

    public RealmList<BillString> getBillStrings() {
        return billStrings;
    }

    public void setBillStrings(RealmList<BillString> billStrings) {
        this.billStrings = billStrings;
    }

    public RealmList<BillPaymentType> getBillPaymentTypes() {
        return billPaymentTypes;
    }

    public void setBillPaymentTypes(RealmList<BillPaymentType> billPaymentTypes) {
        this.billPaymentTypes = billPaymentTypes;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }

    public String getLastEditAuthor() {
        return lastEditAuthor;
    }

    public void setLastEditAuthor(String lastEditAuthor) {
        this.lastEditAuthor = lastEditAuthor;
    }

    public long getLastEditDate() {
        return lastEditDate;
    }

    public void setLastEditDate(long lastEditDate) {
        this.lastEditDate = lastEditDate;
    }

    public int getInProcessOfSync() {
        return inProcessOfSync;
    }

    public void setInProcessOfSync(int inProcessOfSync) {
        this.inProcessOfSync = inProcessOfSync;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getClosedByName() {
        return closedByName;
    }

    public void setClosedByName(String closedByName) {
        this.closedByName = closedByName;
    }

    public int getOperationType() {
        return operationType;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
    }

    public int getShiftReceiptFiscalDevice() {
        return shiftReceiptFiscalDevice;
    }

    public void setShiftReceiptFiscalDevice(int shiftReceiptFiscalDevice) {
        this.shiftReceiptFiscalDevice = shiftReceiptFiscalDevice;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public int getUserCode() {
        return userCode;
    }

    public void setUserCode(int userCode) {
        this.userCode = userCode;
    }
}
