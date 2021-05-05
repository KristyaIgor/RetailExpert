package md.intelectsoft.quickpos.phoneMode.ui.shiftUI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.Shift;

import static md.intelectsoft.quickpos.phoneMode.activity.MainActivityPhone.shiftViewModel;

public class ShiftListActivity extends AppCompatActivity {
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blueItem));

        setContentView(R.layout.activity_shift_list);

        Toolbar toolbar = findViewById(R.id.toolbarShift);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.shift_list);
        context = this;

        shiftViewModel.getShift().observe(this, shifts -> {
            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(shifts));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        shiftViewModel.getAllShifts();
    }

    public static class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Shift> mValues;
        private final View.OnClickListener mOnClickListener = view -> {
            Shift item = (Shift) view.getTag();

            Context context = view.getContext();
            Intent intent = new Intent(context, ShiftDetailActivity.class);
            intent.putExtra("ARG_ITEM_ID", item.getId());

            context.startActivity(intent);
        };

        SimpleItemRecyclerViewAdapter( List<Shift> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shift_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(String.valueOf(mValues.get(position).getGlobalNumber()));
            holder.author.setText("Open by: " + mValues.get(position).getAuthorName());

            boolean state = mValues.get(position).isClosed();
            if(state){
                holder.state.setText("Closed");
                holder.state.setTextColor(Color.GREEN);
            }
            else{
                holder.state.setText("Open");
                holder.state.setTextColor(Color.RED);
            }

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView author;
            final TextView state;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                author = (TextView) view.findViewById(R.id.textAuthor);
                state = (TextView) view.findViewById(R.id.textStateShift);
            }
        }
    }
}