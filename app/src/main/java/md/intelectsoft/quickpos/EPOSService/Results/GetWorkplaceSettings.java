
package md.intelectsoft.quickpos.EPOSService.Results;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetWorkplaceSettings {

    @SerializedName("GetWorkplaceSettingsResult")
    @Expose
    private WorkplaceSettings workplaceSettings;

    public WorkplaceSettings getWorkplaceSettings() {
        return workplaceSettings;
    }

    public void setWorkplaceSettings(WorkplaceSettings workplaceSettings) {
        this.workplaceSettings = workplaceSettings;
    }

}
