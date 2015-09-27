package com.porterjamesf.galacticbattles;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Stars implements Follower {
	public static final float SPEED_RANGE = (float) 3.0;
	public static final float SPEED_MIN = (float) 1.0;
	public static final int COUNT = 30;
	public final float MOVE = (float) 0.075;
	public final float WARP_SPEED = (float) 2.4;
	
	float[] coords;
	float[] adjcoords;
	float[] speeds;
	int width;
	int height;
	Random rand;
	
	Paint starsPaint;
	
	public Stars(int wid, int hei, int col, Paint starPaint) {
		starsPaint = new Paint(starPaint);
		starsPaint.setColor(col);
		width = wid;
		height = hei;
		rand = new Random();
		
		coords = new float[Stars.COUNT*2];
		adjcoords = new float[Stars.COUNT*2];
		speeds = new float[Stars.COUNT];
		
		for (int i = 0; i < Stars.COUNT; i++) {
			coords[2*i] = rand.nextFloat()*width;
			coords[2*i+1] = rand.nextFloat()*height;
			speeds[i] = Stars.SPEED_MIN + rand.nextFloat()*Stars.SPEED_RANGE;
		}
	}
	public void doFrame(float xFollow, float yFollow) {
		//Movement modifiers
		xFollow *= MOVE;
		yFollow *= MOVE;
		
		for (int i = 0; i < Stars.COUNT; i++) {
			coords[2*i] += xFollow;
			coords[2*i+1] += speeds[i]*WARP_SPEED + yFollow;
			if (coords[2*i] >= width) {
				coords[2*i] -= width;
				coords[2*i+1] = rand.nextFloat()*height;
			}
			if (coords[2*i] < 0) {
				coords[2*i] += width;
				coords[2*i+1] = rand.nextFloat()*height;
			}
			if (coords[2*i+1] >= height) {
				coords[2*i+1] -= height;
				coords[2*i] = rand.nextFloat()*width;
			}
		}
	}
	
	public void draw(Canvas c) {
		c.drawPoints(coords, starsPaint);
	}
	public void setStrokeWidth(float strokewidth) {
		if (strokewidth > 0.0) {
			starsPaint.setStrokeWidth(strokewidth);
		}
	}
	public boolean isOutOfFrame() {
		return false;
	}
}
