package com.bombhead.spaceinvaders2;

import android.graphics.Canvas;
import android.graphics.Paint;

public class RenderableText {
	
	private String 	text;
	private float 	screenPositionX	 	 = 0;
	private float 	screenPositionY	 	 = 0;
	private int 	screenWidth			 = 0;
	private int 	screenHeight 		 = 0;
	private float 	x					 = 0;
	private float 	y					 = 0;
	private float 	textMaxLength		 = 0;
	private float	 textMaxHeight		 = 0;
	private int   	textsize 			 = 0;
	
	private Paint p;
	
	
	RenderableText()
	{
		text = "";
		p = new Paint();
		p.setARGB(255,255,0,0);
		p.setTextSize(0.0f);
		p.setFlags(Paint.ANTI_ALIAS_FLAG);
	}
	
	RenderableText(String text)
	{
		this();
		this.text = text;
	}
	
	void render(Canvas canvas)
	{
		canvas.drawText(text, x, y, p);
	}
	
	void setScreenPosition(float x, float y)
	{
		screenPositionX = x;
		screenPositionY = y;
		calc();
	}
	
	void scaleToScreen(int width, int height)
	{
		screenWidth 	= width;
		screenHeight 	= height;
		calc();
	}
	
	private void calc()
	{
		x = (screenPositionX * screenWidth);
		y = (screenPositionY * screenHeight);
		textsize = determineMaxTextSize(text,(textMaxLength * screenWidth),(textMaxHeight * screenHeight));
		p.setTextSize(textsize);
	}
	
	private int determineMaxTextSize(String str, float maxWidth, float maxHeight)
	{
		if (str == null || str.trim().length() == 0) return 0;
		int size = 0;
		float foo = 0;
	    Paint paint = new Paint();

	    do {
	        paint.setTextSize(++ size);
	    } while(paint.measureText(str) < maxWidth);

	    
	    // recompute for height
	    int size1 = size;
	    size = 0;
	    do {
	    	paint.setTextSize(++ size);
	    	foo = -paint.ascent();
	    } while(-paint.ascent() < maxHeight);
	    System.out.println(foo);
	    
	    	// return the smallest fitting the bounds
	    if (size < size1)return size;
	    return size1;
	}
	
	void setTextMax(float textMaxLength, float textMaxHeight)
	{
		this.textMaxLength = textMaxLength;
		this.textMaxHeight = textMaxHeight;
	}
	
	public void setARGB(int a, int r, int g, int b)
	{
		p.setARGB(a,r,g,b);
	}
	
	public void setText(String text)
	{
		this.text = text;
		calc();
	}
	
	

}
