package fr.unice.aap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import fr.unice.loop.*;
import fr.unice.aap.musics.AllSongsListActivity;
import fr.unice.aap.musics.MusicListActivity;
import fr.unice.aap.recorder.AudioRecorder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * <p>
 * Activité liée à la fenêtre principale de notre application. Elle contient toutes les actions déclenchées par les éléments de l'interface
 * </p>
 * 
 * @author Julien LESPAGNARD
 * @author Anthony BONIN
 * @author Michel CARTIER
 * @author Élodie MAZUEL
 */
public class AAP extends Activity {
	
	// Attributs  
	
	/**
	 * L'activité de la fenêtre principale
	 */
	private static AAP activity = null;
	
	//View de l'interface
	
	/**
	 * Bouton play/pause
	 */
	private ImageButton buttonPlayStop;
	/**
	 * Bouton activer/desactiver boucle
	 */
	private ImageButton buttonLoop;
	/**
	 * Barre de progression de la musique
	 */
	private SeekBar seekBar_Music;
	/**
	 * Barre de progression de la valeur de début de la boucle
	 */
	private SeekBar seekBar_debut;
	/**
	 * Barre de progression de la valeur de fin de la boucle
	 */
	private SeekBar seekBar_fin;
	/**
	 * Barre de progression qui permet de régler les valeurs de debut et de fin de la boucle
	 */
	private SeekBar seekBar_reglageLoop;
	/**
	 * Accès à liste des musiques de la sdcard
	 */
	private Intent musicList;
	/**
	 * Elément permettant d'accéder à la liste des musiques pour pouvoir passer aux musiques suivantes et précédentes
	 */
	public static AllSongsListActivity AllSongList = null;		
	/**
	 * Accès à l'égaliseur
	 */
	private Intent intentTonalite = new Intent();
	/**
	 * Accès à l'explorateur de fichiers de la sdcard
	 */
	private Intent intentFileExplorer;
	/**
	 * Activité equaliseur
	 */
	public static EqualizerActivity equalizerActivity;
	/**
	 * Objet contenant la musique
	 */
	public static MediaPlayer mPlayer;
	
	// differents controles
	
	/**
	 * Musique en mode play ou pause
	 */
	private static Boolean isPlay = false;
	/**
	 * Mode boucle enclenchée
	 */
	private Boolean isLoop = false;
	/**
	 * La barre de progression de la musique est en train d'être bougée par l'utilisateur
	 */
	private Boolean onTouchSeekBarMusic = false;
	/**
	 * Réglage manuel de la valeur de début de la boucle activée
	 */
	public boolean btLoopDebutOn = false;
	/**
	 * Réglage manuel de la valeur de fin de la boucle activée
	 */
	public boolean btLoopFinOn = false;
	/**
	 * Mode réglage manuel de la boucle activée
	 */
	public boolean editLoop = false;
	/**
	 * En cours d'enregistrement sonore
	 */
	public boolean isRecording = false;
	/**
	 *  Permet de gérer la thread qui synchronise la musique avec la barre de progression de la musique
	 */
	private Handler mHandler = new Handler();
	
	// Variables utilisées pour les paroles
	
	/**
	 * Nom de l'artiste de la musique en cours d'écoute
	 */
	public static String currentArtist = null;
	/**
	 * Titre de la chanson de la musique en cours d'écoute
	 */
	public static String currentSong = null;
	/**
	 * Url permettant d'accéder aux paroles
	 */
	public String urlLyrics = null;
	/**
	 * Objet AudioRecorder pour enregistrer le son
	 */
	public AudioRecorder audioRecorder = null;
	/**
	 * Titre donné à l'enregistrement sonore
	 */
	public String recordTitle = null;
	/**
	 * Liste des répertoires contenant les musiques
	 */
	private static List<String> listeRepertoireMusique = null;
	/**
	 * Contexte de l'application
	 */
	public static Context appContext;
	/**
	 * Zone de texte de l'application contenant la liste des répertoires contenant les musiques
	 */
	private static TextView txtListeRepertoireMusique;
	
    //appellé quand l'application se crée
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initVars();
        initEven(); 
        ExtractLoopConf.verifXML();
    }

    private void initVars() {
    	appContext = this;   	
    	retrievePreferences();
	}

	/**
	 * Modification de la musique à écouter
	 * @param p_appContext	Contexte de l'application
	 * @param p_rawId		Id de la musique
	 */
    public static void setSong(Context p_appContext, int p_rawId) {

    	mPlayer = MediaPlayer.create(p_appContext,p_rawId);
    	activity.initSeekBarMusic();
    }
    
    /**
     * Modification de la musique à écouter
     * @param p_appContext	Contexte de l'application
     * @param p_uri			Url de la musique
     * @param artiste		Nom de l'artiste
     * @param chanson		Titre de la chanson
     */
    public static void setSong(Context p_appContext, Uri p_uri, String artiste, String chanson) {
    	if(mPlayer != null) {
    		if(isPlay){
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
    	//Mise en route direct de la musique
    	activity.play();
    	
    	((TextView)activity.findViewById(R.id.titre)).setText(chanson + System.getProperty("line.separator") + artiste);
    }
   
    // Sauvegarde de l'état de l'application avant le changement de configuration (orientation)
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
        data[6] = seekBar_debut.getProgress(); //valeur debut de la boucle
        data[7] = seekBar_fin.getProgress(); //valeur fin de la boucle
        data[8] = ((ScrollView) findViewById(R.id.LyricsView)).getVisibility(); //parole ouvert
        data[9] = ((TableLayout) findViewById(R.id.FrameLayout08)).getVisibility(); //paroles web ouvert
        return data;
    }
    
    /** 
     * Initialisation des éléments de l'application 
     */
    private void initEven()
    {   
    	activity = this;   	
    	musicList = new Intent(getApplicationContext(),MusicListActivity.class);
    	intentFileExplorer = new Intent(getApplicationContext(),FileExplorerActivity.class);
    	
    	//---------------- Evènements sur les boutons -----------------------------   	
    	
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
            			((TextView)findViewById(R.id.positiondebut)).setText(heureToString(seekBar_fin.getProgress()));
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
		
        //Ouverture de la fenêtre des fonctionnalités
        ((TextView)findViewById(R.id.fonctionnalites)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				 fermerParoles();
				 fermerRecord();
				animFonctionnalites(false);
			}
		});
               
        //Récupération de la sauvegarde en cas de changement de configuration
        final Object[] data = (Object[]) getLastNonConfigurationInstance();
        if (data == null) {
        	setSong(this, R.raw.testsong);
            //Ouvrir directement la liste des musiques        
    		startActivity(musicList);
        }else{
        	restauration(data);
        }
    }
    
    /**
     * Restauration de l'état de l'application après le changement de configuration
     * @param data	Objet contenant les éléments sauvegardés
     */
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
    
    /**
     * Fermer la fenêtre contenant les paroles
     */
    public void fermerParoles(){
    	ScrollView fenetreParoles = (ScrollView) findViewById(R.id.LyricsView);
		if(fenetreParoles.getVisibility() == ScrollView.VISIBLE){
			//animation pour la fermeture de la fenetre des paroles
	    	Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animalphaout);       
	    	fenetreParoles.startAnimation(a);
	    	fenetreParoles.setVisibility(FrameLayout.INVISIBLE);
		}
    }
    
    /**
     * Fermer la fenêtre contenant l'enregistrement sonore
     */
    public void fermerRecord(){  	      
    	LinearLayout recordLayout =  (LinearLayout)findViewById(R.id.RecordLayout);  
    	if(recordLayout.getVisibility() == LinearLayout.VISIBLE){
	    	Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animalphaout); 
	    	recordLayout.startAnimation(a);
	    	recordLayout.setVisibility(FrameLayout.INVISIBLE);
    	}
    }
    
    /**
     * Ouverture/fermeture de la fenêtre des fonctionnalités
     * @param close		Imposer la fermeture 
     */
    public void animFonctionnalites(Boolean close){
    	RelativeLayout frame = (RelativeLayout) findViewById(R.id.layoutFonctionnalites);
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
    
    /**
     * Clic sur le bouton retour (mode paysage) : fermer fenêtre des fonctionnalités
     * @param v		View qui appelle la méthode
     */
    public void retour(View v){
    	animFonctionnalites(true);
    }
    
    /**
     * Play de la musique
     */
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
    
    /**
     * Pause musique
     */
    public void pause(){
    	buttonPlayStop.setBackgroundResource(R.drawable.play);
		mPlayer.pause();
		isPlay = false;
    }
    
    /**
     * Clic ouverture du menu contenant la liste des boucles enregistrées pour la musique en cours
     * @param v		View qui appelle la méthode
     */
    public void chargerSauv(View v){
    	//ouverture d'un menu contextuel avec la liste des sauv disponibles pour cette chanson
    	registerForContextMenu(v);
    	v.showContextMenu();
    }
    
    /**
     * Menu contenant la liste des boucles enregistrées pour la musique en cours
     */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Chargement");
		menu.setHeaderIcon(R.drawable.uploadclick);
		menu.add(0, 0, 0, "annuler");
		ArrayList<Loop> lstLoop = ExtractLoopConf.getLoops(currentSong);
		for(int k=0;k<lstLoop.size();k++)
		{
			//String hhmmssDeb = millisToDate(lstLoop.get(k).getDebutLoop());
			//String hhmmssFin = millisToDate(lstLoop.get(k).getFinLoop());
			menu.add(0,k,0,lstLoop.get(k).getNom());
		}
	}
	
    /**
     * Evènement sur le clic d'un item du menu des boucles enregistrées
     */
	public boolean onContextItemSelected(MenuItem item) {
		if(item.getTitle().equals("annuler"))
		{
			return true;
		}
		else
		{
			/*String[] tabLoop = item.getTitle().toString().split("-");
			String[] tabDeb = tabLoop[0].split(":");
			String hDeb = tabDeb[0];
			String mDeb = tabDeb[1];
			String sDeb = tabDeb[2];
			
			String[] tabFin = tabLoop[1].split(":");
			String hFin = tabFin[0];
			String mFin = tabFin[1];
			String sFin = tabFin[2];*/
			
			//seekBar_debut.setProgress(Integer.parseInt(String.valueOf(TimeUnit.HOURS.toMillis(Long.parseLong(hDeb))+TimeUnit.MINUTES.toMillis(Long.parseLong(mDeb))+TimeUnit.SECONDS.toMillis(Long.parseLong(sDeb)))));
			//seekBar_fin.setProgress(Integer.parseInt(String.valueOf(TimeUnit.HOURS.toMillis(Long.parseLong(hFin))+TimeUnit.MINUTES.toMillis(Long.parseLong(mFin))+TimeUnit.SECONDS.toMillis(Long.parseLong(sFin)))));
			Loop l = ExtractLoopConf.getLoop(item.getTitle().toString(),currentSong);
			seekBar_debut.setProgress(l.getDebutLoop());
			seekBar_fin.setProgress(l.getFinLoop());
			return true;
		}
	}
    
	/**
	 * Clic enregistrer les paramètres de la boucle
     * @param v		View qui appelle la méthode
	 */
	public void enregistrerLoop(View v){
    	animFonctionnalites(true);
    	//animation pour l'ouverture de la fenetre des paroles
    	Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animalphain);       
    	LinearLayout enregistrementBoucleLayout =  (LinearLayout)findViewById(R.id.LayoutEnregistrementLoop);
    	enregistrementBoucleLayout.setVisibility(FrameLayout.VISIBLE);
    	enregistrementBoucleLayout.startAnimation(a);
	}
	
	/**
	 * Valider enregistrement des valeurs de la boucle
	 * @param v 	View qui appelle la méthode
	 */
	public void validerEnregistrementLoop(View v){
		String titreloop = ((EditText) findViewById(R.id.nomEnregistrementLoop)).getText().toString();
		if(ExtractLoopConf.getLoop(titreloop, currentSong)==null)
		{
			ExtractLoopConf.addLoop(new Loop(titreloop,seekBar_debut.getProgress(),seekBar_fin.getProgress(),currentSong));
		}
		else
		{
			Context context = getApplicationContext();
			CharSequence text = "Une boucle avec le même nom existe deja";
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
		Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animalphaout);       
    	LinearLayout enregistrementBoucleLayout =  (LinearLayout)findViewById(R.id.LayoutEnregistrementLoop);
    	enregistrementBoucleLayout.setVisibility(FrameLayout.INVISIBLE);
    	enregistrementBoucleLayout.startAnimation(a);
	}
	
	/**
	 * Annuler l'enregistrement des valeurs de la boucle : fermer la fenêtre d'enregistrement
	 * @param v		View qui appelle la méthode
	 */
	public void annulerEnregistrementLoop(View v){
		//animation pour l'ouverture de la fenetre des paroles
    	Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animalphaout);       
    	LinearLayout enregistrementBoucleLayout =  (LinearLayout)findViewById(R.id.LayoutEnregistrementLoop);
    	enregistrementBoucleLayout.setVisibility(FrameLayout.INVISIBLE);
    	enregistrementBoucleLayout.startAnimation(a);
	}
	
	/**
	 * Clic ouverture liste des musiques
	 * @param v		View qui appelle la méthode
	 */
    public void openDossierMusic(View v)
	{   
    	animFonctionnalites(true);
    	fermerParoles();
		startActivity(musicList);
	}
    
    /**
     * Clic musique suivante
     * @param v		View qui appelle la méthode
     */
    public void musiqueSuivante(View v) {  	
  		if(AllSongList != null){
  			animFonctionnalites(true);
  			if(equalizerActivity!=null)
  				equalizerActivity.resetEqualizer();
  			AllSongList.nextSong();
  		}
  	}           
    
    /**
     * Clic musique précédente
     * @param v		View qui appelle la méthode
     */
    public void musiquePrecedente(View v) {   	
  		if(AllSongList != null){
  			animFonctionnalites(true);
  			if(equalizerActivity!=null)
  				equalizerActivity.resetEqualizer();
  			AllSongList.previousSong();
  		}
  	}      

    /**
     * Activer/désactiver le mode réglage manuel de la boucle
     * @param v		View qui appelle la méthode
     */
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
  			FrameLayout frame = (FrameLayout) findViewById(R.id.layoutReglageBoucle);
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
    
    /**
     * Clic réglade début de la boucle
     * @param v		View qui appelle la méthode
     */
    public void loopDebut(View v) {
    	if(editLoop){
	  		ImageButton bouton = (ImageButton) findViewById(R.id.loopdebut);
	  		FrameLayout frame = (FrameLayout) findViewById(R.id.layoutReglageBoucle);
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
    
    /**
     * Clic réglade fin de la boucle
     * @param v		View qui appelle la méthode
     */
    public void loopFin(View v) {
    	
    	if(editLoop){
	    	ImageButton bouton = (ImageButton) findViewById(R.id.loopfin);
	    	FrameLayout frame = (FrameLayout) findViewById(R.id.layoutReglageBoucle);
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
    
    /**
     * Clic stop
     * @param v		View qui appelle la méthode
     */
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
    
    /**
     * Clic ouverture egaliseur
     * @param v		View qui appelle la méthode
     */
    public void openEqualizer(View v){
    	animFonctionnalites(true);
    	intentTonalite.setClassName("fr.unice.aap", "fr.unice.aap.EqualizerActivity");
    	startActivity(intentTonalite);
    }
    
    /**
     * Clic activer/désactiver mode boucle
     * @param v		View qui appelle la méthode
     */
    public void onOffLoop(View v){
    	if (isLoop) {
            buttonLoop.setBackgroundResource(R.drawable.loop);  
            isLoop = false;
        }else {
        	buttonLoop.setBackgroundResource(R.drawable.loopclick);  
        	isLoop = true;
        }
    }
  	
    /**
     * Clic ouverture fenêtre des paroles
     * @param v		View qui appelle la méthode
     */
    public void findLyrics(View v){
    	findLyrics();
    }
    
    /**
     * Ouverture de la fenêtre des paroles
     */
    public void findLyrics(){
    	// On enlève le menu 
    	animFonctionnalites(true);
    	//animation pour l'ouverture de la fenetre des paroles
    	Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animalphain);       
    	ScrollView fenetreParoles = (ScrollView) findViewById(R.id.LyricsView);
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
    
    /**
     * Clic fermer la fenêtre des paroles
     * @param v		View qui appelle la méthode
     */
    public void closeLyricViews(View v){
    	fermerParoles();
    }
    
    /**
     * Clic ouverture fenêtre de l'enregistrement sonore
     * @param v		View qui appelle la méthode
     */
    public void openRecordLayout(View v){
    	// On enlève le menu 
    	animFonctionnalites(true);
    	//animation pour l'ouverture de la fenetre des paroles
    	Animation a = AnimationUtils.loadAnimation(AAP.activity, R.anim.animalphain);       
    	LinearLayout recordLayout =  (LinearLayout)findViewById(R.id.RecordLayout);
    	recordLayout.setVisibility(FrameLayout.VISIBLE);
    	recordLayout.startAnimation(a);
    }
    
    /**
     * Mise en route de l'enregistrement sonore
     * @param v		View qui appelle la méthode
     */
    public void recordSound(View v){
    	try{
	    	ImageButton buttonRecord = (ImageButton) findViewById(R.id.RecordLayoutRecButton);
	    	recordTitle = ((EditText) findViewById(R.id.RecordTitle)).getText().toString();
	    	if (isRecording) {
	    		buttonRecord.setBackgroundResource(R.drawable.rec);  
	    		isRecording = false;
	    		if(audioRecorder!=null)
	    			audioRecorder.stop();
	        }else {
	        	buttonRecord.setBackgroundResource(R.drawable.recclick);  
	        	isRecording = true;
	        	audioRecorder = new AudioRecorder("Music/" + recordTitle);
	        	audioRecorder.start();
	        }
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    /**
     * Clic fermer la fenêtre de l'enregistrement sonore
     * @param v		View qui appelle la méthode
     */
    public void closeRecordLayout(View v){
    	fermerRecord();
    }
        
    /**
     * Thread qui synchronise la musique et la barre de musique + boucle
     */
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

    /**
     * Initialisation des barres de progression et textes de postion, durée, ... de la musique
     */
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
        
    /**
     * Convertisseur millisecondes vers string : 00:00:00
     * @param ms	Millisecondes
     */
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

    /**
     * Menu qui s'affiche lorsqu'on clique sur le bouton menu du téléphone
     * Création du menu et des items
     * @param menu	Menu qui sera affiché
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Quit").setIcon(R.drawable.quit);
        return true;
    }

    /**
     * Evènements sur le clic des items du menu
     * @param item 	Item sélectionné sur le menu
     */
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
    
    /* ********************* affichage du menu d'aide ********************** */
    /**
     * Clic ouverture du menu d'aide
     * @param v		View qui appelle la méthode
     */
    public void help(View v){
    	//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(this);
        final View alertDialogView = factory.inflate(R.layout.layouthelp, null);
 
        //Création de l'AlertDialog
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
        //On affecte la vue personnalisé que l'on a crée à notre AlertDialog
        alert.setView(alertDialogView);
 
        //On donne un titre à l'AlertDialog
        alert.setTitle("aide");
       
        alert.setIcon(android.R.drawable.ic_dialog_info);
 
        //On affecte un bouton "OK" à notre AlertDialog et on lui affecte un évènement
        alert.setPositiveButton("Fermer", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {       
          } });     
 
        alert.show();
    }
    
    /**
     * Clic ouverture menu de paramétrage des répertoires contenant les musiques
     * @param v		View qui appelle la méthode
     */
    public void config(View v){
    	//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(this);
        final View alertDialogView = factory.inflate(R.layout.layoutparams, null);
 
        //Création de l'AlertDialog
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
        //On affecte la vue personnalisé que l'on a crée à notre AlertDialog
        alert.setView(alertDialogView);
 
        //On donne un titre à l'AlertDialog
        alert.setTitle("Configurations");
        
        txtListeRepertoireMusique = (TextView)alertDialogView.findViewById(R.id.ListMusicPaths);
        refreshViewListeRepertoireMusique();
        
        alert.setIcon(android.R.drawable.ic_dialog_dialer);
 
        //On affecte un bouton "Fermer" à notre AlertDialog et on lui affecte un évènement
        alert.setPositiveButton("Fermer", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
 
        alert.show();
    }
    
    public void ajouterDossierMusique(View v) {
    	animFonctionnalites(true);
    	startActivity(intentFileExplorer);
    }
    
    public static void retrievePreferences() {
    	SharedPreferences appPref = PreferenceManager.getDefaultSharedPreferences(appContext);
		String temp = appPref.getString("music_files_paths", null);
		if(temp != null) {
			listeRepertoireMusique = Arrays.asList(temp.split(";"));
		}

		String state = Environment.getExternalStorageState();
		if(listeRepertoireMusique == null || listeRepertoireMusique.isEmpty()) {
			listeRepertoireMusique = new LinkedList<String>();
			if(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				listeRepertoireMusique.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath());
			}
		} else {
			List<String> dirMusic = new LinkedList<String>();
			File file;
			for(String path : listeRepertoireMusique) {
				file = new File(path);
				if(file.exists() && file.isDirectory()) {
					dirMusic.add(path);
				}
			}
			if(dirMusic.size() != listeRepertoireMusique.size()) {
				listeRepertoireMusique = dirMusic;
				savePreferences();
			}
		}
    }

	public static void savePreferences() {
		SharedPreferences.Editor appPrefEditor = PreferenceManager.getDefaultSharedPreferences(appContext).edit();
    	StringBuffer result = new StringBuffer();
        if (listeRepertoireMusique != null && !listeRepertoireMusique.isEmpty()) {
            result.append(listeRepertoireMusique.get(0));
            for (int i=1; i<listeRepertoireMusique.size(); i++) {
                result.append(";");
                result.append(listeRepertoireMusique.get(i));
            }
        }
    	appPrefEditor.putString("music_files_paths", result.toString());
		appPrefEditor.commit();
	}
	
	public static List<String> getListeRepertoireMusique() {
		return listeRepertoireMusique;
	}
	
	public static void refreshViewListeRepertoireMusique() {
		retrievePreferences();
        StringBuffer paths = new StringBuffer();
        if (listeRepertoireMusique != null && !listeRepertoireMusique.isEmpty()) {
        	paths.append(listeRepertoireMusique.get(0));
            for (int i=1; i<listeRepertoireMusique.size(); i++) {
            	paths.append("\n");
            	paths.append(listeRepertoireMusique.get(i));
            }
        }
        txtListeRepertoireMusique.setText(paths.toString());
	}
}