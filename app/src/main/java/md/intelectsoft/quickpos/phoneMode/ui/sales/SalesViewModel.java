package md.intelectsoft.quickpos.phoneMode.ui.sales;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.realm.Case;
import io.realm.MutableRealmInteger;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import md.intelectsoft.quickpos.FiscalService.Body.BillLineFiscalService;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.Promotion;
import md.intelectsoft.quickpos.Realm.localStorage.AssortmentRealm;
import md.intelectsoft.quickpos.Realm.localStorage.Bill;
import md.intelectsoft.quickpos.Realm.localStorage.BillString;
import md.intelectsoft.quickpos.Realm.localStorage.History;
import md.intelectsoft.quickpos.Realm.localStorage.Shift;
import md.intelectsoft.quickpos.utils.BaseEnum;
import md.intelectsoft.quickpos.utils.POSApplication;

import static android.content.Context.MODE_PRIVATE;
import static md.intelectsoft.quickpos.utils.POSApplication.SharedPrefSettings;

public class SalesViewModel extends ViewModel {
    private final Realm mRealm;
    private final MutableLiveData<Shift> shift;
    private final MutableLiveData<List<AssortmentRealm>> listProducts;
    private MutableLiveData<Bill> billEntry;

    public SalesViewModel() {
        mRealm = Realm.getDefaultInstance();
        listProducts = new MutableLiveData<>();
        shift = new MutableLiveData<>();
        billEntry = new MutableLiveData<>();
    }

    public LiveData<List<AssortmentRealm>> getAssortment(){
        return listProducts;
    }
    public LiveData<Shift> getShift(){
        return shift;
    }
    public LiveData<Bill> getBillEntry (){ return billEntry;
    }

    public void findAssortment(String parentId){
        RealmResults<AssortmentRealm> listOfAssortment = mRealm.where(AssortmentRealm.class).equalTo("parentID", parentId).findAll();
        if(listOfAssortment != null) {
            listOfAssortment = listOfAssortment.sort("name", Sort.ASCENDING);
            listOfAssortment = listOfAssortment.sort("isFolder", Sort.DESCENDING);
            listProducts.setValue(mRealm.copyFromRealm(listOfAssortment));
        }
    }

    public void searchProductsByText(String searchText) {
        RealmResults<AssortmentRealm> result = mRealm.where(AssortmentRealm.class)
                .like("name",searchText, Case.INSENSITIVE).or()
                .like("marking",searchText, Case.INSENSITIVE).or()
                .like("code",searchText, Case.INSENSITIVE).or()
                .like("barcodes.bar",searchText, Case.INSENSITIVE).and()
                .equalTo("isFolder", false)
                .findAll();
        listProducts.setValue(mRealm.copyFromRealm(result));
    }

    public void getShiftInfo(){
        Shift lastOpenedShift = mRealm.where(Shift.class).equalTo("closed", false).findFirst();
        if(lastOpenedShift != null ) shift.setValue(mRealm.copyFromRealm(lastOpenedShift));
        else shift.setValue(null);
    }

    public int updateShiftInfo(Shift info,boolean toClose){
        int result = 0;
        if(toClose){
            RealmResults<Bill> billEntryResult = mRealm.where(Bill.class)
                    .equalTo("shiftId", info.getId())
                    .and()
                    .equalTo("state",0)
                    .and()
                    .equalTo("isDeleted", false)
                    .findAll();

            if(!billEntryResult.isEmpty()){
                result =  billEntryResult.size();
            }
            else{
                mRealm.beginTransaction();
                Shift shift1 = mRealm.where(Shift.class).equalTo("id", info.getId()).findFirst();
                if(shift1 != null){
                    long close = new Date().getTime();
                    shift1.setClosedBy(POSApplication.getApplication().getUserId());
                    shift1.setEndDate(close);
                    shift1.setClosed(true);
                    shift1.setClosedByName(POSApplication.getApplication().getUser().getFullName());
                    shift1.setSended(false);
                    shift.setValue(info);
                }
                mRealm.commitTransaction();
            }
        }
        else {
            shift.setValue(info);
            mRealm.executeTransaction(realm -> realm.insert(info));
        }
       return result;
    }

    public void insertEntryLog(History history){
        mRealm.executeTransaction(realm -> realm.insert(history));
    }

    public void addProductToBill(AssortmentRealm item, double quantity) {
        Bill bill = billEntry.getValue();
        if(bill == null){
            String id = UUID.randomUUID().toString();
            if(!createBill(id)){
                id = null;
            }
            else{
                bill = billEntry.getValue();
            }
            billEntry.setValue(bill);
        }
        if(bill != null){
            RealmList<BillString> bilString = bill.getBillStrings();
            if(bilString != null  && bilString.size() > 0){
                BillString lines = bilString.last();

                //TODO add check if item is nonIntegerSales

                if (lines.getAssortmentId().equals(item.getId()) && !item.isAllowNonInteger()){
                    double sumBefore = lines.getSum();
                    double sumWithDiscBefore = lines.getSumWithDiscount();
                    double quantityLine = lines.getQuantity() + quantity;
                    double sum = lines.getBasePrice() * quantityLine;
                    double sumWithDisc = lines.getPriceWithDiscount() * quantityLine;
                    String idLine = lines.getId();

                    mRealm.beginTransaction();
                        BillString string = mRealm.where(BillString.class).equalTo("id", idLine).findFirst();
                        if(string != null){
                            string.setQuantity(quantityLine);
                            string.setSum(sum);
                            string.setSumWithDiscount(sumWithDisc);
                        }

                        Bill billEntryRealmResults = mRealm.where(Bill.class).equalTo("id", bill.getId()).findFirst();
                        if (billEntryRealmResults != null) {
                            billEntryRealmResults.setTotalSum(billEntryRealmResults.getTotalSum() + (sum - sumBefore));
                            billEntryRealmResults.setTotalDiscount(billEntryRealmResults.getTotalDiscount() + (sumWithDisc - sumWithDiscBefore));
                            billEntry.setValue(mRealm.copyFromRealm(billEntryRealmResults));
                        }
                }
                else{
                    BillString billString = addItemToBillString(item);

                    double finalPriceWithDisc = billString.getSumWithDiscount();
                    mRealm.beginTransaction();
                        Bill billEntryRealmResults = mRealm.where(Bill.class).equalTo("id", bill.getId()).findFirst();
                        if (billEntryRealmResults != null) {
                            billEntryRealmResults.setTotalSum(billEntryRealmResults.getTotalSum() + item.getBasePrice());
                            billEntryRealmResults.setTotalDiscount(billEntryRealmResults.getTotalDiscount() + finalPriceWithDisc);
                            billEntryRealmResults.getBillStrings().add(billString);
                            billEntry.setValue(mRealm.copyFromRealm(billEntryRealmResults));
                        }
                }
                mRealm.commitTransaction();
            }
            else{
                BillString billString = addItemToBillString(item);

                double finalPriceWithDisc = billString.getSumWithDiscount();
                mRealm.beginTransaction();
                Bill billEntryRealmResults = mRealm.where(Bill.class).equalTo("id", bill.getId()).findFirst();
                if (billEntryRealmResults != null) {
                    billEntryRealmResults.setTotalSum(billEntryRealmResults.getTotalSum() + item.getBasePrice());
                    billEntryRealmResults.setTotalDiscount(billEntryRealmResults.getTotalDiscount() + finalPriceWithDisc);
                    billEntryRealmResults.getBillStrings().add(billString);
                    billEntry.setValue(mRealm.copyFromRealm(billEntryRealmResults));
                }
                mRealm.commitTransaction();
            }
        }

    }

    private BillString addItemToBillString(AssortmentRealm item){
        BillString billString = new BillString();
        double priceWithDisc = item.getBasePrice();

        CheckedAssortmentItemToPromo assortmentItemToPromo = checkedAssortmentItemToPromo(item);

        if(assortmentItemToPromo != null){
            billString.setPromoLineID(assortmentItemToPromo.getPromoId());
            priceWithDisc = assortmentItemToPromo.getPromoPrice();
        }

        billString.setUserId(POSApplication.getApplication().getUser().getId());
        billString.setAssortmentId(item.getId());
        billString.setAssortmentFullName(item.getName());
        billString.setBillID(Objects.requireNonNull(billEntry.getValue()).getId());
        billString.setId(UUID.randomUUID().toString());
        billString.setQuantity(1);
        billString.setBasePrice(item.getBasePrice());
        billString.setPriceLineID(item.getPriceLineId());
        billString.setAllowNonInteger(item.isAllowNonInteger());
        billString.setAllowDiscounts(item.isAllowDiscounts());
        billString.setBarcode(item.getBarcodes().size() > 0 ? item.getBarcodes().get(0).getBar() : "");
        billString.setVatValue(item.getVat());
        billString.setCreateDate(new Date().getTime());
        billString.setDeleted(false);
        billString.setPriceWithDiscount(priceWithDisc);

        billString.setSum(item.getBasePrice());
        billString.setSumWithDiscount(priceWithDisc);

        return billString;
    }

    private boolean createBill(String id) {
        boolean billCreated = false;

        Bill newBill = new Bill();
        newBill.setId(id);
        newBill.setCreateDate(new Date().getTime());
        newBill.setUserId(POSApplication.getApplication().getUser().getId());
        newBill.setUserName(POSApplication.getApplication().getUser().getFullName());
        newBill.setTotalDiscount(0.0);
        newBill.setTotalSum(0.0);
        newBill.setState(0);
        newBill.setShiftId(shift.getValue().getId());
        newBill.setShiftReceiptNumSoftware(shift.getValue().getBillCounter() + 1);
        newBill.setSynchronized(false);
        String version ="0.0";
        try {
            PackageInfo pInfo = POSApplication.getApplication().getPackageManager().getPackageInfo(POSApplication.getApplication().getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        newBill.setCurrentSoftwareVersion(version);
        newBill.setDeviceId(POSApplication.getApplication().getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("deviceId",null));

        mRealm.beginTransaction();
        Shift shift1 = mRealm.where(Shift.class).equalTo("id", shift.getValue().getId()).findFirst();
        if(shift1 != null){
            shift1.setBillCounter(shift1.getBillCounter() + 1);
            shift.setValue(mRealm.copyFromRealm(shift1));
        }
        mRealm.insert(newBill);

        History createdBill = new History();
        createdBill.setDate(newBill.getCreateDate());
        createdBill.setMsg(POSApplication.getApplication().getBaseContext().getString(R.string.message_bill_created_nr) + newBill.getShiftReceiptNumSoftware());
        createdBill.setType(BaseEnum.History_CreateBill);

        mRealm.insert(createdBill);

        billCreated = true;

        mRealm.commitTransaction();

        billEntry.setValue(newBill);


        return billCreated;
    }

    private static CheckedAssortmentItemToPromo checkedAssortmentItemToPromo(AssortmentRealm assortmentRealm){
        CheckedAssortmentItemToPromo promoItem = new CheckedAssortmentItemToPromo();
        Promotion promotion = null;

        if(!assortmentRealm.getPromotions().isEmpty()){
            promotion = assortmentRealm.getPromotions().first();

            long startDate = replaceDate(promotion.getStartDate());
            long endDate = replaceDate(promotion.getEndDate());
            Date curentDate = new Date();
            long currDate = curentDate.getTime();

            long timeBegin = 0;
            long timeEnd = 0;

            if(promotion.getTimeBegin() != null)    timeBegin = replaceDate(promotion.getTimeBegin());
            if(promotion.getTimeEnd() != null)    timeEnd = replaceDate(promotion.getTimeEnd());

            if(currDate > startDate && currDate < endDate){
                if(timeBegin != 0 && timeEnd != 0){
                    Date timeStart = new Date(timeBegin);
                    int hourS = timeStart.getHours();
                    int minS = timeStart.getMinutes();

                    Date timeFinis = new Date(timeEnd);
                    int hourE = timeFinis.getHours();
                    int minE = timeFinis.getMinutes();

                    Date one = new Date();
                    one.setHours(hourS);
                    one.setMinutes(minS);
                    one.setSeconds(0);

                    Date two = new Date();
                    two.setHours(hourE);
                    two.setMinutes(minE);
                    two.setSeconds(0);

                    if(hourE < hourS)
                        two.setDate(two.getDate() + 1);

                    if(curentDate.after(one) && curentDate.before(two)){
                        promoItem.setPromoId(promotion.getId());
                        promoItem.setPromoPrice(promotion.getPrice());
                        return promoItem;
                    }
                    else return null;
                }
                else{
                    promoItem.setPromoId(promotion.getId());
                    promoItem.setPromoPrice(promotion.getPrice());
                    return promoItem;
                }
            }
            else return null;
        }
        else return null;
    }

    private static class CheckedAssortmentItemToPromo{
        String promoId;
        double promoPrice;

        String getPromoId() {
            return promoId;
        }

        void setPromoId(String promoId) {
            this.promoId = promoId;
        }

        double getPromoPrice() {
            return promoPrice;
        }

        void setPromoPrice(double promoPrice) {
            this.promoPrice = promoPrice;
        }
    }
    public static long replaceDate(String date){
        if(date !=null ){
            date = date.replace("/Date(","");
            date = date.replace("+0200)/","");
            date = date.replace("+0300)/","");
            return Long.parseLong(date);
        }
        else
            return 0;

    }
}