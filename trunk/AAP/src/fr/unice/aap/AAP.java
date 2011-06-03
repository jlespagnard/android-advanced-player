package fr.unice.aap;


import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;


import fr.unice.aap.musics.AllSongsListActivity;
import fr.unice.aap.musics.MusicListActivity;
import fr.unice.loop.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TextView;

public class AAP extends Activity {
	
	/* ************************** attributs ******************* */
	
	/* notre fenetre principale */
	private static AAP activity = null;
	/* view de l'interface */
	private ImageButton buttonPlayStop;
	private ImageButton buttonLoop;
	private SeekBar seekBar_Music;
	private SeekBar seekBar_debut;
	private SeekBar seekBar_fin;
	private SeekBar seekBar_reglageLoop;
	private Intent musicList;
	public static AllSongsListActivity AllSongList = null;		
	private Intent intentTonalite = new Intent();
	public static EqualizerActivity equalizerActivity;
	public static String currentArtist = null;
	public static String currentSong = null;
	public String urlLyrics = null;
	/* objet contenant la musique */
	public static MediaPlayer mPlayer;
	/* differents controles */
	private static Boolean isPlay = false;
	private Boolean isLoop = false;
	private Boolean onTouchSeekBarMusic = false;
	public boolean btLoopDebutOn = false;
	public boolean btLoopFinOn = false;
	public boolean editLoop = false;
	public boolean record = false;
	/* permet de gerer la thread qui synchronise la musique avec la seekBar */
	private Handler mHandler = new Handler();
	
	
    /* *********************** Called when the activity is first created. *************** */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initEven();    
    }
    
    /* ************************ modifier la musique ************************ */
    public static void setSong(Context p_appContext, int p_rawId) {

//    	if(mPlayer != null) {
//	    	mPlayer.stop();
//	    	mPlayer.seekTo(0);
//	    	mPlayer.release();
//	    	activity.play();
//    	}

    	mPlayer = MediaPlayer.create(p_appContext,p_rawId);
    	activity.initSeekBarMusic();
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
    	
    	currentArtist = artiste;
    	currentSong = chanson;
    	
    	mPlayer = MediaPlayer.create(p_appContext,p_uri); 
    	activity.initSeekBarMusic();
    	if(pl)
    		activity.play();
    	
    	((TextView)activity.findViewById(R.id.titre)).setText(chanson + System.getProperty("line.separator") + artiste);
    }
    
    /* ************ sauvegarde etat de l'appli avant changement orientation *********** */
    @Override
    public Object onRetainNonConfigurationInstance() {
    	if(mPlayer.getCurrentPosition() == 0){
    		mPlayer.start();
    		mPlayer.pause();
    	}
        final Object[] data = new Object[10];
        data[0] = mPlayer; //musique en cours
        data[1] = isPlay; //etat pause / play
        data[2] = isLoop; //loop select
        data[3] = AllSongList; //liste des musiques (pour suivante et precedente)
        data[4] = currentArtist; //artiste en cours
        data[5] = currentSong; //titre de la chanson en cours
        data[6] = seekBar_debut.getProgress();
        data[7] = seekBar_fin.getProgress();
        data[8] = ((ScrollView) findViewById(R.id.FrameLayout07)).getVisibility(); //parole ouvert
        data[9] = ((TableLayout) findViewById(R.id.FrameLayout08)).getVisibility(); //paroles web ouvert
        return data;
    }
    
    /* **************** initialisation des elements de l'application **************** */
    private void initEven()
    {   
    	activity = this;   	
    	musicList = new Intent(getApplicationContext(),MusicListActivity.class);	
    	
    	//----------------- evenements sur les boutons -----------------------------   	
    	
    	//bouton de la boucle
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
        seekBar_reglageLoop = (SeekBar) findViewById(R.id.seekbar_reglageloop);              
        seekBar_Music.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
       	 
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            	onTouchSeekBarMusic = false;
            	mPlayer.seekTo(seekBar_Music.getProgress());
            	mHandler.removeCallbacks(progressUpdater);
                mHandler.postDelayed(progressUpdater, 0);
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
          
        seekBar_reglageLoop.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
       	 
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
 
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
 
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {           	           	
            	if(btLoopDebutOn){
            		if(progress<seekBar_fin.getProgress()){
            			seekBar_debut.setProgress(progress);
            			((TextView)findViewById(R.id.positiondebut)).setText(heureToString(progress));
            		}else{
            			seekBar.setProgress(seekBar_fin.getProgress()); 
            			((TextView)findViewById(R.id.positiondebut)).setText(seekBar_fin.getProgress());
            		}
            	}else if(btLoopFinOn){
            		if(progress>seekBar_debut.getProgress()){
            			seekBar_fin.setProgress(progress);
            			((TextView)findViewById(R.id.positionfin)).setText(heureToString(progress));
            		}else{
            			seekBar.setProgress(seekBar_debut.getProgress()); 
            			((TextView)findViewById(R.id.positionfin)).setText(heureToString(seekBar_debut.getProgress()));
            		}
            	}
            }
        });             
		
        //ouverture des fonctionnalites
        ((TextView)findViewById(R.id.fonctionnalites)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				 fermerParoles();
				animFonctionnalites(false);
			}
		});
        
        
        //recuperation de la sauvegarde en cas de changement de configuration
        // paysage / paysage
        final Object[] data = (Object[]) getLastNonConfigurationInstance();
        if (data == null) {
        	setSong(this, R.raw.testsong);
            //ouvrir directement la liste des musiques        
    		startActivity(musicList);
        }else{
        	restauration(data);
        }
    }
    
    public void restauration(Object[] data){
    	//restauration
    	mPlayer = (MediaPlayer) data[0];
    	AllSongList = (AllSongsListActivity) data[3];
    	currentArtist = (String) data[4];
    	currentSong = (String) data[5];
    	((TextView)activity.findViewById(R.id.titre)).setText(currentSong + System.getProperty("line.separator") + currentArtist);
    	
    	//initseekbar
    	//seekbar_music
    	seekBar_Music.setMax(mPlayer.getDuration()); 
    	seekBar_Music.setProgress(mPlayer.getCurrentPosition());              
    	//seekbar_debut
    	seekBar_debut.setMax(mPlayer.getDuration());
    	seekBar_debut.setProgress((Integer) data[6]);
    	//seekbar_fin
    	seekBar_fin.setMax(mPlayer.getDuration());
    	seekBar_fin.setProgress((Integer) data[7]);
    	//seekBar_reglageLoop
    	seekBar_reglageLoop.setMax(mPlayer.getDuration());
    	seekBar_reglageLoop.setProgress(0);
    	//textView position
    	((TextView)findViewById(R.id.position)).setText(heureToString(mPlayer.getCurrentPosition()));
    	//textView duree
        ((TextView)findViewById(R.id.duree)).setText(heureToString(mPlayer.getDuration()));
        //textView position debut
    	((TextView)findViewById(R.id.positiondebut)).setText(heureToString(seekBar_debut.getProgress()));
    	//textView position fin
        ((TextView)findViewById(R.id.positionfin)).setText(heureToString(seekBar_fin.getProgress()));
    	
    	//musique en cours d'écoute : play/pause
    	isPlay = (Boolean) data[1];
    	if(isPlay)
    		play();
    	else
    		pause();
    	//loop selectionne
    	isLoop = (Boolean) data[2];
    	if(isLoop)
    		buttonLoop.setBackgroundResource(R.drawable.loopclick); 
    	else
    		buttonLoop.setBackgroundResource(R.drawable.loop); 
    	
    	//fenetre paroles
    	if((Integer)data[8] == ScrollView.VISIBLE){
    		findLyrics();
    	}
    	
    	//fenetre paroles web
    	if((Integer)data[9] == FrameLayout.VISIBLE){
    		openBrowser();
    	}
    }
    
    public void fermerParoles(){
    	ScrollView fenetreParoles = (ScrollView) findViewById(R.id.FrameLayout07);
		if(fenetreParoles.getVisibility() == ScrollView.VISIBLE){
			//animation pour la fermeture de la fenetre des paroles
	    	Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animalphaout);       
	    	fenetreParoles.startAnimation(a);
	    	fenetreParoles.setVisibility(FrameLayout.INVISIBLE);
		}
    }
    
    /* ************** ouverture/fermeture de la fenetre fonctionnalites ************** */
    public void animFonctionnalites(Boolean close){
    	RelativeLayout frame = (RelativeLayout) findViewById(R.id.RelativeLayout04);
    	if(frame.getVisibility() == RelativeLayout.INVISIBLE){	
    		if(!close) {
	            Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animframein);
	            frame.startAnimation(a);
	            frame.setVisibility(RelativeLayout.VISIBLE);
    		}
    	}
    	else{
    		Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animframeout);
            frame.startAnimation(a);
    		frame.setVisibility(RelativeLayout.INVISIBLE); 
    	}
    }
    
    /* ******************** cacher fenetre de fonctionnalites */
    public void retour(View v){
    	animFonctionnalites(true);
    }
    
    /* ********************** play musique **************** */
    public void play(){
    	buttonPlayStop.setBackgroundResource(R.drawable.pause);
		try{
            mPlayer.start();
            
            //supprime tout ce qu'il y avait dans la file d'attente
            mHandler.removeCallbacks(progressUpdater);
            //ajoute dans la file d'attente la thread
            mHandler.postDelayed(progressUpdater, 0);
            
        }catch (IllegalStateException e) {
            mPlayer.pause();
            isPlay = false;
        }
		isPlay = true;
    }
    
    /* *********************** pause musique ****************** */
    public void pause(){
    	buttonPlayStop.setBackgroundResource(R.drawable.play);
		mPlayer.pause();
		isPlay = false;
    }
    
    /* ************************ chargement d'une sauvegarde ******************* */
    public void chargerSauv(View v){
    	//ouverture d'un menu contextuel avec la liste des sauv disponibles pour cette chanson
    	registerForContextMenu(v);
    	v.showContextMenu();
    }
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Chargement");
		menu.setHeaderIcon(R.drawable.uploadclick);
		menu.add(0, 0, 0, "annuler");
		TextView t = (TextView)activity.findViewById(R.id.titre);
		ArrayList<Loop> lstLoop = ExtractLoopConf.getLoops(t.getText().toString());
		for(int k=0;k<lstLoop.size();k++)
		{
			String hhmmssDeb = millisToDate(lstLoop.get(k).getDebutLoop());
			String hhmmssFin = millisToDate(lstLoop.get(k).getFinLoop());
		
			menu.add(0,k,0,hhmmssDeb+"-"+hhmmssFin);
		}
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		if(item.getTitle().equals("annuler"))
		{
			return true;
		}
		else
		{
			String[] tabLoop = item.getTitle().toString().split("-");
			Log.i("INFO","tabLoop"+tabLoop[0]+"-"+tabLoop[1]);
			String[] tabDeb = tabLoop[0].split(":");
			String hDeb = tabDeb[0];
			String mDeb = tabDeb[1];
			String sDeb = tabDeb[2];
			
			String[] tabFin = tabLoop[1].split(":");
			String hFin = tabFin[0];
			String mFin = tabFin[1];
			String sFin = tabFin[2];
			
			seekBar_debut.setProgress(Integer.parseInt(String.valueOf(TimeUnit.HOURS.toMillis(Long.parseLong(hDeb))+TimeUnit.MINUTES.toMillis(Long.parseLong(mDeb))+TimeUnit.SECONDS.toMillis(Long.parseLong(sDeb)))));
			seekBar_fin.setProgress(Integer.parseInt(String.valueOf(TimeUnit.HOURS.toMillis(Long.parseLong(hFin))+TimeUnit.MINUTES.toMillis(Long.parseLong(mFin))+TimeUnit.SECONDS.toMillis(Long.parseLong(sFin)))));
			return true;
		}
	}
    
	/* **************** click bouton enregistrer sauvegarde loop *************** */
	public void enregistrer(View v){
		Log.i("INFO","debut: "+seekBar_debut.getProgress());
		Log.i("INFO","fin: "+seekBar_fin.getProgress());
		TextView t = (TextView)activity.findViewById(R.id.titre);
		Log.i("INFO","chanson: "+t.getText().toString());
		ExtractLoopConf.addLoop(new Loop("test",seekBar_debut.getProgress(),seekBar_fin.getProgress(),t.getText().toString()));
		
		//ouvrir fenetre info
		TextView fenetre = (TextView)findViewById(R.id.infoEnregistrement);
		fenetre.setVisibility(TextView.VISIBLE);
		Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animalphainfo);
		fenetre.startAnimation(a);			
		fenetre.setVisibility(TextView.INVISIBLE);
	}
	
	/* ********************* click bouton ouvrir repertoire Music de la sccard ********* */
    public void openDossierMusic(View v)
	{   
    	animFonctionnalites(true);
    	fermerParoles();
		startActivity(musicList);
	}
    
    /* ************************* musique suivante ***************** */
    public void musiqueSuivante(View v) {  	
  		if(AllSongList != null){
  			animFonctionnalites(true);
  			if(equalizerActivity!=null)
  				equalizerActivity.resetEqualizer();
  			AllSongList.nextSong();
  		}
  	}           
    
    /* ********************** musique precedente ******************* */
    public void musiquePrecedente(View v) {   	
  		if(AllSongList != null){
  			animFonctionnalites(true);
  			if(equalizerActivity!=null)
  				equalizerActivity.resetEqualizer();
  			AllSongList.previousSong();
  		}
  	}      
  
    /* ********************* enregistrement sonore *************** */
    public void recordSon(View v){
    	ImageButton buttonRecord = (ImageButton) findViewById(R.id.rec);
    	if (record) {
    		buttonRecord.setBackgroundResource(R.drawable.rec);  
    		record = false;
    		//arret enregistrement sonore
        }else {
        	buttonRecord.setBackgroundResource(R.drawable.recclick);  
        	record = true;
        	//demarrage enregistrement sonore
        }
    }
    
    
    /* ******************* edition de la boucle ************************************* */
    public void editLoop(View v){
    	ImageButton bouton = (ImageButton) findViewById(R.id.editLoop);
    	if (editLoop) {
    		bouton.setBackgroundResource(R.drawable.edit);      		
    		btLoopFinOn = false;
    		btLoopDebutOn = false;
    		ImageButton boutondebut = (ImageButton) findViewById(R.id.loopdebut);
    		ImageButton boutonfin = (ImageButton) findViewById(R.id.loopfin);
  			boutondebut.setBackgroundResource(R.drawable.boutondebut);
  			boutonfin.setBackgroundResource(R.drawable.boutonfin);
  			FrameLayout frame = (FrameLayout) findViewById(R.id.FrameLayout05);
  			if(frame.getVisibility() == FrameLayout.VISIBLE){
	  			Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animreglageloopout);
	            frame.startAnimation(a);
	  			frame.setVisibility(FrameLayout.INVISIBLE);
  			}  		
            editLoop = false;
        }else {
        	bouton.setBackgroundResource(R.drawable.editclick);  
        	editLoop = true;
        }
    }
    
    /* ********************** click bouton reglage du debut de la boucle *********** */
    public void loopDebut(View v) {
    	if(editLoop){
	  		ImageButton bouton = (ImageButton) findViewById(R.id.loopdebut);
	  		FrameLayout frame = (FrameLayout) findViewById(R.id.FrameLayout05);
	  		if(!btLoopDebutOn){
	  			//reglage debut
	  			btLoopDebutOn = true;
	  			bouton.setBackgroundResource(R.drawable.boutondebutclick);
	  			if(btLoopFinOn){
	  				btLoopFinOn = false;
	  				((ImageButton)findViewById(R.id.loopfin)).setBackgroundResource(R.drawable.boutonfin);
	  			}
	  			seekBar_reglageLoop.setProgress(seekBar_debut.getProgress());
	  			if(frame.getVisibility() == FrameLayout.INVISIBLE)
	  			{
	  				Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animreglageloopin);
		            frame.startAnimation(a);
		            frame.setVisibility(FrameLayout.VISIBLE);
	  			}              
	  		}else{
	  			btLoopDebutOn = false;
	  			bouton.setBackgroundResource(R.drawable.boutondebut);
	  			Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animreglageloopout);
	            frame.startAnimation(a);
	  			frame.setVisibility(FrameLayout.INVISIBLE);
	  		} 
    	}else{  		   		
    		if(mPlayer.getCurrentPosition()<seekBar_fin.getProgress()){
    			seekBar_debut.setProgress(mPlayer.getCurrentPosition());
    		}else{
    			seekBar_debut.setProgress(seekBar_fin.getProgress()); 
    		}
    	}
  	} 
    
    /* ********************** click bouton reglage de la fin de la boucle *********** */
    public void loopFin(View v) {
    	
    	if(editLoop){
	    	ImageButton bouton = (ImageButton) findViewById(R.id.loopfin);
	    	FrameLayout frame = (FrameLayout) findViewById(R.id.FrameLayout05);
	  		if(!btLoopFinOn){
	  			btLoopFinOn = true;
	  			bouton.setBackgroundResource(R.drawable.boutonfinclick);
	  			if(btLoopDebutOn){
	  				btLoopDebutOn = false;
	  				((ImageButton)findViewById(R.id.loopdebut)).setBackgroundResource(R.drawable.boutondebut);
	  			}
	  			seekBar_reglageLoop.setProgress(seekBar_fin.getProgress());
	  			if(frame.getVisibility() == FrameLayout.INVISIBLE){
	  				Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animreglageloopin);
		            frame.startAnimation(a);
	                frame.setVisibility(FrameLayout.VISIBLE);
	  			}
	  		}else{
	  			btLoopFinOn = false;
	  			bouton.setBackgroundResource(R.drawable.boutonfin);
	  			Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animreglageloopout);
	            frame.startAnimation(a);
	  			frame.setVisibility(FrameLayout.INVISIBLE);
	  		}
    	}else{
    		
    		if(mPlayer.getCurrentPosition()>seekBar_debut.getProgress()){
    			seekBar_fin.setProgress(mPlayer.getCurrentPosition());
    		}else{
    			seekBar_fin.setProgress(seekBar_debut.getProgress()); 
    		}    		
    	}
  	}
    
    /* ******************* click bouton stop musique **************** */
    public void stop(View v) {
		   pause();
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
    
    
    /* ******************** click bouton ouverture de l'equalizer ************* */
    public void openEqualizer(View v){
    	animFonctionnalites(true);
    	intentTonalite.setClassName("fr.unice.aap", "fr.unice.aap.EqualizerActivity");
    	startActivity(intentTonalite);
    }
    
    /* ****************** activation/desactivation de la boucle ************ */
    public void onOffLoop(View v){
    	if (isLoop) {
            buttonLoop.setBackgroundResource(R.drawable.loop);  
            isLoop = false;
        }else {
        	buttonLoop.setBackgroundResource(R.drawable.loopclick);  
        	isLoop = true;
        }
    }
  	
    /* ****************** affichage des paroles ****************** */
    public void findLyrics(View v){
    	findLyrics();
    }
    
    public void findLyrics(){
    	// On enlève le menu 
    	animFonctionnalites(true);
    	//animation pour l'ouverture de la fenetre des paroles
    	Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animalphain);       
    	ScrollView fenetreParoles = (ScrollView) findViewById(R.id.FrameLayout07);
    	fenetreParoles.startAnimation(a);
    	fenetreParoles.setVisibility(ScrollView.VISIBLE);
    	
        ((TableLayout) findViewById(R.id.FrameLayout06)).setVisibility(FrameLayout.VISIBLE);
        ((TextView) findViewById(R.id.lyricsTextView)).setText("Bienvenue sur la recherche de paroles.\nUne connexion internet est nécessaire.");
        ((Button)findViewById(R.id.lyricsBrowserButton)).setVisibility(FrameLayout.INVISIBLE);
	    //On initialise les deux champs pour la recherche manuelle
    	((EditText) findViewById(R.id.ArtistName)).setText(currentArtist);
    	((EditText) findViewById(R.id.SongName)).setText(currentSong);
        //On affiche le résultat
    	setLyricsTextResult(currentArtist, currentSong, false);
    }
    
    public void setLyricsTextResult(String artist, String song, boolean estRetour){
    	try{
    		TableLayout webBrowser = (TableLayout) findViewById(R.id.FrameLayout08);
    		if(webBrowser.getVisibility() == FrameLayout.VISIBLE)
    			webBrowser.setVisibility(FrameLayout.INVISIBLE);
    		TableLayout lyricsSearch = (TableLayout) findViewById(R.id.FrameLayout06);
    		if(lyricsSearch.getVisibility() == FrameLayout.INVISIBLE)
    			lyricsSearch.setVisibility(FrameLayout.VISIBLE);
    		boolean artistAlone = false;
    		
    		TextView lyricsTextView = (TextView) findViewById(R.id.lyricsTextView);

    		// L'url "path" permet d'interroger le serveur pour récupérer les paroles de l'artiste et de la chanson passés en paramètre
    		//On demande le format de retour en xml qu'on parsera
    		String path = "http://lyrics.wikia.com/api.php?";
    		artist = formatString(artist);
    		song = formatString(song);
    		if (artist != null && artist.length() > 0){
    			path += "artist=" + artist;
    			artistAlone = true;
    		}
    		if (song != null && song.length() > 0){
    			if(artistAlone)
    				path+="&";
    			path+= "song=" + song;
    			artistAlone = false;
    		}
    		if(artistAlone == true && estRetour == false){
    			urlLyrics = path;
    			openBrowser();
    		} else {
    			path += "&fmt=xml";
	    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    		DocumentBuilder db = dbf.newDocumentBuilder();
	    		Document doc = db.parse(path);
	    		String lyrics = "";
	    		lyrics = doc.getElementsByTagName("lyrics").item(0).getTextContent();
	    		urlLyrics = doc.getElementsByTagName("url").item(0).getTextContent();
	    		
	    		lyricsTextView.setText(lyrics);
	    		//Si les paroles sont disponibles, on affiche un bouton pour afficher la page web qui les contient
	    		if(lyrics.length() > 1 && !lyrics.equalsIgnoreCase("Not Found")) {
	    			((Button)findViewById(R.id.lyricsBrowserButton)).setVisibility(FrameLayout.VISIBLE);
	    		} else {
	    			lyricsTextView.setText("Not Found");
	    			((Button)findViewById(R.id.lyricsBrowserButton)).setVisibility(FrameLayout.INVISIBLE);
	    		}
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public void updateLyricSearch(View v){
    	String artist = ((EditText) findViewById(R.id.ArtistName)).getText().toString();
    	String song = ((EditText) findViewById(R.id.SongName)).getText().toString();
    	setLyricsTextResult(artist, song, false);
    }
    
    public void updateLyricSearchButtonReturn(View v){
    	String artist = ((EditText) findViewById(R.id.ArtistName)).getText().toString();
    	String song = ((EditText) findViewById(R.id.SongName)).getText().toString();
    	setLyricsTextResult(artist, song, true);
    }
    
    public void openBrowser(){
    	((TableLayout) findViewById(R.id.FrameLayout06)).setVisibility(FrameLayout.INVISIBLE);
    	((TableLayout) findViewById(R.id.FrameLayout08)).setVisibility(FrameLayout.VISIBLE);
    	WebView lyricsView = (WebView)findViewById(R.id.lyricsWebView);
    	lyricsView.getSettings().setJavaScriptEnabled(true);
    	lyricsView.loadUrl(urlLyrics);
    	lyricsView.setWebViewClient(new WebViewClient());
    }
    
    public void openBrowser(View v){
    	openBrowser();
    }
    
    public String formatString(String s){
    	return s.replace(" ", "%20");
    }
    
    //On met toutes les view concernées en invisible
    public void closeLyricViews(View v){
    	fermerParoles();
    }
    
    /* ************ thread qui synchronyse la musique et la seekbar et controle la boucle ***** */
    private Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {
                if(isPlay && !onTouchSeekBarMusic) {
                	seekBar_Music.setProgress(mPlayer.getCurrentPosition()); 
                	((TextView)findViewById(R.id.position)).setText(heureToString(mPlayer.getCurrentPosition()));
                	//Log.i("Info", )
                	if(isLoop){
                		if(mPlayer.getCurrentPosition() >= seekBar_fin.getProgress()){
                			mPlayer.seekTo(seekBar_debut.getProgress());
                			}
                	}                	               	
                	
					mHandler.postDelayed(this, 1000);
                }
        }
    };

    /* ******* initialisation des differentes seekbar et text de position, duree, .. musique *** */
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
    	//seekBar_reglageLoop
    	seekBar_reglageLoop.setMax(mPlayer.getDuration());
    	seekBar_reglageLoop.setProgress(0);
    	//textView position
    	((TextView)findViewById(R.id.position)).setText("00:00");
    	//textView duree
        ((TextView)findViewById(R.id.duree)).setText(heureToString(mPlayer.getDuration()));
        //textView position debut
    	((TextView)findViewById(R.id.positiondebut)).setText("00:00");
    	//textView position fin
        ((TextView)findViewById(R.id.positionfin)).setText(heureToString(mPlayer.getDuration()));
    }
    
    /* ************* convertisseur milliseconde to string 00:00:00 ********** */
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

    /* *** menu items correspondant a celui qu'on affiche quand on clique sur le bouton menu du telephone **** */
    /* Creates the menu items */
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
    /* *** fonction de conversion *** */
    /* Conversion de milli-secondes en HH-MM-SS */
    public String millisToDate(long millis)
    {
    	long timeMillis = millis;
		long time = timeMillis / 1000;
		String seconds = Integer.toString((int)(time % 60));
		String minutes = Integer.toString((int)((time % 3600) / 60));
		String hours = Integer.toString((int)(time / 3600));
		for (int i = 0; i < 2; i++) {
		if (seconds.length() < 2) {
		seconds = "0" + seconds;
		}
		if (minutes.length() < 2) {
		minutes = "0" + minutes;
		}
		if (hours.length() < 2) {
		hours = "0" + hours;
		}
		}
		return hours+":"+minutes+":"+seconds;
    }
}