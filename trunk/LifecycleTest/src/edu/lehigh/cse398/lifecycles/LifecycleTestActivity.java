package edu.lehigh.cse398.lifecycles;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LifecycleTestActivity extends Activity {
	 private String lastValue = "";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i(LifecycleTestActivity.class.getName(), "onCreate" + (null == savedInstanceState ? " its null" : ""));
        
        if (null != savedInstanceState) {
            lastValue = savedInstanceState.getString("lastValue");
        }
        
        Button b = (Button) findViewById(R.id.button1);
        b.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                EditText e = (EditText) findViewById(R.id.editText1);
                String text = e.getText().toString();
                if (text.equals(lastValue)) {
                    Toast.makeText(LifecycleTestActivity.this, "You told me that already!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(LifecycleTestActivity.this, "I shall remember " + text, Toast.LENGTH_SHORT).show();
                    lastValue = text;
                }
            }
        });  
        
        b = (Button) findViewById(R.id.button2);
        b.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                LifecycleTestActivity.this.startActivity(new Intent(LifecycleTestActivity.this, SecondActivity.class));
            }
        });
    }
    
    /**
     * Notifies that the activity will be started. This is run to resume an app
     * that has been stopped (taken out of foreground). Called before onStart
     */
    @Override
    protected void onRestart()
    {
        super.onRestart();
        Log.i(LifecycleTestActivity.class.getName(), "onRestart");
    }

    /**
     * notify when we are actually starting. This means everything is becoming
     * visible
     */
    @Override
    protected void onStart()
    {
        super.onStart();
        Log.i(LifecycleTestActivity.class.getName(), "onStart");
    }

    /**
     * After starting, there is a point when the views actually are able to
     * interact with the user
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(LifecycleTestActivity.class.getName(), "onResume");
    }

    /**
     * we will stop interacting with the user. If android needs to kill the
     * activity, this is the only thing we can be guaranteed will run. Note that
     * this is expected to run when another activity instance is going to be
     * visible
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        Log.i(LifecycleTestActivity.class.getName(), "onPause" + (isFinishing() ? " Finishing" : " its not finishing"));
    }

    /**
     * Called when the app is no longer visible to the user.
     */
    @Override
    protected void onStop()
    {
        super.onStop();
        Log.i(LifecycleTestActivity.class.getName(), "onStop");
    }

    /**
     * being destroyed... that means this instance of the activity is going
     * away. So be careful about statics!
     *
     * Note too that this just means the instance is set null, so that the
     * instance can be gc'd
     *
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.i(LifecycleTestActivity.class.getName(), "onDestroy: " + Integer.toString(getChangingConfigurations(), 16));
    }

    /**
     * save instance-specific state. Bundle is a map of key/value pairs. Values
     * must be parcelable, which includes any primitive type.
     *
     * Take care: you should have a data model for storing things that can't be
     * lost. The bundle is just for restoring the View aspect of the program.
     *
     * Note too that every view in this activity is auto-saved to the bundle.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	outState.putString("lastValue", lastValue);
        super.onSaveInstanceState(outState);
        Log.i(LifecycleTestActivity.class.getName(), "onSaveInstanceState");
    }

    /**
     * This is pretty gross: after onStop, this might be called (no guarantees),
     * in which case whatever is returned will be kept around. So if you need to
     * stash an object away for later use, this is how to do it. It can be
     * recovered via Activity.getLastNonConfigurationInstance.
     *
     * In this demo, we save the ID of the task, so that we can demonstrate
     * later how when an app is killed and restarted, its ID won't change.
     *
     * The most common reason for this is to save a query result, but a local
     * database is better
     */
    @Override
    public Object onRetainNonConfigurationInstance()
    {
        Log.i(LifecycleTestActivity.class.getName(), "onRetainNonConfigurationInstance");
        //return null;
        return new Integer(getTaskId());
    }

    /**
     * restore state... Bundle shouldn't be null. This is called after onStart
     * and before onPostCreate.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(LifecycleTestActivity.class.getName(), "onRestoreInstanceState"
                + (null == savedInstanceState ? " its null" : ""));
        Object oldTaskObject = getLastNonConfigurationInstance();
        if (null != oldTaskObject) {
            int oldtask = ((Integer) oldTaskObject).intValue();
            int currentTask = getTaskId();
            assert oldtask == currentTask;
            Log.i(LifecycleTestActivity.class.getName(), "Oldtask = " + oldtask + " newtask = " + currentTask);
        }
    }

    /**
     * probably not needed: called after onRestoreInstanceState if we need
     * two-sLifecycleTestActivity.class.getName()e restoration
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        Log.i(LifecycleTestActivity.class.getName(), "onPostCreate" + (null == savedInstanceState ? " its null" : ""));
    }

    /**
     * probably not needed: called after onResume, so the activity is already
     * visible and interacting with the user
     */
    @Override
    protected void onPostResume()
    {
        super.onPostResume();
        Log.i(LifecycleTestActivity.class.getName(), "onPostResume");
    }

    /**
     * probably not needed: this is for when the activity is going to stop due
     * to user actions, like hitting back or home. It is recommended to clear
     * alerts / dialogs here
     */
    @Override
    protected void onUserLeaveHint()
    {
        super.onUserLeaveHint();
        Log.i(LifecycleTestActivity.class.getName(), "onUserLeaveHint");
    }
    
}