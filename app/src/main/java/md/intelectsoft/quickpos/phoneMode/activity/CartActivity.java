package md.intelectsoft.quickpos.phoneMode.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.BillString;

import static md.intelectsoft.quickpos.phoneMode.activity.MainActivityPhone.salesViewModel;

@SuppressLint("NonConstantResourceId")
public class CartActivity extends AppCompatActivity {
    String idBill;

    @BindView(R.id.order_line_list) RecyclerView recyclerView;
    @BindView(R.id.btn_pay_bill_cart) MaterialButton buttonPay;

    @OnClick(R.id.btn_backFromCartPhoneMode) void onBack() {
        finish();
    }

    @OnClick(R.id.btn_pay_bill_cart) void onPay(){
        Intent payIntent = new Intent(this, PaymentActivity.class);
        payIntent.putExtra("id", idBill);
        startActivityForResult(payIntent, 111);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blueItem));
        setContentView(R.layout.activity_cart);

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        salesViewModel.getBillStrings().observe( this, billStringsList -> {
            if(billStringsList.size() > 0) buttonPay.setVisibility(View.VISIBLE);
            else buttonPay.setVisibility(View.GONE);

            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(billStringsList));
        });

        idBill = getIntent().getStringExtra("billId");
        salesViewModel.getBillStringList(idBill);

        double[] retStatement = salesViewModel.getBillInfo(idBill);
        buttonPay.setText(String.format("%.0f",retStatement[0])+ " items = " + String.format("%.2f", retStatement[1]).replace(",",".") + " MDL");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 111){
            if(resultCode == RESULT_OK){
                setResult(RESULT_OK);
                finish();
            }
            else if(resultCode == 951){
                setResult(951);
                finish();
            }
        }
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

            if(!item.isDeleted()) {
                boolean expanded = item.isExpanded();

                holder.subItem.setVisibility(expanded ? View.VISIBLE : View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                holder.collapsOtherItems(position);
                // Get the current state of the item
                boolean expandeds = item.isExpanded();
                // Change the state
                item.setExpanded(!expandeds);

                // Notify the adapter that item has changed
                notifyItemChanged(position);
            });

            if(item.getQuantity() > 1){
                holder.pricePerOne.setVisibility(View.VISIBLE);
                long afterComma = Math.round((item.getQuantity() % 1) * 100);
                if(afterComma > 0){
                    holder.quantity.setText(String.format("%.2f", item.getQuantity()).replace(",", "."));
                }else{
                    holder.quantity.setText(String.format("%.0f", item.getQuantity()).replace(",", "."));
                }

                holder.pricePerOne.setText(String.format("%.2f", item.getPriceWithDiscount()).replace(",", ".") + " MDL");

                holder.count.setText(String.format("%.0f", item.getQuantity()).replace(",", ".") + " items");

            }
            else {
                holder.pricePerOne.setVisibility(View.GONE);
                holder.quantity.setText(String.format("%.0f", item.getQuantity()).replace(",", "."));

                holder.count.setText(String.format("%.0f", item.getQuantity()).replace(",", ".") + " item");
            }

            holder.price.setText(String.format("%.2f", item.getPriceWithDiscount()).replace(",", ".") + " MDL\nunit");
            holder.sum.setText(String.format("%.2f", item.getSumWithDiscount()).replace(",", ".") + " MDL");

            holder.remove.setOnClickListener(v -> {
                salesViewModel.removeBillLine(item);
                mValues.remove(position);
                notifyItemRemoved(position);
            });
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
            final ConstraintLayout subItem;
            final Button count;
            final Button price;
            final Button remove;


            ViewHolder(View view) {
                super(view);
                quantity = (TextView) view.findViewById(R.id.textLineCount);
                name = (TextView) view.findViewById(R.id.textLineName);
                pricePerOne = (TextView) view.findViewById(R.id.textLinePricePerOne);
                sum = (TextView) view.findViewById(R.id.textLineTotalSum);
                subItem = (ConstraintLayout) view.findViewById(R.id.layoutDetailProductCartAction);

                count = (Button) view.findViewById(R.id.buttonCountItemsAdapterLine);
                price = (Button) view.findViewById(R.id.buttonUnitPriceAdapterLine);
                remove = (Button) view.findViewById(R.id.buttonRemoveItemAdapterLine);
            }

            private void collapsOtherItems(int position){
                int allItems = getItemCount();
                for(int i= 0; i < allItems; i++){
                    if(i == position)
                        continue;
                    else{
                        BillString billString = mValues.get(i);
                        if(!billString.isDeleted()){
                            boolean expand = billString.isExpanded();

                            if(expand)
                                billString.setExpanded(!expand);

                            notifyItemChanged(i);
                        }
                    }
                }
            }
        }
    }
}