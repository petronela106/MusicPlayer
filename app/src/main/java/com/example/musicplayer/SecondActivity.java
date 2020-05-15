package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class SecondActivity extends AppCompatActivity implements View.OnClickListener {

    private static ImageButton prev_btn, next_btn, play_btn, list_btn, repeat_btn;
    private static SeekBar status_bar;
    private static  TextView title_view, time_passed, time_total;
    private static String title;
    private static int repeatSong;
    private static Handler mHandler = new Handler();
    private static SharedPreferences s_repeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        prev_btn = findViewById(R.id.prev_btn);
        prev_btn.setOnClickListener(this);
        prev_btn.setImageResource(android.R.drawable.ic_media_previous);
        play_btn = findViewById(R.id.play_btn);
        play_btn.setOnClickListener(this);
        if (MainActivity.isPlaying()) {
            play_btn.setBackgroundResource(android.R.drawable.ic_media_pause);

        } else {
            play_btn.setBackgroundResource(android.R.drawable.ic_media_play);
        }
        next_btn = findViewById(R.id.next_btn);
        next_btn.setOnClickListener(this);
        next_btn.setImageResource(android.R.drawable.ic_media_next);
        list_btn = findViewById(R.id.list_btn);
        list_btn.setOnClickListener(this);

        repeat_btn = findViewById(R.id.repeat_btn);
        repeat_btn.setOnClickListener(this);

        title_view = findViewById(R.id.title_view);
        title_view.setText(title);

        time_passed = findViewById(R.id.time_passed);

        time_total = findViewById(R.id.time_total);
        time_total.setText(MainActivity.getTime(MainActivity.getDuration()));

        status_bar = (SeekBar) findViewById(R.id.statusBar);

        status_bar.setMax(100);
        status_bar.setProgress(0);

        status_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);
                int totalDuration = MainActivity.getDuration();
                int currentPosition = MainActivity.progressToTimer(seekBar.getProgress(), totalDuration);

                MainActivity.progressUser(currentPosition);
                updateProgressBar();
            }
        });

        MainActivity.setContext(this);

        s_repeat = this.getSharedPreferences("repeat", Context.MODE_PRIVATE);
        repeatSong = s_repeat.getInt("repeat", 0);
        updateRepeatStatus();
    }

    public  static  void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private static Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            int totalDuration = MainActivity.getDuration();
            int currentPosition = MainActivity.currentPosition();


            time_total.setText("" + MainActivity.getTime(totalDuration));
            time_passed.setText("" + MainActivity.getTime(currentPosition));

            int progress = (int) (MainActivity.getProgressPercentage(currentPosition, totalDuration));
            status_bar.setProgress(progress);

            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.list_btn: {
                Intent i = new Intent(SecondActivity.this, MainActivity.class);
                startActivity(i);
                break;}
            case R.id.play_btn:{
                if(MainActivity.isPlaying()){
                    MainActivity.stopPlaying();
                    play_btn.setBackgroundResource(android.R.drawable.ic_media_play);
                }else{
                    MainActivity.playSong(title);
                    play_btn.setBackgroundResource(android.R.drawable.ic_media_pause);
                }
                break;
            }
            case R.id.next_btn:{
                MainActivity.nextSong(title);
                title_view.setText(title);

                play_btn.setBackgroundResource(android.R.drawable.ic_media_pause);
                break;
            }
            case R.id.prev_btn:{
                MainActivity.prevSong(title);
                title_view.setText(title);

                play_btn.setBackgroundResource(android.R.drawable.ic_media_pause);
                break;
            }
            case  R.id.repeat_btn:{
                repeatSong = (repeatSong + 1) % 3;
                updateRepeatStatus();
                s_repeat = this.getSharedPreferences("repeat", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = s_repeat.edit();
                editor.putInt("repeat", repeatSong);
                editor.apply();
                break;
            }
        }
    }

    public static void updateTitle(String mTitle){
        title  = mTitle;
    }


    public static void updateTitleView(String mTitle){
        title_view.setText(mTitle);
    }

    public static void changePlayBtn(){
        play_btn.setBackgroundResource(android.R.drawable.ic_media_play);
    }

    public static int updateRepeatStatus(){
        if(repeatSong == 0){
            repeat_btn.setImageResource(R.drawable.repeat_off);
        }else if(repeatSong == 1){
            repeat_btn.setImageResource(R.drawable.repeat_one);
        }else if(repeatSong == 2){
            repeat_btn.setImageResource(R.drawable.repeat_all);
        }
        return repeatSong;
    }


}
