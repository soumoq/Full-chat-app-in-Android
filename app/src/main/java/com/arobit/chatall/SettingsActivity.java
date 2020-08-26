package com.arobit.chatall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button update;
    private EditText profileStatus, username;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);

            init();

            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateSettings();
                }
            });

            userInfo();

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "1 Error: " + e, Toast.LENGTH_LONG).show();
        }

    }

    private void userInfo() {
        rootRef.child("NewUsers").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if((snapshot.exists()) && (snapshot.hasChild("name")  && (snapshot.hasChild("image")))){
                            String name = snapshot.child("name").getValue().toString();
                            String status = snapshot.child("status").getValue().toString();
                            String image = snapshot.child("image").getValue().toString();

                            username.setText(name);
                            profileStatus.setText(status);

                        }else if((snapshot.exists()) && (snapshot.hasChild("name"))){
                            String name = snapshot.child("name").getValue().toString();
                            String status = snapshot.child("status").getValue().toString();

                            username.setText(name);
                            profileStatus.setText(status);
                        }else {
                            Toast.makeText(getApplicationContext(), "Set your profile info...", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        update = findViewById(R.id.update);
        profileStatus = findViewById(R.id.status);
        username = findViewById(R.id.user_name);
        userProfileImage = findViewById(R.id.profile_image);
    }

    private void updateSettings() {
        String name = username.getText().toString();
        String status = profileStatus.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "Please wright your username", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(status)) {
            Toast.makeText(getApplicationContext(), "Please wright your status", Toast.LENGTH_LONG).show();
        } else {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", name);
            profileMap.put("status", status);

            rootRef.child("NewUsers").child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Profile updated", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Something went wrong: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }
    }
}