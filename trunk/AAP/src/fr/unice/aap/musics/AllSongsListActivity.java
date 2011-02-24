package fr.unice.aap.musics;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.unice.aap.AAP;
import fr.unice.aap.R;

import android.app.ListActivity;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class AllSongsListActivity extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		int metadataKey = -1;
		String metadataValue = null;
		if(getIntent() != null && getIntent().getExtras() != null) {
			metadataKey = getIntent().getExtras().getInt("metadataKey",-1);
			metadataValue = getIntent().getExtras().getString("metadataValue");
		}
		
		setContentView(R.layout.musics_list);
		
		List<Map<String, String>> data = new LinkedList<Map<String,String>>();
		Map<String, String> infos;
		
		MediaMetadataRetriever mmr = MusicListActivity.getMediaMetadataRetriever();
				
		for(File music : MusicListActivity.getMusicFiles()) {
			mmr.setDataSource(music.getPath());
			infos = new LinkedHashMap<String, String>();
						
			String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
			if(metadataKey == MediaMetadataRetriever.METADATA_KEY_ARTIST) {
				if(!metadataValue.equals(artist)) {
					continue;
				}
			}
			if(artist == null || artist.isEmpty()) {
				artist = MusicListActivity.UNKNOWN;
			}
			
			String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
			if(title == null || title.isEmpty()) {
				title = MusicListActivity.UNKNOWN;
			}
			
			String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
			if(metadataKey == MediaMetadataRetriever.METADATA_KEY_ALBUM) {
				if(!metadataValue.equals(album)) {
					continue;
				}
			}
			if(album == null || album.isEmpty()) {
				album = MusicListActivity.UNKNOWN;
			}
			
			String genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
			if(metadataKey == MediaMetadataRetriever.METADATA_KEY_GENRE) {
				if(!metadataValue.equals(genre)) {
					continue;
				}
			}
			if(genre == null || genre.isEmpty()) {
				genre = MusicListActivity.UNKNOWN;
			}
			
			String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			if(duration == null || duration.isEmpty()) {
				duration = MusicListActivity.DEFAULT_DURATION;
			}
			else {
				try {
					int duree = Integer.parseInt(duration);
					int heures = (int)(duree/(1000*3600));
					int reste = (int)(duree % (1000*3600));
					int minutes = (int)(reste/(1000*60));
					reste %= (1000*60);
					int secondes = (reste/1000);
					
					duration = "";
					if(heures > 0) {
						duration = String.valueOf(heures)+":";
					}
					if(minutes < 10) {
						duration += "0"+String.valueOf(minutes);
					}
					else {
						duration += String.valueOf(minutes);
					}
					if(secondes < 10) {
						duration += ":0"+String.valueOf(secondes);
					}
					else {
						duration += ":"+String.valueOf(secondes);
					}
				}
				catch (NumberFormatException e) {
					duration = MusicListActivity.DEFAULT_DURATION;
				}
			}
									
			infos.put(MusicListActivity.ARTIST, artist);
			infos.put(MusicListActivity.TITLE, title);
			infos.put(MusicListActivity.ALBUM, album+" ");
			infos.put(MusicListActivity.GENRE, genre+" ");
			infos.put(MusicListActivity.DURATION, duration+" ");
			infos.put(MusicListActivity.URI, Uri.fromFile(music).getPath());
			data.add(infos);
		}
		
		SimpleAdapter adapter = new SimpleAdapter(
				AllSongsListActivity.this, 
				data, 
				R.layout.musics_list_item, 
				new String[] {MusicListActivity.ARTIST,MusicListActivity.TITLE,MusicListActivity.ALBUM,MusicListActivity.GENRE,MusicListActivity.DURATION}, 
				new int[] {R.id.artist,R.id.title,R.id.album,R.id.genre,R.id.duration});
		
		setListAdapter(adapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(position > -1) {
			Uri uri = Uri.parse(((Map<String,String>)getListView().getItemAtPosition(position)).get(MusicListActivity.URI));
			AAP.setSong(MusicListActivity.getParentContext(),uri);
		}
		this.finish();
	}
}