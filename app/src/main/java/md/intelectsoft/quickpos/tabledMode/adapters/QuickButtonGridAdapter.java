package md.intelectsoft.quickpos.tabledMode.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;

import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.AssortmentRealm;

/**
 * Created by Igor on 30.01.2020
 */

public class QuickButtonGridAdapter extends ArrayAdapter<AssortmentRealm> {

    int heightView;
    public QuickButtonGridAdapter(@NonNull Context context, int resource, @NonNull List<AssortmentRealm> objects, int heightButton) {
        super(context, resource, objects);
        this.heightView = heightButton;
    }

    private static class ViewHolder {
       ConstraintLayout ll;
       TextView txtName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            convertView = inflater.inflate(R.layout.item_grid_quick_buttons,parent,false);

            viewHolder.ll = convertView.findViewById(R.id.ll_item_grid_view);
            viewHolder.txtName = convertView.findViewById(R.id.btn_assortment);

            viewHolder.ll.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, heightView));

            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        AssortmentRealm item = getItem(position);
        if (item != null) {
            if(item.getId() != null){
                viewHolder.txtName.setText(item.getName());
            }
            else{
                viewHolder.txtName.setText("");
            }
        }
        else{
            viewHolder.txtName.setText("");
        }


        return convertView;
    }
}
