package com.arobit.chatall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.*;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GroupsActivity extends AppCompatActivity {

    private TextView test;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> listGroup = new ArrayList<>();
    private ImageView settings, logout, search;

    private DatabaseReference groupRef;
    private FirebaseAuth auth;
    private boolean doubleBackToExitPressedOnce = false;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);


        init();
        displayGroup();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

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

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
    }

    private void allGroup() {
        AllGroupPopUp allGroupPopUp = new AllGroupPopUp();
        allGroupPopUp.show(getSupportFragmentManager(), "PopUpOtpVerification");
    }

    private void init() {
        try {
            test = findViewById(R.id.test);
            auth = FirebaseAuth.getInstance();
            groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
            groupRef.keepSynced(true);
            listView = findViewById(R.id.list_view_g);
            arrayAdapter = new ArrayAdapter<String>(GroupsActivity.this, R.layout.support_simple_spinner_dropdown_item, listGroup);
            listView.setAdapter(arrayAdapter);
            settings = findViewById(R.id.settings);
            logout = findViewById(R.id.logout);
            search = findViewById(R.id.search);
            recyclerView = findViewById(R.id.recycler_view);
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
                String currentUserID = null;
                DatabaseReference userInfo = null;

                try {
                    currentUserID = auth.getCurrentUser().getUid();
                    userInfo = rootRef.child("NewUsers").child(currentUserID);
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


                                        final ArrayList<String> aList = new ArrayList<String>();
                                        for (String x : tempSet)
                                            aList.add(x);


                                        //Toast.makeText(getApplicationContext(), "" + aList, Toast.LENGTH_LONG).show();
                                        //listGroup.clear();
                                        //listGroup.addAll(tempSet);
                                        //arrayAdapter.notifyDataSetChanged();


                                        final String[] lastName = new String[aList.size()];
                                        final String[] lastMessage = new String[aList.size()];
                                        final String[] lastTime = new String[aList.size()];

                                        final ArrayList <String> lt = new ArrayList<>();
                                        final ArrayList <String> ln = new ArrayList<>();
                                        final ArrayList <String> lm = new ArrayList<>();



                                        for (String x : aList) {
                                            //lastName.add("soumo");
                                            //lastMessage.add("hello");
                                            //lastTime.add("12.30");

                                            //DatabaseReference lastChatData;
                                            //lastChatData = groupRef.child("Java");
                                            //lastChatData.keepSynced(true);

                                            Query lastQuery = groupRef.child(x).orderByKey().limitToLast(1);
                                            lastQuery.addChildEventListener(new ChildEventListener() {
                                                @Override
                                                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                                    try {
                                                        if (snapshot.exists()) {

                                                            lt.add(snapshot.child("time").getValue().toString());
                                                            ln.add(snapshot.child("name").getValue().toString());
                                                            lm.add(snapshot.child("message").getValue().toString());


                                                            for(int j =0;j<lt.size();j++){
                                                                lastTime[j] = lt.get(j);
                                                                lastMessage[j] = lm.get(j);
                                                                lastName[j] = ln.get(j);
                                                            }



                                                            //String data = snapshot.getValue().toString();

                                                            GroupRecyclerView mAdapter = new GroupRecyclerView(aList, lastName, lastMessage, lastTime);
                                                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                                            recyclerView.setAdapter(mAdapter);
                                                            mAdapter.setOnItemClickListener(new GroupRecyclerView.OnItemClickListener() {
                                                                @Override
                                                                public void onItemClick(int position) {
                                                                    //Toast.makeText(getApplicationContext(), "" + aList.get(position), Toast.LENGTH_LONG).show();
                                                                    Intent intent = new Intent(getApplicationContext(), GroupChatActivity.class);
                                                                    intent.putExtra("groupName", aList.get(position));
                                                                    startActivity(intent);
                                                                }
                                                            });




                                                        }
                                                    } catch (Exception e) {
                                                        test.setText("Error" + e.getMessage());
                                                    }

                                                }

                                                @Override
                                                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                                }

                                                @Override
                                                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                                                }

                                                @Override
                                                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                        }


                                        //Toast.makeText(getApplicationContext(), lastName.length + "", Toast.LENGTH_LONG).show();


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
                } catch (Exception e) {

                }


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