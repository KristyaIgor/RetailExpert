package md.intelectsoft.quickpos.fisc.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetBillInfoResp {
    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("Bill")
    @Expose
    private FiscalBillInfo bill;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public FiscalBillInfo getBill() {
        return bill;
    }

    public void setBill(FiscalBillInfo bill) {
        this.bill = bill;
    }
}
