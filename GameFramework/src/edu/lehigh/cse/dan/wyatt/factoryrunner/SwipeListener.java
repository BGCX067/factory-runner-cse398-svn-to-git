package edu.lehigh.cse.dan.wyatt.factoryrunner;

/**
 * SwipeListener is an interface that objects that wish to receive swipe events should implement.  It has
 * methods for swiping in each of the directions up, down, left, and right.  These directions are taken from
 * the landscape point of view and the behavior is undefined if the device is in portrait mode.
 */
public interface SwipeListener {
	void onSwipeUp();
	void onSwipeDown();
	void onSwipeLeft();
	void onSwipeRight();
}
