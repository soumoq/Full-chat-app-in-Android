package com.arobit.chatall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class ImageViewActivity extends AppCompatActivity {

    private ImageView image, back;
    private TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_image_view);

            init();

            Intent intent = getIntent();
            String user_image = intent.getStringExtra("user_image");
            String user_name = intent.getStringExtra("user_name");

            name.setText(user_name);

            Glide.with(this)
                    .load(user_image)
                    .into(image);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Error: "+e,Toast.LENGTH_LONG).show();
        }

    }

    private void init() {
        image = findViewById(R.id.image);
        back = findViewById(R.id.back);
        name = findViewById(R.id.sender_name);
    }
}