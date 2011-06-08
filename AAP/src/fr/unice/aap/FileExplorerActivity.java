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
 * Activit� utilis�e pour parcourir les r�pertoires pr�sents au sein du t�l�phones.
 * </p>
 * 
 * @author Julien LESPAGNARD
 * @author Anthony BONIN
 * @author Michel CARTIER
 * @author �lodie MAZUEL
 * @see ListActivity
 */
public class FileExplorerActivity extends ListActivity {
	// Liste des chemins des fichiers pr�sents dans le r�pertoire courant
	private List<String> item = null;
	// Liste des chemins des r�pertoire pr�sents dans le r�pertoire courant
	private List<String> path = null;
	// Le chemin vers la racine
	private String root="/";
	// Le nom du r�pertoire courant � afficher
	private TextView myPath;
	// Le chemin du r�pertoire courant
	private String currentPath;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fileexplorer);
        myPath = (TextView)findViewById(R.id.fileexplorerpath);
        getDir(root);
	}
	
	/**
	 * Initialise la liste des fichiers et r�pertoires pr�sents dans le r�pertoir <code>dirPath</code>.
	 * 
	 * @param dirPath	le nouveau r�pertoire courant 
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
	 * Action appel�e lorsque que l'utilisateur s�lectionne un r�pertoire dans le r�pertoire courant.
	 * 
	 * @param v	la vue par laquelle l'action a �t� appel�e
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