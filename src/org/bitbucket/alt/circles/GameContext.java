package org.bitbucket.alt.circles;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.widget.TextView;

/**
 * Holds game state.
 */
class GameContext implements CirclesView.InputListener {
    private static final int VIBRATO_MS = 200;

    static final int MAX_NUMBER = 25;

    static final int ERROR = -1,
            NOT_STARTED = 0,
            IN_PROGRESS = 1,
            FINISHED = 2;

    private final Activity activity;
    private final Vibrator vibrator;
    private int gameState;
    private int curVal = 1;
    private long gameTime;
    private int errorCount = 0;


    public GameContext(Activity activity) {
        this.activity = activity;
        gameState = NOT_STARTED;

        vibrator = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Handles number submitted by player. Returns next game state.
     */
    @Override
    public void handleInput(int num) {
        switch (gameState) {
            case NOT_STARTED:
                gameTime = System.currentTimeMillis();
            case ERROR:
            case IN_PROGRESS:
                if(num == curVal) {
                    gameState = IN_PROGRESS;
                    if(curVal == MAX_NUMBER) {
                        gameState = FINISHED;
                        gameTime = System.currentTimeMillis() - gameTime;
                    }
                    curVal++;
                } else {
                    gameState = ERROR;
                    errorCount++;
                    if(vibrator != null)
                        vibrator.vibrate(VIBRATO_MS);
                }

                displayStatus();
                break;
            case FINISHED:
                break;
        }
    }

    private void displayStatus() {
        TextView view = (TextView) activity.findViewById(R.id.status);
        switch (gameState) {
            case IN_PROGRESS:
                view.setTextColor(Color.WHITE);
                view.setText("Next: " + String.valueOf(curVal));
                break;
            case ERROR:
                view.setTextColor(Color.RED);
                break;
            case FINISHED:
                view.setTextColor(Color.WHITE);
                view.setText(String.format("%d seconds, %s error%c",
                        gameTime/1000,
                        (errorCount == 0) ? "no" : String.valueOf(errorCount),
                        (errorCount == 1) ? ' ' : 's'));
                break;
        }
    }


}
