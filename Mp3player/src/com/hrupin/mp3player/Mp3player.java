package com.hrupin.mp3player;

import com.hrupin.mp3player.R;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Mp3player extends Activity {
	private Button buttonPlayStop;
    private MediaPlayer mPlayer;
    private SeekBar seekBar;
 
    // Here i override onCreate method.
    //
    // setContentView() method set the layout that you will see then
    // the application will starts
    //
    // initViews() method i create to init views components.
    @Override
    public void onCreate(Bundle icicle) {
            super.onCreate(icicle);
            setContentView(R.layout.main);
            initViews();  
 
    }
 
    // This method set the setOnClickListener and method for it (buttonClick())
    private void initViews() {
        buttonPlayStop = (Button) findViewById(R.id.ButtonPlayStop);
        buttonPlayStop.setOnClickListener(new OnClickListener() {@Override public void onClick(View v) {buttonClick();}});
 
        mPlayer = MediaPlayer.create(this, R.raw.testsong); 
 
        seekBar = (SeekBar) findViewById(R.id.SeekBar01);
        seekBar.setMax(mPlayer.getDuration());
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
 
            }
 
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
 
            }
 
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                seekChange(seekBar, progress, fromUser);
 
            }
        });
 
    }
 
    // This is event handler thumb moving event
    private void seekChange(SeekBar seekBar, int progress,
            boolean fromUser){
        mPlayer.seekTo(progress);
    }
 
    // This is event handler for buttonClick event
    private void buttonClick(){
        if (buttonPlayStop.getText() == getString(R.string.play_str)) {
            buttonPlayStop.setText(getString(R.string.pause_str));
            try{
                mPlayer.start();
            }catch (IllegalStateException e) {
                mPlayer.pause();
            }
        }else {
            buttonPlayStop.setText(getString(R.string.play_str));
            mPlayer.pause();
        }
    }
}