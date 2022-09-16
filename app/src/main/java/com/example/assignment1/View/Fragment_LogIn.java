package com.example.assignment1.View;

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
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.Objects;

public class Fragment_LogIn extends Fragment implements
        GroupAdapter.ViewHolder.joinGroupListener,
        GroupAdapter.ViewHolder.leaveGroupListener,
        GroupAdapter.ViewHolder.refreshGroupListener,
        GroupAdapter.ViewHolder.groupViewListener {
    public static final String TAG = "Fragment_LogIn";

    private MaterialButton button;
    private MaterialButton refreshButton;
    private TextInputEditText textFieldUserName;
    private TextInputEditText textFieldgroupName;

    private LoginListener loginListener;
    private GroupSelectedListener groupPosListener;
    private GroupNameListener groupNameListener;
    private Fragment_Groups.GROUPS_REFRESH groups_refresh;
    private Fragment_Groups.GROUP_REFRESH group_refresh;


    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        button =  view.findViewById(R.id.submitButton);
        refreshButton = view.findViewById(R.id.refreshButton);

        textFieldgroupName = view.findViewById(R.id.groupEditText);
        textFieldUserName = view.findViewById(R.id.userEditText);
        recyclerView = view.findViewById(R.id.recyclerViewGroups);

        MemberData memberData = new ViewModelProvider(requireActivity()).get(MemberData.class);
        memberData.getGroups().observe(getViewLifecycleOwner(), groups -> {
            if (groups.isEmpty()){
                Log.d("Fragment_LogIn", "onCreateView: no groups exist yet");
            }
            GroupAdapter adapter = new GroupAdapter(groups, this, this, this, this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        });

        addButtonListener();
        return view;
    }

    @Override
    public void refreshGroupClicked(String name) {
        group_refresh.GROUP_onRefreshAllGroupsClicked(name);
    }

    @Override
    public void leaveGroupClicked(String groupName) {

    }


    public interface GroupNameListener {
        void LOGIN_onGroupClicked(String name);
    }
    @Override
    public void joinGroupClicked(String groupName) {
        System.out.println(groupName);
        textFieldgroupName.setText(groupName);
        groupNameListener.LOGIN_onGroupClicked(groupName);
        Toast.makeText(requireActivity(), getResources().getString(R.string.placeholderGroup), Toast.LENGTH_LONG).show();
    }

    public interface GroupSelectedListener {
        void LOGIN_onGroupClicked(int pos);
    }


    @Override
    public void onGroupClicked(int pos) {
        System.out.println("WTF IS A " + pos);
        groupPosListener.LOGIN_onGroupClicked(pos);
        Log.d(TAG, "onGroupClicked: ");
    }
    // <----

    public interface LoginListener{
        void LOGIN_onLoginClicked(String groupname, String username) throws IOException;
    }

    private void addButtonListener(){
        button.setOnClickListener(view1 -> {
            String groupname = Objects.requireNonNull(textFieldgroupName.getText()).toString();
            String username = Objects.requireNonNull(textFieldUserName.getText()).toString();
            if(!groupname.isEmpty() && !username.isEmpty()){
                try { loginListener.LOGIN_onLoginClicked(groupname,username); }
                catch (IOException e) { e.printStackTrace(); }
            }
        });
        refreshButton.setOnClickListener(view ->{

            groups_refresh.GROUPS_onRefreshClicked();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            groupNameListener = (GroupNameListener) getActivity();
            loginListener = (LoginListener) getActivity();
            groupPosListener = (GroupSelectedListener) getActivity();
            groups_refresh = (Fragment_Groups.GROUPS_REFRESH)  getActivity();
            group_refresh = (Fragment_Groups.GROUP_REFRESH) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity() +
                    " must implement GroupSelectedListener");
        }
    }
}


