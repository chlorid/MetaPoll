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

public class MDPactivity extends Activity {

    private static final String TAG = "MDPactivity";
    private MDpoll MDpoll;
    private int currentPage = 0;
    private Button btn_fwd;
    private Button btn_bck;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_container);
        context = this;
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
                if(currentPage == -1) {
                    currentPage =0;
                    Log.i(TAG,"reset current page");
                }
                if (currentPage < MDpoll.getPageCount()-1) {


                    MDPfragment cf = MDPfragment.newInstance(MDpoll, currentPage + 1);
                    f = cf;
                }
                else {

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

                if (currentPage == -1) {
                    onBackPressed();
                }
                MDPfragment f = MDPfragment.newInstance(MDpoll, currentPage - 1);
//                f.setArguments(MDpoll.getUiPage(currentPage));

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

            MDPfragment f = MDPfragment.newInstance(MDpoll, 0);
            f.setArguments(MDpoll,0);

            try {
                replaceFragment(f);
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            } catch (InstantiationException e1) {
                e1.printStackTrace();
            }
    }

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

    private void replaceFragment(Fragment fragment) throws IllegalAccessException, InstantiationException {

        // Create new fragment and transaction
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.ui_container, fragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    private Class loadClass(String mClassName) throws ClassNotFoundException {
        PathClassLoader classLoader = (PathClassLoader) this.getClassLoader();

        return classLoader.loadClass(mClassName);

    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        this.finish();


    }
}
