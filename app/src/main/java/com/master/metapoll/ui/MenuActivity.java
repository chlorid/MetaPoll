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
    /**
     * The Data Manager object for the ui.
     */
    static DataManager dataManager;
    private static final String TAG = "MenuActivity";
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


    /**
     * Replaces the fragment, which is shohn in the activity.
     * @param fragment Fragment that replaces the current one.
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
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

    /**
     * Callback of the fragment. Something in the fragment was clicked.
     * We replace the fragment with the one corresponding to the clicked menu entry.
     * @param className Name of the Menu class we rpelace the current fragment with.
     */
    @Override
    public void onFragmentInteractionItemClicked(String className) {
        try {
            PathClassLoader classLoader = (PathClassLoader) this.getClassLoader();
            Class clazz = classLoader.loadClass(className);
            replaceFragment((Fragment) clazz.newInstance());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Instead of kicking the user out of the app, we send him back to the main menu.
     * No matter where in the menu he is. MIght make sense to overwrite this in subclasses.
     */
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

    /**
     * Static function for the menu fragments to pick up the DataManager object.
     * @return
     */
    public static DataManager getDataManager() {
        return dataManager;
    }
}





