package md.intelectsoft.quickpos.phoneMode.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import md.intelectsoft.quickpos.Realm.localStorage.Shift;

public class ShiftViewModel extends ViewModel {
    private final Realm mRealm;
    private MutableLiveData<List<Shift>> shiftList;

    public ShiftViewModel() {
        mRealm = Realm.getDefaultInstance();
        shiftList = new MutableLiveData<>();
    }

    public LiveData<List<Shift>> getShift(){
        return shiftList;
    }

    public void getAllShifts (){
        RealmResults<Shift> listOfShift = mRealm.where(Shift.class).sort("closed", Sort.ASCENDING).findAll();
        if(listOfShift != null) {
            shiftList.setValue(mRealm.copyFromRealm(listOfShift));
        }
    }
}
