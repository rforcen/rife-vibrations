package com.voicesync.rife;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
/*
 * generates the full set of bija mantra for each rife frequency
 */
public class PlayerBM extends Service  {

	public boolean isPlaying=false;
	private double[]freqList;
	AudioTrack track;
	int sampleRate=44100/2;
	playSoundTask ps=new playSoundTask();
	long lap, start, msPerFreq=10*1000;
	private ProgressDialog prg; 	final Handler hand = new Handler(); Context context;
	String message;

	public void setFreqs(double[]freqList) {this.freqList=freqList;	}
	public void setDuration(int secs) {msPerFreq=secs*1000;}

	public void stopPlaying()  { 
		isPlaying=false;
		if (ps!=null) ps.cancel(true);
		ps=null;
	}
	public void startPlaying(Context context, String message, int secsDur) {
		this.context=context;
		this.message=message;
		this.msPerFreq=secsDur*1000;
		isPlaying=true;  
		ps=new playSoundTask(); // play FileName
		ps.execute();
	}

	private class playSoundTask extends AsyncTask<Void, Void, Void> {
		int minSize;
		WaveGen wg;
		double[] vAmp, vHz;
		short []sbuff;

		@Override protected void onPreExecute() { // load samples & init track 
			// generate formants chord
			int bps=AudioFormat.ENCODING_PCM_16BIT;
			int nch=AudioFormat.CHANNEL_OUT_STEREO;
			int samprate=sampleRate;
			minSize = AudioTrack.getMinBufferSize( samprate, nch, bps );        
			track = new AudioTrack(AudioManager.STREAM_MUSIC, samprate, nch, bps, minSize, AudioTrack.MODE_STREAM);
			track.play();
			sbuff=new short[minSize]; // stereo
			prepProgress();
		}
		private void prepProgress() {
			prg=ProgressDialog.show(context, message, "", false, true, new OnCancelListener(){
				@Override public void onCancel(DialogInterface di) {
					isPlaying=false;
				}});
			prg.setMax(freqList.length);
		}
		private void dispProgress(final int i, final int j) {
			hand.post(new Runnable() {public void run() {
				prg.setMessage(String.format("playing BM '%s' tone: %s hz", BM.bmforms[j].name, DescFreqMulti.fmt((float)freqList[i])));
				prg.setProgress(i);		
			}});      
		}
		@Override protected Void doInBackground(Void... params) { // play 
			for (int i=0; i<freqList.length; i++) {	// for all freqs.
				for (int j=0; j<BM.bmforms.length; j++) {	// for all bm
					generateChord(i,j); startTimer(); 
					dispProgress(i,j);
					while (isPlaying & isTiming()) {
						wg.gen(sbuff);
						track.write( sbuff, 0, minSize );
					}
				}
			}
			return null;
		}
		@Override protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			prg.dismiss();
			sendMessage();
			isPlaying=false;
		}

		private boolean isTiming() { return (System.currentTimeMillis() - start) < msPerFreq; }
		private void startTimer()  { start=System.currentTimeMillis(); 	}
		private void generateChord(int nc, int nbm) {
			wg=new WaveGen(WaveGen.STEREO, sampleRate);
			vAmp 	= BM.bmforms[nbm].amp.clone(); // clone to leave intact
			vHz		= BM.bmforms[nbm].hz.clone();
			double ratio=freqList[nc]/vHz[0]; // scale to hz[0]/freqList[nc], first is main formant
			for (int i=0; i<vHz.length; i++) vHz[i] *= ratio;
			wg.Set(vAmp, vHz, null, vHz.length);
			wg.SetDif(1.61803); // phi based binaural			
		}
	}
	@Override public IBinder onBind(Intent arg0) {	return null; }
	void sendMessage() { // send message 
		context.sendBroadcast(new Intent("android.intent.action.MAIN"));
	}
}
