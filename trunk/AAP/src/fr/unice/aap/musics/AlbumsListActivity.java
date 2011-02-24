package fr.unice.aap.musics;

import android.app.ListActivity;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

public class AlbumsListActivity extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MusicListActivity.createMetadataActivity(this,MediaMetadataRetriever.METADATA_KEY_ALBUM);
	}
}