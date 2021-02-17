
package md.intelectsoft.quickpos.EPOSService.Results;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetWorkplaces {

    @SerializedName("GetWorkplacesResult")
    @Expose
    private WorkplaceList workplaceList;

    public WorkplaceList getWorkplaceList() {
        return workplaceList;
    }

    public void setWorkplaceList(WorkplaceList workplaceList) {
        this.workplaceList = workplaceList;
    }

}
