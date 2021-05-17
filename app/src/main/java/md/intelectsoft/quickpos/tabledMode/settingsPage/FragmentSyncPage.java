package md.intelectsoft.quickpos.tabledMode.settingsPage;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmList;
import md.intelectsoft.quickpos.EPOSService.EPOSRetrofitClient;
import md.intelectsoft.quickpos.EPOSService.EposServiceAPI;
import md.intelectsoft.quickpos.EPOSService.Results.Assortment;
import md.intelectsoft.quickpos.EPOSService.Results.AssortmentList;
import md.intelectsoft.quickpos.EPOSService.Results.FiscalDevice;
import md.intelectsoft.quickpos.EPOSService.Results.GetUsersList;
import md.intelectsoft.quickpos.EPOSService.Results.GetWorkplaceSettings;
import md.intelectsoft.quickpos.EPOSService.Results.PaymentType;
import md.intelectsoft.quickpos.EPOSService.Results.QuickGroup;
import md.intelectsoft.quickpos.EPOSService.Results.User;
import md.intelectsoft.quickpos.EPOSService.Results.UsersList;
import md.intelectsoft.quickpos.EPOSService.Results.WorkplaceSettings;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.Promotion;
import md.intelectsoft.quickpos.Realm.localStorage.AssortmentRealm;
import md.intelectsoft.quickpos.Realm.localStorage.Barcodes;
import md.intelectsoft.quickpos.Realm.localStorage.QuickGroupRealm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static md.intelectsoft.quickpos.POSApplication.SharedPrefSettings;
import static md.intelectsoft.quickpos.POSApplication.SharedPrefWorkPlaceSettings;

public class FragmentSyncPage extends Fragment {
    private TextView mLastSync;
    private Switch startUpSyncSwitch,autoSyncSwitch;

    private boolean updateAuto, updateToStart;
    String token, workplaceId;
    String uri;

    private Realm mRealm;

    private ConstraintLayout csl_sync, csl_startSync, csl_autoSync;
    private SharedPreferences sharedPrefSettings;
    private ProgressDialog pgH;

    SimpleDateFormat simpleDateFormatMD;
    TimeZone timeZoneMD;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_sync_page_version0, container, false);

        mLastSync = rootViewAdmin.findViewById(R.id.txt_last_sync);
        csl_sync = rootViewAdmin.findViewById(R.id.csl_sync_start);
        csl_startSync = rootViewAdmin.findViewById(R.id.csl_start_sync);
        csl_autoSync = rootViewAdmin.findViewById(R.id.csl_auto_sync);
        pgH = new ProgressDialog(getContext());
        autoSyncSwitch = rootViewAdmin.findViewById(R.id.switch_auto_sync);
        startUpSyncSwitch = rootViewAdmin.findViewById(R.id.switch_start_sync);

        mRealm = Realm.getDefaultInstance();

        sharedPrefSettings = getActivity().getSharedPreferences(SharedPrefSettings,MODE_PRIVATE);

        simpleDateFormatMD = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        timeZoneMD = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormatMD.setTimeZone(timeZoneMD);

        uri = sharedPrefSettings.getString("URI",null);
        token = sharedPrefSettings.getString("Token","null");
        workplaceId = getActivity().getSharedPreferences(SharedPrefWorkPlaceSettings, MODE_PRIVATE).getString("WorkPlaceID", null);

        mLastSync.setText(getString(R.string.last_sync_time) + sharedPrefSettings.getString("LastSync"," "));
        updateAuto = sharedPrefSettings.getBoolean("AutoSync",false);
        updateToStart =  sharedPrefSettings.getBoolean("SyncToStart",false);

        if(updateToStart)
            startUpSyncSwitch.setChecked(true);
        if(updateAuto){
            autoSyncSwitch.setChecked(true);
        }

        csl_sync.setOnClickListener(v -> {
            workplaceId = getActivity().getSharedPreferences(SharedPrefWorkPlaceSettings, MODE_PRIVATE).getString("WorkPlaceID", null);
            if(workplaceId != null) {
                new AssortmentTask().execute();
            }
        });

        csl_autoSync.setOnClickListener(view -> {
//            if(autoSyncSwitch.isChecked())
//                autoSyncSwitch.setChecked(false);
//            else
//                autoSyncSwitch.setChecked(true);
        });
        csl_startSync.setOnClickListener(view -> {
            if(startUpSyncSwitch.isChecked())
                startUpSyncSwitch.setChecked(false);
            else
                startUpSyncSwitch.setChecked(true);
        });

        startUpSyncSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            if(checked)
                sharedPrefSettings.edit().putBoolean("SyncToStart",true).apply();
            else
                sharedPrefSettings.edit().putBoolean("SyncToStart",false).apply();
        });

        autoSyncSwitch.setOnCheckedChangeListener(((compoundButton, checked) -> {
            if(checked)
                sharedPrefSettings.edit().putBoolean("AutoSync",true).apply();
            else
                sharedPrefSettings.edit().putBoolean("AutoSync",false).apply();
        }));


        return rootViewAdmin;
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
                    sharedPrefSettings.edit().putString("LastSync",form).apply();
                    mLastSync.setText(getString(R.string.last_sync_time) + form);
                    sharedPrefSettings.edit().putBoolean("Synched",true).apply();

                }else{
                    pgH.dismiss();
                    Toast.makeText(getContext(), "Errore sync workplace settings: " + errorecode, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GetWorkplaceSettings> call, Throwable t) {
                pgH.dismiss();
                Toast.makeText(getContext(), "Errore sync workplace settings: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
            String uri = sharedPrefSettings.getString("URI",null);
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
            String uri = sharedPrefSettings.getString("URI",null);
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
            String uri = sharedPrefSettings.getString("URI",null);
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
}
