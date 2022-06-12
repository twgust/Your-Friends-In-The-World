package com.example.assignment1.ViewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.assignment1.controller.entity.TextMessage;

import java.util.ArrayList;
import java.util.List;

public class TextMessages extends ViewModel {
    private static final String TAG = "ViewModel:textmessages";

    private MutableLiveData<String> textmessage;
    private MutableLiveData<Integer> textMessageType;

    private ArrayList<TextMessage> textMessages;
    private MutableLiveData<List<TextMessage>> mutableLiveDataMessages;

    public TextMessages(){
        if (textmessage == null){
            mutableLiveDataMessages = new MutableLiveData<>();
            textMessages = new ArrayList<TextMessage>();
            textmessage = new MutableLiveData<String>();

        }
    }
    public MutableLiveData<List<TextMessage>> getMessages(){
        if(mutableLiveDataMessages == null){
            mutableLiveDataMessages = new MutableLiveData<List<TextMessage>>();
        }
        return mutableLiveDataMessages;
    }

    public void clearChat(){
        textMessages.clear();
        mutableLiveDataMessages.setValue(textMessages);
    }

    public void addMessage(TextMessage message){
        if(textMessages.isEmpty() || textMessages == null){
            textMessages = new ArrayList<>();
            textMessages.add(message);
            mutableLiveDataMessages.setValue(textMessages);

        }
        else{
            textMessages.add(message);

            for (int i = 0; i < textMessages.size(); i++) {
                System.out.println(textMessages.get(i) + "<" + textMessages.get(i).getText() + ">");
            }

            List<TextMessage> as = mutableLiveDataMessages.getValue();
            mutableLiveDataMessages.setValue(textMessages);

            Log.d(TAG, "addMessage: attempting to set textMessages...");

            Log.d(TAG, "addMessage: Successfully set textMessages! ");
        }
    }
}
