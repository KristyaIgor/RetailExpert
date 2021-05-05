package md.intelectsoft.quickpos.phoneMode.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.vfi.smartpos.deviceservice.aidl.IDeviceService;
import com.vfi.smartpos.deviceservice.aidl.IPrinter;
import com.vfi.smartpos.deviceservice.aidl.PrinterConfig;
import com.vfi.smartpos.deviceservice.aidl.PrinterListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.Bill;
import md.intelectsoft.quickpos.Realm.localStorage.BillPaymentType;
import md.intelectsoft.quickpos.Realm.localStorage.FiscalKey;
import md.intelectsoft.quickpos.POSApplication;
import md.intelectsoft.quickpos.utils.SPFHelp;

import static md.intelectsoft.quickpos.phoneMode.activity.MainActivityPhone.salesViewModel;

@SuppressLint("NonConstantResourceId")
public class InOutCashActivity extends AppCompatActivity {
    @BindView(R.id.buttonInsertOrWithdraw) MaterialButton buttonAction;
    @BindView(R.id.textSumOperationInput) TextView sumOperation;
    @BindView(R.id.textSumAvailbleToCashOut) TextView sumAvailableCashOUt;
    int operationType = 0;
    private Realm mRealm;
    double sumOfCash = 0, sumCashOut;
    IDeviceService idevice;
    IPrinter printer;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    TimeZone timeZone = TimeZone.getTimeZone("Europe/Chisinau");

    @OnClick(R.id.buttonInsertOrWithdraw) void onInsertWithdraw() {
       boolean isSuccess = false;

        if(operationType == 1) {
            isSuccess = salesViewModel.changeMoneyInBox(Double.valueOf(sumOperation.getText().toString()) , 1);
        }
        else if(operationType == -1){
            if(sumOfCash - sumCashOut - Double.valueOf(sumOperation.getText().toString()) > 0){
                isSuccess = salesViewModel.changeMoneyInBox(Double.valueOf(sumOperation.getText().toString()) , -1);
            }
        }
        if(isSuccess){
            int billCounter = SPFHelp.getInstance().getInt("GlobalNumberBills",0) + 1;
            SPFHelp.getInstance().putInt("GlobalNumberBills", billCounter);
            doPrintString();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blueItem));
        setContentView(R.layout.activity_in_out_cash);

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        Toolbar toolbar = findViewById(R.id.toolbarFinReportsExecuted);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        mRealm = Realm.getDefaultInstance();
        simpleDateFormat.setTimeZone(timeZone);

        operationType = getIntent().getIntExtra("OperationType", 0);

        if(operationType == 1){
            setTitle("Insert money");
            buttonAction.setText("Insert");
            sumAvailableCashOUt.setVisibility(View.GONE);
        }
        if (operationType == -1){
            setTitle("Withdraw money");
            buttonAction.setText("Withdraw");
            sumAvailableCashOUt.setVisibility(View.VISIBLE);
        }

        if(POSApplication.isVFServiceConnected()){
            idevice = POSApplication.getApplication().getDeviceService();
            try {
                printer = idevice.getPrinter();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        salesViewModel.getShift().observe(this, shift -> {
            if(shift != null){

                RealmResults<Bill> shiftBills = mRealm.where(Bill.class).equalTo("shiftId", shift.getId()).findAll();
                RealmQuery<BillPaymentType> query = mRealm.where(BillPaymentType.class);
                // ids is a list of the category ids
                if (shiftBills.size() > 0) {
                    query = query.equalTo("billID", shiftBills.get(0).getId());
                    for (int i = 1; i < shiftBills.size(); i++) {
                        query = query.or().equalTo("billID", shiftBills.get(i).getId());
                    }
                }
                sumOfCash = 0;
                if(!shiftBills.isEmpty()){
                    if (shiftBills.size() > 0) {
                        RealmResults<BillPaymentType> result = query.findAll();
                        if(!result.isEmpty()){
                            for(BillPaymentType pay : result){
                                if(pay.getPaymentCode() == 1)
                                    sumOfCash += pay.getSum();
                            }
                        }
                    }
                }
                sumOfCash = sumOfCash + shift.getCashIn();
                sumCashOut = shift.getCashOut();

                if(operationType == -1) {
                    sumAvailableCashOUt.setText("Suma disponibila de extras: " + String.format("%.2f", sumOfCash - shift.getCashOut()));
                }
            }
            else{
                sumAvailableCashOUt.setVisibility(View.VISIBLE);
                sumAvailableCashOUt.setText("Tura nu este activa!");
            }
        });

        salesViewModel.getShiftInfo();
    }

    @OnClick(R.id.textButtonCount1) void onButton1(){
        if (sumOperation.getText().toString().equals("0")) sumOperation.setText("1");
        else if(sumOperation.getText().toString().contains(".")){
            String test = sumOperation.getText().toString().substring(sumOperation.getText().toString().indexOf("."), sumOperation.getText().toString().length());
            if (test.length() < 3){
                sumOperation.append("1");
            }
        }
        else sumOperation.append("1");
    }
    @OnClick(R.id.textButtonCount2) void onButton2(){
        if (sumOperation.getText().toString().equals("0")) sumOperation.setText("2");
        else if(sumOperation.getText().toString().contains(".")){
            String test = sumOperation.getText().toString().substring(sumOperation.getText().toString().indexOf("."), sumOperation.getText().toString().length());
            if (test.length() < 3){
                sumOperation.append("2");
            }
        }
        else sumOperation.append("2");
    }
    @OnClick(R.id.textButtonCount3) void onButton3(){
        if (sumOperation.getText().toString().equals("0")) sumOperation.setText("3");
        else if (sumOperation.getText().toString().contains(".")) {
            String test = sumOperation.getText().toString().substring(sumOperation.getText().toString().indexOf("."), sumOperation.getText().toString().length());
            if (test.length() < 3) {
                sumOperation.append("3");
            }
        } else sumOperation.append("3");
    }
    @OnClick(R.id.textButtonCount4) void onButton4(){
        if (sumOperation.getText().toString().equals("0")) sumOperation.setText("4");
        else if (sumOperation.getText().toString().contains(".")) {
            String test = sumOperation.getText().toString().substring(sumOperation.getText().toString().indexOf("."), sumOperation.getText().toString().length());
            if (test.length() < 3) {
                sumOperation.append("4");
            }
        } else sumOperation.append("4");
    }
    @OnClick(R.id.textButtonCount5) void onButton5(){
        if (sumOperation.getText().toString().equals("0")) sumOperation.setText("5");
        else if (sumOperation.getText().toString().contains(".")) {
            String test = sumOperation.getText().toString().substring(sumOperation.getText().toString().indexOf("."), sumOperation.getText().toString().length());
            if (test.length() < 3) {
                sumOperation.append("5");
            }
        } else sumOperation.append("5");
    }
    @OnClick(R.id.textButtonCount6) void onButton6() {
        if (sumOperation.getText().toString().equals("0")) sumOperation.setText("6");
        else if (sumOperation.getText().toString().contains(".")) {
            String test = sumOperation.getText().toString().substring(sumOperation.getText().toString().indexOf("."), sumOperation.getText().toString().length());
            if (test.length() < 3) {
                sumOperation.append("6");
            }
        } else sumOperation.append("6");
    }
    @OnClick(R.id.textButtonCount7) void onButton7(){
        if (sumOperation.getText().toString().equals("0")) sumOperation.setText("7");
        else if (sumOperation.getText().toString().contains(".")) {
            String test = sumOperation.getText().toString().substring(sumOperation.getText().toString().indexOf("."), sumOperation.getText().toString().length());
            if (test.length() < 3) {
                sumOperation.append("7");
            }
        } else sumOperation.append("7");
    }
    @OnClick(R.id.textButtonCount8) void onButton8(){
        if (sumOperation.getText().toString().equals("0"))
            sumOperation.setText("8");
        else if (sumOperation.getText().toString().contains(".")) {
            String test = sumOperation.getText().toString().substring(sumOperation.getText().toString().indexOf("."), sumOperation.getText().toString().length());
            if (test.length() < 3) {
                sumOperation.append("8");
            }
        } else
            sumOperation.append("8");
    }
    @OnClick(R.id.textButtonCount9) void onButton9(){
        if (sumOperation.getText().toString().equals("0")) sumOperation.setText("9");
        else if (sumOperation.getText().toString().contains(".")) {
            String test = sumOperation.getText().toString().substring(sumOperation.getText().toString().indexOf("."), sumOperation.getText().toString().length());
            if (test.length() < 3) {
                sumOperation.append("9");
            }
        } else sumOperation.append("9");
    }
    @OnClick(R.id.textButtonCount0) void onButton0(){
        if (!sumOperation.getText().toString().equals("0"))
            if (sumOperation.getText().toString().contains(".")) {
                String test = sumOperation.getText().toString().substring(sumOperation.getText().toString().indexOf("."));
                if (test.length() < 3) {
                    sumOperation.append("0");
                }
            } else {
                sumOperation.append("0");
            }
    }
    @OnClick(R.id.textButtonClearCountSum) void onButtonClear(){
        if(!sumOperation.getText().toString().contains("."))
            sumOperation.append(".");
    }
    @OnClick(R.id.textButtonDeleteCountSum) void onButtonDelete(){
        String text = sumOperation.getText().toString();
        if(text.length() - 1 != 0){
            sumOperation.setText(text.substring(0, text.length() - 1));
        }
        else
            sumOperation.setText("0");
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


            format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.CENTER );
            printer.addText(format, "--------------------------------");

            String oper = "";
            if(operationType == 1)
                oper = "SUME INTRODUSE";
            if(operationType == -1)
                oper = "SUME SCOASE";
            double sum = Double.valueOf(sumOperation.getText().toString());
            printer.addTextInLine(fmtAddTextInLine, oper, "", String.format("%.2f", sum).replace(",","."), 1);
            printer.addTextInLine(fmtAddTextInLine, "NUMERAR IN SERTAR", "", String.format("%.2f", sumOfCash - sumCashOut).replace(",","."), 1);

            printer.addText(format, "--------------------------------");

            int billCounter = SPFHelp.getInstance().getInt("GlobalNumberBills",0) ;

            String numberToString = "";
            int length = (int)(Math.log10(billCounter)+1);

            if (length == 1)
                numberToString = "0000" + billCounter;
            else if (length == 2)
                numberToString = "000" + billCounter;
            else if (length == 3)
                numberToString = "00" + billCounter;
            else if (length == 4)
                numberToString = "0" + billCounter;
            else
                numberToString = "" + billCounter;

            printer.addTextInLine(fmtAddTextInLine, numberToString , "", simpleDateFormat.format(new Date().getTime()), 1);

            format.putInt(PrinterConfig.addText.FontSize.BundleName, PrinterConfig.addText.FontSize.LARGE_32_32);
            format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.CENTER);
            printer.addText(format, "BON DE SERVICIU");

            format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.LEFT );
            format.putInt(PrinterConfig.addText.FontSize.BundleName, PrinterConfig.addText.FontSize.NORMAL_24_24 );
            printer.addText(format, "\n");
            printer.feedLine(2);

            printer.startPrint(new MyListener());

        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
                finish();
            }
        }
    };
}