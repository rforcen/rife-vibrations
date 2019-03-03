package com.voicesync.rife;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import org.apache.commons.lang3.StringUtils;

public class MainActivity extends Activity {
	ListView lv;
	EditText edFind, edSecs;
	ImageButton ibSearch, ibPlay; 
	int lastPos=0; boolean found=false;
	RifeListAdapter rifeListAdapter;
	private RifeData rife;
	private DescFreqMulti dfm;
	private PlayerBM player;
	Context context;
	private BroadcastReceiver mReceiver;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
	}

	private void init() {
		rife=RifeDepot.rife; // loadeded in splash

		edSecs=(EditText)findViewById(R.id.edSecs);  				// secs duration
		edFind=(EditText)findViewById(R.id.edFind);  				//search
		clearEditOnClick(edFind);
		ibSearch=(ImageButton)findViewById(R.id.ibSearch);			
		ibSearch.setOnClickListener(new OnClickListener(){
			@Override public void onClick(View arg0) {
				if (doSearch()) setPosition(lastPos);
				else 			mess("not found");
			}
		});
		
		ibPlay=(ImageButton)findViewById(R.id.ibPlay);				// play
		ibPlay.setOnClickListener(new OnClickListener(){
			@Override public void onClick(View arg0) {
				if (! found) mess("find or select the item to play");
				else play();
			}
			private void play() {
				if (player.isPlaying) {
					ibPlay.setImageResource(android.R.drawable.ic_media_play);
					player.stopPlaying();
				} else {
					dfm = rife.getItem(lastPos);
					ibPlay.setImageResource(android.R.drawable.ic_media_pause);
					player.setFreqs(dfm.freqs2AudibleDouble());
					player.startPlaying(context, String.format("%s\n%s", dfm.desc, dfm.freqs2AudibleString()), secsDuration());
					
				}			
			}
		});
		ibPlay.setEnabled(false);
		
		lv=(ListView)findViewById(R.id.lvRife);						// list
		rifeListAdapter=new RifeListAdapter();
		lv.setAdapter(rifeListAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() { // click on item: disp item
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				edFind.setText(rife.getItem(position).desc);
				found=true; setPosition(position);
			}
		});		
		
		player=new PlayerBM();
		this.context=this;
	}

	private boolean doSearch() {
		found=false;
		String s=edFind.getText().toString();
		if (TextUtils.isEmpty(s)) return found;
		for (int i=lastPos+1; !found & i<rife.getSize(); i++) {
			if (StringUtils.containsIgnoreCase(rife.getItem(i).desc, s)) {
				lastPos=i; found=true;
			}
		}
		if (!found) lastPos=0;
		return found;
	}
	private void setPosition(int pos) {
		lastPos=pos;
		lv.setSelection(lastPos);
		ibPlay.setEnabled(found);
	}
	private int secsDuration() {
		int n;
		try { n=Integer.valueOf(edSecs.getText().toString());} 
		catch (NumberFormatException nfe) {	n=10; } // default duration = 10 secs
		return n;
	}
	private void regReceiver() { // register broadcast receiver for end recording message
		IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
		mReceiver = new BroadcastReceiver() {
			@Override public void onReceive(Context context, Intent intent) {
				// stop playing msg received
				ibPlay.setImageResource(android.R.drawable.ic_media_play);
				mess("playing completed");
			}
		};
		registerReceiver(mReceiver, intentFilter);//registering our receiver		
	}
	@Override protected void onResume() { // app. resume back again
		super.onResume();
		regReceiver();
	} 
	@Override protected void onPause()   { 
		super.onPause();
		player.stopPlaying();
		unregisterReceiver(this.mReceiver);
	}
	@Override protected void onStop()   { // ap stop, maybe not called
		super.onStop();
		if (player!=null) player.stopPlaying();
	}
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override  public boolean onOptionsItemSelected(MenuItem item)  {
		switch(item.getItemId()) {
		case R.id.menuInfo: startActivity(new Intent(this, HelpActivity.class)); break;
		}
		return true;
	}
	class RifeListAdapter extends BaseAdapter  {			// adapter
		class ViewHolder { 
			private TextView 	tvDesc, tvFreqs;
			void setPosinTag(int position) {  
				tvDesc.setTag(position); tvFreqs.setTag(position);
			}
		} 
		private LayoutInflater mInflater;
		private final int BaseRowLayout=R.layout.df_line;

		public RifeListAdapter() { 
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			refresh();
		}
		public int getCount() 					{return rife.getSize(); }
		public Object getItem(int position) 	{return position;}
		public long getItemId(int position) 	{return position;}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) { // create a new viewholder based on row layout
				holder = new ViewHolder();
				convertView = mInflater.inflate(BaseRowLayout, null); // the base row layout
				holder.tvDesc		=(TextView)convertView.findViewById(R.id.tvDesc);
				holder.tvFreqs		=(TextView)convertView.findViewById(R.id.tvFreq);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tvDesc.setText((rife.getItem(position).isOk() ? "":"**") + rife.getItem(position).desc);
			holder.tvFreqs.setText(rife.getItem(position).freqs2AudibleString());

			holder.setPosinTag(position);

			return convertView;
		}
		public void refresh() 					{notifyDataSetChanged(); }
	}
	
	void mess(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
	void clearEditOnClick(final EditText et){ // adds x to clear
		String value = "";//any text you are pre-filling in the EditText

		et.setText(value);
		final Drawable x = getResources().getDrawable(android.R.drawable.presence_offline);//your x image, this one from standard android images looks pretty good actually
		x.setBounds(0, 0, x.getIntrinsicWidth(), x.getIntrinsicHeight());
		et.setCompoundDrawables(null, null, value.equals("") ? null : x, null);
		et.setOnTouchListener(new OnTouchListener() {
		    @Override public boolean onTouch(View v, MotionEvent event) {
		        if (et.getCompoundDrawables()[2] == null) { return false;  }
		        if (event.getAction() != MotionEvent.ACTION_UP) {return false; }
		        if (event.getX() > et.getWidth() - et.getPaddingRight() - x.getIntrinsicWidth()) {
		            et.setText("");
		            et.setCompoundDrawables(null, null, null, null);
		            found=false; setPosition(0); // reset position
		        }
		        return false;
		    }
		});
		et.addTextChangedListener(new TextWatcher() {
		    @Override  public void onTextChanged(CharSequence s, int start, int before, int count) {
		        et.setCompoundDrawables(null, null, et.getText().toString().equals("") ? null : x, null);
		    }
		    @Override  public void afterTextChanged(Editable arg0) {   }
		    @Override  public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
		});
	}
}
