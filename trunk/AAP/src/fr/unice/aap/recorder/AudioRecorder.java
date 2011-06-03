package fr.unice.aap.recorder;

import java.io.File;
import java.io.IOException;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

/*Classe d'enregistrement du son*/
public class AudioRecorder {

  final MediaRecorder recorder = new MediaRecorder();
  final String path;

  /**
   * Lieu o� sera plac� l'enregistrement.
   */
  public AudioRecorder(String path) {
    this.path = sanitizePath(path);
  }

  private String sanitizePath(String path) {
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    if (!path.contains(".")) {
      path += ".3gp";
    }
    path = Environment.getExternalStorageDirectory().getAbsolutePath() + path ;
    int clone = 1;
    File file = new File(path);
    while(file.exists()){
    	file = new File(path.replace(".3gp", "(" + clone + ")" + ".3gp"));
    	clone++;
    }
    
    return file.getAbsolutePath();
  }

  /**
   * D�marre un enregistrement.
   */
  public void start() throws IOException {
    String state = android.os.Environment.getExternalStorageState();
    if(!state.equals(android.os.Environment.MEDIA_MOUNTED))  {
        throw new IOException("SD Card is not mounted.  It is " + state + ".");
    }

    // make sure the directory we plan to store the recording in exists
    File directory = new File(path).getParentFile();
    if (!directory.exists() && !directory.mkdirs()) {
      throw new IOException("Path to file could not be created.");
    }

    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    recorder.setOutputFile(path);
    recorder.prepare();
    recorder.start();
  }

  /**
   * Arr�t de l'enregistrement
   */
  public void stop() throws IOException {
    recorder.stop();
    recorder.release();
  }

}