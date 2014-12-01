package org.bitbucket.alt.circles;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Vibrator;
import android.widget.TextView;

/**
 * Holds game state.
 */
class GameContext implements CirclesView.InputListener {
    private static final int VIBRATO_MS = 200;
    private static final String BEST_TIME = "BEST_TIME";

    static final int MAX_NUMBER = 25;

    static final int ERROR = -1,
            NOT_STARTED = 0,
            IN_PROGRESS = 1,
            FINISHED = 2;

    private final Activity activity;
    private final Vibrator vibrator;
    private int gameState = NOT_STARTED;
    private int curVal = 1;
    private long gameTime;
    private int errorCount = 0;


    public GameContext(Activity activity) {
        this.activity = activity;
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
                break;
            case FINISHED:
                gameState = NOT_STARTED;
                curVal = 1;
                errorCount = 0;
                CirclesView gameView = (CirclesView)activity.findViewById(R.id.circles);
                gameView.reset();
                gameView.invalidate();
                break;
        }
        displayStatus();
    }

    private void displayStatus() {
        TextView view = (TextView) activity.findViewById(R.id.status);
        switch (gameState) {
            case NOT_STARTED:
                view.setText(R.string.start_message);
            case IN_PROGRESS:
                view.setTextColor(Color.WHITE);
                view.setText("Next: " + String.valueOf(curVal));
                break;
            case ERROR:
                view.setTextColor(Color.RED);
                break;
            case FINISHED:
                int elapsed =  (int)gameTime/1000;
                long best = getPreviousBest();
                String status;

                if(elapsed < best) {
                    storeBest(elapsed);
                    status = String.format("%d seconds",  elapsed);
                } else {
                    status = String.format("%d sec (best: %d sec)", elapsed, best);
                }

                if(errorCount > 0)
                    status += String.format(", %d %s", errorCount, (errorCount == 1) ? "error" : "errors");
                view.setTextColor(Color.WHITE);
                view.setText(status);

                break;
        }
    }

    private int getPreviousBest() {
        return activity.getPreferences(Context.MODE_PRIVATE)
                .getInt(BEST_TIME, Integer.MAX_VALUE);
    }

    private void storeBest(int best) {
        activity.getPreferences(Context.MODE_PRIVATE)
                .edit()
                .putInt(BEST_TIME, best)
                .apply();
    }


}
