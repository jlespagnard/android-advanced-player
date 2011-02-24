package fr.unice.aap;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class AAP extends Activity {
	
	private ImageButton buttonPlayStop;
	private ImageButton buttonLoop;
	private ImageButton buttonVolume;
	private SeekBar seekBar_Volume;
	private SeekBar seekBar_Music;
	private MediaPlayer mPlayer;
	private Thread thread_music;
	private Boolean isPlay = false;
	private Boolean isLoop = false;
	private Boolean onTouchSeekBarMusic = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initEven();    
        createProgressThread();
    }
    
    private void initEven()
    {     	
    	mPlayer = MediaPlayer.create(this, R.raw.testsong);    	    	
    	
    	//----------------- evenements sur les boutons -----------------------------
    	
    	//play/pause
    	buttonPlayStop = (ImageButton) findViewById(R.id.playPause);
        buttonPlayStop.setOnTouchListener( new OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				if(isPlay){
					switch ( event.getAction() ) { 
					case MotionEvent.ACTION_DOWN:
						buttonPlayStop.setBackgroundResource(R.drawable.pauseclick);						
						break; 
					case MotionEvent.ACTION_UP: 
						buttonPlayStop.setBackgroundResource(R.drawable.play);
						mPlayer.pause();
						isPlay = false;
						break; 
					}
				}else{
					switch ( event.getAction() ) { 
					case MotionEvent.ACTION_DOWN:
						buttonPlayStop.setBackgroundResource(R.drawable.playclick);
						break; 
					case MotionEvent.ACTION_UP: 
						buttonPlayStop.setBackgroundResource(R.drawable.pause);
						try{
			                mPlayer.start();
			            }catch (IllegalStateException e) {
			                mPlayer.pause();
			                isPlay = false;
			            }
						isPlay = true;
						break; 
					}
				}						
				return true;
			}
		}); 
        
        //loop
        buttonLoop = (ImageButton) findViewById(R.id.loop);
        buttonLoop.setOnClickListener(new OnClickListener() {
        	@Override public void onClick(View v) {
        		if (isLoop) {
                    buttonLoop.setBackgroundResource(R.drawable.loop);  
                    isLoop = false;
                }else {
                	buttonLoop.setBackgroundResource(R.drawable.loopclick);  
                	isLoop = true;
                }
        	}
        });   
        
      //Volume
        buttonVolume = (ImageButton) findViewById(R.id.volume);
        buttonVolume.setOnClickListener(new OnClickListener() {
        	@Override public void onClick(View v) {
        		if (seekBar_Volume.getVisibility() == View.VISIBLE) {
        			seekBar_Volume.setVisibility(View.INVISIBLE);
                }else {
                	seekBar_Volume.setVisibility(View.VISIBLE);
                }
        	}
        }); 
        
        //seebar volume
        seekBar_Volume = (SeekBar) findViewById(R.id.seekbar_volume);
        seekBar_Volume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        	 
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { 
            }
 
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
 
            }
 
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                if(progress == 0){
                	buttonVolume.setBackgroundResource(R.drawable.sonoff);
                }
                else{
                	buttonVolume.setBackgroundResource(R.drawable.sonon);
                }
 
            }
        });
        
        //seekbar musique
        seekBar_Music = (SeekBar) findViewById(R.id.seekbar_music);
        seekBar_Music.setMax(mPlayer.getDuration()); 
        seekBar_Music.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
       	 
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            	mPlayer.seekTo(seekBar_Music.getProgress());
            	onTouchSeekBarMusic = false;
            }
 
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            	onTouchSeekBarMusic = true;
            }
 
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {         	              
            }
        });
    }
    
    private void createProgressThread() {

        Runnable _progressUpdater = new Runnable() {
            @Override
            public void run() {
            	while(true)
            	{
                    if(isPlay && !onTouchSeekBarMusic) {
                        try
                        {
                        	seekBar_Music.setProgress(mPlayer.getCurrentPosition());  
                        }
                        catch(Exception e)
                        {}
                    }
                }
            }
        };
        thread_music = new Thread(_progressUpdater);
        thread_music.start();
    }
}