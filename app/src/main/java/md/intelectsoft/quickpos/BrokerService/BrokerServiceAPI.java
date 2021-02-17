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
    @GET("/ISConnectionBrokerService/json/Ping")
    Call<Boolean> ping ();

    @POST("/ISConnectionBrokerService/json/RegisterApplication")
    Call<RegisterApplication> registerApplicationCall(@Body SendRegisterApplication bodyRegisterApp);

    @POST("/ISConnectionBrokerService/json/GetURI")
    Call<RegisterApplication> getURICall(@Body SendGetURI sendGetURI);

    @POST("/ISConnectionBrokerService/json/UpdateDiagnosticInformation")
    Call<ErrorMessage> updateDiagnosticInfo (@Body InformationData informationData);

    @GET("/ISConnectionBrokerService/json/GetNews")
    Call<GetNews> getNews (@Query("ID") int id, @Query("ProductType") int productType);
}
