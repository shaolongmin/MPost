package com.zoneol.mpost.fragment;

import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.zoneol.mpost.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link SettingAppDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingAppDialogFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    // TODO: Rename and change types of parameters
    private int mType;
    private int mPosition;
    private String mString ;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingAppDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingAppDialogFragment newInstance(int param1, int param2 , String param3) {
        SettingAppDialogFragment fragment = new SettingAppDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        args.putInt(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3 ,param3 );
        fragment.setArguments(args);
        return fragment;
    }

    public SettingAppDialogFragment() {
        // Required empty public constructor
    }

    public interface AppToastListener
    {
        void onAppToastListener(int position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(ARG_PARAM1);
            mPosition = getArguments().getInt(ARG_PARAM2);
            mString = getArguments().getString(ARG_PARAM3) ;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = null ;
        if (mType == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setMessage("确认删除该项？")
                    .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                            AppToastListener appToastListener = (AppToastListener)getActivity() ;
                            appToastListener.onAppToastListener(mPosition);
                        }
                    })
                    .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    }) ;
            dialog = builder.create();
        } else if (mType == 1) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_setting_app_progress, null);
            TextView textView = (TextView)view.findViewById(R.id.app_progress_content) ;
            textView.setText("正在处理中...");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setView(view);
            dialog = builder.create();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setMessage(mString);
            dialog = builder.create();
        }
        return dialog;
    }
}
