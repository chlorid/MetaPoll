package com.master.metapoll.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.master.metapoll.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MDPinfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MDPinfoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";

    // TODO: Rename and change types of parameters
    private boolean success;
    private long db_id;
    private long count;
    private int minQty;
        /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param success true shows a success message, flase an error message.
     * @param db_id ID in the database.
     * @param count Number of already finished evaluations.
     * @param minQty minimal quantity of evaluations that have to be dne. If -1, means not set.
     * @return A new instance of fragment MDPinfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MDPinfoFragment newInstance(boolean success, long db_id, long count, int minQty) {
        MDPinfoFragment fragment = new MDPinfoFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, success);
        args.putLong(ARG_PARAM2, db_id);
        args.putLong(ARG_PARAM3, count);
        args.putInt(ARG_PARAM4, minQty);
        fragment.setArguments(args);
        return fragment;
    }

    public MDPinfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            success = getArguments().getBoolean(ARG_PARAM1);
            db_id = getArguments().getLong(ARG_PARAM2);
            count = getArguments().getLong(ARG_PARAM3);
            minQty = getArguments().getInt(ARG_PARAM4);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_evaluation_end, container, false);

        TextView successMessage = (TextView) rootView.findViewById(R.id.success_message);
        TextView info = (TextView) rootView.findViewById(R.id.evaluation_information);

        if (success) {
            successMessage.setText("Added Evaluatin successfully to the database.");
        }
        else {
            successMessage.setText("Dataset could not be safed");
        }
        if (minQty > 0) {
            info.setText("Dataset has id " + db_id + ".\n You already have " + count + " of " + minQty + " Datasets.");
        }
        else {
            info.setText("Dataset has id " + db_id + ".\n You already have " + count + " Datasets.");
        }


        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
