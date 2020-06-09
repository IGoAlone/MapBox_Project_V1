package com.example.igoalone_mapboxapi_training;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import org.w3c.dom.Text;

import static java.lang.Thread.sleep;

public class LogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);


        TextView textView = findViewById(R.id.textView);
        Animation anim;
        anim = new AlphaAnimation(0.0f,1.0f);
        anim.setDuration(200);
        anim.setStartOffset(100);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);

        textView.startAnimation(anim);



        Handler hand = new Handler();

        hand.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent loadIntent = new Intent(LogoActivity.this, RegisterPhoneNum.class);
                startActivity(loadIntent);
                finish();
            }
        },2300);
    }
}
