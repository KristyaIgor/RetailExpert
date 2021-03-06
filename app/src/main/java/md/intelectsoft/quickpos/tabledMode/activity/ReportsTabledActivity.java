package md.intelectsoft.quickpos.tabledMode.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReport;


import md.intelectsoft.quickpos.FiscalService.FiscalServiceAPI;
import md.intelectsoft.quickpos.FiscalService.FiscalServiceRetrofitClient;
import md.intelectsoft.quickpos.FiscalService.Result.SimpleResult;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.utils.BaseEnum;
import md.intelectsoft.quickpos.POSApplication;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static md.intelectsoft.quickpos.POSApplication.SharedPrefSettings;

public class ReportsTabledActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    private ConstraintLayout drawerConstraint;

    DatecsFiscalDevice myFiscalDevice;

    private ConstraintLayout csl_sales;
    private ConstraintLayout csl_shifts;
    private ConstraintLayout csl_tickets;
    private ConstraintLayout csl_reports;
    private ConstraintLayout csl_finReport;
    private ConstraintLayout csl_history;
    private ConstraintLayout csl_settings;

    Button X_report,Z_report;
    private ProgressDialog progress;
    TextView x_errore,z_errore;

    int fiscalManager = 0;
    TextView tvUserNameNav;
    TextView tvUserEmailNav;
    LayoutInflater layoutInflater;

    AlertDialog dialog_summary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout_reports);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_reports);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout_reports);
        drawerConstraint = findViewById(R.id.nav_view_menu_reports);

        csl_sales = findViewById(R.id.csl_sales);
        csl_shifts = findViewById(R.id.csl_shift);
        csl_reports = findViewById(R.id.csl_reports);
        csl_finReport = findViewById(R.id.csl_fin_reports);
        csl_history = findViewById(R.id.csl_history);
        csl_settings = findViewById(R.id.csl_setting_nav);

        X_report = findViewById(R.id.btn_x_report_settings);
        Z_report = findViewById(R.id.btn_z_report_settings);
        x_errore = findViewById(R.id.txt_error_x_report_settings);
        z_errore = findViewById(R.id.txt_error_z_report_settings);
        tvUserNameNav = findViewById(R.id.tv_user_name_nav);
        tvUserEmailNav = findViewById(R.id.tv_email_auth_user);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        layoutInflater = getLayoutInflater();

        fiscalManager = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork", BaseEnum.FISCAL_SERVICE);

        csl_sales.setOnClickListener(view -> {
            finish();
        });
        csl_shifts.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, ShiftsTabledActivity.class), BaseEnum.Activity_Shifts);
            finish();
        });
        csl_reports.setOnClickListener(view -> {
            drawer.closeDrawer(GravityCompat.START);
        });
        csl_finReport.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, FinancialRepTabledActivity.class), BaseEnum.Activity_FinRep);
            finish();
        });
        csl_history.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, HistoryTabledActivity.class), BaseEnum.Activity_History);
            finish();
        });
        csl_settings.setOnClickListener(v ->{
            startActivityForResult(new Intent(this, SettingsTabledActivity.class),BaseEnum.Activity_Settings);
            finish();
        });

        X_report.setOnClickListener(v -> {
            if(fiscalManager == BaseEnum.FISCAL_DEVICE){
                x_errore.setText("");
                progress = new ProgressDialog(ReportsTabledActivity.this);
                progress.setTitle("X report is starting !!!");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setCancelable(false);
                progress.show();

                final int[] reportNummber = new int[1];
                final cmdReport.ReportSummary reportSummary = new cmdReport.ReportSummary();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {

                            cmdReport cmd = new cmdReport();
                            reportNummber[0] = cmd.PrintXreport(reportSummary);
                        } catch (Exception e) {
                            e.printStackTrace();
                            foundMessage.obtainMessage(110,e.getMessage()).sendToTarget();
                        } finally {
                            progress.dismiss();
                            if (reportNummber[0] == -1){
                                foundMessage.obtainMessage(101).sendToTarget();
                            }
                        }
                        ReportsTabledActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (reportNummber[0] != -1) {
                                    Toast.makeText(ReportsTabledActivity.this, "X report imprimat!", Toast.LENGTH_SHORT).show();

                                    foundMessage.obtainMessage(111,reportNummber[0]).sendToTarget();
//
//
//                                    View dialogView = layoutInflater.inflate(R.layout.dialog_x_z_total,null);
//
//                                    dialog_summary = new AlertDialog.Builder(ReportsActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
//                                    dialog_summary.setCancelable(false);
//                                    dialog_summary.setView(dialogView);
//
//                                    TextView TaxA = dialogView.findViewById(R.id.tvTaxA);
//                                    TextView TaxB = dialogView.findViewById(R.id.tvTaxB);
//                                    TextView TaxC = dialogView.findViewById(R.id.tvTaxC);
//                                    TextView TaxD = dialogView.findViewById(R.id.tvTaxD);
//                                    TextView TaxE = dialogView.findViewById(R.id.tvTaxE);
//                                    TextView TaxF = dialogView.findViewById(R.id.tvTaxF);
//                                    TextView TaxG = dialogView.findViewById(R.id.tvTaxG);
//                                    TextView TaxH = dialogView.findViewById(R.id.tvTaxH);
//                                    MaterialButton btnOk = dialogView.findViewById(R.id.btn_total_reports);
//
//                                    btnOk.setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View view) {
//                                            dialog_summary.dismiss();
//                                        }
//                                    });
//
//                                    TaxA.setText(String.valueOf(reportSummary.totalA));
//                                    TaxB.setText(String.valueOf(reportSummary.totalB));
//                                    TaxC.setText(String.valueOf(reportSummary.totalC));
//                                    TaxD.setText(String.valueOf(reportSummary.totalD));
//                                    TaxE.setText(String.valueOf(reportSummary.totalE));
//                                    TaxF.setText(String.valueOf(reportSummary.totalF));
//                                    TaxG.setText(String.valueOf(reportSummary.totalG));
//                                    TaxH.setText(String.valueOf(reportSummary.totalH));
//
//                                    dialog_summary.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
//                                    dialog_summary.show();
//
//                                    DisplayMetrics metrics = new DisplayMetrics(); //get metrics of screen
//                                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
//                                    int width = (int) (metrics.widthPixels * 0.4); //set width to 50% of display
//                                    dialog_summary.getWindow().setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT); //set la
                                    progress.dismiss();
                                }
                            }
                        });
                    }
                }).start();
            }
            if(fiscalManager == BaseEnum.FISCAL_SERVICE){
                x_errore.setText("");
                String uri = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getString("FiscalServiceAddress","0.0.0.0:1111");
                FiscalServiceAPI commandServices = FiscalServiceRetrofitClient.getApiFiscalService(uri);
                Call<SimpleResult> responseCall = commandServices.printXReport(BaseEnum.FiscalPrint_Master);

                responseCall.enqueue(new Callback<SimpleResult>() {
                    @Override
                    public void onResponse(Call<SimpleResult> call, Response<SimpleResult> response) {
                        SimpleResult xResponse = response.body();
                        if(xResponse != null){
                            int errorCode = xResponse.getErrorCode();
                            if(errorCode == 0)
                                x_errore.setText("Raportul X a fost imprimat!");
                        }
                    }

                    @Override
                    public void onFailure(Call<SimpleResult> call, Throwable t) {
                        x_errore.setText(t.getMessage());
                    }
                });
            }

        });
        Z_report.setOnClickListener(v -> {
            if(fiscalManager == BaseEnum.FISCAL_DEVICE){
                z_errore.setText("");
                progress = new ProgressDialog(ReportsTabledActivity.this);
                progress.setTitle("Z report is working !!!");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setCancelable(false);
                progress.show();
                final int[] reportNumber = {0};
                final cmdReport.ReportSummary reportSummary = new cmdReport.ReportSummary();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            cmdReport cmd = new cmdReport();
                            reportNumber[0] = cmd.PrintZreport(reportSummary);
                        } catch (Exception e) {
                            e.printStackTrace();
                            foundMessage.obtainMessage(110,e.getMessage()).sendToTarget();
                        } finally {
                            progress.dismiss();
                            if (reportNumber[0] == -1){
                                foundMessage.obtainMessage(202).sendToTarget();
                            }
                        }
                        ReportsTabledActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (reportNumber[0] != -1) {
                                    Toast.makeText(ReportsTabledActivity.this, "Z report a fost imrpimat!", Toast.LENGTH_SHORT).show();

                                    foundMessage.obtainMessage(222,reportNumber[0]).sendToTarget();
//                                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_x_z_total,null);
//
//                                    dialog_summary = new AlertDialog.Builder(ReportsActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
//                                    dialog_summary.setCancelable(false);
//                                    dialog_summary.setView(dialogView);
//
//                                    TextView tvTaxA = dialogView.findViewById(R.id.tvTaxA);
//                                    TextView tvTaxB = dialogView.findViewById(R.id.tvTaxB);
//                                    TextView tvTaxC = dialogView.findViewById(R.id.tvTaxC);
//                                    TextView tvTaxD = dialogView.findViewById(R.id.tvTaxD);
//                                    TextView tvTaxE = dialogView.findViewById(R.id.tvTaxE);
//                                    TextView tvTaxF = dialogView.findViewById(R.id.tvTaxF);
//                                    TextView tvTaxG = dialogView.findViewById(R.id.tvTaxG);
//                                    TextView tvTaxH = dialogView.findViewById(R.id.tvTaxH);
//                                    MaterialButton btnOk = dialogView.findViewById(R.id.btn_total_reports);
//
//                                    btnOk.setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View view) {
//                                            dialog_summary.dismiss();
//                                        }
//                                    });
//                                    tvTaxA.setText(String.valueOf(reportSummary.totalA));
//                                    tvTaxB.setText(String.valueOf(reportSummary.totalB));
//                                    tvTaxC.setText(String.valueOf(reportSummary.totalC));
//                                    tvTaxD.setText(String.valueOf(reportSummary.totalD));
//                                    tvTaxE.setText(String.valueOf(reportSummary.totalE));
//                                    tvTaxF.setText(String.valueOf(reportSummary.totalF));
//                                    tvTaxG.setText(String.valueOf(reportSummary.totalG));
//                                    tvTaxH.setText(String.valueOf(reportSummary.totalH));
//
//                                    dialog_summary.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
//                                    dialog_summary.show();
//
//                                    DisplayMetrics metrics = new DisplayMetrics(); //get metrics of screen
//                                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
//                                    int width = (int) (metrics.widthPixels * 0.4); //set width to 50% of display
//                                    dialog_summary.getWindow().setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT); //set la
                                    progress.dismiss();
                                }
                            }
                        });

                    }
                }).start();
            }
            if(fiscalManager == BaseEnum.FISCAL_SERVICE){
                z_errore.setText("");
                String uri = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getString("FiscalServiceAddress","0.0.0.0:1111");

                FiscalServiceAPI commandServices = FiscalServiceRetrofitClient.getApiFiscalService(uri);
                Call<SimpleResult> responseCall = commandServices.printZReport(BaseEnum.FiscalPrint_Master);

                responseCall.enqueue(new Callback<SimpleResult>() {
                    @Override
                    public void onResponse(Call<SimpleResult> call, Response<SimpleResult> response) {
                        SimpleResult zResponse = response.body();
                        if(zResponse != null){
                            int errorCode = zResponse.getErrorCode();
                            if(errorCode == 0)
                                z_errore.setText("Raportul Z a fost imprimat!");
                        }
                    }

                    @Override
                    public void onFailure(Call<SimpleResult> call, Throwable t) {
                        z_errore.setText(t.getMessage());
                    }
                });
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        tvUserNameNav.setText(POSApplication.getApplication().getUser().getFirstName() + " " +  POSApplication.getApplication().getUser().getLastName());
        tvUserEmailNav.setText(POSApplication.getApplication().getUser().getEmail());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.

                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

    }
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private final Handler foundMessage = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 101){
                x_errore.setText("Nu este raspuns de la aparat!");
            }
            else if (msg.what == 202){
                z_errore.setText("Nu este raspuns de la aparat!");
            }
            else if (msg.what == 222){
                z_errore.setText("Raportul Z nr: " + msg.obj.toString());
            }
            else if (msg.what == 111){
                x_errore.setText("Raportul X din Z cu nr:" + msg.obj.toString());
            }
            else if (msg.what == 110){
                x_errore.setText("Erroare: " + msg.obj.toString());
            }
        }
    };
}
