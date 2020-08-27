package com.arobit.chatall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    private ImageButton sendMessage;
    private EditText inputGroupMsg;
    private TextView message, groupName;
    private String groupNameFrom, currentUserId, currentUserName, currentDate, currentTime;

    private FirebaseAuth auth;
    private DatabaseReference userRef, groupRef, groupMsgKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        Intent intent = getIntent();
        groupNameFrom = intent.getStringExtra("groupName").toString();

        init();

        groupName.setText(groupNameFrom);

        getUserInfo();

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = inputGroupMsg.getText().toString();
                sendMessageToDB(message);
                inputGroupMsg.setText("");
            }
        });

    }


    private void init() {
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("NewUsers");
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupNameFrom);

        sendMessage = findViewById(R.id.send_message);
        inputGroupMsg = findViewById(R.id.input_group_msg);
        message = findViewById(R.id.group_chat_text_display);
        groupName = findViewById(R.id.group_name);

    }

    private void getUserInfo() {
        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserName = snapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessageToDB(String message) {
        String messageKey = groupRef.push().getKey();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(getApplicationContext(), "Wright something...", Toast.LENGTH_LONG).show();
        } else {
            Calendar date = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = simpleDateFormat.format(date.getTime());


            Calendar time = Calendar.getInstance();
            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = simpleTimeFormat.format(time.getTime());

            HashMap<String, Object> groupMsgKey = new HashMap<>();
            groupRef.updateChildren(groupMsgKey);

            groupMsgKeyRef = groupRef.child(messageKey);
            HashMap<String, Object> msgInfoMap = new HashMap<>();
            msgInfoMap.put("name", currentUserName);
            msgInfoMap.put("message", message);
            msgInfoMap.put("date", currentDate);
            msgInfoMap.put("time", currentTime);

            groupMsgKeyRef.updateChildren(msgInfoMap);

        }
    }


}