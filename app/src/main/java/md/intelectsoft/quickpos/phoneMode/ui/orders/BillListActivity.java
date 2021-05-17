package md.intelectsoft.quickpos.phoneMode.ui.orders;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.Bill;

import static md.intelectsoft.quickpos.phoneMode.activity.MainActivityPhone.salesViewModel;

public class BillListActivity extends AppCompatActivity {
    Context context;
    ActionMode actionMode;
    Bill itemSelected;
    String idShift;
    SimpleItemRecyclerViewAdapter adapter;
    TextView textNoBills;
    ImageView imageNoBills;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blueItem));

        setContentView(R.layout.fragment_orders);

        Toolbar toolbar = findViewById(R.id.toolbarOrders);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        context = this;

        idShift = getIntent().getStringExtra("ShiftId");

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.orders_list);
        textNoBills = findViewById(R.id.textNoBills);
        imageNoBills = findViewById(R.id.imageNoBills);
        salesViewModel.getBillList(idShift);

        salesViewModel.getBills().observe(this, billList -> {
            if(billList.size() == 0){
                recyclerView.setVisibility(View.GONE);
                textNoBills.setVisibility(View.VISIBLE);
                imageNoBills.setVisibility(View.VISIBLE);
            }
            else{
                adapter = new SimpleItemRecyclerViewAdapter(billList);
                recyclerView.setAdapter(adapter);

                recyclerView.setVisibility(View.VISIBLE);
                textNoBills.setVisibility(View.GONE);
                imageNoBills.setVisibility(View.GONE);
            }
        });
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Chisinau");

        private final List<Bill> mValues;
//        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(actionMode != null)
//                    actionMode.finish();
//                Bill item = (Bill) view.getTag();
//
//                Context context = view.getContext();
//                Intent intent = new Intent(context, BillDetailActivity.class);
//                intent.putExtra("ARG_ITEM_ID", item.getId());
//
//                context.startActivity(intent);
//            }
//        };

        SimpleItemRecyclerViewAdapter(List<Bill> items) {
            mValues = items;
            simpleDateFormat.setTimeZone(timeZone);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.bill_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Bill pos = mValues.get(position);

            boolean expanded = pos.isExpanded();
            holder.subItem.setVisibility(expanded ? View.VISIBLE : View.GONE);

            holder.itemView.setOnClickListener(v -> {
                holder.collapsOtherItems(position);
                // Get the current state of the item
                boolean expandeds = pos.isExpanded();
                // Change the state
                pos.setExpanded(!expandeds);

                // Notify the adapter that item has changed
                notifyItemChanged(position);
            });

            holder.number.setText(String.valueOf(pos.getShiftNumberSoftware()));
            holder.sum.setText(pos.getTotalSum() + " MDL");

            if(pos.getState() == 0)
                holder.sum.setTextColor(context.getColor(R.color.red));

            holder.date.setText(simpleDateFormat.format(pos.getCreateDate()));

            holder.itemView.setTag(mValues.get(position));
//            holder.itemView.setOnClickListener(mOnClickListener);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    itemSelected = (Bill)v.getTag();
                    actionMode = BillListActivity.this.startActionMode(callback);
                    v.setSelected(true);
                    actionMode.setTitle(itemSelected.getShiftNumberSoftware() + " " + itemSelected.getUserName());

                    return true;
                }
            });

            holder.edit.setOnClickListener(v -> {
                Intent resumeEditOrder = new Intent();
                resumeEditOrder.putExtra("id", pos.getId());
                setResult(154, resumeEditOrder);
                finish();
            });
            holder.info.setOnClickListener(v -> {
                Context context = v.getContext();
                Intent intent = new Intent(context, BillDetailActivity.class);
                intent.putExtra("ARG_ITEM_ID", pos.getId());

                context.startActivity(intent);
            });
            holder.remove.setOnClickListener(v -> {
                if(salesViewModel.deleteBill(pos)) {
                    salesViewModel.getBillList(idShift);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView sum, date, number, name;
            final ConstraintLayout subItem;
            final Button edit, info, remove;

            ViewHolder(View view) {
                super(view);
                name = view.findViewById(R.id.textBillName);
                date = view.findViewById(R.id.textBillDate);
                sum = view.findViewById(R.id.textBillTotalSum);
                number =  view.findViewById(R.id.textBillNumberList);

                edit = view.findViewById(R.id.buttonEditBill);
                info = view.findViewById(R.id.buttonInfoBill);
                remove =  view.findViewById(R.id.buttonDeleteBill);

                subItem = (ConstraintLayout) view.findViewById(R.id.layoutDetailProductCartAction);
            }

            private void collapsOtherItems(int position){
                int allItems = getItemCount();
                for(int i= 0; i < allItems; i++){
                    if(i == position)
                        continue;
                    else{
                        Bill billString = mValues.get(i);
                        boolean expand = billString.isExpanded();
                        if(expand)
                            billString.setExpanded(!expand);

                        notifyItemChanged(i);

                    }
                }
            }
        }
    }

    public ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.selected_bill_menu, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            MenuItem menuItemDelete = menu.findItem(R.id.item_delete);
            MenuItem menuItemShare = menu.findItem(R.id.item_editOrder);

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_delete:{
                    if(salesViewModel.deleteBill(itemSelected)) {
                        salesViewModel.getBillList(idShift);
                        actionMode.finish();
                    }

                    return true;
                }
                case R.id.item_editOrder: {
                    Intent resumeEditOrder = new Intent();
                    resumeEditOrder.putExtra("id", itemSelected.getId());
                    setResult(154, resumeEditOrder);
                    finish();
                }
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    };
}
