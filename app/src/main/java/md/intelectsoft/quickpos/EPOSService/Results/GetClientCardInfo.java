package md.intelectsoft.quickpos.EPOSService.Results;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetClientCardInfo {

    @SerializedName("GetClientCardInfoResult")
    @Expose
    private GetClientCardInfoResult getClientCardInfoResult;

    public GetClientCardInfoResult getGetClientCardInfoResult() {
        return getClientCardInfoResult;
    }

    public void setGetClientCardInfoResult(GetClientCardInfoResult getClientCardInfoResult) {
        this.getClientCardInfoResult = getClientCardInfoResult;
    }

}
