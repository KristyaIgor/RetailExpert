package md.intelectsoft.quickpos.tabledMode.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import md.intelectsoft.quickpos.tabledMode.dynamicTabs.ViewPagerDynamicTabs;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.Realm.localStorage.QuickGroupRealm;

/**
 * Created by Igor on 10.02.2020
 */

public class FragmentQuickButtons extends Fragment {

    Realm mRealm;
    TabLayout tab;
    ViewPager viewPager;
    ViewPagerDynamicTabs viewPagerAdapter;
    ArrayList<QuickGroupRealm> list;
    final int[] sizeGroup = {0};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_quick_groups, container, false);

        Log.d("QuickButtons","onCreateView");

        mRealm = Realm.getDefaultInstance();

        viewPager = rootViewAdmin.findViewById(R.id.viewpager_quick);
        tab = rootViewAdmin.findViewById(R.id.tab_groups);

        list = new ArrayList<>();

        mRealm.executeTransaction(realm -> {
            RealmResults<QuickGroupRealm> result = realm.where(QuickGroupRealm.class).findAll();
            if(!result.isEmpty()) {
                sizeGroup[0] = result.size();
                for (int i = 0; i < result.size(); i++){
                    QuickGroupRealm quickGroupRealm = realm.copyFromRealm(result.get(i));
                    list.add(quickGroupRealm);

                }
            }
        });
        viewPager.setAdapter(null);
        viewPagerAdapter = new ViewPagerDynamicTabs(getActivity().getSupportFragmentManager(), sizeGroup[0],list);

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(15);
        tab.setupWithViewPager(viewPager);

        return rootViewAdmin;
    }


}
