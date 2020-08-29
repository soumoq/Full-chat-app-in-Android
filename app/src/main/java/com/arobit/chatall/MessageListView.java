package com.arobit.chatall;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class MessageListView extends ArrayAdapter<String> {
    final Activity context;
    final ArrayList<String> user_name;
    final ArrayList<String> user_time;
    final ArrayList<String> user_date;
    final ArrayList<String> user_message;


    public MessageListView(@NonNull Activity context, ArrayList<String> name, ArrayList<String> time, ArrayList<String> date, ArrayList<String> message) {
        super(context, R.layout.message_view, name);
        this.context = context;
        this.user_name = name;
        this.user_time = time;
        this.user_date = date;
        this.user_message = message;

    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        @SuppressLint("ViewHolder") final View rowView = inflater.inflate(R.layout.message_view, null, true);

        TextView userName = rowView.findViewById(R.id.name);
        TextView userTime = rowView.findViewById(R.id.time);
        TextView userDate = rowView.findViewById(R.id.date);
        TextView userMessage = rowView.findViewById(R.id.message);


        userName.setText(user_name.get(position));
        userTime.setText(user_time.get(position));
        userDate.setText(user_date.get(position));
        userMessage.setText(user_message.get(position));

        return rowView;
    }
}

