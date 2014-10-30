package com.bombheadgames.nitrogen2;

import java.io.Serializable;

public class Renderer_LitAffineTexture implements Renderer, Serializable{
private static final long serialVersionUID = 1L;
private static final int SHIFT = 20;
private static final int ZSHIFT = 20;
private static final int TEXTURE_SHIFT = 20; // align with Nitrogen2Vertex

private static final int LIGHT_SHIFT = 10;
private static final int LIGHT_NUM = 1 << LIGHT_SHIFT;
private static final int ALPHA = 0xFF000000;

public void render(
		
		final NitrogenContext context,	
		
		Nitrogen2Vertex leftN2V,
		Nitrogen2Vertex leftDestN2V,
		
		Nitrogen2Vertex rightN2V,
		Nitrogen2Vertex rightDestN2V,
		Nitrogen2Vertex stopN2V,
		
		
		final int[] polyData,
		final TexMap textureMap,
		final float lightingValue	
		)
{
	
	// **************** initialise colour ********************************************
	final int lightVal = (int)(lightingValue * LIGHT_NUM);

	// **************** initialise nitrogen context references ***********************
	final int[] contextPixels = context.pix;
	final int[] contextZBuffer = context.zbuff;
	final int contextWidth = context.w;
	final int[] tex;
	final int textureWidth;
	
	if(textureMap !=  null)
	{
		tex = textureMap.tex;
		textureWidth = textureMap.w;
	}
	else{return;}
	
	
	// **************** Initialise Left start position and calculate delta ***********
	int 	bigLeftSX 		= leftN2V.intSX << SHIFT;
	int 	leftSY			= leftN2V.intSY;
	int 	leftSZ			= leftN2V.intSZ;
	long 	bigLeftSZ		= ((long)leftSZ) << ZSHIFT;
	
	int		leftTX			= leftN2V.intTX;
	int		leftTY			= leftN2V.intTY;

	int 	bigLeftDestSX 	= leftDestN2V.intSX << SHIFT;
	int 	leftDestSY 		= leftDestN2V.intSY;
	long 	bigLeftDestSZ	= ((long)leftDestN2V.intSZ) << ZSHIFT;

	int		leftDestTX		= leftDestN2V.intTX;
	int		leftDestTY		= leftDestN2V.intTY;
	
	int 	leftDeltaSX; 
	int 	leftDeltaSY;
	long 	leftDeltaSZ;
	
	int		leftDeltaTX;
	int 	leftDeltaTY;
	
	leftDeltaSY = leftDestSY - leftSY;
	
	if(leftDeltaSY > 0)
	{
			leftDeltaSX = (bigLeftDestSX - bigLeftSX)/leftDeltaSY;
			leftDeltaSY = (leftDestSY - leftSY);	// down counter
			leftDeltaSZ = (bigLeftDestSZ - bigLeftSZ)/leftDeltaSY;
			
			leftDeltaTX = (leftDestTX - leftTX) / leftDeltaSY;
			leftDeltaTY = (leftDestTY - leftTY) / leftDeltaSY;
	}
	else
	{
			leftDeltaSX = 0;
			leftDeltaSY = 0;
			leftDeltaSZ = 0;
			
			leftDeltaTX = 0;
			leftDeltaTY = 0;		
	}
	
	// **************** Initialise Right start position and calculate delta ***********
	int 	bigRightSX 		= rightN2V.intSX << SHIFT;
	int 	rightSY			= rightN2V.intSY;
	int 	rightSZ			= rightN2V.intSZ;
	long 	bigRightSZ		= ((long)rightSZ) << SHIFT;
	
	int		rightTX			= rightN2V.intTX;
	int		rightTY			= rightN2V.intTY;

	int 	bigRightDestSX 	= rightDestN2V.intSX << SHIFT;
	int 	rightDestSY 	= rightDestN2V.intSY;
	long 	bigRightDestSZ	= ((long)rightDestN2V.intSZ) << ZSHIFT;

	int		rightDestTX			= rightDestN2V.intTX;
	int		rightDestTY			= rightDestN2V.intTY;
	
	int 	rightDeltaSX; 
	int 	rightDeltaSY;
	long 	rightDeltaSZ;
	
	int 	rightDeltaTX; 
	int 	rightDeltaTY;
	
	rightDeltaSY = rightDestSY - rightSY;
	
	if(rightDeltaSY > 0)
	{
			rightDeltaSX = (bigRightDestSX - bigRightSX)  / rightDeltaSY;
			rightDeltaSY = (rightDestSY - rightSY);	// down counter
			rightDeltaSZ = (bigRightDestSZ - bigRightSZ) / rightDeltaSY;		

			rightDeltaTX = (rightDestTX - rightTX) / rightDeltaSY;
			rightDeltaTY = (rightDestTY - rightTY) / rightDeltaSY;
	}
	else
	{
			rightDeltaSX = 0;
			rightDeltaSY = 0;
			rightDeltaSZ = 0;
			
			rightDeltaTX = 0;
			rightDeltaTY = 0;
	}
	
	boolean trucking = true;
	
	while(trucking)
	{
		// ************ Render a line *******
		renderLine(
				bigLeftSX, 
				bigLeftSZ, 
				leftSY * contextWidth, 
				
				bigRightSX,
				bigRightSZ,
				
				tex,
				textureWidth,
				
				leftTX,
				leftTY,
				
				rightTX,
				rightTY,
				
				contextPixels,
				contextZBuffer,
				contextWidth,
				lightVal
		);
		
		// *********** move by delta *******
		bigLeftSX += leftDeltaSX;
		leftSY++;
		bigLeftSZ += leftDeltaSZ;
		leftTX += leftDeltaTX;
		leftTY += leftDeltaTY;
				
		bigRightSX += rightDeltaSX;
		// rightSY++;
		bigRightSZ += rightDeltaSZ;
		rightTX += rightDeltaTX;
		rightTY += rightDeltaTY;
		
		// *********** decrement steps to destination down counters ****
		leftDeltaSY--;
		rightDeltaSY--;
		
		// *********** handle if we reach left destination ******************
		while(leftDeltaSY <= 0)
		{
			leftN2V			= leftDestN2V;
			if(leftN2V == stopN2V)return;
			bigLeftSX		= bigLeftDestSX;
			leftSY 			= leftDestSY;
			bigLeftSZ		= bigLeftDestSZ;	
			
			leftTX			= leftDestTX;
			leftTY			= leftDestTY;
//			leftTX			= leftN2V.intTX;   // WHAT IT WAS
//			leftTY			= leftN2V.intTY;   // WHAT IT WAS
			
			// find a new destination
			leftDestN2V = leftDestN2V.anticlockwise;
			
			leftDestSY 			= leftDestN2V.intSY;
			leftDestTX			= leftDestN2V.intTX;
			leftDestTY			= leftDestN2V.intTY;
			
			leftDeltaSY = leftDestSY - leftSY;
			
			bigLeftDestSX = leftDestN2V.intSX << SHIFT;
			bigLeftDestSZ = ((long)leftDestN2V.intSZ) << ZSHIFT;
			
			if(leftDeltaSY > 0)
			{
				leftDeltaSX = (bigLeftDestSX - bigLeftSX)/leftDeltaSY;
				leftDeltaSZ = (bigLeftDestSZ - bigLeftSZ)/leftDeltaSY;	
				leftDeltaTX = (leftDestTX - leftTX) / leftDeltaSY;
				leftDeltaTY = (leftDestTY - leftTY) / leftDeltaSY;
			}
			else
			{
				leftDeltaSX = 0;
				leftDeltaSY = 0;
				leftDeltaSZ = 0;
				leftDeltaTX = 0;
				leftDeltaTY = 0;
			}
		}
		// *********** handle if we reach right destination ******************
		while(rightDeltaSY <= 0)
		{
			rightN2V		= rightDestN2V;
			bigRightSX		= bigRightDestSX;
			rightSY 		= rightDestSY;
			bigRightSZ		= bigRightDestSZ;
			
			rightTX			= rightDestTX;
			rightTY			= rightDestTY;
			
//			rightTX			= rightN2V.intTX;
//			rightTY			= rightN2V.intTY;
			
			// find a new destination
			rightDestN2V = rightDestN2V.clockwise;
			
			rightDestSY 			= rightDestN2V.intSY;
			rightDestTX			= rightDestN2V.intTX;
			rightDestTY			= rightDestN2V.intTY;
			
			rightDeltaSY = rightDestSY - rightSY;
			
			bigRightDestSX = rightDestN2V.intSX << SHIFT;
			bigRightDestSZ = ((long)rightDestN2V.intSZ) << ZSHIFT;

			
			if(rightDeltaSY > 0)
			{
				rightDeltaSX = (bigRightDestSX - bigRightSX)/rightDeltaSY;
				rightDeltaSZ = (bigRightDestSZ - bigRightSZ)/rightDeltaSY;	
				rightDeltaTX = (rightDestTX - rightTX) / rightDeltaSY;
				rightDeltaTY = (rightDestTY - rightTY) / rightDeltaSY;
			}
			else
			{
				rightDeltaSX = 0;
				rightDeltaSY = 0;
				rightDeltaSZ = 0;
				rightDeltaTX = 0;
				rightDeltaTY = 0;
			}
		}
	}
}

	
//*****************************************************************************
//*****************************************************************************
// ****************************** Render Line *********************************
//*****************************************************************************
//*****************************************************************************

private final void renderLine(
		final int 	bigLeftSX, 
		long 	bigLeftSZ, 
		final int 	indexOffset,
		
		final int 	bigRightSX,
		final long 	bigRightSZ,

		final int[] tex,
		final int textureWidth,
		
		int leftTX,
		int leftTY,
		
		final int rightTX,
		final int rightTY,
		
		final int[] contextPixels,
		final int[] contextZBuffer,
		final int contextWidth,
		final int lightVal
		)
{
	int lineStart 	= bigLeftSX >> SHIFT;
	int lineFinish 	= bigRightSX >> SHIFT;
				
	int lineLength = lineFinish - lineStart;
	
	// calculate zDelta and texture delta
	long zDelta;
	int txDelta;
	int tyDelta;
	if(lineLength > 0)
	{
		zDelta = (bigRightSZ - bigLeftSZ) / lineLength;
		txDelta = (rightTX - leftTX) / lineLength;
		tyDelta = (rightTY - leftTY) / lineLength;
	}
	else
	{
		zDelta = 0;
		txDelta = 0;
		tyDelta = 0;
	}
	
	
	
	while(lineLength >= 0)
	{
		// ***************** RENDER PIXEL ****************
		int pixelZ = (int)(bigLeftSZ >> ZSHIFT);
		int index = indexOffset + lineStart;
		
		if(pixelZ > contextZBuffer[index])
		{
			contextZBuffer[index] = pixelZ;
			int texPix = tex[(leftTY >> TEXTURE_SHIFT) * textureWidth + (leftTX >> TEXTURE_SHIFT)];
			contextPixels[index] = litPixelValue( lightVal, texPix);
		}
		
		// ***********************************************
		bigLeftSZ += zDelta;
		leftTX += txDelta;
		leftTY += tyDelta;
		
		lineStart++;
		lineLength--;	
	}
}

//*****************************************************************************
//*****************************************************************************
//*****************************************************************************
//*****************************************************************************
final private int litPixelValue(int lightVal, int colour)
{
	// convert to RGB
	int red = (colour 	& 0xFF0000) >> 16;
	int green = (colour & 0x00FF00) >> 8;
	int blue = (colour & 0x0000FF);
	
	// calculate lit RGB
	red = (red * lightVal) >> LIGHT_SHIFT;
	green = (green * lightVal) >> LIGHT_SHIFT;
	blue = (blue * lightVal) >> LIGHT_SHIFT;
	
	// clamp
	if(red > 255)		red = 255;
	if(green > green)	green = 255;
	if(blue > 255)		blue = 255;
	
	return (ALPHA | (red << 16) | (green << 8) | blue);
}

//*****************************************************************************
//*****************************************************************************
//*****************************************************************************
//*****************************************************************************

public void renderHLP(
		final NitrogenContext context,	
		
		final Nitrogen2Vertex leftN2V,
		final Nitrogen2Vertex leftDestN2V,
		
		final Nitrogen2Vertex rightN2V,
		final Nitrogen2Vertex rightDestN2V,
		Nitrogen2Vertex stopN2V,
		
		final int[] polyData,
		final TexMap textureMap,
		final float lightingValue	
		){}

public boolean isTextured(){return true;}

public boolean allowsHLP(){return false;}


}
