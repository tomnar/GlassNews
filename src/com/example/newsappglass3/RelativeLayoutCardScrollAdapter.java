package com.example.newsappglass3;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

public class RelativeLayoutCardScrollAdapter extends CardScrollAdapter {

	private ArrayList<RelativeLayout> mViews;
	public RelativeLayoutCardScrollAdapter(ArrayList<RelativeLayout> views) {
		mViews = views;
	}
	
	@Override
	public int getCount() {
		return mViews.size();
	}

	@Override
	public Object getItem(int index) {
		return mViews.get(index);
	}

	@Override
	public int getPosition(Object obj) {
		return mViews.indexOf(obj);
	}

	@Override
	public View getView(int index, View arg1, ViewGroup arg2) {
		return mViews.get(index);
	}
	

	public void removeItem(int index){
		mViews.remove(index);
	}

}
