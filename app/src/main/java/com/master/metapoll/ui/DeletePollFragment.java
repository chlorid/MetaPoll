package com.master.metapoll.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.master.metapoll.core.DataManager;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by ich on 09.10.15.
 */
public class DeletePollFragment extends MenuFragment {
    private static final String TAG = "DeletePollFragment";
    private DataManager dataManager;
    private boolean haveEntries = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataManager = MenuActivity.getDataManager();
    }

    @Override
    protected ArrayList<String> getList() {
        listItems = dataManager.getStoredPollList();

        Log.i(TAG,"We have " + listItems.size() + "polls");
//        pollList = EvalHandling.getStoredEvalNames(getActivity());
        if (listItems.size() < 1) {

            listItems.add("Sorry, No Polls found. Do you want to import a poll? Tab the text to get to the import menue.");
            haveEntries = false;
        }
        //Sorting
        Collections.sort(listItems);

        return listItems;
    }

    public static  String getMenuEntry() {
        return "delete Poll";
    }

    @Override
    public void onItemClicked(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {
            
            if (haveEntries) {
                dataManager.deletePoll(listItems.get(position));
                adapter.clear();
                adapter.addAll(getList());
                listView.invalidate();
            } else {
                Log.i(TAG, "open import");
//                listItems.clear();
//                listItems.add("Import");
                mListener.onFragmentInteractionItemClicked("com.master.metapoll.ui.ImportFragment");

            }
        }
    }
}
