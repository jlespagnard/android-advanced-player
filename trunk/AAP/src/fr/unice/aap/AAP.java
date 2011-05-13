package fr.unice.aap;

import fr.unice.aap.musics.MusicListActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class AAP extends Activity {
	
	private static AAP activity = null;
	private ImageButton buttonPlayStop;
	private ImageButton buttonLoop;
	private SeekBar seekBar_Music;
	private SeekBar seekBar_debut;
	private SeekBar seekBar_fin;
	public static MediaPlayer mPlayer;
	private static Boolean isPlay = false;
	private Boolean isLoop = false;
	private Boolean onTouchSeekBarMusic = false;
	private Boolean down = false;
	private actionClicProlonge action;
	private Thread clicProlonge;
	private Handler mHandler = new Handler();
	private Intent musicList;
	
	private enum actionClicProlonge{
		debutmoins,
		debutplus,
		finmoins,
		finplus
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initEven();    
    }
    
    public static void setSong(Context p_appContext, int p_rawId) {
    	if(mPlayer != null) {
	    	mPlayer.stop();
	    	mPlayer.seekTo(0);
	    	mPlayer.release();
	    	activity.play();
    	}
    	mPlayer = MediaPlayer.create(p_appContext,p_rawId);
    }
    
    public static void setSong(Context p_appContext, Uri p_uri, String artiste, String chanson) {
    	Boolean pl = false;
    	if(mPlayer != null) {
    		if(isPlay){
    			pl = true;
    			activity.pause();  
    		}
            mPlayer.stop();
            mPlayer.seekTo(0);
            mPlayer.release();
    	}
    	mPlayer = MediaPlayer.create(p_appContext,p_uri); 
    	activity.initSeekBarMusic();
    	if(pl)
    		activity.play();
    	((TextView)activity.findViewById(R.id.titre)).setText(chanson + System.getProperty("line.separator") + artiste);
    }
    
    private void initEven()
    {   
    	activity = this;
    	setSong(this, R.raw.testsong);
    	musicList = new Intent(getApplicationContext(),MusicListActivity.class);
    	
    	//remplir TextView avec titre et auteur de la chanson   	
    	
    	//----------------- evenements sur les boutons -----------------------------   	
    	
    	buttonLoop = (ImageButton) findViewById(R.id.loop);
    	
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
						pause();
						break; 
					}
				}else{
					switch ( event.getAction() ) { 
					case MotionEvent.ACTION_DOWN:
						buttonPlayStop.setBackgroundResource(R.drawable.playclick);
						break; 
					case MotionEvent.ACTION_UP: 
						play();
						break; 
					}
				}						
				return true;
			}
		});           
        
        //seekbar musique
        seekBar_Music = (SeekBar) findViewById(R.id.seekbar_music);
        seekBar_debut = (SeekBar) findViewById(R.id.seekbar_debut);
        seekBar_fin = (SeekBar) findViewById(R.id.seekbar_fin);
        initSeekBarMusic();     
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
        
        //bouton debut
        ImageButton buttonloopdebut = (ImageButton) findViewById(R.id.loopdebut);
        buttonloopdebut.setOnClickListener(new OnClickListener() {
      	@Override public void onClick(View v) {
      		FrameLayout frame = (FrameLayout) findViewById(R.id.FrameLayout05);
      		if(frame.getVisibility() == FrameLayout.VISIBLE){
      			frame.setVisibility(FrameLayout.INVISIBLE);
      		}else{
      			frame.setVisibility(FrameLayout.VISIBLE);
      		}
      	}
      }); 
        
      //bouton fin
        ImageButton buttonloopfin = (ImageButton) findViewById(R.id.loopfin);
        buttonloopfin.setOnClickListener(new OnClickListener() {
      	@Override public void onClick(View v) {
      		FrameLayout frame = (FrameLayout) findViewById(R.id.FrameLayout06);
      		if(frame.getVisibility() == FrameLayout.VISIBLE){
      			frame.setVisibility(FrameLayout.INVISIBLE);
      		}else{
      			frame.setVisibility(FrameLayout.VISIBLE);
      		}
      	}
      }); 
        
        //moins debut
        ImageButton buttonmoinsdebut = (ImageButton) findViewById(R.id.debutmoins);      
        buttonmoinsdebut.setOnTouchListener( new OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {									
					if(event.getAction() == MotionEvent.ACTION_DOWN){
						down = true;
						action = actionClicProlonge.debutmoins;						
						threadClicProlonge();
						
					}
					if(event.getAction() == MotionEvent.ACTION_UP){
						down = false;
					}						
					return true;						
			}
		});         
        
        //plus debut
        ImageButton buttonplusdebut = (ImageButton) findViewById(R.id.debutplus);
        buttonplusdebut.setOnTouchListener( new OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {									
					if(event.getAction() == MotionEvent.ACTION_DOWN){
						down = true;
						action = actionClicProlonge.debutplus;						
						threadClicProlonge();
						
					}
					if(event.getAction() == MotionEvent.ACTION_UP){
						down = false;
					}						
					return true;						
			}
		}); 
        
        //moins fin
        ImageButton buttonmoinsfin = (ImageButton) findViewById(R.id.finmoins);
        buttonmoinsfin.setOnTouchListener( new OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {									
					if(event.getAction() == MotionEvent.ACTION_DOWN){
						down = true;
						action = actionClicProlonge.finmoins;					
						threadClicProlonge();
						
					}
					if(event.getAction() == MotionEvent.ACTION_UP){
						down = false;
					}						
					return true;						
			}
		}); 
        
        //plus fin
        ImageButton buttonplusfin = (ImageButton) findViewById(R.id.finplus);
        buttonplusfin.setOnTouchListener( new OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {									
					if(event.getAction() == MotionEvent.ACTION_DOWN){
						down = true;
						action = actionClicProlonge.finplus;						
						threadClicProlonge();
						
					}
					if(event.getAction() == MotionEvent.ACTION_UP){
						down = false;
					}						
					return true;						
			}
		}); 
        
        
      //bouton stop
        ImageButton buttonstop = (ImageButton) findViewById(R.id.stop);
        buttonstop.setOnClickListener(new OnClickListener() {
        	@Override public void onClick(View v) {
        		   pause();
        		   //definir position seekbar
        		   //premier click retour debut loop
        		   if(seekBar_Music.getProgress() > seekBar_debut.getProgress()){
        			   seekBar_Music.setProgress(seekBar_debut.getProgress());
        			   mPlayer.seekTo(seekBar_debut.getProgress());
        		   }
        		   //deuxieme click retour debut chanson
        		   else{
        			   mPlayer.seekTo(0);
        			   seekBar_Music.setProgress(0);
        		   }
        		   //textView position
        	    	((TextView)findViewById(R.id.position)).setText(heureToString(mPlayer.getCurrentPosition()));
        	}
        });
        
        //musique suivante
        ImageButton buttonsuivante = (ImageButton) findViewById(R.id.next);
        buttonsuivante.setOnClickListener(new OnClickListener() {
      	@Override public void onClick(View v) {
      		//TODO musique suivante
      	}
        }); 
        
        //musique precedente
        ImageButton buttonprecedente = (ImageButton) findViewById(R.id.previous);
        buttonprecedente.setOnClickListener(new OnClickListener() {
      	@Override public void onClick(View v) {
      		//TODO musique precedente
      	}
        }); 
        
        
        //ouvrir directement la liste des musiques
        Intent intent = new Intent(getApplicationContext(),MusicListActivity.class);
		startActivity(intent);
    }
    
    public void play(){
    	buttonPlayStop.setBackgroundResource(R.drawable.pause);
		try{
            mPlayer.start();
            
            mHandler.removeCallbacks(progressUpdater);
            mHandler.postDelayed(progressUpdater, 1000);
            
        }catch (IllegalStateException e) {
            mPlayer.pause();
            isPlay = false;
        }
		isPlay = true;
    }
    
    public void pause(){
    	buttonPlayStop.setBackgroundResource(R.drawable.play);
		mPlayer.pause();
		isPlay = false;
    }
    
    public void openDossierMusic(View v)
	{   		
		startActivity(musicList);
	}
    
    public void openEqualizer(View v){
    	Intent intentTonalite = new Intent();
    	intentTonalite.setClassName("fr.unice.aap", "fr.unice.aap.EqualizerActivity");
    	startActivity(intentTonalite);
    }
    
    public void onOffLoop(View v){
    	if (isLoop) {
            buttonLoop.setBackgroundResource(R.drawable.loop);  
            isLoop = false;
        }else {
        	buttonLoop.setBackgroundResource(R.drawable.loopclick);  
        	isLoop = true;
        }
    }
    
    private void threadClicProlonge() {

        Runnable _progress = new Runnable() {
            @Override
            public void run() {
            	while(down)
            	{   
            		try {
	            		if(action == actionClicProlonge.debutmoins) {
							if(seekBar_debut.getProgress()>0){
			        			seekBar_debut.setProgress(seekBar_debut.getProgress()-1000);			      			
			        		} 
	            		}else if(action == actionClicProlonge.debutplus){
	            			if(seekBar_debut.getProgress()<seekBar_debut.getMax() && seekBar_debut.getProgress()<seekBar_fin.getProgress()){
	                			seekBar_debut.setProgress(seekBar_debut.getProgress()+1000); 
	                		}
	            		}else if(action == actionClicProlonge.finmoins){
	            			if(seekBar_fin.getProgress()>seekBar_debut.getProgress() && seekBar_fin.getProgress()>0){
	                			seekBar_fin.setProgress(seekBar_fin.getProgress()-1000);               			
	                		} 
	            		}else if(action == actionClicProlonge.finplus){
	            			if(seekBar_fin.getProgress()<seekBar_fin.getMax()){
	                			seekBar_fin.setProgress(seekBar_fin.getProgress()+1000);                			
	                		}
	            		}           			            		
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
                }
            }
        };        
        clicProlonge = new Thread(_progress);
        clicProlonge.start();
    }

        private Runnable progressUpdater = new Runnable() {
            @Override
            public void run() {
                    if(isPlay && !onTouchSeekBarMusic) {
                    	seekBar_Music.setProgress(mPlayer.getCurrentPosition()); 
                    	((TextView)findViewById(R.id.position)).setText(heureToString(mPlayer.getCurrentPosition()));
                    	//IMPLEMENTATION DE LA BOUCLE
                    	//=> pour l'instant, codé en dur 
                    	//(arrivé a la seconde 5 ou plus, la musique redemare à la seconde 1)
                    	if(isLoop){
                    		if(mPlayer.getCurrentPosition() >= 5000){mPlayer.seekTo(0);}
                    	}                                       	
						mHandler.postDelayed(this, 1000);
                    }
            }
        };

    private void initSeekBarMusic()
    {
    	//seekbar_music
    	seekBar_Music.setMax(mPlayer.getDuration()); 
    	seekBar_Music.setProgress(0);              
    	//seekbar_debut
    	seekBar_debut.setMax(mPlayer.getDuration());
    	seekBar_debut.setProgress(0);
    	//seekbar_fin
    	seekBar_fin.setMax(mPlayer.getDuration());
    	seekBar_fin.setProgress(seekBar_fin.getMax());
    	//textView position
    	((TextView)findViewById(R.id.position)).setText("00:00");
    	//textView duree
        ((TextView)findViewById(R.id.duree)).setText(heureToString(mPlayer.getDuration()));
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

    /* Creates the menu items 
     * quand on clique sur le bouton menu du telephone*/
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Quit").setIcon(R.drawable.quit);
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case 1:
            finish();
            System.exit(0);
            return true;
            default :
            	return false;
        }
    }
}