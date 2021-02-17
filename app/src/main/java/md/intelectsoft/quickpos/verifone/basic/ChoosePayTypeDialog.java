package md.intelectsoft.quickpos.verifone.basic;
/*
 *  author: Derrick
 *  Time: 2019/5/24 17:18
 */

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import md.intelectsoft.quickpos.R;


public class ChoosePayTypeDialog extends Dialog implements View.OnClickListener {

    private View btnCardType;
    private View btnQRType;


    private Context mContext;

    OnChooseListener onChooselistener;

    public void setOnChooselistener(OnChooseListener onChooselistener) {
        this.onChooselistener = onChooselistener;
    }

    public ChoosePayTypeDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    public ChoosePayTypeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public ChoosePayTypeDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_common);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView(){

        btnCardType = findViewById(R.id.btnCardType);
        btnCardType.setOnClickListener(this);
        btnQRType = findViewById(R.id.btnQRType);
        btnQRType.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCardType:
                if(onChooselistener != null){
                    onChooselistener.onClickCardType();
                }
                this.dismiss();
                break;
            case R.id.btnQRType:
                if(onChooselistener != null){
                    onChooselistener.onClickQRType();
                }
                break;
        }
    }

    public interface OnChooseListener{
        void onClickCardType();
        void onClickQRType();
    }


}
