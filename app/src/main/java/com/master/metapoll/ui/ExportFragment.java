package com.master.metapoll.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.master.metapoll.core.DataManager;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;


/**
 * Created by ich on 09.10.15.
 */
public class ExportFragment extends MenuFragment {
    private static final String TAG = "ExportFragment";
    private  ArrayList<String> evalList;
    private String menu = "choose";
    private String itemToexport;
    private DataManager dataManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataManager = MenuActivity.getDataManager();
    }

    @Override
    protected ArrayList<String> getList() {

        ArrayList<String> entries = new ArrayList<>();
        entries.add("Upload");
        entries.add("auf Dateisystem");

        return entries;
    }

    public static  String getMenuEntry() {
        return "Export";
    }

    @Override
    public void onItemClicked(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {
            if (menu.equals("filesystem"))  {
                itemToexport = adapter.getItem(position);
                // Start File chooser to choose directory.
                Intent i = new Intent(getActivity(), FilePickerActivity.class);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                startActivityForResult(i, FilePickerActivity.MODE_DIR);
            }
            else if (menu.equals("upload")) {
                dataManager.exportDbToWeb(adapter.getItem(position));
                getActivity().onBackPressed();
            }
            else if (menu.equals("choose")){
                adapter.clear();
                adapter.addAll(dataManager.getStoredDbList());
                }
                switch (position) {
                    case 0:
                        Log.i(TAG, "upload");

                        menu="upload";
//                        adapter.add();

                        break;
                    case 1:
                        Log.i(TAG, "auf Dateisystem");
                            menu = "filesystem";
                        break;

                    default:
                        Log.i(TAG, "Invalid entry. This should nt happen.");
                }
            }


    }


    // File Chooser result is received here.
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Fix no activity available
        if (data == null)
            return;
        switch (requestCode) {
            case FilePickerActivity.MODE_DIR:
                if (resultCode == Activity.RESULT_OK) {
                    String filePath = data.getData().getPath();
                    Log.i(TAG, "Path: " + filePath);
                    dataManager.exportDbToFs(filePath, itemToexport);
                    getActivity().onBackPressed();
                }
        }
    }
}
