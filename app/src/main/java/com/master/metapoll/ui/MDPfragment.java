package com.master.metapoll.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.master.metapoll.R;
import com.master.metapoll.mdo.MDpoll;

/**
 * This fragment shows one page of a poll.
 */
public class MDPfragment extends Fragment {
    private static final String TAG = "MDPfragment";
    /**
     * The poll, this page is from
     */
    private MDpoll MDpoll;
    /**
     * The page number of this fragment
     */
    private int pageNo;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param MDpoll
     * @return A new instance of fragment MDPfragment.
     */
    public static MDPfragment newInstance(MDpoll MDpoll, int pageNo) {
        MDPfragment fragment = new MDPfragment();
        fragment.setArguments(MDpoll,pageNo);
        return fragment;
    }

    /**
     * Called by the activity tp get the poll into the fragment. MDpoll is not parcelable.
     * @param MDpoll The poll we want to show a page of
     * @param pageNo The number of the page we show
     */
    public void setArguments(MDpoll MDpoll, int pageNo) {

        this.MDpoll = MDpoll;
        this.pageNo = pageNo;
    }

    private MDpoll getMDpoll() {
        return this.MDpoll;
    }
    private int getPageNo() {
        return this.pageNo;
    }

    public MDPfragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.MDpoll = getMDpoll();
            this.pageNo = getPageNo();


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView =inflater.inflate(R.layout.fragment_ui_container, container, false);

        LinearLayout l = (LinearLayout) rootView.findViewById(R.id.frag_ui_container);

        for (View v : MDpoll.getViews(pageNo)) {
            l.addView(v);
        }
        return rootView;
    }

}
