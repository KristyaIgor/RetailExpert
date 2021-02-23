package md.intelectsoft.quickpos.phoneMode.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SimpleAdapter;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.realm.Realm;
import io.realm.RealmList;
import md.intelectsoft.quickpos.EPOSService.EPOSRetrofitClient;
import md.intelectsoft.quickpos.EPOSService.EposServiceAPI;
import md.intelectsoft.quickpos.EPOSService.Results.Assortment;
import md.intelectsoft.quickpos.EPOSService.Results.AssortmentList;
import md.intelectsoft.quickpos.EPOSService.Results.GetAssortmentList;
import md.intelectsoft.quickpos.EPOSService.Results.GetUsersList;
import md.intelectsoft.quickpos.EPOSService.Results.GetWorkplaceSettings;
import md.intelectsoft.quickpos.EPOSService.Results.GetWorkplaces;
import md.intelectsoft.quickpos.EPOSService.Results.PaymentType;
import md.intelectsoft.quickpos.EPOSService.Results.QuickGroup;
import md.intelectsoft.quickpos.EPOSService.Results.User;
import md.intelectsoft.quickpos.EPOSService.Results.UsersList;
import md.intelectsoft.quickpos.EPOSService.Results.Workplace;
import md.intelectsoft.quickpos.EPOSService.Results.WorkplaceList;
import md.intelectsoft.quickpos.EPOSService.Results.WorkplaceSettings;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.AssortmentRealm;
import md.intelectsoft.quickpos.Realm.localStorage.Barcodes;
import md.intelectsoft.quickpos.Realm.Promotion;
import md.intelectsoft.quickpos.Realm.localStorage.QuickGroupRealm;
import md.intelectsoft.quickpos.utils.IOnBackPressed;
import md.intelectsoft.quickpos.utils.POSApplication;
import md.intelectsoft.quickpos.utils.Rfc2898DerivesBytes;
import md.intelectsoft.quickpos.utils.SPFHelp;
import md.intelectsoft.quickpos.phoneMode.ui.sales.SalesFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SalesActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private EposServiceAPI eposServiceAPI;
    private ProgressDialog progressDialog;
    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blueItem));

        setContentView(R.layout.activity_sales);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_sales, R.id.nav_orders, R.id.nav_products, R.id.nav_shifts).setDrawerLayout(drawer).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        progressDialog = new ProgressDialog(this);
        mRealm = Realm.getDefaultInstance();

        String workPlace = SPFHelp.getInstance().getString("WorkplaceId", null);
        String uri = SPFHelp.getInstance().getString("URI", "0.0.0.0:1111");
        String tokenEPOS = SPFHelp.getInstance().getString("TokenEPOS", null);

        eposServiceAPI = EPOSRetrofitClient.getApiEposService(uri);

        if(workPlace == null){
            getWorkplaces(tokenEPOS);
        }
    }

    private void getWorkplaces(String tokenEPOS) {
        Call<GetWorkplaces> call = eposServiceAPI.getWorkplaces(tokenEPOS);

        progressDialog.setMessage("Loading workplaces...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            call.cancel();
            if(call.isCanceled())
                finish();
        });
        progressDialog.show();

        call.enqueue(new Callback<GetWorkplaces>() {
            @Override
            public void onResponse(Call<GetWorkplaces> call, Response<GetWorkplaces> response) {
                GetWorkplaces getWorkplaces = response.body();
                WorkplaceList workplaceList = getWorkplaces != null ? getWorkplaces.getWorkplaceList() : null;
                progressDialog.dismiss();

                if(workplaceList == null) onFinishApp("Response is null");
                else{
                    if(workplaceList.getErrorCode() == 0){
                        List<Workplace> listOfWorkplace = workplaceList.getWorkplaces();
                        List<Map<String,String>> workplaceListToDialog = new ArrayList<>();

                        if(listOfWorkplace != null && listOfWorkplace.size() > 0){
                            for(Workplace workplace : listOfWorkplace){
                                Map<String, String> item = new HashMap<>();
                                item.put("name",workplace.getName());
                                item.put("id", workplace.getID());

                                workplaceListToDialog.add(item);
                            }

                            SimpleAdapter simpleAdapterWareHouses = new SimpleAdapter(SalesActivity.this, workplaceListToDialog ,android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});

                            new MaterialAlertDialogBuilder(SalesActivity.this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                    .setTitle("Select your workplace:")
                                    .setCancelable(false)
                                    .setSingleChoiceItems(simpleAdapterWareHouses, -1, (dialog, which) -> {
                                        String workPlaceName = workplaceListToDialog.get(which).get("name");
                                        String workPlaceId = workplaceListToDialog.get(which).get("id");

                                        SPFHelp.getInstance().putString("WorkplaceName", workPlaceName);
                                        SPFHelp.getInstance().putString("WorkplaceId", workPlaceId);

                                        getAssortmentSync(workPlaceId, tokenEPOS);

                                        dialog.dismiss();
                                    })
                                    .setNegativeButton("Cancel", (dialog, which) -> {
                                        finish();
                                    })
                                    .show();
                        }
                        else onFinishApp("List of workplaces is empty!");

                    }
                    else onFinishApp("Error getting workplaces!" + workplaceList.getErrorText());
                }
            }

            @Override
            public void onFailure(Call<GetWorkplaces> call, Throwable t) {
                progressDialog.dismiss();
                onFinishApp("Fail getting workplaces! Because: " + t.getMessage());
            }
        });
    }

    private void getAssortmentSync(String workPlaceId, String tokenEPOS) {
        Call<GetAssortmentList> call = eposServiceAPI.getAssortmentList(tokenEPOS, workPlaceId);

        progressDialog.setMessage("Loading assortment list...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            call.cancel();
            //TODO show dialog with message that not sync
        });
        progressDialog.show();

        call.enqueue(new Callback<GetAssortmentList>() {
            @Override
            public void onResponse(Call<GetAssortmentList> call, Response<GetAssortmentList> response) {
                GetAssortmentList getAssortmentList = response.body();
                AssortmentList assortmentList = getAssortmentList != null ? getAssortmentList.getAssortmentList() : null;

                if(assortmentList != null && assortmentList.getErrorCode() == 0){
                    List<Assortment> assortments = assortmentList.getAssortments();

                    if(assortments != null && assortments.size() > 0){
                        mRealm.beginTransaction();
                        
                        mRealm.delete(QuickGroupRealm.class);
                        mRealm.delete(Barcodes.class);
                        mRealm.delete(Promotion.class);
                        mRealm.delete(AssortmentRealm.class);
                        
                        for(Assortment assortment : assortments){
                            RealmList<Barcodes> listBarcode = new RealmList<>();
                            RealmList<Promotion> listPromotion = new RealmList<>();

                            if(assortment.getBarcodes() != null){
                                for(String barcodes : assortment.getBarcodes()){
                                    Barcodes barcodes1 = new Barcodes();
                                    barcodes1.setBar(barcodes);
                                    listBarcode.add(barcodes1);
                                }
                            }
                            if(assortment.getPromotions()!= null) listPromotion.addAll(assortment.getPromotions());

                            AssortmentRealm assortmentRealm = new AssortmentRealm();

                            assortmentRealm.setId(assortment.getID());
                            assortmentRealm.setName(assortment.getName());
                            assortmentRealm.setBarcodes(listBarcode);
                            assortmentRealm.setFolder(assortment.getIsFolder());
                            assortmentRealm.setPromotions(listPromotion);
                            assortmentRealm.setAllowDiscounts(assortment.getAllowDiscounts());
                            assortmentRealm.setAllowNonInteger(assortment.getAllowNonInteger());
                            assortmentRealm.setCode(assortment.getCode());
                            assortmentRealm.setEnableSaleTimeRange(assortment.getEnableSaleTimeRange());
                            assortmentRealm.setMarking(assortment.getMarking());
                            assortmentRealm.setParentID(assortment.getParentID());
                            assortmentRealm.setBasePrice(assortment.getPrice());
                            assortmentRealm.setPriceLineId(assortment.getPriceLineId());
                            assortmentRealm.setShortName(assortment.getShortName());
                            assortmentRealm.setVat(assortment.getVAT());
                            assortmentRealm.setUnit(assortment.getUnit());
                            assortmentRealm.setQuickButtonNumber(assortment.getQuickButtonNumber());
                            assortmentRealm.setQuickGroupName(assortment.getQuickGroupName());
                            assortmentRealm.setStockBalance(assortment.getStockBalance());
                            assortmentRealm.setStockBalanceDate(assortment.getStockBalanceDate());
                            assortmentRealm.setSaleStartTime(replaceDate(assortment.getSaleStartTime()));
                            assortmentRealm.setSaleEndTime(replaceDate(assortment.getSaleEndTime()));
                            assortmentRealm.setPriceLineStartDate(replaceDate(assortment.getPriceLineStartDate()));
                            assortmentRealm.setPriceLineEndDate(replaceDate(assortment.getPriceLineEndDate()));
                            
                            mRealm.insert(assortmentRealm);
                        }
                        if(assortmentList.getQuickGroups() != null){
                            for(QuickGroup quickGroup : assortmentList.getQuickGroups()){
                                QuickGroupRealm quickGroupRealm = new QuickGroupRealm();

                                RealmList<String> assortment = new RealmList<>();
                                assortment.addAll(quickGroup.getAssortmentID());

                                quickGroupRealm.setGroupName(quickGroup.getName());
                                quickGroupRealm.setAssortmentId(assortment);

                                mRealm.insert(quickGroupRealm);
                            }
                        }
                        mRealm.commitTransaction();
                        progressDialog.dismiss();

                        SalesFragment.showAssortment();

                        getUserSync(workPlaceId, tokenEPOS);
                    }
                    else onSyncError("Error download assortment, list is empty!");
                }else onSyncError("Error download assortment. Code: " + assortmentList.getErrorText());
            }

            @Override
            public void onFailure(Call<GetAssortmentList> call, Throwable t) {
                onSyncError("Fail download assortment. Message: " + t.getMessage());
            }
        });
    }

    private void getUserSync(String workPlaceId, String tokenEPOS) {
        Call<GetUsersList> call = eposServiceAPI.getUsers(tokenEPOS, workPlaceId);

        progressDialog.setMessage("Loading users list...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            call.cancel();
            //TODO show dialog with message that not sync
        });
        progressDialog.show();
        
        call.enqueue(new Callback<GetUsersList>() {
            @Override
            public void onResponse(Call<GetUsersList> call, Response<GetUsersList> response) {
                GetUsersList getUsersList = response.body();
                UsersList usersList = getUsersList != null ? getUsersList.getUsersList() : null;
                
                if(usersList != null && usersList.getErrorCode() == 0){
                    List<User> users = usersList.getUsers();

                    if(users != null && users.size() > 0){
                        mRealm.beginTransaction();
                        mRealm.delete(User.class);
                        
                        String login = POSApplication.getApplication().getUserLogin();
                        String passwordHashed = getSHA1HashUserPassword("This is the code for UserPass", POSApplication.getApplication().getUserPasswordsNotHashed()).replace("\n","");
                        
                        for(User user : users) {
                            if(user.getPassword() != null && user.getUserName() != null)
                                if (user.getPassword().equals(passwordHashed) && user.getUserName().equals(login)) {
                                    if(SPFHelp.getInstance().getBoolean("KeepMeSigned", false))
                                        SPFHelp.getInstance().putString("LastUser", user.getId());
                                    POSApplication.getApplication().setUser(user);
                                }
                           mRealm.insert(user); 
                        }
                        mRealm.commitTransaction();
                        
                        progressDialog.dismiss();
                        getWorkplaceSettings(workPlaceId, tokenEPOS);

                    }else onSyncError("User list is empty!");
                }else onSyncError("Error loading users.Message: " + usersList.getErrorCode() == null ? "Response is null!" : usersList.getErrorText());
            }

            @Override
            public void onFailure(Call<GetUsersList> call, Throwable t) {
                onSyncError("Fail loading users. Message: " + t.getMessage());
            }
        });
    }

    private void getWorkplaceSettings(String workPlaceId, String tokenEPOS) {
        Call<GetWorkplaceSettings> call = eposServiceAPI.getWorkplaceSettings(tokenEPOS, workPlaceId);

        progressDialog.setMessage("Loading settings...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            call.cancel();
            //TODO show dialog with message that not sync
        });
        progressDialog.show();

        call.enqueue(new Callback<GetWorkplaceSettings>() {
            @Override
            public void onResponse(Call<GetWorkplaceSettings> call, Response<GetWorkplaceSettings> response) {
                GetWorkplaceSettings getWorkplaceSettings = response.body();
                WorkplaceSettings workplaceSettings = getWorkplaceSettings.getWorkplaceSettings();

                if(workplaceSettings != null && workplaceSettings.getErrorCode() == 0){
                    List<PaymentType> paymentTypes = workplaceSettings.getPaymentTypes();

                    if(paymentTypes != null && paymentTypes.size() > 0) {
                        mRealm.beginTransaction();
                        mRealm.delete(PaymentType.class);
                        for(PaymentType paymentType : paymentTypes) mRealm.insert(paymentType);
                        mRealm.commitTransaction();
                        progressDialog.dismiss();
                    } else onSyncError("Payments list empty!");
                }else onSyncError("Error loading workplace settings.Message: " + workplaceSettings.getErrorCode() == null ? "Response is null!" : workplaceSettings.getErrorText());
            }

            @Override
            public void onFailure(Call<GetWorkplaceSettings> call, Throwable t) {
                onSyncError("Fail loading users. Message: " + t.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sales, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        User user = mRealm.where(User.class).equalTo("id", SPFHelp.getInstance().getString("LastUser", "")).findFirst();
        POSApplication.getApplication().setUser(user);

    }

    private void onFinishApp(String text){
        new MaterialAlertDialogBuilder(SalesActivity.this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                .setTitle("Attention!")
                .setMessage(text)
                .setCancelable(false)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    finish();
                })
                .show();
    }

    private void onSyncError(String text) {
        progressDialog.dismiss();

        new MaterialAlertDialogBuilder(SalesActivity.this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                .setTitle("Attention!")
                .setMessage(text)
                .setCancelable(false)
                .setPositiveButton("OK", (dialogInterface, i) -> {

                })
                .show();
    }

    public static long replaceDate(String date){
        if(date != null ){
            date = date.replace("/Date(","");
            date = date.replace("+0200)/","");
            date = date.replace("+0300)/","");
            return Long.parseLong(date);
        }
        else
            return 0;

    }

    public static String getSHA1HashUserPassword(String keyHint, String message) {
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
}