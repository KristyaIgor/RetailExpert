package md.intelectsoft.quickpos.phoneMode.ui.shiftUI;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
import md.intelectsoft.quickpos.Realm.localStorage.Shift;
import md.intelectsoft.quickpos.POSApplication;

import static md.intelectsoft.quickpos.phoneMode.activity.MainActivityPhone.salesViewModel;

@SuppressLint("NonConstantResourceId")
public class ShiftDetailActivity extends AppCompatActivity {

    @BindView(R.id.btn_closeShift) MaterialButton buttonCloseShift;
    @BindView(R.id.textShiftNumberInfo) TextView shiftNumber;
    @BindView(R.id.textOpenByInfo) TextView shiftOpenBy;
    @BindView(R.id.textOpenDateInfo) TextView shiftOpenDate;
    @BindView(R.id.textClosedByInfo) TextView shiftClosedBy;
    @BindView(R.id.textClosedDateInfo) TextView shiftClosedDate;
    @BindView(R.id.textNumberOfSalesInfo) TextView shiftNumberBills;
    @BindView(R.id.textCashSalesInfo) TextView shiftCashSales;
    @BindView(R.id.textCardSalesInfo) TextView shiftCardSales;
    @BindView(R.id.textOtherSalesInfo) TextView shiftOtherSales;
    @BindView(R.id.textTotalSalesInfo) TextView shiftTotalSales;
    @BindView(R.id.textTotalCashSalesInfo) TextView shiftCashTotal;
    @BindView(R.id.textCashWithdrawInfo) TextView shiftCashWithdraw;
    @BindView(R.id.textCashInsertInfo) TextView shiftCashInsert;
    @BindView(R.id.textCashInBoxInfo) TextView shiftCashInBox;
    @BindView(R.id.textOpenBillListInfo) TextView shiftOpenBillList;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    TimeZone timeZone = TimeZone.getTimeZone("Europe/Chisinau");
    private Realm mRealm;
    String shiftId;
    int numberShift;
    Shift shift;

    @OnClick(R.id.textOpenBillListInfo) void onOpenBills(){
        Intent shiftBills = new Intent(ShiftDetailActivity.this, BillShiftsActivity.class);
        shiftBills.putExtra("id", shiftId);
        shiftBills.putExtra("number", numberShift);
        startActivity(shiftBills);
    }

    @OnClick(R.id.btn_closeShift) void onCloseShift(){
        closeShift(shift);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blueItem));
        setContentView(R.layout.activity_shift_detail);

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        mRealm = Realm.getDefaultInstance();
        simpleDateFormat.setTimeZone(timeZone);

        String idShift = getIntent().getStringExtra("ARG_ITEM_ID");

        shift = mRealm.copyFromRealm(mRealm.where(Shift.class).equalTo("id", idShift).findFirst());

        if(shift != null){
            setTitle("Shift: " + shift.getGlobalNumber());
            shiftId = shift.getId();
            numberShift = shift.getGlobalNumber();
            shiftNumber.setText(String.valueOf(shift.getGlobalNumber()));
            shiftOpenBy.setText(shift.getAuthorName());
            shiftOpenDate.setText(simpleDateFormat.format(shift.getStartDate()));
            if(shift.isClosed()){
                shiftClosedBy.setText(shift.getClosedByName());
                shiftClosedDate.setText(simpleDateFormat.format(shift.getEndDate()));
            }
            else{
                shiftClosedBy.setText("-");
                shiftClosedDate.setText("-");
            }
            shiftNumberBills.setText(String.valueOf(shift.getBillCounter()));

            if(shift.isClosed()) buttonCloseShift.setVisibility(View.GONE);

            double sumOfCash = 0;
            double sumOfCard = 0;
            double sumOfOther = 0;

            RealmResults<Bill> shiftBills = mRealm.where(Bill.class).equalTo("shiftId", shift.getId()).and().equalTo("state",1).findAll();
            RealmQuery<BillPaymentType> query = mRealm.where(BillPaymentType.class);

            shiftNumberBills.setText(String.valueOf(shiftBills.size()));
            // ids is a list of the category ids
            if (shiftBills.size() > 0) {
                query = query.equalTo("billID", shiftBills.get(0).getId());
                for (int i = 1; i < shiftBills.size(); i++) {
                    query = query.or().equalTo("billID", shiftBills.get(i).getId());
                }
            }
            if(!shiftBills.isEmpty()){
                if (shiftBills.size() > 0) {
                    RealmResults<BillPaymentType> result = query.findAll();
                    if(!result.isEmpty()){
                        for(BillPaymentType pay : result){
                            if(pay.getPaymentCode() == 1)
                                sumOfCash += pay.getSum();
                            else if (pay.getPaymentCode() == 2)
                                sumOfCard += pay.getSum();
                            else
                                sumOfOther += pay.getSum();
                        }
                    }
                }
            }
            shiftCashSales.setText("MDL " + String.valueOf(sumOfCash));
            shiftCardSales.setText("MDL " + String.valueOf(sumOfCard));
            shiftOtherSales.setText("MDL " + String.valueOf(sumOfOther));
            shiftTotalSales.setText("MDL " + String.valueOf(sumOfOther + sumOfCash + sumOfCard));

            shiftCashTotal.setText("MDL " + String.valueOf(sumOfCash));
            shiftCashWithdraw.setText("MDL " + String.valueOf(shift.getCashOut()));
            shiftCashInsert.setText("MDL " + String.valueOf(shift.getCashIn()));
            shiftCashInBox.setText("MDL " + String.valueOf(sumOfCash + shift.getCashIn() - shift.getCashOut()));
        }
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
            new MaterialAlertDialogBuilder(ShiftDetailActivity.this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setTitle(R.string.message_attention)
                    .setMessage(getString(R.string.message_cannot_close_shift_bill_active) + result + getString(R.string.message_open_bills))
                    .setCancelable(false)
                    .setPositiveButton(R.string.btn_ok, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .show();
        }
        else{
            buttonCloseShift.setVisibility(View.GONE);

            shiftClosedBy.setText(shift.getClosedByName());
            shiftClosedDate.setText(simpleDateFormat.format(shift.getEndDate()));
        }
    }
}