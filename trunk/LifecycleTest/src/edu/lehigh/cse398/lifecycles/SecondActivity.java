package edu.lehigh.cse398.lifecycles;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SecondActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.second);
		Button b = (Button) findViewById(R.id.button3);
        b.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                SecondActivity.this.finish();
            }
        });
	}
}
