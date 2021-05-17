package md.intelectsoft.quickpos.phoneMode.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.realm.Realm;
import md.intelectsoft.quickpos.BrokerService.Body.SendGetURI;
import md.intelectsoft.quickpos.BrokerService.Body.SendRegisterApplication;
import md.intelectsoft.quickpos.BrokerService.BrokerRetrofitClient;
import md.intelectsoft.quickpos.BrokerService.BrokerServiceAPI;
import md.intelectsoft.quickpos.BrokerService.Enum.BrokerServiceEnum;
import md.intelectsoft.quickpos.BrokerService.Results.AppDataRegisterApplication;
import md.intelectsoft.quickpos.BrokerService.Results.RegisterApplication;
import md.intelectsoft.quickpos.EPOSService.EPOSRetrofitClient;
import md.intelectsoft.quickpos.EPOSService.EposServiceAPI;
import md.intelectsoft.quickpos.EPOSService.Results.AuthenticateUser;
import md.intelectsoft.quickpos.EPOSService.Results.TokenEPOS;
import md.intelectsoft.quickpos.EPOSService.Results.User;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.POSApplication;
import md.intelectsoft.quickpos.utils.Rfc2898DerivesBytes;
import md.intelectsoft.quickpos.utils.SPFHelp;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("NonConstantResourceId")
public class AuthorizeActivity extends AppCompatActivity {
    @BindView(R.id.layoutActivateApp) ConstraintLayout registerForm;
    @BindView(R.id.layoutLoginToApp) ConstraintLayout authForm;
    // Register form views
    @BindView(R.id.layoutCode) TextInputLayout inputLayoutCode;
    @BindView(R.id.inputCode) TextInputEditText inputEditTextCode;
//    @BindView(R.id.progressBarAppActivate) ProgressBar progressBarActivate;
//    @BindView(R.id.backgroundAppActivate) View backgroundActivateApp;
    //Sign in form views
    @BindView(R.id.layoutLogin) TextInputLayout inputLayoutLogin;
    @BindView(R.id.layoutPasswordLogin) TextInputLayout inputLayoutPasswordLogin;
    @BindView(R.id.inputLogin) TextInputEditText inputEditTextLogin;
    @BindView(R.id.inputPasswordLogin) TextInputEditText inputEditTextPasswordLogin;

    String androidID, deviceName, publicIp, privateIp, deviceSN, osVersion, deviceModel;

    ProgressDialog progressDialog;
    BrokerServiceAPI brokerServiceAPI;
    Context context;

    @OnClick(R.id.buttonActivate) void registerApplication() {
        String activationCode = inputEditTextCode.getText().toString();
        preparedActivateApp(activationCode);
    }

    @OnClick(R.id.buttonLogin) void loginUser() {
        String login = inputEditTextLogin.getText().toString();
        String password = inputEditTextPasswordLogin.getText().toString();

        if(login.equals("") && password.equals("")){
            inputLayoutLogin.setError("Input the field!");
            inputLayoutPasswordLogin.setError("Input the field!");
        }
        else{
            if(login.equals("") || password.equals("")){
                if(login.equals(""))
                    inputLayoutLogin.setError("Input the field!");
                if(password.equals(""))
                    inputLayoutPasswordLogin.setError("Input the field!");
            }
            else{
                if(SPFHelp.getInstance().getBoolean("FirstStart", false))
                    authenticateUser(login, password);
                else{
                    String passwordGenerate = getSHA1HashUserPassword("This is the code for UserPass", password).replace("\n","");

                    //search in local data base user with such data
                    Realm realm = Realm.getDefaultInstance();

                    User userFind = realm.where(User.class).equalTo("userName", login).and().equalTo("password", passwordGenerate).findFirst();

                    if(userFind != null){
                        String token = SPFHelp.getInstance().getString("TokenEPOS",null);
                        long tokenValidDate = SPFHelp.getInstance().getLong("TokenValidTo",0);
                        Date dateToken = new Date(tokenValidDate);
                        Date currDate = new Date();

                        if(token == null || tokenValidDate == 0 || currDate.after(dateToken)){
                            authenticateUser(login,password);
                        }
                        else {
                            Intent main = new Intent(context, MainActivityPhone.class);
                            startActivity(main);
                            finish();
                        }
                    }
                    else{
                        Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progressDialog!= null && !this.isDestroyed())
            progressDialog.dismiss();
    }

    @OnCheckedChanged(R.id.checkRememberMe) void rememberMe(boolean b){
        SPFHelp.getInstance().putBoolean("KeepMeSigned", b);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));

        setContentView(R.layout.activity_auth_phone);

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        context = this;
        progressDialog = new ProgressDialog(context);
        brokerServiceAPI = BrokerRetrofitClient.getApiBrokerService();

        deviceModel = Build.MODEL;
        deviceSN = Build.SERIAL;
        deviceName = Build.DEVICE;
        osVersion = Build.VERSION.RELEASE;
        androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = new UUID(androidID.hashCode(), deviceName.hashCode()).toString();
        publicIp = getPublicIPAddress(this);
        privateIp = getIPAddress(true);

        String licenseID = SPFHelp.getInstance().getString("LicenseID", null);

        if(licenseID == null){
            authForm.setVisibility(View.GONE);
            registerForm.setVisibility(View.VISIBLE);
            SPFHelp.getInstance().putBoolean("FirstStart", true);
        }
        else{
            registerForm.setVisibility(View.GONE);
            authForm.setVisibility(View.VISIBLE);

            getURI(licenseID,false);
        }

        inputEditTextCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.equals(""))
                    inputLayoutCode.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

//        inputEditTextCode.setOnEditorActionListener(new EditText.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE){
//                    String activationCode = inputEditTextCode.getText().toString();
//                    preparedActivateApp(activationCode);
//                    return true;
//                }
//                else
//                    return false;
//            }
//        });
    }

    private void setWaitActivateViews(boolean show){
//        if(show){
//            backgroundActivateApp.setVisibility(View.VISIBLE);
//            progressBarActivate.setVisibility(View.VISIBLE);
//        }
//        else{
//            progressBarActivate.setVisibility(View.GONE);
//            backgroundActivateApp.setVisibility(View.GONE);
//        }
    }


    private void authenticateUser(String login, String password) {
        String uri = SPFHelp.getInstance().getString("URI", "0.0.0.0:1111");
        String licenseID = SPFHelp.getInstance().getString("LicenseID", null);

        EposServiceAPI eposServiceAPI = EPOSRetrofitClient.getApiEposService(uri);

        Call<AuthenticateUser> call = eposServiceAPI.authenticateUser(licenseID, login, password);

        progressDialog.setMessage("Authenticate user...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setButton(-1, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                call.cancel();
                if(call.isCanceled())
                    dialog.dismiss();
            }
        });
        progressDialog.show();

        call.enqueue(new Callback<AuthenticateUser>() {
            @Override
            public void onResponse(Call<AuthenticateUser> call, Response<AuthenticateUser> response) {
                AuthenticateUser authenticateUser = response.body();
                progressDialog.dismiss();
                if(authenticateUser != null && authenticateUser.getTokenEPOS() != null){
                    TokenEPOS tokenEPOS = authenticateUser.getTokenEPOS();
                    if(tokenEPOS.getErrorCode() == 0){
                        String date = tokenEPOS.getTokenValidTo();
                        date = date.replace("/Date(","");
                        date = date.replace("+0200)/","");
                        date = date.replace("+0300)/","");
                        long dateLong = Long.parseLong(date);

                        SPFHelp.getInstance().putString("TokenEPOS", tokenEPOS.getToken());
                        SPFHelp.getInstance().putLong("TokenValidTo", dateLong);

                        POSApplication.getApplication().setUserPasswordsNotHashed(password);
                        POSApplication.getApplication().setUserLogin(login);

                        SPFHelp.getInstance().putBoolean("FirstStart", false);
                        Intent main = new Intent(context, MainActivityPhone.class);
                        startActivity(main);
                        finish();
                    }
                    else{
                        Toast.makeText(context, "Error to authenticate user to server! "+ tokenEPOS.getErrorCode(), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(context, "Error to authenticate user to server! Response is empty!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthenticateUser> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, "Error to authenticate user to server! "+ t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void preparedActivateApp(String activationCode) {
        if(activationCode.equals(""))
            inputLayoutCode.setError("Input the field!");
        else{
            //data send to register app in broker server
            SendRegisterApplication registerApplication = new SendRegisterApplication();

            String ids = new UUID(androidID.hashCode(),androidID.hashCode()).toString();
            registerApplication.setDeviceID(ids);
            registerApplication.setDeviceModel(deviceModel);
            registerApplication.setDeviceName(deviceName);
            registerApplication.setSerialNumber(deviceSN);
            registerApplication.setPrivateIP(privateIp);
            registerApplication.setPublicIP(publicIp);
            registerApplication.setOSType(BrokerServiceEnum.Android);
            registerApplication.setApplicationVersion(getAppVersion(this));
            registerApplication.setProductType(BrokerServiceEnum.Retail);
            registerApplication.setOSVersion(osVersion);
            registerApplication.setLicenseActivationCode(activationCode);

            Log.e("TAG", "registerDeviceToBrokerService: "
                    + "setDeviceID: " + ids
                    + "\n setDeviceModel: " + deviceModel
                    + "\n setDeviceName: " + deviceName
                    + "\n setSerialNumber: " + deviceSN
                    + "\n setPrivateIP: " + privateIp
                    + "\n setPublicIP: " + publicIp
                    + "\n setOSType: " + BrokerServiceEnum.Android
                    + "\n setApplicationVersion: " + getAppVersion(this)
                    + "\n setProductType: " + BrokerServiceEnum.Retail
                    + "\n setOSVersion: " + osVersion
                    + "\n setLicenseActivationCode: " + activationCode);


            registerApplicationToBroker(registerApplication, activationCode);
        }
    }

    private void registerApplicationToBroker(SendRegisterApplication registerApplication, String activationCode) {
        Call<RegisterApplication> registerApplicationCall = brokerServiceAPI.registerApplicationCall(registerApplication);
        progressDialog.setMessage("Activate device...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setButton(-1, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                registerApplicationCall.cancel();
                if(registerApplicationCall.isCanceled())
                    dialog.dismiss();
            }
        });
        progressDialog.show();

        registerApplicationCall.enqueue(new Callback<RegisterApplication>() {
            @Override
            public void onResponse(Call<RegisterApplication> call, Response<RegisterApplication> response) {
                RegisterApplication result = response.body();

                if (result == null){
                    progressDialog.dismiss();
                    Toast.makeText(context, "Response from broker server is null!", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(result.getErrorCode() == 0) {
                        AppDataRegisterApplication appDataRegisterApplication = result.getAppData();
                        //if app registered successful , save installation id and company name
                        Map<String,String> licenseData = new HashMap<>();
                        licenseData.put("LicenseID", appDataRegisterApplication.getLicenseID());
                        licenseData.put("LicenseCode", appDataRegisterApplication.getLicenseCode());
                        licenseData.put("CompanyName", appDataRegisterApplication.getCompany());
                        licenseData.put("CompanyIDNO", appDataRegisterApplication.getIDNO());
                        licenseData.put("LicenseActivationCode", activationCode);

                        SPFHelp.getInstance().putStrings(licenseData);

                        //after register app ,get URI for accounting system on broker server
                        progressDialog.dismiss();

                        registerForm.setVisibility(View.GONE);
                        authForm.setVisibility(View.VISIBLE);

                        if(appDataRegisterApplication.getURI() != null && !appDataRegisterApplication.getURI().equals("") && appDataRegisterApplication.getURI().length() > 5){
                            long nowDate = new Date().getTime();
                            String serverStringDate = appDataRegisterApplication.getServerDateTime();
                            serverStringDate = serverStringDate.replace("/Date(","");
                            serverStringDate = serverStringDate.replace("+0200)/","");
                            serverStringDate = serverStringDate.replace("+0300)/","");

                            long serverDate = Long.parseLong(serverStringDate);

                            SPFHelp.getInstance().putString("URI", appDataRegisterApplication.getURI());
                            SPFHelp.getInstance().putLong("DateReceiveURI", nowDate);
                            SPFHelp.getInstance().putLong("ServerDateTime", serverDate);
                        }
                        else{
                            new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                    .setTitle("URL not set!")
                                    .setMessage("The application is not fully configured.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialogInterface, i) -> {
                                        finish();
                                    })
                                    .setNegativeButton("Retry",((dialogInterface, i) -> {
                                        getURI(appDataRegisterApplication.getLicenseID(), true);
                                    }))
                                    .show();

                        }
                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(context, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterApplication> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, "Failure get URI: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getURI(String licenseID, boolean fromRegistration) {
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
        registerApplication.setProductType(BrokerServiceEnum.SalesAgent);
        registerApplication.setOSVersion(osVersion);

        Call<RegisterApplication> getURICall = brokerServiceAPI.getURI(registerApplication);

        if (fromRegistration) {
            progressDialog.setMessage("Obtain URI...");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setButton(-1, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getURICall.cancel();
                    if (getURICall.isCanceled())
                        dialog.dismiss();
                }
            });
            progressDialog.show();
        }

        getURICall.enqueue(new Callback<RegisterApplication>() {
            @Override
            public void onResponse(Call<RegisterApplication> call, Response<RegisterApplication> response) {
                RegisterApplication result = response.body();
                if (result == null){
                    progressDialog.dismiss();
                    Toast.makeText(context, "Response from broker server is null!", Toast.LENGTH_SHORT).show();
                    //check installation id if valid from broker service
                    checkApplicationToUse();
                }
                else{
                    if(result.getErrorCode() == 0) {
                        AppDataRegisterApplication appDataRegisterApplication = result.getAppData();
                        //if app registered successful , save installation id and company name

                        Map<String,String> licenseData = new HashMap<>();
                        licenseData.put("LicenseID",appDataRegisterApplication.getLicenseID());
                        licenseData.put("LicenseCode",appDataRegisterApplication.getLicenseCode());
                        licenseData.put("CompanyName",appDataRegisterApplication.getCompany());
                        licenseData.put("CompanyIDNO",appDataRegisterApplication.getIDNO());

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

                            //check installation id if valid from broker service
                            checkApplicationToUse();
                        }else{
                            new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                    .setTitle("URL not set!")
                                    .setMessage("The application is not fully configured.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialogInterface, i) -> {
                                        finish();
                                    })
                                    .setNegativeButton("Retry",((dialogInterface, i) -> {
                                        getURI(licenseID, fromRegistration);
                                    }))
                                    .show();
                        }
                    }
                    else if(result.getErrorCode() == 133){

                        Map<String,String> licenseData = new HashMap<>();
                        licenseData.put("LicenseID", null);
                        licenseData.put("LicenseCode", null);
                        licenseData.put("CompanyName", null);
                        licenseData.put("CompanyIDNO", null);

                        SPFHelp.getInstance().putStrings(licenseData);
                        SPFHelp.getInstance().putBoolean("KeepMeSigned", false);

                        new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                .setTitle("Application not activated!")
                                .setMessage("The application is not activated! Please activate can you continue.")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialogInterface, i) -> {
                                    finish();
                                })
                                .show();
                    }
                    else if(result.getErrorCode() == 134){
                        Map<String,String> licenseData = new HashMap<>();
                        licenseData.put("LicenseID", null);
                        licenseData.put("LicenseCode", null);
                        licenseData.put("CompanyName", null);
                        licenseData.put("CompanyIDNO", null);

                        SPFHelp.getInstance().putStrings(licenseData);
                        SPFHelp.getInstance().putBoolean("KeepMeSigned", false);

                        new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                .setTitle("License not activated!")
                                .setMessage("The license for this application not activated! Please activate can you continue.")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialogInterface, i) -> {
                                    authForm.setVisibility(View.GONE);
                                    registerForm.setVisibility(View.VISIBLE);
                                })
                                .setNegativeButton("Cancel",((dialogInterface, i) -> {
                                    finish();
                                }))
                                .show();
                    }
                    else {
                        Toast.makeText(context, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        //check installation id if valid from broker service
                        checkApplicationToUse();
                    }
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<RegisterApplication> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();

                //check installation id if valid from broker service
                checkApplicationToUse();
            }
        });
    }

    private void checkApplicationToUse() {
        boolean restriction = false;

        // get all date for check it
        long dateReceiveURI = SPFHelp.getInstance().getLong("DateReceiveURI",0);
        long oneDay = 86400000;
        long dateLimitCanUseApp = dateReceiveURI + (oneDay * 30);
        long brokerServerDate = SPFHelp.getInstance().getLong("ServerDateTime",0);
        long currentDate = new Date().getTime();

        //check if user can use application
        restriction = currentDate < dateLimitCanUseApp && currentDate > brokerServerDate;

        //check if user can use application
        if (restriction){
            //TODO add restriction
        }
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
}