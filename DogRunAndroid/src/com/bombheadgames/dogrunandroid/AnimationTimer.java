package com.bombheadgames.dogrunandroid;

import java.util.ArrayList;
import java.util.List;
import java.lang.System;

public class AnimationTimer {
	
	long targetInterval = 0;
	long lastTime = -1;
	long startDelay = 0;
	List<AnimationTimerListener> listeners;
	boolean pause = false;

	AnimationTimer(long targetInterval, long startDelay)
	{
		this.targetInterval = targetInterval;
		this.startDelay = startDelay;
		listeners = new ArrayList<AnimationTimerListener>();
	}
	
	void process()
	{
		if(pause){doPause();return;}
		long now = System.currentTimeMillis();
		if (lastTime > 0)
		{
			long delta = now - lastTime;
			if(startDelay > 0)
			{
				startDelay -= delta;
				doPause();
			}
			else
			{
				for(AnimationTimerListener listener : listeners)
				{
					listener.animationTimerNotify(delta);
				}				
			}
		}
		else
		{
			lastTime = now;
		}
	}
	
	public void pause()
	{
		pause = true;
		lastTime = -1;	
	}
	
	public void resume()
	{
		pause = false;
	}
	
	private void doPause()
	{
		for(AnimationTimerListener listener : listeners)
		{
			listener.animationTimerNotify(0);
		}		
	}
	
	void pad()
	{
		long now = System.currentTimeMillis();
		long delta = now - lastTime;
		long target = this.targetInterval;
		if (delta < target)
		{
			try {
				Thread.sleep(target - delta);
			} catch (InterruptedException e) {}
		}
	}
	
	void addListener(AnimationTimerListener listener)
	{
		listeners.add(listener);
	}
	
	void removeListener(AnimationTimerListener listener)
	{
		listeners.remove(listener);
	}
}
