package com.voicesync.rife;

import java.text.DecimalFormat;


public class DescFreqMulti {
	public DescFreqMulti(String desc, float[] freq) {this.desc=desc; this.freq=freq;}
	String desc;
	float[]freq;
	int baseOct=0;
	String freqs2String() {
		String s="";
		for (int i=0; i<freq.length; i++) s+=fmt(freq[i]) + ((i!=freq.length-1) ? ", ":"");
		return s;
	}
	public static String fmt(float f)	{ return new DecimalFormat("#.##").format(f); }
	public float[]freqs2Audible() {
		int dl=freq.length;
		float[]f=new float[dl];
		for (int i=0; i<dl; i++) f[i]=(float)MusicFreq.FreqInOctave(freq[i], baseOct);
		return f;
	}
	public float[]freqs2Solfeggio() { // this would change the freq...
		int dl=freq.length;
		float[]f=new float[dl];
		for (int i=0; i<dl; i++) f[i]=(float)Solfeggio.noteFit(freq[i]);
		return f;
	}
	public double[]freqs2AudibleDouble() {
		int dl=freq.length;
		double[]d=new double[dl];
		for (int i=0; i<dl; i++) d[i]=MusicFreq.FreqInOctave(freq[i], baseOct);
		return d;
	}
	public String freqs2AudibleString() {
		String s="";
		for (int i=0; i<freq.length; i++) s+=fmt((float)MusicFreq.FreqInOctave(freq[i], baseOct)) + ((i!=freq.length-1) ? ", ":"");
		return s;
	}
	public boolean isOk() {
		boolean ok=freq.length!=0;
		for (int i=0; ok & i<freq.length; i++) ok = (freq[i]!=0);
		return ok;
	}
}

