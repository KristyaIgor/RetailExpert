
package md.intelectsoft.quickpos.EPOSService.Results;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class AssortmentList {

    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorText")
    @Expose
    private String errorText;
    @SerializedName("Assortments")
    @Expose
    private List<Assortment> assortments = null;

    @SerializedName("QuickGroups")
    @Expose
    private List<QuickGroup> quickGroups = null;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public List<Assortment> getAssortments() {
        return assortments;
    }

    public void setAssortments(List<Assortment> assortments) {
        this.assortments = assortments;
    }

    public List<QuickGroup> getQuickGroups() {
        return quickGroups;
    }

    public void setQuickGroups(List<QuickGroup> quickGroups) {
        this.quickGroups = quickGroups;
    }

}
