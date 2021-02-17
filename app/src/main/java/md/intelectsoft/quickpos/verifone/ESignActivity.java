package md.intelectsoft.quickpos.verifone;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.verifone.Utilities.Utility;
import md.intelectsoft.quickpos.verifone.basic.BaseActivity;
import md.intelectsoft.quickpos.verifone.transaction.SurfaceDrawCanvas;
import md.intelectsoft.quickpos.verifone.transaction.TransBasic;
import md.intelectsoft.quickpos.verifone.transaction.TransactionParams;

public class ESignActivity extends BaseActivity {

    private static final String TAG = "ESignActivity";
    TextView tvAmount;
    Button btnToPrint;
    private SurfaceDrawCanvas canvas;
    private String conditionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_sign);

        initView();

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        super.onAttachedToWindow();
        initESign();
    }

    //init other view
    private void initView() {
        tvAmount = findViewById(R.id.input_amount_tv);
        btnToPrint = findViewById(R.id.to_print);
        tvAmount.setText(Utility.getReadableAmount(TransactionParams.getInstance().getTransactionAmount()));
        btnToPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvas.setVisibility(View.GONE);
                saveEsignData();
                TransBasic.getInstance().printTest(1);
                finish();
            }
        });
    }

    private boolean saveEsignData() {
        Bitmap bitmap = canvas.saveCanvas();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        byte[] signData = baos.toByteArray();
        if (signData != null && signData.length > 0) {
            Log.i(TAG, "compress bmp data(length=" + signData.length + ")=" +  Utility.bcd2Asc(signData));
            TransactionParams.getInstance().setConditionCode(conditionCode);
            TransactionParams.getInstance().setEsignData(Utility.bcd2Asc(signData));
            TransactionParams.getInstance().setEsignWidth(String.valueOf(bitmap.getWidth()));
            TransactionParams.getInstance().setEsignHeight(String.valueOf(bitmap.getHeight()));
            TransactionParams.getInstance().setEsignUploadFlag(false);
        }
        return true;
    }

    //init e-sign
    @SuppressLint("ClickableViewAccessibility")
    private void initESign() {
        LinearLayout layout = findViewById(R.id.layout);
        canvas = new SurfaceDrawCanvas(md.intelectsoft.quickpos.verifone.ESignActivity.this, getConditionCode(), 360, 700, 480);
        LinearLayout.LayoutParams canvasLayout = new LinearLayout.LayoutParams(700, 480);
        canvas.setBackgroundColor(Color.parseColor("#F5F6FA"));
        canvas.setLayoutParams(canvasLayout);
        layout.addView(canvas);
        canvas.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });
    }

    private String getConditionCode() {
        int i = 0;
        TransactionParams.getInstance().setDate(Utility.getSystemDatetime().substring(0, 8));
        TransactionParams.getInstance().setReferNum(Utility.getSystemDatetime().substring(6));

        String str = TransactionParams.getInstance().getDate() + TransactionParams.getInstance().getReferNum();
        if (str != null && !str.equals("")) {
            byte[] block = Utility.asc2Bcd(str, 16);
            for (i = 0; i < 4; i++) {
                block[i] ^= block[i + 4];
            }
            conditionCode = Utility.bcd2Asc(block, 4);
            return conditionCode;
        } else
            return str;
    }

    //Set menu / home / back button invalid
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}