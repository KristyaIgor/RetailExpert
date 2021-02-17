
package md.intelectsoft.quickpos.EPOSService.Results;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetAssortmentList {

    @SerializedName("GetAssortmentListResult")
    @Expose
    private AssortmentList assortmentList;

    public AssortmentList getAssortmentList() {
        return assortmentList;
    }

    public void setAssortmentList(AssortmentList assortmentList) {
        this.assortmentList = assortmentList;
    }

}
