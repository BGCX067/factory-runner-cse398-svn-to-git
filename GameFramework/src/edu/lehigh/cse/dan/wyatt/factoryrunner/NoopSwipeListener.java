package edu.lehigh.cse.dan.wyatt.factoryrunner;

/**
 * A NoopSwipeListener receives swipe events and does nothing.  This is useful when you just care
 * about swipes in some directions because you can override the method for the direction you care
 * about.
 * 
 * @author Daniel Finnie and Wyatt Pierson
 *
 */
public class NoopSwipeListener implements SwipeListener {
	public void onSwipeUp() {

	}

	public void onSwipeDown() {

	}
	
	public void onSwipeLeft() {
		
	}
	
	public void onSwipeRight() {
		
	}
}
