package org.bitbucket.alt.circles;

import android.app.Activity;
import android.os.Bundle;

public class CirclesActivity extends Activity
{

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        GameContext gameContext = new GameContext(this);
        CirclesView circlesView = (CirclesView)findViewById(R.id.circles);
        circlesView.setInputListener(gameContext);
    }

}
