package md.intelectsoft.quickpos.phoneMode.ui.orders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import md.intelectsoft.quickpos.Realm.localStorage.Bill;

public class OrdersViewModel extends ViewModel {
    private final Realm mRealm;
    private MutableLiveData<List<Bill>> billList;

    public OrdersViewModel() {
        mRealm = Realm.getDefaultInstance();
        billList = new MutableLiveData<>();
    }

    public LiveData<List<Bill>> getBills() {
        return billList;
    }

    public void getBillList(String shiftId){
        RealmResults<Bill> results = mRealm.where(Bill.class).equalTo("shiftId", shiftId).and().equalTo("isDeleted", false).findAll();
        if(results !=null && !results.isEmpty()){
            RealmList<Bill> list = new RealmList<>();
            list.addAll(results);
            billList.setValue(mRealm.copyFromRealm(list));
        }
    }

    public boolean deleteBill(Bill item){
        if(item.getState() == 0){
            mRealm.beginTransaction();
            Bill bill = mRealm.where(Bill.class).equalTo("id", item.getId()).findFirst();
            if(bill != null)
                bill.setDeleted(true);
            mRealm.commitTransaction();
            return true;
        }
        else
            return false;
    }
}