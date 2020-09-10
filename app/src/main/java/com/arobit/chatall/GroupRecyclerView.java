package com.arobit.chatall;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GroupRecyclerView extends RecyclerView.Adapter<GroupRecyclerView.ProgrammingViewHolder> {

    private ArrayList<String> group_name;
    private String[] last_name;
    private String[] last_message;
    private String[] last_time;

    private OnItemClickListener mListener;


    public GroupRecyclerView(ArrayList<String> groupName, String[] lastName, String[] lastMessage, String[] lastTime) {
        this.group_name = groupName;
        this.last_name = lastName;
        this.last_message = lastMessage;
        this.last_time = lastTime;

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

        return new ProgrammingViewHolder(view, mListener);
    }


    @Override
    public void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int position) {
        //String lastName = last_name.get(position);

        //group_name =
        //holder.lastName.setText(lastName);
        holder.groupName.setText(group_name.get(position));

        String name, time, text;

        try {

            name = last_name[position];
            time = last_time[position];
            text = last_message[position];

            boolean isValid = URLUtil.isValidUrl(text);

            holder.lastName.setText(name);
            holder.lastTime.setText(time.toLowerCase());
            if (isValid) {
                holder.lastMessage.setVisibility(View.GONE);
                holder.sentImage.setVisibility(View.VISIBLE);
            } else {
                holder.lastMessage.setText(text);
                holder.sentImage.setVisibility(View.GONE);
            }


        } catch (Exception e) {
            Toast.makeText(holder.groupName.getContext(), "error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }


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

        TextView lastName, groupName, lastMessage, lastTime;
        ImageView sentImage;

        public ProgrammingViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            lastName = itemView.findViewById(R.id.last_name);
            groupName = itemView.findViewById(R.id.group_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            lastTime = itemView.findViewById(R.id.last_time);
            sentImage = itemView.findViewById(R.id.image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
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
