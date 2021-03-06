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
import android.widget.ListView;

import com.popsecu.sdk.CfgInfo;
import com.popsecu.sdk.Controller;
import com.zoneol.mpost.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link SettingSelectDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingSelectDialogFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final int TYPE_PARENT_LONG_ONCLICK = 0 ;
    public static final int TYPE_PARENT_ADD = 1;

    // TODO: Rename and change types of parameters
    private int mType;
    private String mName;
    private String[] sList ={ "删除该组" , "添加子项"} ;
    public interface SelectListener
    {
        void onSelectListener(int type, int position , Object obj);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param type Parameter 1.
     * @param name Parameter 2.
     * @return A new instance of fragment SettingSelectDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingSelectDialogFragment newInstance(int type, String name) {
        SettingSelectDialogFragment fragment = new SettingSelectDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, type);
        args.putString(ARG_PARAM2, name);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingSelectDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(ARG_PARAM1);
            mName = getArguments().getString(ARG_PARAM2);
        }

        if (mType == TYPE_PARENT_LONG_ONCLICK) {

        } else if (mType == TYPE_PARENT_ADD) {
            addItem();
        }
    }

    public void addItem() {
        List<CfgInfo.CfgClassInfo> classList = Controller.getInstance().getTreeInfoImp().getClassInfoList() ;
        String[] tmpList = new String[classList.size()] ;
        for (int i = 0 ; i < classList.size() ; i ++) {
            tmpList[i] = classList.get(i).name ;
        }
        sList = tmpList ;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_setting_select_dialog, null);
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
                SelectListener selectListener = (SelectListener)getActivity() ;
                if (mType == TYPE_PARENT_LONG_ONCLICK) {
                    selectListener.onSelectListener(mType , position , mName);
                } else if(mType == TYPE_PARENT_ADD) {
                    Controller.getInstance().getTreeInfoImp().addClass(sList[position]);
                    selectListener.onSelectListener(mType , position , mName);
                }

            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(mName)
                .setView(view);
        AlertDialog dialog = builder.create();

        return dialog;
    }
}
