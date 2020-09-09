package com.arobit.chatall;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GroupRecyclerView extends RecyclerView.Adapter<GroupRecyclerView.ProgrammingViewHolder> {

    private ArrayList<String> group_name;
    private ArrayList<String> last_name;
    private OnItemClickListener mListener;


    public GroupRecyclerView(ArrayList<String> groupName, ArrayList<String> lastName) {
        this.group_name = groupName;
        this.last_name = lastName;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }




    @NonNull
    @Override
    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.groups_recycler_view, parent, false);

        return new ProgrammingViewHolder(view,mListener);
    }


    @Override
    public void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int position) {
        //String lastName = last_name.get(position);

        //group_name =
        //holder.lastName.setText(lastName);
        holder.groupName.setText(group_name.get(position));
        holder.lastName.setText(last_name.get(position));

        //holder.groupName.setText(group_name.get(position));

    }

    @Override
    public int getItemCount() {
        return group_name.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ProgrammingViewHolder extends RecyclerView.ViewHolder {

        TextView lastName, groupName;

        public ProgrammingViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            lastName = itemView.findViewById(R.id.last_name);
            groupName = itemView.findViewById(R.id.group_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position!= RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
