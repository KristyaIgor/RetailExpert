package md.intelectsoft.quickpos.phoneMode.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import md.intelectsoft.quickpos.R;

public class FinancialReportsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blueItem));
        setContentView(R.layout.activity_financial_reports);

        Toolbar toolbar = findViewById(R.id.toolbarFinReports);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton buttonInsert = findViewById(R.id.btn_InserMoney);
        MaterialButton buttonWithDraw = findViewById(R.id.btn_WithdrawMoney);

        Intent operation = new Intent(this, InOutCashActivity.class);

        buttonInsert.setOnClickListener(v -> {
            operation.putExtra("OperationType", 1);
            startActivity(operation);
        });

        buttonWithDraw.setOnClickListener(v -> {
            operation.putExtra("OperationType", -1);
            startActivity(operation);
        });
    }
}