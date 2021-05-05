package md.intelectsoft.quickpos.phoneMode.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vfi.smartpos.deviceservice.aidl.IDeviceService;
import com.vfi.smartpos.deviceservice.aidl.IPrinter;
import com.vfi.smartpos.deviceservice.aidl.PrinterConfig;
import com.vfi.smartpos.deviceservice.aidl.PrinterListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.Bill;
import md.intelectsoft.quickpos.Realm.localStorage.BillPaymentType;
import md.intelectsoft.quickpos.Realm.localStorage.BillString;
import md.intelectsoft.quickpos.Realm.localStorage.FiscalKey;
import md.intelectsoft.quickpos.fisc.BillServiceAPI;
import md.intelectsoft.quickpos.fisc.BillServiceRetrofitClient;
import md.intelectsoft.quickpos.fisc.body.BillItem;
import md.intelectsoft.quickpos.fisc.body.FiscalBill;
import md.intelectsoft.quickpos.fisc.body.RegisterFiscalBill;
import md.intelectsoft.quickpos.fisc.enums.BillServiceEnum;
import md.intelectsoft.quickpos.fisc.response.RespRegisterFiscalBill;
import md.intelectsoft.quickpos.POSApplication;
import md.intelectsoft.quickpos.utils.SPFHelp;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("NonConstantResourceId")
public class InputReceiveActivity extends AppCompatActivity {
    BillServiceAPI billServiceAPI;
    IDeviceService idevice;
    IPrinter printer;

    Bill bill;
    Realm mRealm;

    boolean notInputAmount = true;
    double sumInput, sumOfBill, change = 0;
    String billId, payId, payName;
    int payCode;
    ProgressDialog progressDialog;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    TimeZone timeZone = TimeZone.getTimeZone("Europe/Chisinau");

    @BindView(R.id.textReceiveAmount) TextView receiveAmount;
    @BindView(R.id.textSumBill) TextView sumBill;
    @BindView(R.id.textChangePayBill) TextView changeSum;
    @BindView(R.id.textSelectedPaymentMethod) TextView paymentMethod;

    @OnClick(R.id.btn_backFromPaymentInputSumActivityPhoneMode) void onBack(){
        finish();
    }

    @OnClick(R.id.textButtonCount1) void onButton1(){
        if(notInputAmount){
            receiveAmount.setText("1");
            notInputAmount = false;
        }
        else{
            if (receiveAmount.getText().toString().equals("0")) receiveAmount.setText("1");
            else if(receiveAmount.getText().toString().contains(".")){
                String test = receiveAmount.getText().toString().substring(receiveAmount.getText().toString().indexOf("."), receiveAmount.getText().toString().length());
                if (test.length() < 3){
                    receiveAmount.append("1");
                }
            }
            else receiveAmount.append("1");
        }
    }
    @OnClick(R.id.textButtonCount2) void onButton2(){
        if(notInputAmount){
            receiveAmount.setText("2");
            notInputAmount = false;
        }
        else{
            if (receiveAmount.getText().toString().equals("0")) receiveAmount.setText("2");
            else if(receiveAmount.getText().toString().contains(".")){
                String test = receiveAmount.getText().toString().substring(receiveAmount.getText().toString().indexOf("."), receiveAmount.getText().toString().length());
                if (test.length() < 3){
                    receiveAmount.append("2");
                }
            }
            else receiveAmount.append("2");
        }
    }
    @OnClick(R.id.textButtonCount3) void onButton3(){
        if(notInputAmount){
            receiveAmount.setText("3");
            notInputAmount = false;
        }
        else {
            if (receiveAmount.getText().toString().equals("0")) receiveAmount.setText("3");
            else if (receiveAmount.getText().toString().contains(".")) {
                String test = receiveAmount.getText().toString().substring(receiveAmount.getText().toString().indexOf("."), receiveAmount.getText().toString().length());
                if (test.length() < 3) {
                    receiveAmount.append("3");
                }
            } else receiveAmount.append("3");
        }
    }
    @OnClick(R.id.textButtonCount4) void onButton4(){
        if(notInputAmount){
            receiveAmount.setText("4");
            notInputAmount = false;
        }
        else {
            if (receiveAmount.getText().toString().equals("0")) receiveAmount.setText("4");
            else if (receiveAmount.getText().toString().contains(".")) {
                String test = receiveAmount.getText().toString().substring(receiveAmount.getText().toString().indexOf("."), receiveAmount.getText().toString().length());
                if (test.length() < 3) {
                    receiveAmount.append("4");
                }
            } else receiveAmount.append("4");
        }
    }
    @OnClick(R.id.textButtonCount5) void onButton5(){
        if(notInputAmount){
            receiveAmount.setText("5");
            notInputAmount = false;
        }else {
            if (receiveAmount.getText().toString().equals("0")) receiveAmount.setText("5");
            else if (receiveAmount.getText().toString().contains(".")) {
                String test = receiveAmount.getText().toString().substring(receiveAmount.getText().toString().indexOf("."), receiveAmount.getText().toString().length());
                if (test.length() < 3) {
                    receiveAmount.append("5");
                }
            } else receiveAmount.append("5");
        }
    }
    @OnClick(R.id.textButtonCount6) void onButton6() {
        if(notInputAmount){
            receiveAmount.setText("6");
            notInputAmount = false;
        }
        else {
            if (receiveAmount.getText().toString().equals("0")) receiveAmount.setText("6");
            else if (receiveAmount.getText().toString().contains(".")) {
                String test = receiveAmount.getText().toString().substring(receiveAmount.getText().toString().indexOf("."), receiveAmount.getText().toString().length());
                if (test.length() < 3) {
                    receiveAmount.append("6");
                }
            } else receiveAmount.append("6");
        }
    }
    @OnClick(R.id.textButtonCount7) void onButton7(){
        if(notInputAmount){
            receiveAmount.setText("7");
            notInputAmount = false;
        }else {
            if (receiveAmount.getText().toString().equals("0")) receiveAmount.setText("7");
            else if (receiveAmount.getText().toString().contains(".")) {
                String test = receiveAmount.getText().toString().substring(receiveAmount.getText().toString().indexOf("."), receiveAmount.getText().toString().length());
                if (test.length() < 3) {
                    receiveAmount.append("7");
                }
            } else receiveAmount.append("7");
        }
    }
    @OnClick(R.id.textButtonCount8) void onButton8(){
        if(notInputAmount){
            receiveAmount.setText("8");
            notInputAmount = false;
        }
        else {
            if (receiveAmount.getText().toString().equals("0"))
                receiveAmount.setText("8");
            else if (receiveAmount.getText().toString().contains(".")) {
                String test = receiveAmount.getText().toString().substring(receiveAmount.getText().toString().indexOf("."), receiveAmount.getText().toString().length());
                if (test.length() < 3) {
                    receiveAmount.append("8");
                }
            } else
                receiveAmount.append("8");
        }
    }
    @OnClick(R.id.textButtonCount9) void onButton9(){
        if(notInputAmount){
            receiveAmount.setText("9");
            notInputAmount = false;
        }
        else {
            if (receiveAmount.getText().toString().equals("0")) receiveAmount.setText("9");
            else if (receiveAmount.getText().toString().contains(".")) {
                String test = receiveAmount.getText().toString().substring(receiveAmount.getText().toString().indexOf("."), receiveAmount.getText().toString().length());
                if (test.length() < 3) {
                    receiveAmount.append("9");
                }
            } else receiveAmount.append("9");
        }
    }
    @OnClick(R.id.textButtonCount0) void onButton0(){
        if(notInputAmount){
            receiveAmount.setText("0");
            notInputAmount = false;
        }else {
            if (!receiveAmount.getText().toString().equals("0"))
                if (receiveAmount.getText().toString().contains(".")) {
                    String test = receiveAmount.getText().toString().substring(receiveAmount.getText().toString().indexOf("."), receiveAmount.getText().toString().length());
                    if (test.length() < 3) {
                        receiveAmount.append("0");
                    }
                } else {
                    receiveAmount.append("0");
                }
        }
    }
    @OnClick(R.id.textButtonClearCountSum) void onButtonClear(){
        if(!receiveAmount.getText().toString().contains("."))
            receiveAmount.append(".");
    }
    @OnClick(R.id.textButtonDeleteCountSum) void onButtonDelete(){
        if(notInputAmount){
            receiveAmount.setText("0");
        }
        else{
            String text = receiveAmount.getText().toString();
            if(text.length() - 1 != 0){
                receiveAmount.setText(text.substring(0, text.length() - 1));
            }
            else
                receiveAmount.setText("0");
        }
    }

    @OnClick(R.id.buttonPayInputSum) void onPay(){

            if(sumInput >= sumOfBill) {
                if(POSApplication.isVFServiceConnected()){
                    Message msg = new Message();
                    msg.getData().putInt("resp", -1);
                    handler.sendMessage(msg);
                }
                BillPaymentType billPaymentType = new BillPaymentType();
                billPaymentType.setAuthor(POSApplication.getApplication().getUserId());
                billPaymentType.setBillID(billId);
                billPaymentType.setCreateDate(new Date().getTime());
                billPaymentType.setName(payName);
                billPaymentType.setSum(sumOfBill);
                billPaymentType.setPaymentCode(payCode);
                billPaymentType.setPaymentTypeID(payId);
                billPaymentType.setId(UUID.randomUUID().toString());

                mRealm.executeTransaction(realm -> {
                    bill = realm.where(Bill.class).equalTo("id", billId).findFirst();
                    bill.setState(1);
                    bill.setCloseDate(new Date().getTime());
                    bill.setClosedBy(POSApplication.getApplication().getUserId());

                    int billFiscalCounter = SPFHelp.getInstance().getInt("ShiftFiscalNumber", 0) + 1;
                    int billCounter = SPFHelp.getInstance().getInt("GlobalNumberBills", 0) + 1;

                    SPFHelp.getInstance().putInt("GlobalNumberBills", billCounter);
                    SPFHelp.getInstance().putInt("ShiftFiscalNumber", billFiscalCounter);

                    bill.setGlobalNumber(billCounter);
                    bill.setShiftNumberFiscal(billFiscalCounter);
                    realm.insert(billPaymentType);
                });
                if(POSApplication.isVFServiceConnected()){
                    doPrintString();
                    createAndSendBillToFisc();
                }
                else{
                    Message msg = new Message();
                    msg.getData().putInt("resp", 0);
                    handler.sendMessage(msg);
                }
            }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blueItem));
        setContentView(R.layout.activity_input_receive);

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        mRealm = Realm.getDefaultInstance();
        simpleDateFormat.setTimeZone(timeZone);
        billServiceAPI = BillServiceRetrofitClient.getBillService();
        progressDialog = new ProgressDialog(this);

        Intent intent = getIntent();
        billId = intent.getStringExtra("billId");
        payCode = intent.getIntExtra("payCode",0);
        payName = intent.getStringExtra("payName");
        payId = intent.getStringExtra("payId");
        sumOfBill = intent.getDoubleExtra("sum",0);

        paymentMethod.setText("Payment: " + payName);
        receiveAmount.setText(String.format("%.2f", sumOfBill).replace(",","."));
        sumInput = sumOfBill;
        sumBill.setText("Sum bill: " + String.format("%.2f", sumOfBill).replace(",",".") + " MDL");

        bill = mRealm.where(Bill.class).equalTo("id", billId).findFirst();

        if(POSApplication.isVFServiceConnected()){
            idevice = POSApplication.getApplication().getDeviceService();
            try {
                printer = idevice.getPrinter();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        receiveAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if(text.length() > 0){
                    sumInput = Double.parseDouble(text);
                    if(sumInput > sumOfBill){
                        change = sumInput - sumOfBill;
                        changeSum.setVisibility(View.VISIBLE);
                        changeSum.setText("REST: " + String.format("%.2f", change).replace(",",".") + " MDL");
                    }
                    else{
                        change = 0;
                        changeSum.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private boolean getPrinterStatus(){
        boolean printerReady = false;
        try {
            int status = printer.getStatus();
            switch (status){
                case (int) 0x00 : {  // no error
                    printerReady = true;
                }break;
                case (int) 0xF0 : {
                    showDialogPrinterError("Paper ended, please put new paper in printer and retry print bill!");
                    printerReady = false;
                }break;
                case (int) 0xF3 : {
                    showDialogPrinterError("Printer overheat!");
                    printerReady = false;
                }break;
                case (int) 0xE1 : {
                    showDialogPrinterError("Battery is low nivel!");
                    printerReady = false;
                }break;
                case (int) 0xF4 : {
                    showDialogPrinterError("Paper endendig. Please put new paper!");
                    printerReady = false;
                }break;
                case (int) 0xFB : {
                    showDialogPrinterError("Moto error in printer!");
                    printerReady = false;
                }break;
                case (int) 0xEE : {
                    showDialogPrinterError("Paper jam. Please check paper!");
                    printerReady = false;
                }break;
                case (int) 0xF7 : {
                    showDialogPrinterError("Paper is busy. Please wait!");
                    printerReady = false;
                }break;
                default: printerReady = false;
            }
        } catch (RemoteException e) {
            showDialogPrinterError("Remote exception: " + e.getMessage());
        }

        return printerReady;
    }

    private void showDialogPrinterError(String text){
        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                .setTitle("Printer error!")
                .setMessage(text)
                .setCancelable(false)
                .setPositiveButton("OK", (dialogInterface, i) -> {

                })
                .setNeutralButton("Retry", (dialog, which) -> {
                    if(getPrinterStatus())
                        doPrintString();
                })
                .show();
    }

    public void doPrintString() {
        try {
            // bundle format for addText
            Bundle format = new Bundle();

            // bundle formate for AddTextInLine
            Bundle fmtAddTextInLine = new Bundle();

            format.putInt(PrinterConfig.addText.FontSize.BundleName, PrinterConfig.addText.FontSize.NORMAL_24_24);
            format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.CENTER);
            printer.addText(format, "\"" + SPFHelp.getInstance().getString("CompanyName", "") + "\"");

            printer.addText(format, "IDNO: " + SPFHelp.getInstance().getString("CompanyIDNO", ""));

            fmtAddTextInLine.putInt(PrinterConfig.addTextInLine.FontSize.BundleName, PrinterConfig.addTextInLine.FontSize.NORMAL_24_24);

            String headerFull = SPFHelp.getInstance().getString("StationAddress","");
            if(!headerFull.equals("")) {
                String[] subStr = headerFull.split(" ");
                String firstLine = "";

                for(int i = 0; i < subStr.length; i++){
                    if(i == 0){
                        firstLine = subStr[0];
                        continue;
                    }
                    if(firstLine.length() < 32) {
                        String nextLine = subStr[i];
                        if((firstLine + " " + nextLine).length() > 32){
                            printer.addText(format, firstLine);
                            firstLine = nextLine;
                        }
                        else
                            firstLine = firstLine+ " " + nextLine;
                    }
                    if(i == subStr.length - 1)
                        printer.addText(format, firstLine);
                }
            }
            FiscalKey key = mRealm.where(FiscalKey.class).findFirst();
            String nrReg = "";
            if(key == null)
                nrReg = SPFHelp.getInstance().getString("LicenseCode","");
            else
                nrReg = SPFHelp.getInstance().getString("FiscalCode","");

            printer.addText(format, "Inr.Nr: " + nrReg);
            printer.addText(format, "***");

            int n2 = bill.getShiftNumberFiscal();
            String numberShiftFiscal = "";
            int length1 = (int)(Math.log10(n2) + 1);

            if (length1 == 1)
                numberShiftFiscal = "0000" + n2;
            else if (length1 == 2)
                numberShiftFiscal = "000" + n2;
            else if (length1 == 3)
                numberShiftFiscal = "00" + n2;
            else if (length1 == 4)
                numberShiftFiscal = "0" + n2;
            else
                numberShiftFiscal = "" + n2;

            printer.addTextInLine(fmtAddTextInLine, numberShiftFiscal , "", bill.getShiftNumberSoftware() + "#", 0);

            format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.LEFT );
            printer.addText(format, "#-" + SPFHelp.getInstance().getString("WorkplaceName", "--"));
            printer.addText(format, "#-" + POSApplication.getApplication().getUser().getFullName());

            printer.feedLine(2);

            RealmList<BillString> line = bill.getBillStrings();
            for(BillString billString : line){
                String count = String.format("%.2f",billString.getQuantity()).replace(",", ".");
                String namePrint = billString.getAssortmentShortName() != null && billString.getAssortmentShortName().length() > 0 ? billString.getAssortmentShortName() : billString.getAssortmentFullName();
                if(namePrint.length() > 32){
                    namePrint = namePrint.substring(0,31);
                }

                printer.addText(format, namePrint);
                printer.addTextInLine(fmtAddTextInLine, "", "",
                        (count.length() > 4 ? count.substring(0, count.indexOf(".") + 3) : count )
                                + " x "+ String.format("%.2f", billString.getBasePrice()).replace(",", ".")
                                + " =" + String.format("%.2f", billString.getQuantity() * billString.getBasePrice()).replace(",", "."),
                        PrinterConfig.addTextInLine.mode.Devide_flexible);
            }
            printer.addText(format, "--------------------------------");

            fmtAddTextInLine.putInt(PrinterConfig.addTextInLine.FontSize.BundleName, PrinterConfig.addTextInLine.FontSize.NORMAL_DH_24_48_IN_BOLD);
            printer.addTextInLine(fmtAddTextInLine, "TOTAL" , "", String.format("%.2f", bill.getTotalSum()).replace(",","."), 0);
            fmtAddTextInLine.putInt(PrinterConfig.addTextInLine.FontSize.BundleName, PrinterConfig.addTextInLine.FontSize.NORMAL_24_24);
            fmtAddTextInLine.putInt(PrinterConfig.addTextInLine.FontSize.BundleName, PrinterConfig.addTextInLine.FontSize.NORMAL_24_24);

            RealmResults<BillString> itsa = line.where().distinct("vatValue").sort("vatCode", Sort.ASCENDING).findAll();
            for(int i = 0; i < itsa.size(); i++){
                double sumVat = (double) line.where().equalTo("vatValue", itsa.get(i).getVatValue()).sum("sumVat");
                printer.addTextInLine(fmtAddTextInLine, "TVA " + itsa.get(i).getVatCode() + "=" + String.format("%.2f",itsa.get(i).getVatValue()).replace(",", ".").trim() + "%" , "", String.format("%.2f", sumVat).replace(",", "."), 0);
            }
            printer.addTextInLine(fmtAddTextInLine, "NUMERAR" , "", String.format("%.2f", sumInput).replace(",", "."), 0);
            if(change != 0)
                printer.addTextInLine(fmtAddTextInLine, "REST" , "", String.format("%.2f", change).replace(",", "."), 0);

            fmtAddTextInLine.putInt(PrinterConfig.addTextInLine.FontSize.BundleName, PrinterConfig.addTextInLine.FontSize.NORMAL_24_24);
            printer.feedLine(2);
            printer.addTextInLine(fmtAddTextInLine, "" , "V A  M U L T U M I M !", "", 0);
            printer.addTextInLine(fmtAddTextInLine, "" , line.size() > 1 ? line.size() + " ARTICOLE" : line.size() + " ARTICOL", "", 0);

            int n = bill.getGlobalNumber();

            String numberToString = "";
            int length = (int)(Math.log10(n)+1);

            if (length == 1)
                numberToString = "0000" + n;
            else if (length == 2)
                numberToString = "000" + n;
            else if (length == 3)
                numberToString = "00" + n;
            else if (length == 4)
                numberToString = "0" + n;
            else
                numberToString = "" + n;

            printer.addTextInLine(fmtAddTextInLine, numberToString , "", simpleDateFormat.format(bill.getCreateDate()), 1);

            format.putInt(PrinterConfig.addText.FontSize.BundleName, PrinterConfig.addText.FontSize.HUGE_48);
            format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.CENTER);


            if(key != null) {
                printer.addText(format, "BON NEFISCAL!");
            }
            else {
                printer.addText(format, "BON FISCAL!");

                Bitmap bitmap = createQRGradientImage("https://client.eservicii.md/fiscal?bill=" + bill.getId() + "&device=" + SPFHelp.getInstance().getString("LicenseID","0"), 250, 250);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();

                InputStream bs = new ByteArrayInputStream(bitmapdata);

                // image
                byte[] buffer = null;
                try {
                    int size = bs.available();
                    // crete the array of byte
                    buffer = new byte[size];
                    bs.read(buffer);
                    // close the stream
                    bs.close();

                } catch (IOException e) {
                    // Should never happen!
                    throw new RuntimeException(e);
                }
                if( null != buffer) {

//       * @param format - the format setting
//  	 * <ul>
//  	 * <li>offset(int) - the offset from left</li>
//  	 * <li>width(int) - the width of the image want to print.(MAX = 384)</li>
//       * <li>height(int) - the height want to print</li>
//       * <li>gray(int) - set pixcel gray to pint（0~255 default = 128）</li>

                    Bundle fmtImage = new Bundle();
                    fmtImage.putInt("offset", (384 - 250) / 2);
                    fmtImage.putInt("width", 250);  // bigger then actual, will print the actual
                    fmtImage.putInt("height", 250); // bigger then actual, will print the actual
                    printer.addImage( fmtImage, buffer );
                }

                fmtAddTextInLine.putInt(PrinterConfig.addTextInLine.FontSize.BundleName, PrinterConfig.addTextInLine.FontSize.SMALL_16_16);
                printer.addTextInLine(fmtAddTextInLine, "", "Scaneaza codul pentru vizualizarea\n bonului online!", "", 0);
            }

            fmtAddTextInLine.putInt(PrinterConfig.addTextInLine.FontSize.BundleName, PrinterConfig.addTextInLine.FontSize.NORMAL_24_24 );
            printer.addTextInLine(fmtAddTextInLine, "" , "IntelectSoft S.R.L.", "", 0);

            format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.LEFT );
            format.putInt(PrinterConfig.addText.FontSize.BundleName, PrinterConfig.addText.FontSize.NORMAL_24_24 );
            printer.addText(format, "\n");
            printer.feedLine(2);

            printer.startPrint(new MyListener());

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void createAndSendBillToFisc () {
        FiscalBill fiscalBill = new FiscalBill();
        fiscalBill.setDate(simpleDateFormat.format(bill.getCreateDate()));
        fiscalBill.setNumber(String.valueOf(bill.getGlobalNumber()));
        fiscalBill.setOperationType(BillServiceEnum.Sales);
        fiscalBill.setOperatorCode(SPFHelp.getInstance().getString("UserCodeAuth", "0"));
        fiscalBill.setTotalArticle(bill.getBillStrings().size());
        fiscalBill.setPaymantCode(payCode);
        fiscalBill.setPaymantType("NUMERAR");
        fiscalBill.setDiscount(round(bill.getTotalSum() - bill.getTotalDiscount(), 4));
        fiscalBill.setID(bill.getId());
        fiscalBill.setSumm(bill.getTotalSum());
        fiscalBill.setShiftNumber(String.valueOf(bill.getShiftNumberSoftware()));
        fiscalBill.setWorkplace(SPFHelp.getInstance().getString("WorkplaceName", "--"));
        fiscalBill.setUser(POSApplication.getApplication().getUser().getFullName());
        fiscalBill.setIDNO(SPFHelp.getInstance().getString("CompanyIDNO", ""));
        fiscalBill.setAddress(SPFHelp.getInstance().getString("StationAddress", "").replace("\\", "/"));
        fiscalBill.setFiscalNumber(SPFHelp.getInstance().getString("LicenseCode", ""));
        fiscalBill.setFreeTextHeader("***/" + bill.getShiftNumberFiscal());
        fiscalBill.setFreeTextFooter("IntelectSoft S.R.L.");
        fiscalBill.setChange(change);

        List<BillItem> listBillItems = new ArrayList<>();

        RealmList<BillString> listString = bill.getBillStrings();

        for (BillString items : listString) {

            BillItem item = new BillItem();
            item.setBasePrice(items.getBasePrice());
            item.setCPVCod(items.getCpvCode());
            item.setDiscount(round(items.getBasePrice() - items.getPriceWithDiscount(), 2));
            item.setFinalPrice(items.getBasePrice());
            item.setName(items.getAssortmentFullName());
            item.setQuantity(items.getQuantity());
            item.setSumm(items.getSum());
            item.setVATCode(items.getVatCode());

            item.setVATTotal(items.getSumVat());
            item.setVATValue(items.getVatValue());

            listBillItems.add(item);
        }

        fiscalBill.setBillItems(listBillItems);

        registerFiscalBill(fiscalBill);
    }

    private void registerFiscalBill(FiscalBill fiscalBill) {
        RegisterFiscalBill registerFiscalBill = new RegisterFiscalBill();
        registerFiscalBill.setBill(fiscalBill);
        registerFiscalBill.setLicenseID(SPFHelp.getInstance().getString("LicenseID", ""));

        Call<RespRegisterFiscalBill> call = billServiceAPI.registerBill(registerFiscalBill);
        call.enqueue(new Callback<RespRegisterFiscalBill>() {
            @Override
            public void onResponse(Call<RespRegisterFiscalBill> call, Response<RespRegisterFiscalBill> response) {
                RespRegisterFiscalBill resp = response.body();
                if(resp.getErrorCode() == 134){
                    Message msg = new Message();
                    msg.getData().putInt("resp", 134);
                    handler.sendMessage(msg);
                }

                Log.e("TAG", "onResponse: " + response.toString() + " id: "  + fiscalBill.getID() + " device: " + SPFHelp.getInstance().getString("LicenseID","0"));
            }

            @Override
            public void onFailure(Call<RespRegisterFiscalBill> call, Throwable t) {
                Log.e("TAG", "onFailure: " + t.getMessage());
            }
        });
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static Bitmap createQRGradientImage(String url, final int width, final int height){
        try {
            // Determine the legality of the URL
            if (url == null || "".equals(url) || url.length() < 1){
                return null;
            }
            Hashtable<EncodeHintType, Object> hints = new Hashtable();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.MARGIN, 2);
            // Image data conversion, using matrix conversion
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];

            // Gradient color draw from top to bottom
            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {// QR code color
                        int red = (int)(2 - (2.0 - 3.0) / height * (y + 1));
                        int green = (int)(119 - (119.0 - 7.0) / height * (y + 1));
                        int blue =  (int)(189 - (189.0 - 4.0) / height * (y + 1));
                        Color color = new Color();
//                        int colorInt = color.argb( red, green, blue);
                        int col = Color.rgb(0, 0, 0);
                        // Modify the color of the QR code, you can separately develop the color of the QR code and background
                        pixels[y * width + x] = bitMatrix.get(x, y) ? col: 16777215;// 0x000000:0xffffff
                    } else {
                        pixels[y * width + x] = Color.rgb(255, 255, 255);;// background color
                    }
                }
            }

            // Generate the format of the QR code image, using ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    class MyListener extends PrinterListener.Stub {
        @Override
        public void onError(int error) throws RemoteException {
            Message msg = new Message();
            msg.getData().putInt("msg", error);
            msg.getData().putInt("resp", 1);
            handler.sendMessage(msg);
        }

        @Override
        public void onFinish() throws RemoteException {
            Message msg = new Message();
            msg.getData().putInt("resp", 0);
            handler.sendMessage(msg);
        }
    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int dataResp = msg.getData().getInt("resp");
           if(dataResp == 0){
               setResult(RESULT_OK);
               progressDialog.dismiss();
               finish();
           }
           else if(dataResp == -1){
               progressDialog.setMessage("Printarea bonului...");
               progressDialog.setCancelable(false);
               progressDialog.setIndeterminate(true);
               progressDialog.show();
           }
           else if (dataResp == 134){
               Map<String,String> licenseData = new HashMap<>();
               licenseData.put("LicenseID", null);
               licenseData.put("CompanyName", null);
               licenseData.put("CompanyIDNO", null);
               SPFHelp.getInstance().putString("CashId", null);

               SPFHelp.getInstance().putStrings(licenseData);

               new MaterialAlertDialogBuilder(InputReceiveActivity.this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                       .setTitle("Licenta nu este activata!")
                       .setMessage("Licenta pentru aplicatie nu este activa.Va rugam sa verificati codul licentei: " + SPFHelp.getInstance().getString("LicenseCode","Nan"))
                       .setCancelable(false)
                       .setPositiveButton("OK", (dialogInterface, i) -> {
                           setResult(951);
                           finish();
                       })
                       .show();
           }
           else{
               progressDialog.dismiss();
               int error = msg.getData().getInt("msg");
               Toast.makeText(InputReceiveActivity.this, "Error print: " + error, Toast.LENGTH_SHORT).show();
           }
        }
    };



}