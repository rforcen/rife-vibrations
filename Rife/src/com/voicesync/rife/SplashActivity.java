package com.voicesync.rife;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Window;

public class SplashActivity extends Activity {
	private static final Class<?>mainClass=MainActivity.class;	
	private Thread mSplashThread;  

	@Override  public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 				
		setContentView(R.layout.activity_splash);

		RifeDepot.rife.setContext(this); 

		mSplashThread=new Thread(){									
			@Override public void run(){
				RifeDepot.rife.loadDataFromAssest(); // load all the data
				finish();
				Intent intent = new Intent(SplashActivity.this, mainClass); // and go!
				SplashActivity.this.startActivity(intent);
			}
		};
		mSplashThread.start();       
	}
}
