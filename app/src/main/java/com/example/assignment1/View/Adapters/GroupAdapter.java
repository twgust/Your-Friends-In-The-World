package com.example.assignment1.View.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment1.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;


public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
        private final ViewHolder.OnGroupSelectedListener groupSelectedListener;
        private final ViewHolder.onJoinGroupClicked joinGroupListener;
        private ViewHolder.onRefreshGroupClicked refreshGroupListener;
        private List<String> localDataSet;


        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
           // public MaterialTextView getMemberCountView() {
              //  return memberCountView;
         //   }

            private final MaterialTextView groupNameView;
            //private final MaterialTextView memberCountView;
            private final MaterialButton joinButton;
            private final MaterialButton refreshButton;
            OnGroupSelectedListener onGroupSelectedListener;
            onJoinGroupClicked joinButtonListener;
            onRefreshGroupClicked refreshButtonListener;
            public ViewHolder(View view, OnGroupSelectedListener onGroupSelectedListener1, onJoinGroupClicked joinButtonListener, onRefreshGroupClicked refreshButtonListener) {
                super(view);
                // Define click listener for the ViewHolder's View
                this.onGroupSelectedListener = onGroupSelectedListener1;
                this.joinButtonListener = joinButtonListener;
                this.refreshButtonListener = refreshButtonListener;
                joinButton = (MaterialButton) view.findViewById(R.id.signUp_GroupItem);
                groupNameView = (MaterialTextView) view.findViewById(R.id.groupTextView_GroupItem);
                refreshButton = (MaterialButton) view.findViewById(R.id.refreshButton_GroupItem);
                // memberCountView = (MaterialTextView) view.findViewById(R.id.onlineInGroup);
                //textView = (TextView) view.findViewById(R.id.textView);
                view.setOnClickListener(this);
                onClick();
            }

            @Override
            public void onClick(View view) {
                onGroupSelectedListener.onGroupClicked(getAdapterPosition());

            }

            public MaterialTextView getGroupNameView() {
                return groupNameView;
            }
            public interface getMembers{
                void setMembers(String name);
            }
            public void onClick(){
                joinButton.setOnClickListener((view -> {
                        joinButtonListener.joinGroupClicked(groupNameView.getText().toString());
                }));
                refreshButton.setOnClickListener((view ->{
                        refreshButtonListener.refreshGroupClicked(groupNameView.getText().toString());
                }));
            }
            public interface onRefreshGroupClicked{
                void refreshGroupClicked(String name);
            }
            public interface onJoinGroupClicked {
                void joinGroupClicked(String groupName);
            }
            public interface OnGroupSelectedListener {
                void onGroupClicked(int pos);
            }
        }


        /**
         * Initialize the dataset of the Adapter.
         *
         * @param dataSet String[] containing the data to populate views to be used
         * by RecyclerView.
         */
        public GroupAdapter(List<String> dataSet, ViewHolder.OnGroupSelectedListener onGroupSelectedListener, ViewHolder.onJoinGroupClicked groupSelectedListener, ViewHolder.onRefreshGroupClicked refreshGroupListener) {
            localDataSet = dataSet;
            this.groupSelectedListener = onGroupSelectedListener;
            this.joinGroupListener = groupSelectedListener;
            this.refreshGroupListener = refreshGroupListener;

        }

        // Create new views (invoked by the layout manager)
        @NonNull
        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            // Create a new view, which defines the UI of the list item
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.recycler_item_group, viewGroup, false);


            MaterialTextView textView =  view.findViewById(R.id.groupTextView_GroupItem);
            //MaterialTextView number = view.findViewById(R.id.onlineInGroup);


            //tV.setBackground(view.getResources().getDrawable(R.drawable.mesh1));
            return new ViewHolder(view, groupSelectedListener, joinGroupListener, refreshGroupListener);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            MaterialTextView groupNameView =  viewHolder.getGroupNameView();
            for (int i = 0; i < localDataSet.size(); i++) {
                groupNameView.setText(localDataSet.get(position));

            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return localDataSet.size();
        }
    }

