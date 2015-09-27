package com.porterjamesf.galacticbattles;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.FrameLayout;


public class GameActivity extends Activity implements SurfaceHolder.Callback {
	
	public final int FRAMERATE = 30;
	public final int LASER_DELAY = 3;
	public final float FOLLOW = (float) 0.7;
	public final float HORIZ_FOLLOW = (float) 0.09;
	public final float HORIZONTAL_ZONECHANGE = (float) .2;
	public final float VERTICAL_ZONECHANGE = (float) .3;
	
	private int mShowFlags;
	private int mHideFlags;
	
	private boolean systemUIIsVisible = true;
	private View contentView;
	private View controlsView;
	
	private SensorManager sensorManager;
	private Sensor accel;
	private boolean hasSensor;
	
	private Timer timer;
	private SurfaceHolder holder;
	
	private Paint starPaint;
	
	int centerX = 0;
	int centerY = 0;
	int screenWidth;
	int screenHeight;
	float zoneChangeLeft;
	float zoneChangeRight;
	float zoneChangeTop;
	float zoneChangeBottom;
	
	private Ship ship;
	private Stars[] stars;
	private ArrayList<Enemy1> enemies;
	Bitmap laserBitmap;
	Bitmap missileBitmap;
	Bitmap doctorBitmap;
	Bitmap enemy1Bitmap;
	
	GradientDrawable laserFire;
	GradientDrawable missileFire;
	GradientDrawable doctorFire;
	Rect laserFireRect;
	Rect missileFireRect;
	Rect doctorFireRect;
	private boolean hasMissiles;
	private boolean hasDoctor;
	private boolean firingLaser;
	int laserFireDelay;
	ArrayList<Follower> followers;

	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_game);

		controlsView = findViewById(R.id.fullscreen_content_controls);
		contentView = findViewById(R.id.fullscreen_content);
		contentView.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(!systemUIIsVisible) {
					screenTouch(event);
				}
				return true;
			}
		});
		
		if (Build.VERSION.SDK_INT >= 16) {
			mShowFlags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
			mHideFlags = View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_IMMERSIVE
					| mShowFlags;
		}

		contentView.setOnSystemUiVisibilityChangeListener(
				new View.OnSystemUiVisibilityChangeListener() {
			
			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
					// Ui visible
					systemUIIsVisible = true;
					controlsView.setVisibility(View.VISIBLE);
					stopThePress();
				} else {
					// Fullscreen
					systemUIIsVisible = false;
					controlsView.setVisibility(View.GONE);
					startThePress();
				}				
			}
		});
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accel = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		boolean isRotationVector = hasSensor = accel != null;
		boolean isGyroscope = false;
		if (!hasSensor) {
			System.out.println("couldn't get rotation vector");
			accel = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			isGyroscope = hasSensor = accel != null;
			if (!hasSensor) {
				System.out.println("couldn't get gyroscope");
				accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				hasSensor = accel != null;
			}
		}
		
		laserFire = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.laser_fire);
		missileFire = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.missile_fire);
		doctorFire = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.doctor_fire);
		followers = new ArrayList<Follower>();
		
		SurfaceView surface = new SurfaceView(this,null);
		FrameLayout gameFrame = (FrameLayout) contentView;
		gameFrame.addView(surface);
		surface.getHolder().addCallback(this);
		
		starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		starPaint.setStrokeCap(Paint.Cap.ROUND);
		starPaint.setStrokeWidth((float) 2);
		
		ship = new Ship((BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.ship), isGyroscope, isRotationVector);
		laserBitmap = ((BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.laser)).getBitmap();
		//missileBitmap = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.missile);
		//doctorBitmap = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.doctor);
		enemy1Bitmap = ((BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.enemy1)).getBitmap();
		
		enemies = new ArrayList<Enemy1>();
		enemies.add(new Enemy1(enemy1Bitmap, ship.xorig, ship.yorig - 500));
		
		stars = new Stars[3];
		stars[0] = new Stars(1500,1500,0xFFFFCFC0, starPaint);
		stars[1] = new Stars(1500,1500,0xFFF2F0F4, starPaint);
		stars[2] = new Stars(1500,1500,0xFFD0C0FF, starPaint);
		for (Stars star : stars) {
			followers.add(star);
		}
		followers.add(enemies.get(0));
		
		hasMissiles = false;
		hasDoctor = false;
		firingLaser = false;
		
		//Testing
		hasMissiles = hasDoctor = true;
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    stopThePress();
	}
	
	private void stopThePress () {
		timer.cancel(); //TODO: fix activity management problem here
	    if(hasSensor) sensorManager.unregisterListener(ship, accel);
	    ship.referenceSet = false;
	}
	
	private void startThePress() {
		timer = new Timer();
		timer.schedule(new GameTimerTask(),0,FRAMERATE);
	    if(hasSensor) sensorManager.registerListener(ship, accel, SensorManager.SENSOR_DELAY_GAME);
	}
	
	public void setSystemVisibility(boolean isVisible) {
		if (isVisible && !systemUIIsVisible) {
			if (Build.VERSION.SDK_INT >= 16) {
				contentView.setSystemUiVisibility(mShowFlags);
			} else {
				getWindow().setFlags(
						WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			}
		} else {
			if (!isVisible && systemUIIsVisible) {
				if (Build.VERSION.SDK_INT >= 16) {
					contentView.setSystemUiVisibility(mHideFlags);
				} else {
					getWindow().setFlags(
							WindowManager.LayoutParams.FLAG_FULLSCREEN,
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}
			}
		}
	}
	
	public void send_message(View view) {
		/*if(view.equals(findViewById(R.id.ui_button))) {
			setSystemVisibility(true);
			return;
		}*/
		if(view.equals(findViewById(R.id.dummy_button))) {
			setSystemVisibility(false);
			return;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder callbackHolder) {
		holder = callbackHolder;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		screenWidth = width;
		screenHeight = height;
		centerX = screenWidth/2;
		centerY = screenHeight * 80 / 100;
		zoneChangeLeft = screenWidth*HORIZONTAL_ZONECHANGE;
		zoneChangeRight = screenWidth - zoneChangeLeft;
		zoneChangeTop = screenHeight*VERTICAL_ZONECHANGE;
		zoneChangeBottom = screenHeight - zoneChangeTop;
		calculateFireButtons();
		ship.setCenter(centerX, centerY);
		float strokewidth = (screenWidth+screenHeight)/500;
		for(Stars star : stars) {
			star.setStrokeWidth(strokewidth);
		}
	}
	private void calculateFireButtons() {
		float radius = zoneChangeTop*7/20;
		float hwidth = screenWidth*5/6;
		float hheight = (screenHeight+zoneChangeBottom)/2;

		laserFire.setGradientRadius(radius);
		missileFire.setGradientRadius(radius);
		doctorFire.setGradientRadius(radius);
		
		laserFireRect = new Rect((int)(hwidth - radius),
				(int)(hheight - radius), (int)(hwidth + radius),
				(int)(hheight + radius));
		laserFire.setBounds(laserFireRect);
		
		hwidth = screenWidth/2;
		missileFireRect = new Rect((int)(hwidth - radius),
				(int)(hheight - radius), (int)(hwidth + radius),
				(int)(hheight + radius));
		missileFire.setBounds(missileFireRect);
	
		hwidth = screenWidth/6;
		doctorFireRect = new Rect((int)(hwidth - radius),
				(int)(hheight - radius), (int)(hwidth + radius),
				(int)(hheight + radius));
		doctorFire.setBounds(doctorFireRect);
		
	}
	
	private void screenTouch(MotionEvent event) {
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (laserFireRect.contains((int) event.getX(), (int) event.getY())) {
					firingLaser = true;
				}
				if (hasMissiles && missileFireRect.contains((int) event.getX(), (int) event.getY())) {
					System.out.println("firing missile");
				}
				if (hasDoctor && doctorFireRect.contains((int) event.getX(), (int) event.getY())) {
					System.out.println("firing doctor");
				}
				break;
			case MotionEvent.ACTION_UP:
				if (firingLaser) {
					firingLaser = false;
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if(timer != null) timer.cancel();
		System.out.println("destroy");		
	}
	
	private void fireLaser () {
		Point fireOrig = ship.getFirePoint("Laser");
		
		followers.add(new WeaponFire("Laser", laserBitmap, fireOrig.x, fireOrig.y));
	}
	
	private class GameTimerTask extends TimerTask {
		float xFollow;
		float yFollow;
		
		@Override
		public void run() {
			//Game timing
			if(laserFireDelay > 0) {
				laserFireDelay--;
			} else {
				if (firingLaser) {
					laserFireDelay = LASER_DELAY;
					fireLaser();
				}
			}
			
			//Drawing
			Canvas c = holder.lockCanvas();
			if (c != null) {
				ship.doFrame();
				calculateFollow();
				ship.move(xFollow, yFollow);
				//Use doFrame interface
				for(int i = 0; i < followers.size(); i++) {
					Follower follower = followers.get(i);
					follower.doFrame(xFollow, yFollow);
					if(follower.isOutOfFrame()) {
						followers.remove(i);
						i--;
					}
				}
				/*for(Follower follower : followers) {
					follower.doFrame(xFollow, yFollow);
					if(follower.isOutOfFrame()) {
						followers.remove(follower);
					}
				}*/
								
				c.drawRGB(0, 0, 0);
				for (Follower follower : followers) {
					follower.draw(c);
				}
				ship.drawShip(c);
				
				laserFire.draw(c);
				if (hasMissiles) missileFire.draw(c);
				if (hasDoctor) doctorFire.draw(c);
				
				holder.unlockCanvasAndPost(c);
			}
		}
		private void calculateFollow() {
			xFollow = (float) (Math.pow((centerX - ship.xorig),3)/10000);
			float ypos = ship.yorig;
			if (ypos < zoneChangeTop) {
				yFollow = (zoneChangeTop - ypos);
			} else {
				if (ypos > zoneChangeBottom) {
					yFollow = (zoneChangeBottom - ypos)*FOLLOW;
				} else {
					yFollow = 0;
				}
			}
		}
	}
}
