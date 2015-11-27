package com.master.metapoll.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.master.metapoll.core.DataManager;
import com.master.metapoll.core.NWhelper;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ich on 09.10.15.
 */
public class ImportFragment extends MenuFragment {
    private static final String TAG = "ImportFragment";
    private String menu = "choose";
    private String itemToexport;
    HashMap<String,String> entryMap;
    private DataManager dataManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataManager = MenuActivity.getDataManager();
        new NWhelper.MetadataListDownloadTask().execute(getActivity());

    }

    @Override
    protected ArrayList<String> getList() {

        ArrayList<String> entries = new ArrayList<>();
        entries.add("Download");
        entries.add("von Dateisystem");

        return entries;
    }

    public static  String getMenuEntry() {
        return "Import";
    }

    @Override
    public void onItemClicked(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {
            if (menu.equals("filesystem")) {
                itemToexport = adapter.getItem(position);
                // Start File chooser to choose directory.
                Intent i = new Intent(getActivity(), FilePickerActivity.class);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                startActivityForResult(i, FilePickerActivity.MODE_FILE);
            } else if (menu.equals("download")) {
                Log.i(TAG, "It's a download ");
                    dataManager.downloadFile(adapter.getItem(position));
//                    getActivity().onBackPressed();
            } else if (menu.equals("choose")) {
                adapter.clear();
                for (String s : dataManager.getDlPollList()) {
                    Log.i(TAG,"dlpolllist entry:" + s);
                }
                adapter.addAll(dataManager.getDlPollList());

//                String[] dbNames = EvalHandling.getStoredDbNames(getActivity());
//                if (dbNames != null) {
//                    for (String s : dbNames) {
//                        adapter.add(s);
//                    }
//                }
                switch (position) {
                    case 0:
                        Log.i(TAG, "download");
                        menu = "download";
                        Log.i(TAG, "dowload");
//

                        break;
                    case 1:
                        Log.i(TAG, "auf Dateisystem");
                        // Start File chooser to choose directory.
                        // Start File chooser to choose directory.
//                        listView.setVisibility(View.INVISIBLE);
                        Intent i = new Intent(getActivity(), FilePickerActivity.class);
                        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                        startActivityForResult(i, FilePickerActivity.MODE_FILE);
                        menu = "filesystem";


                        break;

                    default:
                        Log.i(TAG, "Invalid entry. This should nt happen.");
                }
            }

        }
    }



    // File Chooser result is received here.
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Fix no activity available
//        listView.setVisibility(View.VISIBLE);
        if (data == null)
            return;
        switch (requestCode) {
            case FilePickerActivity.MODE_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    String filePath = data.getData().getPath();
                    Log.i(TAG, "Path: " + filePath);
                    //FilePath is your file as a string

                        File source = new File(filePath);
                        Log.i(TAG, "source name: " + source.getName() );

                        dataManager.importToFs(filePath);
//                        FSHelper.copyFile(source,new File(getActivity().getFilesDir() + "/" + source.getName()));
                }
        }
        getActivity().onBackPressed();
    }
//    public static void refreshFragment() {
//        HashMap<String,String> entryMap = XMLhelper.parseMetaList(context.getFilesDir().getAbsolutePath() + "/evallist.xml");
//        if (entryMap == null) {
//            Log.e(TAG,"error reading metaList. Stop.");
//                return;
//            }
//
//        Iterator entries = entryMap.entrySet().iterator();
//
//        adapter.clear();
//        while (entries.hasNext()) {
//            HashMap.Entry entry = (HashMap.Entry) entries.next();
//
//            adapter.add((String) entry.getKey());
//            Log.i(TAG, "Entry: " + entry.getKey() + " Classname: " + entry.getValue());
//        }
//    }


}
