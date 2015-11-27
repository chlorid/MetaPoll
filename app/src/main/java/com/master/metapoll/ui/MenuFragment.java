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
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MenuFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
    protected ArrayList<String> listItems = null;
    private static final String TAG = "MenuFragment";
    HashMap<String,String> menuClasses;
    protected static ArrayAdapter<String> adapter;
    protected ListView listView;

    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

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
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    public MenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =inflater.inflate(R.layout.activity_main_menu, container, false);
        listView = (ListView) rootView.findViewById(R.id.main_menu_list);
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1);

        // in case we don't get a list we just return the rootView.
        listItems = this.getList();
        if (listItems == null) return rootView;

        for (String item :listItems) {
            adapter.add(item);
        }
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onItemClicked(parent, view, position, id);

            }
        });

        return rootView;
    }

    public void onItemClicked(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {

            mListener.onFragmentInteractionItemClicked(position);
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
        public void onFragmentInteractionItemClicked(int position);
        public void onFragmentInteractionItemClicked(String listEntry);

    }

    public static  String getMenuEntry() {
        return null;
    }
    public static String getParentMenuEntry() {

        return null;
    }

    protected ArrayList<String> getList() {

//        ArrayList<String> list = new ArrayList<String>();
//        list.add("this is a test");
//        list.add("this is another test");
//        return list;
        menuClasses = getMenuClasses();
        ArrayList<String> menuEntries = new ArrayList<>();
        Iterator entries = menuClasses.entrySet().iterator();
        while (entries.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) entries.next();

                menuEntries.add((String) entry.getKey());
                Log.i(TAG, "Entry: " + entry.getKey() + " Classname: " + entry.getValue());
            }
            Log.i(TAG, "******/found classes **********");
            return menuEntries;


    }
    public void clickEvent(int entry) {

    }

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


