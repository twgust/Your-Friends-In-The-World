package com.example.assignment1.View;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment1.R;
import com.example.assignment1.View.Adapters.MessagesAdapter;
import com.example.assignment1.ViewModel.TextMessages;
import com.example.assignment1.ViewModel.UserData;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class Fragment_Chat extends Fragment {
    public static final String TAG = "Fragment_Chat";

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private MaterialButton sendButton;
    private TextInputEditText textInputEditText;
    private UserData userData;
    private SendMessageListener listener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.messageRecyclerView);
        userData = new ViewModelProvider(requireActivity()).get(UserData.class);

        addSendButtonListener(view);

        TextMessages messages = new ViewModelProvider(requireActivity()).get(TextMessages.class);
        messages.getMessages().observe(getViewLifecycleOwner(), message ->{
            if(message.isEmpty()){Log.d("Fragment_Chat", "onCreateView: no messages to load in adapter");}

            MessagesAdapter adapter = new MessagesAdapter(message);
            recyclerView.setAdapter(adapter);
            setupLayoutManager();
        });
        return view;
    }
    private void setupLayoutManager(){
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
    }

    public interface SendMessageListener{
        void onSendMessageButtonClicked(String userID, String textMessage);
        void onSendImageButtonClicked(String userID, String textMessage, String longitude, String latitude);
    }

    private void addSendButtonListener(View view){
        textInputEditText = view.findViewById(R.id.textInputEditText_chatFragment);
        sendButton = view.findViewById(R.id.materialButton_chatFragment);
        sendButton.setOnClickListener((view1 -> {
            try{
                String message = Objects.requireNonNull(textInputEditText.getText()).toString();
                String userID = Objects.requireNonNull(userData.getUserID().getValue());
                listener.onSendMessageButtonClicked(userID, message);
                //TODO solve hardcoding
                //listener.onSendImageButtonClicked(userID, message,  "12.979154", "55.613393");
            }catch (Exception e){
                e.printStackTrace();
            }
        }));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (Fragment_Chat.SendMessageListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity() +
                    " must implement SendMessageListener");
        }
    }
}
