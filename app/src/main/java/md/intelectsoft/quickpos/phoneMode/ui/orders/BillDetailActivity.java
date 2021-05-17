package md.intelectsoft.quickpos.phoneMode.ui.orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.Bill;
import md.intelectsoft.quickpos.Realm.localStorage.BillString;

import static md.intelectsoft.quickpos.phoneMode.activity.MainActivityPhone.salesViewModel;

public class BillDetailActivity extends AppCompatActivity {

    private Realm mRealm;

    @BindView(R.id.orders_listLines) RecyclerView recyclerView;
    @BindView(R.id.buttonCloseBillDetail)
    MaterialButton buttonClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blueItem));
        setContentView(R.layout.activity_order_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarOrderDetail);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        mRealm = Realm.getDefaultInstance();

        String billId = getIntent().getStringExtra("ARG_ITEM_ID");
        Bill itemDetail = mRealm.where(Bill.class).equalTo("id", billId).findFirst();
        if (itemDetail != null) {
            setTitle("Bill nr: " + itemDetail.getShiftNumberSoftware());

            List<BillString> items = mRealm.copyFromRealm(itemDetail.getBillStrings().where().equalTo("isDeleted",false).findAll());
            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(items));

            if(itemDetail.getState() == 0){
                buttonClose.setVisibility(View.VISIBLE);
                buttonClose.setText(items.size() + " items = " + String.format("%.2f", itemDetail.getTotalSum()).replace(",", ".") + " MDL");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_bill_menu, menu);
        return true;
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

            holder.price.setText(String.format("%.0f", item.getPriceWithDiscount()).replace(",", ".") + " MDL\nunit");

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
