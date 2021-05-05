package md.intelectsoft.quickpos.fisc;

import md.intelectsoft.quickpos.fisc.body.RegisterFiscalBill;
import md.intelectsoft.quickpos.fisc.response.RespRegisterFiscalBill;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface BillServiceAPI {
    @POST("json/RegisterBill")
    Call<RespRegisterFiscalBill> registerBill (@Body RegisterFiscalBill bill);

}
