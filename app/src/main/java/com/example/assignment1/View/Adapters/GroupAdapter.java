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
        private final ViewHolder.joinGroupListener joinGroupListener;
        private final ViewHolder.leaveGroupListener leaveGroupListener;
        private final ViewHolder.refreshGroupListener refreshGroupListener;
        private final ViewHolder.groupViewListener groupSelectedListener;

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
            private final MaterialButton leaveButton;
            private final MaterialButton refreshButton;

            ViewHolder.leaveGroupListener leaveButtonListener;
            ViewHolder.joinGroupListener joinButtonListener;
            ViewHolder.refreshGroupListener refreshButtonListener;
            groupViewListener groupViewListener;

            public ViewHolder(View view, groupViewListener groupViewListener1, ViewHolder.joinGroupListener joinButtonListener, ViewHolder.refreshGroupListener refreshButtonListener, ViewHolder.leaveGroupListener onLeaveButtonListener) {
                super(view);

                // Define click listener for the ViewHolder's View
                this.joinButtonListener = joinButtonListener;
                this.leaveButtonListener = onLeaveButtonListener;
                this.refreshButtonListener = refreshButtonListener;
                this.groupViewListener = groupViewListener1;

                joinButton = (MaterialButton) view.findViewById(R.id.joinButton_GroupItem);
                groupNameView = (MaterialTextView) view.findViewById(R.id.groupTextView_GroupItem);
                refreshButton = (MaterialButton) view.findViewById(R.id.refreshButton_GroupItem);
                leaveButton = (MaterialButton) view.findViewById(R.id.leaveButton_GroupItem);

                // memberCountView = (MaterialTextView) view.findViewById(R.id.onlineInGroup);
                //textView = (TextView) view.findViewById(R.id.textView);

                view.setOnClickListener(this);
                initializeButtonListeners();
            }

            @Override
            public void onClick(View view) {
                groupViewListener.onGroupClicked(getAdapterPosition());

            }

            public MaterialTextView getGroupNameView() {
                return groupNameView;
            }
            public interface getMembers{
                void setMembers(String name);
            }

            // by doing this we get access to the name of the group which the user interacted with
            public void initializeButtonListeners(){
                joinButton.setOnClickListener((view -> {
                        joinButtonListener.joinGroupClicked(groupNameView.getText().toString());
                }));
                refreshButton.setOnClickListener((view ->{
                        refreshButtonListener.refreshGroupClicked(groupNameView.getText().toString());
                }));
                leaveButton.setOnClickListener((view ->{
                        leaveButtonListener.leaveGroupClicked(groupNameView.getText().toString());
                }));
                
            }

            public interface joinGroupListener {
                void joinGroupClicked(String groupName);
            }
            public interface leaveGroupListener {
                void leaveGroupClicked(String groupName);
            }
            public interface refreshGroupListener {
                void refreshGroupClicked(String name);
            }

            public interface groupViewListener {
                void onGroupClicked(int pos);
            }

        }


        /**
         * Initialize the dataset of the Adapter.
         *
         * @param dataSet String[] containing the data to populate views to be used
         * by RecyclerView.
         */
        public GroupAdapter(List<String> dataSet, ViewHolder.groupViewListener groupViewListener, ViewHolder.joinGroupListener joinGroupListener, ViewHolder.refreshGroupListener refreshGroupListener, ViewHolder.leaveGroupListener leaveGroupListener) {
            localDataSet = dataSet;
            this.joinGroupListener = joinGroupListener;
            this.leaveGroupListener = leaveGroupListener;
            this.refreshGroupListener = refreshGroupListener;

            this.groupSelectedListener = groupViewListener;

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
            return new ViewHolder(view, groupSelectedListener, joinGroupListener, refreshGroupListener, leaveGroupListener);
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

