package md.intelectsoft.quickpos.EPOSService.Body;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SaveBills {
    @SerializedName("Bills")
    @Expose
    private List<BillRemote> billRemotes = null;
    @SerializedName("ShiftID")
    @Expose
    private String shiftID;
    @SerializedName("Token")
    @Expose
    private String token;

    public List<BillRemote> getBillRemotes() {
        return billRemotes;
    }

    public void setBillRemotes(List<BillRemote> billRemotes) {
        this.billRemotes = billRemotes;
    }

    public String getShiftID() {
        return shiftID;
    }

    public void setShiftID(String shiftID) {
        this.shiftID = shiftID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
