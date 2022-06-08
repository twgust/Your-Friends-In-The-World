package com.example.assignment1.View;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment1.R;
import com.example.assignment1.View.Adapters.GroupAdapter;
import com.example.assignment1.ViewModel.Users;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.Objects;

public class Fragment_LogIn extends Fragment
        implements GroupAdapter.ViewHolder.OnGroupSelectedListener{

    private MaterialButton button;
    private TextInputEditText textfieldUserName;
    private TextInputEditText textfieldgroupName;

    private LoginListener loginListener;
    private GroupSelectedListener groupListener;

    //TODO DEPRECATED
    private MaterialToolbar toolbar;

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        button =  view.findViewById(R.id.submitButton);
        textfieldgroupName = view.findViewById(R.id.groupEditText);
        textfieldgroupName.setText(R.string.placeholderGroup);
        textfieldUserName = view.findViewById(R.id.userEditText);
        recyclerView = view.findViewById(R.id.recyclerViewGroups);

        Users users = new ViewModelProvider(requireActivity()).get(Users.class);
        users.getGroups().observe(getViewLifecycleOwner(), groups -> {
            if (groups.isEmpty()){
                System.out.println("empty");
            }
            GroupAdapter adapter = new GroupAdapter(groups, this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        });

        addButtonListener();
        setupToolbar();
        return view;
    }

    // ---->
    public interface GroupSelectedListener {
        void selectedGroupListener(int pos);
    }
    @Override
    public void onGroupClicked(int pos) {
        groupListener.selectedGroupListener(pos);
        Log.d(TAG, "onGroupClicked: ");
    }
    // <----

    public interface LoginListener{
        void onLoginButtonClicked(String groupname, String username) throws IOException;
    }

    private void addButtonListener(){
        button.setOnClickListener(view1 -> {
            String groupname = Objects.requireNonNull(textfieldgroupName.getText()).toString();
            String username = Objects.requireNonNull(textfieldUserName.getText()).toString();
            if(!groupname.isEmpty() && !username.isEmpty()){
                try {
                    loginListener.onLoginButtonClicked(groupname,username);
                    textfieldgroupName.setText("");
                    textfieldUserName.setText(""); }
                catch (IOException e) { e.printStackTrace(); }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            loginListener = (LoginListener) getActivity();
            groupListener = (GroupSelectedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity() +
                    " must implement GroupSelectedListener");
        }
    }


    // TODO DEPRECATED
    private void setupToolbar(){
        /*
        toolbar = getActivity().findViewById(R.id.topAppBar);
        toolbar.setTitle("Your Friends in the world");
        toolbar.setSubtitle("assignment 2");
        toolbar.setTitleCentered(true);
        toolbar.setSubtitleCentered(true);
        */
    }
}


