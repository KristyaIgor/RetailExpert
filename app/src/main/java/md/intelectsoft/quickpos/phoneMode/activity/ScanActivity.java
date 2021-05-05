package md.intelectsoft.quickpos.phoneMode.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.zxing.Result;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.AssortmentRealm;
import md.intelectsoft.quickpos.Realm.localStorage.Bill;
import md.intelectsoft.quickpos.Realm.localStorage.BillString;
import md.intelectsoft.quickpos.Realm.localStorage.History;
import md.intelectsoft.quickpos.Realm.localStorage.Shift;
import md.intelectsoft.quickpos.utils.BaseEnum;
import md.intelectsoft.quickpos.POSApplication;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static md.intelectsoft.quickpos.phoneMode.activity.MainActivityPhone.addToCart;
import static md.intelectsoft.quickpos.phoneMode.activity.MainActivityPhone.salesViewModel;
import static md.intelectsoft.quickpos.POSApplication.SharedPrefSettings;

@SuppressLint("NonConstantResourceId")
public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView mScannerView;
    TimerTask timerTaskResumeScan;
    Timer timerResumeScan;
    private Context context;
    Realm mRealm;
    ZXingScannerView.ResultHandler handler;
    String billId;

    @BindView(R.id.btn_pay_bill_scan) MaterialButton buttonPay;
    @BindView(R.id.order_line_list_scan) RecyclerView recyclerView;
    @BindView(R.id.textNoLinesInScanActivity) TextView textNoLines;

    @OnClick(R.id.imageBackToSales) void onBack(){
        finish();
    }

    @OnClick(R.id.btn_pay_bill_scan) void onPayContinue() {
        Intent payIntent = new Intent(this, PaymentActivity.class);
        payIntent.putExtra("id", billId);
        startActivityForResult(payIntent, 111);
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
        //cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        mRealm = Realm.getDefaultInstance();

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);
        mScannerView.setAspectTolerance(0.9f);
        handler = this;

        context = this;
        Intent intent = getIntent();
        billId = intent.getStringExtra("billId");

        salesViewModel.getBillStrings().observe( this, billStringsList -> {
            if(billStringsList.size() > 0) {
                buttonPay.setVisibility(View.VISIBLE);
                textNoLines.setVisibility(View.GONE);
            }
            else {
                buttonPay.setVisibility(View.GONE);
                textNoLines.setVisibility(View.VISIBLE);
            }

            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(billStringsList));
        });
        if(billId != null)
            salesViewModel.getBillStringList(billId);

        salesViewModel.getBillEntry().observe(this, bill -> {
            if(bill != null){
                billId = bill.getId();
                salesViewModel.getBillStringList(billId);

                RealmList<BillString> lines = bill.getBillStrings();
                int countLines = 0;
                if(lines != null && lines.size() > 0) {
                    for (BillString line : lines) {
                        if (line.isAllowNonInteger()) {
                            countLines += 1;
                        } else
                            countLines += line.getQuantity();
                    }
                    double sum = bill.getTotalDiscount();
                    buttonPay.setText(countLines + " items = " + String.format("%.2f", sum).replace(",", "."));
                }
                else{

                }
            }
            else{
                billId = null;
                buttonPay.setText("0 item = 0.00 MDL");
            }

        });
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
        if(item != null){
            addToCart(item);

            if (timerResumeScan != null)
                timerResumeScan.cancel();
            timerResumeScan = new Timer();

            resumeScanBarcode();
            timerResumeScan.schedule(timerTaskResumeScan, 1300);
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
                    createdBill.setMsg(context.getString(R.string.message_bill_created_nr) + newBill.getShiftNumberSoftware());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 111){
            if(resultCode == RESULT_OK){
                setResult(RESULT_OK);
                finish();
            }
            else if(resultCode == 951){
                setResult(951);
                finish();
            }
        }
    }

    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {
        private final List<BillString> mValues;
        SimpleItemRecyclerViewAdapter(List<BillString> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.billstring_list_content, parent, false);
            return new SimpleItemRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            BillString item = mValues.get(position);

            holder.name.setText(item.getAssortmentFullName());

            if(!item.isDeleted()) {
                boolean expanded = item.isExpanded();

                holder.subItem.setVisibility(expanded ? View.VISIBLE : View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                holder.collapsOtherItems(position);
                // Get the current state of the item
                boolean expandeds = item.isExpanded();
                // Change the state
                item.setExpanded(!expandeds);

                // Notify the adapter that item has changed
                notifyItemChanged(position);
            });

            if(item.getQuantity() > 1){
                holder.pricePerOne.setVisibility(View.VISIBLE);
                long afterComma = Math.round((item.getQuantity() % 1) * 100);
                if(afterComma > 0){
                    holder.quantity.setText(String.format("%.2f", item.getQuantity()).replace(",", "."));
                }else{
                    holder.quantity.setText(String.format("%.0f", item.getQuantity()).replace(",", "."));
                }

                holder.pricePerOne.setText(String.format("%.2f", item.getPriceWithDiscount()).replace(",", ".") + " MDL");

                holder.count.setText(String.format("%.0f", item.getQuantity()).replace(",", ".") + " items");

            }
            else {
                holder.pricePerOne.setVisibility(View.GONE);
                holder.quantity.setText(String.format("%.0f", item.getQuantity()).replace(",", "."));

                holder.count.setText(String.format("%.0f", item.getQuantity()).replace(",", ".") + " item");
            }

            holder.price.setText(String.format("%.0f", item.getPriceWithDiscount()).replace(",", ".") + " MDL\nunit");

            holder.sum.setText(String.format("%.2f", item.getSumWithDiscount()).replace(",", ".") + " MDL");

            holder.remove.setOnClickListener(v -> {
                salesViewModel.removeBillLine(item);
                mValues.remove(position);
                notifyItemRemoved(position);
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView quantity;
            final TextView name;
            final TextView pricePerOne;
            final TextView sum;
            final ConstraintLayout subItem;
            final Button count;
            final Button price;
            final Button remove;


            ViewHolder(View view) {
                super(view);
                quantity = (TextView) view.findViewById(R.id.textLineCount);
                name = (TextView) view.findViewById(R.id.textLineName);
                pricePerOne = (TextView) view.findViewById(R.id.textLinePricePerOne);
                sum = (TextView) view.findViewById(R.id.textLineTotalSum);
                subItem = (ConstraintLayout) view.findViewById(R.id.layoutDetailProductCartAction);

                count = (Button) view.findViewById(R.id.buttonCountItemsAdapterLine);
                price = (Button) view.findViewById(R.id.buttonUnitPriceAdapterLine);
                remove = (Button) view.findViewById(R.id.buttonRemoveItemAdapterLine);
            }

            private void collapsOtherItems(int position){
                int allItems = getItemCount();
                for(int i= 0; i < allItems; i++){
                    if(i == position)
                        continue;
                    else{
                        BillString billString = mValues.get(i);
                        if(!billString.isDeleted()){
                            boolean expand = billString.isExpanded();

                            if(expand)
                                billString.setExpanded(!expand);

                            notifyItemChanged(i);
                        }
                    }
                }
            }
        }
    }
}