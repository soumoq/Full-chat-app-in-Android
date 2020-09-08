package com.arobit.chatall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private Button chat, group, contact, logout, findFriend, settings, createGroup;


    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            init();


            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    auth.signOut();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });

            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }
            });

            createGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestNewGroup();
                }
            });


            if (getIntent().hasExtra("category")) {
                Intent intent = new Intent(MainActivity.this, GroupsActivity.class);
                intent.putExtra("category", getIntent().getStringExtra("category"));
                intent.putExtra("brandId", getIntent().getStringExtra("brandId"));
                startActivity(intent);
            }
            mRequestQue = Volley.newRequestQueue(this);
            FirebaseMessaging.getInstance().subscribeToTopic("news");


            group.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //sendNotification();
                    startActivity(new Intent(getApplicationContext(), GroupsActivity.class));
                }
            });


        } catch (Exception e) {
            Toast.makeText(this, "1 Error: " + e, Toast.LENGTH_LONG).show();
        }

    }


    private void init() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

        chat = findViewById(R.id.chats);
        logout = findViewById(R.id.logout);
        settings = findViewById(R.id.settings);
        createGroup = findViewById(R.id.create_group);
        group = findViewById(R.id.grup);
    }


    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter group name");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("Group name");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(getApplicationContext(), "Wright something", Toast.LENGTH_LONG).show();
                } else {
                    createNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();

    }


    private void createNewGroup(String groupName) {
        rootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Group is created...", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Group is not created...", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        //methodRequiresTwoPermission();
        if (currentUser == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            String currentUserId = auth.getCurrentUser().getUid();
            rootRef.child("NewUsers").child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child("name").exists()) {
                    } else {
                        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        startActivity(new Intent(getApplicationContext(), GroupsActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void methodRequiresTwoPermission() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "Grant permission for read wright storage",
                    1, perms);
        }
    }


}