package com.voicesync.rife;

public class Solfeggio { // fit a freq to a Solfeggio note in the same octave.
	static double SolfeggioNotes[]={ 396, 417, 528, 639, 741, 852 }; // oct 0
	
	static public double noteFit(double freq) { //
		double xn=SolfeggioNotes[5], mn=SolfeggioNotes[0];
		double min=Double.MAX_VALUE; int pm=0; int of=0;

		if (freq==0) return mn;

		if (freq < mn) { while (freq<mn) {  of--; freq*=2; } }// reach the scale
		else           { while (freq>xn) {  of++; freq/=2; } }// reach the scale

		for (int i=0; i<6; i++) {
			double dist=Math.abs(SolfeggioNotes[i]-freq);
			if ( dist < min ) {
				min=dist;
				pm=i;
			}
		}
		return SolfeggioNotes[pm];  // * Math.pow(2,of);
	}
}
