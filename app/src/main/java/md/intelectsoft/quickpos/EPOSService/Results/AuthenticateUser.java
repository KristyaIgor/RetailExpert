package md.intelectsoft.quickpos.EPOSService.Results;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 04.02.2020
 */

public class AuthenticateUser {
    @SerializedName("AuthentificateUserResult")
    @Expose
    private TokenEPOS tokenEPOS;

    public TokenEPOS getTokenEPOS() {
        return tokenEPOS;
    }

    public void setTokenEPOS(TokenEPOS tokenEPOS) {
        this.tokenEPOS = tokenEPOS;
    }
}
