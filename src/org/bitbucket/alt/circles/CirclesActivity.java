package org.bitbucket.alt.circles;

import android.app.Activity;
import android.os.Bundle;

public class CirclesActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(new CirclesView(this));
    }
}
