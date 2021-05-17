package md.intelectsoft.quickpos.Realm.localStorage;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by Igor on 02.10.2019
 */

public class BillString extends RealmObject {
    @Required
    private String billID;
    @Required
    private String id;
    @Required
    private String assortmentId;
    private String assortmentFullName;
    private String assortmentShortName;
    private String cpvCode;
    private boolean allowNonInteger;   //продажа не целово кол-во true - продается не целое кол-во  false - запрещено
    private boolean allowDiscounts;
    @Required
    private String userId;
    private long createDate;
    private String promoLineID;
    private String priceLineID;
    private double quantity;
    private double basePrice;
    private double priceWithDiscount;
    private double promoPrice;
    private double price;
    private double sum;
    private double sumWithDiscount;
    private double sumVat;
    private double sumWithoutVat;
    private String vatCode;
    private double vatValue;
    private String barcode;
    private boolean isDeleted;
    private long deletedDate;
    private String deleteBy;
    private boolean expanded;

    public String getBillID() {
        return billID;
    }

    public void setBillID(String billID) {
        this.billID = billID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssortmentId() {
        return assortmentId;
    }

    public void setAssortmentId(String assortmentId) {
        this.assortmentId = assortmentId;
    }

    public String getAssortmentFullName() {
        return assortmentFullName;
    }

    public void setAssortmentFullName(String assortmentFullName) {
        this.assortmentFullName = assortmentFullName;
    }

    public boolean isAllowNonInteger() {
        return allowNonInteger;
    }

    public void setAllowNonInteger(boolean allowNonInteger) {
        this.allowNonInteger = allowNonInteger;
    }

    public boolean isAllowDiscounts() {
        return allowDiscounts;
    }

    public void setAllowDiscounts(boolean allowDiscounts) {
        this.allowDiscounts = allowDiscounts;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public String getPromoLineID() {
        return promoLineID;
    }

    public void setPromoLineID(String promoLineID) {
        this.promoLineID = promoLineID;
    }

    public String getPriceLineID() {
        return priceLineID;
    }

    public void setPriceLineID(String priceLineID) {
        this.priceLineID = priceLineID;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getPriceWithDiscount() {
        return priceWithDiscount;
    }

    public void setPriceWithDiscount(double priceWithDiscount) {
        this.priceWithDiscount = priceWithDiscount;
    }

    public double getPromoPrice() {
        return promoPrice;
    }

    public void setPromoPrice(double promoPrice) {
        this.promoPrice = promoPrice;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getSumWithDiscount() {
        return sumWithDiscount;
    }

    public void setSumWithDiscount(double sumWithDiscount) {
        this.sumWithDiscount = sumWithDiscount;
    }

    public double getVatValue() {
        return vatValue;
    }

    public void setVatValue(double vatValue) {
        this.vatValue = vatValue;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public long getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(long deletedDate) {
        this.deletedDate = deletedDate;
    }

    public String getDeleteBy() {
        return deleteBy;
    }

    public void setDeleteBy(String deleteBy) {
        this.deleteBy = deleteBy;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getCpvCode() {
        return cpvCode;
    }

    public void setCpvCode(String cpvCode) {
        this.cpvCode = cpvCode;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getVatCode() {
        return vatCode;
    }

    public void setVatCode(String vatCode) {
        this.vatCode = vatCode;
    }

    public String getAssortmentShortName() {
        return assortmentShortName;
    }

    public void setAssortmentShortName(String assortmentShortName) {
        this.assortmentShortName = assortmentShortName;
    }

    public double getSumVat() {
        return sumVat;
    }

    public void setSumVat(double sumVat) {
        this.sumVat = sumVat;
    }

    public double getSumWithoutVat() {
        return sumWithoutVat;
    }

    public void setSumWithoutVat(double sumWithoutVat) {
        this.sumWithoutVat = sumWithoutVat;
    }
}
