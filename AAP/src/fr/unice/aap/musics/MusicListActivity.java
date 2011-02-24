package fr.unice.aap.musics;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.unice.aap.R;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MusicListActivity extends ListActivity {
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
	
	private static MediaMetadataRetriever mediaMetadataRetriever = null;
	private static List<File> musicFiles = null;
	private static Map<Integer,List<String>> mediaMetadatas = null;
	private static Context parentContext = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		parentContext = this.getApplicationContext();
		
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
				startActivity(intent);
			}
		}
	}
	
	public static Context getParentContext() {
		return parentContext;
	}
	
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
	
	public static MediaMetadataRetriever getMediaMetadataRetriever() {
		if(mediaMetadataRetriever == null) {
			mediaMetadataRetriever = new MediaMetadataRetriever();
		}
		return mediaMetadataRetriever;
	}
	
	private void initMusicFiles() {
		// Edition des preferences : il faut la mettre a la bonne place dans AAP
		SharedPreferences.Editor appPrefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		appPrefEditor.putString(MUSIC_FILES_PATHS, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath());
		appPrefEditor.commit();
		
		SharedPreferences appPref = PreferenceManager.getDefaultSharedPreferences(this);
		String prefMusicFilesPaths = appPref.getString(MUSIC_FILES_PATHS, null);
		if(prefMusicFilesPaths != null) {
			String[] paths = prefMusicFilesPaths.split(";");
			for(String path : paths) {
				musicFiles = new LinkedList<File>();
				File musicFile = new File(path);
				if(musicFile != null && musicFile.exists()) {
					if(musicFile.isDirectory()) {
						AudioFileFilter filter = new AudioFileFilter();
						
						for(File music : musicFile.listFiles()) {
							if(music.isFile() && filter.accept(music)) {
								musicFiles.add(music);
							}
						}
					}
				}
			}
		}
		else {
			System.out.println("musicFiles is null or doesn't exist !");
		}
	}
	
	public static List<File> getMusicFiles() {
		return musicFiles;
	}
	
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
}