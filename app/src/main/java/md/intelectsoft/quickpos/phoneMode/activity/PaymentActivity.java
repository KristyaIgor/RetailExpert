package md.intelectsoft.quickpos.phoneMode.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import md.intelectsoft.quickpos.EPOSService.Results.PaymentType;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.Bill;
import md.intelectsoft.quickpos.phoneMode.models.CartViewModel;

@SuppressLint("NonConstantResourceId")
public class PaymentActivity extends AppCompatActivity {
    Realm mRealm;

    @BindView(R.id.textTotalToPay) TextView textTotalToPay;

    @OnClick(R.id.btn_backFromPaymentActivityPhoneMode) void onBack(){
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blueItem));
        setContentView(R.layout.activity_payment);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);
        mRealm = Realm.getDefaultInstance();

        String billId = getIntent().getStringExtra("id");
        Bill bill = mRealm.where(Bill.class).equalTo("id", billId).findFirst();
        if(bill != null){
            textTotalToPay.setText(String.format("%.2f", bill.getTotalSum()).replace(",", "."));
        }
        RealmResults<PaymentType> payments = mRealm.where(PaymentType.class).findAll();
        if(!payments.isEmpty()){

        }
    }
}