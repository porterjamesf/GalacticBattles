package com.porterjamesf.galacticbattles;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class Ship implements SensorEventListener {
	
	public final float MOVE = (float) 30.0;
	public final float FIRE_Y = (float) 0.001;
	public final float FIRE_X = (float) 0.8221;
	
	float xval;
	float yval;
	float zval;
	float xval2;
	float yval2;
	float xdiff;
	float ydiff;
	float xcum;
	float ycum;
	float gyroMinimum = 0.1f;
	boolean use1;
	float xReference;
	float yReference;
	float zReference;
	boolean referenceSet = false;
	boolean gyro;
	boolean rot;
	Bitmap image;
	float xorig;
	float yorig;
	int hwidth;
	int hheight;
	Paint shipPaint;
	boolean fireLeft;
	int fireXLeft;
	int fireXRight;
	int fireY;
	int fireX;
	
	public Ship (BitmapDrawable shipImage, boolean isGyroscope, boolean isRotation) {
		xval = 0;
		yval = 0;
		xval2 = 0;
		yval2 = 0;
		xdiff = 0;
		ydiff = 0;
		xcum = 0;
		ycum = 0;
		use1 = true;
		image = shipImage.getBitmap();
		xorig = 0;
		yorig = 0;
		hwidth = image.getWidth()/2;
		hheight = image.getHeight()/2;
		shipPaint = new Paint();
		gyro = isGyroscope;
		rot = isRotation;
		fireLeft = true;
		fireY = (int) (hheight*FIRE_Y);
		fireX = (int) (hwidth*FIRE_X);
	}
	
	public void drawShip(Canvas c) {
		c.drawBitmap(image, xorig - hwidth, yorig - hheight, shipPaint);
	}
	public void doFrame() {
		if (rot) {
			System.out.println("x: " + String.format("%.3f",xval) + " y: " + String.format("%.3f",yval) + " z: " + String.format("%.3f",zval));
		} else {
			if (gyro) {
				System.out.println("x: " + xcum + " y: " + ycum);
			} else {
				xorig -= Math.atan(xval/3.0)*MOVE;
				yorig += Math.atan(yval/3.0)*MOVE;
			}
		}
	}
	
	public void setCenter(int xpos, int ypos) {
		xorig = xpos;
		yorig = ypos;
	}
	public void move(float deltaX, float deltaY) {
		xorig += deltaX;
		yorig += deltaY;
	}
	
	public Point getFirePoint(String type) {
		fireLeft = !fireLeft;
		if(type.equals("Laser")) {
			return new Point((fireLeft?-1:1)*fireX + (int)xorig, (int)yorig - fireY);
		} else {
			return (new Point((fireLeft?fireXLeft:fireXRight) + (int)xorig, fireY + (int)yorig));
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (gyro) {
			/*if(use1) {
				xval2 = event.values[0];
				yval2 = event.values[1];
				xdiff = xval2 - xval;
				ydiff = yval2 - yval;
			} else {
				xval = event.values[0];
				yval = event.values[1];
				xdiff = xval - xval2;
				ydiff = yval - yval2;
			}
			use1 = !use1;
			xcum += xdiff;
			ycum += ydiff;*/
			if(Math.abs(event.values[0]) > gyroMinimum) {
				xcum += event.values[0];
			}
			if(Math.abs(event.values[1]) > gyroMinimum) {
				ycum += event.values[1];
			}
		} else {
			if (referenceSet) {
				xval = event.values[0] - xReference;
				yval = event.values[1] - yReference;
				zval = event.values[2] - zReference;
			} else {
				
				//xReference = event.values[0];
				//yReference = event.values[1];
				
				xReference = 0;
				yReference = 0;
				zReference = 0;
				referenceSet = true;
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
}
