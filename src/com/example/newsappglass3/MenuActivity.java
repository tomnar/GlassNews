package com.example.newsappglass3;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

/**
 * Activity showing the options menu.
 */
public class MenuActivity extends Activity {
  
	private Intent mIntent;
	private boolean mEventMode;
	
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
       
       
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.score_card_menu, menu);     
     
        return true;
    }
    
    @Override
    public boolean onPreparePanel(int featureId, android.view.View view, Menu menu) {
    	Intent intent = getIntent();
     	mEventMode =  intent.getBooleanExtra("eventmode", false);
    	if(featureId == Window.FEATURE_OPTIONS_PANEL){
			menu.removeItem(R.id.matchscroll_item);
			menu.removeItem(R.id.stop_item);
			menu.removeItem(R.id.close_event_item);
			getMenuInflater().inflate(R.menu.score_card_menu, menu);
			boolean eventMode = intent.getBooleanExtra("eventmode", false);
			if(eventMode){
				menu.removeItem(R.id.matchscroll_item);
				menu.removeItem(R.id.stop_item);
			}else{
				menu.removeItem(R.id.close_event_item);
			}
		}
    	return super.onPreparePanel(featureId, view, menu);
    };
    
    

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
        	case R.id.matchscroll_item: 
                Intent intent = getIntent();
                Bundle bundle = intent.getBundleExtra("matches");
                 //ArrayList<Event> mEvents = (ArrayList<Event>)bundle.get("events");
                Intent intent2 = new Intent(this, MatchScrollLiveCardActivity.class);
                intent2.putExtra("matches", bundle);
                startActivity(intent2);
                break;
            case R.id.stop_item:
                stopService(new Intent(this, ScoreCard.class));
                break;
            case R.id.close_event_item:
                //do SOMETHING here that set eventmode = false;
            	//set intent with eventmode = false;
            	Intent intent3 = new Intent(this, ScoreCard.class);
                startService(intent3);
            	break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        // Nothing else to do, closing the activity.
    	Debugger.log("menu closed");
        finish();
    }
}
