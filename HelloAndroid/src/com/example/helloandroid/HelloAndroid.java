package com.example.helloandroid;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
//import android.content.CursorLoader;
import android.content.Intent;
//import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


@SuppressLint("ParserError")
public class HelloAndroid extends Activity {
	
	private static final int TAKE_PICTURE = 1;
	
	Button takeImage;
	Button fromFile;
	Button exitApp;
	//Switch recSwitch;
	//Switch highresSwitch;
	
	String fileName = "capture.png";
	Uri imageUri;
	ContentValues values = new ContentValues();	
    TextView text;
    Intent myIntent;
    Intent myIntent2;
    ImageView image;
    String test = "false";
    Uri outputFileUri;

    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);
        text = (TextView) findViewById(R.id.text);
		final Bundle bundle = new Bundle();

		exitApp = (Button)findViewById(R.id.exitApp);
	    exitApp.setTextColor(Color.GRAY);
	    exitApp.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        	     finish();
        	}        	        	
        });
   
        takeImage = (Button)findViewById(R.id.takeImage);
        takeImage.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				
				// TODO Auto-generated method stub
				
				values.put(MediaStore.Images.Media.TITLE, fileName);
				values.put(MediaStore.Images.Media.DESCRIPTION,"No description");
				
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		        
		        //setting the directory and filename
		        File file = new File(Environment.getExternalStorageDirectory(), "capture.png");
		        
		        //output file URI
		        outputFileUri = Uri.fromFile(file);
		        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		        startActivityForResult(intent, TAKE_PICTURE);

			}
			
						
		});
        
        fromFile = (Button)findViewById(R.id.fromFile);
        fromFile.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				
				// TODO Auto-generated method stub

	            bundle.putString("source", "file");
	            bundle.putInt("res", 4000000);
	            
	            myIntent = new Intent(view.getContext(), Precomp.class);
	            myIntent.putExtras(bundle);
                
	           startActivity(myIntent);
			}
						
		});

        
	   // fromFile.performClick();
        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
		if (requestCode == TAKE_PICTURE) {
			           
                Bundle bundle = new Bundle();
                bundle.putString("source", "camera");
                bundle.putString("uri", outputFileUri.toString());
                bundle.putInt("res", 5000000);
                
                myIntent = new Intent(this, Precomp.class);
                myIntent.putExtras(bundle);
                
                startActivity(myIntent);		
      
            
		}
		
		
	}
    


    
}