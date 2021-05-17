package md.intelectsoft.quickpos.phoneMode.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import md.intelectsoft.quickpos.EPOSService.Results.PaymentType;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.Bill;
import md.intelectsoft.quickpos.utils.BaseEnum;

@SuppressLint("NonConstantResourceId")
public class PaymentActivity extends AppCompatActivity {
    Realm mRealm;

    @BindView(R.id.textTotalToPay) TextView textTotalToPay;
    @BindView(R.id.buttonCashPay) Button btnCashPay;
    @BindView(R.id.buttonCreditPay) Button btnCreditCard;
    @BindView(R.id.buttonOtherPay) Button btnOtherPay;

    @OnClick(R.id.btn_backFromPaymentActivityPhoneMode) void onBack(){
        finish();
    }

    Context context;

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
        context = this;

        String billId = getIntent().getStringExtra("id");
        Bill bill = mRealm.where(Bill.class).equalTo("id", billId).findFirst();
        if(bill != null){
            textTotalToPay.setText(String.format("%.2f", bill.getTotalSum()).replace(",", "."));
        }

        //caut tipurile de plata care sunt in baza si le adaug la cash si card plus altele
        RealmResults<PaymentType> paymentTypesResult = mRealm.where(PaymentType.class).findAll();
        if(!paymentTypesResult.isEmpty()){
            for(int o = 0; o <paymentTypesResult.size(); o++){
                PaymentType paymentType = new PaymentType();
                paymentType.setCode(paymentTypesResult.get(o).getCode());
                paymentType.setPredefinedIndex(paymentTypesResult.get(o).getPredefinedIndex());
                paymentType.setPrintFiscalCheck(paymentTypesResult.get(o).getPrintFiscalCheck());
                paymentType.setExternalId(paymentTypesResult.get(o).getExternalId());
                paymentType.setName(paymentTypesResult.get(o).getName());

                if(paymentTypesResult.get(o).getPredefinedIndex() == BaseEnum.Pay_Cash) {
                    btnCashPay.setTag(paymentType);
                    btnCashPay.setOnClickListener(v -> {
                        Intent payCash = new Intent(context, InputReceiveActivity.class);
                        payCash.putExtra("billId", billId);
                        payCash.putExtra("payCode", paymentType.getPredefinedIndex());
                        payCash.putExtra("payId", paymentType.getExternalId());
                        payCash.putExtra("payName", paymentType.getName());
                        payCash.putExtra("sum", bill.getTotalSum());
                        startActivityForResult(payCash,101);
                    });


//                    if(!onlyFiscalPay && billInitPay)
//                        btnCashPay.setEnabled(false);   //daca este vreo achitare nefiscala , dezactivez buton cash
                }
                else if(paymentTypesResult.get(o).getPredefinedIndex() == BaseEnum.Pay_CreditCard){
                    btnCreditCard.setTag(paymentType);
                    btnCreditCard.setOnClickListener(v -> {
                        Intent payCash = new Intent(context, CreditCardPayActivity.class);
                        payCash.putExtra("billId", billId);
                        payCash.putExtra("payCode", paymentType.getPredefinedIndex());
                        payCash.putExtra("payId", paymentType.getExternalId());
                        payCash.putExtra("payName", paymentType.getName());
                        payCash.putExtra("sum", bill.getTotalSum());
                        startActivityForResult(payCash,101);
                    });

//                    if(!onlyFiscalPay && billInitPay)
//                        btnCreditCard.setEnabled(false);   //daca este vreo achitare nefiscala , dezactivez buton card
                }
            }

            btnOtherPay.setOnClickListener(view -> {
                showOtherPaymentType();
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101){
            if(resultCode == RESULT_OK){
                setResult(RESULT_OK);
                finish();
            }
            else if (resultCode == 951){
                setResult(951);
                finish();
            }
        }

    }

    private void showOtherPaymentType(){
        mRealm.executeTransaction(realm -> {
            RealmResults<PaymentType> result = realm.where(PaymentType.class)
                    .notEqualTo("predefinedIndex",BaseEnum.Pay_Cash)
                    .and()
                    .notEqualTo("predefinedIndex",BaseEnum.Pay_CreditCard)
                    .findAll();
            if(!result.isEmpty()){
                List<PaymentType> list = new ArrayList<>();
                list.addAll(result);

                OtherPaymentTypes adapter = new OtherPaymentTypes(list);

//                ListAdapter adapterDialog = new ArrayAdapter<PaymentType>(context, R.layout.item_workplace_main_dialog, list) {
//
//                    ViewHolder holder;
//
//                    class ViewHolder {
//                        TextView title;
//                    }
//
//                    public View getView(int position, View convertView, ViewGroup parent) {
//                        if (convertView == null) {
//                            convertView = inflater.inflate(R.layout.item_workplace_main_dialog, null);
//
//                            holder = new ViewHolder();
//                            holder.title = (TextView) convertView.findViewById(R.id.textView122);
//                            convertView.setTag(holder);
//                        } else {
//                            // view already defined, retrieve view holder
//                            holder = (ViewHolder) convertView.getTag();
//                        }
//                        holder.title.setText(list.get(position).getName());
//                        holder.title.setTag(list.get(position));
//
//                        return convertView;
//                    }
//                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.message_select_other_pay_type);
                builder.setAdapter(adapter, (dialog, position) -> {
                    PaymentType paySelected = list.get(position);

//                    setClickListenerOtherPay(paySelected);

                    dialog.dismiss();
                });
                builder.setNegativeButton(R.string.btn_cancel,(dialogInterface, i) -> dialogInterface.dismiss());

                AlertDialog alert = builder.create();

                alert.show();
//
//                int displayWidth = displayMetrics.widthPixels;
//                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
//                layoutParams.copyFrom(alert.getWindow().getAttributes());
//                int dialogWindowWidth = (int) (displayWidth * 0.4f);
//                layoutParams.width = dialogWindowWidth;
//                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
//                alert.getWindow().setAttributes(layoutParams);
            }
        });
    }

    private class OtherPaymentTypes implements ListAdapter {

        List<PaymentType> list;

        public OtherPaymentTypes(List<PaymentType> list) {
            this.list = list;
        }

        ViewHolder holder;

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int i) {
//            if(onlyFiscalPay && billInitPay && list.get(i).getPrintFiscalCheck())
//                return true;
//            else if(!onlyFiscalPay && billInitPay && !list.get(i).getPrintFiscalCheck())
//                return true;
//            else if (!onlyFiscalPay && !billInitPay && !list.get(i).getPrintFiscalCheck())
//                return true;
//            else
//                return false;
            return true;
        }

        class ViewHolder {
            TextView title;
        }


        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public PaymentType getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return list.get(i).getPredefinedIndex();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(PaymentActivity.this).inflate(R.layout.item_workplace_main_dialog, null);

                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.textView122);
                convertView.setTag(holder);
            } else {
                // view already defined, retrieve view holder
                holder = (ViewHolder) convertView.getTag();
            }

            PaymentType item = list.get(position);
            holder.title.setText(item.getName());
            holder.title.setTag(item);



            convertView.setEnabled( isEnabled(position));

            return convertView;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return list.size();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}