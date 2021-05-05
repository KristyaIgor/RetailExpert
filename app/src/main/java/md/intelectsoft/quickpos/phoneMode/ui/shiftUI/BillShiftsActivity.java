package md.intelectsoft.quickpos.phoneMode.ui.shiftUI;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.Bill;

public class BillShiftsActivity extends AppCompatActivity {

    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blueItem));

        setContentView(R.layout.activity_bill_shifts);

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarBillShift);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());

        mRealm = Realm.getDefaultInstance();

        String shiftId = getIntent().getStringExtra("id");
        int number = getIntent().getIntExtra("number", 1);

        setTitle("Bills at shift: " + number);

        RealmResults<Bill> shiftBills = mRealm.where(Bill.class).equalTo("shiftId", shiftId).findAll();
        if(!shiftBills.isEmpty()){

        }
    }
}