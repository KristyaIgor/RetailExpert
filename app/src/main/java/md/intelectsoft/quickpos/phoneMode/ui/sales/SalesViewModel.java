package md.intelectsoft.quickpos.phoneMode.ui.sales;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import md.intelectsoft.quickpos.Realm.localStorage.AssortmentRealm;
import md.intelectsoft.quickpos.Realm.localStorage.History;
import md.intelectsoft.quickpos.Realm.localStorage.Shift;

public class SalesViewModel extends ViewModel {
    private Realm mRealm;
    private MutableLiveData<Shift> shift;
    private MutableLiveData<List<AssortmentRealm>> listProducts;

    public SalesViewModel() {
        mRealm = Realm.getDefaultInstance();
        listProducts = new MutableLiveData<>();
        shift = new MutableLiveData<>();
    }

    public LiveData<List<AssortmentRealm>> getAssortment(){
        return listProducts;
    }
    public LiveData<Shift> getShift(){
        return shift;
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
        if(lastOpenedShift != null ) {
            shift.setValue(mRealm.copyFromRealm(lastOpenedShift));
        }
        else
            shift.setValue(null);
    }

    public void updateShiftInfo(Shift info){
        shift.setValue(info);
        mRealm.executeTransaction(realm -> realm.insert(info));
    }

    public void insertEntryLog(History history){
        mRealm.executeTransaction(realm -> realm.insert(history));
    }
}