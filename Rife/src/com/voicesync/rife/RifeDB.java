package com.voicesync.rife;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;


public class RifeDB {
	class DBHelper extends SQLiteOpenHelper { // 			dbHelper : table(desc, freq) link _id
		final static String 	DATABASE_NAME="Rife";
		final static int 		DATABASE_VERSION=1; // must be >=1

		public DBHelper(Context context, String name, CursorFactory factory,	int version) {super(context, name, factory, version);}
		public DBHelper(Context context) {	super(context, DATABASE_NAME, null, DATABASE_VERSION);	}
		@Override public void onCreate(SQLiteDatabase db) {
			String createDesc="create table desc (_id integer primary key autoincrement, desc text);";
			String createFreq=
					"create table freq ("+
							"freq 	float not null,"+
							"_id 		integer,"+
							"FOREIGN KEY(_id) REFERENCES id(_id) );"; // FOREIGN KEY(trackartist) REFERENCES artist(artistid)
			dropTables(db);
			db.execSQL(createDesc); 
			db.execSQL(createFreq);
		}
		@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {// upgrade db schema, remove & create new tables
			dropTables(db);
			onCreate(db);
		} 
		void dropTables(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS desc");
			db.execSQL("DROP TABLE IF EXISTS freq");
		}
	}

	private SQLiteDatabase db;
	private	 DBHelper		dbHlp;
	ContentValues values = new ContentValues();
	Cursor cursor;
	long id;	// current id
	int[]ids;   // list of ids in getall

	RifeDB(Context context) { // create helper & open db
		dbHlp = new DBHelper(context);
		db = dbHlp.getWritableDatabase();
	}
	public int size() {
		int n=0;
		Cursor cursorDesc = db.query("desc", null, null, null, null, null, null);
		if (cursorDesc.moveToFirst())  n=cursorDesc.getCount();
		return n;
	}
	public DescFreqMulti[]loadDB() {
		DescFreqMulti []dfm=null;
		Cursor cursorDesc = db.query("desc", null, null, null, null, null, null);
		if (cursorDesc.moveToFirst()) {
			int n=cursorDesc.getCount();
			dfm = new DescFreqMulti[n];
			for (int i=0; i<n; i++) {
				
				String desc=cursorDesc.getString(1);
				id=cursorDesc.getInt(0); // save id for getIds()
				cursor = db.query("freq", null, "_id="+id, null, null, null, null);
				cursor.moveToFirst();
				int nFreq=cursor.getCount();
				float []freq=new float[nFreq];
				for (int j=0; j<nFreq; j++) {
					freq[j]=cursor.getFloat(0);
					cursor.moveToNext();
				}
				dfm[i]=new DescFreqMulti(desc,freq);
				cursorDesc.moveToNext();
			}
		}
		return dfm;
	}
	public int[]getIds() { return ids; } // return ids from getall
	public void deleteSession() { // drop tables(desc)
		db.delete("desc", "_id="+id, null);
		db.delete("freq", "_id="+id, null);
	}
	public int getLastId() {
		cursor = db.query("sqlite_sequence", new String[]{"seq"}, "name='id'" , null, null, null, null);
		if (cursor.moveToFirst()) return (int) (id=cursor.getInt(0)); // after a query MUST moveTo...
		else return -1;			
	}
	public void addParams(String desc, float[]freq) { // insert(desc(desc), freq(id,freq))
		values.clear(); // insert desc(desc)
		values.put("desc", desc);
		id=db.insert("desc", null, values); // save current id

		for (int i=0; i<freq.length; i++) { // insert freq[](...)
			values.clear();
			values.put("_id", 		id);
			values.put("freq", 		freq[i]);
			db.insert("freq", null, values);
		}
	}
	public void close() 		{   db.close(); dbHlp.close(); } // close db
	public void dropTables() 	{	dbHlp.onCreate(db); } // zero(desc,freq) 
}
