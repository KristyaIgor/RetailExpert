package md.intelectsoft.quickpos.phoneMode.ui.orders;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import io.realm.RealmList;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.Bill;
import md.intelectsoft.quickpos.Realm.localStorage.Shift;
import md.intelectsoft.quickpos.phoneMode.activity.SalesActivity;
import md.intelectsoft.quickpos.phoneMode.activity.shiftUI.ShiftDetailActivity;
import md.intelectsoft.quickpos.phoneMode.activity.shiftUI.ShiftListActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static md.intelectsoft.quickpos.phoneMode.ui.sales.SalesFragment.idShift;

public class OrdersFragment extends Fragment {

    private OrdersViewModel ordersViewModel;
    Context context;
    ActionMode actionMode;
    Bill itemSelected;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ordersViewModel = new ViewModelProvider(this).get(OrdersViewModel.class);
        
        View root = inflater.inflate(R.layout.fragment_orders, container, false);

        context = getContext();

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.orders_list);
        ordersViewModel.getBillList(idShift);
        
        ordersViewModel.getBills().observe(getViewLifecycleOwner(), billList -> {
            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(billList));
        });

        return root;
    }
    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Chisinau");

        private final List<Bill> mValues;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(actionMode != null)
                    actionMode.finish();
//                Shift item = (Shift) view.getTag();
//
//                Context context = view.getContext();
//                Intent intent = new Intent(context, OrdersDetailActivity.class);
//                intent.putExtra("ARG_ITEM_ID", item.getId());
//
//                context.startActivity(intent);
            }
        };

        SimpleItemRecyclerViewAdapter( List<Bill> items) {
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

            holder.number.setText(String.valueOf(pos.getShiftReceiptNumSoftware()));
            holder.sum.setText(pos.getTotalSum() + " MDL");

            if(pos.getState() == 0)
                holder.state.setText("Open");
            else
                holder.state.setText("Closed");

            holder.date.setText(simpleDateFormat.format(pos.getCreateDate()));

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    itemSelected = (Bill)v.getTag();
                    actionMode = getActivity().startActionMode(callback);
                    v.setSelected(true);
                    actionMode.setTitle(itemSelected.getShiftReceiptNumSoftware() + " " + itemSelected.getUserName());

                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView state, sum, date, number, name;

            ViewHolder(View view) {
                super(view);
                state = view.findViewById(R.id.textBillState);
                name = view.findViewById(R.id.textBillName);
                date = view.findViewById(R.id.textBillDate);
                sum = view.findViewById(R.id.textBillTotalSum);
                number =  view.findViewById(R.id.textBillNumberList);
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
            MenuItem menuItemShare = menu.findItem(R.id.item_share);

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_delete:{
                    if(ordersViewModel.deleteBill(itemSelected)) {
                        ordersViewModel.getBillList(idShift);
                        actionMode.finish();
                    }

                    return true;
                }
                case R.id.item_share: {

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
