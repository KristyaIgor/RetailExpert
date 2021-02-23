package md.intelectsoft.quickpos.phoneMode.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import md.intelectsoft.quickpos.R;

import md.intelectsoft.quickpos.Realm.localStorage.Shift;
import md.intelectsoft.quickpos.phoneMode.viewModels.ShiftViewModel;

import java.util.List;

/**
 * An activity representing a list of Shifts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ShiftDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ShiftListActivity extends Fragment {
    public ShiftViewModel shiftViewModel;
    private Context context;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        shiftViewModel = new ViewModelProvider(this).get(ShiftViewModel.class);

        View rootView = inflater.inflate(R.layout.activity_shift_list, container, false);

        context = getContext();

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.shift_list);

        shiftViewModel.getShift().observe(getViewLifecycleOwner(), shifts -> {
            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter((SalesActivity)getActivity(), shifts));
        });

        shiftViewModel.getAllShifts();

        return rootView;
    }

    public static class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final SalesActivity mParentActivity;
        private final List<Shift> mValues;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Shift item = (Shift) view.getTag();

                Context context = view.getContext();
                Intent intent = new Intent(context, ShiftDetailActivity.class);
                intent.putExtra(ShiftDetailFragment.ARG_ITEM_ID, item.getId());

                context.startActivity(intent);
            }
        };

        SimpleItemRecyclerViewAdapter(SalesActivity parent, List<Shift> items) {
            mValues = items;
            mParentActivity = parent;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.shift_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(mValues.get(position).getAuthorName());
            holder.mContentView.setText(mValues.get(position).getName());

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }
}