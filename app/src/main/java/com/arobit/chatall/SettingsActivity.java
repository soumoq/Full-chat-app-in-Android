package com.arobit.chatall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button update;
    private EditText profileStatus, username;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private static int RESULT_LOAD_IMAGE = 1;
    private StorageReference userProfileImageRef;
    private String downloadUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);

            init();

            userInfo();

            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        updateSettings();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            userProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                }
            });


        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "1 Error: " + e, Toast.LENGTH_LONG).show();
        }

    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        update = findViewById(R.id.update);
        profileStatus = findViewById(R.id.status);
        username = findViewById(R.id.user_name);
        userProfileImage = findViewById(R.id.profile_image);
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

            userProfileImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            final StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");
            filePath.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Profile image updated", Toast.LENGTH_LONG).show();

                        //downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();
                                Toast.makeText(getApplicationContext(), downloadUrl, Toast.LENGTH_LONG).show();
                            }
                        });

                    } else {
                        Toast.makeText(getApplicationContext(), "Profile image update failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });


        }
    }

    private void userInfo() {
        DatabaseReference userInfo = rootRef.child("NewUsers").child(currentUserID);
        userInfo.keepSynced(true);
        userInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("name") && (snapshot.hasChild("image")))) {
                    String name = snapshot.child("name").getValue().toString();
                    String status = snapshot.child("status").getValue().toString();
                    String image = snapshot.child("image").getValue().toString();
                    downloadUrl = image;

                    username.setText(name);
                    profileStatus.setText(status);

                    try {
                        Glide.with(SettingsActivity.this)
                                .load(image)
                                .into(userProfileImage);


                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                } else if ((snapshot.exists()) && (snapshot.hasChild("name"))) {
                    String name = snapshot.child("name").getValue().toString();
                    String status = snapshot.child("status").getValue().toString();

                    username.setText(name);
                    profileStatus.setText(status);

                } else {
                    Toast.makeText(getApplicationContext(), "Set your profile info...", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
            profileMap.put("image", downloadUrl);

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
