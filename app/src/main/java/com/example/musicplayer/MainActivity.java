package com.example.musicplayer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnItemClickListener, MediaPlayer.OnCompletionListener {

    private static HashMap<String, String> songMap = new HashMap<>();
    private static String[] songList;
    private static ImageButton play_btn;
    private static TextView title_view;
    private  static SharedPreferences c_song, time_song ;
    private static MediaPlayer mp;
    private static Context mContext ;
    private static String mTitle;
    private static int time_passed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
            setContentView(R.layout.activity_main);
        }

        mContext = this;

        if(mp == null){
            mp = new MediaPlayer();
        }

        play_btn = findViewById(R.id.play_btn);
        play_btn.setOnClickListener(this);
        play_btn.setBackgroundResource(android.R.drawable.ic_media_play);


        songMap = getAllAudioFromDevice(this);

        songList = new String[songMap.size()];
        int i = 0;
        for (String s : songMap.keySet()){
            songList[i] = s;
            i++;
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter(this, R.layout.song_activity, songList);

        ListView listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        title_view = findViewById(R.id.title_view);

        c_song = this.getSharedPreferences("song", Context.MODE_PRIVATE);
        mTitle = c_song.getString("song", "");
        title_view.setText(mTitle);


        SecondActivity.updateTitle(mTitle);

        title_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(i);
            }
        });

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                adapter.getFilter().filter(query);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        mp.setOnCompletionListener(this);

        if (mp.isPlaying()) {
            play_btn.setBackgroundResource(android.R.drawable.ic_media_pause);

        } else {
            play_btn.setBackgroundResource(android.R.drawable.ic_media_play);
        }
    }

    public HashMap<String, String> getAllAudioFromDevice(final Context context) {
        final HashMap<String, String> tempAudioList = new HashMap<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.ArtistColumns.ARTIST,
                MediaStore.Audio.AudioColumns.DATA,
        };
        Cursor c = context.getContentResolver().query(uri,
                projection,
                null,
                null,
                null);

        if (c != null) {
            while (c.moveToNext()) {
                String name = c.getString(0);
                String artist = c.getString(1);
                String path = c.getString(2);

                if(artist.equals("<unknown>")){
                    tempAudioList.put(name, path + "/");

                }else {
                    tempAudioList.put(artist + " - " + name, path + "/");

                }



            }
            c.close();
        }

        return tempAudioList;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.play_btn) {
            if( mp.isPlaying()){
                MainActivity.stopPlaying();
                play_btn.setBackgroundResource(android.R.drawable.ic_media_play);

            }else{
                playSong(mTitle);
                play_btn.setBackgroundResource(android.R.drawable.ic_media_pause);
            }
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context, final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(MainActivity.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        TextView  textView = (TextView) view.findViewById(R.id.song_view);
        String text = textView.getText().toString();

        playSong(text);

        updateTitleView(text);

        SecondActivity.updateTitle(mTitle);

        Intent i = new Intent(MainActivity.this, SecondActivity.class);
        startActivity(i);
    }

    public static void  playSong(String title){

        String path = songMap.get(title);
        play_btn.setBackgroundResource(android.R.drawable.ic_media_pause);

        mp.reset();
        try {
            mp.setDataSource(getContext(), Uri.parse(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(time_passed != 0){

            mp.seekTo(time_passed);
        }
        mp.start();

        SecondActivity.updateProgressBar();

    }

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context context){
        mContext = context;
    }

    public static  void stopPlaying(){
        mp.stop();
        int  c_time = mp.getCurrentPosition();
        updatePreferences(c_time);
    }

    public static  void updatePreferences(int c_time){
        time_song = getContext().getSharedPreferences("time", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = time_song.edit();
        editor.putInt("time", c_time);
        editor.apply();

        time_song = getContext().getSharedPreferences("time", Context.MODE_PRIVATE);
        time_passed = time_song.getInt("time", 0);
    }

    public static boolean isPlaying(){
        if(mp.isPlaying()){
            return true;
        }
        return false;
    }

    public static void prevSong(String title){
        int currentSong = 0;
        for(int i = 0 ; i < songList.length; i++){
            if(songList[i].equals( title)){
                currentSong = i;
                break;
            }
        }
        if(currentSong == 0){
            currentSong = songList.length -1;
        }else{
            currentSong--;
        }

        updateTitleView(songList[currentSong]);
        SecondActivity.updateTitle(songList[currentSong]);
        SecondActivity.updateTitleView(songList[currentSong]);
        playSong(songList[currentSong]);
        updatePreferences(0);
    }

    public static void nextSong(String title){
        int currentSong = 0;
        for(int i = 0 ; i < songList.length; i++){
            if(songList[i].equals( title)){
                currentSong = i;
                break;
            }
        }
        if(currentSong == songList.length - 1){
            currentSong = 0;
        } else {
            currentSong++;
        }

        MainActivity.updateTitleView(songList[currentSong]);
        SecondActivity.updateTitle(songList[currentSong]);
        SecondActivity.updateTitleView(songList[currentSong]);
        playSong(songList[currentSong]);
        updatePreferences(0);
    }

    public static void updateTitleView(String title) {
        title_view.setText(title);
        mTitle = title;
    }

    public static int currentPosition(){
        return mp.getCurrentPosition();
    }

    public static int getDuration(){
        if(mp != null){
            return mp.getDuration();
        }
        return 0;
    }

    public static void progressUser(int progress){
        mp.seekTo(progress);
    }

    public static String getTime(int time){
        String mtime = "";

        int min = time /1000 / 60;
        int sec = time /1000 %60;

        mtime = min + ":";

        if (sec < 10){
            mtime += "0" + sec;
        }else {
            mtime += sec;
        }

        return mtime;

    }

    public static int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        percentage =(((double)currentSeconds)/totalSeconds)*100;

        return percentage.intValue();
    }

    public static int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        return currentDuration * 1000;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        int repeatSong  = SecondActivity.updateRepeatStatus();
        play_btn.setBackgroundResource(android.R.drawable.ic_media_play);
        if(repeatSong == 0){
            stopPlaying();
            SecondActivity.changePlayBtn();
        }else if(repeatSong == 1){
            playSong(mTitle);
            play_btn.setBackgroundResource(android.R.drawable.ic_media_pause);
        }else if(repeatSong == 2){
            nextSong(mTitle);
            play_btn.setBackgroundResource(android.R.drawable.ic_media_pause);
        }
    }

}

