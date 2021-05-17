package md.intelectsoft.quickpos.phoneMode.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import md.intelectsoft.quickpos.R;

@SuppressLint("NonConstantResourceId")
public class CountSelectActivity extends AppCompatActivity {

    boolean notInputAmount = true;
    @BindView(R.id.textQuantitySelected)
    TextView inputQunatity;

    @OnClick(R.id.btn_backFromSelectQuantityActivityPhoneMode) void onBack(){
        finish();
    }

    @OnClick(R.id.textButtonCount1) void onButton1(){
        if(notInputAmount){
            inputQunatity.setText("1");
            notInputAmount = false;
        }
        else{
            if (inputQunatity.getText().toString().equals("0")) inputQunatity.setText("1");
            else inputQunatity.append("1");
        }
    }
    @OnClick(R.id.textButtonCount2) void onButton2(){
        if(notInputAmount){
            inputQunatity.setText("2");
            notInputAmount = false;
        }
        else{
            if (inputQunatity.getText().toString().equals("0")) inputQunatity.setText("2");
            else inputQunatity.append("2");
        }
    }
    @OnClick(R.id.textButtonCount3) void onButton3(){
        if(notInputAmount){
            inputQunatity.setText("3");
            notInputAmount = false;
        }
        else {
            if (inputQunatity.getText().toString().equals("0")) inputQunatity.setText("3");
            else inputQunatity.append("3");
        }
    }
    @OnClick(R.id.textButtonCount4) void onButton4(){
        if(notInputAmount){
            inputQunatity.setText("4");
            notInputAmount = false;
        }
        else {
            if (inputQunatity.getText().toString().equals("0")) inputQunatity.setText("4");
            else inputQunatity.append("4");
        }
    }
    @OnClick(R.id.textButtonCount5) void onButton5(){
        if(notInputAmount){
            inputQunatity.setText("5");
            notInputAmount = false;
        }else {
            if (inputQunatity.getText().toString().equals("0")) inputQunatity.setText("5");
            else inputQunatity.append("5");
        }
    }
    @OnClick(R.id.textButtonCount6) void onButton6() {
        if(notInputAmount){
            inputQunatity.setText("6");
            notInputAmount = false;
        }
        else {
            if (inputQunatity.getText().toString().equals("0")) inputQunatity.setText("6");
            else inputQunatity.append("6");
        }
    }
    @OnClick(R.id.textButtonCount7) void onButton7(){
        if(notInputAmount){
            inputQunatity.setText("7");
            notInputAmount = false;
        }else {
            if (inputQunatity.getText().toString().equals("0")) inputQunatity.setText("7");
            else inputQunatity.append("7");
        }
    }
    @OnClick(R.id.textButtonCount8) void onButton8(){
        if(notInputAmount){
            inputQunatity.setText("8");
            notInputAmount = false;
        }
        else {
            if (inputQunatity.getText().toString().equals("0"))
                inputQunatity.setText("8");
            else
                inputQunatity.append("8");
        }
    }
    @OnClick(R.id.textButtonCount9) void onButton9(){
        if(notInputAmount){
            inputQunatity.setText("9");
            notInputAmount = false;
        }
        else {
            if (inputQunatity.getText().toString().equals("0"))
                inputQunatity.setText("9");
            else
                inputQunatity.append("9");
        }
    }
    @OnClick(R.id.textButtonCount0) void onButton0(){
        if(notInputAmount){
            inputQunatity.setText("0");
            notInputAmount = false;
        }else {
            if (inputQunatity.getText().toString().equals("0"))
                inputQunatity.setText("0");
            else
                inputQunatity.append("0");
        }
    }
    @OnClick(R.id.textButtonClearCountSum) void onButtonClear(){
        if(!inputQunatity.getText().toString().contains("."))
            inputQunatity.append(".");
    }
    @OnClick(R.id.textButtonDeleteCountSum) void onButtonDelete(){
        if(notInputAmount){
            inputQunatity.setText("0");
        }
        else{
            String text = inputQunatity.getText().toString();
            if(text.length() - 1 != 0){
                inputQunatity.setText(text.substring(0, text.length() - 1));
            }
            else
                inputQunatity.setText("0");
        }
    }

    @OnClick(R.id.buttonSelectQuantity) void onSelect() {
        if(!inputQunatity.getText().toString().equals("0")){
            Intent selected = new Intent();
            selected.putExtra("CountSelected", Integer.valueOf(inputQunatity.getText().toString()));
            setResult(RESULT_OK, selected);
            finish();
        }
    }

    @OnClick(R.id.btnRemoveQuantity) void onRemove(){
        String count = inputQunatity.getText().toString().equals("") ? "0" : inputQunatity.getText().toString();
        inputQunatity.setText(String.valueOf(Integer.valueOf(count) - 1 > 0 ? Integer.valueOf(count) - 1 : 0));
    }

    @OnClick(R.id.btnAddQuantity) void onAdd(){
        String count = inputQunatity.getText().toString().equals("") ? "0" : inputQunatity.getText().toString();
        inputQunatity.setText(String.valueOf(Integer.valueOf(count) + 1 ));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blueItem));

        setContentView(R.layout.activity_count_select);

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);
    }
}