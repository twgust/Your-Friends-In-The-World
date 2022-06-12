package com.example.assignment1.View;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment1.R;
import com.example.assignment1.View.Adapters.GroupAdapter;
import com.example.assignment1.ViewModel.MemberData;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public class Fragment_Groups extends Fragment
        implements GroupAdapter.ViewHolder.OnGroupSelectedListener,
        GroupAdapter.ViewHolder.onJoinGroupClicked,
        GroupAdapter.ViewHolder.onRefreshGroupClicked
{

    public static final String TAG = "Fragment_Groups";

    public interface GROUPS_ON_JOIN_CLICK {
        void GROUPS_OnJoinClicked(String name);
    }
    public interface GROUPS_REFRESH{
        void GROUPS_refresh();
    }
    public interface GROUP_REFRESH{
        void GROUP_byName(String name);
    }

    private MaterialTextView textViewGroups;
    private GROUPS_ON_JOIN_CLICK joinlistener;
    private GROUPS_REFRESH refreshGroups;
    private GROUP_REFRESH refreshGroupByName;
    private MaterialButton refreshButton;

    private MemberData memberData;
    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.groupRecycler);
        textViewGroups = view.findViewById(R.id.onlineInGroup_FRAGMENT_GROUPS);
        refreshButton = view.findViewById(R.id.refresh);

        memberData = new ViewModelProvider(requireActivity()).get(MemberData.class);
        memberData.getGroups().observe(requireActivity(), groups -> {
            if (groups.isEmpty()){
                Log.d("FragmentChat", "onCreateView: no groups exist yet");
            }else{
                Log.d(TAG, "Fragment: observing Members ViewModel");
                GroupAdapter adapter = new GroupAdapter(groups,this,this, this);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
                textViewGroups.setText(groups.size() + " groups online");
            }
        });
        addButtonListener();
        return view;
    }


    @SuppressLint("SetTextI18n")
    private void addButtonListener(){
        refreshButton.setOnClickListener((view)->{
            refreshGroups.GROUPS_refresh();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            joinlistener = (GROUPS_ON_JOIN_CLICK) requireActivity();
            refreshGroups = (GROUPS_REFRESH) requireActivity();
            refreshGroupByName = (GROUP_REFRESH) requireActivity();

        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity() + " must implement GroupSelectedListener");
        }
    }

    @Override
    public void joinGroupClicked(String groupName) {

        System.out.println(groupName);
        joinlistener.GROUPS_OnJoinClicked(groupName);
    }

    @Override
    public void onGroupClicked(int pos) {
        // if index of the selected element in adapter is needed
    }

    @Override
    public void refreshGroupClicked(String name) {
        Toast.makeText(requireActivity(), name + " refreshed", Toast.LENGTH_SHORT).show();
        refreshGroupByName.GROUP_byName(name);
    }
}