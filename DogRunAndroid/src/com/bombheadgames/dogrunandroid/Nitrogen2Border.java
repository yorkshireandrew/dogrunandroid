package com.bombheadgames.dogrunandroid;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.bombheadgames.nitrogen2.NitrogenContext;

public class Nitrogen2Border {
	
	float aspectRatioTolerance = 0.1f;
	float targetAspectRatio;
	float aspectRatioMax;
	float aspectRatioMin;
	NitrogenContext nitrogenContext = null;
	private float lightingAmbient;
	Bitmap bitmap = null;
	
	int screenWidth = 1;
	int screenHeight = 1;
	
	int nitrogenWidth = 1;
	int nitrogenHeight = 1;
	float nitrogenOffset = 0;
	
	float xClip = 1;
	float yClip = 1;
	float nearClip = 0.05f;
	float farClip = 50000f;
	
	
	/** class that sizes a nitrogen2 class to the available screen */
	Nitrogen2Border(float targetAspectRatio, float xClip, float yClip, float nearClip, float farClip)
	{
		this.targetAspectRatio = targetAspectRatio;
		this.aspectRatioMax = (1 + aspectRatioTolerance)  * targetAspectRatio;
		this.aspectRatioMin = (1 - aspectRatioTolerance)  * targetAspectRatio;
		this.xClip = xClip;
		this.yClip = yClip;
		this.nearClip = nearClip;
		this.farClip = farClip;		
	}
	
	void scaleToScreen(int width, int height)
	{
		System.out.println("scw:" +width + " sch:" + height);
		screenWidth = width;
		screenHeight = height;
		float screenAspectRatio = ((float)height)/((float)width);
		
		if(screenAspectRatio > this.aspectRatioMax)
		{
			System.out.println("Aspect ratio above max");
			nitrogenWidth 	= screenWidth;
			nitrogenHeight 	= (int)(this.aspectRatioMax * (float)width);
			nitrogenOffset 	= 0;	
		}
		else if (screenAspectRatio >= this.aspectRatioMin)
		{
			System.out.println("Aspect ratio above min");
			nitrogenWidth 	= screenWidth;
			nitrogenHeight 	= screenHeight;
			nitrogenOffset 	= 0;	
		}
		else
		{
			System.out.println("Aspect ratio below min");
			nitrogenWidth = (int)(((float)height) / this.aspectRatioMin);
			nitrogenHeight 	= screenHeight;
			nitrogenOffset = (screenWidth - nitrogenWidth) / 2;
		}
		
		yClip = xClip * ((float)nitrogenHeight)/((float)nitrogenWidth);
		bitmap = Bitmap.createBitmap(nitrogenWidth, nitrogenHeight, Bitmap.Config.ARGB_8888);
		for(int x = 0; x < nitrogenWidth;x++)
		{
			for(int y = 0; y < nitrogenHeight; y++)bitmap.setPixel(x, y, 0xFFFFFFFF);
		}
		nitrogenContext = new NitrogenContext(nitrogenWidth, nitrogenHeight, xClip, yClip, nearClip, farClip);
		System.out.println("nw:" +nitrogenWidth + " nh:" + nitrogenHeight + " xc:" + xClip + " yc:" + yClip);
        nitrogenContext.contentGeneratorForcesNoPerspective = false;
        NitrogenContext.lightingAmbient = 0.4f;
	}
	
	public NitrogenContext getNitrogenContext()
	{
		return nitrogenContext;
	}
	
    /** Draws stuff to the provided Canvas. */
    public void doDraw(Canvas canvas) {
  //  	int max = 0;
  //  	for(int x = 0; x < (this.nitrogenWidth * this.nitrogenHeight); x++)
  //  	{
  //  		if(this.nitrogenContext.pix[x] > max)max = this.nitrogenContext.pix[x];
  //  	}
  //  	System.out.println("max:" + max);
    	bitmap.setPixels(this.nitrogenContext.pix, 0, this.nitrogenWidth, 0, 0, this.nitrogenWidth, this.nitrogenHeight);
    	canvas.drawBitmap(bitmap, nitrogenOffset, 0, null);
    }
    
    

}
