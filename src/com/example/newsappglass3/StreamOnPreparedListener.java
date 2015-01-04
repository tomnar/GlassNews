package com.example.newsappglass3;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;

import com.google.android.glass.widget.CardBuilder;

public class StreamOnPreparedListener implements OnPreparedListener {

	private CardBuilder mCard;
	private MyCardScrollAdapter mCardScrollAdapter;
	public StreamOnPreparedListener(CardWrapper card, MyCardScrollAdapter cardScrollAdapter){
		mCard = CardUtils.getCard(card);
		mCard.setTimestamp("preparing audio");
		mCardScrollAdapter = cardScrollAdapter;
		mCardScrollAdapter.notifyDataSetChanged();
	}
	@Override
	public void onPrepared(MediaPlayer mp) {
		StreamPlayer.startSoundFile();
		mCard.setTimestamp("reading aloud");
		mCardScrollAdapter.notifyDataSetChanged();
	}

}
