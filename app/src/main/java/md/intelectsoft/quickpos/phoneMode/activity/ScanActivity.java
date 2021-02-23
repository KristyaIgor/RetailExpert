package md.intelectsoft.quickpos.phoneMode.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.zxing.Result;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.AssortmentRealm;
import md.intelectsoft.quickpos.Realm.localStorage.Bill;
import md.intelectsoft.quickpos.Realm.localStorage.History;
import md.intelectsoft.quickpos.Realm.localStorage.Shift;
import md.intelectsoft.quickpos.utils.BaseEnum;
import md.intelectsoft.quickpos.utils.POSApplication;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static md.intelectsoft.quickpos.utils.POSApplication.SharedPrefSettings;

@SuppressLint("NonConstantResourceId")
public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView mScannerView;


    TimerTask timerTaskResumeScan;
    Timer timerResumeScan;
    private Context context;
    Realm mRealm;
    ZXingScannerView.ResultHandler handler;

    private int counter = 0;

    String billId;

    @OnClick(R.id.imageBackToSales) void onBack(){
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blueItem));

        setContentView(R.layout.activity_scan);

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        mRealm = Realm.getDefaultInstance();

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);
        mScannerView.setAspectTolerance(0.9f);
        handler = this;

        context = this;
        Intent intent = getIntent();
        billId = intent.getStringExtra("billId");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();
    }


    private void searchProductByBarcode (String barcode) {
        AssortmentRealm item = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar", barcode).findFirst();
        counter = 0;
        if(item != null){
            addProductsToBill(item);

            if (timerResumeScan != null)
                timerResumeScan.cancel();
            timerResumeScan = new Timer();

            resumeScanBarcode();
            timerResumeScan.schedule(timerTaskResumeScan, 1500);
        }
        else{
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Products not found!")
                    .setMessage("Products with barcode: " + barcode + " were not found!")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> {
                        mScannerView.resumeCameraPreview(handler);
                    })
                    .show();
        }
    }

    private void resumeScanBarcode() {
        timerTaskResumeScan = new TimerTask() {
            @Override
            public void run() {
                ScanActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // If you would like to resume scanning, call this method below:
                        mScannerView.resumeCameraPreview(handler);
                    }
                });
            }
        };
    }

    private void addProductsToBill(AssortmentRealm item) {
        if(billId == null){
            billId = UUID.randomUUID().toString();
            if(!createNewBill(billId)){
                billId = null;
            }
        }

        if(billId != null){

        }
    }

    private boolean createNewBill(String billId) {
       boolean billCreated = false;

        Shift lastOpenedShift = mRealm.where(Shift.class).equalTo("closed", false).findFirst();
        if(lastOpenedShift != null ){
            if(new Date().getTime() < lastOpenedShift.getNeedClose()){
                Bill newBill = new Bill();
                newBill.setId(billId);
                newBill.setCreateDate(new Date().getTime());
                newBill.setUserId(POSApplication.getApplication().getUser().getId());
                newBill.setUserName(POSApplication.getApplication().getUser().getFullName());
                newBill.setTotalDiscount(0.0);
                newBill.setTotalSum(0.0);
                newBill.setState(0);
                newBill.setShiftId(lastOpenedShift.getId());
                newBill.setSynchronized(false);
                String version ="0.0";
                try {
                    PackageInfo pInfo = POSApplication.getApplication().getPackageManager().getPackageInfo(POSApplication.getApplication().getPackageName(), 0);
                    version = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                newBill.setCurrentSoftwareVersion(version);
                newBill.setDeviceId(POSApplication.getApplication().getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("deviceId",null));

                mRealm.beginTransaction();
                    lastOpenedShift.setBillCounter(lastOpenedShift.getBillCounter() + 1);
                    mRealm.insert(newBill);
                    History createdBill = new History();
                    createdBill.setDate(newBill.getCreateDate());
                    createdBill.setMsg(context.getString(R.string.message_bill_created_nr) + newBill.getShiftReceiptNumSoftware());
                    createdBill.setType(BaseEnum.History_CreateBill);
                    mRealm.insert(createdBill);
                    billCreated = true;
                mRealm.commitTransaction();
            }
            else{
                Toast.makeText(context, "Shift is expired!", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(context, "Shift is not opened!", Toast.LENGTH_SHORT).show();
        }
       return billCreated;
    }

    @Override
    public void handleResult(Result rawResult) {
       Log.e ("TAG", "Contents = " + rawResult.getText() + ", Format = " + rawResult.getBarcodeFormat().toString() + ", Date = " + new Date().getTime());
        Log.v("TAG", rawResult.getText()); // Prints scan results
        Log.v("TAG", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        searchProductByBarcode(rawResult.getText());
    }
}