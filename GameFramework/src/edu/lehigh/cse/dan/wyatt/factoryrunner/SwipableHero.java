package edu.lehigh.cse.dan.wyatt.factoryrunner;
/*package edu.lehigh.cse.paclab.gameframework;

import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

import android.util.Log;

public class SwipableHero extends Hero {

	private SwipableHero() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean onAreaTouched(TouchEvent e, float x, float y) {
		if(e.getAction() == TouchEvent.ACTION_MOVE) {
    		float dx = e.getX() - x;
    		float dy = e.getY() - y;
    		
    		Log.i("swipe", "swipe dx:" + dx + ", dy: " + dy);
    		
    		if(dy > swipeDelta) {
    			onSwipeUp();
    		} else if(dy < -swipeDelta) {
    			onSwipeDown();
    		}
    		
    		if(dx > swipeDelta) {
    			onSwipeRight();
    		} else if(dx < -swipeDelta) {
    			onSwipeLeft();
    		}
    		
    		return true;
    	} else { // TODO: call super all the time?
    		return super.onAreaTouched(e, x, y);
    	}
	}

}
*/