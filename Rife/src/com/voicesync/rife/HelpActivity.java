package com.voicesync.rife;

import android.os.Bundle;
import android.app.Activity;
import android.webkit.WebView;

public class HelpActivity extends Activity {
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		((WebView) findViewById(R.id.webView1)).loadUrl("file:///android_asset/html/rife.html");
	}
}
