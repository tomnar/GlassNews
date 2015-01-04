package com.example.newsappglass3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;


public class NewsItem implements Serializable{
	private String mHeadline;
	private String mSubHeadline;
	private String mImageUrl;
	private String mBody;
	private String mDate;
	private boolean mBookmarked;
	private int mId;
	private String mThumbnailUrl;
	private String mSection;
	private String mAudioUrl;
	private String mExternalImageUrl = null;
	private String mExternalAudioUrl = null;
	//private Bitmap mBitmap;
	//private byte[] mBitmapBytes;
	private ArrayList<NewsItem> mRelated;
	private ArrayList<Integer> mRelatedIDs;
	
	public NewsItem(int id, String headline, String subHeadline, String body, String date, String imageUrl, String thumbnailUrl, String section, String audioUrl){
		mId = id;
		mHeadline = headline;
		mSubHeadline = subHeadline;
		mBody = body;
		mDate = date;
		mImageUrl = imageUrl;
		mBookmarked = false;
		mThumbnailUrl = thumbnailUrl;
		mSection = section;
		mAudioUrl = audioUrl;
		mRelated = new ArrayList<NewsItem>();
		mRelatedIDs = new ArrayList<Integer>();
	}
	public int getID(){
		return mId;
	}
/*	public void setID(String id){
		mId = id;
	}*/
	public String getHeadline() {
		return mHeadline;
	}
	public void setHeadline(String headline) {
		mHeadline = headline;
	}
	public String getSubHeadline() {
		return mSubHeadline;
	}
	public String getBody() {
		return mBody;
	}
	public String getDate() {
		return mDate;
	}
	public String getImageUrl() {
		return mImageUrl;
	}
//	public Bitmap getBitmapImage() {
//		return mBitmap;
//	}
//	public void setBitmapImage(Bitmap bitmap){
//		mBitmap = bitmap;
//	}
//	public byte[] getBitmapBytes() {
//		return mBitmapBytes;
//	}
//	public void setBitmapBytes(byte[] bitmapBytes){
//		mBitmapBytes = bitmapBytes;
//	}
	public String getExternalImageUrl() {
		return mExternalImageUrl;
	}
	public void setExternalImageUrl(String externalImageUrl){
		mExternalImageUrl = externalImageUrl;
	}
	public String getExternalAudioUrl() {
		return mExternalAudioUrl;
	}
	public void setExternalAudioUrl(String externalAudioUrl){
		mExternalAudioUrl = externalAudioUrl;
	}
	public String getThumbnailUrl(){
		return mThumbnailUrl;
	}
	public boolean getBookmarked() {
		return mBookmarked;
	}
	public void setBookmarked(boolean bookmarked){
		mBookmarked = bookmarked;
	}
	public String getSection(){
		return mSection;
	}
	public String getAudioUrl(){
		return mAudioUrl;
	}
	public String getTimeliness(){
		return calculateTimeliness(mDate);
	}
	
	public ArrayList<Integer> getRelatedIDs(){
		return mRelatedIDs;
	}
	public void setRelatedIDs(ArrayList<Integer> relatedIDs){
		mRelatedIDs = relatedIDs;
	}
	public void addRelatedIDs(int ID){
		mRelatedIDs.add(ID);
	}
	
	public ArrayList<NewsItem> getRelated(){
		return mRelated;
	}
	public void setRelated(ArrayList<NewsItem> related){
		mRelated = related;
	}
	public void addRelated(NewsItem related){
		mRelated.add(related);
	}
	@Override
	public boolean equals(Object other){
		NewsItem otherNewsItem = (NewsItem) other;
		return mId == otherNewsItem.getID();
	}
	
	@Override
	public String toString(){
		return mHeadline + " "  +mSection;
	}
	
	private String calculateTimeliness(String dateString) {
 		Debugger.log("date string " + dateString);
 		String dayString = dateString.substring(0, 2);
    	String monthString = dateString.substring(3, 5);
    	String yearString = dateString.substring(6, 10);
    	String hourString = dateString.substring(11, 13);
    	String minuteString = dateString.substring(14, 16);
    	int year = Integer.parseInt( yearString );
    	int month = Integer.parseInt( monthString );
    	int day = Integer.parseInt( dayString );
    	int hour = Integer.parseInt( hourString );
    	int minute = Integer.parseInt( minuteString );
    	Debugger.log("year " + year + " month " + month + " day " + day + " hour " + hour + " minute " + minute);
    	GregorianCalendar pubDate = new GregorianCalendar(year, month-1, day, hour, minute);
    	GregorianCalendar today =  (GregorianCalendar)GregorianCalendar.getInstance();

//    	
    	Debugger.log("time pubdate " + pubDate.getTime() + " time zone " + pubDate.getTimeZone().getDisplayName());
    	Debugger.log("time today " + today.getTime() + " time zone " + today.getTimeZone().getDisplayName());
//    	
    	long pubDateMillis= pubDate.getTimeInMillis();
    	long todayMillis = today.getTimeInMillis();

    	
    	long difference = todayMillis - pubDateMillis;
    	double days = difference / 1000.0 / 60.0 / 60.0 / 24.0;
    	if(days < 1){
    		double hours = days * 24.0;
    		if(hours < 1){
    			double minutes = hours * 60.0;
    			return (int)minutes + " minutes ago";
    		}else{
    			return (int)hours + " hours ago";
    		}
    	}else{
    		return (int)days + " days ago";
    	}
    	
    	
 	}
	
	
	
	// Parcelling part
//    public NewsItem(Parcel in){
//        String[] data = new String[9];
//        boolean[] dataBoolean = new boolean[1];
//
//        in.readStringArray(data);
//        in.readBooleanArray(dataBoolean);
////        mHeadline = data[0];
////        mSubHeadline = data[1];
////        mBody= data[2];
////        mDate = data[3];
////        mImageUrl = data[4];
//        
//        mId = data[0];
//		mHeadline = data[1];
//		mSubHeadline = data[2];
//		mBody = data[3];
//		mDate = data[4];
//		mImageUrl = data[5];
//		mThumbnailUrl = data[6];
//		mSection = data[7];
//		mAudioUrl = data[8];
//		
//		mBookmarked = dataBoolean[0];
//		
//		if(mBitmap != null){
//			mBitmap = Bitmap.CREATOR.createFromParcel(in);
//		}
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        //dest.writeStringArray(new String[] {mHeadline, mSubHeadline, mBody, mDate, mImageUrl});
//        dest.writeStringArray(new String[] {mId, mHeadline, mSubHeadline, mBody, mDate, mImageUrl, mThumbnailUrl, mSection, mAudioUrl});
//        dest.writeBooleanArray(new boolean[]{mBookmarked});
//        if(mBitmap != null){
//        	mBitmap.writeToParcel(dest, 0);
//        	dest.setDataPosition(0);
//        }
//    }
//    public static final Parcelable.Creator<NewsItem> CREATOR = new Parcelable.Creator<NewsItem>() {
//        public NewsItem createFromParcel(Parcel in) {
//            return new NewsItem(in); 
//        }
//
//        public NewsItem[] newArray(int size) {
//            return new NewsItem[size];
//        }
//    };
//
//	@Override
//	public int describeContents() {
//		return 0;
//	}

}