package org.bitbucket.alt.circles;

import android.content.Context;
import android.graphics.*;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.*;

public class CirclesView implements SurfaceHolder.Callback, View.OnTouchListener {
    private static final String TAG = CirclesView.class.getSimpleName();
    private static final Typeface[] typefaces = { Typeface.SANS_SERIF, Typeface.SERIF};
    private static final int[] colors = { Color.RED, 0xff00dd00, Color.BLUE};
    private static final Random rand = new Random();
    private static final int
            MAX_CYCLES = 2000,
            MAX_RADIUS = 50,
            MIN_RADIUS = 3;

    private Paint circleBg, circleStroke, selectedStroke;

    private int width, height;
    private ArrayList<Circle> circles;
    private MotionEvent event;
    private InputListener listener;
    private SurfaceHolder holder;


    public CirclesView() {
        circleBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleBg.setColor(Color.WHITE);
        circleBg.setStyle(Paint.Style.FILL);

        circleStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleStroke.setStyle(Paint.Style.STROKE);
        circleStroke.setStrokeWidth(0);
        circleStroke.setColor(Color.BLACK);

        selectedStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedStroke.setStyle(Paint.Style.STROKE);
        selectedStroke.setStrokeWidth(6.0f);

        circles = new ArrayList<Circle>();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if(listener != null) {
                    for(Circle c : circles) {
                        if(c.contains(event.getX(), event.getY())) {
                            listener.handleInput(c.val);
                            break;
                        }
                    }
                }
            case MotionEvent.ACTION_UP:
                this.event = event;
                draw();

        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.width = width;
        this.height = height;
        this.holder = holder;

        if(circles.isEmpty())
            genCircles();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    interface InputListener {
        void handleInput(int num);
    }

    public void draw() {
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(0xffeeeeee);
        drawCircles(canvas);
        holder.unlockCanvasAndPost(canvas);
    }

    void setInputListener(InputListener listener) {
        this.listener = listener;
    }

    void reset() {
        circles.clear();
    }

    class Circle {
        private final int centerX, centerY, radius;

        private TextPaint numPaint;
        private int textHeight;
        private int val;

        Circle(int centerX, int centerY, int radius) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
            val = -1;
        }

        public void setNumber(int val) {
            this.val = val;

            numPaint = randomTextPaint();
            numPaint.setTextSize(radius * ((val < 10) ? 1.6f : 1.1f));

            // pre-calculate text height
            Rect r = new Rect();
            String num = String.valueOf(val);
            numPaint.getTextBounds(num, 0, num.length(), r);
            textHeight = r.height();
        }

        int phi(double x, double y) {
            double a = centerX - x, b = centerY - y;
            double d = Math.sqrt(a * a + b * b);
            return (int)(d - radius);
        }

        boolean contains(double x, double y) {
            return phi(x, y) < 0;
        }

        @Override
        public String toString() {
            return String.format("Circle [%d, %d]", centerX, centerY);
        }

        public void draw(Canvas canvas, boolean selected) {
            canvas.drawCircle(centerX, centerY, radius, circleBg);
            Paint stroke = selected ? selectedStroke : circleStroke;
            stroke.setColor(hasValue() ? numPaint.getColor() : Color.BLACK);
            canvas.drawCircle(centerX, centerY, radius, stroke);
            if(hasValue())  {
                String num = String.valueOf(val);
                canvas.drawText(num, centerX, centerY+textHeight/2, numPaint);
            }
        }

        public boolean hasValue() {
            return val > 0;
        }
    }


    private void genCircles() {
        int x, y, minPhi, phi, numCycles = MAX_CYCLES;
        do {
            minPhi = MAX_RADIUS;
            x = rand.nextInt(width);
            y = rand.nextInt(height);

            phi = Math.min(Math.min(x, width - x), Math.min(y, height - y));
            if(phi < minPhi)
                minPhi = phi;

            for(Circle c: circles) {
                phi = c.phi(x, y);
                if(phi < minPhi)
                    minPhi = phi;
                if(phi < 0)
                    break;
            }

            if(minPhi > MIN_RADIUS)
                circles.add(new Circle(x, y, minPhi));

        } while(--numCycles > 0);

        numberCircles();
    }

    private void drawCircles(Canvas canvas) {
        boolean selected;
        for(Circle c : circles) {
            selected = event != null
                    && event.getAction() == MotionEvent.ACTION_DOWN
                    && c.contains(event.getX(), event.getY())
                    && c.hasValue();
            c.draw(canvas, selected);
        }
    }

    /**
     * Generate text style with color randomly selected from array of predefined colors and randomly chosen typeface,
     * either serif or sans-serif
     */
    private static TextPaint randomTextPaint() {
        TextPaint tp = new TextPaint();
        tp.setFakeBoldText(true);
        tp.setSubpixelText(true);
        tp.setAntiAlias(true);
        tp.setColor(colors[rand.nextInt(colors.length)]);
        tp.setTypeface(typefaces[rand.nextInt(typefaces.length)]);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setTextScaleX(1.0f + rand.nextFloat() * 0.4f);
        return tp;
    }

    /**
     * Assign numbers to circles. Current implementation selects MAX_NUMBER biggest circles, than randomly assigns numbers to it.
     */
    private void numberCircles() {
        Collections.sort(circles, new Comparator<Circle>() {
            @Override
            public int compare(Circle c0, Circle c1) {
                return c1.radius - c0.radius;
            }
        });
        List<Circle> subList = circles.subList(0, GameContext.MAX_NUMBER);
        Collections.shuffle(subList);
        int val = 1;
        for(Circle c : subList)
            c.setNumber(val++);
    }

}
