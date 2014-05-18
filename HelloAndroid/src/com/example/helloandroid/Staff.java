package com.example.helloandroid;

public class Staff {

	private int width;
	private int height;
	private int strip_height;
	private int staff_height;
	private int staff_space;
	private int[] data;
	private int[] proj;
	private int[] lines;
	
	float matchingThreshold = 0.0f;
  	int pos = 0;
	float vote = 0;
	int duration;   
	int min;
	int clef_pos;
	
	public void setValues(int width, int height, int strip_height, int staff_height, int staff_space) {
		this.width = width;
		this.strip_height = strip_height;
		this.staff_height = staff_height;
		this.staff_space = staff_space;
		this.data = new int[width*height];
	}
	
	public void dowhat() {
		

		
	}
	
	private int[] verticalProjection(int[] data, int height, int width)  {
	    	
		int[] proj = new int[width];
	 	int idx;
	 	
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				
				idx = y*width + x;
				if (data[idx] == 0)
					proj[x]++;
				}
	 	}
		 	    	
	 	return proj;
   	
	}
	
	private int getMinimum(int[] proj) {
		
    	// get global minimum to estimate noise
    	int min = -1;
    	for (int i = 0; i < proj.length; i++) {    		
    		if (proj[i] != 0) {	
    			if (min == -1 || proj[i] < min)
    				min = proj[i];	
    		}    		
    	}
    	
    	return min;
	}
	
}
