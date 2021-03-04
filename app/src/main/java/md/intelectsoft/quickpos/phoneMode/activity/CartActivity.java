package md.intelectsoft.quickpos.phoneMode.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.BillString;
import md.intelectsoft.quickpos.phoneMode.models.CartViewModel;

@SuppressLint("NonConstantResourceId")
public class CartActivity extends AppCompatActivity {

    CartViewModel cartViewModel;
    String idBill;

    @BindView(R.id.order_line_list) RecyclerView recyclerView;
    @BindView(R.id.btn_pay_bill_cart) MaterialButton buttonPay;
    @BindView(R.id.textTotalSumCart) TextView textTotalSum;

    @OnClick(R.id.btn_backFromCartPhoneMode) void onBack() {
        finish();
    }

    @OnClick(R.id.btn_pay_bill_cart) void onPay(){
        Intent payIntent = new Intent(this, PaymentActivity.class);
        payIntent.putExtra("id", idBill);
        startActivity(payIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blueItem));
        setContentView(R.layout.activity_cart);

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        cartViewModel.getBillStrings().observe( this, billStringsList -> {
            if(billStringsList.size() > 0) buttonPay.setVisibility(View.VISIBLE);
            else buttonPay.setVisibility(View.GONE);

            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(billStringsList));
        });

        idBill = getIntent().getStringExtra("billId");
        cartViewModel.getBillStringList(idBill);

        double[] retStatement = cartViewModel.getBillInfo(idBill);
        buttonPay.setText(String.format("%.0f",retStatement[0])+ " items = " + String.format("%.2f", retStatement[1]).replace(",","."));
        textTotalSum.setText("TOTAL: " + String.format("%.2f", retStatement[1]).replace(",",".") + " MDL");
    }

    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {
        private final List<BillString> mValues;
        SimpleItemRecyclerViewAdapter(List<BillString> items) {
            mValues = items;
        }

        @Override
        public SimpleItemRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.billstring_list_content, parent, false);
            return new SimpleItemRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final SimpleItemRecyclerViewAdapter.ViewHolder holder, int position) {
            BillString item = mValues.get(position);

            holder.name.setText(item.getAssortmentFullName());

            if(item.getQuantity() > 1){
                holder.pricePerOne.setVisibility(View.VISIBLE);
                long afterComma = Math.round((item.getQuantity() % 1) * 100);
                if(afterComma > 0){
                    holder.quantity.setText(String.format("%.2f", item.getQuantity()).replace(",", "."));
                }else{
                    holder.quantity.setText(String.format("%.0f", item.getQuantity()).replace(",", "."));
                }

                holder.pricePerOne.setText(String.format("%.2f", item.getPriceWithDiscount()).replace(",", ".") + " MDL");
            }
            else {
                holder.pricePerOne.setVisibility(View.GONE);
                holder.quantity.setText(String.format("%.0f", item.getQuantity()).replace(",", "."));
            }

            holder.sum.setText(String.format("%.2f", item.getSumWithDiscount()).replace(",", ".") + " MDL");
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

            ViewHolder(View view) {
                super(view);
                quantity = (TextView) view.findViewById(R.id.textLineCount);
                name = (TextView) view.findViewById(R.id.textLineName);
                pricePerOne = (TextView) view.findViewById(R.id.textLinePricePerOne);
                sum = (TextView) view.findViewById(R.id.textLineTotalSum);
            }
        }
    }
}