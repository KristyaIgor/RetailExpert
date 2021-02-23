package md.intelectsoft.quickpos.Realm.localStorage.fiscalObjects.bon;

import io.realm.RealmObject;

public class BonCasaItem extends RealmObject {
    private String bonCasaId;
    private int cpvCode;
    private String name;
    private double quantity;
    private double price;
    private double sum;
    private double discount;
    private String vatCode;
    private int vatValue;

    public String getBonCasaId() {
        return bonCasaId;
    }

    public void setBonCasaId(String bonCasaId) {
        this.bonCasaId = bonCasaId;
    }

    public int getCpvCode() {
        return cpvCode;
    }

    public void setCpvCode(int cpvCode) {
        this.cpvCode = cpvCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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

    public String getVatCode() {
        return vatCode;
    }

    public void setVatCode(String vatCode) {
        this.vatCode = vatCode;
    }

    public int getVatValue() {
        return vatValue;
    }

    public void setVatValue(int vatValue) {
        this.vatValue = vatValue;
    }
}
