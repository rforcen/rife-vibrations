package com.voicesync.rife;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;

public class Player extends Service {

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
		int nv=3;
		double[] vAmp=new double[nv], vHz=new double[nv];
		double PHI=1/((1 + Math.sqrt(5)) / 2); // 0.618...
		double []mults={PHI, 1, 0.79368932}; // 1, phi, third
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
		private void dispProgress(final int i) {
			hand.post(new Runnable() {public void run() {
				prg.setMessage(String.format("playing tone: %s hz", DescFreqMulti.fmt((float)freqList[i])));
				prg.setProgress(i);		
			}});      
		}
		@Override protected Void doInBackground(Void... params) { // play (write) or generate
			for (int i=0; i<freqList.length; i++) {
				generateChord(i); startTimer(); 
				dispProgress(i);
				while (isPlaying & isTiming()) {
					wg.gen(sbuff);
					track.write( sbuff, 0, minSize );
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
		private void generateChord(int nc) {
			wg=new WaveGen(WaveGen.STEREO, sampleRate);
			for (int i=0; i<nv; i++) { // generate the amp/hz set
				vAmp[i]= mults[i] * Short.MAX_VALUE;
				vHz[i] = mults[i] * freqList[nc];
			}
			wg.Set(vAmp, vHz, null, nv);
			wg.SetDif(1.61803); // phi based binaural			
		}
	}
	@Override public IBinder onBind(Intent arg0) {	return null; }
	void sendMessage() { // send message 
		context.sendBroadcast(new Intent("android.intent.action.MAIN"));
	}
}
