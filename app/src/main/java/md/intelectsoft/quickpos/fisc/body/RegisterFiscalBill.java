package md.intelectsoft.quickpos.fisc.body;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegisterFiscalBill {
    @SerializedName("Bill")
    @Expose
    private FiscalBill bill;
    @SerializedName("LicenseID")
    @Expose
    private String licenseID;

    public FiscalBill getBill() {
        return bill;
    }

    public void setBill(FiscalBill bill) {
        this.bill = bill;
    }

    public String getLicenseID() {
        return licenseID;
    }

    public void setLicenseID(String licenseID) {
        this.licenseID = licenseID;
    }
}
