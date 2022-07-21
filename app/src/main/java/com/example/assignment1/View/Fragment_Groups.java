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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment1.R;
import com.example.assignment1.View.Adapters.GroupAdapter;
import com.example.assignment1.ViewModel.MemberData;
import com.example.assignment1.ViewModel.UserData;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public class Fragment_Groups extends Fragment implements
        GroupAdapter.ViewHolder.joinGroupListener,
        GroupAdapter.ViewHolder.leaveGroupListener,
        GroupAdapter.ViewHolder.refreshGroupListener,
        GroupAdapter.ViewHolder.groupViewListener
        {

    public static final String TAG = "Fragment_Groups";


     public interface GROUPS_JOIN {
        void GROUPS_onJoinClicked(String name);
    }
    public interface GROUPS_LEAVE{
         void GROUPS_onLeaveClicked(String name);
     }

    public interface GROUPS_REFRESH{
        void GROUPS_onRefreshClicked();
    }
    public interface GROUP_REFRESH{
        void GROUPS_onGroupViewCLicked(String name);
    }



    private MaterialButton refreshButton;
    private MaterialTextView textViewGroups;
    private MemberData memberData;
    private UserData userData;

    private GROUPS_JOIN joinListener;
    private GROUPS_LEAVE leaveListener;
    private GROUPS_REFRESH refreshGroupsListener;
    private GROUP_REFRESH refreshGroupByNameListener;



    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        textViewGroups = view.findViewById(R.id.onlineInGroup_FRAGMENT_GROUPS);
        refreshButton = view.findViewById(R.id.refresh);

        userData = new ViewModelProvider(requireActivity()).get(UserData.class);
        memberData = new ViewModelProvider(requireActivity()).get(MemberData.class);

        getAllGroups_VIEWMODEL(view);
        updateMembersForGroup_VIEWMODEL();
        initializeButtonListeners();
        return view;
    }

    private void getUserGroups_VIEWMODEL(String groupname){
        userData.getUserGroups().observe(requireActivity(), groups ->{
            if (groups.isEmpty()){
                System.out.println("EMPTY @USERDATA-GROUPS");
            }
            for (String group : groups) {
                if(group.equals(groupname)){
                    Toast.makeText(requireActivity(),
                            "You're already registered to the selected group",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void updateMembersForGroup_VIEWMODEL(){
        memberData.getMembersForGroup().observe(requireActivity(), group ->{
            Toast.makeText(requireActivity(),
                    group.getMembersCount() + " currently online in " + group.getName(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @SuppressLint("SetTextI18n")
    private void getAllGroups_VIEWMODEL(View view){
        RecyclerView recyclerView = view.findViewById(R.id.groupRecycler);
        memberData.getGroups().observe(requireActivity(), groups -> {
            if (groups.isEmpty()){
                Log.d("FragmentChat", "onCreateView: no groups exist yet");
            }else{
                Log.d(TAG, "Fragment: observing Members ViewModel");

                GroupAdapter adapter = new GroupAdapter(groups,
                        this,this,
                        this, this);

                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
                if(groups.size() == 1){
                    textViewGroups.setText(groups.size() + " group registered");
                }
                else{
                    textViewGroups.setText(groups.size() + " groups registered");
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initializeButtonListeners(){
        refreshButton.setOnClickListener((view)->{
            refreshGroupsListener.GROUPS_onRefreshClicked();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            joinListener = (GROUPS_JOIN) requireActivity();
            leaveListener = (GROUPS_LEAVE) requireActivity();
            refreshGroupsListener = (GROUPS_REFRESH) requireActivity();
            refreshGroupByNameListener = (GROUP_REFRESH) requireActivity();

        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity() + " must implement GroupSelectedListener");
        }
    }
    //TODO SOLVE THIS
    @Override
    public void joinGroupClicked(String groupName) {
        Log.d(TAG, "joinGroupClicked: !");
        joinListener.GROUPS_onJoinClicked(groupName);
        getUserGroups_VIEWMODEL(groupName);
    }

    @Override
    public void leaveGroupClicked(String groupName) {
        Log.d(TAG, "leaveGroupClicked: !");
        leaveListener.GROUPS_onLeaveClicked(groupName);
    }

    @Override
    public void refreshGroupClicked(String name) {
        Log.d(TAG, "refreshGroupClicked: !");
        refreshGroupByNameListener.GROUPS_onGroupViewCLicked(name);
    }

    @Override
    public void onGroupClicked(int pos) {
        // if index of the selected element in adapter is needed
    }
}