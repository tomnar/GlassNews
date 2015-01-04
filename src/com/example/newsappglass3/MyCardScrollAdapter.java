package com.example.newsappglass3;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

public class MyCardScrollAdapter extends CardScrollAdapter {
	
	private ArrayList<CardWrapper> mCards;
	public MyCardScrollAdapter(){
		mCards = new ArrayList<CardWrapper>();
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
        return mCards.get(position).getCard();
    }
    

    @Override
    public int getViewTypeCount() {
        return CardBuilder.getViewTypeCount();
    }

//    @Override
//    public int getItemViewType(int position){
//        return mCards.get(position).getCard().getItemViewType();
//    }

	@Override
	public View getView(int position, View convertView,
            ViewGroup parent) {
		Object obj = mCards.get(position).getCard();
		if(obj instanceof CardBuilder){
			CardBuilder cardBuilder = (CardBuilder)obj;
			return cardBuilder.getView(convertView, parent);
		}else if(obj instanceof RelativeLayout){
			return (RelativeLayout)obj;
		}
		return null;
	}
	
	public void addItem(CardWrapper card){
		mCards.add(card);
	}
	
	public void removeItem(int index){
		mCards.remove(index);
	}
	
	public void setCards(ArrayList<CardWrapper> cards){
		mCards = cards;
	}
	
	
}
