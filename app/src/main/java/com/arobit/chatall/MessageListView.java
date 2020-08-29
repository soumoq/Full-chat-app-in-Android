package com.arobit.chatall;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        @SuppressLint("ViewHolder") final View rowView = inflater.inflate(R.layout.message_view, null, true);


        FirebaseAuth auth;
        auth = FirebaseAuth.getInstance();
        DatabaseReference rootRef;
        final String[] temp_name = new String[1];
        String currentUserID = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userInfo = rootRef.child("NewUsers").child(currentUserID);
        userInfo.keepSynced(true);
        rootRef.keepSynced(true);
        userInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                temp_name[0] = snapshot.child("name").getValue().toString();


                TextView userName = rowView.findViewById(R.id.name);
                TextView userTime = rowView.findViewById(R.id.time);
                TextView userDate = rowView.findViewById(R.id.date);
                TextView userMessage = rowView.findViewById(R.id.message);
                LinearLayout msgBox = rowView.findViewById(R.id.message_box);

                //RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)msgBox.getLayoutParams();
                //params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                if (temp_name[0].equals(user_name.get(position))) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)msgBox.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                }

                userName.setText(user_name.get(position));
                userTime.setText(user_time.get(position));
                userDate.setText(user_date.get(position));
                userMessage.setText(user_message.get(position));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return rowView;
    }


    public void receiveMessage() {

    }
}
