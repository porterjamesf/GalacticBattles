package com.porterjamesf.galacticbattles;

import android.graphics.Canvas;

public interface Follower {
	public void doFrame(float xFollow, float yFollow);
	public void draw(Canvas c);
	public boolean isOutOfFrame();
}
