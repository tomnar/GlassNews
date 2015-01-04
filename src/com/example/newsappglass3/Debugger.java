package com.example.newsappglass3;

public class Debugger {
	
	public static void log(String msg){
		android.util.Log.d("newsapp", msg);
	}
	public static void logCycle(String msg){
		android.util.Log.d("cycle", msg);
	}
}
