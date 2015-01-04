package com.example.newsappglass3;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;

public class StreamPlayer {
	
	public static final int S1 = R.raw.applause;
    public static final int S2 = R.raw.boo;
    //public static final int S3 = R.raw.s3;
	
	private static MediaPlayer player;
	private static ArrayList<String> urls = new ArrayList<String>();
	private static int index = 0;


	/** Populate the SoundPool*/
	public static void initMediaPlayer(OnCompletionListener completionListener) {
	    player = new MediaPlayer();
	    player.setScreenOnWhilePlaying(true);
		
	}
	
	public static void stopMediaPlayer() {
		if(player != null){
			if(player.isPlaying()){
				fadeOut(); //move to speraee thread!!
				player.stop();
				//new Thread(fadeOutRunnable).start();
				
			}
			player.reset();
		}
	}
	
	public static void releaseMediaPlayer() {
		if(player != null){
			player.release();
			player = null;
		}
	}
	
	public static boolean isPlaying(){
		if(player != null){
			return player.isPlaying();
		}
		return false;
	}
	
	 /** Play a given sound in the soundPool */
	 public static void streamSoundFile(String url, OnCompletionListener completionListener, 
			 OnPreparedListener preparedListener) {
		if(player == null){
		   initMediaPlayer(completionListener);
		}
	    float volume = 1;// whatever in the range = 0.0 to 1.0
	    
	    if(!player.isPlaying()){
		    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		    try {
		    	 player.setDataSource(url);
				 player.setOnCompletionListener(completionListener);
//				 player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//				        @Override
//				        public void onPrepared(MediaPlayer mp) {
//				            player.start();
//				        }
//				    });
				 player.setOnPreparedListener(preparedListener);

			  player.prepareAsync(); // might take long! (for buffering, etc)
				//player.start();
				 //new Thread(streamRunnable).start();
			} catch (IllegalArgumentException | SecurityException
					| IllegalStateException | IOException e) {
				e.printStackTrace();
			}
	    }
	 }
	 
	 public static void startSoundFile(){
		 player.start();
	 }
	 
//	 public static void playDownloadedSound(String url, OnCompletionListener completionListener){
//		   initMediaPlayer(completionListener);
//				
//		   float volume = 1;// whatever in the range = 0.0 to 1.0
//			    
//		    if(!player.isPlaying()){
//			    
//		    }
//	 }
	 
	 private static void fadeOut(){
		 float i = 1;
		 while( i > 0){
			 i -= 0.05;
			 player.setVolume(i, i);
			 try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	 }
	 
	 public static void addUrl(String url){
		 urls.add(url);
	 }
	 
	 public static ArrayList<String> getURLs(){
		 return urls;
	 }
	 
	 public static String getURL(int index){
		 return urls.get(index);
	 }
	 
//	 private static final Runnable streamRunnable = new Runnable() {
//		
//		@Override
//		public void run() {
//			try {
//				
//				player.prepare();// might take long! (for buffering, etc)
//				player.start();
//				//player.start();
//			} catch (IllegalStateException | IOException e) {
//				e.printStackTrace();
//			}  
//		}
//	};
	
//	 private static final Runnable fadeOutRunnable = new Runnable() {
//			
//			@Override
//			public void run() {
//				try {
//					fadeOut();
//					player.stop();
//				} catch (IllegalStateException e) {
//					e.printStackTrace();
//				}  
//			}
//		};
	 
	
}
