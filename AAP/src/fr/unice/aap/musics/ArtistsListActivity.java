package fr.unice.aap.musics;

import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class ArtistsListActivity extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MusicListActivity.createMetadataActivity(this,MediaMetadataRetriever.METADATA_KEY_ARTIST);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(position > -1) {
			String itemSelected = ((Map<String,String>)getListView().getItemAtPosition(position)).get(MusicListActivity.METADATA);

			MusicListActivity.refreshListSongs(MediaMetadataRetriever.METADATA_KEY_ARTIST, itemSelected);
			
			Intent intent = new Intent(this, AllSongsListActivity.class);
			startActivity(intent);
			this.finish();
		}
	}
}