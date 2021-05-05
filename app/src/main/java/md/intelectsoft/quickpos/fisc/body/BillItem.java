package md.intelectsoft.quickpos.fisc.body;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BillItem {

    @SerializedName("BasePrice")
    @Expose
    private Double basePrice;
    @SerializedName("CPVCod")
    @Expose
    private String cPVCod;
    @SerializedName("Discount")
    @Expose
    private Double discount;
    @SerializedName("FinalPrice")
    @Expose
    private Double finalPrice;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Quantity")
    @Expose
    private Double quantity;
    @SerializedName("Summ")
    @Expose
    private Double summ;
    @SerializedName("VATCode")
    @Expose
    private String vATCode;
    @SerializedName("VATTotal")
    @Expose
    private Double vATTotal;
    @SerializedName("VATValue")
    @Expose
    private Double vATValue;

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public String getCPVCod() {
        return cPVCod;
    }

    public void setCPVCod(String cPVCod) {
        this.cPVCod = cPVCod;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getSumm() {
        return summ;
    }

    public void setSumm(Double summ) {
        this.summ = summ;
    }

    public String getVATCode() {
        return vATCode;
    }

    public void setVATCode(String vATCode) {
        this.vATCode = vATCode;
    }

    public Double getVATTotal() {
        return vATTotal;
    }

    public void setVATTotal(Double vATTotal) {
        this.vATTotal = vATTotal;
    }

    public Double getVATValue() {
        return vATValue;
    }

    public void setVATValue(Double vATValue) {
        this.vATValue = vATValue;
    }
}
