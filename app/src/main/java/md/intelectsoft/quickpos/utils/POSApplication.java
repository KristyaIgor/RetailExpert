package md.intelectsoft.quickpos.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.datecs.fiscalprinter.SDK.FiscalException;
import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.vfi.smartpos.deviceservice.aidl.IDeviceService;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import md.intelectsoft.quickpos.EPOSService.Body.BillLine;
import md.intelectsoft.quickpos.EPOSService.Body.BillPayment;
import md.intelectsoft.quickpos.EPOSService.Body.BillRemote;
import md.intelectsoft.quickpos.EPOSService.Body.SaveBills;
import md.intelectsoft.quickpos.EPOSService.Body.SaveShift;
import md.intelectsoft.quickpos.EPOSService.Body.ShiftRemote;
import md.intelectsoft.quickpos.EPOSService.EPOSRetrofitClient;
import md.intelectsoft.quickpos.EPOSService.EposServiceAPI;
import md.intelectsoft.quickpos.EPOSService.Results.PaymentType;
import md.intelectsoft.quickpos.EPOSService.Results.SimpleResponseEPOS;
import md.intelectsoft.quickpos.EPOSService.Results.User;
import md.intelectsoft.quickpos.FiscalService.Body.BillLineFiscalService;
import md.intelectsoft.quickpos.FiscalService.Body.BillPaymentFiscalService;
import md.intelectsoft.quickpos.FiscalService.Body.PrintBillFiscalService;
import md.intelectsoft.quickpos.FiscalService.FiscalServiceAPI;
import md.intelectsoft.quickpos.FiscalService.FiscalServiceRetrofitClient;
import md.intelectsoft.quickpos.FiscalService.Result.SimpleResult;
import md.intelectsoft.quickpos.Realm.RealmMigrations;
import md.intelectsoft.quickpos.Realm.localStorage.Bill;
import md.intelectsoft.quickpos.Realm.localStorage.BillPaymentType;
import md.intelectsoft.quickpos.Realm.localStorage.BillString;
import md.intelectsoft.quickpos.Realm.localStorage.Shift;
import md.intelectsoft.quickpos.tabledMode.activity.MainTabledActivity;
import md.intelectsoft.quickpos.verifone.Utilities.DeviceHelper;
import md.intelectsoft.quickpos.verifone.Utilities.ToastUtil;
import md.intelectsoft.quickpos.verifone.transaction.AppParams;
import md.intelectsoft.quickpos.verifone.transaction.TransBasic;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice.getConnectedModelV2;

public class POSApplication extends Application {

    private static POSApplication application;

    private static boolean isVFServiceConnected = false;
    private static boolean deviceIsFiscal = false;
    private static IDeviceService deviceService;  //info about device

    private String word;

    //service connection for verifone service
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("TAG", "onServiceConnected, DeviceHelper, TransBasic,AppParams init");
            deviceService = IDeviceService.Stub.asInterface(service);
            DeviceHelper.getInstance().initDeviceHelper(POSApplication.this);
            TransBasic.getInstance().initTransBasic(handler , POSApplication.this);
            AppParams.getInstance().initAppParam();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("TAG", name.getPackageName() + " is disconnected");
            deviceService = null;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        bindDeviceService();
        application = this;
        ToastUtil.init(getApplicationContext());
        Realm.init(this);

        RealmConfiguration configuration = new RealmConfiguration.Builder().name("quickpos.realm").schemaVersion(1).migration(new RealmMigrations()).build();
        Realm.setDefaultConfiguration(configuration);
        Realm.getInstance(configuration);

        mRealm  = Realm.getDefaultInstance();

        word = SPFHelp.getInstance().getString("WordTime", null);

        if(word == null) {
            KeyGenerator keyGenerator;
            SecretKey myWord;
            try {
                keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(256);
                myWord = keyGenerator.generateKey();

                word = funEncodeWord(myWord.getEncoded());

                SPFHelp.getInstance().putString("WordTime" , word);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    public static POSApplication getApplication() {
        return application;
    }

    public IDeviceService getDeviceService(){
        return deviceService;
    }

    private String funEncodeWord (byte[] enVal) {
        String conVal= Base64.encodeToString(enVal,Base64.DEFAULT);
        return conVal;
    }
    private byte[] funDecodeWord (String decVal) {
        byte[] conVal = Base64.decode(decVal,Base64.DEFAULT);
        return conVal;

    }

    public byte[] getWordTime() {
        return funDecodeWord(word);
    }


    //bind to device service verifone
    private void bindDeviceService(){
        if (null != deviceService) {
            return;
        }

        Intent intent = new Intent();
        intent.setAction("com.vfi.smartpos.device_service");
        intent.setPackage("com.vfi.smartpos.deviceservice");
        // or
//        ComponentName componentName = new ComponentName("com.vfi.smartpos.deviceservice", "com.verifone.smartpos.service.VerifoneDeviceService");
//        intent.setComponent(componentName);

        isVFServiceConnected = bindService(intent, connection, Context.BIND_AUTO_CREATE);
        if (!isVFServiceConnected) {
            Log.i("TAG", "deviceService bind failed");
        } else {
            Log.i("TAG", "deviceService bind success");
        }
    }

    public static boolean isVFServiceConnected() {
        return isVFServiceConnected;
    }

    public static void setVFServiceConnected(boolean isVFServiceConnected) {
        isVFServiceConnected = isVFServiceConnected;
    }

    // log & display
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("TAG", msg.getData().getString("msg"));
            Toast.makeText(POSApplication.this, msg.getData().getString("msg"), Toast.LENGTH_SHORT).show();
        }
    };


    // __________________________________ This code for tabled methode work____________________________________________________________


    public void checkUpdates(){
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(6)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);

        //defaultvalue
        Map<String,Object> defaultValue = new HashMap<>();
        defaultValue.put(UpdateHelper.KEY_UPDATE_URL,"https://intelectsoft.md/androidapps/cash.apk");
        defaultValue.put(UpdateHelper.KEY_UPDATE_VERSION,"1.0");
        defaultValue.put(UpdateHelper.KEY_UPDATE_ENABLE,false);
        defaultValue.put(UpdateHelper.KEY_UPDATE_CHANGES,"");

        defaultValue.put(UpdateHelper.KEY_UPDATE_TRIAL_URL,"https://intelectsoft.md/androidapps/cash_trial.apk");
        defaultValue.put(UpdateHelper.KEY_UPDATE_TRIAL_VERSION,"1.1");
        defaultValue.put(UpdateHelper.KEY_UPDATE_TRIAL_ENABLE,false);
        defaultValue.put(UpdateHelper.KEY_UPDATE_TRIAL_CHANGES,"");

        remoteConfig.setDefaultsAsync(defaultValue);

        remoteConfig.fetch(6).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("TAG", "remote config is fetched.");
                    remoteConfig.activate();
                }
            }
        });
    }

    @Override
    public void onTerminate() {
        Realm.getDefaultInstance().close();
        if(timerSyncBill !=null)
            timerSyncBill.cancel();

        super.onTerminate();
    }

    private Realm mRealm;

    private TimerTask timerTaskSyncBill;
    private Timer timerSyncBill;
    private TimerTask timerTaskSyncBackground;
    private Timer syncBackground;

    public DatecsFiscalDevice myFiscalDevice = null;
    private User user = null;
    private String userPassWithoutHash, login;
    private Shift shift = null;

    //SharedPreference name
    public static final String SharedPrefSyncSettings = "SyncSetting";
    public static final String SharedPrefFiscalService = "FiscalService";
    public static final String SharedPrefSettings = "Settings";
    public static final String SharedPrefWorkPlaceSettings = "WorkPlace";

    //SharedPreference variable
    public static final String deviceId = "DeviceId";

    private UsbDevice deviceFiscal;

    public void setUser(User user235) {
        this.user = user235;
    }
    public User getUser() {
        return user;
    }
    public String getUserId(){
        return user.getId();
    }
    public String getUserPasswordsNotHashed(){
        return userPassWithoutHash;
    }
    public void setUserPasswordsNotHashed(String pass){
        this.userPassWithoutHash = pass;
    }


    public Shift getShift() {
        return shift;
    }
    public String getShiftID(){
        return shift.getId();
    }
    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public UsbDevice getDeviceFiscal() {
        return deviceFiscal;
    }

    public void setDeviceFiscal(UsbDevice deviceFiscal) {
        this.deviceFiscal = deviceFiscal;
    }

    public DatecsFiscalDevice getMyFiscalDevice() {
        return myFiscalDevice;
    }
    public void setMyFiscalDevice(DatecsFiscalDevice myFiscalDevice) {
        this.myFiscalDevice = myFiscalDevice;
    }

    public int printFiscalReceipt(cmdReceipt.FiscalReceipt fiscalReceipt, RealmList<BillString> billString, PaymentType paymentType, double suma, RealmList<BillPaymentType> paymentList, int number){
        String resCloseBill = "0";
        //                String resFreText = getConnectedModelV2().customCommand(54,"Intelect Soft");
        try {
            if (!fiscalReceipt.isOpen()) {

                //Open Fiscal bon in current receipt
                String resultStringOpenReceipt = openFiscalReceipt("30","000","1","");

                String result = getConnectedModelV2().customCommand(48,resultStringOpenReceipt);
                String numberString = String.valueOf(number);
                fiscalReceipt.printFreeText(numberString);
                //print bill strings
                for(BillString billStringEntry: billString){
                    String name = billStringEntry.getAssortmentFullName();
                    if(name.length() > 25)
                        name = name.substring(0,24);
                    String taxCod = String.format("%.0f", billStringEntry.getVat());
                    String codeVat = "1";
                    if(taxCod.equals("20"))
                        codeVat = "1";
                    else if (taxCod.equals("8"))
                        codeVat = "2";
                    else
                        codeVat = "3";
                    String price = String.format("%.2f", billStringEntry.getPrice()).replace(",",".");
                    String count = String.format("%.2f", billStringEntry.getQuantity()).replace(",",".");

                    double discVal = billStringEntry.getSum() - billStringEntry.getSumWithDiscount();

                    String resultStringRegSales = "";
                    if(discVal == 0) {
                        resultStringRegSales = regSalesVar0Version0(name,codeVat,price,count,"","","0");
                    }
                    else
                        resultStringRegSales = regSalesVar0Version0(name,codeVat,price,count,"4",String.format("%.2f", discVal).replace(",","."),"0");

                    //inregistrez pozitia in bonul fiscal
                    getConnectedModelV2().customCommand(49,resultStringRegSales);
                }

                if(!paymentList.isEmpty()){
                    for(BillPaymentType billPaymentType:paymentList){
                        String code = String.valueOf(billPaymentType.getPaymentCode());
                        String summ = String.format("%.2f", billPaymentType.getSum()).replace(",",".");

                        //Pay in calc TOTAL receip
                        String PayBill = PaymentBill(code,summ,"1");

                        //payment
                        getConnectedModelV2().customCommand(53,PayBill);
                    }
                    String code = String.valueOf(paymentType.getCode());
                    String summ = String.format("%.2f",suma).replace(",",".");

                    //Pay in calc TOTAL receip
                    String PayBill = PaymentBill(code,summ,"1");

                    //payment
                    getConnectedModelV2().customCommand(53,PayBill);
                }
                else{
                    String code = String.valueOf(paymentType.getCode());
                    String summ = String.format("%.2f",suma).replace(",",".");

                    //Pay in calc TOTAL receip
                    String PayBill = PaymentBill(code,summ,"1");

                    //payment
                    getConnectedModelV2().customCommand(53,PayBill);
                }

                fiscalReceipt.printFreeText("Intelect Soft S.R.L.");
                //close bill
                resCloseBill = getConnectedModelV2().customCommand(56,"");

                String erroreCod = Character.toString(resCloseBill.charAt(0));

                if(erroreCod.equals("0")){
                    char[] myNameChars = resCloseBill.toCharArray();
                    myNameChars[0] = ' ';
                    resCloseBill = String.valueOf(myNameChars);

                    resCloseBill = resCloseBill.trim();

                    //TODO close bill message succes
//                    postToast("Receipt number: " + resCloseBill + " was printed");

                    return Integer.valueOf(resCloseBill);
                }
                else{
                    return 0;
                }
            }
            else {
                boolean isCanceled = cancelSale(fiscalReceipt);

                if(!isCanceled){
                    //Open Fiscal bon in current receipt
                    String resultStringOpenReceipt = openFiscalReceipt("30","000","1","");

                    String result = getConnectedModelV2().customCommand(48,resultStringOpenReceipt);
                    String numberString = String.valueOf(number);
                    fiscalReceipt.printFreeText(numberString);
                    //print bill strings
                    for(BillString billStringEntry: billString){
                        String name = billStringEntry.getAssortmentFullName();
                        if(name.length() > 25)
                            name = name.substring(0,24);
                        String taxCod = String.format("%.0f", billStringEntry.getVat());
                        String codeVat = "1";
                        if(taxCod.equals("20"))
                            codeVat = "1";
                        else if (taxCod.equals("8"))
                            codeVat = "2";
                        else
                            codeVat = "3";
                        String price = String.format("%.2f", billStringEntry.getPrice()).replace(",",".");
                        String count = String.format("%.2f", billStringEntry.getQuantity()).replace(",",".");

                        double discVal = billStringEntry.getSum() - billStringEntry.getSumWithDiscount();

                        String resultStringRegSales = "";
                        if(discVal == 0) {
                            resultStringRegSales = regSalesVar0Version0(name,codeVat,price,count,"","","0");
                        }
                        else
                            resultStringRegSales = regSalesVar0Version0(name,codeVat,price,count,"4",String.format("%.2f", discVal).replace(",","."),"0");

                        //inregistrez pozitia in bonul fiscal
                        getConnectedModelV2().customCommand(49,resultStringRegSales);
                    }

                    if(!paymentList.isEmpty()){
                        for(BillPaymentType billPaymentType:paymentList){
                            String code = String.valueOf(billPaymentType.getPaymentCode());
                            String summ = String.format("%.2f", billPaymentType.getSum()).replace(",",".");

                            //Pay in calc TOTAL receip
                            String PayBill = PaymentBill(code,summ,"1");

                            //payment
                            getConnectedModelV2().customCommand(53,PayBill);
                        }
                        String code = String.valueOf(paymentType.getCode());
                        String summ = String.format("%.2f",suma).replace(",",".");

                        //Pay in calc TOTAL receip
                        String PayBill = PaymentBill(code,summ,"1");

                        //payment
                        getConnectedModelV2().customCommand(53,PayBill);
                    }
                    else{
                        String code = String.valueOf(paymentType.getCode());
                        String summ = String.format("%.2f",suma).replace(",",".");

                        //Pay in calc TOTAL receip
                        String PayBill = PaymentBill(code,summ,"1");

                        //payment
                        getConnectedModelV2().customCommand(53,PayBill);
                    }

                    fiscalReceipt.printFreeText("Intelect Soft S.R.L.");
                    //close bill
                    resCloseBill = getConnectedModelV2().customCommand(56,"");

                    String erroreCod = Character.toString(resCloseBill.charAt(0));

                    if(erroreCod.equals("0")){
                        char[] myNameChars = resCloseBill.toCharArray();
                        myNameChars[0] = ' ';
                        resCloseBill = String.valueOf(myNameChars);

                        resCloseBill = resCloseBill.trim();

                        //TODO close bill message succes
//                    postToast("Receipt number: " + resCloseBill + " was printed");

                        return Integer.valueOf(resCloseBill);
                    }
                    else{
                        return 0;
                    }
                }
                else{
                    return Integer.valueOf(resCloseBill);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            if(e.getMessage() != null){

                String erMsg = e.getMessage();
                erMsg = erMsg.replace("\n\r","");

                String er = "";

                try{
                    er = erMsg.substring(0,7);
                }
                catch (Exception e1){
                    er = erMsg.substring(0,7);
                }

                int errore = Integer.parseInt(er);

                String msg = FiscalException.locale(errore,1073741824);
                postToast(msg);
            }
            return Integer.valueOf(resCloseBill);
        }
    }
    public void printReceiptFiscalService (RealmList<BillString> billString, PaymentType paymentType, double suma, RealmList<BillPaymentType> paymentList,String numberBill){
        List<BillPaymentFiscalService> paymentFiscalServices = new ArrayList<>();
        List<BillLineFiscalService> lineFiscalService = new ArrayList<>();
        PrintBillFiscalService printBillFiscalService = new PrintBillFiscalService();
        printBillFiscalService.setHeaderText(user.getFirstName() + " " +  user.getLastName());
        printBillFiscalService.setNumber(numberBill);



        for(BillString billStringEntry: billString){
            BillLineFiscalService billLineFiscalService = new BillLineFiscalService();

            String taxCod = String.format("%.0f", billStringEntry.getVat());
            String codeVat = "1";
            if(taxCod.equals("20"))
                codeVat = "1";
            else if (taxCod.equals("8"))
                codeVat = "2";
            else
                codeVat = "3";

            billLineFiscalService.setAmount(billStringEntry.getQuantity());
            billLineFiscalService.setName(billStringEntry.getAssortmentFullName());
            billLineFiscalService.setPrice(billStringEntry.getPriceWithDiscount());
            if(billStringEntry.getPrice() != billStringEntry.getPriceWithDiscount()){
                billLineFiscalService.setDiscount(billStringEntry.getPrice() - billStringEntry.getPriceWithDiscount());
            }
            billLineFiscalService.setVAT(codeVat);

            lineFiscalService.add(billLineFiscalService);
        }

        if(!paymentList.isEmpty()){
            for(BillPaymentType billPaymentType:paymentList){
                BillPaymentFiscalService billPaymentFiscalService = new BillPaymentFiscalService();

                billPaymentFiscalService.setCode(String.valueOf(paymentType.getCode()));
                billPaymentFiscalService.setPaymentSum(billPaymentType.getSum());

                paymentFiscalServices.add(billPaymentFiscalService);
            }
            BillPaymentFiscalService billPaymentFiscalService = new BillPaymentFiscalService();

            billPaymentFiscalService.setPaymentSum(suma);
            billPaymentFiscalService.setCode("1");
//            billPaymentFiscalService.setCode(String.valueOf(paymentType.getCode()));

            paymentFiscalServices.add(billPaymentFiscalService);
        }
        else{
            BillPaymentFiscalService billPaymentFiscalService = new BillPaymentFiscalService();

            billPaymentFiscalService.setPaymentSum(suma);
            billPaymentFiscalService.setCode(String.valueOf(paymentType.getCode()));

            paymentFiscalServices.add(billPaymentFiscalService);
        }

        printBillFiscalService.setLines(lineFiscalService);
        printBillFiscalService.setPayments(paymentFiscalServices);

        String uri = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getString("FiscalServiceAddress","0.0.0.0:1111");



        FiscalServiceAPI apiFiscalService = FiscalServiceRetrofitClient.getApiFiscalService(uri);
        Call<SimpleResult> call = apiFiscalService.printBill(printBillFiscalService);

        call.enqueue(new Callback<SimpleResult>() {
            @Override
            public void onResponse(Call<SimpleResult> call, Response<SimpleResult> response) {
                SimpleResult result1 = response.body();
                if(result1 != null){
                    if(result1.getErrorCode() == 0){
                        MainTabledActivity.doAfterCloseBill();
                    }
                    else{
                        postToast("Error code: " + result1.getErrorCode());
                    }
                }
            }

            @Override
            public void onFailure(Call<SimpleResult> call, Throwable t) {
                postToast("Error service: " + t.getMessage());
            }
        });
    }

    //fiscal device operation
    private String regSalesVar0Version0 (String PluName, String taxCod,String price,String count,String DiscType,String Discval,String Departament){
        String InputStringRegSales = "";
        InputStringRegSales = InputStringRegSales + PluName + "\t";
        InputStringRegSales = InputStringRegSales + taxCod + "\t";
        InputStringRegSales = InputStringRegSales + price + "\t";
        InputStringRegSales = InputStringRegSales + count + "\t";
        InputStringRegSales = InputStringRegSales + DiscType + "\t";
        InputStringRegSales = InputStringRegSales + Discval + "\t";
        InputStringRegSales = InputStringRegSales + Departament + "\t";

        /*Mandatory parameters: PluName, TaxCd, Price
             PluName - Name of product, up to 72 characters not empty string;
             TaxCd - Tax code;
             '1' - vat group A;
             '2' - vat group B;
             '3' - vat group C;
             '4' - vat group D;
             '5' - vat group E;
             '6' - vat group F;
             '7' - vat group G;
             '8' - vat group H;
             Price - Product price, with sign '-' at void operations. Format: 2 decimals; up to *9999999.99
             Department - Number of the department 0..99; If '0' - Without department;
            Optional parameters: Quantity, DiscountType, DiscountValue
             Quantity - Quantity of the product ( default: 1.000 ); Format: 3 decimals; up to *999999.999
             Unit - Unit name, up to 6 characters not empty string;
        !!! Max value of Price * Quantity is *9999999.99. !!!
             DiscountType - type of discount.
             '0' or empty - no discount;
             '1' - surcharge by percentage;
             '2' - discount by percentage;
             '3' - surcharge by sum;
             '4' - discount by sum; If DiscountType is non zero, DiscountValue have to contain value. The format must be a value with two decimals.
             DiscountValue - value of discount.
             a number from 0.01 to 9999999.99 for sum operations;
             a number from 0.01 to 99.99 for percentage operations;
            Note
            If DiscountType is zero or empty, parameter DiscountValue must be empty.
         */

        return InputStringRegSales;
    }
    private String openFiscalReceipt ( String OperCode,String OperPass,String TillNumber,String Invoice){
        String InputString = "";
        InputString = InputString + OperCode + "\t";             //Operator number from 1...30;
        InputString = InputString + OperPass + "\t";             //Operator password, ascii string of digits. Lenght from 1...8;
        InputString = InputString + TillNumber + "\t";           // Number of point of sale from 1...99999;
        InputString = InputString + Invoice + "\t";              //If this parameter has value 'I' it opens an invoice receipt. If left blank it opens fiscal receipt;

        return InputString;
    }
    private String PaymentBill ( String payMode, String amount, String type){
        String PayBill = "";
        PayBill = PayBill + payMode + "\t";         //Type of payment; '0' - cash; '1' - credit card; '2' - debit card; '3' - other pay#3 '4' - other pay#4 '5' - other pay#5
        PayBill = PayBill + amount + "\t";          // Amount to pay ( 0.00...9999999.99 or 0...999999999 depending dec point position );
        PayBill = PayBill + type;            //Type of card payment. Only for payment with debit card;   '1' - with money; '12'- with points from loyal scheme;

        return PayBill;

    }

    private boolean cancelSale(final cmdReceipt.FiscalReceipt fiscalReceipt) {
        try {
            if (fiscalReceipt.isOpen()) {

//                fiscalReceipt.closeFiscalReceipt();
                fiscalReceipt.cancel();
                return fiscalReceipt.isOpen();
            }
            else
                return true;

//            if (fiscalReceipt.isOpen()) {
//                postToast("fiscal is open a doua oara");
//                final Double owedSum = new cmdReceipt.FiscalTransaction().getNotPaid();//owedSum=Amount-Tender
//                Double payedSum = new cmdReceipt.FiscalTransaction().getPaid();
//                //If a TOTAL in the opened receipt has not been set, it will be canceled
//                if (payedSum == 0.0) {
//                    fiscalReceipt.cancel();
//                    return;
//                }
//
//                //If a TOTAL is set with a partial payment, there is a Amount and Tender is positive.
//                //Offer payment of the amount and completion of the sale.
//                if (owedSum > 0.0) {
//                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//                    dialog.setTitle("Cancel opened receipt.");
//                    String sQuestion = String.format("Cannot cancel receipt, payment has already started.\n\r" +
//                            "Doц you want to pay the owed sum: %2.2f -and close it?", owedSum);
//                    dialog.setMessage(sQuestion);
//                    dialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            try {
//                                //TOTAL
//                                new cmdReceipt.FiscalSale().saleTotal(
//                                        cmdReceipt.FiscalSale.PaymentType.cash,
//                                        "0.0"    //Whole sum
//                                );
//                                fiscalReceipt.closeFiscalReceipt();
//                            } catch (Exception e) {
//                                if(e.getMessage().contains("-111018"))
//                                    postToast("Cancel sales! Registration mode error: Payment is initiated");
//                                else
//                                    postToast(e.getMessage());
//
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                    //The operator decides on his own what to do with the unpaid receipts !
//                    // So if the answer is NO, the receipt is not closed
//                    dialog.setNegativeButton("no", null);
//                    dialog.show();
//                } else {
//                    //If a TOTAL is set with a full payment, there is a Amount-Tender=0.
//                    //All is OK, completion of the sale!
//                    fiscalReceipt.closeFiscalReceipt();
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //sync shift and bills to server background
    public void sendShiftToServer(Shift shift){
        String uri = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("URI",null);
        String token = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("Token","null");
        EposServiceAPI apiEposService = EPOSRetrofitClient.getApiEposService(uri);

        ShiftRemote saveShift = new ShiftRemote();
        SaveShift sendShiftToServer = new SaveShift();

        saveShift.setiD(shift.getId());
        saveShift.setCashID(shift.getWorkPlaceId());
        saveShift.setOpenedById(shift.getAuthor());

        SimpleDateFormat format = new SimpleDateFormat("Z");
        Date date = new Date(shift.getStartDate());
        String createDate = "/Date(" + String.valueOf(shift.getStartDate()) + format.format(date) + ")/";
        saveShift.setCreationDate(createDate);

        if(shift.isClosed()){
            Date dateClosed = new Date(shift.getEndDate());
            String closedDate = "/Date(" + String.valueOf(shift.getEndDate()) + format.format(dateClosed) + ")/";
            saveShift.setClosed(shift.isClosed());
            saveShift.setClosingDate(closedDate);
            saveShift.setClosedByID(shift.getClosedBy());
        }

        sendShiftToServer.setShift(saveShift);
        sendShiftToServer.setToken(token);

        Call<SimpleResponseEPOS> call = apiEposService.saveShift(sendShiftToServer);
        call.enqueue(new Callback<SimpleResponseEPOS>() {
            @Override
            public void onResponse(Call<SimpleResponseEPOS> call, Response<SimpleResponseEPOS> response) {
                SimpleResponseEPOS resultEposSimple = response.body();
                if(resultEposSimple != null){
                    int error = resultEposSimple.getErrorCode();

                    if(error == 0){
                        mRealm  = Realm.getDefaultInstance();
                        mRealm.executeTransaction(realm -> {
                            Shift results = mRealm.where(Shift.class).equalTo("id",saveShift.getiD()).findFirst();
                            if(results != null){
                                results.setSended(true);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<SimpleResponseEPOS> call, Throwable t) {
                String tt= t.getMessage();
            }
        });

    }
    public void sendBilltToServer(RealmResults<Bill> billRealmResults){
        String uri = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("URI",null);
        String token = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("Token","null");
        EposServiceAPI apiEposService = EPOSRetrofitClient.getApiEposService(uri);

        SimpleDateFormat format = new SimpleDateFormat("Z");

        RealmList<Bill> billRealmList = new RealmList<>();

        for(Bill bill:billRealmResults){
            billRealmList.add(bill);

            SaveBills sendBillsToServer = new SaveBills();

            List<BillRemote> listBill = new ArrayList<>();
            List<BillLine> listLines = new ArrayList<>();
            List<BillPayment> listBillPayment = new ArrayList<>();

            BillRemote saveBill = new BillRemote();

            saveBill.setID(bill.getId());
            saveBill.setClosedByID(bill.getClosedBy());

            Date date = new Date(bill.getCloseDate());
            String closingDate = "/Date(" + String.valueOf(bill.getCloseDate()) + format.format(date) + ")/";
            saveBill.setClosingDate(closingDate);

            Date dateCreate = new Date(bill.getCreateDate());
            String createDate = "/Date(" + String.valueOf(bill.getCreateDate()) + format.format(dateCreate) + ")/";
            saveBill.setCreationDate(createDate);

            saveBill.setDiscountCardId(bill.getDiscountCardId());

            RealmList<BillString> billStringRealm = bill.getBillStrings();
            for(BillString billString: billStringRealm){
                BillLine saveBillLine = new BillLine();

                saveBillLine.setCount(billString.getQuantity());
                saveBillLine.setCreatedByID(billString.getCreateBy());

                Date dateCreateLine = new Date(billString.getCreateDate());
                String createDateLine = "/Date(" + String.valueOf(billString.getCreateDate()) + format.format(dateCreateLine) + ")/";
                saveBillLine.setCreationDate(createDateLine);

                saveBillLine.setDeletedByID(billString.getDeleteBy());

                Date dateDelet = new Date(billString.getDeletionDate());
                String deletingDate = "/Date(" + String.valueOf(billString.getDeletionDate()) + format.format(dateDelet) + ")/";
                saveBillLine.setDeletionDate(deletingDate);

                saveBillLine.setIsDeleted(billString.isDeleted());
                saveBillLine.setPrice(billString.getPrice());
                saveBillLine.setPriceLineID(billString.getPriceLineID());
                saveBillLine.setPromoPrice(billString.getPromoPrice());
                saveBillLine.setSum(billString.getSum());
                saveBillLine.setSumWithDiscount(billString.getSumWithDiscount());
                saveBillLine.setVATQuote(billString.getVat());

                listLines.add(saveBillLine);
            }

            RealmList<BillPaymentType> billPaymentTypeRealmList = bill.getBillPaymentTypes();
            for(BillPaymentType billPaymentType: billPaymentTypeRealmList){
                BillPayment saveBillPayment = new BillPayment();

                saveBillPayment.setCreatedByID(billPaymentType.getAuthor());
                saveBillPayment.setID(billPaymentType.getId());
                saveBillPayment.setPaymentTypeID(billPaymentType.getPaymentTypeID());
                saveBillPayment.setSum(billPaymentType.getSum());

                Date createDatePayment = new Date(billPaymentType.getCreateDate());
                String createDatePayments = "/Date(" + String.valueOf(billPaymentType.getCreateDate()) + format.format(createDatePayment) + ")/";
                saveBillPayment.setCreationDate(createDatePayments);

                listBillPayment.add(saveBillPayment);
            }

            saveBill.setNumber(bill.getShiftReceiptNumSoftware());
            saveBill.setOpenedByID(bill.getAuthor());

            saveBill.setLines(listLines);
            saveBill.setPayments(listBillPayment);

            listBill.add(saveBill);

            sendBillsToServer.setBillRemotes(listBill);
            sendBillsToServer.setShiftID(bill.getShiftId());
            sendBillsToServer.setToken(token);



            Call<SimpleResponseEPOS> call = apiEposService.saveBills(sendBillsToServer);
            call.enqueue(new Callback<SimpleResponseEPOS>() {
                @Override
                public void onResponse(Call<SimpleResponseEPOS> call, Response<SimpleResponseEPOS> response) {
                    SimpleResponseEPOS resultEposSimple = response.body();
                    if(resultEposSimple != null){
                        int error = resultEposSimple.getErrorCode();

                        if(error == 0){
                            String bilId = saveBill.getID();
                            mRealm  = Realm.getDefaultInstance();
                            mRealm.executeTransaction(realm -> {
                                Bill bilRealm = realm.where(Bill.class).equalTo("id",bilId).findFirst();
                                if(bilRealm != null){
                                    bilRealm.setSynchronized(true);
                                }
                            });
                        }
                        else{
                            String bilId = saveBill.getID();
                            mRealm  = Realm.getDefaultInstance();
                            mRealm.executeTransaction(realm -> {
                                Bill bilRealm = realm.where(Bill.class).equalTo("id",bilId).findFirst();
                                if(bilRealm != null){
                                    bilRealm.setSynchronized(false);
                                    bilRealm.setInProcessOfSync(2);
                                }

                            });

                        }
                    }
                }
                @Override
                public void onFailure(Call<SimpleResponseEPOS> call, Throwable t) {
                    String tt = t.getMessage();
                }
            });
        }
    }
    private void sheduleSendBillSHiftToServer(){
        timerTaskSyncBill = new TimerTask() {
            @Override
            public void run() {
                mRealm  = Realm.getDefaultInstance();
                mRealm.executeTransaction(realm -> {
                    RealmResults<Shift> resultShift = realm.where(Shift.class).equalTo("isSended",false).findAll();
                    if(!resultShift.isEmpty()){
                        for(Shift shift:resultShift){
                            sendShiftToServer(shift);
                        }
                    }
                    RealmResults<Bill> resultBills = realm.where(Bill.class)
                            .equalTo("state",1)
                            .and()
                            .equalTo("isSynchronized",false)
                            .findAll();

                    if(!resultBills.isEmpty())
                        sendBilltToServer(resultBills);
                });
            }
        };
    }

    private static void postToast(final String message) {
        Toast.makeText(application, message, Toast.LENGTH_LONG).show();
    }

    public void setUserLogin(String login) {
        this.login = login;
    }

    public String getUserLogin(){
        return login;
    }
}
