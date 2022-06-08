package com.example.assignment1.View.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment1.R;
import com.example.assignment1.controller.entity.TextMessage;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter{
    private final List<TextMessage> localMessagesList;
    final static int OUTBOUND = 1;
    final static int INBOUND = 2;

    public static class OutboundHolder extends RecyclerView.ViewHolder{
        private final MaterialTextView textViewMessageOutbound;
        private final MaterialTextView textViewAuthorOutbound;

        public OutboundHolder(@NonNull View view) {
            super(view);

            textViewMessageOutbound = view.findViewById(R.id.textViewMessageOutbound);
            textViewAuthorOutbound = view.findViewById(R.id.textViewAuthorOutbound);
        }

        public void Bind(TextMessage message){
            textViewMessageOutbound.setText(message.getText());
            textViewAuthorOutbound.setText("client");
        }
    }
    public static class InboundHolder extends RecyclerView.ViewHolder{
        private final MaterialTextView textViewMessageInbound;
        private final MaterialTextView textViewAuthorInbound;

        public InboundHolder(@NonNull View view){
            super(view);

            textViewMessageInbound = view.findViewById(R.id.textViewMessageInbound);
            textViewAuthorInbound = view.findViewById(R.id.textViewAuthorInbound);
        }
        public void Bind(TextMessage message){
            textViewMessageInbound.setText(message.getText());
            textViewAuthorInbound.setText(message.getAuthor());
        }
    }

    public MessagesAdapter(List<TextMessage> messagesList){
        System.out.println(messagesList.size() + " !input data size ");

        localMessagesList = messagesList;
        System.out.println(localMessagesList.size() + " !local dataset size");
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        System.out.println("VIEWTYPE " + viewType);
        if (viewType == INBOUND){
            return new InboundHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_message_inbound, parent, false));
        }
        else {
            return new OutboundHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_message_outbound, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof InboundHolder){
            System.out.println(localMessagesList.get(position).getText() + "INBOUND");
            ((InboundHolder)holder).Bind(localMessagesList.get(position));
        }
        else if (holder instanceof OutboundHolder){
            System.out.println(localMessagesList.get(position).getText() + "OUTBOUND");
            ((OutboundHolder)holder).Bind(localMessagesList.get(position));
            }
    }


    @Override
    public int getItemViewType(int position) {
        if(localMessagesList.get(position).getType() == TextMessage.TEXT_MESSAGE_OUTBOUND){
            return OUTBOUND;
        }
       else if(localMessagesList.get(position).getType() == TextMessage.TEXT_MESSAGE_INBOUND){
           return INBOUND;
        }
       return 0;
    }
    @Override
    public int getItemCount() {
        return localMessagesList.size();
    }
}
