package com.example.cookieclickerapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    ImageView spongebobClicker, plankton;
    TextView score, multiply;
    Button playAudio;
    int count = 0;
    int multiplier = 1;
    boolean isPlaying = true;
    ConstraintLayout cl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spongebobClicker = findViewById(R.id.spongebobClicker);
        plankton = findViewById(R.id.plankton);
        score = findViewById(R.id.score);
        multiply = findViewById(R.id.multiply);
        playAudio = findViewById(R.id.pause_audio);
        cl = findViewById(R.id.main);

        final ScaleAnimation animation = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);

        animation.setDuration(400);

        plankton.setOnClickListener(new View.OnClickListener() {
            boolean isCooldown = false;

            @Override
            public void onClick(View v) {
                if (!isCooldown) {
                    //start cooldown
                    isCooldown = true;

                    multiplier++;
                    multiply.setText("Multiplier: x" + multiplier);

                    plankton.startAnimation(animation);
                    plankton.animate()
                            .alpha(0.5f) //fade to 50% opacity
                            .setDuration(500) //duration of fade-out
                            .withEndAction(() -> {
                                //wait for 5 seconds before restoring plankton's visibility
                                plankton.postDelayed(() -> {
                                    plankton.animate()
                                            .alpha(1f) //fully visible again
                                            .setDuration(400) //duration of fade in
                                            .withEndAction(() -> isCooldown = false) //end cooldown after fade in
                                            .start();
                                }, 5000); //5 second delay for cooldown
                            })
                            .start();

                }
            }
        });

        spongebobClicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spongebobClicker.startAnimation(animation);
                count += multiplier;
                score.setText("Score: " + count);
                addFloatingLabel(cl, 500, 600);
            }
        });

        playAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying) {
                    stopService(new Intent(MainActivity.this, BackgroundMusicService.class));
                    isPlaying = false;
                    playAudio.setText("OFF");
                } else {
                    startService(new Intent(MainActivity.this, BackgroundMusicService.class));
                    isPlaying = true;
                    playAudio.setText("ON");
                }
            }
        });
    }

    public void addFloatingLabel(ConstraintLayout layout, int clickX, int clickY) {
        Log.d("LABEL", "+1 activated");

        //make a "+1" label
        TextView label = new TextView(layout.getContext());
        label.setText("+" + multiplier);
        label.setTextColor(Color.BLACK);
        label.setTextSize(20);
        label.setVisibility(View.VISIBLE);

        //id for label
        label.setId(View.generateViewId());

        layout.addView(label);

        //layout parameters
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        label.setLayoutParams(params);

        //position
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.connect(label.getId(), ConstraintSet.START, layout.getId(), ConstraintSet.START, clickX); //x offset
        set.connect(label.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, clickY); //y offset
        set.applyTo(layout);

        label.animate()
                .translationYBy(-200) //move the label upward
                .setDuration(1000)    //animation duration
                .withEndAction(() -> layout.removeView(label)) //remove the label after animation
                .start();
    }
}