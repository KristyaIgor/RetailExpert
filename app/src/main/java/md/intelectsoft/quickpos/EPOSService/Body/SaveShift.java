package md.intelectsoft.quickpos.EPOSService.Body;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SaveShift {
    @SerializedName("Shift")
    @Expose
    private ShiftRemote shift;
    @SerializedName("Token")
    @Expose
    private String token;

    public ShiftRemote getShift() {
        return shift;
    }

    public void setShift(ShiftRemote shift) {
        this.shift = shift;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
