package md.intelectsoft.quickpos.FiscalService;

import md.intelectsoft.quickpos.FiscalService.Body.PrintBillFiscalService;
import md.intelectsoft.quickpos.FiscalService.Result.SimpleResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface FiscalServiceAPI {
    @POST("/fpservice/json/PrintBill")
    Call<SimpleResult> printBill (@Body PrintBillFiscalService bill);

    @GET("/fpservice/json/PrintReportX")
    Call<SimpleResult> printXReport (@Query("prn") int param);

    @GET("/fpservice/json/PrintReportZ")
    Call<SimpleResult> printZReport(@Query("prn") int param);

    @GET("/fpservice/json/GetState")
    Call<SimpleResult> getState ();
}
