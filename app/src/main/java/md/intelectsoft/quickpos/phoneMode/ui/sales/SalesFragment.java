package md.intelectsoft.quickpos.phoneMode.ui.sales;

import android.Manifest;
import android.animation.Animator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.AssortmentRealm;
import md.intelectsoft.quickpos.Realm.localStorage.History;
import md.intelectsoft.quickpos.Realm.localStorage.Shift;
import md.intelectsoft.quickpos.phoneMode.activity.ScanActivity;
import md.intelectsoft.quickpos.utils.BaseEnum;
import md.intelectsoft.quickpos.utils.CircleAnimationUtil;
import md.intelectsoft.quickpos.utils.IOnBackPressed;
import md.intelectsoft.quickpos.utils.POSApplication;
import md.intelectsoft.quickpos.utils.SPFHelp;
import md.intelectsoft.quickpos.utils.SearchView;
import md.intelectsoft.quickpos.phoneMode.adapters.AssortmentListGridAdapter;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.SEARCH_SERVICE;

public class SalesFragment extends Fragment  implements IOnBackPressed {

    public static SalesViewModel salesViewModel;
    private GridView gridViewProducts;
    private AssortmentListGridAdapter adapter;
    private ImageView scanCode, changeColumns;
    private GestureDetectorCompat detector;
    private Context context;
    private SearchView searchView;
    private MaterialButton buttonPay;

    private String previousParentId;

    SimpleDateFormat simpleDateFormatMD;
    SimpleDateFormat simpleDateFormatShift;
    TimeZone timeZoneMD;
    TimerTask timerTaskSearchText;
    Timer timerSearch;


    public static boolean isViewWithCatalog = true;
    public static AssortmentRealm assortmentClicked = null;
    View root;
    int currentColumns;

    String billId;
    private boolean shiftOpenButtonPay;
    private boolean shiftClosedButtonPay = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        salesViewModel = new ViewModelProvider(this).get(SalesViewModel.class);

        root = inflater.inflate(R.layout.fragment_sales, container, false);
        context = getContext();

        gridViewProducts = root.findViewById(R.id.gridProducts);
        scanCode = root.findViewById(R.id.imageScanBarcode);
        changeColumns = root.findViewById(R.id.imageChangeGridColumns);
        searchView = root.findViewById(R.id.searchProducts);
        buttonPay = root.findViewById(R.id.mtrbtn_pay_cart);

        simpleDateFormatMD = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        timeZoneMD = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormatMD.setTimeZone(timeZoneMD);

        if(SPFHelp.getInstance().getBoolean("ViewWithCatalog", false)){
            changeColumns.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid_black_24dp));
            isViewWithCatalog = true;
            gridViewProducts.setNumColumns(3);
        }
        else{
            changeColumns.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_black_24dp));
            isViewWithCatalog = false;
            gridViewProducts.setNumColumns(1);
        }

        showAssortment();

        detector = new GestureDetectorCompat(context, new MyGestureListener());

        salesViewModel.getAssortment().observe(getViewLifecycleOwner(), assortmentRealms -> {
            adapter = new AssortmentListGridAdapter(context, R.layout.item_grid_one_columns, assortmentRealms);
            gridViewProducts.setAdapter(adapter);

            adapter.setAssortmentItemActionListener(imageView -> {
                if (imageView != null && !shiftOpenButtonPay)
                    makeFlyAnimation(imageView);
                else
                    Toast.makeText(context, "Shift is not valid!", Toast.LENGTH_SHORT).show();
            });
        });

        salesViewModel.getShift().observe(getViewLifecycleOwner(), shift -> {
            if(shift == null){
                shiftOpenButtonPay = true;
                buttonPay.setText(R.string.text_open_shift);
                buttonPay.setBackgroundColor(context.getColor(R.color.btnPay));
                buttonPay.setTextColor(Color.WHITE);
            }
            else{
                if(!shift.isClosed() && new Date().getTime() > shift.getNeedClose() && shift.getNeedClose() != 0){
                    shiftClosedButtonPay = true;
                    new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                            .setTitle(R.string.message_attention)
                            .setMessage(R.string.message_shift_expired_want_close)
                            .setCancelable(false)
                            .setPositiveButton(R.string.btn_yes, (dialogInterface, i) -> {
//                                closeShift();
                            })
                            .setNegativeButton(R.string.btn_no,((dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            }))
                            .show();
                }
                else
                    shiftOpenButtonPay = false;
            }
        });

        gridViewProducts.setOnItemClickListener((parent, view, position, id) -> {
            AssortmentRealm assortmentRealm = adapter.getItem(position);
            previousParentId = assortmentRealm.getParentID();
            if (assortmentRealm.isFolder()) salesViewModel.findAssortment(assortmentRealm.getId());
            else addToCart(assortmentRealm);
        });

        gridViewProducts.setOnItemLongClickListener((parent, view, position, id) -> {
            Dialog tedsxt = new Dialog(context);
            Log.e("TAG", "onCreateView: " + adapter.getItem(position).getName() );
            return true;
        });

        gridViewProducts.setOnTouchListener((v, event) -> {
            detector.onTouchEvent(event);
            return false;
        });

        scanCode.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent scanBarcodes = new Intent(context, ScanActivity.class);
                scanBarcodes.putExtra("billId",billId);
                startActivityForResult(scanBarcodes,303);
            }else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 201);
            }
        });

        changeColumns.setOnClickListener(v -> {
            currentColumns = gridViewProducts.getNumColumns();
            if(currentColumns > 1) {
                SPFHelp.getInstance().putBoolean("ViewWithCatalog", false);
                changeColumns.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_black_24dp));
                isViewWithCatalog = false;
                gridViewProducts.setNumColumns(1);
            }else {
                SPFHelp.getInstance().putBoolean("ViewWithCatalog", true);
                changeColumns.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid_black_24dp));
                isViewWithCatalog = true;
                gridViewProducts.setNumColumns(3);
            }
            adapter.notifyDataSetChanged();
        });

        EditText search = searchView.findViewById(R.id.search_input_text);
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0){
                    showAssortment();
                }
                else if(s.length() > 3){
                    if (timerSearch != null)
                        timerSearch.cancel();
                    timerSearch = new Timer();

                    startTimerSearchText(s.toString());
                    timerSearch.schedule(timerTaskSearchText, 600);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        buttonPay.setOnClickListener(v -> {
            if(shiftOpenButtonPay){
                buttonPay.setEnabled(false);
                new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .setTitle(R.string.message_attention)
                        .setMessage(R.string.message_open_shift)
                        .setCancelable(false)
                        .setPositiveButton(R.string.btn_yes, (dialogInterface, i) -> {
                            long opened_new_shift = new Date().getTime();
                            long setShiftDuring= SPFHelp.getInstance().getLong("ShiftDuringSettings",14400000);
                            long need_close = opened_new_shift + setShiftDuring;

                            Shift shiftEntry = new Shift();
                            shiftEntry.setName("SHF " + simpleDateFormatMD.format(opened_new_shift));
                            shiftEntry.setWorkPlaceId(SPFHelp.getInstance().getString("WorkPlaceID", null));
                            shiftEntry.setWorkPlaceName(SPFHelp.getInstance().getString("WorkPlaceName", null));
                            shiftEntry.setAuthor(POSApplication.getApplication().getUserId());
                            shiftEntry.setAuthorName(POSApplication.getApplication().getUser().getFullName());
                            shiftEntry.setStartDate(new Date().getTime());
                            shiftEntry.setClosed(false);
                            shiftEntry.setNeedClose(need_close);
                            shiftEntry.setId(UUID.randomUUID().toString());

                            salesViewModel.updateShiftInfo(shiftEntry);

                            History history = new History();
                            history.setDate(new Date().getTime());
                            history.setMsg("Shift: " + shiftEntry.getName());
                            history.setType(BaseEnum.History_OpenShift);
                            salesViewModel.insertEntryLog(history);

                            POSApplication.getApplication().setShift(shiftEntry);
//                            startTimer(need_close - new Date().getTime());

                            shiftOpenButtonPay = false;
                            buttonPay.setEnabled(true);
                            buttonPay.setBackgroundColor(context.getColor(R.color.white));
                            buttonPay.setTextColor(context.getColor(R.color.colorPrimary));
                            buttonPay.setText("0 items = 0.00 MDL");
                        })
                        .setNegativeButton(R.string.btn_no,((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            buttonPay.setEnabled(true);
                        }))

                        .show();
            }
            else{

            }
        });

        return root;
    }

    private void startTimerSearchText(final String newText) {
        timerTaskSearchText = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        salesViewModel.searchProductsByText(newText);
                    }
                });
            }
        };
    }

    public static void showAssortment(){
        salesViewModel.findAssortment("00000000-0000-0000-0000-000000000000");
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

            float diffY = event2.getY() - event1.getY();
            float diffX = event2.getX() - event1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                }
            }
            return true;
        }
    }

    private void onSwipeLeft() {

    }

    private void onSwipeRight() {
        salesViewModel.findAssortment(previousParentId);
    }

    private void onSwipeTop() {

    }

    private void onSwipeBottom() {

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==303){
            if (resultCode == RESULT_OK){
                if (data!=null) {
                    String AllowedBalance = data.getStringExtra("AllowedBalance");
                    String CardNumber = data.getStringExtra("CardNumber");
                    String CustomerName = data.getStringExtra("CustomerName");
                    String DailyLimit = data.getStringExtra("DailyLimit");
                    String MonthlyLimit = data.getStringExtra("MonthlyLimit");
                    String TankCapacity = data.getStringExtra("TankCapacity");
                    String WeeklyLimit = data.getStringExtra("WeeklyLimit");
                    String AslName = data.getStringExtra("AslName");
                    String AslPrice = data.getStringExtra("AslPrice");


                    AlertDialog.Builder exit = new AlertDialog.Builder(context);
                    exit.setTitle("Informatie despre card:");
                    exit.setMessage(
                            "Clientul : "+ CustomerName + "\n" +
                                    "Cardul : "+ CardNumber + "\n" +
                                    "Balanta: "+ AllowedBalance + "\n" +
                                    "Capacitatea rezervorului: "+ TankCapacity + "\n" +
                                    "Limite: " + "\n" +
                                    "Luna: "+ MonthlyLimit + "\n" +
                                    "Saptamina: "+ WeeklyLimit + "\n" +
                                    "Zi: "+ DailyLimit + "\n" +
                                    "Asortiment: " + "\n" +
                                    "Denumirea: "+ AslName + "\n" +
                                    "Pretul: "+ AslPrice );
                    exit.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    exit.show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 201){
            if(permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startActivityForResult(new Intent(context, ScanActivity.class),303);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        salesViewModel.getShiftInfo();
    }

    private void addToCart(AssortmentRealm assortmentRealm) {
        Toast.makeText(context, assortmentRealm.getId() , Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    public static boolean isIsViewWithCatalog() {
        return isViewWithCatalog;
    }

    private void makeFlyAnimation(ImageView targetView) {

        MaterialButton destView = (MaterialButton) root.findViewById(R.id.mtrbtn_pay_cart);

        new CircleAnimationUtil().attachActivity(getActivity()).setTargetView(targetView).setMoveDuration(300).setDestView(destView).setAnimationListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //addItemToCart();
                if(assortmentClicked != null){
                    Toast.makeText(context, assortmentClicked.getName() + " added...", Toast.LENGTH_SHORT).show();
                    addToCart(assortmentClicked);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).startAnimation();
    }

    public static void setAssortmentClicked(AssortmentRealm item){
        assortmentClicked = item;
    }
}