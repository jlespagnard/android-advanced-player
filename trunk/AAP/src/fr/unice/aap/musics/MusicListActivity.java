package fr.unice.aap.musics;

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.unice.aap.AAP;
import fr.unice.aap.R;

import android.app.ListActivity;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * <p>
 * Activit� regroupant les m�thodes pour le tri des fichiers audios.<br />
 * Elle est �galement charg�e d'initialiser le menu de tri des fichiers audios et de l'afficher.
 * </p>
 * 
 * @author Julien LESPAGNARD
 * @author Anthony BONIN
 * @author Michel CARTIER
 * @author �lodie MAZUEL
 * @see ListActivity
 * @see MediaMetadataRetriever
 * @see AlbumsListActivity
 * @see AllSongsListActivity
 * @see ArtistsListActivity
 * @see AudioFileFilter
 * @see GenresListActivity
 */
public class MusicListActivity extends ListActivity {
	// Constantes utilis�es comme key dans les listes
	public static final String MUSIC_FILES_PATHS = "music_files_paths";
	public static final String MENU_ITEM = "menu_item";
	public static final String METADATA = "Metadata";
	public static final String ARTIST = "Artist";
	public static final String TITLE = "Title";
	public static final String ALBUM = "Album";
	public static final String GENRE = "Genre";
	public static final String DURATION = "DURATION";
	public static final String DEFAULT_DURATION = "00:00";
	public static final String UNKNOWN = "(Unknown)";
	public static final String URI = "Uri";
	// Pour r�cup�rer les m�tadatas dans les fichiers audio
	private static MediaMetadataRetriever mediaMetadataRetriever = null;
	// Liste des musique
	private static List<File> musicFiles = null;
	// Liste contenant les diff�rentes valeurs existantes pour une m�tadata donn�e
	private static Map<Integer,List<String>> mediaMetadatas = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.musics_list);
				
		initMusicFiles();

		this.setMenuListItem();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(position > -1) {
			String itemSelected = ((Map<String,String>)getListView().getItemAtPosition(position)).get(MusicListActivity.MENU_ITEM);

			Intent intent = null;
			if(itemSelected.equals(getResources().getText(R.string.all_songs))) {
				intent = new Intent(this, AllSongsListActivity.class);
			}
			else if(itemSelected.equals(getResources().getText(R.string.artist))) {
				intent = new Intent(this, ArtistsListActivity.class);
			}
			else if(itemSelected.equals(getResources().getText(R.string.album))) {
				intent = new Intent(this, AlbumsListActivity.class);
			}
			else if(itemSelected.equals(getResources().getText(R.string.genre))) {
				intent = new Intent(this, GenresListActivity.class);
			}
			
			if(intent != null) {
				startActivityForResult(intent, 1);
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		this.setResult(resultCode);
		if(resultCode == 1) {
			this.finish();
		}
	}
	
	@Override
	public void onBackPressed() {
		this.setResult(0);
		this.finish();
	}
	
	/**
	 * Construis le menu des options de tris propos�s.
	 */
	private void setMenuListItem() {
		List<Map<String,String>> data = new LinkedList<Map<String,String>>();
		
		String[] menuItems = getResources().getStringArray(R.array.menu_items);
		
		Map<String,String> menu;
		for(String menuItem : menuItems) {
			menu = new LinkedHashMap<String, String>();
			menu.put(MENU_ITEM,menuItem);
			data.add(menu);
		}
		
		SimpleAdapter adapter = new SimpleAdapter(
				MusicListActivity.this, 
				data, 
				R.layout.menu_list_item, 
				new String[] {MENU_ITEM}, 
				new int[] {R.id.menu_item});
		
		setListAdapter(adapter);
	}
	
	/**
	 * @return	la variable utilis�e pour r�cup�rer les m�tadatas dans les fichiers audio
	 */
	public static MediaMetadataRetriever getMediaMetadataRetriever() {
		if(mediaMetadataRetriever == null) {
			mediaMetadataRetriever = new MediaMetadataRetriever();
		}
		return mediaMetadataRetriever;
	}
	
	/**
	 * Initialise la liste des fichiers audio support�s pr�sents dans les r�pertoires de stockage configur�s
	 */
	private static void initMusicFiles() {
		musicFiles = new LinkedList<File>();
		for(String path : AAP.getListeRepertoireMusique()) {
			File dirMusic = new File(path);
			AudioFileFilter filter = new AudioFileFilter();
			parcourPathAndAddFiles(dirMusic, musicFiles, filter);
		}
	}
	
	/**
	 * Parcours r�cursiement les r�pertoires de stockage pour r�cup�rer les fichiers audio support�s.
	 * 
	 * @param dir		le r�pertoire � parcourir
	 * @param files		la liste des fichiers audio support�s d�j� r�cup�r�s
	 * @param filter	le filtre utilis� pour la s�lection des fichiers audio pris en charge 
	 */
	private static void parcourPathAndAddFiles(File dir, List<File> files, FileFilter filter) {
		if(dir.listFiles() != null) {
			for(File file : dir.listFiles()) {
				if(file.isFile() && filter.accept(file)) {
					files.add(file);
				}
				else if(file.isDirectory()) {
					parcourPathAndAddFiles(file, files, filter);
				}
			}
		}
	}
	
	/**
	 * @return	la liste des fichiers audios support�s r�cup�r�s dans les r�pertoire de stockage
	 */
	public static List<File> getMusicFiles() {
		return musicFiles;
	}
	
	/**
	 * @param p_metadataKey	le type de m�tadata � r�cup�rer
	 * @return	la liste des diff�rentes valeurs existantes pour la m�tadata <code>p_metadataKey</code>
	 */
	public static List<String> getDistinctMediadataFromKey(int p_metadataKey) {
		if(mediaMetadatas == null) {
			mediaMetadatas = new LinkedHashMap<Integer, List<String>>();
		}
		
		List<String> metadatas = mediaMetadatas.get(p_metadataKey);
		if(metadatas == null) {
			metadatas = new LinkedList<String>();
			mediaMetadatas.put(p_metadataKey, metadatas);
		}
			
		MediaMetadataRetriever mmr = getMediaMetadataRetriever();
		String mediaMetadata;
		for(File music : musicFiles) {
			mmr.setDataSource(music.getPath());
			
			mediaMetadata = mmr.extractMetadata(p_metadataKey);
			if(mediaMetadata == null || mediaMetadata.isEmpty()) {
				mediaMetadata = UNKNOWN;
			}
			
			if(!metadatas.contains(mediaMetadata)) {
				metadatas.add(mediaMetadata);
			}
		}
		Collections.sort(metadatas);
		
		return mediaMetadatas.get(p_metadataKey);
	}
	
	/**
	 * Initialise la liste des valeurs de la m�tadata <code>p_metadataKey</code> � afficher au sein de l'activit� <code>p_metadataActivity</code>
	 * 
	 * @param p_metadataActivity	l'activit� qui affichera les valeurs du m�tadata <code>p_metadataKe</code>
	 * @param p_metadataKey			le type de m�tadata � r�cup�rer
	 */
	public static void createMetadataActivity(ListActivity p_metadataActivity, int p_metadataKey) {
		p_metadataActivity.setContentView(R.layout.musics_list);
		
		List<String> metadatas = MusicListActivity.getDistinctMediadataFromKey(p_metadataKey);
		List<Map<String,String>> data = new LinkedList<Map<String,String>>();
		Map<String,String> infos;		
		for(String metadata : metadatas) {
			infos = new LinkedHashMap<String,String>();
			infos.put(MusicListActivity.METADATA,metadata);
			data.add(infos);
		}
		
		SimpleAdapter adapter = new SimpleAdapter(
				p_metadataActivity, 
				data, 
				R.layout.metadatas_list_item, 
				new String[] {MusicListActivity.METADATA}, 
				new int[] {R.id.metadata});
		
		p_metadataActivity.setListAdapter(adapter);
	}
	
	/**
	 * Rafra�chit la liste des musiques � afficher selon la valeur <code>p_metadataValue</code> du type de m�tadata <code>p_metadataKey</code>
	 * 
	 * @param p_metadataKey		le type de m�tadata
	 * @param p_metadataValue	la valeur du m�tadta
	 */
	public static void refreshListSongs(int p_metadataKey, String p_metadataValue) {
		initMusicFiles();
		if(p_metadataKey != -1 && p_metadataValue != null && !p_metadataValue.trim().isEmpty()) {
			List<File> oldMusicFiles = musicFiles;
			musicFiles = new LinkedList<File>();
			
			MediaMetadataRetriever mmr = MusicListActivity.getMediaMetadataRetriever();
			for(File musicFile : oldMusicFiles) {
				mmr.setDataSource(musicFile.getPath());
				
				String value = mmr.extractMetadata(p_metadataKey);
				if(value == null || value.isEmpty()) {
					if(p_metadataValue.equals(MusicListActivity.UNKNOWN)) {
						musicFiles.add(musicFile);
					}
				}
				else if(p_metadataValue.equals(value)) {
					musicFiles.add(musicFile);
				}
			}
		}
	}
}