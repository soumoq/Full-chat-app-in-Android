package com.arobit.chatall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.*;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> listGroup = new ArrayList<>();
    private ImageView settings, logout, search;

    private DatabaseReference groupRef;
    private FirebaseAuth auth;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);


        init();
        displayGroup();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String groupName = adapterView.getItemAtPosition(position).toString();
                Intent intent = new Intent(getApplicationContext(), GroupChatActivity.class);
                intent.putExtra("groupName", groupName);
                startActivity(intent);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });

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

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allGroup();
            }
        });


    }

    private void allGroup() {
        AllGroupPopUp allGroupPopUp = new AllGroupPopUp();
        allGroupPopUp.show(getSupportFragmentManager(), "PopUpOtpVerification");
    }

    private void init() {
        try {
            auth = FirebaseAuth.getInstance();
            groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
            groupRef.keepSynced(true);
            listView = findViewById(R.id.list_view_g);
            arrayAdapter = new ArrayAdapter<String>(GroupsActivity.this, R.layout.support_simple_spinner_dropdown_item, listGroup);
            listView.setAdapter(arrayAdapter);
            settings = findViewById(R.id.settings);
            logout = findViewById(R.id.logout);
            search = findViewById(R.id.search);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e, Toast.LENGTH_LONG).show();
        }

    }

    public void displayGroup() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final ArrayList<String> set = new ArrayList<>();
                final Set<String> tempSet = new HashSet<>();


                Iterator iterator = snapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    set.add(((DataSnapshot) iterator.next()).getKey());
                }


                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                String currentUserID = auth.getCurrentUser().getUid();
                DatabaseReference userInfo = rootRef.child("NewUsers").child(currentUserID);
                userInfo.keepSynced(true);
                userInfo.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        final String phoneNo;
                        phoneNo = snapshot.child("phone").getValue().toString();


                        int i;
                        for (i = 0; i < set.size(); i++) {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                    .child("new_group").child(set.get(i));
                            final int finalI = i;
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String groupPhoneNo = null;
                                    String user_id = auth.getCurrentUser().getUid();
                                    //Toast.makeText(getApplicationContext(), user_id, Toast.LENGTH_LONG).show();
                                    if (snapshot.child(user_id).exists())
                                        groupPhoneNo = snapshot.child(user_id).getValue().toString();

                                    if (phoneNo.equals(groupPhoneNo)) {
                                        tempSet.add(set.get(finalI));
                                    }

                                    listGroup.clear();
                                    listGroup.addAll(tempSet);
                                    arrayAdapter.notifyDataSetChanged();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
                Intent intent = new Intent(getApplicationContext(), ExitActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }
        }, 2000);
    }


}