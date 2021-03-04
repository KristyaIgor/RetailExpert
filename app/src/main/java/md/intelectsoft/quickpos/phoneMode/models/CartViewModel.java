package md.intelectsoft.quickpos.phoneMode.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import md.intelectsoft.quickpos.Realm.localStorage.Bill;
import md.intelectsoft.quickpos.Realm.localStorage.BillString;

public class CartViewModel  extends ViewModel {
    private final Realm mRealm;
    private MutableLiveData<List<BillString>> billString;
    private MutableLiveData<Bill> cartBill;

    public CartViewModel() {
        mRealm = Realm.getDefaultInstance();
        billString = new MutableLiveData<>();
        cartBill = new MutableLiveData<>();
    }

    public LiveData<List<BillString>> getBillStrings() {
        return billString;
    }

    public LiveData<Bill> getBillCart() {
        return cartBill;
    }

    public void getBillStringList(String billId){
        RealmResults<BillString> results = mRealm.where(BillString.class).equalTo("billID", billId).and().equalTo("isDeleted", false).findAll();
        if(results !=null && !results.isEmpty()){
            RealmList<BillString> list = new RealmList<>();
            list.addAll(results);

            billString.setValue(mRealm.copyFromRealm(list));
        }
    }

    public double[] getBillInfo(String billId){
        double[] retStatement = new double[2];
        Bill bill = mRealm.where(Bill.class).equalTo("id", billId).findFirst();
        if(bill != null){
            RealmList<BillString> lines = bill.getBillStrings();
            int countLines = 0;
            if(lines != null && lines.size() > 0) {
                for (BillString line : lines) {
                    if (line.isAllowNonInteger()) {
                        countLines += 1;
                    } else
                        countLines += line.getQuantity();
                }
            }
            double sum = bill.getTotalDiscount();
            retStatement[0] = countLines;
            retStatement[1] = sum;
        }
        return retStatement;
    }
}
