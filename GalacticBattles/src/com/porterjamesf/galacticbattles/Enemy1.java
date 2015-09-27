package com.porterjamesf.galacticbattles;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Enemy1 implements Follower {
	float xorig;
	float yorig;
	int hwidth;
	int hheight;
	Bitmap image;
	Paint shipPaint;
	
	public Enemy1(Bitmap shipImage, float xpos, float ypos) {
		xorig = xpos;
		yorig = ypos;
		image = shipImage;
		hwidth = shipImage.getWidth()/2;
		hheight = shipImage.getHeight()/2;
		shipPaint = new Paint();
	}
	
	public void doFrame(float xFollow, float yFollow) {
		xorig += xFollow;
		yorig += yFollow;
	}

	@Override
	public void draw(Canvas c) {
		c.drawBitmap(image, xorig - hwidth, yorig - hheight, shipPaint);
	}

	@Override
	public boolean isOutOfFrame() {
		return false;
	}
}
