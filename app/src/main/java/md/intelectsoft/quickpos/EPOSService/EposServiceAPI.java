package md.intelectsoft.quickpos.EPOSService;

import md.intelectsoft.quickpos.EPOSService.Body.SaveBills;
import md.intelectsoft.quickpos.EPOSService.Body.SaveShift;
import md.intelectsoft.quickpos.EPOSService.Results.AssortmentList;
import md.intelectsoft.quickpos.EPOSService.Results.AuthenticateUser;
import md.intelectsoft.quickpos.EPOSService.Results.GetClientCardInfo;
import md.intelectsoft.quickpos.EPOSService.Results.GetUsersList;
import md.intelectsoft.quickpos.EPOSService.Results.GetWorkplaceSettings;
import md.intelectsoft.quickpos.EPOSService.Results.GetWorkplaces;
import md.intelectsoft.quickpos.EPOSService.Results.SimpleResponseEPOS;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface EposServiceAPI {
    @GET("json/GetAssortmentList")
    Call<AssortmentList> getAssortmentList (@Query("Token") String token, @Query("WorkplaceId") String workplace);

    @POST("json/SaveBills")
    Call<SimpleResponseEPOS> saveBills (@Body SaveBills bills);

    @POST("json/SaveShift")
    Call<SimpleResponseEPOS> saveShift (@Body SaveShift shift);

    @GET("json/GetWorkPlaces")
    Call<GetWorkplaces> getWorkplaces(@Query("Token") String token);

    @GET("json/GetWorkplaceSettings")
    Call<GetWorkplaceSettings> getWorkplaceSettings(@Query("Token") String token, @Query("WorkplaceId") String workPlaceId);

    @GET("json/AuthentificateUser")
    Call<AuthenticateUser> authenticateUser(@Query("APIKey") String apiKey, @Query("userLogin") String login, @Query("userPass") String pass);

    @GET("json/GetUsersList")
    Call<GetUsersList> getUsers (@Query("Token") String token, @Query("WorkplaceId") String workPlaceId);

    @GET("json/GetClientCardInfo")
    Call<GetClientCardInfo> getClientCardInfo (@Query("Token") String token, @Query("ClientCard") String clientCard);
}
