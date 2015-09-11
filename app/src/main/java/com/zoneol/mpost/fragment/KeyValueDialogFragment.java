package com.zoneol.mpost.fragment;

import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.zoneol.mpost.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link KeyValueDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KeyValueDialogFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4" ;

    // TODO: Rename and change types of parameters
    private boolean mIsEditEbable;
    private int mPosition;
    private boolean mIsKey ;

    private ArrayList<String> sList = new ArrayList<>() ;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param position Parameter 2.
     * @return A new instance of fragment KeyValueDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KeyValueDialogFragment newInstance(boolean param1, int position , ArrayList<String> list , boolean isKey) {
        KeyValueDialogFragment fragment = new KeyValueDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, param1);
        args.putInt(ARG_PARAM2, position);
        args.putStringArrayList(ARG_PARAM3, list);
        args.putBoolean(ARG_PARAM4 , isKey);
        fragment.setArguments(args);
        return fragment;
    }

    public interface KeyValueListener
    {
        void onKeyValueListener(int position , String value , boolean isKey);
    }

    public KeyValueDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsEditEbable = getArguments().getBoolean(ARG_PARAM1);
            mPosition = getArguments().getInt(ARG_PARAM2);
            sList = getArguments().getStringArrayList(ARG_PARAM3) ;
            mIsKey = getArguments().getBoolean(ARG_PARAM4) ;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = null ;
        String title = "" ;
        if (mIsEditEbable) {
            title = "请输入Value值" ;
            view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_key_value_edit_dialog, null);
            final EditText edit = (EditText)view.findViewById(R.id.fragment_key_value_edit) ;
            view.findViewById(R.id.fragment_key_value_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            view.findViewById(R.id.fragment_key_value_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    String editValue = edit.getText().toString() ;
                    if (editValue.isEmpty()) {
                        return ;
                    }

                    KeyValueListener keyValueListener = (KeyValueListener)getActivity() ;
                    keyValueListener.onKeyValueListener(mPosition , editValue , mIsKey);
                }
            });
        } else {
            if (mIsKey) {
                title = "请选择Key值" ;
            } else {
                title = "请选择Value值" ;
            }
            view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_setting_select_dialog, null);
            ListView selectListView = (ListView)view.findViewById(R.id.setting_dialog_listview) ;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getActivity(),
                    android.R.layout.simple_expandable_list_item_1,
                    sList);
            selectListView.setAdapter(adapter);
            selectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dismiss();
                    KeyValueListener keyValueListener = (KeyValueListener)getActivity() ;
                    keyValueListener.onKeyValueListener(mPosition , sList.get(position) , mIsKey);
                }
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(view);
        AlertDialog dialog = builder.create();
        return dialog;
    }
}
