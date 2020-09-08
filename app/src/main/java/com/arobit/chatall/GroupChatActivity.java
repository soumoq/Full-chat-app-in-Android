package com.arobit.chatall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.libizo.CustomEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import pub.devrel.easypermissions.EasyPermissions;

public class GroupChatActivity extends AppCompatActivity {

    private ImageView gallery;
    private ImageView sendMessage;
    private CustomEditText inputGroupMsg;
    private TextView groupName;
    private String groupNameFrom, currentUserId, currentUserName, currentDate, currentTime, userDP = "";
    private String downloadUrl = null;

    private static int RESULT_LOAD_IMAGE = 1;
    private FirebaseAuth auth;
    private DatabaseReference userRef, groupRef, groupMsgKeyRef;
    private StorageReference userProfileImageRef;
    private ConnectivityManager connectivityManager;
    private ImageView back;


    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);


        methodRequiresTwoPermission();
        Intent intent = getIntent();
        groupNameFrom = intent.getStringExtra("groupName").toString();

        init();
        groupName.setText(groupNameFrom);
        getUserInfo();

        inputGroupMsg.setBackgroundResource(android.R.color.transparent);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = inputGroupMsg.getText().toString();
                sendMessageToDB(message);
                inputGroupMsg.setText("");
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.
                        getActiveNetworkInfo().isConnected()) {
                    Intent i = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                } else {
                    Toast.makeText(getApplicationContext(), "Please turn on your internet", Toast.LENGTH_LONG).show();
                }
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), GroupsActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Toast.makeText(getApplicationContext(), "Please wait...", Toast.LENGTH_LONG).show();
            String currentImageID = getSaltString();
            final StorageReference filePath = userProfileImageRef.child(currentImageID + ".jpg");
            filePath.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Toast.makeText(getApplicationContext(), "image updated", Toast.LENGTH_LONG).show();
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                            //Toast.makeText(getApplicationContext(), downloadUrl, Toast.LENGTH_LONG).show();
                            sendMessageToDB(downloadUrl);
                        }
                    });
                }
            });


        }
    }

    protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("NewUsers");
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupNameFrom);
        userRef.keepSynced(true);
        groupRef.keepSynced(true);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("chat_image");

        sendMessage = findViewById(R.id.send_message);
        inputGroupMsg = findViewById(R.id.input_group_msg);
        groupName = findViewById(R.id.group_name);
        gallery = findViewById(R.id.gallery);
        back = findViewById(R.id.back);

    }

    private void getUserInfo() {
        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserName = snapshot.child("name").getValue().toString();
                    userDP = snapshot.child("image").getValue().toString();

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
            msgInfoMap.put("dp", userDP);

            mRequestQue = Volley.newRequestQueue(this);
            FirebaseMessaging.getInstance().subscribeToTopic("news");

            groupMsgKeyRef.updateChildren(msgInfoMap);

            if (Helper.isAppForground(getApplicationContext()))
                sendNotification(groupNameFrom, currentUserName + " : " + message);

        }
    }


    private void sendNotification(String title, String body) {

        JSONObject json = new JSONObject();
        try {
            json.put("to", "/topics/" + "news");
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", title);
            notificationObj.put("body", body);

            JSONObject extraData = new JSONObject();
            extraData.put("brandId", "puma");
            extraData.put("category", "Shoes");


            json.put("notification", notificationObj);
            json.put("data", extraData);


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("MUR", "onResponse: ");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("MUR", "onError: " + error.networkResponse);
                }
            }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=AAAAm4DGmXs:APA91bFDcSqGPLb-doFgixxjj8jUxIqYJy4adWpBWGHZMumBm8yGvzZqpsXz9UvpGipYUgkbMXOIV6GHPXZSlVQTSBO8Qda46d065G1cAMfSoMx9GMUzoONfPGuKScjThL7EZRMx7PfM");
                    return header;
                }
            };
            mRequestQue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        final ArrayList<String> names = new ArrayList<>();
        final ArrayList<String> dates = new ArrayList<>();
        final ArrayList<String> messages = new ArrayList<>();
        final ArrayList<String> times = new ArrayList<>();
        final ArrayList<String> userDp = new ArrayList<>();


        final ListView listView = findViewById(R.id.list_view);
        final MessageListView[] adopter = new MessageListView[1];

        final RecyclerView recyclerView = findViewById(R.id.rv);
        final MyRecyclerViewAdapter[] adapter = new MyRecyclerViewAdapter[1];

        groupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                try {
                    if (dataSnapshot.exists()) {
                        //displayMessages(snapshot);


                        Message message = dataSnapshot.getValue(Message.class);
                        String user_name = message.getName();
                        String user_date = message.getDate();
                        String user_time = message.getTime();
                        String user_message = message.getMessage();
                        String dp = message.getDp();

                        names.add(user_name);
                        dates.add(user_date);
                        messages.add(user_message);
                        times.add(user_time);
                        userDp.add(dp);

                        //Toast.makeText(getApplicationContext(),dp,Toast.LENGTH_LONG).show();

                        //listView.setStackFromBottom(true);
                        //listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
                        //adopter[0] = new MessageListView(GroupChatActivity.this, names, times, dates, messages);
                        //listView.setAdapter(adopter[0]);
                        //scrollMyListViewToBottom();

                        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                        linearLayoutManager.setStackFromEnd(true);
                        recyclerView.setLayoutManager(linearLayoutManager);
                        adapter[0] = new MyRecyclerViewAdapter(getApplicationContext(), names, times, dates, messages, userDp);
                        //adapter[0].setClickListener(this);
                        recyclerView.setAdapter(adapter[0]);
                        recyclerView.smoothScrollToPosition(Objects.requireNonNull(recyclerView.getAdapter()).getItemCount());


                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error" + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Error: ", e + "");
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                if (dataSnapshot.exists()) {
                    //displayMessages(snapshot);


                }
            }

            private void scrollMyListViewToBottom() {
                /*listView.post(new Runnable() {
                    @Override
                    public void run() {
                        // Select the last row so it will scroll into view...
                        listView.setSelection(adopter[0].getCount() - 1);
                    }
                });*/
                listView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listView.setSelection(adopter[0].getCount());
                    }
                }, 1000);
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