package md.intelectsoft.quickpos.tabledMode.settingsPage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.realm.Realm;
import io.realm.RealmList;
import md.intelectsoft.quickpos.EPOSService.EPOSRetrofitClient;
import md.intelectsoft.quickpos.EPOSService.EposServiceAPI;
import md.intelectsoft.quickpos.EPOSService.Results.Assortment;
import md.intelectsoft.quickpos.EPOSService.Results.AssortmentList;
import md.intelectsoft.quickpos.EPOSService.Results.AuthenticateUser;
import md.intelectsoft.quickpos.EPOSService.Results.FiscalDevice;
import md.intelectsoft.quickpos.EPOSService.Results.GetUsersList;
import md.intelectsoft.quickpos.EPOSService.Results.GetWorkplaceSettings;
import md.intelectsoft.quickpos.EPOSService.Results.GetWorkplaces;
import md.intelectsoft.quickpos.EPOSService.Results.PaymentType;
import md.intelectsoft.quickpos.EPOSService.Results.QuickGroup;
import md.intelectsoft.quickpos.EPOSService.Results.TokenEPOS;
import md.intelectsoft.quickpos.EPOSService.Results.User;
import md.intelectsoft.quickpos.EPOSService.Results.UsersList;
import md.intelectsoft.quickpos.EPOSService.Results.Workplace;
import md.intelectsoft.quickpos.EPOSService.Results.WorkplaceList;
import md.intelectsoft.quickpos.EPOSService.Results.WorkplaceSettings;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.Promotion;
import md.intelectsoft.quickpos.Realm.localStorage.AssortmentRealm;
import md.intelectsoft.quickpos.Realm.localStorage.Barcodes;
import md.intelectsoft.quickpos.Realm.localStorage.QuickGroupRealm;
import md.intelectsoft.quickpos.SplashActivity;
import md.intelectsoft.quickpos.tabledMode.activity.MainTabledActivity;
import md.intelectsoft.quickpos.POSApplication;
import md.intelectsoft.quickpos.utils.Rfc2898DerivesBytes;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static md.intelectsoft.quickpos.POSApplication.SharedPrefSettings;
import static md.intelectsoft.quickpos.POSApplication.SharedPrefWorkPlaceSettings;

public class FragmentCompanyPage extends Fragment {
    private TextView mSelectWorkPlace;
    private List<Workplace> workplaceArrayList = new ArrayList<>();

    private SharedPreferences sPrefWorkPlace, sPrefSettings;

    private ConstraintLayout csl_workplace;
    private ProgressDialog pgH;
    private LayoutInflater inflater2;
    private Realm mRealm;
    String token, workplaceId;

    SimpleDateFormat simpleDateFormatMD;
    TimeZone timeZoneMD;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_company_page, container, false);
        inflater2 = inflater;

        mSelectWorkPlace = rootViewAdmin.findViewById(R.id.txt_company_workplace);
        csl_workplace = rootViewAdmin.findViewById(R.id.csl_company_workplace);
        TextView companyName = rootViewAdmin.findViewById(R.id.txt_company_name);
        TextView idnoCompany = rootViewAdmin.findViewById(R.id.txt_company_idno);
        TextView licenseCode = rootViewAdmin.findViewById(R.id.txt_license_code);
        pgH = new ProgressDialog(getContext());

        mRealm = Realm.getDefaultInstance();

        sPrefWorkPlace = getActivity().getSharedPreferences(SharedPrefWorkPlaceSettings, MODE_PRIVATE);
        sPrefSettings = getActivity().getSharedPreferences(SharedPrefSettings, MODE_PRIVATE);

        simpleDateFormatMD = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        timeZoneMD = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormatMD.setTimeZone(timeZoneMD);

        companyName.setText(sPrefSettings.getString("CompanyName",""));
        idnoCompany.setText(sPrefSettings.getString("CompanyIDNO",""));
        licenseCode.setText(sPrefSettings.getString("LicenseCode",""));
        mSelectWorkPlace.setText(sPrefWorkPlace.getString("WorkPlaceName",""));

        csl_workplace.setOnClickListener(view -> {
            if(POSApplication.getApplication().getShift() == null || POSApplication.getApplication().getShift().isClosed()){
                pgH.setMessage("loading workplace...");
                pgH.setIndeterminate(true);
                pgH.show();

                String uri = sPrefSettings.getString("URI",null);
                token = sPrefSettings.getString("Token",null);
                getSyncWorkplace(uri,token);
            }
            else{
                Toast.makeText(getContext(), "Shift is open,need closed shift after change workplace!", Toast.LENGTH_SHORT).show();
            }
        });

        return rootViewAdmin;
    }
    private void getSyncWorkplace(final String uri, String token){
        EposServiceAPI commandServices = EPOSRetrofitClient.getApiEposService(uri);
        final Call<GetWorkplaces> workplace = commandServices.getWorkplaces(token);
        workplace.enqueue(new Callback<GetWorkplaces>() {
            @Override
            public void onResponse(Call<GetWorkplaces> call, Response<GetWorkplaces> response) {

                GetWorkplaces workPlaceService = response.body();
                WorkplaceList result = workPlaceService != null ? workPlaceService.getWorkplaceList() : null;

                int errorecode = 101;
                if (result != null) {
                    errorecode = result.getErrorCode();
                }

                if(errorecode == 0){
                    pgH.dismiss();
                    if(result.getWorkplaces() != null){
                        workplaceArrayList = result.getWorkplaces();

                        String[] itemsWo = new String[result.getWorkplaces().size()];
                        String[] itemsWoID = new String[result.getWorkplaces().size()];
                        int i = 0;
                        for (Workplace wo :result.getWorkplaces()) {
                            itemsWo[i] = wo.getName();
                            itemsWoID[i] = wo.getID();
                            i++;
                        }


                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(getContext().getResources().getString(R.string.dialog_select_workplace));
                        builder.setSingleChoiceItems(itemsWo, -1, (dialogInterface, position) -> {
                            workplaceId = itemsWoID[position];
                            sPrefWorkPlace.edit().putString("WorkPlaceID", itemsWoID[position]).apply();
                            sPrefWorkPlace.edit().putString("WorkPlaceName", itemsWo[position]).apply();
                            sPrefWorkPlace.edit().putBoolean("WorkPlaceChanged",true).apply();
                            mSelectWorkPlace.setText(itemsWo[position]);
                            syncDataNewWorkPlace(itemsWoID[position]);

                            dialogInterface.dismiss();
                        });

                        builder.setNegativeButton("Cancel",(dialogInterface, posi) -> {
                           dialogInterface.dismiss();
                        });

                        AlertDialog alert = builder.create();
                        alert.setCanceledOnTouchOutside(false);
                        alert.show();

                    }
                }
                else if(errorecode == 405){
                    pgH.dismiss();
                    // не прав на просмотр рабочих мест
                    new MaterialAlertDialogBuilder(getContext(), R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                            .setTitle(getContext().getResources().getString(R.string.message_attention))
                            .setMessage(getContext().getResources().getString(R.string.noright_workplace_ride))
                            .setCancelable(false)
                            .setPositiveButton("YES", (dialogInterface, i) -> {

                                View dialogView = inflater2.inflate(R.layout.dialog_login_user, null);

                                AlertDialog reLogin = new AlertDialog.Builder(getContext()).create();
                                reLogin.setCancelable(false);
                                reLogin.setView(dialogView);

                                EditText LetUserName = dialogView.findViewById(R.id.et_login_user_form);
                                EditText LetPassword = dialogView.findViewById(R.id.et_password_login_user);
                                MaterialButton btnLogin = dialogView.findViewById(R.id.btn_login_user_form);

                                btnLogin.setOnClickListener(view -> {
                                    pgH.setMessage(getContext().getResources().getString(R.string.text_loading));
                                    pgH.setIndeterminate(true);
                                    pgH.show();

                                    authUserToServer(LetUserName.getText().toString(),LetPassword.getText().toString());
                                });
                                reLogin.show();

                                dialogInterface.dismiss();
                            })
                            .setNegativeButton(getContext().getResources().getString(R.string.btn_no),(dialogInterface, i) -> {

                            })
                            .show();
                }
                else if(errorecode == 401){
                    //необходимо обновить токен
                    String login = POSApplication.getApplication().getUser().getUserName();
                    String pass = POSApplication.getApplication().getUserPasswordsNotHashed();

                    authUserToServer(login,pass);
                }
                else{
                    pgH.dismiss();
                    Toast.makeText(getContext(), "Other error code:" + errorecode, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GetWorkplaces> call, Throwable t) {
                pgH.dismiss();
                Toast.makeText(getContext(), "onFailure get workplace settings:" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void syncDataNewWorkPlace(String s) {
        pgH.setMessage(getString(R.string.sync_page));
        pgH.setCancelable(false);
        pgH.setIndeterminate(true);
        pgH.show();

        new AssortmentTask().execute();
    }

    class AssortmentTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pgH.setMessage(getString(R.string.message_loading_assortment));
            pgH.setCancelable(false);
            pgH.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pgH.show();
        }
        @Override
        protected Void doInBackground(Void... dates) {
            String uri = sPrefSettings.getString("URI",null);
            EposServiceAPI commandServices = EPOSRetrofitClient.getApiEposService(uri);

            final Call<AssortmentList> assortiment = commandServices.getAssortmentList(token, workplaceId);
            readAssortment(assortiment);
            return null;
        }
    }
    class UserTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pgH.dismiss();
            pgH.setCancelable(false);
            pgH.setMessage(getString(R.string.message_loading_users));
            pgH.setIndeterminate(true);
            pgH.show();
        }
        @Override
        protected Void doInBackground(Void... dates) {
            String uri = sPrefSettings.getString("URI",null);
            EposServiceAPI commandServices = EPOSRetrofitClient.getApiEposService(uri);

            final Call<GetUsersList> userListServiceResultCall = commandServices.getUsers(token, workplaceId);
            readUsers(userListServiceResultCall);
            return null;
        }
    }
    class WorkPlaceTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pgH.dismiss();
            pgH.setMessage(getString(R.string.message_loading_workplace_settings));
            pgH.setIndeterminate(true);
            pgH.setCancelable(false);
            pgH.show();
        }
        @Override
        protected Void doInBackground(Void... dates) {
            String uri = sPrefSettings.getString("URI",null);
            EposServiceAPI commandServices = EPOSRetrofitClient.getApiEposService(uri);

            final Call<GetWorkplaceSettings> workPlaceSettingsCall = commandServices.getWorkplaceSettings(token, workplaceId);
            readWorkPlaceSettings(workPlaceSettingsCall);
            return null;
        }
    }

    private long replaceDate(String date){
        if(date !=null ){
            date = date.replace("/Date(","");
            date = date.replace("+0200)/","");
            date = date.replace("+0300)/","");
            return Long.parseLong(date);
        }
        else
            return 0;

    }

    private void readAssortment (final Call<AssortmentList> assortiment){
        assortiment.enqueue(new Callback<AssortmentList>() {
            @Override
            public void onResponse(Call<AssortmentList> call, Response<AssortmentList> response) {

                AssortmentList result = response.body();

                int errorecode = 101;
                if (result != null) {
                    errorecode = result.getErrorCode();
                }
                if(errorecode == 0){
                    mRealm.executeTransaction(realm -> {
                        realm.delete(AssortmentRealm.class);
                        realm.delete(QuickGroupRealm.class);
                        realm.delete(Barcodes.class);
                        realm.delete(Promotion.class);
                    });

                    List<Assortment> assortmentListData = result.getAssortments();
                    mRealm.executeTransaction(realm -> {

                        for(Assortment assortmentServiceEntry: assortmentListData){
                            AssortmentRealm ass = new AssortmentRealm();

                            RealmList<Barcodes> listBarcode = new RealmList<>();
                            RealmList<Promotion> listPromotion = new RealmList<>();

                            if(assortmentServiceEntry.getBarcodes() != null){
                                for(String barcodes : assortmentServiceEntry.getBarcodes()){
                                    Barcodes barcodes1 = new Barcodes();
                                    barcodes1.setBar(barcodes);
                                    listBarcode.add(barcodes1);
                                }
                            }
                            if(assortmentServiceEntry.getPromotions()!= null){
                                listPromotion.addAll(assortmentServiceEntry.getPromotions());
                            }
                            ass.setId(assortmentServiceEntry.getID());
                            ass.setName(assortmentServiceEntry.getName());
                            ass.setBarcodes(listBarcode);
                            ass.setFolder(assortmentServiceEntry.getIsFolder());
                            ass.setPromotions(listPromotion);
                            ass.setAllowDiscounts(assortmentServiceEntry.getAllowDiscounts());
                            ass.setAllowNonInteger(assortmentServiceEntry.getAllowNonInteger());
                            ass.setCode(assortmentServiceEntry.getCode());
                            ass.setEnableSaleTimeRange(assortmentServiceEntry.getEnableSaleTimeRange());
                            ass.setMarking(assortmentServiceEntry.getMarking());
                            ass.setParentID(assortmentServiceEntry.getParentID());
                            ass.setBasePrice(assortmentServiceEntry.getPrice());
                            ass.setPriceLineId(assortmentServiceEntry.getPriceLineId());
                            ass.setShortName(assortmentServiceEntry.getShortName());
                            ass.setVat(assortmentServiceEntry.getVAT());
                            ass.setUnit(assortmentServiceEntry.getUnit());
                            ass.setQuickButtonNumber(assortmentServiceEntry.getQuickButtonNumber());
                            ass.setQuickGroupName(assortmentServiceEntry.getQuickGroupName());
                            ass.setStockBalance(assortmentServiceEntry.getStockBalance());
                            ass.setStockBalanceDate(assortmentServiceEntry.getStockBalanceDate());
                            ass.setSaleStartTime(replaceDate(assortmentServiceEntry.getSaleStartTime()));
                            ass.setSaleEndTime(replaceDate(assortmentServiceEntry.getSaleEndTime()));
                            ass.setPriceLineStartDate(replaceDate(assortmentServiceEntry.getPriceLineStartDate()));
                            ass.setPriceLineEndDate(replaceDate(assortmentServiceEntry.getPriceLineEndDate()));

                            realm.insert(ass);
                        }

                        if(result.getQuickGroups() != null){
                            for(QuickGroup quickGroup : result.getQuickGroups()){
                                QuickGroupRealm quickGroupRealm = new QuickGroupRealm();

                                String nameGroup = quickGroup.getName();
                                RealmList<String> assortment = new RealmList<>();
                                assortment.addAll(quickGroup.getAssortmentID());

                                quickGroupRealm.setGroupName(nameGroup);
                                quickGroupRealm.setAssortmentId(assortment);

                                realm.insert(quickGroupRealm);
                            }
                        }

                    });
                    new UserTask().execute();

                }else{
                    pgH.dismiss();
                    Toast.makeText(getContext(), "Errore sync assortment: " + errorecode, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<AssortmentList> call, Throwable t) {
                pgH.dismiss();
                Toast.makeText(getContext(), "Errore sync assortment: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void readWorkPlaceSettings(Call<GetWorkplaceSettings> workPlaceSettingsCall){
        workPlaceSettingsCall.enqueue(new Callback<GetWorkplaceSettings>() {
            @Override
            public void onResponse(Call<GetWorkplaceSettings> call, Response<GetWorkplaceSettings> response) {
                GetWorkplaceSettings workPlaceSettings = response.body();

                WorkplaceSettings result = workPlaceSettings != null ? workPlaceSettings.getWorkplaceSettings() : null;

                int errorecode = 101;
                if (result != null) {
                    errorecode = result.getErrorCode();
                }
                if(errorecode == 0){

                    mRealm.executeTransaction(realm -> {
                        realm.delete(PaymentType.class);
                        realm.delete(FiscalDevice.class);
                    });

                    if(result.getPaymentTypes() != null){
                        List<PaymentType> paymentTypes = result.getPaymentTypes();
                        for(PaymentType paymentType : paymentTypes){
                            mRealm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.insert(paymentType);
                                }
                            });

                        }
                    }
                    if( result.getFiscalDevice() != null){
                        FiscalDevice fiscalDevice = result.getFiscalDevice();
                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.insert(fiscalDevice);
                            }
                        });
                    }
                    pgH.dismiss();
                    long tim = new Date().getTime();
                    String form = simpleDateFormatMD.format(tim);
                    sPrefSettings.edit().putString("LastSync",form).apply();

                    //TODO dialog for close app

                    Activity activity = MainTabledActivity.getActivity();
                    Intent start = new Intent(getContext(), SplashActivity.class);

                    activity.finish();
                    start.putExtra("workPlaceChanged",true);
                    activity.startActivity(start);
                }else{
                    pgH.dismiss();
                    Toast.makeText(getContext(), "Errore sync workplace: " + errorecode, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GetWorkplaceSettings> call, Throwable t) {
                pgH.dismiss();
                Toast.makeText(getContext(), "Errore sync workplace: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void readUsers(Call<GetUsersList> userListServiceResultCall){
        userListServiceResultCall.enqueue(new Callback<GetUsersList>() {
            @Override
            public void onResponse(Call<GetUsersList> call, Response<GetUsersList> response) {
                GetUsersList userListServiceResult = response.body();
                UsersList result = userListServiceResult != null ? userListServiceResult.getUsersList() : null;

                int errorecode = 101;
                if (result != null) {
                    errorecode = result.getErrorCode();
                }
                if(errorecode == 0){

                    mRealm.executeTransaction(realm -> {
                        realm.delete(User.class);
                    });

                    List<User> users = result.getUsers();
                    mRealm.executeTransaction(realm -> {
                        for(User user : users){
                            realm.insert(user);
                        }
                    });
                    new WorkPlaceTask().execute();

                }else{
                    pgH.dismiss();
                    Toast.makeText(getContext(), "Errore sync users: " + errorecode, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GetUsersList> call, Throwable t) {
                pgH.dismiss();
                Toast.makeText(getContext(), "Errore sync users: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static String GetSHA1HashUserPassword(String keyHint, String message) {
        byte[] hintBytes = ("This is strong key").getBytes();
        String form = "";
        try {

            Rfc2898DerivesBytes test = new Rfc2898DerivesBytes(keyHint,hintBytes,1000);
            byte[] secretKey = test.GetBytes(18);

            SecretKeySpec signingKey = new SecretKeySpec(secretKey, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] bytes = mac.doFinal(message.getBytes());
            form = Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return form;
    }

    private void authUserToServer(String login , String pass){
        String uri = sPrefSettings.getString("URI",null);
        String install_id = sPrefSettings.getString("InstallationID",null);

        EposServiceAPI commandServices = EPOSRetrofitClient.getApiEposService(uri);

        Call<AuthenticateUser> call = commandServices.authenticateUser(install_id,login,pass);

        call.enqueue(new Callback<AuthenticateUser>() {
            @Override
            public void onResponse(Call<AuthenticateUser> call, Response<AuthenticateUser> response) {
                AuthenticateUser authentificateUserResult = response.body();
                if(authentificateUserResult != null){
                    TokenEPOS token = authentificateUserResult.getTokenEPOS();
                    if(token.getErrorCode() == 0){
                        sPrefSettings.edit().putString("Token",token.getToken()).apply();
                        String date = token.getTokenValidTo();
                        date = date.replace("/Date(","");
                        date = date.replace("+0200)/","");
                        long dateLong = Long.parseLong(date);
                        sPrefSettings.edit().putLong("TokenValidTo",dateLong).apply();

                        getSyncWorkplace(uri,token.getToken());
                    }
                    else{
                        AlertDialog.Builder dialog_user = new AlertDialog.Builder(getContext());
                        dialog_user.setTitle("Atentie!");
                        dialog_user.setMessage("Eroare!Codul: " + token.getErrorCode());
                        dialog_user.setPositiveButton("Ok", (dialog, which) -> {
                            dialog.dismiss();
                        });
                        dialog_user.setNeutralButton("Oricum intra",(dialog,which) -> {

                        });
                        dialog_user.show();
                    }
                }
                else{
                    AlertDialog.Builder dialog_user = new AlertDialog.Builder(getContext());
                    dialog_user.setTitle("Atentie!");
                    dialog_user.setMessage("Nu este raspuns de la serviciu!");
                    dialog_user.setPositiveButton("Ok", (dialog, which) -> {
                        dialog.dismiss();
                    });
                    dialog_user.setNeutralButton("Oricum intra",(dialog,which) -> {

                    });
                    dialog_user.show();
                }
            }

            @Override
            public void onFailure(Call<AuthenticateUser> call, Throwable t) {
                String err = t.getMessage();
            }
        });
    }
}
