package md.intelectsoft.quickpos.phoneMode.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;

import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.AssortmentRealm;
import md.intelectsoft.quickpos.Realm.localStorage.Barcodes;

import static md.intelectsoft.quickpos.phoneMode.ui.sales.SalesFragment.isIsViewWithCatalog;
import static md.intelectsoft.quickpos.phoneMode.ui.sales.SalesFragment.setAssortmentClicked;


/**
 * Created by Igor on 10.02.2020
 */

public class AssortmentListGridAdapter extends ArrayAdapter<AssortmentRealm> {
    int layoutId;
    private AssortmentItemActionListener assortmentItemActionListener;

    public void setAssortmentItemActionListener (AssortmentItemActionListener actionListener){
        this.assortmentItemActionListener = actionListener;
    }

    public AssortmentListGridAdapter(@NonNull Context context, int resource, @NonNull List<AssortmentRealm> objects) {
        super(context, resource, objects);
        this.layoutId = resource;
    }

    private static class ViewHolder {
        TextView productName, productName2, productCode, productPrice, productStock;
        ImageView productImage, productImageCopy;
        ConstraintLayout layoutParent;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;

        viewHolder = new ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if(isIsViewWithCatalog()){
            // show products in grid view
            convertView = inflater.inflate(R.layout.item_grid_multi_columns ,null,false);

            viewHolder.productName = convertView.findViewById(R.id.productName);
            viewHolder.productName2 = convertView.findViewById(R.id.textProductName2);
            viewHolder.productImage = convertView.findViewById(R.id.roundedImageView);
            viewHolder.productImageCopy = convertView.findViewById(R.id.roundedImageViewCopy);
            viewHolder.productPrice = convertView.findViewById(R.id.productPrice);
            viewHolder.layoutParent = convertView.findViewById(R.id.ll_item_grid_view);

            AssortmentRealm item = getItem(position);

            viewHolder.productName.setText(item.getName());
            if(item.isFolder()) {
                viewHolder.productImage.setVisibility(View.VISIBLE);
                viewHolder.productName2.setVisibility(View.GONE);
                viewHolder.productPrice.setVisibility(View.INVISIBLE);
            }else {
                viewHolder.productImage.setVisibility(View.INVISIBLE);
                viewHolder.productImageCopy.setVisibility(View.INVISIBLE);
                viewHolder.productName2.setVisibility(View.VISIBLE);
                viewHolder.productName2.setText(item.getName());
                viewHolder.productPrice.setText(item.getPrice() + " MDL");

                viewHolder.layoutParent.setOnClickListener(v -> {
                    if(assortmentItemActionListener != null)
                        assortmentItemActionListener.onItemTap(viewHolder.productImageCopy);
                    setAssortmentClicked(item);
                });
            }
        }
        else{
            convertView = inflater.inflate(R.layout.item_grid_one_columns ,null,false);

            viewHolder.productName = convertView.findViewById(R.id.productName);
            viewHolder.productName2 = convertView.findViewById(R.id.textProductName2);
            viewHolder.productImage = convertView.findViewById(R.id.roundedImageView);
            viewHolder.productImageCopy = convertView.findViewById(R.id.roundedImageViewCopy);
            viewHolder.productPrice = convertView.findViewById(R.id.productPrice);
            viewHolder.productCode = convertView.findViewById(R.id.productCode);
            viewHolder.productStock = convertView.findViewById(R.id.productStock);
            viewHolder.layoutParent = convertView.findViewById(R.id.ll_item_grid_view);

            AssortmentRealm item = getItem(position);

            viewHolder.productName.setText(item.getName());
            if(item.isFolder()) {
                viewHolder.productImage.setVisibility(View.VISIBLE);
                viewHolder.productName2.setVisibility(View.GONE);
                viewHolder.productPrice.setVisibility(View.GONE);
                viewHolder.productCode.setVisibility(View.GONE);
                viewHolder.productStock.setVisibility(View.GONE);
            }else {

                viewHolder.productImage.setVisibility(View.INVISIBLE);
                viewHolder.productImageCopy.setVisibility(View.INVISIBLE);
                viewHolder.productName2.setVisibility(View.VISIBLE);

                viewHolder.layoutParent.setOnClickListener(v -> {
                    if(assortmentItemActionListener != null)
                        assortmentItemActionListener.onItemTap(viewHolder.productImageCopy);
                    setAssortmentClicked(item);
                });


                viewHolder.productName2.setText(item.getName());
                viewHolder.productPrice.setText(item.getPrice() + " MDL");

                if(item.getBarcodes() != null && item.getBarcodes().size() == 1){
                    Barcodes itemBarcode = item.getBarcodes().get(0);
                    viewHolder.productCode.setVisibility(View.VISIBLE);
                    viewHolder.productCode.setText("Code " + itemBarcode.getBar());
                }
                else
                    viewHolder.productCode.setVisibility(View.GONE);

                if(item.getStockBalance() != 0){
                    viewHolder.productStock.setVisibility(View.VISIBLE);
                    viewHolder.productStock.setText(item.getStockBalance() + item.getUnit());
                }
                else{
                    viewHolder.productStock.setVisibility(View.GONE);
                }
            }
        }

        return convertView;
    }

    public interface AssortmentItemActionListener{
        void onItemTap(ImageView imageView);
    }
}
