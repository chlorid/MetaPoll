package com.master.metapoll.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.master.metapoll.R;
import com.master.metapoll.mdo.MDpoll;

import dalvik.system.PathClassLoader;

/**
 * This Activity shows the polls to the user.
 * It loads the path with the poll from the intent and creates an MDPoll object.
 * It does the naviagtion between the sites and replaces the fragments with the ones needed
 * by the user.
 */
public class MDPactivity extends Activity {

    private static final String TAG = "MDPactivity";
    private MDpoll MDpoll;
    private int currentPage = 0;
    private Button btn_fwd;
    private Button btn_bck;
    private Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_container);
        activity = this;
        Intent intent = getIntent();
        String mFilename = intent.getStringExtra("mEvalPath");


        if (mFilename == null) {
            Log.e(TAG,"Error: MDPactivity did not get a valid filepath");
            return;
        }

        MDpoll = new MDpoll(this,mFilename);
        // Init Forward and Back Button

        btn_fwd = (Button) findViewById(R.id.btn_fwd);
        btn_bck = (Button) findViewById(R.id.btn_back);
        setBtnState();

        btn_fwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MDpoll.savePage(currentPage);

                Log.i(TAG,"**current page: " + currentPage);

                Fragment f;
//                if(currentPage == -1) {
//                    currentPage =0;
//                    Log.i(TAG,"reset current page");
//                }
                // show next page.
                if (currentPage < MDpoll.getPageCount()-1) {
                    MDPfragment cf = MDPfragment.newInstance(MDpoll, currentPage + 1);
                    f = cf;
                }
                else {
                    // It's the last page. Sho the info fragment and edit the buttons.
                    long rowId = MDpoll.db_save();
                    boolean success = false;
                    if (rowId > 0) {
                        success = true;
                    }
                    long t = MDpoll.getDatasetCount();
                    Log.i(TAG, "db row count: " + t );
                    f = MDPinfoFragment.newInstance(success, rowId, MDpoll.getDatasetCount(), MDpoll.getMinQty());
                    currentPage = -1;
                    btn_fwd.setText("again");

                }

                try {
                    replaceFragment(f);
                    currentPage++;
                    setBtnState();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }

            }
        });

        btn_bck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // last page and back pressed means close the activity.
                if (currentPage == -1) {
                    activity.finish();
                }
                MDPfragment f = MDPfragment.newInstance(MDpoll, currentPage - 1);
                try {
                    replaceFragment(f);
                    currentPage--;
                    setBtnState();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        });
            // Create first fragment. Page 0
            MDPfragment f = MDPfragment.newInstance(MDpoll, 0);
            f.setArguments(MDpoll,0);

            try {
                // set the fragment
                replaceFragment(f);
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            } catch (InstantiationException e1) {
                e1.printStackTrace();
            }
    }

    /**
     * Keeps control over the buttons.
     */
    private void setBtnState() {
        if (currentPage <= 0 || currentPage >= MDpoll.getPageCount()) {
            btn_bck.setVisibility(View.INVISIBLE);
        }
        else {
            btn_bck.setVisibility(View.VISIBLE);
        }
        if (currentPage >= MDpoll.getPageCount()-1) {
            //TODO: put inr res
            btn_fwd.setText("save");
        }
        else {
            //TODO: put inr res
            btn_fwd.setText("Next");
        }
    }
    // Replace the fragment with the one the user wants to see.
    private void replaceFragment(Fragment fragment) throws IllegalAccessException, InstantiationException {

        // Create new fragment and transaction
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.ui_container, fragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }
}
