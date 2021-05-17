package md.intelectsoft.quickpos.phoneMode.activity;

import android.Manifest;
import android.animation.Animator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.realm.Realm;
import io.realm.RealmList;
import md.intelectsoft.quickpos.BrokerService.Body.SendGetURI;
import md.intelectsoft.quickpos.BrokerService.BrokerRetrofitClient;
import md.intelectsoft.quickpos.BrokerService.BrokerServiceAPI;
import md.intelectsoft.quickpos.BrokerService.Enum.BrokerServiceEnum;
import md.intelectsoft.quickpos.BrokerService.Results.AppDataRegisterApplication;
import md.intelectsoft.quickpos.BrokerService.Results.RegisterApplication;
import md.intelectsoft.quickpos.EPOSService.EPOSRetrofitClient;
import md.intelectsoft.quickpos.EPOSService.EposServiceAPI;
import md.intelectsoft.quickpos.EPOSService.Results.Assortment;
import md.intelectsoft.quickpos.EPOSService.Results.AssortmentList;
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
import md.intelectsoft.quickpos.POSApplication;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.Promotion;
import md.intelectsoft.quickpos.Realm.localStorage.AssortmentRealm;
import md.intelectsoft.quickpos.Realm.localStorage.Barcodes;
import md.intelectsoft.quickpos.Realm.localStorage.BillString;
import md.intelectsoft.quickpos.Realm.localStorage.History;
import md.intelectsoft.quickpos.Realm.localStorage.QuickGroupRealm;
import md.intelectsoft.quickpos.Realm.localStorage.Shift;
import md.intelectsoft.quickpos.phoneMode.adapters.AssortmentListGridAdapter;
import md.intelectsoft.quickpos.phoneMode.models.SalesViewModel;
import md.intelectsoft.quickpos.phoneMode.models.ShiftViewModel;
import md.intelectsoft.quickpos.phoneMode.ui.orders.BillListActivity;
import md.intelectsoft.quickpos.phoneMode.ui.shiftUI.ShiftListActivity;
import md.intelectsoft.quickpos.utils.BaseEnum;
import md.intelectsoft.quickpos.utils.CircleAnimationUtil;
import md.intelectsoft.quickpos.utils.IOnBackPressed;
import md.intelectsoft.quickpos.utils.Rfc2898DerivesBytes;
import md.intelectsoft.quickpos.utils.SPFHelp;
import md.intelectsoft.quickpos.utils.SearchView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivityPhone extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    String androidID, deviceName, publicIp, privateIp, deviceSN, osVersion, deviceModel, deviceId;
    private EposServiceAPI eposServiceAPI;
    private ProgressDialog progressDialog;
    private Realm mRealm;

    public static SalesViewModel salesViewModel;
    public static ShiftViewModel shiftViewModel;

    private GridView gridViewProducts;
    private AssortmentListGridAdapter adapter;
    private ImageView scanCode, changeColumns;
    private static Context context;
    private SearchView searchView;
    private MaterialButton buttonPay, buttonNewSales;
    private ImageButton buttonBackFolder;
    public static TextView textSelectCount;

    private String previousParentId;
    BrokerServiceAPI brokerServiceAPI;
    SimpleDateFormat simpleDateFormatMD;
    TimeZone timeZoneMD;
    TimerTask timerTaskSearchText;
    Timer timerSearch;
    NavigationView navigationView;
    DrawerLayout drawer;

    public static boolean isViewWithCatalog = true;
    int currentColumns;
    public static int countSelected = 1;
    String billId;
    private static boolean shiftOpenButtonPay;
    private static boolean shiftClosedButtonPay = false;

    public static String idShift = null;
    Shift shift;
    TextView operatorName;
    List<String> mList_Clicked_item =new ArrayList<>();
    List<String> lastClickedItemName =new ArrayList<>();
    int index_clecked_item = 0,index_clecked_item_name = 1, positionClicked;

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

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        progressDialog = new ProgressDialog(this);
        mRealm = Realm.getDefaultInstance();
        salesViewModel = new ViewModelProvider(this).get(SalesViewModel.class);
        shiftViewModel = new ViewModelProvider(this).get(ShiftViewModel.class);
        context = this;

        gridViewProducts = findViewById(R.id.gridProducts);
        scanCode = findViewById(R.id.imageScanBarcode);
        changeColumns = findViewById(R.id.imageChangeGridColumns);
        searchView = findViewById(R.id.searchProducts);
        buttonPay = findViewById(R.id.mtrbtn_pay_cart);
        buttonBackFolder = findViewById(R.id.imageButtonBackToFolderUp);
        buttonNewSales = findViewById(R.id.btn_new_sales);
        textSelectCount = findViewById(R.id.textSelectCountOfNext);
        operatorName = navigationView.getHeaderView(0).findViewById(R.id.textViewCasierName);

        simpleDateFormatMD = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        timeZoneMD = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormatMD.setTimeZone(timeZoneMD);

        String workPlace = SPFHelp.getInstance().getString("WorkplaceId", null);
        String uri = SPFHelp.getInstance().getString("URI", "0.0.0.0:1111");
        String tokenEPOS = SPFHelp.getInstance().getString("TokenEPOS", null);
        String licenseId = SPFHelp.getInstance().getString("LicenseID", null);
        brokerServiceAPI = BrokerRetrofitClient.getApiBrokerService();
        eposServiceAPI = EPOSRetrofitClient.getApiEposService(uri);
        if(workPlace == null){
            getWorkplaces(tokenEPOS);
        }

        deviceModel = Build.MODEL;
        deviceSN = Build.SERIAL;
        deviceName = Build.DEVICE;
        osVersion = Build.VERSION.RELEASE;
        androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        deviceId = SPFHelp.getInstance().getString("deviceId", "");
        publicIp = getPublicIPAddress(this);
        privateIp = getIPAddress(true);

        getURI(licenseId);

        if(SPFHelp.getInstance().getBoolean("ViewWithCatalog", false)){
            changeColumns.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid_black_24dp));
            isViewWithCatalog = true;
            gridViewProducts.setNumColumns(3);
        }
        else{
            changeColumns.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_black_24dp));
            isViewWithCatalog = false;
            gridViewProducts.setNumColumns(1);
        }

        showAssortment();

        lastClickedItemName.add(0,"Home");

        salesViewModel.getAssortment().observe(this, assortmentRealms -> {
            adapter = new AssortmentListGridAdapter(context, R.layout.item_grid_one_columns, assortmentRealms);
            gridViewProducts.setAdapter(adapter);

            adapter.setAssortmentItemActionListener(imageView -> {
                if (imageView != null){
                    if(!shiftOpenButtonPay){
                        if(!shiftClosedButtonPay)
                            makeFlyAnimation(imageView);
                        else
                            Toast.makeText(context, "Shift is expired!", Toast.LENGTH_SHORT).show();
                    }
                    else Toast.makeText(context, "Shift is not open!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        salesViewModel.getBillEntry().observe(this, bill -> {
            if(bill != null){
                billId = bill.getId();
                RealmList<BillString> lines = bill.getBillStrings();
                int countLines = 0;
                if(lines != null && lines.size() > 0)
                    for (BillString line : lines) {
                        if(line.isAllowNonInteger()){
                            countLines +=1;
                        }
                        else
                            countLines += line.getQuantity();
                    }
                double sum = bill.getTotalDiscount();

                buttonPay.setText(countLines + " items = " + String.format("%.2f", sum).replace(",","."));
            }
            else{
                billId = null;
                buttonPay.setText("0 item = 0.00 MDL");
            }
        });

        salesViewModel.getShift().observe(this, shiftM -> {
            if(shiftM == null){
                shiftOpenButtonPay = true;
                buttonPay.setText(R.string.text_open_shift);
                buttonPay.setBackgroundColor(context.getColor(R.color.btnPay));
                buttonPay.setTextColor(Color.WHITE);
            }
            else{
                idShift = shiftM.getId();
                shift = shiftM;
                if(!shiftM.isClosed() && new Date().getTime() > shiftM.getNeedClose() && shiftM.getNeedClose() != 0){
                    shiftClosedButtonPay = true;

                    buttonPay.setText(R.string.text_close_shift);
                    buttonPay.setBackgroundColor(context.getColor(R.color.btnPay));
                    buttonPay.setTextColor(Color.WHITE);

                    new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                            .setTitle(R.string.message_attention)
                            .setMessage(R.string.message_shift_expired_want_close)
                            .setCancelable(false)
                            .setPositiveButton(R.string.btn_yes, (dialogInterface, i) -> {
                                closeShift(shiftM);
                            })
                            .setNegativeButton(R.string.btn_no,((dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            }))
                            .show();
                }
                else
                    shiftOpenButtonPay = false;
            }
        });

        gridViewProducts.setOnItemClickListener((parent, view, position, id) -> {
            AssortmentRealm assortmentRealm = adapter.getItem(position);
            previousParentId = assortmentRealm.getParentID();
            positionClicked = position;

            if (assortmentRealm.isFolder()){
                mList_Clicked_item.add(index_clecked_item, previousParentId);
                lastClickedItemName.add(index_clecked_item_name, assortmentRealm.getName());
                index_clecked_item += 1;
                index_clecked_item_name +=1;

                buttonBackFolder.setVisibility(View.VISIBLE);
                salesViewModel.findAssortment(assortmentRealm.getId());
            }
        });

        buttonBackFolder.setOnClickListener(v -> {
            if (mList_Clicked_item.size() == 0) {
                buttonBackFolder.setVisibility(View.GONE);
            }
            else{
                index_clecked_item_name -= 2;
                index_clecked_item_name +=1;
                lastClickedItemName.remove(index_clecked_item_name);
                index_clecked_item -= 1;
                previousParentId = mList_Clicked_item.get(index_clecked_item);
                mList_Clicked_item.remove(index_clecked_item);

                if (mList_Clicked_item.size() == 0) {
                    buttonBackFolder.setVisibility(View.GONE);
                }
                salesViewModel.findAssortment(previousParentId);
            }
        });

        gridViewProducts.setOnItemLongClickListener((parent, view, position, id) -> {
            Dialog tedsxt = new Dialog(context);
            Log.e("TAG", "onCreateView: " + adapter.getItem(position).getName());
            return true;
        });

        scanCode.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent scanBarcodes = new Intent(context, ScanActivity.class);
                scanBarcodes.putExtra("billId",billId);
                startActivityForResult(scanBarcodes,303);
            }else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 201);
            }
        });

        changeColumns.setOnClickListener(v -> {
            currentColumns = gridViewProducts.getNumColumns();
            if(currentColumns > 1) {
                SPFHelp.getInstance().putBoolean("ViewWithCatalog", false);
                changeColumns.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_black_24dp));
                isViewWithCatalog = false;
                gridViewProducts.setNumColumns(1);
            }else {
                SPFHelp.getInstance().putBoolean("ViewWithCatalog", true);
                changeColumns.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid_black_24dp));
                isViewWithCatalog = true;
                gridViewProducts.setNumColumns(3);
            }
            adapter.notifyDataSetChanged();
        });

        EditText search = searchView.findViewById(R.id.search_input_text);
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0){
                    showAssortment();
                }
                else if(s.length() > 3){
                    if (timerSearch != null)
                        timerSearch.cancel();
                    timerSearch = new Timer();

                    startTimerSearchText(s.toString());
                    timerSearch.schedule(timerTaskSearchText, 600);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        buttonPay.setOnClickListener(v -> {
            if(shiftOpenButtonPay){
                buttonPay.setEnabled(false);
                new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .setTitle(R.string.message_attention)
                        .setMessage(R.string.message_open_shift)
                        .setCancelable(false)
                        .setPositiveButton(R.string.btn_yes, (dialogInterface, i) -> {
                            long opened_new_shift = new Date().getTime();
                            long setShiftDuring= SPFHelp.getInstance().getLong("ShiftDuringSettings",14400000);
                            long need_close = opened_new_shift + setShiftDuring;

                            int shiftNumber = SPFHelp.getInstance().getInt("ShiftGlobalNumber", 0);
                            shiftNumber += 1;

                            Shift shiftEntry = new Shift();
                            shiftEntry.setName("SHF " + simpleDateFormatMD.format(opened_new_shift));
                            shiftEntry.setWorkPlaceId(SPFHelp.getInstance().getString("WorkplaceId", null));
                            shiftEntry.setWorkPlaceName(SPFHelp.getInstance().getString("WorkplaceName", null));
                            shiftEntry.setAuthor(POSApplication.getApplication().getUserId());
                            shiftEntry.setAuthorName(POSApplication.getApplication().getUser().getFullName());
                            shiftEntry.setStartDate(new Date().getTime());
                            shiftEntry.setGlobalNumber(shiftNumber);
                            shiftEntry.setClosed(false);
                            shiftEntry.setNeedClose(need_close);
                            shiftEntry.setId(UUID.randomUUID().toString());

                            salesViewModel.updateShiftInfo(shiftEntry, false);

                            SPFHelp.getInstance().putInt("ShiftGlobalNumber", shiftNumber);
                            SPFHelp.getInstance().putInt("ShiftFiscalNumber", 0);

                            History history = new History();
                            history.setDate(new Date().getTime());
                            history.setMsg("Shift: " + shiftEntry.getName());
                            history.setType(BaseEnum.History_OpenShift);
                            salesViewModel.insertEntryLog(history);

                            POSApplication.getApplication().setShift(shiftEntry);
//                            startTimer(need_close - new Date().getTime());

                            shiftOpenButtonPay = false;
                            buttonPay.setEnabled(true);
                            buttonPay.setBackgroundColor(context.getColor(R.color.white));
                            buttonPay.setTextColor(context.getColor(R.color.colorPrimary));
                            buttonPay.setText("0 items = 0.00 MDL");
                        })
                        .setNegativeButton(R.string.btn_no,((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            buttonPay.setEnabled(true);
                        }))

                        .show();
            }
            else if(shiftClosedButtonPay){
                closeShift(shift);
            }
            else if(billId != null){
                Intent cart = new Intent(context, CartActivity.class);
                cart.putExtra("billId", billId);
                startActivityForResult(cart,456);
            }

        });

        buttonNewSales.setOnClickListener(v -> {
            salesViewModel.setNewSales();
        });

        textSelectCount.setOnClickListener(v -> {
            startActivityForResult(new Intent(context, CountSelectActivity.class), 147);
        });
    }

    private void getURI(String licenseID) {
        //data send to register app in broker server
        SendGetURI registerApplication = new SendGetURI();

        String ids = new UUID(androidID.hashCode(),androidID.hashCode()).toString();
        registerApplication.setDeviceID(ids);
        registerApplication.setDeviceModel(deviceModel);
        registerApplication.setDeviceName(deviceName);
        registerApplication.setSerialNumber(deviceSN);
        registerApplication.setPrivateIP(privateIp);
        registerApplication.setPublicIP(publicIp);
        registerApplication.setLicenseID(licenseID);
        registerApplication.setOSType(BrokerServiceEnum.Android);
        registerApplication.setApplicationVersion(getAppVersion(this));
        registerApplication.setProductType(131);
        registerApplication.setWorkPlace(SPFHelp.getInstance().getString("Cash",""));
        registerApplication.setSalePointAddress(SPFHelp.getInstance().getString("StationAddress",""));
        registerApplication.setLastAuthorizedUser(SPFHelp.getInstance().getString("Owner", ""));
        registerApplication.setOSVersion(osVersion);

        Call<RegisterApplication> getURICall = brokerServiceAPI.getURI(registerApplication);

        getURICall.enqueue(new Callback<RegisterApplication>() {
            @Override
            public void onResponse(Call<RegisterApplication> call, Response<RegisterApplication> response) {
                RegisterApplication result = response.body();
                if (result == null){
                    Toast.makeText(context, "Response empty from central service!", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(result.getErrorCode() == 0) {
                        String logo = null;
                        AppDataRegisterApplication appDataRegisterApplication = result.getAppData();
                        //if app registered successful , save installation id and company name
                        if(appDataRegisterApplication.getLogo() != null && !appDataRegisterApplication.getLogo().equals("")){
                            String photo = appDataRegisterApplication.getLogo();
                            if(photo != null && photo.length() > 0){
                                photo = photo.replace("data:image/","");
                                String typePhoto = photo.substring(0,3);

                                switch (typePhoto) {
                                    case "jpe":
                                        photo = photo.replace("jpeg;base64,", "");
                                        break;
                                    case "jpg":
                                        photo = photo.replace("jpg;base64,", "");
                                        break;
                                    case "png":
                                        photo = photo.replace("png;base64,", "");
                                        break;
                                }

                                logo = photo;
//                                byte[] decodedString = Base64.decode(logo, Base64.DEFAULT);
//                                Bitmap photoBm = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//                                imageMain.setImageBitmap(photoBm);
                            }
                        }
                        Map<String,String> licenseData = new HashMap<>();
                        licenseData.put("LicenseID",appDataRegisterApplication.getLicenseID());
                        licenseData.put("LicenseCode",appDataRegisterApplication.getLicenseCode());
                        licenseData.put("CompanyName",appDataRegisterApplication.getCompany());
                        licenseData.put("CompanyIDNO",appDataRegisterApplication.getIDNO());
                        licenseData.put("CompanyLogo", logo == null ? "" : logo);
                        SPFHelp.getInstance().putStrings(licenseData);

                        if(appDataRegisterApplication.getURI() != null && !appDataRegisterApplication.getURI().equals("") && appDataRegisterApplication.getURI().length() > 5) {
                            long nowDate = new Date().getTime();

                            String serverStringDate = appDataRegisterApplication.getServerDateTime();
                            serverStringDate = serverStringDate.replace("/Date(","");
                            serverStringDate = serverStringDate.replace("+0200)/","");
                            serverStringDate = serverStringDate.replace("+0300)/","");

                            long serverDate = Long.parseLong(serverStringDate);

                            SPFHelp.getInstance().putString("URI", appDataRegisterApplication.getURI());
                            SPFHelp.getInstance().putLong("DateReceiveURI", nowDate);
                            SPFHelp.getInstance().putLong("ServerDateTime", serverDate);

                        }else{
                            new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                    .setTitle("Adresa URL nu este setat!")
                                    .setMessage("Aplicatia nu este configurata complet!")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialogInterface, i) -> {
                                        finish();
                                    })
                                    .setNegativeButton("REPETA",((dialogInterface, i) -> {
                                        getURI(licenseID);
                                    }))
                                    .show();
                        }
                    }
                    else if(result.getErrorCode() == 133){

                        Map<String,String> licenseData = new HashMap<>();
                        licenseData.put("LicenseID", null);
                        licenseData.put("CompanyName", null);
                        licenseData.put("CompanyIDNO", null);
                        SPFHelp.getInstance().putString("CashId", null);

                        SPFHelp.getInstance().putStrings(licenseData);
                        SPFHelp.getInstance().putBoolean("KeepMeSigned", false);

                        new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                .setTitle("Aplicatia nu este activata!")
                                .setMessage("Aplicatia nu este activata! Va rugam sa activati pentru a putea continua!")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialogInterface, i) -> {
                                    finish();
                                })
                                .show();
                    }
                    else if(result.getErrorCode() == 134){
                        Map<String,String> licenseData = new HashMap<>();
                        licenseData.put("LicenseID", null);
                        licenseData.put("CompanyName", null);
                        licenseData.put("CompanyIDNO", null);
                        SPFHelp.getInstance().putString("CashId", null);

                        SPFHelp.getInstance().putStrings(licenseData);

                        new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                .setTitle("Licenta nu este activata!")
                                .setMessage("Licenta pentru aplicatie nu este activa.Va rugam sa verificati codul licentei: " + SPFHelp.getInstance().getString("LicenseCode","Nan"))
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialogInterface, i) -> {
                                    startActivity(new Intent(context, AuthorizeActivity.class));
                                    finish();
                                })
                                .show();
                    }
                    else if(result.getErrorCode() == 124){
                        Map<String,String> licenseData = new HashMap<>();
                        licenseData.put("LicenseID", null);
                        licenseData.put("CompanyName", null);
                        licenseData.put("CompanyIDNO", null);
                        SPFHelp.getInstance().putString("CashId", null);

                        SPFHelp.getInstance().putStrings(licenseData);

                        new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                .setTitle("Licenta nu a fost gasita!")
                                .setMessage("Licența pentru această aplicație nu există! Vă rugăm să incercati din nou activarea aplicatiei!")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialogInterface, i) -> {
                                    startActivity(new Intent(context, AuthorizeActivity.class));
                                    finish();
                                })
                                .show();
                    }
                    else {
                        Toast.makeText(context, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterApplication> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void closeShift(Shift shift) {

        long close = new Date().getTime();
        shift.setClosedBy(POSApplication.getApplication().getUserId());
        shift.setEndDate(close);
        shift.setClosed(true);
        shift.setClosedByName(POSApplication.getApplication().getUser().getFullName());
        shift.setSended(false);

        int result = salesViewModel.updateShiftInfo(shift, true);
        if(result > 0){
            new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setTitle(R.string.message_attention)
                    .setMessage(getString(R.string.message_cannot_close_shift_bill_active) + result + getString(R.string.message_open_bills))
                    .setCancelable(false)
                    .setPositiveButton(R.string.btn_ok, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        buttonPay.setEnabled(true);
                    })
                    .setNeutralButton("Go to", (dialog, which) -> {
                        Intent orders = new Intent(this, BillListActivity.class);
                        orders.putExtra("ShiftId", idShift);
                        startActivityForResult(orders,132);
                    })
                    .show();
        }
        else{
            shiftClosedButtonPay = false;
            shiftOpenButtonPay = true;
            buttonPay.setEnabled(true);
            buttonPay.setText(context.getString(R.string.text_open_shift));
        }
    }

    private void startTimerSearchText(final String newText) {
        timerTaskSearchText = new TimerTask() {
            @Override
            public void run() {
                MainActivityPhone.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        salesViewModel.searchProductsByText(newText);
                    }
                });
            }
        };
    }

    public static void showAssortment(){
        salesViewModel.findAssortment("00000000-0000-0000-0000-000000000000");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 456 || requestCode == 303){
            if(resultCode == RESULT_OK){
                salesViewModel.setNewSales();
            }
            else if(resultCode == 951){
                Map<String,String> licenseData = new HashMap<>();
                licenseData.put("LicenseID", null);
                licenseData.put("CompanyName", null);
                licenseData.put("CompanyIDNO", null);
                SPFHelp.getInstance().putString("CashId", null);

                SPFHelp.getInstance().putStrings(licenseData);

                new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .setTitle("Licenta nu este activata!")
                        .setMessage("Licenta pentru aplicatie nu este activa.Va rugam sa verificati codul licentei: " + SPFHelp.getInstance().getString("LicenseCode","Nan"))
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            startActivity(new Intent(context, AuthorizeActivity.class));
                            finish();
                        })
                        .show();
            }
        }
        else if(requestCode == 132){
            if(resultCode == 154){
                String id = data.getStringExtra("id");
                salesViewModel.getBillById(id);
            }
        }
        else if(requestCode == 147){
            if (resultCode == RESULT_OK){
                countSelected = data.getIntExtra("CountSelected" , 1);
                textSelectCount.setText("x " + countSelected);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 201){
            if(permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startActivityForResult(new Intent(context, ScanActivity.class),303);
            }
        }
    }

    public static void addToCart(AssortmentRealm assortmentRealm) {
        if(!shiftOpenButtonPay){
            if(!shiftClosedButtonPay){
                 if(countSelected == 1){
                salesViewModel.addProductToBill(assortmentRealm, 1);
            }
            else{
                salesViewModel.addProductToBill(assortmentRealm, countSelected);
                countSelected = 1;
                textSelectCount.setText("x 1");
            }
            }

            else
                Toast.makeText(context, "Shift is expired!", Toast.LENGTH_SHORT).show();
        }
        else Toast.makeText(context, "Shift is not open!", Toast.LENGTH_SHORT).show();
    }

    public static boolean isIsViewWithCatalog() {
        return isViewWithCatalog;
    }

    private void makeFlyAnimation(ImageView targetView) {

        MaterialButton destView = (MaterialButton) findViewById(R.id.mtrbtn_pay_cart);

        new CircleAnimationUtil().attachActivity(this).setTargetView(targetView).setMoveDuration(300).setDestView(destView).setAnimationListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //addItemToCart();
//                if(assortmentClicked != null){
////                    Toast.makeText(context, assortmentClicked.getName() + " added...", Toast.LENGTH_SHORT).show();
//                    salesViewModel.addProductToBill(assortmentClicked, 1);
//                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).startAnimation();
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

                            SimpleAdapter simpleAdapterWareHouses = new SimpleAdapter(MainActivityPhone.this, workplaceListToDialog ,android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});

                            new MaterialAlertDialogBuilder(MainActivityPhone.this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
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
        Call<AssortmentList> call = eposServiceAPI.getAssortmentList(tokenEPOS, workPlaceId);

        progressDialog.setMessage("Loading assortment list...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            call.cancel();
            //TODO show dialog with message that not sync
        });
        progressDialog.show();

        call.enqueue(new Callback<AssortmentList>() {
            @Override
            public void onResponse(Call<AssortmentList> call, Response<AssortmentList> response) {
                AssortmentList getAssortmentList = response.body();

                if(getAssortmentList != null && getAssortmentList.getErrorCode() == 0){
                    List<Assortment> assortments = getAssortmentList.getAssortments();

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
                            assortmentRealm.setVatCode(assortment.getVatCode());
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
                        if(getAssortmentList.getQuickGroups() != null){
                            for(QuickGroup quickGroup : getAssortmentList.getQuickGroups()){
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

                        showAssortment();

                        getUserSync(workPlaceId, tokenEPOS);
                    }
                    else onSyncError("Error download assortment, list is empty!");
                }
                else onSyncError("Error download assortment. Code: " + getAssortmentList.getErrorCode());
            }

            @Override
            public void onFailure(Call<AssortmentList> call, Throwable t) {
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
        if(user != null){
            POSApplication.getApplication().setUser(mRealm.copyFromRealm(user));
            operatorName.setText(POSApplication.getApplication().getUser().getFullName());
        }

        salesViewModel.getShiftInfo();
    }

    private void onFinishApp(String text){
        new MaterialAlertDialogBuilder(MainActivityPhone.this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
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

        new MaterialAlertDialogBuilder(MainActivityPhone.this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_orders) {
            Intent orders = new Intent(this, BillListActivity.class);
            orders.putExtra("ShiftId", idShift);
            startActivityForResult(orders,132);
        }
        else if(id == R.id.nav_shifts){
            Intent settings = new Intent(this, ShiftListActivity.class);
            startActivity(settings);
        }
        else if (id == R.id.nav_settings) {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
        }
        else if (id == R.id.nav_fin_rep) {
            Intent finRep = new Intent(this, FinancialReportsActivity.class);
            startActivity(finRep);
        }
//        else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }
//

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }

        return super.dispatchTouchEvent(event);
    }

    private String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }

    private String getPublicIPAddress(Context context) {
        //final NetworkInfo info = NetworkUtils.getNetworkInfo(context);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = cm.getActiveNetworkInfo();

        RunnableFuture<String> futureRun = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                if ((info != null && info.isAvailable()) && (info.isConnected())) {
                    StringBuilder response = new StringBuilder();

                    try {
                        HttpURLConnection urlConnection = (HttpURLConnection) (
                                new URL("http://checkip.amazonaws.com/").openConnection());
                        urlConnection.setRequestProperty("User-Agent", "Android-device");
                        //urlConnection.setRequestProperty("Connection", "close");
                        urlConnection.setReadTimeout(1000);
                        urlConnection.setConnectTimeout(1000);
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setRequestProperty("Content-type", "application/json");
                        urlConnection.connect();

                        int responseCode = urlConnection.getResponseCode();

                        if (responseCode == HttpURLConnection.HTTP_OK) {

                            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }

                        }
                        urlConnection.disconnect();
                        return response.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //Log.w(TAG, "No network available INTERNET OFF!");
                    return null;
                }
                return null;
            }
        });

        new Thread(futureRun).start();

        try {
            return futureRun.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getAppVersion(Context context){
        String result = "";

        try{
            result = context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName;
            result = result.replaceAll("[a-zA-Z] |-","");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }
}