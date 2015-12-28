package edu.lehigh.cse.dan.wyatt.factoryrunner;

import android.util.Log;

public class DebugSwipeListener extends NoopSwipeListener {
	public DebugSwipeListener() {
		super();
		Log.i("SWIPE", "DebugSwipeListener created");
	}
	
	public void onSwipeUp() {
		Log.i("SWIPE", "Swipe Up");
	}

	public void onSwipeDown() {
		Log.i("SWIPE", "Swipe Down");
	}
	
	public void onSwipeLeft() {
		Log.i("SWIPE", "Swipe Left");
	}

	public void onSwipeRight() {
		Log.i("SWIPE", "Swipe Right");
	}
}
