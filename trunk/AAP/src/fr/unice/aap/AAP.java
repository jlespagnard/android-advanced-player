package fr.unice.aap;

import fr.unice.aap.musics.MusicListActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class AAP extends Activity {
	
	private ImageButton btnDossier;
	private ImageButton btnAlbum;
	private ImageButton buttonPlayStop;
	private ImageButton buttonLoop;
	private ImageButton buttonVolume;
	private ImageButton buttonTonalite;
	private SeekBar seekBar_Volume;
	private SeekBar seekBar_Music;
	public static MediaPlayer mPlayer;
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
    
    public static void setSong(Context p_appContext, int p_rawId) {
    	if(mPlayer != null) {
	    	mPlayer.stop();
	    	mPlayer.seekTo(0);
	    	mPlayer.release();
    	}
    	mPlayer = MediaPlayer.create(p_appContext,p_rawId);
    }
    
    public static void setSong(Context p_appContext, Uri p_uri) {
    	if(mPlayer != null) {
	    	mPlayer.stop();
	    	mPlayer.seekTo(0);
	    	mPlayer.release();
    	}
    	mPlayer = MediaPlayer.create(p_appContext,p_uri);
    }
    
    private void initEven()
    {     	
    	setSong(this, R.raw.testsong);
    	System.out.println("ARTISTE = " + MediaStore.Audio.Artists.ARTIST);
    	
    	//remplir TextView avec titre et auteur de la chanson
    	
    	
    	//----------------- evenements sur les boutons -----------------------------
    	
    	// Bouton dossier musiques
    	btnDossier = (ImageButton)findViewById(R.id.btnDossier);
    	btnDossier.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),MusicListActivity.class);
				startActivity(intent);
			}
		});
    	
    	// Bouton album
//    	btnAlbum = (ImageButton)findViewById(R.id.btnAlbum);
//    	btnAlbum.setOnTouchListener(new OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
////				Intent intent = new Intent(getApplicationContext(),MusicListActivity.class);
////				
////				Bundle bundle = new Bundle();
////				bundle.putInt(MediaMetadataRetriever.METADATA_KEY_ALBUM,mPlayer.);
////				startActivity(intent);
//			return true;
//			}
//		});
    	
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
        
        //tonalite
        buttonTonalite = (ImageButton) findViewById(R.id.tonalite);
        buttonTonalite.setOnClickListener(new OnClickListener() {
        	@Override public void onClick(View v) {
        		Intent intentTonalite = new Intent();
            	intentTonalite.setClassName("fr.unice.aap", "fr.unice.aap.EqualizerActivity");
            	startActivity(intentTonalite);
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
                	menuLoop();
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
        ((TextView)findViewById(R.id.duree)).setText(heureToString(mPlayer.getDuration()));
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
                        	((TextView)findViewById(R.id.position)).setText(heureToString(mPlayer.getCurrentPosition()));
                        	Log.i("INFO", mPlayer.getCurrentPosition()+" <=");
                        	//IMPLEMENTATION DE LA BOUCLE
                        	//=> pour l'instant, codé en dur 
                        	//(arrivé a la seconde 5 ou plus, la musique redemare à la seconde 1)
                        	//Afficher des "bares" deplacable pour faire ca dynamiquement !!
                        	if(mPlayer.getCurrentPosition() >= 5000){mPlayer.seekTo(1000);}
                        }
                        catch(Exception e)
                        {}
                        try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    }
                }
            }
        };
        thread_music = new Thread(_progressUpdater);
        thread_music.start();
    }

    private String heureToString(int ms)
    {
		int reste = ms/1000;
		String texte;	
		texte = (reste%60) + "";
		if(reste%60 < 10)
			texte = "0" + texte;
		reste = reste/60;
		texte = (reste%60) + ":" + texte;
		if(reste%60 < 10)
			texte = "0" + texte;
		reste = reste/60;
		if(reste%60 != 0){		
			texte = (reste%60) + ":" + texte;
			if(reste%60 < 10)
				texte = "0" + texte;
		}		
		return texte;
    }
    
    public void menuLoop(){
    	//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(this);
        final View alertDialogView = factory.inflate(R.layout.paramloop, null);
 
        //Création de l'AlertDialog
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
        //On affecte la vue personnalisé que l'on a crée à notre AlertDialog
        alert.setView(alertDialogView);
 
        //On donne un titre à l'AlertDialog
        alert.setTitle("personnaliser la boucle");
 
        //On modifie l'icône de l'AlertDialog pour le fun ;)
        alert.setIcon(android.R.drawable.btn_star);
 
        //On affecte un bouton "OK" à notre AlertDialog et on lui affecte un évènement
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {       
          } });
        
        alert.setNegativeButton("annuler", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {       
          } });
 
        alert.show();
    }
}