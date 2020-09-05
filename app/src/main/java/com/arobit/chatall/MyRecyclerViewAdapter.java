package com.arobit.chatall;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private ArrayList<String> user_name;
    private ArrayList<String> user_time;
    private ArrayList<String> user_dates;
    private ArrayList<String> user_messages;

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, ArrayList<String> name, ArrayList<String> times, ArrayList<String> dates, ArrayList<String> messages) {
        this.mInflater = LayoutInflater.from(context);
        this.user_name = name;
        this.user_time = times;
        this.user_dates = dates;
        this.user_messages = messages;

    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.message_view, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.name.setText(user_name.get(position));
        holder.time.setText(user_time.get(position));
        holder.date.setText(user_dates.get(position));


        boolean isValid = URLUtil.isValidUrl(user_messages.get(position));

        holder.message.setText(user_messages.get(position));
        if (isValid) {
            try {
                holder.userImage.setVisibility(View.VISIBLE);
                holder.message.setVisibility(View.GONE);
                Glide.with(holder.itemView.getContext())
                        .load(user_messages.get(position))
                        .into(holder.userImage);
                holder.userImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Intent intent = new Intent(view.getContext(), ImageViewActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("user_image", user_messages.get(position));
                            intent.putExtra("user_name", user_name.get(position));
                            view.getContext().startActivity(intent);
                        }catch (Exception e){
                            Toast.makeText(holder.itemView.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } catch (Exception e) {
                Toast.makeText(holder.itemView.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else
            holder.message.setText(user_messages.get(position));


    }

    // total number of rows
    @Override
    public int getItemCount() {
        return user_name.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView name, time, date, message;
        private ImageView userImage;

        ViewHolder(View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.user_img);
            name = itemView.findViewById(R.id.name);
            time = itemView.findViewById(R.id.time);
            date = itemView.findViewById(R.id.date);
            message = itemView.findViewById(R.id.message);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}