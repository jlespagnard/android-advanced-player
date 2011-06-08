package fr.unice.aap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * <p>
 * Activité utilisée pour parcourir les répertoires présents au sein du téléphones.
 * </p>
 * 
 * @author Julien LESPAGNARD
 * @author Anthony BONIN
 * @author Michel CARTIER
 * @author Élodie MAZUEL
 * @see ListActivity
 */
public class FileExplorerActivity extends ListActivity {
	// Liste des chemins des fichiers présents dans le répertoire courant
	private List<String> item = null;
	// Liste des chemins des répertoire présents dans le répertoire courant
	private List<String> path = null;
	// Le chemin vers la racine
	private String root="/";
	// Le nom du répertoire courant à afficher
	private TextView myPath;
	// Le chemin du répertoire courant
	private String currentPath;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fileexplorer);
        myPath = (TextView)findViewById(R.id.fileexplorerpath);
        getDir(root);
	}
	
	/**
	 * Initialise la liste des fichiers et répertoires présents dans le répertoir <code>dirPath</code>.
	 * 
	 * @param dirPath	le nouveau répertoire courant 
	 */
	private void getDir(String dirPath) {
		currentPath = dirPath;
		myPath.setText("Location: " + dirPath);
		
		item = new ArrayList<String>();
		path = new ArrayList<String>();
		
		File f = new File(dirPath);
		File[] files = f.listFiles();
		if(!dirPath.equals(root)) {
			item.add(root);
			path.add(root);
			
			item.add("../");
			path.add(f.getParent());
		}
		
		for(int i=0; i < files.length; i++) {
			File file = files[i];
			path.add(file.getPath());
			
			if(file.isDirectory()) {
				item.add(file.getName() + "/");
			} else {
				item.add(file.getName());
			}
		}
		
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.fileexplorerrow, item);
		Button btn = (Button)findViewById(R.id.btnSelectRepCourant);
		if(fileList == null || fileList.isEmpty()) {
			btn.setVisibility(View.INVISIBLE);
		}
		else {
			btn.setVisibility(View.VISIBLE);
		}
		setListAdapter(fileList);
	}
	
	/**
	 * Action appelée lorsque que l'utilisateur sélectionne un répertoire dans le répertoire courant.
	 * 
	 * @param v	la vue par laquelle l'action a été appelée
	 */
	public void selectionnerRepertoireCourant(View v) {
		SharedPreferences appPref = PreferenceManager.getDefaultSharedPreferences(this);
		String temp = appPref.getString("music_files_paths", null);
		if(temp != null) {
			temp += ";"+currentPath;
		}
		
		SharedPreferences.Editor appPrefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		appPrefEditor.putString("music_files_paths", temp);
		appPrefEditor.commit();
		
		AAP.refreshViewListeRepertoireMusique();
		finish();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File file = new File(path.get(position));
		
		if (file.isDirectory()) {
			if(file.canRead()) {
				getDir(path.get(position));
			} else {
				new AlertDialog.Builder(this)
				.setIcon(R.drawable.icon)
				.setTitle("[" + file.getName() + "] folder can't be read!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				})
				.show();
			}
		} else {
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.icon)
			.setTitle("[" + file.getName() + "]")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			})
			.show();
		}
	}
}