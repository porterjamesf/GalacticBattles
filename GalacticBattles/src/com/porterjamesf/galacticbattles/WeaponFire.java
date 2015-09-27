package com.porterjamesf.galacticbattles;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class WeaponFire implements Follower {
	public static final float LASER_SPEED = -100;
	public static final float MISSILE_SPEED = -70;
	public static final float DOCTOR_SPEED = -50;
	
	public final int OUT_TOP = -500;
	
	String type;
	Bitmap image;
	int hwidth;
	int hheight;
	Paint firePaint;
	
	float x;
	float y;
	float xVel;
	float yVel;
	
	public WeaponFire(String fireType, Bitmap fireImage, float xPos,
			float yPos) {
		type = fireType;
		image = fireImage;
		hwidth = image.getWidth()/2;
		hheight = image.getHeight()/2;
		firePaint = new Paint();
		
		x = xPos;
		y = yPos;
		xVel = 0;
		switch(type) {
		case "Laser":
			yVel = WeaponFire.LASER_SPEED;
			break;
		case "Missile":
			yVel = WeaponFire.MISSILE_SPEED;
			break;
		case "Doctor":
			yVel = WeaponFire.DOCTOR_SPEED;
			break;
		default:
			yVel = 1;
			break;
		}
	}
	
	public void doFrame(float xFollow, float yFollow) {
		x += xVel + xFollow;
		y += yVel + yFollow;
	}
	
	public void draw(Canvas c) {
		c.drawBitmap(image, x - hwidth, y - hheight, firePaint);
	}
	
	public boolean isOutOfFrame() {
		return y <= OUT_TOP;
	}
}
