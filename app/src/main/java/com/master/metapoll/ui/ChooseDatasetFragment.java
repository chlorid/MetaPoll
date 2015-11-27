package com.master.metapoll.ui;

import android.content.Intent;
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
public class ChooseDatasetFragment extends MenuFragment {
    private static final String TAG = "ChooseDatasetFragment";
    private  ArrayList<String> pollList;
    private DataManager dataManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataManager = MenuActivity.getDataManager();
    }

    @Override
    protected ArrayList<String> getList() {
        pollList = dataManager.getStoredPollList();
        Log.i(TAG,"We have " + pollList + "polls");
//        pollList = EvalHandling.getStoredEvalNames(getActivity());
        if (pollList == null) {
            pollList = new ArrayList<>();
            pollList.add("Sorry, No Evaluations found!!");
        }
        //Sorting
        Collections.sort(pollList);

        return pollList;
    }

    public static  String getMenuEntry() {
        return "Daten erheben";
    }

    public void onItemClicked(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {

//            mListener.onFragmentInteractionItemClicked(position);
//            mListener.onFragmentInteractionItemClicked(listItems.get(position));
            clickEvent(position);
        }
    }

    @Override
    public void clickEvent(int entry) {
        String path = dataManager.getPollPathByName(pollList.get(entry));
        Log.i(TAG, "launching with: " + path);
        Intent launch = new Intent(getActivity(), MDPactivity.class);
        launch.putExtra("mEvalPath", path);
        startActivity(launch);
    }

//    public static String getParentMenuEntry() {
//
//        return "MainMenu";
//    }
}
