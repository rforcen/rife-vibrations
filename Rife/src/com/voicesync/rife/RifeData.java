package com.voicesync.rife;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import android.content.Context;
import android.os.Environment;

public class RifeData {
	class DescFreq {
		public DescFreq(String desc, int freq) {this.desc=desc; this.freq=freq;}
		String desc;
		int freq;
	}
	public DescFreq []mainDF={ // main set of freqs and 
			new DescFreq("Parasites", 		72),  new DescFreq("Staphylococci", 727),
			new DescFreq("Parasites", 		95),  new DescFreq("Diphtheria", 	776 ),
			new DescFreq("Parasites", 		120), new DescFreq("Rubella", 		787 ),
			new DescFreq("Lyme Disease", 	432), new DescFreq("Tuberculosis", 	800 ),
			new DescFreq("Candida", 		465), new DescFreq("E. Coli", 		802 ),
			new DescFreq("BubonicPlague",  	500), new DescFreq("Streptococci", 	880 ),
			new DescFreq("Leprosy", 		600), new DescFreq("Herpes", 		1552), 
			new DescFreq("Syphilis", 		650), new DescFreq("Typhoid", 		1865), 
			new DescFreq("Gonorrhea", 		660), new DescFreq("Sarcoma", 		2008), 
			new DescFreq("H. Pylori", 		676), new DescFreq("Carcinoma", 	2128),
	};
	public ArrayList<DescFreqMulti>dfm=new ArrayList<DescFreqMulti>();
	public DescFreqMulti []rife=null;
	private Context context;

	public RifeData() {}
	public RifeData(Context context) { // parse the file to -> dfm
		setContext(context);
		loadDataFromAssest(); // faster than db read
	}
	public void setContext(Context context) {this.context=context;}
	public void loadDataFromAssest() {
		readAsset();
		removeWrongData();
	}
	private void loadAndWriteDB(Context context) {
		if (dbsize()==0) {
			loadDataFromAssest();
			writeDB();
		}
		loadFromDB();
	}
	public void loadFromDB() {
		RifeDB rdb=new RifeDB(context);
		rife=rdb.loadDB();
		rdb.close();
	}
	public int dbsize( ) {
		RifeDB rdb=new RifeDB(context);
		int n=rdb.size();
		rdb.close();
		return n;
	}
	public void writeDB( ) {
		RifeDB rdb=new RifeDB(context);
		rdb.dropTables();
		for (int i=0; i<dfm.size(); i++) 
			rdb.addParams(dfm.get(i).desc, dfm.get(i).freq);
		rdb.close();
	}
	public DescFreqMulti getItem(int i) { return (rife!=null) ? rife[i % rife.length] : dfm.get(i);	}
	public int getSize() { return (rife!=null) ? rife.length : dfm.size();}
	private void removeWrongData() {
		for (int i=0; i<dfm.size(); i++) {
			if ( ! dfm.get(i).isOk()) {
				dfm.remove(i);
			}
		}
	}
	private void readAsset() {
		Parser parser; char ch;
		int state=0; // 0 - desc, 1 -num list, 2-(...)
		try {
			BufferedReader br=loadAssetReader(context, "freqs/RifeFreqs");
			parser=new Parser(br);
			String lin="", numList="";

			while ((ch=parser.getch())!=0) {
				if (ch<' ') continue;
				if (ch=='(') state=2;
				if (ch==')') state=0;
				if (state!=2 & (ch=='-' | ch==':')) { state=1;  continue; }
				switch (state) {
				case 2:
				case 0: lin+=ch; break;
				case 1: 
					if (parser.isalpha(ch)) {
						if (accept(lin, numList)) {
							dfm.add(new DescFreqMulti(lin.trim(), getFloatList(numList)));
						}
						//						if (dfm.size()>500) {br.close();  return; } // debug: don't load all data, takes ages!!
						lin=""; lin+=ch; 
						numList="";
						state = 0;
					} else
						numList += ch;
					break;
				}

			}
			br.close();
		} catch (IOException e) { e.printStackTrace();	}		
	}
	private boolean accept(String lin, String numList) {
		return lin!=null & numList!=null & !lin.contains("see ") & !lin.contains("Refer:") & numList.length()!=0;
	}
	private float[] getFloatList(String numList) { // removing final zeros
		float[]fl, tl; int ncn=0;
		String[]snl=numList.split(",");
		int ls=snl.length;
		tl=new float[ls];
		try {
			for (int i=0; i<ls; i++) {
				tl[i]=Float.valueOf(snl[i].trim());
				ncn++;
			}
		}catch (NumberFormatException n) {
			n.printStackTrace();
		}
		if (ncn!=0) {
			fl=new float[ncn];
			for (int i=0, ic=0; i<ls; i++) if (tl[i]!=0) fl[ic++]=tl[i]; 
		} else fl=null;
		return fl;
	}

	private BufferedReader loadAssetReader(Context context, String fName) {
		InputStreamReader isr;
		try {
			isr=new InputStreamReader(context.getAssets().open(fName));
		} catch (IOException e) {isr=null;}
		return isr==null ? null : new BufferedReader (isr);
	}
	private void genJavaSourceStructure() { // exceeds 64kb !
		//		 DescFreqMulti []rife= {new DescFreqMulti("desc", new float[]{0,1,2})};
		try {
			FileWriter fw=new FileWriter(Environment.getExternalStorageDirectory()+"/javaRifeConst.java");
			fw.write("public DescFreqMulti []rife= {\n");
			for (int i=0; i<dfm.size(); i++) {
				DescFreqMulti d=dfm.get(i);
				fw.write(String.format("new DescFreqMulti(\"%s\",new float[]{", d.desc));
				for (int j=0; j<d.freq.length; j++)  fw.write(String.format("%sf%c",d.fmt(d.freq[j]), (j<d.freq.length-1)?',':' '));
				fw.write(String.format("})%c\n",(i<dfm.size()-1 ? ',':' ')));
			}
			fw.write("};");
			fw.close();
		} catch (IOException e) { e.printStackTrace();	}
	}

}
