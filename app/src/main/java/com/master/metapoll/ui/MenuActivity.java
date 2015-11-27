package com.master.metapoll.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.master.metapoll.R;
import com.master.metapoll.core.DataManager;

import java.util.HashMap;

import dalvik.system.PathClassLoader;

public class MenuActivity extends Activity implements MenuFragment.OnFragmentInteractionListener {
    MenuFragment menuFragment;
Context mContext;
    static DataManager dataManager;
    ArrayAdapter<String> adapter;
    private static final String TAG = "MenuActivity";
    HashMap<String,String> menuClasses;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        mContext = this;
        dataManager = new DataManager(mContext);

        MenuFragment f = MenuFragment.newInstance();
        try {
            replaceFragment(f);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }



    private void replaceFragment(Class subMenuEntryClass) throws IllegalAccessException, InstantiationException {

        menuFragment = (MenuFragment) subMenuEntryClass.newInstance();
        FragmentManager fragmentManager = getFragmentManager();

        replaceFragment(menuFragment);
    }

    private void replaceFragment(Fragment fragment) throws IllegalAccessException, InstantiationException {

        // Create new fragment and transaction
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.sub_menu, fragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    private Class loadClass(String mClassName) throws ClassNotFoundException {
        PathClassLoader classLoader = (PathClassLoader) this.getClassLoader();

        return classLoader.loadClass(mClassName);

    }

    @Override
    public void onFragmentInteractionItemClicked(int position) {

//                            Intent launch = new Intent(mContext, SubMenuActivity.class);
//                    launch.putExtra("mClassName", menuClasses.get(adapter.getItem(position)));
//                    Log.i(TAG, "Got launch intent:" + launch.toString());
//                    startActivity(launch);
//        menuFragment.clickEvent(position);
    }

    @Override
    public void onFragmentInteractionItemClicked(String listEntry) {
        try {
            Class clazz = loadClass(listEntry);
            Log.i(TAG,"clicki: " + listEntry);

            replaceFragment((Fragment) clazz.newInstance());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        MenuFragment f = MenuFragment.newInstance();
        try {
            replaceFragment(f);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }

    public static DataManager getDataManager() {
        return dataManager;
    }
}





