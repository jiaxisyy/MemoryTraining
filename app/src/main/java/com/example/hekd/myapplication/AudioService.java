package com.example.hekd.myapplication;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

/**
 * Created by hekd on 2017/9/5.
 */

public class AudioService extends Service implements MediaPlayer.OnCompletionListener {
    private MediaPlayer mediaPlayer;
    private IBinder binder=new AudioBinder();
    private MediaPlayer player;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
//        stopSelf();//结束了。则结束Service
        mp.start();
    }

    class AudioBinder extends Binder{

        AudioService getService(){
            return AudioService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        player = MediaPlayer.create(this, R.raw.bgm_bg);
        player.setOnCompletionListener(this);
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        if(!player.isPlaying()){
            player.start();
        }

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player.isPlaying()){
            player.stop();
        }
        player.release();
    }
}
