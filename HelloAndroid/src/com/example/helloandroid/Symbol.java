package com.example.helloandroid;

public class Symbol implements Comparable<Symbol>{

	int pos = -1;
	int pitch = -1;
	int dur = -1;
	Symbol(int pos, int pitch, int dur) {
		this.pos = pos;
		this.pitch = pitch;
		this.dur = dur;
	}
	
	public int compareTo(Symbol another) {
		// TODO Auto-generated method stub
		return ((Integer) pos).compareTo((Integer) another.pos);
	}
	
	public int getPos(){
		return this.pos;
	}

	public int getPitch(){
		return this.pitch;
	}
	
	public int getDur(){
		return this.dur;
	}
	
}
