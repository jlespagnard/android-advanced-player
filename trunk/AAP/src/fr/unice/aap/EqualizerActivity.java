package fr.unice.aap;

import fr.unice.aap.EqualizerView;

import android.app.Activity;
import android.media.AudioManager;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class EqualizerActivity extends Activity {
     
	private static final String TAG = "AudioFxDemo";
    private static final float VISUALIZER_HEIGHT_DIP = 50f;
    private Visualizer mVisualizer;
    private Equalizer mEqualizer;
    private LinearLayout mLinearLayout;
    private EqualizerView mVisualizerView;
    private TextView mStatusTextView;
	private Button closeButton;

    @Override
    public void onCreate(Bundle icicle) {
    	super.onCreate(icicle);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mStatusTextView = new TextView(this);
        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.addView(mStatusTextView);
        setContentView(mLinearLayout);
        setupVisualizerFxAndUI();
        setupEqualizerFxAndUI();
        AAP.equalizerActivity = this;
        // Make sure the visualizer is enabled only when you actually want to receive data, and
        // when it makes sense to receive data.
        mVisualizer.setEnabled(true);
        
      	closeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        
    }
    
    private void setupEqualizerFxAndUI() {
    	// Create the Equalizer object (an AudioEffect subclass) and attach it to our media player,
    	mEqualizer = new Equalizer(0, AAP.mPlayer.getAudioSessionId());
    	mEqualizer.setEnabled(true);
    	TextView eqTextView = new TextView(this);
    	eqTextView.setText("Equalizer:");
    	mLinearLayout.addView(eqTextView);
    	short bands = mEqualizer.getNumberOfBands();
    	final short minEQLevel = mEqualizer.getBandLevelRange()[0];
    	final short maxEQLevel = mEqualizer.getBandLevelRange()[1];
    	for (short i = 0; i < bands; i++) {
    		final short band = i;
    		TextView freqTextView = new TextView(this);
    		freqTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    		freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
    		freqTextView.setText((mEqualizer.getCenterFreq(band) / 1000) + " Hz");
    		mLinearLayout.addView(freqTextView);
    		LinearLayout row = new LinearLayout(this);
    		row.setOrientation(LinearLayout.HORIZONTAL);
    		TextView minDbTextView = new TextView(this);
    		minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    		minDbTextView.setText((minEQLevel / 100) + " dB");
        	TextView maxDbTextView = new TextView(this);
        	maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        	maxDbTextView.setText((maxEQLevel / 100) + " dB");
        	LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        	layoutParams.weight = 1;
        	SeekBar bar = new SeekBar(this);
        	bar.setLayoutParams(layoutParams);
        	bar.setMax(maxEQLevel - minEQLevel);
        	bar.setProgress(mEqualizer.getBandLevel(band));
        	bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        			mEqualizer.setBandLevel(band, (short) (progress + minEQLevel));
        		}
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        row.addView(minDbTextView);
        row.addView(bar);
        row.addView(maxDbTextView);
        mLinearLayout.addView(row);
    	}
    	
    	closeButton = new Button(this);
    	closeButton.setText("Fermer");
    	mLinearLayout.addView(closeButton);
    }
    
    private void setupVisualizerFxAndUI() {
    	// Create a VisualizerView (defined below), which will render the simplified audio
    	// wave form to a Canvas.
    	mVisualizerView = new EqualizerView(this);
    	mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.FILL_PARENT,
            (int)(VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density)));
    	mLinearLayout.addView(mVisualizerView);
    	// Create the Visualizer object and attach it to our media player.
    	mVisualizer = new Visualizer(AAP.mPlayer.getAudioSessionId());
    	mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
    	mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
    		public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
    			mVisualizerView.updateVisualizer(bytes);
    		}
    		public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {}
    	}, Visualizer.getMaxCaptureRate() / 2, true, false);
    }
    
    public void resetEqualizer(){
    	mEqualizer.release();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if (isFinishing() && AAP.mPlayer != null) {
    		mVisualizer.release();
    	}
    }
}