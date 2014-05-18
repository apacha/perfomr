package com.example.helloandroid;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
//import android.os.Message;

class Templates {
	
		Bitmap tplbmp;
		int height;
		int width;
		float ratio;
		int[] template;
		int staff_space;
		int staff_height;
		int[] quarter;
		int[] half;
		int[] full;
		int[] clef;
		int[] dot;
		int[] rest;
		int[] qurest;
		int[] accsh;
		int[] accfl;

		
		public Bitmap loadTemplate(String filename) {
        	
        	Bitmap tplbmp = null;
        	
        	try {
            	File sdcard = Environment.getExternalStorageDirectory();
            	File dir = new File (sdcard.getAbsolutePath() + "/perfomr/data/templates");
            	File file = new File(dir, filename);
            	String source = file.toString();
            	tplbmp = BitmapFactory.decodeFile(source);
            	       	
        		} catch (Exception e) {
    			    	e.printStackTrace();
    			}
    	    	catch (OutOfMemoryError e) {
    		    	//msg = Message.obtain();
    	    		//msg.what = 667;
    	    		//msg.obj = "Sorry, I ran out of memory. Do you want me to try at a lower resolution?";
    	    		//progressHandler.sendMessage(msg);
    				
    		    }
        		
        		return tplbmp;
        	
        }

		
}

class Clef extends Templates {
	
	private Clef() {
	
		tplbmp = loadTemplate("sym_clef.png");
		height = 8*staff_space;
		ratio = (float) tplbmp.getHeight()/height;
		width = (int) (tplbmp.getWidth() / ratio);
		template = new int[width*height];
		tplbmp = Bitmap.createScaledBitmap(tplbmp, width, height, false);
		tplbmp.getPixels(clef, 0, width, 0, 0, width, height);
	
	}
	
	public int[] getTemplate() {
		return clef;
	}
	
}

class Dot extends Templates {
	
	private Dot() {
	
	  	tplbmp = loadTemplate("sym_dot.png");
    	height = 4*staff_height;
		ratio = (float) tplbmp.getHeight()/height;
		width = (int) (tplbmp.getWidth() / ratio);
		template = new int[width*height];
		tplbmp = Bitmap.createScaledBitmap(tplbmp, width, height, false);
		tplbmp.getPixels(dot, 0, width, 0, 0, width, height);
	
	}
	
	
	public int[] getTemplate() {
		return dot;
	}
	
	
}

class Quarter extends Templates {
	
	private Quarter() {
	
		tplbmp = loadTemplate("nh1_4.png");
    	height = staff_space+2*staff_height;
		ratio = (float) tplbmp.getHeight()/height;
		width = (int) (tplbmp.getWidth() / ratio);
		template = new int[width*height];
		tplbmp = Bitmap.createScaledBitmap(tplbmp, width, height, false);
		tplbmp.getPixels(quarter, 0, width, 0, 0, width, height);
	
	}
	
		public int[] getTemplate() {
		return quarter;
	}
	
	
}
	
class Half extends Templates {
	
	private Half() {
	
	  	tplbmp = loadTemplate("nh1_2.png");
    	height = staff_space+2*staff_height;
		ratio = (float) tplbmp.getHeight()/height;
		width = (int) (tplbmp.getWidth() / ratio);
		template = new int[width*height];
		tplbmp = Bitmap.createScaledBitmap(tplbmp, width, height, false);
		tplbmp.getPixels(half, 0, width, 0, 0, width, height);
	
	}
	
	public int[] getTemplate() {
		return half;
	}
	
	
}

class Full extends Templates {
	
	private Full() {
	
		tplbmp = loadTemplate("nh1_1.png");
    	height = staff_space+2*staff_height;
		ratio = (float) tplbmp.getHeight()/height;
		width = (int) (tplbmp.getWidth() / ratio);
		template = new int[width*height];
		tplbmp = Bitmap.createScaledBitmap(tplbmp, width, height, false);
		tplbmp.getPixels(full, 0, width, 0, 0, width, height);
	
	}
	
	public int[] getTemplate() {
		return full;
	}
	
	
}

class Rest extends Templates {
	
	private Rest() {
	
    	tplbmp = loadTemplate("rest.png");
    	height = staff_space/2;
		ratio = (float) tplbmp.getHeight()/height;
		width = (int) (tplbmp.getWidth() / ratio);
		template = new int[width*height];
		tplbmp = Bitmap.createScaledBitmap(tplbmp, width, height, false);
		tplbmp.getPixels(rest, 0, width, 0, 0, width, height);
	
	}
	
	public int[] getTemplate() {
		return rest;
	}
	
	
}

class QuRest extends Templates {
	
	private QuRest() {
	
    	tplbmp = loadTemplate("qu_rest.png");
    	height = staff_space*3+3*staff_height;
		ratio = (float) tplbmp.getHeight()/height;
		width = (int) (tplbmp.getWidth() / ratio);
		template = new int[width*height];
		tplbmp = Bitmap.createScaledBitmap(tplbmp, width, height, false);
		tplbmp.getPixels(qurest, 0, width, 0, 0, width, height);
	
	}
	
	public int[] getTemplate() {
		return qurest;
	}
	
	
}

class AccSharp extends Templates {
	
	private AccSharp() {
	
    	tplbmp = loadTemplate("acc_sh.png");
    	height = staff_space/2;
		ratio = (float) tplbmp.getHeight()/height;
		width = (int) (tplbmp.getWidth() / ratio);
		template = new int[width*height];
		tplbmp = Bitmap.createScaledBitmap(tplbmp, width, height, false);
		tplbmp.getPixels(accsh, 0, width, 0, 0, width, height);
	
	}
	
	public int[] getTemplate() {
		return accsh;
	}
	
	
}

class AccFlat extends Templates {
	
	private AccFlat() {
	
    	tplbmp = loadTemplate("acc_fl.png");
    	height = staff_space/2;
		ratio = (float) tplbmp.getHeight()/height;
		width = (int) (tplbmp.getWidth() / ratio);
		template = new int[width*height];
		tplbmp = Bitmap.createScaledBitmap(tplbmp, width, height, false);
		tplbmp.getPixels(accfl, 0, width, 0, 0, width, height);
	
	}
	
	public int[] getTemplate() {
		return accfl;
	}
	
	
}

		
	
    	
	

