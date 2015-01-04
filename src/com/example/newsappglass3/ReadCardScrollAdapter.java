package com.example.newsappglass3;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

public class ReadCardScrollAdapter extends CardScrollAdapter {
	
	private ArrayList<CardBuilder> mCards;
	public ReadCardScrollAdapter(){
		mCards = new ArrayList<CardBuilder>();
	}
	@Override
    public int getPosition(Object item) {
        return mCards.indexOf(item);
    }

    @Override
    public int getCount() {
        return mCards.size();
    }

    @Override
    public Object getItem(int position) {
        return mCards.get(position);
    }

    @Override
    public int getViewTypeCount() {
        return CardBuilder.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position){
        return mCards.get(position).getItemViewType();
    }

	@Override
	public View getView(int position, View convertView,
            ViewGroup parent) {
		return  mCards.get(position).getView(convertView, parent);
	}
	
	public void addItem(CardBuilder card){
		mCards.add(card);
	}
	
	public void removeItem(int index){
		mCards.remove(index);
	}
	
	public void setCards(ArrayList<CardBuilder> cards){
		mCards = cards;
	}
	
	
}
