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
 *This Fragment shows informations about the current made poll.
 * The Number of datasets that are saved.
 * The number of polls that have to be done.
 */
public class MDPinfoFragment extends Fragment {
    private static final String POLL_SUCCESS = "poll_success";
    private static final String DB_ID = "db_id";
    private static final String DB_COUNT = "db_count";
    private static final String MIN_QTY = "min_qty";

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
     * @param count Number of already present datasets..
     * @param minQty minimal quantity of polls that have to be dne. If -1, means not set.
     * @return A new instance of fragment MDPinfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MDPinfoFragment newInstance(boolean success, long db_id, long count, int minQty) {
        MDPinfoFragment fragment = new MDPinfoFragment();
        Bundle args = new Bundle();
        args.putBoolean(POLL_SUCCESS, success);
        args.putLong(DB_ID, db_id);
        args.putLong(DB_COUNT, count);
        args.putInt(MIN_QTY, minQty);
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
            success = getArguments().getBoolean(POLL_SUCCESS);
            db_id = getArguments().getLong(DB_ID);
            count = getArguments().getLong(DB_COUNT);
            minQty = getArguments().getInt(MIN_QTY);
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
            successMessage.setText("Added Poll successfully to the database.");
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
}
