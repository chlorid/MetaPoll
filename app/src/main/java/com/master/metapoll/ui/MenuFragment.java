package com.master.metapoll.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.master.metapoll.R;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The main menu fragment. Submenus can be built by extending this
 * class and overwriting the getEntryList function.
 */
public class MenuFragment extends Fragment {
    protected ArrayList<String> listItems = null;
    private static final String TAG = "MenuFragment";
    HashMap<String,String> menuClasses;
    protected static ArrayAdapter<String> adapter;
    protected ListView listView;

    protected OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *.
     * @return A new instance of fragment GetEvalsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MenuFragment newInstance() {
        MenuFragment fragment = new MenuFragment();
        return fragment;
    }

    public MenuFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =inflater.inflate(R.layout.activity_main_menu, container, false);
        // get the ListView.
        listView = (ListView) rootView.findViewById(R.id.main_menu_list);
        // set it to the adapter
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1);

        // in case we don't get a list we just return the rootView.
        listItems = this.getEntryList();
        if (listItems == null) {
            Log.e(TAG, "no valid menu entries.");
            return rootView;
        }
        adapter.addAll(listItems);
        listView.setAdapter(adapter);
        //set callback
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onItemClicked(parent, view, position, id);
            }
        });
        return rootView;
    }

    /**
     * Callback Setup
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    public void onItemClicked(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {

//            mListener.onFragmentInteractionItemClicked(position);
            mListener.onFragmentInteractionItemClicked(menuClasses.get(listItems.get(position)));


        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteractionItemClicked(String listEntry);

    }

    /**
     * // This Fragment should not be shown in the main menu,
     * because it is the main menu itself.
     * @return always null.
     */
    public static  String getMenuEntry() {
        return null;
    }

    /**
     * Returns the list entries. Should be overwritten by subclasses.
     * @return List with menu entries.
     */
    protected ArrayList<String> getEntryList() {
        // All subclasse od MenuFragment found  are saved to a Map.
        // Key is the Menu Entry and Object is the classname.
        menuClasses = getMenuClasses();
        ArrayList<String> menuEntries = new ArrayList<>();
        // The class names are filled into the menu entry list.
        Iterator entries = menuClasses.entrySet().iterator();
        while (entries.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) entries.next();

                menuEntries.add((String) entry.getKey());
            }
            return menuEntries;
    }

    /**
     * Scans for Subclasses of MenuFragment.
     * @return
     */
    private HashMap<String,String> getMenuClasses() {
        final HashMap<String,String> classes = new HashMap<String,String>();
        try {
            new ClassScanner(getActivity()) {

                @Override
                protected boolean isTargetClassName(String className) {
                    return className.startsWith(getContext().getPackageName())//I want classes under my package
                            && !className.contains("$");//I don't need none-static inner classes
                }

                @Override
                protected boolean isTargetClass(Class clazz) {
                    return MenuFragment.class.isAssignableFrom(clazz)//I want subclasses of MenuFragment
                            && !Modifier.isAbstract(clazz.getModifiers());//I don't want abstract classes
                }

                @Override
                protected void onScanResult(Class clazz)  {
                    try {
                        Constructor constructor = clazz.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        String menuName = (String) clazz.getDeclaredMethod("getMenuEntry").invoke(null,null);
                        if (menuName != null) {
                            classes.put(menuName, clazz.getName());
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

            }.scan();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return classes;
    }
}


