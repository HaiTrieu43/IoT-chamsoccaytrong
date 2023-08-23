package com.example.duankythuat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalTime;

public class MainActivity extends AppCompatActivity {
    DatabaseReference mDatabase;
    TextView temp;
    TextView hump;
    MaterialButton auto_btn;
    MaterialButton manual_btn;
    MaterialButton motor_btn;
    MaterialButton statusDot;
    Boolean motorIsActive = false;
    Boolean modeStatus = false;
    MaterialCardView motorControl;
    TimePicker timePicker;
    String TAG = "TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(getApplicationContext());
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), ContextCompat.getColor(getBaseContext(), R.color.gray), ContextCompat.getColor(getBaseContext(), R.color.green));
        colorAnimation.setDuration(2000); // Đặt thời gian chuyển màu là 2 giây
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int color = (int) animator.getAnimatedValue();
                motorControl.setStrokeColor(color);
            }
        });

        setContentView(R.layout.activity_main);

        temp = findViewById(R.id.temp);
        hump = findViewById(R.id.hum);
        auto_btn = findViewById(R.id.auto_btn);
        manual_btn = findViewById(R.id.manual_btn);
        motor_btn = findViewById(R.id.motor_btn);
        statusDot = findViewById(R.id.statusDot);
        motorControl = findViewById(R.id.motorControl);
        timePicker = findViewById(R.id.timepicker);

        auto_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child("mode").setValue(1);
                mDatabase.child("motor").setValue(0);
            }
        });
        manual_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child("mode").setValue(0);
            }
        });
        motor_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!modeStatus) {
                    mDatabase.child("motor").setValue(motorIsActive ? 0 : 1);
                    return;
                }
                Toast.makeText(getBaseContext(), "Cannot turn motor!! Mode Auto is enable", Toast.LENGTH_SHORT).show();

            }
        });

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay;
                int minus = minute;
                LocalTime time = LocalTime.of(hourOfDay,minute);
                mDatabase.child("time").setValue(time);
            }
        });
        mDatabase.child("motor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long status = (Long) snapshot.getValue();
                motorIsActive = status == 1 ? true : false;
                if (motorIsActive) {
                    colorAnimation.start();
                } else {
                    colorAnimation.cancel();
                    motorControl.setStrokeColor(ContextCompat.getColor(getBaseContext(), R.color.gray));
                }
                motorActiveChanged(motorIsActive);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mDatabase.child("mode").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long mode = (Long) snapshot.getValue();
                modeStatus = mode == 1 ? true : false;
                modeChanged(modeStatus);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mDatabase.child("temp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Float data = snapshot.getValue(Float.class);
                if (data != null) {
                    temp.setText(String.valueOf(data) + " 'C");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    void modeChanged(Boolean auto) {
        if (auto) {
            auto_btn.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
            auto_btn.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.blue));

            manual_btn.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.black));
            manual_btn.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.white));
        } else {
            manual_btn.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
            manual_btn.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.blue));

            auto_btn.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.black));
            auto_btn.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.white));        mDatabase.child("hump").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Float data = snapshot.getValue(Float.class);
                    if (data != null) {
                        hump.setText(String.valueOf(data) + " %");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

    void motorActiveChanged(Boolean motor) {
        statusDot.setIconTint(ColorStateList.valueOf(motor ? ContextCompat.getColor(getBaseContext(), R.color.green) : ContextCompat.getColor(getBaseContext(), R.color.gray)));
        motor_btn.setText(motor ? "Turn off" : "Turn on");

    }

}