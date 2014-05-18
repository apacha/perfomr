package com.example.helloandroid;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
//import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
//import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Precomp extends Activity {

    boolean test = false;
	String uriString;
	Uri uri;
	String keystring = "";
	String durstring = ":";
	int dur = 0;
	String error = "";
	boolean hasError = false;
	boolean nextisbeam = false;
	boolean success = false;
	int imgW = 320;
	int imgH = 480;
	int height, width, N, inputheight, inputwidth;
	int feedback = 1234;
	int resolution;
	int valid = -1;
	int num_notes = 0;
	String offsets = "";
	String filenamestring = "";
	String sigstring = "";
	int l = 0;
	int staff_height = -1;
	int strip_height = 0;
	int staff_space = -1;
	int numerator = 0;
	int denominator = 0;
	int limit = 0;
	int num_lines = 0;
	int num_staves = 0;
	int first_sym = 9999;
	int pos = 0;
	float skew = 0;
	float somevalue = 0;
	float ratio = 0;
	int clef_pos = 0;
	long startTime, stopTime, elapsedTime;
	TextView text;
	TextView titletext;
	int[] pixels;
	int[] projection;
	int[] lines;
	int[] result;
	int[][] staves;
	private Bitmap bitmap;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
	//private ImageView image;
	Stack<Integer> s;
	boolean recognition;
	boolean highres;
	boolean firststaff = true;
	String src;
	Message msg;
	Uri imageUri;   
	Midifile mf;
	int num_symbols = -1;
	String notestring = "";
	String sig_y;
	String l0, l1, l2, l3, l4;
	MediaPlayer mp;
	Button playButton;
	Button stopButton;
	String source;
	Bundle bundle;
	ImageView image;

	Bitmap resultbmp;
	Bitmap resultbmp2;
	Bitmap staff_bmp;
	

	int[] tpl_clef, tpl_dot, tpl_4, tpl_2, tpl_1, tpl_rest, tpl_qu_rest, tpl_sharp, tpl_flat;
	int w_clef, w_dot, w_4, w_2, w_1, w_rest, w_qu_rest, w_sharp, w_flat;
	int h_clef, h_dot, h_4, h_2, h_1, h_rest, h_qu_rest, h_sharp, h_flat;

	ArrayList<Symbol> symbols = new ArrayList<Symbol>();
	int[][] keys = new int[7][2];
	int key_cnt;
	
	// only called once when app is created
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        bundle = this.getIntent().getExtras();
        setContentView(R.layout.precomp);
        text = (TextView) findViewById(R.id.text);
        titletext = (TextView) findViewById(R.id.titletext);
        progressDialog = ProgressDialog.show(this, "", "");
        progressDialog.setCancelable(true);
        alertDialog = new AlertDialog.Builder(this).create();
		src = bundle.getString("source");
		resolution = bundle.getInt("res");
		uriString = bundle.getString("uri");
		mf = new Midifile();
		image = (ImageView) findViewById(R.id.imageView);
		


		if (src.matches("camera")) {
			
			// input: CAM

			source = "Photo";
	        try {

	            final int IMAGE_MAX_SIZE = resolution;
	        	Uri tmp_uri = Uri.parse(uriString);
				uriString = tmp_uri.getPath();
	        	
				// Decode image size
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				bitmap = BitmapFactory.decodeFile(uriString, o);



				int scale = 1;
				while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
				    scale++;
				}
				
				if (scale > 1) {
				    scale--;
				    // scale to max possible inSampleSize that still yields an image
				    // larger than target
				    o = new BitmapFactory.Options();
				    o.inSampleSize = scale;
				    bitmap = BitmapFactory.decodeFile(uriString, o);

				    // resize to desired dimensions
				    height = bitmap.getHeight();
				    width = bitmap.getWidth();
					
					// check rotation (EXIF tag)
		            int rotate = 0;
		            try {
		                ExifInterface exif = new ExifInterface(uriString);
		                int orientation = exif.getAttributeInt(
		                        ExifInterface.TAG_ORIENTATION,
		                        ExifInterface.ORIENTATION_NORMAL);

		                switch (orientation) {
		                case ExifInterface.ORIENTATION_ROTATE_270:
		                    rotate = 270;
		                    break;
		                case ExifInterface.ORIENTATION_ROTATE_180:
		                    rotate = 180;
		                    break;
		                case ExifInterface.ORIENTATION_ROTATE_90:
		                    rotate = 90;
		                    break;
		                }
		            } catch (Exception e) {
		                e.printStackTrace();
		            }

		            Matrix mtx = new Matrix();
		            mtx.postRotate(rotate);
		            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, mtx, true);
				    
				    height = bitmap.getHeight();
				    width = bitmap.getWidth();
		            
				    double y = Math.sqrt(IMAGE_MAX_SIZE
				            / (((double) width) / height));
				    double x = (y / height) * width;

				    bitmap = Bitmap.createScaledBitmap(bitmap, (int) x,     (int) y, true);
			
				}
				
				precompute();
	        }
	        
	        catch (OutOfMemoryError e) {
	        	msg = Message.obtain();
				msg.what = 667;
				msg.obj = "Sorry, I ran out of memory. Do you want me to try at a lower resolution?";
				progressHandler.sendMessage(msg);
				
	        }
			
		}
		
		else {
			
			// input: FILE	        	
			Intent intent = new Intent();
	        intent.setType("image/*");
	        intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
		
		}
		
			

    }

    
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

        
        switch(requestCode) { 
        case 1:
            if(resultCode == RESULT_OK){  
                uri = imageReturnedIntent.getData();
                
                uriString = getRealPathFromURI(uri);
				source = uriString;
				
					try {
						getBitmap(uri);				
		                precompute();
					}
			        catch (OutOfMemoryError e) {
			        	msg = Message.obtain();
						msg.what = 667;
						msg.obj = "Sorry, I ran out of memory. Do you want me to try at a lower resolution?";
						progressHandler.sendMessage(msg);
						
			        }

            }
            else
            	  progressDialog.dismiss();
        }
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null); 
        cursor.moveToFirst(); 
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
        return cursor.getString(idx); 
    }
    
    private void getBitmap(Uri uri) {

        final int IMAGE_MAX_SIZE = resolution;
        

		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(uriString, o);

		int scale = 1;
		while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
		    scale++;
		}
		
		if (scale > 1) {
		    scale--;
		    // scale to max possible inSampleSize that still yields an image
		    // larger than target
		    o = new BitmapFactory.Options();
		    o.inSampleSize = scale;
		    bitmap = BitmapFactory.decodeFile(uriString, o);

		    // resize to desired dimensions
		    height = bitmap.getHeight();
		    width = bitmap.getWidth();
		  
		    double y = Math.sqrt(IMAGE_MAX_SIZE
		            / (((double) width) / height));
		    double x = (y / height) * width;

		    bitmap = Bitmap.createScaledBitmap(bitmap, (int) x,     (int) y, true);
		    
		} else {
		    
		}
		

    }

    
    private Handler progressHandler = new Handler() {
    	
    	public void handleMessage(Message msg) {

    		if (msg.what == 0)
    			progressDialog.setMessage("Computation started...");
    		else if (msg.what == 1)
    			progressDialog.setMessage("Scaling image...");
    		else if (msg.what == 2)
    			progressDialog.setMessage("Correcting skew...");
    		else if (msg.what == 3)
    			progressDialog.setMessage("Extracting staves...");
    		else if (msg.what == 4) {
    			int num = (Integer) msg.obj;
    			progressDialog.setMessage("Detecting symbols from staff\n\n" + num + " of " + num_staves + ".\n\nThis could take some time...");
    		}
    		else if (msg.what == 8282) {
    			// DEBUG
    			progressDialog.setMessage(String.valueOf(height) + ", " + String.valueOf(width));
    		}
    		else if (msg.what == 666) {
    			progressDialog.dismiss();
    			String errormsg = (String) msg.obj;
    			//String errormsg = String.valueOf("NUMSTAVES="+num_staves+", SKEW="+skew+"\nstaff_height = " + staff_height + "\nstaff_space = " + staff_space + "\nnum_lines = " + num_lines);
    			//String errormsg = String.valueOf("width = "+width+", height = "+height);

    			alertDialog.setTitle("Oops!");
    			alertDialog.setMessage(errormsg);
    			alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
						
						return;  
					}
				});

    			alertDialog.show();
    			
    		}
    		else if (msg.what == 667) {
    			progressDialog.dismiss();
    			String errormsg = (String) msg.obj;
    			
    			alertDialog.setTitle("Oops!");
    			alertDialog.setMessage(errormsg);

				resolution -= 1000000;
				int resmp = resolution/1000000;
    			alertDialog.setButton(-1, "Yes @ " + String.valueOf(resmp) + "MP", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent intent = getIntent();
			            bundle.putInt("res", resolution);
			            bundle.putString("uri", uriString);
			            intent.putExtras(bundle);
			            System.gc();
						finish();
						startActivity(intent);
						
						return;  
					}
				});
    			
    			alertDialog.setButton(-2, "No", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
    		        	return;  
					}
				});
    			
    			
    		  	alertDialog.show();
    		}
    		
    	}
    	
    };
    
    private Handler handler = new Handler() {
    	
    	public void handleMessage(Message msg) {
    		
    		if (msg.what == 0) {
    			

	    		
	            progressDialog.dismiss();
	    		stopTime = System.currentTimeMillis();
	    		elapsedTime = Math.round((stopTime-startTime)/10);
	    		elapsedTime = elapsedTime/100;    		
	    		
	    		titletext.setText(source);
	    		titletext.setTextSize(22);
	    		//titletext.setTextColor(Color.YELLOW);
	    		text.setTextSize(14);
				text.setText("\nDuration: " + durstring + "\nProcessing time: " + String.valueOf(elapsedTime) + "s" + "\n" + "\nKey accidentals: " + sigstring + "\nSong Key: " + keystring + "\nNumber of staves: " + num_staves + "\nPage skew: " + skew + "°" + "\nWidth: " + width + "\nHeight: " + height);
	    		//text.setText(String.valueOf("NUMSTAVES="+num_staves+", SKEW="+skew+"\nstaff_height = " + staff_height + "\nstaff_space = " + staff_space));    	
	    		//text.setText("Key " + sig_y + ", l0 " + l0 + ", l1 " + l1 + ", l2 " + l2 + ", l3 " + l3 + ", l4 " + l4);
	    		//text.setText(filenamestring);
	    		
				image.setImageBitmap(staff_bmp);
				image.setAdjustViewBounds(true);
				Display display = getWindowManager().getDefaultDisplay();
				DisplayMetrics dm = new DisplayMetrics();
				display.getMetrics(dm);
				int maxWidth = dm.widthPixels;
				int maxHeight = dm.heightPixels-100;
				image.setMaxHeight(maxHeight);
				image.setMaxWidth(maxWidth);
				
    			
    		}
    	}
    	
    };


	
    public void precompute() {
    	
    	Runnable runnable = new Runnable() {
    		
    		public void run() {
    			    			
        		msg = Message.obtain();
    			msg.what = 0;    			
    			progressHandler.sendMessage(msg);
    			startTime = System.currentTimeMillis();

    			height = bitmap.getHeight();
    			inputheight = height;
    			width = bitmap.getWidth();
    			inputwidth = width;
    			
    			try {

    				cropImage2(bitmap);
    				resultbmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    				resultbmp.setPixels(pixels, 0, width, 0, 0, width, height);
    				saveToFile(resultbmp, "post_cropping.png");    
    					    			
	    			// downscale image if necessary to prevent OOM error
	    			//handler.sendEmptyMessage(1);
	    			
	    			msg = Message.obtain();
	    			msg.what = 1;
	    			progressHandler.sendMessage(msg);
	        		    			   			
		        	msg = Message.obtain();
	    			msg.what = 2;    			
	    			progressHandler.sendMessage(msg);
	    			
		        	int valid = detectSkew();	

		        	
		        	if (valid != -1 && num_staves > 0) {
		        		msg = Message.obtain();
		    			msg.what = 3;    			
		    			progressHandler.sendMessage(msg);
		    			loadTemplates();
		    			computeStaves();
		    			
		    			handler.sendEmptyMessage(0);
		    					    			
		            	File sdcard = Environment.getExternalStorageDirectory();
		            	File dir = new File (sdcard.getAbsolutePath() + "/perfomr");
		            	dir.mkdirs();
		            	File file = new File(dir, "output.mid");
			        	
		    			//staff_bmp = bitmap;
			        	try {
			                mf.writeToFile("output.mid");
			                
			    	    } catch (Exception e) {
			    	    	e.printStackTrace();
			    	    }
		    			
			        	FileInputStream fis = null;
		    			FileDescriptor fd = null;
			    		
		    			mp = new MediaPlayer();
		    			try {
							fis = new FileInputStream(file);
							fd = fis.getFD();
							mp.setDataSource( fd );
							mp.prepare();
							int secs = (int)((mp.getDuration()/1000)%60);
							String secstring = "";
							if (secs < 10)
								secstring = "0"+String.valueOf(secs);
							else
								secstring = String.valueOf(secs);
								
							durstring = (int)(mp.getDuration()/1000/60) + ":" + secstring;
							
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    			mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {    				
		    				
		    				public void onCompletion(MediaPlayer mp) {
		    					bitmap.recycle();
		    					bitmap = null;
		    	                playButton.setText("Play again");
		    	                mp.seekTo(0);
		    	            }
		    	        });
		    			
	
		    			
	
		    			
		        	}
		        	else {	        		
		        		msg = Message.obtain();
		    			msg.what = 666;
		    			msg.obj = "Notation could not be detected on the input image. Resolution might be too low.";
		    			progressHandler.sendMessage(msg);
		        	}
		        	
	    			playButton = (Button)findViewById(R.id.playbutton);
	    			playButton.setOnClickListener(new View.OnClickListener(){
	
	    			    public void onClick(View v) {
	    				    // TODO Auto-generated method stub
	    			    	if (mp != null) {
	    					    if(!mp.isPlaying()){
	    					    	mp.start();
	    					    	playButton.setText("Pause");
	    			    		}
	    					    else {
	    					    	mp.pause();
	    					    	playButton.setText("Play");
	    					    }
	    				    }
	    		    	}
	    		    });
	    			
	    			
	    			
	    			handler.sendEmptyMessage(0);
    			
				}
				catch (OutOfMemoryError e) {
			    	msg = Message.obtain();
					msg.what = 667;
					msg.obj = "Sorry, I ran out of memory. Do you want me to try at a lower resolution?";
					progressHandler.sendMessage(msg);
					
			    }
    			
	        	

    		}
    	};
    	new Thread(runnable).start();
    	
               
    }
    
public void cropImage(Bitmap bmp) {
		
		width = bmp.getWidth();
		height = bmp.getHeight();
		
		// downscale in order to remove noise
		Bitmap bitmap_ARGB888 = Bitmap.createScaledBitmap(bmp.copy(Config.ARGB_8888, false), width/10, height/10, false);
		Mat inputM = Utils.bitmapToMat(bitmap_ARGB888);
		Mat edgeM = new Mat();

		Imgproc.cvtColor(inputM, edgeM, Imgproc.COLOR_RGBA2GRAY);
		
		//Imgproc.medianBlur(edgeM, edgeM, 7);
		Imgproc.threshold(edgeM, edgeM, 127, 255, Imgproc.THRESH_OTSU);
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,5), new Point(-1,-1));
		Mat edgeM2 = edgeM.clone();

		Mat cont = new Mat();
		Rect maxroi = new Rect();
		Rect secroi = new Rect();
		Rect roi = new Rect();
		boolean first =true;
		
		while (secroi.width < edgeM.width()*0.6 || secroi.height < edgeM.height()*0.3) {
			
			maxroi = new Rect();
			secroi = new Rect();
			roi = new Rect();
			
			if (!first)
				Imgproc.erode(edgeM2, edgeM2, kernel);
			first = false;
			
			List<Mat> contours = new ArrayList<Mat>();
			Mat hierarchy = new Mat();
			Imgproc.findContours(edgeM2, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
			
			for (int i = 0; i < contours.size(); i++) {
				
				cont = contours.get(i);
				
				List<Point> points = new ArrayList<Point>(5);
				int num = (int) cont.total();
				int buff[] = new int[num*2];
				cont.get(0, 0, buff);
				
				for (int j = 0; j < num*2; j+=2) 
					points.add(new Point(buff[j], buff[j+1]));

				//Core.rectangle(mask, Imgproc.boundingRect(points).br(), Imgproc.boundingRect(points).tl(), new Scalar(255,255,255),-1);

				roi = Imgproc.boundingRect(points);
				if (roi.area() > maxroi.area()) {
					secroi = maxroi;
					maxroi = roi;
				}
				else if (roi.area() > secroi.area()) {
					secroi = roi;
				}
			
			}		
			
		}
		
		roi = secroi;
		roi.height = roi.height * 10;
		roi.width = roi.width * 10;
		roi.x = roi.x*10;
		roi.y = roi.y*10;
		Bitmap resultbmp = Bitmap.createBitmap(bmp, roi.x, roi.y, roi.width, roi.height);
    	pixels = new int[roi.width*roi.height];
    	width = roi.width;
    	height = roi.height;
		resultbmp.getPixels(pixels, 0, roi.width, 0, 0, roi.width, roi.height);  
		bitmap_ARGB888.recycle();
		resultbmp.recycle();
		
		
	}

public void cropImage2(Bitmap bmp) {
	
	width = bmp.getWidth();
	height = bmp.getHeight();
	
	// downscale in order to remove noise
	Bitmap bitmap_ARGB888 = Bitmap.createScaledBitmap(bmp.copy(Config.ARGB_8888, false), width/10, height/10, false);
	Mat inputM = Utils.bitmapToMat(bitmap_ARGB888);
	Mat edgeM = new Mat();

	Imgproc.cvtColor(inputM, edgeM, Imgproc.COLOR_RGBA2GRAY);
	
	//Imgproc.medianBlur(edgeM, edgeM, 7);
	Imgproc.threshold(edgeM, edgeM, 127, 255, Imgproc.THRESH_BINARY);
	Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,5), new Point(-1,-1));
	Mat edgeM2 = edgeM.clone();

	Mat cont = new Mat();
	Rect maxroi = new Rect();
	Rect secroi = new Rect();
	Rect roi = new Rect();
	Rect temproi = new Rect();
	boolean first =true;
	
	
		maxroi = new Rect();
		secroi = new Rect();
		roi = new Rect();
		
		if (!first)
			Imgproc.erode(edgeM2, edgeM2, kernel);
		first = false;
		
		List<Mat> contours = new ArrayList<Mat>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(edgeM2, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point (0,0));
		
		for (int i = 0; i < contours.size(); i++) {
			
			cont = contours.get(i);
			
			List<Point> points = new ArrayList<Point>(5);
			int num = (int) cont.total();
			int buff[] = new int[num*2];
			cont.get(0, 0, buff);
			
			for (int j = 0; j < num*2; j+=2) 
				points.add(new Point(buff[j], buff[j+1]));

			//Core.rectangle(mask, Imgproc.boundingRect(points).br(), Imgproc.boundingRect(points).tl(), new Scalar(255,255,255),-1);

			roi = Imgproc.boundingRect(points);
			if (roi.area() > maxroi.area()) {
				secroi = maxroi;
				maxroi = roi;
			}
			else if (roi.area() > secroi.area()) {
				secroi = roi;
			}
		
		}	
	
	roi = maxroi;
	roi.height = roi.height * 10;
	roi.width = roi.width * 10;
	roi.x = roi.x*10;
	roi.y = roi.y*10;
	Bitmap resultbmp = Bitmap.createBitmap(bmp, roi.x, roi.y, roi.width, roi.height);


	
	inputM = Utils.bitmapToMat(resultbmp);
	resultbmp.recycle();
	edgeM = new Mat();

	Imgproc.cvtColor(inputM, edgeM, Imgproc.COLOR_RGBA2GRAY);
	
	//Imgproc.medianBlur(edgeM, edgeM, 7);
	Imgproc.threshold(edgeM, edgeM, 127, 255, Imgproc.THRESH_OTSU);
	kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,5), new Point(-1,-1));
	edgeM2 = edgeM.clone();

	cont = new Mat();
	maxroi = new Rect();
	secroi = new Rect();
	roi = new Rect();
	first =true;
	int counter = 0;
	
	while ((secroi.width < edgeM.width()*0.6 || secroi.height < edgeM.height()*0.3) && counter < 20) {

		counter++;
		
		maxroi = new Rect();
		secroi = new Rect();
		roi = new Rect();
		
		if (!first)
			Imgproc.erode(edgeM2, edgeM2, kernel);
		first = false;
		
		contours = new ArrayList<Mat>();
		hierarchy = new Mat();
		Imgproc.findContours(edgeM2, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		for (int i = 0; i < contours.size(); i++) {
			
			cont = contours.get(i);
			
			List<Point> points = new ArrayList<Point>(5);
			int num = (int) cont.total();
			int buff[] = new int[num*2];
			cont.get(0, 0, buff);
			
			for (int j = 0; j < num*2; j+=2) 
				points.add(new Point(buff[j], buff[j+1]));

			//Core.rectangle(mask, Imgproc.boundingRect(points).br(), Imgproc.boundingRect(points).tl(), new Scalar(255,255,255),-1);

			roi = Imgproc.boundingRect(points);
			if (roi.area() > maxroi.area()) {
				secroi = maxroi;
				maxroi = roi;
			}
			else if (roi.area() > secroi.area()) {
				secroi = roi;
			}
		
		}
		
	}

		roi = secroi;
	resultbmp = Bitmap.createBitmap(bmp, roi.x, roi.y, roi.width, roi.height);
	pixels = new int[roi.width*roi.height];
	width = roi.width;
	height = roi.height;
	resultbmp.getPixels(pixels, 0, roi.width, 0, 0, roi.width, roi.height);  
	bitmap_ARGB888.recycle();
	resultbmp.recycle();
	
	
}



    
	protected void onDestroy() {

		
		super.onDestroy();
	}
    
	public void onBackPressed() {

		if (mp != null) {
			mp.stop();
			mp.release();
		}
		finish();
		
	}  
    
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
		    	msg = Message.obtain();
				msg.what = 667;
				msg.obj = "Sorry, I ran out of memory. Do you want me to try at a lower resolution?";
				progressHandler.sendMessage(msg);
				
		    }
    		
    		return tplbmp;
    	
    }
    
    
    private void loadTemplates() {
		
		Bitmap tplbmp;
		
    	tplbmp = loadTemplate("sym_clef.png");
		h_clef = 8*staff_space;
    	ratio = (float) tplbmp.getHeight()/h_clef;
    	w_clef = (int) (tplbmp.getWidth() / ratio);
    	tpl_clef = new int[w_clef*h_clef];
    	tplbmp = Bitmap.createScaledBitmap(tplbmp, w_clef, h_clef, false);
    	tplbmp.getPixels(tpl_clef, 0, w_clef, 0, 0, w_clef, h_clef);
    	
    	tplbmp = loadTemplate("sym_dot.png");
    	h_dot = 4*staff_height;
    	ratio = (float) tplbmp.getHeight()/h_dot;
    	w_dot = (int) (tplbmp.getWidth() / ratio);
    	tpl_dot = new int[w_dot*h_dot];
    	tplbmp = Bitmap.createScaledBitmap(tplbmp, w_dot, h_dot, false);
    	tplbmp.getPixels(tpl_dot, 0, w_dot, 0, 0, w_dot, h_dot);
		
    	tplbmp = loadTemplate("nh1_4.png");
    	h_4 = staff_space+2*staff_height;
    	ratio = (float) tplbmp.getHeight()/h_4;
    	w_4 = (int) (tplbmp.getWidth() / ratio);
    	tpl_4 = new int[w_4*h_4];
    	tplbmp = Bitmap.createScaledBitmap(tplbmp, w_4, h_4, false);
    	tplbmp.getPixels(tpl_4, 0, w_4, 0, 0, w_4, h_4);
    	
    	tplbmp = loadTemplate("nh1_2.png");
    	h_2 = staff_space+2*staff_height;
    	ratio = (float) tplbmp.getHeight()/h_2;
    	w_2 = (int) (tplbmp.getWidth() / ratio);
    	tpl_2 = new int[w_2*h_2];
    	tplbmp = Bitmap.createScaledBitmap(tplbmp, w_2, h_2, false);
    	tplbmp.getPixels(tpl_2, 0, w_2, 0, 0, w_2, h_2);
    		
    	tplbmp = loadTemplate("nh1_1.png");
    	h_1 = staff_space+2*staff_height;
    	ratio = (float) tplbmp.getHeight()/h_1;
    	w_1 = (int) (tplbmp.getWidth() / ratio);
    	tpl_1 = new int[w_1*h_1];
    	tplbmp = Bitmap.createScaledBitmap(tplbmp, w_1, h_1, false);
    	tplbmp.getPixels(tpl_1, 0, w_1, 0, 0, w_1, h_1);
    	
    	tplbmp = loadTemplate("rest.png");
    	h_rest = staff_space/2;
    	ratio = (float) tplbmp.getHeight()/h_rest;
    	w_rest = (int) (tplbmp.getWidth() / ratio);
    	tpl_rest = new int[w_rest*h_rest];
    	tplbmp = Bitmap.createScaledBitmap(tplbmp, w_rest, h_rest, false);
    	tplbmp.getPixels(tpl_rest, 0, w_rest, 0, 0, w_rest, h_rest);
    	
    	tplbmp = loadTemplate("qu_rest.png");
    	h_qu_rest = staff_space*3+3*staff_height;
    	ratio = (float) tplbmp.getHeight()/h_qu_rest;
    	w_qu_rest = (int) (tplbmp.getWidth() / ratio);
    	tpl_qu_rest = new int[w_qu_rest*h_qu_rest];
    	tplbmp = Bitmap.createScaledBitmap(tplbmp, w_qu_rest, h_qu_rest, false);
    	tplbmp.getPixels(tpl_qu_rest, 0, w_qu_rest, 0, 0, w_qu_rest, h_qu_rest);
    	
    	tplbmp = loadTemplate("acc_sh.png");
    	h_sharp = staff_space/2;
    	ratio = (float) tplbmp.getHeight()/h_sharp;
    	w_sharp = (int) (tplbmp.getWidth() / ratio);
    	tpl_sharp = new int[w_sharp*h_sharp];
    	tplbmp = Bitmap.createScaledBitmap(tplbmp, w_sharp, h_sharp, false);
    	tplbmp.getPixels(tpl_sharp, 0, w_sharp, 0, 0, w_sharp, h_sharp);
		
    	tplbmp = loadTemplate("acc_fl.png");
    	h_flat = staff_space/2;
    	ratio = (float) tplbmp.getHeight()/h_flat;
    	w_flat = (int) (tplbmp.getWidth() / ratio);
    	tpl_flat = new int[w_flat*h_flat];
    	tplbmp = Bitmap.createScaledBitmap(tplbmp, w_flat, h_flat, false);
    	tplbmp.getPixels(tpl_flat, 0, w_flat, 0, 0, w_flat, h_flat);
    	
	}
	
    public void saveToFile (Bitmap bitmap, String fileName) {
    	
    	try {
        	File sdcard = Environment.getExternalStorageDirectory();
        	File dir = new File (sdcard.getAbsolutePath() + "/perfomr");
        	dir.mkdirs();
        	File file = new File(dir, fileName);
            FileOutputStream out = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(out);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
            
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
    	
    }

    
    public void computeStaves() {
    	
    	// reset values
		first_sym = -1;
    	int current = -1;
    	int last = -1;
    	int j = 0;
    	int[] staff = null;
    	lines = new int[5];
    	
    	for (int i = 0; i < num_staves; i++) {
    		//for (int i = 2; i < 3; i++) {
    		
    		msg = Message.obtain();
    		msg.what = 4;
    		msg.obj = i+1;
    		progressHandler.sendMessage(msg);
    		
    		j = 0;
    		current = Math.max(staves[i][0], staves[i][1]-3*staff_space);
    		last = Math.min(staves[i][5]+3*staff_space, staves[i][6]);
    		strip_height = last-current+1;
    		
    		// save relative positions of lines for each staff
    		for (int k = 0; k < 5; k++)
    			lines[k] = staves[i][k+1] - current;
    		
    		staff = new int[width*strip_height];
    		
    		while (current <= last) {
    			
    			for (int x = 0; x < width; x++) {
	    			staff[j*width+x] = pixels[current*width+x];
    			}
    			j++;
    			current++;
    			
    		}
    			    	
    		l0 = String.valueOf(lines[0]);
    		l1 = String.valueOf(lines[1]);
    		l2 = String.valueOf(lines[2]);
    		l3 = String.valueOf(lines[3]);
    		l4 = String.valueOf(lines[4]);
    		int pitch;
    		int[] key;
    		
    		// start recognition algorithm
   			identifySymbols(staff, i, lines, strip_height, width);
   			
			staff_bmp = Bitmap.createBitmap(width, strip_height, Bitmap.Config.RGB_565);
			staff_bmp.setPixels(staff, 0, width, 0, 0, width, strip_height);
			saveToFile(staff_bmp, "staff_"+i+".png");
   			   			
   			Collections.sort(symbols);
   			Symbol sym;
   			int num_s = symbols.size();

   			for (int s = 0; s < num_s; s++) {
				sym = symbols.get(s);
				//notestring += String.valueOf(sym.getPitch()) + ", ";
				pitch = sym.getPitch();
				if (pitch != -1) {
					
		   			keystring = getKey(keys);
					
					for (int k = 0; k < keys.length; k++) {
						
						if (keys[k][0] != 0) {
						
							key = keys[k];
							if (pitch%12 == key[1]%12) {
								if (key[0] == 2) // SHARP
									pitch+=1;
								else
									pitch-=1;
							}
						}
					}
					mf.noteOnOffNow ((int)(sym.getDur()*0.66), pitch+12, 127);
				}
				else // pause
					mf.noteOnOffNow ((int)(sym.getDur()*0.66), 60, 0);
				
				
    		}
    		
    		symbols.clear();
    		
    	}
    	
    }
    

    public int detectSkew()  {
        
 	
    	// 0 = global threshold
    	deskewImage(pixels);   
    	
		resultbmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		resultbmp.setPixels(pixels, 0, width, 0, 0, width, height);
		saveToFile(resultbmp, "post_deskewing.png");
    	
    	binariseImage(pixels);
		
		resultbmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		resultbmp.setPixels(pixels, 0, width, 0, 0, width, height);
		saveToFile(resultbmp, "post_binarisation.png");
    	
    	//pixels = smoothImage(pixels, height, width);
    	projection = horizontalProjection(pixels, height, width);
    	staves = new int[num_lines][7];
    	
    	int[] staff_pos = new int[height];
    	valid = estimateStaffs(pixels, height, width);
    	
		resultbmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		resultbmp.setPixels(pixels, 0, width, 0, 0, width, height);
		saveToFile(resultbmp, "post_eststaffs.png");
    	
    	if (valid != -1) {
    		staff_pos = removeStaves(projection, height, width);
    		if (num_lines > 0)
    			staves = extractStaves(staff_pos);
    			
    	}
    	
		resultbmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		resultbmp.setPixels(pixels, 0, width, 0, 0, width, strip_height);
		saveToFile(resultbmp, "post_removestaffs.png");
    	
    	return valid;
    	    	
    }

    
public void matchNotes(int[] staff, int[] tpl, int[] dot, int[] proj, int[] lines, int min, int new_h, int new_w, int d_h, int d_w, float matchingThreshold, String mode, int pos, int duration) {
    	
    	int histThr = 3*staff_space;
    	int l, r, lr, rl;
    	boolean aborted = false;
    	boolean has_dot = false;
    	int left_boundary = pos + 5*staff_space;
		int[][] notes = new int[9999][6];
		int num_cnd = 0;
		int tpl_size = new_w * new_h;
    	int earlyThr = (int) (matchingThreshold * tpl_size);
    	int stemThr = 2*staff_space;
		int half_dist = (lines[1] - lines[0]) /2;
		int num_stems = 0;
		int error = 0;
		int valI, valT;
		float coeff;
		int N = strip_height*width;
		
    	for (int i = left_boundary; i < width; i++) {
    		
    		if (proj[i] >= histThr) {
    			
    			//for (int y = 0; y < strip_height; y++)
    			//	staff[y*width + i] = (255 << 16) | (0 << 8) | 0;
    			
    			// find note head candidates on projection peaks (local) with template matching.
    			// between lines, on line or on supplementary lines
    			// size of note head == staff_space -> scale template
    			
    			l =  Math.max(0, i-2*staff_space);
    			lr = i-staff_space;
    			rl = i-staff_space;
    			r =  Math.min(width-1, i+2*staff_space);
    			
    			int window_width = r-l;

    			
    			// TEMPLATE MATCHING
    			// calculate SAD and maximize matching function
    			
    			int hspan = window_width - new_w+1;
    			int index = 0;
    			
    			if (hspan > 0) {
    				
    				for (int y = 0; y < strip_height; y++) {
    				
    					if (y == lines[0] || y == lines[1] || y == lines[2] || y == lines[3] || y == lines[4]) {
    						
    						for (int yh = 0; yh < 2; yh++) {
    						
	    						for (int x = l; x < r-new_w; x++) {
	    						
	    							if (x < lr || x > rl) {
	    							
		    							error = 0;
		    							aborted = false;
		    							
		    							// stop if match not possible anymore
		    							
		    							while (!aborted) {
		    							
				    						for (int y2 = 0; y2 < new_h; y2++) {
				    							for (int x2 = 0; x2 < new_w; x2++) {
				    								
				    								index = (y+y2)*width+x+x2;
				    								if (index < staff.length) {				    								
					    								valI = (staff[(y+y2)*width+x+x2] >> 16) & 0xff;
					    								valT = (tpl[y2*new_w+x2] >> 16) & 0xff;
					    								
					    								if (Math.abs(valI-valT) > 128) {	     								
					    									error++;
					    									if (error > earlyThr)
					    										aborted = true;
					    								}				    								
				    								}
				    								else {				    									
				    									
				    									coeff = matchingThreshold;
				    									y2 = new_h;
				    									x2 = new_w;
				    									aborted = true;
				    								}
				    								
				    								
				    								
				    							}   							
				    						}
				    						
				    						aborted = true;
				    						
		    							}
			    						
			    						coeff = (float)error/tpl_size;
			    						boolean has_succ = false;
			    						int idx = 0;
			    						int len1 = 0, len2 = 0;
			    						int max = 0;
			    						int maxpos = -1;
			    						num_stems = 0;
			    		    			if (coeff < matchingThreshold) {
		    				
			    		    				// MATCH!
			    		    							    		    				
			    		    				notes[num_cnd][0] = y;
			    		    				notes[num_cnd][1] = x;
			    		    				notes[num_cnd][2] = y+new_h-1;
			    		    				notes[num_cnd][3] = x+new_w-1;
			    		    				notes[num_cnd][4] = (int) (coeff*100);
			    		    				notes[num_cnd][5] = 0;
			    		    				
			    		    				int centre_x = x+Math.round((float)new_w/2);			    		    				
			    		    				
				    						for (int y2 = 0; y2 < new_h; y2++) {
				    							for (int x2 = 0; x2 < new_w; x2++) {
				    								index = (y+y2)*width+x+x2;
				    								if (index < staff.length) {	
					    								if (staff[index] == 0) {
					    									if (mode == "1_4")
					    										staff[(y+y2)*width+x+x2] = (255 << 16) | (255 << 8) | 255;
					    									else if (mode == "1_2")
					    										staff[(y+y2)*width+x+x2] = (255 << 16) | (255 << 8) | 255;
					    									else
					    										staff[(y+y2)*width+x+x2] = (255 << 16) | (255 << 8) | 255;
					    								}
				    								}
				    								else {
				    									x2 = new_w;
				    									y2 = new_h;
				    								}
				    							}
			    		    				}
/*				    						
				    						int y3 = notes[num_cnd][0]+Math.round((float)new_h/2);
			    							for (int x2 = 0; x2 < new_w; x2++) {
			    								
			    								staff[y3*width+x+x2] = (255 << 16) | (0 << 8) | 0;
			    								
			    							}
				    			    		*/
				    						max = 0;
				    						maxpos = -1;				
				    									

				    						if (centre_x < i) {
			    		    					
// STEM: right upper corner
		    									for (int k = centre_x; k < centre_x+staff_space; k++) {
					    							
					    							if (proj[k] > max) {
					    								max = proj[k];
					    								maxpos = k;
					    								i = maxpos;
					    							}
					    							
					    						}
					    						
					    						idx = (y-1)*width + i;
					    						//staff[idx] = (255 << 16) | (0 << 8) | 0;
			    								has_succ = true;
			    												    								
			    								while (has_succ && idx < staff.length) {
			    								
			    									has_succ = false;
			    									len1++;
			    									
			    									if (staff[idx] == 0) {			    										
			    										staff[idx] = (255 << 16) | (0 << 8) | 255;
			    										idx = idx - width;
			    										has_succ = true;			    										
			    									}
			    									else if (staff[idx-1] == 0) {			    										
			    										staff[idx-1] = (255 << 16) | (0 << 8) | 255;
			    										idx = idx - width;
			    										has_succ = true;			    										
			    									}
			    									else if (staff[idx+1] == 0) {			    										
			    										staff[idx+1] = (255 << 16) | (0 << 8) | 255;
			    										idx = idx - width;
			    										has_succ = true;			    										
			    									}
			    									
			    									
				    								/*for (int j = -2*staff_height; j <= 2*staff_height; j++) {
				    									
				    									if (staff[idx+j] == 0) {
				    										staff[idx] = (255 << 16) | (0 << 8) | 255;
				    										idx = idx - width + j;
				    										has_succ = true;
				    											
				    									}
				    								}*/				    								
			    								}
			    								
			    								if (len1 > stemThr) {				    									
			    									num_stems = 1;

			    									// start looking for hook, beam
			    									int off_n = 0;
			    									int off_s = 0;
			    									int v_travel = 0;
			    									int h_travel = 0;
			    									int v_adj = 0;
			    									int steps = 0;
			    									boolean has_hook = false;
			    									boolean has_beam = false;
			    									
			    									idx += (int) width*(staff_space/2);
			    									
			    									int i2 = i;
			    									while (proj[i2] >= histThr) {
			    										for (int y2 = 0; y2 < strip_height; y2++)
			    											staff[y2*width + i2] = (255 << 16) | (255 << 8) | 255;
			    										idx++;
			    										i2++;
			    									}
			    									
			    									// seedpoint for hook/beam tracking algorithm (toyama)
			    									staff[idx] = (0 << 16) | (255 << 8) | 255;
		    										
			    									// go downwards
			    									int idx_old = idx;
			    									
			    									while (steps < 2*staff_space && !has_hook && !has_beam) {
			    										
			    										v_travel = 0;
			    										h_travel = 0;
			    										//while (staff[idx+1] == 0)
			    											idx++;
			    										
			    										for (int j = 0; j < staff_space; j++) {
				    										
			    											if (idx+(j*width) < N) {
				    											
					    										if (staff[idx-(j*width)] == 0)
					    											off_n++;
					    										if (staff[idx+(j*width)] == 0)
					    											off_s++;
			    											}
			    											else
			    												break;
				    												    										
				    									}
				    									
				    									while (off_n > staff_height || off_s > staff_height) {
					    									
				    										off_n = 0;
				    										off_s = 0;
				    										
				    										for (int j = 0; j < staff_space; j++) {
					    										
					    										if (staff[idx-(j*width)] == 0)
					    											off_n++;
					    										if (staff[idx+(j*width)] == 0)
					    											off_s++;
					    												    										
					    									}
					    									v_adj = (off_s - off_n) / 2;
					    									staff[idx] = (255 << 16) | (0 << 8) | 0;
					    									idx += v_adj*width+1;
					    									h_travel++;
					    									v_travel += v_adj;
					    									
					    									
					    									if (v_travel > (1.5 * staff_space)) {
					    										// hook detected
					    										has_hook = true;
					    										break;
					    									}
					    									
					    									if (h_travel > (3*staff_space)) {
					    										// beam detected
					    										has_beam = true;
					    										break;
					    										
					    									}
					    										
				    									}
			    										
				    									steps++;
			    										idx = idx_old + v_adj*width;
			    									}
			    									
			    									
			    									boolean halted = false;
			    									if (has_hook) {
			    										
			    										// look for another hook
			    										
			    										idx = idx_old+(staff_space+staff_height)*width;
			    										idx_old = idx;
			    										staff[idx] = (0 << 16) | (255 << 8) | 255;
			    										steps = 0;
			    										off_n = 0;
			    										off_s = 0;
			    										
			    										while (steps < 2*staff_space && has_hook && !halted) {
				    										
				    										v_travel = 0;
				    										h_travel = 0;
				    										//while (staff[idx+1] == 0 && (idx+1) < N)
				    											idx++;
				    										
				    										for (int j = 0; j < staff_space; j++) {
					    										
				    											if (idx+(j*width) < N) {
					    															    											
						    										if (staff[idx-(j*width)] == 0)
						    											off_n++;
						    										if (staff[idx+(j*width)] == 0)
						    											off_s++;
					    											}
				    											else
				    												break;
					    									}
					    									
				    										while (off_n > staff_height || off_s > staff_height) {
						    									
					    										off_n = 0;
					    										off_s = 0;
					    										
					    										for (int j = 0; j < 0.5*staff_space; j++) {
						    										
						    										if (staff[idx-(j*width)] == 0) {
						    											staff[idx-(j*width)] = (0 << 16) | (255 << 8) | 255;
						    											off_n++;
						    										}
						    										if (staff[idx+(j*width)] == 0) {
						    											staff[idx+(j*width)] = (0 << 16) | (255 << 8) | 255;
						    											off_s++;
						    										}
						    												    										
						    									}
						    									v_adj = (off_s - off_n) / 2;
						    									if (v_adj < 0) {
						    										halted = true;
						    										break;
						    									}
						    									staff[idx] = (255 << 16) | (0 << 8) | 0;
						    									idx += v_adj*width+1;
						    									h_travel++;
						    									v_travel += v_adj;
						    									
						    									
						    									if (v_travel > 0.5*staff_space && h_travel > 0.5*staff_space) {
						    										// 2nd hook detected, set to FALSE
						    										has_hook = false;
						    										break;
						    									}

						    										
					    									}
				    										
					    									steps++;
				    										//idx = idx_old + v_adj*width;
				    									}
			    										
			    										// ONE hook
			    										if (has_hook) {
				    										x = notes[num_cnd][3];
				    										notes[num_cnd][5] = 1;
			    										}
			    										else { // TWO hooks
			    											x = notes[num_cnd][3];
				    										notes[num_cnd][5] = 2;
			    										}
			    										for (int j = 0; j < strip_height; j++) {
			    											
			    											idx = x + j*width;
			    											staff[idx]= (255 << 16) | (0 << 8) | 0;
			    										}
			    										
			    									}
			    									
			    									if (has_beam || nextisbeam) {
			    										
			    										x = notes[num_cnd][1];
			    										notes[num_cnd][5] = 1;
			    										for (int j = 0; j < strip_height; j++) {
			    											
			    											idx = x + j*width;
			    											staff[idx]= (0 << 16) | (255 << 8) | 255;
			    										}
			    										
			    										if (has_beam)
			    											nextisbeam = true;
			    										else
			    											nextisbeam = false;
			    										
			    									}	    									
				    												    									
			    								}
				    							
			    		    				}
				    						
				    						else {
				    							
// STEM: left lower corner
				    								max = 0;
				    								for (int k = centre_x; k > centre_x-staff_space; k--)  {
							    							
							    							if (proj[k] > max) {
							    								max = proj[k];
							    								maxpos = k;
							    							}
							    							
							    						}
				    								
					    								idx = (y+new_h+1)*width + maxpos;
							    						//staff[idx] = (0 << 16) | (255 << 8) | 0;
					    								has_succ = true;
					    												    								
					    								while (has_succ && idx < staff.length) {
					    								
					    									has_succ = false;
					    									len2++;
					    									
					    									if (staff[idx] == 0) {
					    										staff[idx] = (255 << 16) | (0 << 8) | 255;
					    										idx = idx + width;
					    										has_succ = true;
					    									}
					    									else if (staff[idx-1] == 0) {
					    										staff[idx-1] = (255 << 16) | (0 << 8) | 255;
					    										idx = idx + width;
					    										has_succ = true;
					    									}
					    									else if (staff[idx+1] == 0) {
					    										staff[idx+1] = (255 << 16) | (0 << 8) | 255;
					    										idx = idx + width;
					    										has_succ = true;
					    									}
					    									
					    									
						    								/*for (int j = -staff_height; j <= staff_height; j++) {
						    									
						    									if (staff[idx+j] == 0) {
						    										staff[idx] = (255 << 16) | (0 << 8) | 255;
						    										idx = idx + width + j;
						    										has_succ = true;
						    										break;
						    									}
						    								}*/				    								
					    								}
					    								
					    								if (len2 > stemThr) {				    									
					    									num_stems = 1;
					    									
					    									// start looking for hook, beam
					    									int off_n = 0;
					    									int off_s = 0;
					    									int v_travel = 0;
					    									int h_travel = 0;
					    									int v_adj = 0;
					    									int steps = 0;
					    									boolean has_hook = false;
					    									boolean has_beam = false;
					    									
					    									idx -= (int) width*(staff_space/2);
					    									
					    									/*int i2 = maxpos;
					    									while (proj[i2] >= histThr) {
					    										for (int y2 = 0; y2 < strip_height; y2++)
					    											staff[y2*width + i2] = (255 << 16) | (255 << 8) | 255;
					    										idx++;
					    										i2++;
					    									}*/
					    									
					    									
					    									// seedpoint for hook/beam tracking algorithm (toyama)
					    									staff[idx] = (0 << 16) | (255 << 8) | 255;
				    										
					    									// go upwards
					    									int idx_old = idx;
					    									
					    									while (steps < 2*staff_space && !has_hook && !has_beam) {
					    										
					    										v_travel = 0;
					    										h_travel = 0;
					    										
					    										//while (staff[idx+1] == 0)
					    											idx++;
					    										
					    										for (int j = 0; j < staff_space; j++) {
						    										
					    											if (idx+(j*width) < N) {
					    											
							    										if (staff[idx-(j*width)] == 0)
							    											off_n++;
							    										if (staff[idx+(j*width)] == 0)
							    											off_s++;
					    											}
					    											else
					    												break;
						    												    										
						    									}
						    									
					    										while (off_n > staff_height || off_s > staff_height) {
							    									
						    										off_n = 0;
						    										off_s = 0;
						    										
						    										for (int j = 0; j < staff_space; j++) {
							    										
						    											if (idx+(j*width) < N) {							    											
						    											
								    										if (staff[idx-(j*width)] == 0)
								    											off_n++;
								    										if (staff[idx+(j*width)] == 0)
								    											off_s++;
							    											}
						    											else
						    												break;
							    												    										
							    									}
							    									v_adj = (off_s - off_n) / 2;
							    									staff[idx] = (255 << 16) | (0 << 8) | 0;
							    									idx += v_adj*width+1;
							    									h_travel++;
							    									v_travel = v_travel + Math.abs(v_adj);
							    									
							    									
							    									if (v_travel > (1.5 * staff_space)) {
							    										// hook detected
							    										has_hook = true;
							    										break;
							    									}
							    									
							    									if (h_travel > (3*staff_space)) {
							    										// beam detected
							    										has_beam = true;
							    										break;
							    										
							    									}
							    										
						    									}
					    										
						    									steps++;
					    										idx = idx_old + v_adj*width;
					    									}
					    									
					    									boolean halted = false;
					    									if (has_hook) {
					    										
					    										// look for another hook
					    										
					    										idx = idx_old-(staff_space+staff_height)*width;
					    										idx_old = idx;
					    										staff[idx] = (0 << 16) | (255 << 8) | 255;
					    										steps = 0;
					    										off_n = 0;
					    										off_s = 0;					    									
					    										
					    										while (steps < 2*staff_space && has_hook && !halted) {
						    										
						    										v_travel = 0;
						    										h_travel = 0;
						    										
						    										//while (staff[idx+1] == 0 && (idx+1) < N)
						    											idx++;
						    										
						    										for (int j = 0; j < staff_space; j++) {
							    										
							    										if (staff[idx-(j*width)] == 0)
							    											off_n++;
							    										if (staff[idx+(j*width)] == 0)
							    											off_s++;
							    												    										
							    									}
							    									
						    										while (off_n > staff_height || off_s > staff_height) {
								    									
							    										off_n = 0;
							    										off_s = 0;
							    										
							    										for (int j = 0; j < 0.5*staff_space; j++) {

								    										if (staff[idx-(j*width)] == 0) {
								    											staff[idx-(j*width)] = (0 << 16) | (255 << 8) | 255;
								    											off_n++;
								    										}
								    										if (staff[idx+(j*width)] == 0) {
								    											staff[idx+(j*width)] = (0 << 16) | (255 << 8) | 255;
								    											off_s++;
								    										}
								    												    										
								    									}
								    									v_adj = (off_s - off_n) / 2;
								    									if (v_adj > 0) {
								    										halted = true;
								    										break;
								    									}
								    									staff[idx] = (255 << 16) | (0 << 8) | 0;
								    									idx += v_adj*width+1;
								    									h_travel++;
								    									v_travel = v_travel + Math.abs(v_adj);
								    									
								    									
								    									if (v_travel > 0.5*staff_space && h_travel > 0.5*staff_space) {
								    										// 2nd hook detected
								    										has_hook = false;
								    										break;
								    									}
								    										
							    									}
						    										
							    									steps++;
						    										//idx = idx_old + v_adj*width;
						    									}
					    										
					    										// ONE hook
					    										if (has_hook) {
						    										x = notes[num_cnd][3];
						    										notes[num_cnd][5] = 1;
					    										}
					    										else { // TWO hooks
					    											x = notes[num_cnd][3];
						    										notes[num_cnd][5] = 2;
					    										}
					    										
					    										for (int j = 0; j < strip_height; j++) {
					    											
					    											idx = x + j*width;
					    											staff[idx]= (255 << 16) | (255 << 8) | 0;
					    										}
					    										
					    									}
					    									
					    									if (has_beam || nextisbeam) {
					    										
					    										x = notes[num_cnd][1];
					    										notes[num_cnd][5] = 1;
					    										for (int j = 0; j < strip_height; j++) {
					    											
					    											idx = x + j*width;
					    											staff[idx]= (0 << 16) | (255 << 8) | 255;
					    										}
					    										
					    										if (has_beam)
					    											nextisbeam = true;
					    										else
					    											nextisbeam = false;
					    										
					    									}
						    												    									
					    								}
					    							
				    						}
			    		    				
				    						if (num_stems > 0) // prevent false positives of note heads
				    							num_cnd++;				    						
				    						
				    						
			    		    			}
			    		    			
		    						}
	    							
	    						} // finished left-right search
	    						
	    						if (yh == 0)
	    							y+=half_dist;
    						
    						}

    					}
    				}
   				
    			}
				
    			
    		}    		
    	}
    	
    	if (num_cnd > 0) {
    		
    		// FIND PROLONGATION DOTS
    		   		
    		matchingThreshold = 0.4f;
    		int upper = 0, lower = 0;
    		boolean match;
    		int dot_size = d_h*d_w;
    		aborted = false;
    		
    		earlyThr = (int) (matchingThreshold * dot_size);
    		
    		for (int i = 0; i < num_cnd; i++) {
    			
        		has_dot = false;
    			l = (int) (notes[i][3]+(int)(0.5*staff_space));
    			r = (int) Math.min(l+1.5*staff_space, width-1);
    			upper = (int) (notes[i][0]-(int)(0.5*staff_height));
    			lower = (int) (notes[i][2]-(int)(0.5*staff_height));
    			
    			for (int y = upper; y < lower; y++) {
    				
    				for (int x = l; x < r; x++) {

    					if (proj[x] < staff_space) {
    					
							// stop if match not possible anymore or if there is a sufficiently high vote for a candidate
							match = false;
							aborted = false;
							error = 0;
						
							while (!aborted && !match) {
							
								for (int y2 = 0; y2 < d_h; y2++) {
									for (int x2 = 0; x2 < d_w; x2++) {
											
										valI = (staff[(y+y2)*width+x+x2] >> 16) & 0xff;
										valT = (dot[y2*d_w+x2] >> 16) & 0xff;
										
										if (Math.abs(valI-valT) > 128) {	     								
											error++;
											if (error > earlyThr) {
												aborted = true;
												break;
											}
										}
									}   							
								}
								
								match = true;
								
							}
							
							if (match == true) {
							
								coeff = (float)error/dot_size;
					  			if (coeff < matchingThreshold) {
										  				
					  				has_dot = true;
					  				
					  				for (int y2 = 0; y2 < d_h; y2++) {
											for (int x2 = 0; x2 < d_w; x2++) {
												if (staff[(y+y2)*width+x+x2] == 0)
													staff[(y+y2)*width+x+x2] = (255 << 16) | (0 << 8) | 0;
											}
					  				}
					  				
					  			}
							}
						
    					}
    					
    					
    				}
	    				
	    			
    			
    			}
    			
    			int x_pos = notes[i][1]+Math.round((float)new_w/2)-1;
    			int y_pos = notes[i][0]+Math.round((float)new_h/2)-1;

    			for (int y = 0; y < strip_height; y++) {
    				if (notes[i][5] != 1) {
	    				for (int x = x_pos-staff_space; x < x_pos+staff_space; x++) {
	    					try {
	    						staff[y*width+x] = (255 << 16) | (255 << 8) | 255;
	    					}
	    					catch (Exception e1) {
	    						
	    					}
	    				}
    				}
    				else {
    					for (int x = x_pos-staff_space; x < x_pos+3*staff_space; x++) {
	    					try {
	    						staff[y*width+x] = (255 << 16) | (255 << 8) | 255;
	    					}
	    					catch (Exception e1) {
	    						
	    					}
	    				}
    				}
    			}

    			
    			int n = getNoteMIDIVal(y_pos);
    			
    			// NOTE = OK
    			// DUR  = OK
    			// crotchet=quarter=1crotchet
    			// half=minim=2c
    			// whole=semibreve=4c
    			// quaver=1/2c
    			// semiquaver=1/4c
    			
    			// save x-value of first note candidate for later use (key signature)
    			if (x_pos < first_sym || first_sym == -1)
    				first_sym = x_pos;
    			
    			dur = duration;
    			while (notes[i][5] > 0) {
    				dur *= 0.5;
    				notes[i][5]--;
    			}
    			if (has_dot)
    				dur *= 1.5;
    			Symbol note = new Symbol(x_pos, n, dur);
    			symbols.add(note);
    			num_symbols++;
    			
    		} // end for
    		
    	}
    
    	if (num_cnd > num_notes)
    		num_notes = num_cnd;

    	return;
    	
    }

  private String getKey(int[][] keys) {

	  
	  
	int size = key_cnt;
	
	boolean error = false;
	int key = 0;
	
	key = keys[0][0];
	
	if (!error) {
		
		if (size == 1) {
			if (keys[0][1] == 35) {
				if (key == 1)
					return "F Major";
			}
			else if (keys[0][1] == 41) {
				if (key == 2)
					return "G Major";
			}
			return "size1";
		}
		else {
						
			for (int i = 0; i < size-1; i++) {
				if (keys[i][0] != keys[i+1][0]) {
					error = true;
					return "ERROR";
				}
			}
			
			if (size == 2) {
				
				if (key == 2) {
					for (int i = 0; i < size; i++) {
						for (int j = 0; j < size; j++) {
							if (i != j) {
								if (keys[i][1] == 41 || keys[j][1] == 41) {
									if (keys[i][1] == 36 || keys[j][1] == 36) {
										return "D Major";
									}
								}
							}
								
						}
					}
				}
				else {
					for (int i = 0; i < size; i++) {
						for (int j = 0; j < size; j++) {
							if (i != j) {
								if (keys[i][1] == 35 || keys[j][1] == 35) {
									if (keys[i][1] == 40 || keys[j][1] == 40) {
										return "Bb Major";
									}
								}
							}
								
						}
					}
				}
				
				return "ERROR";
				
			}
			else if (size == 3) {
							
				
			}
			else if (size == 4) {
							
				
			}
			else if (size == 5) {
							
				
			}
			else if (size == 6) {
							
				
			}
			else if (size == 7) {
							
				
			}
			else if (size == 0) {
				return "C Major";
			}
		}
		
	}
	else
		return "ERROR";
	
	return "ERROR";

	  
  }

  private int getNoteMIDIVal(int y_pos) {
	  
	  	// check note value (pseudo binary search)
	  	int dist = lines[1]-lines[0];
	  	int half_dist = Math.round((float)dist/2);
		int quarter_dist = Math.round((float)dist/4);
		int line_h1 = lines[0]-dist;
		int line_h2 = lines[0]-2*dist;
		int line_h3 = lines[0]-3*dist;
		int line_l1 = lines[4]+dist;
		int line_l2 = lines[4]+2*dist;
		int line_l3 = lines[4]+3*dist;
		
		String NOTE = "";
		int n = 0;
		
		if (y_pos < lines[0]) {
			if (y_pos < line_h2) {
				if (y_pos < line_h3) {
					if (y_pos < line_h3-half_dist) {
						if (y_pos < line_h3-half_dist-quarter_dist) {
							NOTE = "g3";
							n = 55;
						}
						else {
							NOTE = "f3";
							n = 53;
						}
					}
					else {
						if (y_pos < line_h3-quarter_dist) {
							NOTE = "f3";
							n = 53;
						}
						else {
							NOTE = "e3";
							n = 52;
						}
					}
				}
				else {
					if (y_pos < line_h2-half_dist) {
						if (y_pos < line_h3+quarter_dist) {
							NOTE = "e3";
							n = 52;
						}
						else {
							NOTE = "d3";
							n = 50;
						}
					}
					else {
						if (y_pos < line_h2-quarter_dist) {
							NOTE = "d3";
							n = 50;
						}
						else {
							NOTE = "c3";
							n = 48;
						}
					}
				}
			}
			else { // > line_h2
				if (y_pos < line_h1) {
					if (y_pos < line_h1-half_dist) {
						if (y_pos < line_h2+quarter_dist) {
							NOTE = "c3";
							n = 48;
						}
						else {
							NOTE = "h2";
							n = 47;
						}
					}
					else {
						if (y_pos < line_h1-quarter_dist) {
							NOTE = "h2";
							n = 47;
						}
						else {
							NOTE = "a2";
							n = 45;
						}
					}
				}
				else { // > line_h1
					if (y_pos < lines[0]-half_dist) {
						if (y_pos < line_h1+quarter_dist) {
							NOTE = "a2";
							n = 45;
						}
						else {
							NOTE = "g2";
							n = 43;
						}
					}
					else {
						if (y_pos < lines[0]-quarter_dist) {
							NOTE = "g2";
							n = 43;
						}
						else {
							NOTE = "f2";
							n = 41;
						}
					}
				}
			}
		}
		else if (y_pos > lines[4]) {
			if (y_pos < line_l2) {
				if (y_pos < line_l1) {
					if (y_pos < line_l1-half_dist) {
						if (y_pos < lines[4]+quarter_dist) {
							NOTE = "e1";
							n = 28;
						}
						else {
							NOTE = "d1";
							n = 26;
						}
					}
					else {
						if (y_pos < line_l1-quarter_dist) {
							NOTE = "d1";
							n = 26;
						}
						else {
							NOTE = "c1";
							n = 24;
						}
					}
				}
				else {
					if (y_pos < line_l2-half_dist) {
						if (y_pos < line_l1+quarter_dist) {
							NOTE = "c1";
							n = 24;
						}
						else {
							NOTE = "h";
							n = 23;
						}
					}
					else {
						if (y_pos < line_l2-quarter_dist) {
							NOTE = "h";
							n = 23;
						}
						else {
							NOTE = "a";
							n = 21;
						}
					}
				}
			}
			else {
				if (y_pos < line_l3) {
					if (y_pos < line_l3-half_dist) {
						if (y_pos < line_l2+quarter_dist) {
							NOTE = "a";
							n = 21;
						}
						else {
							NOTE = "g";
							n = 19;
						}
					}
					else {
						if (y_pos < line_l3-quarter_dist) {
							NOTE = "g";
							n = 19;
						}
						else {
							NOTE = "f";
							n = 17;
						}
					}
				}
				else {
					if (y_pos < line_l3+half_dist) {
						if (y_pos < line_l3+quarter_dist) {
							NOTE = "f";
							n = 17;
						}
						else {
							NOTE = "e";
							n = 16;
						}
					}
					else {
						if (y_pos < line_l3+half_dist+quarter_dist) {
							NOTE = "e";
							n = 16;
						}
						else {
							NOTE = "d";
							n = 14;
						}
					}
				}
			}
		}
		else {
		    			
			if (y_pos < lines[2]) {
				if (y_pos < lines[1]) {
					if (y_pos < lines[1]-half_dist) {
						if (y_pos < lines[0]+quarter_dist) {
							NOTE = "f2";
							n = 41;
	  					}
	  					else {
	  						NOTE = "e2";
	  						n = 40;
	  					}
					}
					else {
						if (y_pos < lines[1]-quarter_dist) {
							NOTE = "e2";
							n = 40;
	  					}
	  					else {
	  						NOTE = "d2";
	  						n = 38;
	  					}
					}
				}
				else {	    					    					
					if (y_pos < lines[1]+half_dist) {
						if (y_pos < lines[1]+quarter_dist) {
							NOTE = "d2";
							n = 38;
	  					}
	  					else {
	  						NOTE = "c2";
	  						n = 36;
	  					}
					}
					else {
						if (y_pos < lines[2]-quarter_dist) {
							NOTE = "c2";
							n = 36;
	  					}
	  					else {
	  						NOTE = "h1";
	  						n = 35;
	  					}
					}
				}	    				
			}
			else {
				if (y_pos < lines[3]) {
					if (y_pos < lines[3]-half_dist) {
						if (y_pos < lines[2]+quarter_dist) {
							NOTE = "h1";
							n = 35;
	  					}
	  					else {
	  						NOTE = "a1";
	  						n = 33;
	  					}
					}
					else {
						if (y_pos < lines[3]-quarter_dist) {
							NOTE = "a1";
							n = 33;
	  					}
	  					else {
	  						NOTE = "g1";
	  						n = 31;
	  					}	    						
					}
				}
				else {	    					    					
					if (y_pos < lines[3]+half_dist) {
						if (y_pos < lines[3]+quarter_dist) {
							NOTE = "g1";
							n = 31;
  					}
	  					else {
	  						NOTE = "f1";
	  						n = 29;
	  					}
					}
					else {
						if (y_pos < lines[4]-quarter_dist) {
							NOTE = "f1";
							n = 29;
						}
						else {
							NOTE = "e1";
							n = 28;
						}
					}
				}		    				
			}    			
		}
		return n;
	  
  }
    
  public int matchClefs(int[] staff, int[] tpl, int[] proj, int[] lines, int min, int new_h, int new_w, float matchingThreshold, String mode, float vote) {
    	
    	int histThr = min + 3*staff_space;
    	int l, r;
		int[] clef_cnd = new int[5];
    	int pos = 0;
    	float coeff = 0.0f;
    	int x2 = 0, y2;
    	
    	for (int i = 0; i < width*0.2; i++) {
    		
    		if (proj[i] >= histThr) {
    			    			
    			// scale templates:
    			// bass clef dimension: 3*staff_space
    			// treble clef dimension: 8*staff_space
    			// width: approx. 3*staff_space
    			
    			l =  Math.max(0, i-3*staff_space);
    			r =  Math.min(width-1, i+3*staff_space);
    			
    			int window_width = r-l;
    			int error = 0;
    			int valI, valT;
    			
    			// TEMPLATE MATCHING
    			// maximize matching function
		
    			int hspan = window_width - new_w+1;
    			int tpl_size = new_w * new_h;
    	    	int earlyThr = (int) (matchingThreshold * tpl_size);
    			int idx;
    	    	
    			if (hspan > 0) {
    				
    				for (int y = lines[0]-3*staff_space; y < lines[0]-staff_space; y++) {
    					
    					for (int x = l; x < r-new_w; x++) {

							error = 0;
							
							// stop if match not possible anymore or if there is a sufficiently high vote for a candidate
							
    						for (y2 = 0; y2 < new_h; y2++) {
    							for (x2 = 0; x2 < new_w; x2++) {
    								
    								idx = (y+y2)*width+x+x2;
    								if (idx > 0 && idx < staff.length) {
    								
	    								valI = (staff[(y+y2)*width+x+x2] >> 16) & 0xff;
	    								//staff[(y+y2)*width+x+x2] = (255 << 16) | (0 << 8) | 0;
	    								valT = (tpl[y2*new_w+x2] >> 16) & 0xff;
	    								
	    								if (Math.abs(valI-valT) > 128) {	     								
	    									error++;
	    									if (error > earlyThr) {
	    										break;
	    									}
	    								}
    								
    								}
    							}   							
    						}

							
							coeff = (float)error/tpl_size;
    		    			if (coeff < matchingThreshold) {
				
		    					clef_cnd[0] = y;
    		    				clef_cnd[1] = x;
    		    				clef_cnd[2] = y+new_h;
    		    				clef_cnd[3] = x+new_w;
    		    				clef_cnd[4] = (int) (coeff*100);
        		    				
    		    				y = 0;
        				    	x = clef_cnd[1];
        				    	
        						for (y2 = 0; y2 < strip_height; y2++) {
        							for (x2 = 0; x2 < new_w+0.5*staff_space; x2++) {
        								staff[(y+y2)*width+x+x2] = (255 << 16) | (255 << 8) | 0;
        							}        												    								
        						}
        						
        						pos += new_w+0.5*staff_space;
        						return pos;
        		    		}
    					
    				
    			    	}
    					
    				}
   				
    			}
				
    			
    		}    		
    	}  	
    	
    	
    	return pos;
    	
    }
   
  public int[][] matchRests(int[] staff, int[] tpl, int[] proj, int[] lines, int min, int new_h, int new_w, float matchingThreshold, int pos) {
	  	
	  	int histThr = min + 3*staff_height;
  		int l, r;
  		boolean aborted = false;
  		int left_boundary = pos + 3*staff_space;
		int tpl_size = new_w * new_h;
		int earlyThr = (int) (matchingThreshold * tpl_size);
		int error = 0;
		int valI, valT;
		int numRests = 0;
		float coeff;
		float tmp_c = 0;
		int idx;
		int tmp_pos = 0;
		int[][] rests = new int[99][2];
  	
  	for (int i = left_boundary; i < width; i++) {
  		
  		if (proj[i] >= histThr && proj[i] < (min + 2*staff_space)) {
  			  			
  			l =  Math.max(0, i-staff_space);
  			r =  Math.min(width-1, i+3*staff_space);
  			
  			int window_width = r-l;

  			
  			// TEMPLATE MATCHING
  			// calculate SAD and maximize matching function
  			
  			int hspan = window_width - new_w+1;
  			
  			if (hspan > 0) {
  				
  				for (int y = lines[1]; y < lines[3]; y++) {
  				  						
					for (int x = l; x < r-new_w; x++) {
						
							error = 0;
							aborted = false;
							
							// stop if match not possible anymore
							
							while (!aborted) {
							
	    						for (int y2 = 0; y2 < new_h; y2++) {
	    							for (int x2 = 0; x2 < new_w; x2++) {
	    								
	    								valI = (staff[(y+y2)*width+x+x2] >> 16) & 0xff;
	    								valT = (tpl[y2*new_w+x2] >> 16) & 0xff;
	    								
	    								if (Math.abs(valI-valT) > 128) {	     								
	    									error++;
	    									if (error > earlyThr)
	    										aborted = true;
	    								}
	    							}   							
	    						}
	    						
	    						aborted = true;
	    						
							}
    						
    						coeff = (float)error/tpl_size;
    						if (coeff < matchingThreshold) {
    							
    							if (coeff < tmp_c || tmp_c == 0) {
					  				tmp_c = coeff;
					  				tmp_pos = x;
					  			}
    							
    							for (int j = 0; j < strip_height; j++) {
									
									idx = x + j*width;
									staff[idx]= (255 << 16) | (0 << 8) | 255;
								}
    							
    						}

						}						
  				
  					}  				
				
  					rests[numRests][0] = tmp_pos;
  					numRests++;
  					i += 2*new_w;
  				
				}
  			}
  		}
  	
  	return rests;
	  	
  }
  
  public void matchQuarterRests(int[] staff, int[] tpl, int[] proj, int[] lines, int min, int new_h, int new_w, float matchingThreshold, int pos, int duration) {
	  	
	  	int histThr = min + staff_space;
		int l, r;
		boolean aborted = false;
		int left_boundary = pos + 3*staff_space;
		int tpl_size = new_w * new_h;
		int earlyThr = (int) (matchingThreshold * tpl_size);
		int error = 0;
		int valI, valT;
		float coeff;
		int idx;
	
	for (int i = left_boundary; i < width; i++) {
		
		if (proj[i] >= histThr && proj[i] < (min + 3*staff_space)) {
			
			l =  Math.max(0, i-staff_space);
			r =  Math.min(width-1, i+staff_space);
			
			int window_width = r-l;

			
			// TEMPLATE MATCHING
			// calculate SAD and maximize matching function
			
			int hspan = window_width - new_w+1;
			
			if (hspan > 0) {
				
				for (int y = lines[0]; y < lines[1]; y++) {
				  						
					for (int x = l; x < r-new_w; x++) {
						
							error = 0;
							aborted = false;
							
							// stop if match not possible anymore
							
							while (!aborted) {
							
	    						for (int y2 = 0; y2 < new_h; y2++) {
	    							for (int x2 = 0; x2 < new_w; x2++) {
	    								
	    								valI = (staff[(y+y2)*width+x+x2] >> 16) & 0xff;
	    								valT = (tpl[y2*new_w+x2] >> 16) & 0xff;
	    								//staff[(y+y2)*width+x+x2]= (0 << 16) | (255 << 8) | 255;
	    									    								
	    								if (Math.abs(valI-valT) > 128) {	     								
	    									error++;
	    									if (error > earlyThr)
	    										aborted = true;
	    								}
	    							}   							
	    						}
	    						
	    						aborted = true;
	    						
							}
  						
	  						coeff = (float)error/tpl_size;
	  						if (coeff < matchingThreshold) {
	  							
	  				   			Symbol note = new Symbol(i, -1, duration);
	  			    			symbols.add(note);
	  							
	  							for (int j = 0; j < strip_height; j++) {
										
										idx = x + j*width;
										if (staff[idx] == 0)
										staff[idx]= (0 << 16) | (255 << 8) | 255;
								}
	  							
	  							x = r;
	  							y = lines[1];
	  							i+=3*staff_space;
	  							
	  						}

						}						
				
					} 				
				}
			}
		}
	  	
}
  
  public int matchAccidentals(int[] staff, int[][] notes, int[] tpl, int[] proj, int[] lines, int min, int new_h, int new_w, float matchingThreshold) {
  	
  	int pos = 0;
  	int i = 0;
  	int y, x, y_end;
  	int upper, lower, left, right;
	int[] window = null;
	boolean has_neighbour = false;
	int idx = 0;
  	
	while (notes[i][0] != 0) {
  		
  		y = notes[i][0];
  		x = notes[i][1];
  		y_end = notes[i][2];
  		has_neighbour = false;

  		// specify boundaries of matching window
  		upper = (int) (y-1.25*staff_space);
  		lower = (int) (y_end+1.25*staff_space);
  		left = (int) (x-staff_space);
  		right = x;
  		
  		for (int j = left; j < right; j++) {
  			
  			if (proj[j] > 2*staff_space && proj[j] < 3.5*staff_space) {
  				has_neighbour = true;
  				break;
  			}
  			
  		}
  		
  		if (has_neighbour) {
  		
  			// perform vertical projection
  			int w = right-left+1;
  			int h = lower-upper+1;
  			window = new int[w*h];
  			idx = 0;
  			
  			int[] proj2 = new int[w];
  			int x3 = 0;
	  		for (int x2 = left; x2 < right; x2++) {  			
	  			for (int y2 = upper; y2 < lower; y2++) {

	  				if (staff[y2*width+x2] == 0)
	  					proj2[x3]++;
	  				
		  		}
	  			x3++;
	  		}
	  		
	  		
			// find number of peaks to distinguish between flat and sharp
		

			
			// zero crossing function
			
			int num_peaks = 0;
			min = (int) 1.5*staff_space;
			int last_pos = 0;
			
			for (int j = 1; j < w-1; j++) {
				
				if (((proj2[j+1] - proj2[j] >= 0) && (proj2[j] - proj2[j-1] < 0)) || ((proj2[j+1] - proj2[j] < 0) && (proj2[j] - proj2[j-1] >= 0))) {
					
					// zero crossing detected
					
					if ((proj2[j] > min || proj2[j-1] > min || proj2[j+1] > min))
						if (last_pos == 0) {
							num_peaks++;
							last_pos = j;
						}
						else {
							
							if (j-last_pos > 0.3*staff_space) {
								num_peaks++;
								last_pos = j;
							}
							else if (proj2[last_pos] < proj2[j])
								last_pos = j;
							
						}
					
				}
				
			}			

			if (num_peaks == 2) {
				// is sharp
				
				for (x = left; x < right; x++) {  			
		  			for (y = upper; y < lower; y++) {
			  			
		  				if (staff[y*width+x] == 0)
		  					staff[y*width + x] = (0 << 16) | (255 << 8) | 0;
			  		}
		  		}				
			}
			
			else if (num_peaks == 1){
				// is flat
				
				for (x = left; x < right; x++) {  			
		  			for (y = upper; y < lower; y++) {
			  			
		  				if (staff[y*width+x] == 0)
		  					staff[y*width + x] = (0 << 16) | (255 << 8) | 255;
			  		}
		  		}
				
			}
		
			if (x < first_sym)
				first_sym = x;
						
  		}
  		
  		i++;
  	}
 		
	return pos;
  	
  }
  
  public void matchKeySignature(int[] staff, int[] tpl, int[] proj, int[] lines, int min, int new_h, int new_w, float matchingThreshold, int clef_pos) {
	  	
	  	int i = 0;
	  	int y = 0, x = 0;
	  	int upper, lower, left, right;
		int cnt = 0;
	  		
		// specify boundaries of matching window
		upper = Math.max(0, lines[0]-3*staff_space);
		lower = lines[4]+staff_space;
		left = clef_pos+3*staff_space;
		right = first_sym-3*staff_space;
	
		i = left;		
		while (i < right && cnt < 1.5*staff_space) {
			
			if (proj[i] > 2*staff_space && proj[i] < 4*staff_space) {

				// perform vertical projection
				int w = 2*staff_space;
				
				int[] proj2 = new int[w];
				int x3 = 0;
		  		for (int x2 = i; x2 < i+2*staff_space; x2++) {  			
		  			for (int y2 = upper; y2 < lower; y2++) {

		  				if (staff[y2*width+x2] == 0)
		  					proj2[x3]++;
		  				
			  		}
		  			x3++;
		  		}
		  		
		  		
				// find number of peaks to distinguish between flat and sharp
			
				// zero crossing function - LOCAL MINIMA
				
				int num_lows = 0;
				int max = staff_space;
				int last_pos = 0;
				int[] minima = new int[20];
				
				for (int j = 1; j < w-1; j++) {
									
					if (((proj2[j+1] - proj2[j] >= 0) && (proj2[j] - proj2[j-1] < 0)) || ((proj2[j+1] - proj2[j] < 0) && (proj2[j] - proj2[j-1] >= 0))) {
						
						// zero crossing detected
						
						if ((proj2[j] < max || proj2[j-1] < max || proj2[j+1] < max))
							if (last_pos == 0) {
								minima[num_lows] = j;
								num_lows++;
								last_pos = j;
							}
							else {
								
								if (j-last_pos > staff_space) {
									minima[num_lows] = j;
									num_lows++;
									last_pos = j;
								}
								else if (proj2[last_pos] > proj2[j])
									minima[num_lows] = j;
									last_pos = j;
								
							}
						
					}
					
				}			

				somevalue = num_lows;
				int j = 0;
				int max_pos = -1;
				int sum = 0;
				int y2;
				max = 0;
				
				for (int l = 0; l < num_lows; l++) {
					
					// zero crossing function - LOCAL MAXIMA
					
					int num_peaks = 0;
					min = (int) 2*staff_space;
					last_pos = 0;
					
					if (l == 0)
						j = 1;
					else
						j = minima[l-1];
					
					for (; j < minima[num_lows]; j++) {
						
						if (((proj2[j+1] - proj2[j] >= 0) && (proj2[j] - proj2[j-1] < 0)) || ((proj2[j+1] - proj2[j] < 0) && (proj2[j] - proj2[j-1] >= 0))) {
							
							// zero crossing detected
							
							if ((proj2[j] > min || proj2[j-1] > min || proj2[j+1] > min))
								if (last_pos == 0) {
									num_peaks++;
									last_pos = j;
								}
								else {
									
									if (j-last_pos > 0.3*staff_space) {
										num_peaks++;
										last_pos = j;
									}
									else if (proj2[last_pos] < proj2[j])
										last_pos = j;
									
								}
							
						}
						
					}
					
					max = 0;
					
					// refine local maximum
					int local_max = 0;
					for (j = i-2*staff_height; j < i+2*staff_height; j++) {
						if (proj[j] > local_max) {
							local_max = proj[j];
							x = j;
						}
					}
					
					j = x;
					
					if (num_peaks == 2) {
						// is sharp
						/*
						for (x = i; x < i+staff_space; x++) {  			
				  			for (y = upper; y < lower; y++) {
					  			
				  				if (staff[y*width+x] == 0)
				  					staff[y*width + x] = (255 << 16) | (0 << 8) | 0;
					  		}
				  		}*/
						

						
						int span = 3*staff_space + staff_height;
						
						
			  			for (y = upper; y < lower-span; y++) {
								
		  					sum = 0;
		  					
							for (y2 = 0; y2 < span; y2++) {									 					
								if (staff[(y+y2)*width+x] == 0)
									sum++;
							}
							
							if (sum > max) {

								max_pos = y + (y2/2);
								max = sum;
							}
			  				
			  			}		  			
			  			
			  					
				  			for (x = 0; x < width; x++)				  			
				  					staff[max_pos*width + x] = (255 << 16) | (0 << 8) | 0;
				  			for (y = 0; y < strip_height; y++)	{			  			
			  				if (staff[y*width + j] == 0)
				  				staff[y*width + j] = (255 << 16) | (0 << 8) | 0;
				  			}
			  			
			  			cnt = 0;
				    	keys[key_cnt][0] = 2;
				    	keys[key_cnt][1] = getNoteMIDIVal(max_pos);
				    	key_cnt++;
				    	sigstring += "#";
						
					}
					
					else if (num_peaks == 1){
						// is flat
						/*
						for (x = i; x < i+staff_space; x++) {  			
				  			for (y = upper; y < lower; y++) {
					  			
				  				if (staff[y*width+x] == 0)
				  					staff[y*width + x] = (0 << 16) | (255 << 8) | 0;
					  		}
				  		}
						*/		
						int span = 3*staff_space + staff_height;
						
			  			for (y = upper; y < lower-span; y++) {
			  												
		  					sum = 0;
		  					
							for (y2 = 0; y2 < span; y2++) {									 					
								if (staff[(y+y2)*width+x] == 0)
									sum++;
							}
							
							if (sum > max) {

								max_pos = (int) (y + 2*staff_space);
								max = sum;
							}
			  				
			  			}
			  			
						staff[max_pos*width + x] = (255 << 16) | (0 << 8) | 0;
			  			
			  			cnt = 0;
			  			keys[key_cnt][0] = 1;
				    	keys[key_cnt][1] = getNoteMIDIVal(max_pos);
				    	key_cnt++;
				    	sigstring += "b";
					}
					
				}
				
				i+=1.5*staff_space;
			
			}
			else
				cnt++;
		
		i++;
		}
	  	
	  }
	  
  
  public int matchTimeSignature(int[] staff, int[] proj, int[] lines, int clef_pos, float matchingThreshold) {
	  	
	  	int pos = 0;
	  	float coeff = 0.0f;
	  	boolean aborted = false;
	  	int y, x, l = 0, r = 0, upper = 0, lower = 0;
		int error = 0;
		int tmp_x = 0, tmp_y = 0, tmp_num = 0;
		int early_thr = 0;
		float tmp_c = 0;
		int valI, valT;
		int tpl_size = 0;
		int new_h = 0, new_w = 0;
		int[] tpl = null;
		boolean match = false;
		Bitmap tplbmp = null;	  				
		
    	l = clef_pos + staff_space;
		r = first_sym - staff_space;    	
    	
		for (int y2 = lines[0]; y2 < lines[4]; y2++) {
			for (int x2 = l; x2 < r; x2++) {
				staff[y2*width+x2] = (255 << 16) | (0 << 8) | 0;
			}
		}
		
    	while (proj[l] == 0)
    		l++;
    	
    	while (proj[r] == 0)
    		r--;
    	
    	if (r > l) {
    		
    		for (int k = 0; k < 2; k++) {	// NUMERATOR (k=0) or DENOMINATOR (k=1)
    		
    			error = 0;
    			coeff = 0;
    			tmp_x = 0;
    			tmp_y = 0;
    			tmp_c = 0;
    			
    			if (k == 0) {
					upper = lines[0];
					lower = lines[2];
    			}
    			else {
    				upper = lines[2];
					lower = lines[4];    				
    			}
						
				for (int i = 1; i < 6; i++) {
				
					if (k == 1 && i == 5)
						continue;
					
					if (i == 0 && k == 0) {
				    	tplbmp = loadTemplate("time_c.png");
						upper = lines[1];
						lower = lines[3];
					}
				    else if (i == 1)
						tplbmp = loadTemplate("time_4.png");
					else if (i == 2)
						tplbmp = loadTemplate("time_3.png");
					else if (i == 3)
						tplbmp = loadTemplate("time_2.png");
					else if (i == 4)
						tplbmp = loadTemplate("time_8.png");
					else if (i == 5)
						tplbmp = loadTemplate("time_6.png");
					
			    	new_h = 2*staff_space+2*staff_height;
			    	new_w = tplbmp.getWidth() / (tplbmp.getHeight()/new_h);
					r = l + new_w;
				    	
			    	tpl_size = new_w * new_h;
			    	tpl = new int[new_w*new_h];
			    	tplbmp = Bitmap.createScaledBitmap(tplbmp, new_w, new_h, false);
			    	tplbmp.getPixels(tpl, 0, new_w, 0, 0, new_w, new_h);
			    	early_thr = (int) (matchingThreshold * tpl_size);			    	
					
					for (y = upper; y < lower; y++) {
						for (x = l; x < r; x++) {  			
									  			
							// stop if match not possible anymore or if there is a sufficiently high vote for a candidate
							match = false;
							aborted = false;
							error = 0;
						
							while (!aborted && !match) {
							
								for (int y2 = 0; y2 < new_h; y2++) {
									for (int x2 = 0; x2 < new_w; x2++) {
										
										valI = (staff[(y+y2)*width+x+x2] >> 16) & 0xff;
										valT = (tpl[y2*new_w+x2] >> 16) & 0xff;
										
										if (Math.abs(valI-valT) > 128) {	     								
											error++;
											if (error > early_thr)
												aborted = true;
										}
									}   							
								}
								
								match = true;
										
							}
							
							if (match) {
								coeff = (float)error/tpl_size;
					  			if (coeff < tmp_c || tmp_c == 0) {
					  				tmp_c = coeff;
					  				tmp_x = x;
					  				tmp_y = y;		
					  				tmp_num = i;
									first_sym = tmp_x;
					  			}
							}					
						}
					}	
				}
					
				for (int y2 = tmp_y; y2 < tmp_y+new_h; y2++) {
					for (int x2 = tmp_x; x2 < tmp_x+new_w; x2++) {
						if (staff[(y2)*width+x2] == 0)
							staff[(y2)*width+x2] = (255 << 16) | (0 << 8) | 0;
					}
				}
								
				if (k == 0) {
				
					if (tmp_num == 0) { // c found
						numerator = 4;
						denominator = 4;
						return pos;
					}
				    else if (tmp_num == 1)
				    	numerator = 4;
					else if (tmp_num == 2)
						numerator = 3;
					else if (tmp_num == 3)
						numerator = 2;
					else if (tmp_num == 4)
						numerator = 8;
					else if (tmp_num == 5)
						numerator = 6;
					
					// continue to search for denominator at the same y-value of the previously found numerator
					l = tmp_x;				
				}
				else {
					if (tmp_num == 1)
						denominator = 4;
					else if (tmp_num == 2)
						denominator = 3;
					else if (tmp_num == 3)
						denominator = 2;
					else if (tmp_num == 4)
						denominator = 8;
					else if (tmp_num == 5)
						denominator = 6;				
					
				}
				
    		}
    	}
			
	  	return pos;
	  	
	  }
  
  public int[] locateBars(int[] staff, int[] proj, int min) {
  	
  	int histThr = 4*staff_space;
  	int[] boundaries = new int[20];
  	int num = 0;
  	
  	// get bar line candidates through vertical projection
  	
  	for (int i = 0; i < width; i++) {
  		
  		if (proj[i] >= histThr) {
  			// candidate found
  			//boundaries[num] = i;
			for (int y2 = 0; y2 < strip_height; y2++) {
				for (int j = i-staff_space; j < i+staff_space;j++) {
					if (staff[y2*width+j] == 0)
						staff[y2*width+j] = (255 << 16) | (255 << 8) | 255;
				}
			}

  			num++;
  			
  		}
  	}
  	
  	
  	return boundaries;
  	
  }  
  
    public void identifySymbols(int[] staff_old, int staff_num, int[] lines, int height, int width)  {
    	
    	//strip_height = Math.min(height, lines[4]+3*staff_space);
    	int[] staff = new int[width*height];
    	
    	for (int y = 0; y < strip_height; y++) {
    		for (int x = 0; x < width; x++) {
    			staff[y*width+x] = staff_old[y*width+x];
    		}
    	}
    	
    	// 1) get positions of symbols through projection onto x-axis (fujinaga)
    	int[] proj = verticalProjection(staff, strip_height, width);

    	int[] tmp = new int[strip_height*width];
    	for (int y = 0; y < strip_height; y++) {
	    	for (int x = 0; x < width; x++) {
	    			tmp[y*width+x] = (255 << 16) | (255 << 8) | 255;
	    	}
    	}
    	
    	int y = 0;
    	int z = 0;
    	for (int x = 0; x < width; x++) {
    		y = strip_height-1;
    		z = proj[x];
    		for (;z > 0; z--) {
    			tmp[y*width+x] = (0 << 16) | (0 << 8) | 0;
    			y--;
    		}    	   		
    	}
    	      	
		resultbmp = Bitmap.createBitmap(width, strip_height, Bitmap.Config.RGB_565);
		resultbmp.setPixels(tmp, 0, width, 0, 0, width, strip_height);
		saveToFile(resultbmp, "post_vertproj_"+staff_num+".png");
    	
    	// get global minimum to estimate noise
    	int min = -1;
    	for (int i = 0; i < proj.length; i++) {    		
    		if (proj[i] != 0) {	
    			if (min == -1 || proj[i] < min)
    				min = proj[i];	
    		}    		
    	}
    	
    	float matchingThreshold = 0.0f;
      	int pos = 0;
    	float vote = 0;
    	int duration;   	

    	if (firststaff) {
	    	matchingThreshold = 0.3f;    	
			while (pos == 0 && matchingThreshold < 0.5f) {
	    		pos = matchClefs(staff, tpl_clef, proj, lines, min, h_clef, w_clef, matchingThreshold, "clef", vote);
	       		matchingThreshold += 0.1f;
			}
			clef_pos = pos;
			proj = verticalProjection(staff, strip_height, width);
    	}
    	else
    		pos = clef_pos;

		
    	// QUARTER NOTES		
    	matchingThreshold = 0.25f;
    	duration = 16;
    	matchNotes(staff, tpl_4, tpl_dot, proj, lines, min, h_4, w_4, h_dot, w_dot, matchingThreshold, "1_4", pos, duration);
    	proj = verticalProjection(staff, strip_height, width);
    	
		resultbmp = Bitmap.createBitmap(width, strip_height, Bitmap.Config.RGB_565);
		resultbmp.setPixels(staff, 0, width, 0, 0, width, strip_height);
		saveToFile(resultbmp, "post_quarter_"+staff_num+".png");
		
    	// HALF NOTES		    	
    	matchingThreshold = 0.35f;
    	duration = 32;
    	matchNotes(staff, tpl_2, tpl_dot, proj, lines, min, h_2, w_2, h_dot, w_dot, matchingThreshold, "1_2", pos, duration); 	
    	proj = verticalProjection(staff, strip_height, width);
    	
    	resultbmp = Bitmap.createBitmap(width, strip_height, Bitmap.Config.RGB_565);
		resultbmp.setPixels(staff, 0, width, 0, 0, width, strip_height);
		saveToFile(resultbmp, "post_half_"+staff_num+".png");
    	
    	// FULL NOTES		
    	matchingThreshold = 0.35f;    	
    	duration = 64;
    	matchNotes(staff, tpl_1, tpl_dot, proj, lines, min, h_1, w_1, h_dot, w_dot, matchingThreshold, "1_1", pos, duration);	
    	proj = verticalProjection(staff, strip_height, width);
    	
    	resultbmp = Bitmap.createBitmap(width, strip_height, Bitmap.Config.RGB_565);
		resultbmp.setPixels(staff, 0, width, 0, 0, width, strip_height);
		saveToFile(resultbmp, "post_full_"+staff_num+".png");
    	
		    	/*
		    	// BAR LINES
		    	// existence of double staff if same x-values for bar lines in adjacent staves (gozzi)
		    	int[] barlines = locateBars(staff, proj, min);
		    	proj = verticalProjection(staff, strip_height, width);
		    	*/
    	
      	// RESTS
    	matchingThreshold = 0.2f;
    	duration = 16;
    	matchQuarterRests(staff, tpl_qu_rest, proj, lines, min, h_qu_rest, w_qu_rest, matchingThreshold, pos, duration);    	
    	//proj = verticalProjection(staff, strip_height, width);
    	
		    	/*
		    	matchingThreshold = 0.1f;
		    	int[][] rests = matchRests(staff, tpl_rest, proj, lines, min, h_rest, w_rest, matchingThreshold, pos);    	
		    	proj = verticalProjection(staff, strip_height, width);
		    	*/
		    	/*
		    	// search area next to note heads but exclude barlines
		    	matchAccidentals(staff, quarter_notes, tpl, proj, lines, min, new_h, new_w, matchingThreshold);
		    	proj = verticalProjection(staff, strip_height, width);
		    	matchAccidentals(staff, half_notes, tpl, proj, lines, min, new_h, new_w, matchingThreshold);
		    	proj = verticalProjection(staff, strip_height, width);
		    	matchAccidentals(staff, full_notes, tpl, proj, lines, min, new_h, new_w, matchingThreshold);
				
		    	proj = verticalProjection(staff, strip_height, width);
		    			*/
    	
    	
    	matchingThreshold = 0.3f;
		    	//int time = matchTimeSignature(staff, proj, lines, pos, matchingThreshold);
		    	
		    	//proj = verticalProjection(staff, strip_height, width);

    	// KEY SIGNATURE
    	if (firststaff) {
			proj = verticalProjection(staff, strip_height, width);
			matchKeySignature(staff, tpl_sharp, proj, lines, min, h_sharp, w_sharp, 0.4f, pos);
    	}
	
    	
    	
    	result = staff;
    	firststaff = false;
    }

    
    public int[][] extractStaves(int[] staff_pos) {
    	
    	int high = 0, low = 0, cnt = 0, diff = 0;
    	boolean first = true;
    	int[][] staves = new int[num_lines][7];
    	int s = 0;
    	int line = 1;
    	
    	for (int i = 0; i < height; i++) {
    		
    		if (i == height-1) { // end reached
    			
    			if (cnt == 0) { // otherwise broken staff, will not be extracted    			
	    			low = Math.min(low+3*staff_space, i);
	    			staves[s][0] = high;
	    			staves[s][6] = low;
	    			num_staves++;
	    			line = 1;
    			}
    		}
    		else if (staff_pos[i] == 1) {
	    		if (cnt == 4) { // last staff_line
	    			low = i;
	    			cnt = 0;
	    		}
	    		else if (cnt == 0) {
	    			
	    			if (first) {
	    				
	    				high = Math.max(0,i-(staff_space*3));
	    				first = false;
	    			}
	    			else {
	    			
		    			diff = i - low;
		    			low = i - diff/2;
		    			staves[s][0] = high;
		    			staves[s][6] = low;
		    			s++;
		    			high = low;
		    			num_staves++;
		    			line = 1;

	    			}
	    			    			
	    			cnt++;
	    			
	    		}
	    		else
	    			cnt++;
	    		
				staves[s][line] = i;
				line++;
    		
    		}

    		
    	}
    	
    	return staves;
    	
    }
    
    public int[] removeStaves(int[] projection, int height, int width)  {
    	
    	N = height*width;
    	int idx = 0;
    	int rl = 0;
    	int val = staff_height * 2;
    	int[] staffs = pixels.clone();
    	    	
    	// remove vertical black runs > 2*staff_height
    	// save positions of segments for later use (stems, bar lines)
    	
    	for (int x = 0; x < width; x++) {
	    	for (int y = 0; y < height; y++) {
	    		
	    		if (pixels[idx] == 0)
	    			rl++;
	    		else {
	    			if (rl > 0) {
		    			
		    			if (rl > val) {
		    				
		    				for (int i = 1; i <= rl; i++)
		    					staffs[idx-(i*width)] = (255 << 16) | (255 << 8) | 255;    				
		    			}
		    			rl = 0;
	    			}	
	    		}
	    		
	    		idx += width;
	    		   		
	    	}
	    	idx = x;
	    	rl = 0;
    	}
     	
    	projection = horizontalProjection(staffs, height, width);
    	
    	
    	// remove short line segments - only consider rows where projection[row] > 0
    	idx = 0;
       	boolean has_nb = false;
    	int[] labels = new int[width*height];
    	l = 0;
    	int[] nb_l = new int[4];
    	int min = -1;

    	
    	
    	// FORWARD PASS
    	
    	idx = width+1;
    	for (int y = 1; y < height; y++) {
    		
    		if (projection[y] > 0) {
    			
    			for (int x = 1; x < width-1; x++) {
    				
    				has_nb = false;
    				nb_l[0] = 0;
			    	nb_l[1] = 0;
			    	nb_l[2] = 0;
			    	nb_l[3] = 0;
			    	
					if (staffs[idx] == 0) {
					
						if (projection[y-1] > 0) {
							nb_l[0] = labels[idx-width-1];
							nb_l[1] = labels[idx-width];
							nb_l[2] = labels[idx-width+1];
						}
						nb_l[3] = labels[idx-1];
						for (int i = 0; i < 4; i++) {
							if (nb_l[i] > 0) {
								has_nb = true;
								
							}
						}
													
						if (!has_nb) {
							l++;
							labels[idx] = l;							
								
						}
						else {
				
							min = -1;
							for (int i = 0; i < 4; i++) {
																
								if (nb_l[i] > 0) {
									if (min < 0)
										min = nb_l[i];
									else 
										if (nb_l[i] < min)
											min = nb_l[i];
								}								
							}
																		
							labels[idx] = min;
														
						}
						
						
    					
					}
    		    				    	
					idx+=1;
					
    			}
    			
    			idx+=2;

    		}
    		else
    			idx+=width;
    		
    	}
    	
    	feedback = idx;
    	
    	
    	
    	// BACKWARD PASS
    	idx = (height-1)*width-2;
        int[][] lwidth = new int[l+1][2];
        int[][] lheight = new int[l+1][2];
        
        
        // initialize array
        for (int j = 0; j < l+1; j++) {
        	lwidth[j][0] = 9999;
        	lwidth[j][1] = 0;
        	lheight[j][0] = 9999;
        	lheight[j][1] = 0;
        }
    	
        int nb_cnt = 0;
        
    	for (int y = height-2; y >= 0; y--) {
    		
    		if (projection[y] > 0) {
    			
    			for (int x = width-2; x > 0; x--) {
    				
    				has_nb = false;
    				nb_l[0] = 0;
			    	nb_l[1] = 0;
			    	nb_l[2] = 0;
			    	nb_l[3] = 0;
    				
					nb_cnt = 0;
			    	
    				if (staffs[idx] == 0) {
					
    					if (projection[y+1] > 0) {
	    					nb_l[0] = labels[idx+width+1];
	    					nb_l[1] = labels[idx+width];
	    					nb_l[2] = labels[idx+width-1];
    					}
    					nb_l[3] = labels[idx+1];
						
    					for (int i = 0; i < 4; i++) {
							if (nb_l[i] > 0) {
								nb_cnt++;
							}
						}
	
    					int minX = 9999, maxX = 0, minY = 9999, maxY = 0;
    					
    					if (x < lwidth[labels[idx]][0])
							lwidth[labels[idx]][0] = x;
						
						if (x > lwidth[labels[idx]][1])
							lwidth[labels[idx]][1] = x;
						
    					if (y < lheight[labels[idx]][0])
    						lheight[labels[idx]][0] = y;
						
						if (y > lheight[labels[idx]][1])
							lheight[labels[idx]][1] = y;
						
						minX = lwidth[labels[idx]][0];
						maxX = lwidth[labels[idx]][1];
						minY = lheight[labels[idx]][0];
						maxY = lheight[labels[idx]][1];
    					
    					if (nb_cnt > 0) {							
									
							min = -1;
							for (int i = 0; i < 4; i++) {
																
								if (nb_l[i] > 0) {
									
									if (min < 0)
										min = nb_l[i];
									else 
										if (nb_l[i] < min)
											min = nb_l[i];
								}								
							}
							
							if (min < labels[idx])
								labels[idx] = min;
							
    						
							for (int i = 0; i < 4; i++) {
							
								if (nb_l[i] > 0) {
								
									minX = Math.min(minX, lwidth[nb_l[i]][0]);
									maxX = Math.max(maxX, lwidth[nb_l[i]][1]);
									minY = Math.min(minY, lheight[nb_l[i]][0]);
									maxY = Math.max(maxY, lheight[nb_l[i]][1]);
								
								}
								
							}
							
							for (int i = 0; i < 4; i++) {
								
								if (nb_l[i] > 0) {
								
									lwidth[nb_l[i]][0] = minX;
									lwidth[nb_l[i]][1] = maxX;
									lheight[nb_l[i]][0] = minY;
									lheight[nb_l[i]][1] = maxY;
									
								}
							}
							
							lwidth[labels[idx]][0] = minX;
							lwidth[labels[idx]][1] = maxX;
							lheight[labels[idx]][0] = minY;
							lheight[labels[idx]][1] = maxY;
															
						}
    					
												
					}    		    				    	
					idx-=1;
					
    			}    		
    			idx-=2;

    		}
    		else
    			idx-=width;
    		
    	}
    	
    	
    int spanX, spanY = 0;	
	int thresh = staff_space*2;
	int[] proj;
    
	// XOR staff image with original image
	for (idx = 0; idx < N; idx++) {
	    		
		if (staffs[idx] == 0) {
		    
			int cur_val = labels[idx];
			spanX = lwidth[cur_val][1] - lwidth[cur_val][0];
			spanY = lheight[cur_val][1] - lheight[cur_val][0];
			
			if (spanX < staff_space || spanY > thresh)
				staffs[idx] = (255 << 16) | (255 << 8) | 255;
			
			
		}
    	
	}
	
	// recalculate staff_height and staff_space and remove any component higher than staff_height + x
	estimateStaffs(staffs, height, width);
	
	for (idx = 0; idx < N; idx++) {
		
		if (staffs[idx] == 0) {
		    
			int cur_val = labels[idx];
			spanY = lheight[cur_val][1] - lheight[cur_val][0];
			
			if (spanY > staff_height+1)
				staffs[idx] = (255 << 16) | (255 << 8) | 255;
			
		}
    	
	}
	
	// find position of lines by calculation of local maxima
	proj = horizontalProjection(staffs, height, width);
	estimateStaffs(staffs, height, width);
	
	int[] staff_pos = new int[height];
	int max = -1;
	int pos = -1;
	int y = 0;
	int i = 0;
	int thr = (int) (width*0.4);
	
	while (y < height) {
		
		if (proj[y] > thr) {
		
			while (i < staff_height) {
				
				if (proj[y+i] > thr)
					staff_pos[y+i] = 2;
				
				if (proj[y+i] > max) {
					max = proj[y+i];
					pos = y+i;
				}
				i++;
			}
					
			staff_pos[pos] = 1;
			num_lines++;
			
			i = 0;
			max = -1;
			pos = -1;
			
			y += staff_space/2;
		}
		else
			y++;
		

	
	}
	
	
	// remove components in non-staff-lines
	idx = 0;
	for (y = 0; y < height; y++) {
		
		if (staff_pos[y] == 0) {
		
			for (int x = 0; x < width; x++) {
				
				if (staffs[idx] == 0) {
					staffs[idx] = (255 << 16) | (255 << 8) | 255;
				}
				
				idx++;			
			}			
		}		
		else
			idx+=width;				
	}
	

	for (idx = 0; idx < N; idx++) {
			
		if (staffs[idx] == 0)
			pixels[idx] = (255 << 16) | (255 << 8) | 255;
		
	}
			    
	
  	
	return staff_pos;
	
}

    
    public int ccSearch(int[] pixels, int idx) {
    	
    	int ccw = 0;
    	
    	
    	
    	
    	return ccw;
    	
    }
    
public int[] deskewImage(int[] pixels)  {
    	
		int[] binary_pixels = binariseImage(pixels);  
	
    	// set centre window
    	int l = width/2-16;
    	int r = l+32;
    	int N = height*width;
    	int idx, idxw;
    	int[] window;
    	int[] proj = null;
    	int[] proj_ref = null;
    	int str_w = (int) Math.ceil(width/32);
    	int[] offset_right = new int[str_w];
    	int[] offset_left = new int[str_w];
    	int s = 0;
    	int max_pos = 0;
    	int max_coeff = 0;
		int coeff = 0;
		int y2 = 0;
		float slope = 0;
		int offset = 0;
    	
    	window = new int[32*height];
    	
    	while (r < width) {
    		
	    	for (int y = 0; y < height; y++) {
	    		for (int x = 0; x < 32; x++) {
	    			idx = y*width + l + x;
	    			idxw = y*32 + x;
	    			if (idx < N)
	    				window[idxw] = binary_pixels[idx];
	    			else
	    				window[idxw] = (255 << 16) | (255 << 8) | 255;
	    		}
	    	}
	    		    	
	    	proj = horizontalProjection(window, height, 32);
	    	
	    	if (s == 0) 
	    		proj_ref = proj;

	    	else {
	    		
	    		int search_size = height/16;
	    		int i = search_size * -1;
	    		
	    		while (i < search_size) {
	    		
		    		// largest correlation coefficient is best match
			    	for (int y = 0; y < height; y++) {
			    					    			
			    		y2 = y + i;
			    		if (y2 > 0 && y2 < height)
			    			coeff += proj_ref[y] * proj[y2];

			    	}
	
		    		if (coeff > max_coeff) {
		    			max_pos = i;
		    			max_coeff = coeff;
		    		}
		    		
		    		i++;
		    		coeff = 0;
	    		
	    		}
	    	 		
	    		// once the best match has been found, combine the 2 projections
    			int new_y;
    			
				for (int y = 0; y < height; y++) {
					
					new_y = y+max_pos;
					
					if (new_y >= 0 && new_y < height)
						proj_ref[y] += proj[y+max_pos];
    			}
	    		
	    	}
	    	
	    	max_coeff = 0;
	    	offset_right[s] = max_pos;
	    	
	    	int d = 0;
	    	
	    	if (s > 0)
	    		d = max_pos-offset_right[s-1];
	    	offset += d;
	    	offsets += s + "th strip:" + d + ", ";
	    		    
	    	l += 32;
	    	r += 32;
	    	
	    	s++;
    	
    	}

    	s = 0;
    	l = (int) Math.ceil(width/2-16);
    	r = l+32;
    	max_pos = 0;

    	// d(y) = 40px (centre-right)
    	

    	while (l > 0) {
        	
	    	for (int y = 0; y < height; y++) {
	    		for (int x = 0; x < 32; x++) {
	    			idx = y*width + l + x;
	    			idxw = y*32 + x;
	    			if (idx > 0)
	    				window[idxw] = binary_pixels[idx];
	    			else
	    				window[idxw] = 255;
	    		}
	    	}
	    		    	
	    	proj = horizontalProjection(window, height, 32);
	    	
	    	if (s == 0) 
	    		proj_ref = proj;

	    	else {
	    		
	    		int search_size = height/16;
	    		int i = search_size * -1;
	    		
	    		while (i < search_size) {
	    		
		    		// largest correlation coefficient is best match
			    	for (int y = 0; y < height; y++) {
			    					    			
			    		y2 = y + i;
			    		if (y2 > 0 && y2 < height)
			    			coeff += proj_ref[y] * proj[y2];

			    	}
	
		    		if (coeff > max_coeff) {
		    			max_pos = i;
		    			max_coeff = coeff;
		    		}
		    		
		    		i++;
		    		coeff = 0;
	    		
	    		}
	    	 		
	    		// once the best match is found, combine the 2 projections
    			
				for (int y = 0; y < height; y++) {
					if ((y+max_pos) >= 0 && (y+max_pos) < height)
						proj_ref[y] += proj[y+max_pos];
    			}
	    		
	    	}
	    	
	    	max_coeff = 0;
	    	offset_left[s] = max_pos;
	    	
	    	int d = 0;
	    	
	    	if (s > 0)
	    		d = max_pos-offset_left[s-1];
	    	offset -= d;
	    	offsets += s + "th strip:" + d + ", ";
	    		    
	    	l -= 32;
	    	r -= 32;
	    	
	    	s++;
	    	
	    	
    	
    	}
    	
    	// shear image
    	// offset negative: image had to be shifted upwards y
    	
    	// offset = slope per 32 (or less) px
    	// example: offset=2 -> 32/2 = 16 -> shift every 16 rows
    	
    	// use bresenham's line drawing algorithm
    	
    	
    	int y = 0;
    	int x = 0;
    	int idx2;
    	slope = (float)offset/(2*(s-1)*32);
    	float shift = 0;
    	int rnd_shift;
    	int[] temp = new int[height*width];
    	
		N = height*width;
    	

    	// shearing 1
    	
    	for (x = 0; x < width; x++) {
    		
    		rnd_shift = Math.round(shift);
    		
    		for (y = 0; y < height; y++) {
    		
    			idx = y*width + x;
    			idx2 = Math.max(0, Math.min(height, y+rnd_shift))*width + x;
    			if (idx2 >= 0 && idx2 < N)
    				temp[idx] = pixels[idx2];
    			else 
    				temp[idx] = pixels[idx];
    			
    			
    		}    		   
    		
    		shift += slope;
    		
    	}
    	
    	// shearing 2
    	
    	shift = 0;
    	
    	for (y = 0; y < height; y++) {
    		
    		rnd_shift = Math.round(shift);
    		
    		for (x = width-1; x >= 0; x--) {
    			
    			idx = y*width + x;
    			idx2 = y*width + Math.max(0, Math.min(width, x-rnd_shift));
    			if (idx2 >= 0 && idx2 < N)
    				pixels[idx] = temp[idx2];
    			else 
    				pixels[idx] =  (255 << 16) | (255 << 8) | 255;
    			
    		}
    		
    		shift += slope;
    		
    	}
    
    	
    	skew = (float) Math.toDegrees(Math.atan(slope));
    	skew *= 100;
    	skew = (int)skew;
    	skew /= 100;
    	return pixels;
    	
    }
    
    
    public int[] horizontalProjection(int[] pixels, int height, int width)  {
    	
    	int[] proj = new int[height];
    	int idx, val;
    	
    	for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				idx = y*width + x;
				val = (pixels[idx] >> 16) & 0xff;
				if (val == 0)
					proj[y]++;
				}
    	}

    	    	
    	return proj;
    	
    }
    
	 public int[] verticalProjection(int[] staff, int height, int width)  {
	    	
		int[] proj = new int[width];
	 	int idx;
	 	
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				
				idx = y*width + x;
				if (staff[idx] == 0)
					proj[x]++;
				}
	 	}
		 	    	
	 	return proj;
    	
    }
    
    public int estimateStaffs(int[] pixels, int height, int width)  {

    	int[] lines = new int[height];
    	int[] spaces = new int[height];
    	int idx, val, last, current = 0;
    	int max = 0, max2 = 0;
		  
    	// create a run-length matrix
    	
    	int x = width/2;
    	
    		val = 0;
    		last = -1;
    		
    		for (int y = 0; y < height; y++) {
				
    			idx = y*width + x;
    			current = (pixels[idx] >> 16) & 0xff;
    			
				if (last == current || last == -1) {
					val++;
					
				}
				else {
					if (last == 0)
						lines[val]++;
					else
						spaces[val]++;
					val = 1;

				}
				
				last = current;
				
    		}
					
    	
    	
    	// get staffline height and inter-staff space)
    	
    	for (int i = 1; i < height; i++) {
    		
    		if (lines[i] >= max) {
    			max = lines[i];
    			staff_height = i;
    		}
    		
    		if (spaces[i] >= max2) {
    			max2 = spaces[i];
    			staff_space = i;
    		}
    		
    		
    	}
    	
    	if (staff_height < 1 || staff_space < 2)
    		return -1;
    	else
    		return 0;

    	
    }
    
    
    public int[] binariseImage(int[] pixels)  {
    	
    	int idx, T, newT, meanFG, meanBG;
    	int val;
    	long FG, BG, numBG, numFG;
  	
    	int winStartX, winEndX, winStartY, winEndY;
    	int chunkX = width/8;
    	int chunkY = height/8;
    	
    	for (int i = 0; i < 8; i++) {
    		for (int j = 0; j < 8; j++) {
    	
    		winStartX = 0 + j*chunkX;
    		winEndX = winStartX + chunkX;
    		winStartY = 0 + i*chunkY;
    		winEndY = winStartY + chunkY;
    	
    		T = 128;
    		newT = 0;
    		
	    	while (T != newT) {
	    	    		
	    		numFG = 0;
	    		numBG = 0;
	    		FG = 0;
	    		BG = 0;
	    		
	    		T = newT;
	    		     		
		    	for (int y = winStartY; y < winEndY; y++) {
		    		
		    		if (y >= height)
		    			break;
		    		
					for (int x = winStartX; x < winEndX; x++) {
		    	
						idx = y * width + x;
						if (x >= width)
							break;
												
						val = (pixels[idx] >> 16) & 0xff;
						if (val < newT) {
							FG += val;
							numFG++;
						}
						else {
							BG += val;
							numBG++;
						}
						
					}
		    	}
		    	
		    	if (numFG > 0)
		    		meanFG = (int)(FG/numFG);
		    	else
		    		meanFG = 0;
		    	
		    	if (numBG > 0)
		    		meanBG = (int)(BG/numBG);
		    	else
		    		meanBG = 0;
		    	
		    	newT = (int)((meanFG + meanBG)/2);	    	
		    	
	    	}
	    	
	    	// apply threshold to window
	    	for (int y = winStartY; y < winEndY; y++) {
				for (int x = winStartX; x < winEndX; x++) {
					
					idx = y * width + x;
					
					if (idx < width*height) {
						val = (pixels[idx] >> 16) & 0xff;
		
						if (val < T) {
							val = 0;
						}
						else {
							val = 255;
						}
						
						pixels[idx] = (val << 16) | (val << 8) | val;
					}
				}
				}
	    	}
    	}
		return pixels;
    	
    }
    
    
    public int[] smoothImage(int[] pixels, int height, int width)  {
    	
    	int[] median = new int[9];
    	int idx, med;
    	
    	// median filter to remove noise
    	for (int y = 1; y < height-1; y++) {
			for (int x = 1; x < width-1; x++) {
				
				idx = y*width + x;
				
				median[0] = pixels[(y-1)*width + (x-1)] & 0xff;
				median[1] = pixels[(y-1)*width + (x)] & 0xff;
				median[2] = pixels[(y-1)*width + (x+1)] & 0xff;
				median[3] = pixels[(y)*width + (x-1)] & 0xff;
				median[4] = pixels[(y)*width + (x)] & 0xff;
				median[5] = pixels[(y)*width + (x+1)] & 0xff;
				median[6] = pixels[(y+1)*width + (x-1)] & 0xff;
				median[7] = pixels[(y+1)*width + (x)] & 0xff;
				median[8] = pixels[(y+1)*width + (x+1)] & 0xff;
				
				Arrays.sort(median);
				med = median[4];
				pixels[idx] = (med << 16) | (med << 8) | med;
				
			}
		}
		
    	return pixels;
    	
    }
    
    
    public int[] houghTransform(int[] pixels, int height, int width)  {
    	
    	// HOUGH TRANSFORM
    	// http://csb.essex.ac.uk/software/HoughTransform/HoughTransform.java.html
    	//
    	
    	
    	int[] hough = new int[pixels.length];
    	final int maxTheta = 30;
    	double tStep = Math.PI/maxTheta;
    	int threshold = 200;
    	int rmax = (int)Math.sqrt(width*width + height*height);
    	int idx;
    	
    	// array consists of number of occurances for each combination of alpha, d
    	int[][] hArray = new int[maxTheta][rmax];	

    	int r;
    	/*
    	// initialise arrays
    	for (int t = 0; t < maxTheta; t++) { 
    		tval  = t * tStep; 
            sinuses[t] = Math.sin(tval); 
            cosinuses[t] = Math.cos(tval); 
        } 
    	*/
    	    	
    	for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
			
				idx = y * width + x;
				int val = pixels[idx] & 0xff;
				if (val == 0) {
					
					for (int t = 0; t < maxTheta; t++) {
						
						r = (int) ((x - width/2)*Math.cos(t)+(y-height/2)*Math.sin(t));
					
						 if (r > 0) {
							 hArray[t][r]++;
						 }
					}
				}
			}
    	}
    	
    	
    	// find local maximum in the hough array (5x5 neighborhood)
        boolean max = true; 
        for (int t = 0; t < maxTheta; t++) { 
            for (r = 0; r < rmax; r++) { 
                if (hArray[t][r] > threshold) { 

                	max = true;
                	
                	for (int dt = (t-2); dt <= (t+2); dt++) {
                		for (int dr = (r-2); dt <= (r+2); dr++) {
                			
                			if ((dr >= 0) && (dr < rmax) && (dt >= 0) && (dt < maxTheta) &&  (hArray[dt][dr] > hArray[t][r]))
                            {
                               max = false;
                               break;
                            }
                		}
                	}
                	
	               	// draw lines onto image
	                if (max == true) {
	                	
	                	for (int x = 0; x < width; x++) {
	                		int y = (int) (r/Math.sin(t) - x*Math.cos(t)/Math.sin(t));
	                		if (y < height)
	                			hough[y*width + x] = (255 << 16) | (255 << 8) | 255;
	                	}
	                }
                	
                }

            } 
        }
        
    	
    	/*
    	// find cells with highest values (local maxima in accumulator space)
    	for (int t = 0; t < maxTheta; t++) {
    		
    		loop:
    		
    		for (r = neighbours; r < (maxHeight*2 - neighbours); r++) {
    			
    			if (hArray[t][r] > threshold) {
    				
    				int peak = hArray[t][r];
    				
    				// is peak the local maximum?
    				for (int dx = -neighbours; dx < neighbours+1; dx++) {
    					for (int dy = -neighbours; dy < neighbours+1; dy++) {
    						
    						int dt = t + dx; 
                            int dr = r + dy; 
                            if (dt < 0) dt = dt + maxTheta; 
                            else if (dt >= maxTheta) dt = dt - maxTheta; 
                            if (hArray[dt][dr] > peak) { 
                                // found a bigger point nearby, skip 
                                continue loop; 
                            }     						
    					}
    				}
    				
    				// define a new hough line (theta, r)
                    double theta = t * tStep;
                    
    			}
    			
    		}
    		
    	}
    	*/
    	

    	return hough;
    	
    	
    	
    }
    
    public int[] getEdges(int[] pixels, int height, int width)  {
    
    	int[] edges = new int[width*height];
    	
    	int idx, sum, a, b, c, d, e, f;
    	
    	// sobel filter
    	for (int y = 1; y < height-1; y++) {
			for (int x = 1; x < width-1; x++) {
				
				sum = 0;
				idx = y*width + x;
				
				a = pixels[(y-1)*width + (x-1)] & 0xff;
				b = pixels[(y-1)*width + (x)] & 0xff;
				c = pixels[(y-1)*width + (x+1)] & 0xff;
				d = pixels[(y+1)*width + (x-1)] & 0xff;
				e = pixels[(y+1)*width + (x)] & 0xff;
				f = pixels[(y+1)*width + (x+1)] & 0xff;
				
				sum = -a-b-c+d+e+f;
				
				if (sum < 0)
					sum = 0;
				else if (sum > 255)
					sum = 255;

				edges[idx] = (sum << 16) | (sum << 8) | sum;
				
			}
		}
    	
    	return edges;
    	
    
    }
    
}

/*
// median filter

int[] proj_filtered = new int[w];
int num_filtering = staff_height;
int[] ker = new int[3];

for (int n = 0; n < num_filtering; n++) {

	for (int j = 0; j < w; j++) {

		if (j == 0) {
			ker[0] = proj2[j];
			ker[1] = proj2[j];
			ker[2] = proj2[j+1];
		}
		else if (j == w-1) {
			ker[0] = proj2[j-1];
			ker[1] = proj2[j];
			ker[2] = proj2[j];
		}
		else {
			ker[0] = proj2[j-1];
			ker[1] = proj2[j];
			ker[2] = proj2[j+1];
		}
		
		Arrays.sort(ker);
		proj_filtered[j] = ker[1];
		
	}
	
	for (int p = 0; p < w; p++) {
		proj2[p] = proj_filtered[p];
	}
	
}			

*/
