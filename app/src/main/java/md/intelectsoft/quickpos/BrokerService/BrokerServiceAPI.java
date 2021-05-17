package md.intelectsoft.quickpos.BrokerService;


import md.intelectsoft.quickpos.BrokerService.Body.InformationData;
import md.intelectsoft.quickpos.BrokerService.Body.SendGetURI;
import md.intelectsoft.quickpos.BrokerService.Body.SendRegisterApplication;
import md.intelectsoft.quickpos.BrokerService.Results.ErrorMessage;
import md.intelectsoft.quickpos.BrokerService.Results.GetNews;
import md.intelectsoft.quickpos.BrokerService.Results.RegisterApplication;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface BrokerServiceAPI {
    @POST("/ISLicenseService/json/RegisterApplication")
    Call<RegisterApplication> registerApplicationCall(@Body SendRegisterApplication bodyRegisterApp);

    @POST("/ISLicenseService/json/GetURI")
    Call<RegisterApplication> getURI(@Body SendGetURI sendGetURI);

    @POST("/ISLicenseService/json/UpdateDiagnosticInformation")
    Call<ErrorMessage> updateDiagnosticInfo (@Body InformationData informationData);

    @GET("/ISLicenseService/json/GetNews")
    Call<GetNews> getNews (@Query("ID") int id, @Query("ProductType") int productType);
}
