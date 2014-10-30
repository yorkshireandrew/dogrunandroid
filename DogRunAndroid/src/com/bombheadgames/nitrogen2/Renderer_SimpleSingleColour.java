/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bombheadgames.nitrogen2;

import java.io.Serializable;

/**
 *
 * @author andrew
 */
final public class Renderer_SimpleSingleColour implements Renderer,Serializable{

private static final long serialVersionUID = -7435141406825586043L;

private static final int ALPHA = 0xFF000000;
private static final int SHIFT = 20;
private static final int ZSHIFT = 20;

public void render(
		
		final NitrogenContext context,	
		
		Nitrogen2Vertex leftN2V,
		Nitrogen2Vertex leftDestN2V,
		
		Nitrogen2Vertex rightN2V,
		Nitrogen2Vertex rightDestN2V,
		final Nitrogen2Vertex stopN2V,
		
		final int[] polyData,
		final TexMap textureMap,
		final float lightingValue	
		)
{
	// **************** initialise colour ********************************************
	int colour = -1; // white
	if(polyData != null)colour = polyData[0] | ALPHA;
	
	// **************** initialise nitrogen context references ***********************
	final int[] contextPixels = context.pix;
	final int[] contextZBuffer = context.zbuff;
	final int contextWidth = context.w;
	
	
	// **************** Initialise Left start position and calculate delta ***********
	int 	bigLeftSX 		= leftN2V.intSX << SHIFT;
	int 	leftSY			= leftN2V.intSY;
	int 	leftSZ			= leftN2V.intSZ;
	long 	bigLeftSZ		= ((long)leftSZ) << ZSHIFT;

	int 	bigLeftDestSX 	= leftDestN2V.intSX << SHIFT;
	int 	leftDestSY 		= leftDestN2V.intSY;
	long 	bigLeftDestSZ	= ((long)leftDestN2V.intSZ) << ZSHIFT;
		
	int 	leftDeltaSX; 
	int 	leftDeltaSY;
	long 	leftDeltaSZ;
	
	leftDeltaSY = leftDestSY - leftSY;
	
	if(leftDeltaSY > 0)
	{
			leftDeltaSX = (bigLeftDestSX - bigLeftSX)/leftDeltaSY;
			leftDeltaSY = (leftDestSY - leftSY);	// down counter
			leftDeltaSZ = (bigLeftDestSZ - bigLeftSZ)/leftDeltaSY;		
	}
	else
	{
			leftDeltaSX = 0;
			leftDeltaSY = 0;
			leftDeltaSZ = 0;
	}
	
	// **************** Initialise Right start position and calculate delta ***********
	int 	bigRightSX 		= rightN2V.intSX << SHIFT;
	int 	rightSY			= rightN2V.intSY;
	int 	rightSZ			= rightN2V.intSZ;
	long 	bigRightSZ		= ((long)rightSZ) << SHIFT;

	int 	bigRightDestSX 	= rightDestN2V.intSX << SHIFT;
	int 	rightDestSY 	= rightDestN2V.intSY;
	long 	bigRightDestSZ	= ((long)rightDestN2V.intSZ) << ZSHIFT;
		
	int 	rightDeltaSX; 
	int 	rightDeltaSY;
	long 	rightDeltaSZ;
	
	rightDeltaSY = rightDestSY - rightSY;
	
	if(rightDeltaSY > 0)
	{

			rightDeltaSX = (bigRightDestSX - bigRightSX) / rightDeltaSY;
			rightDeltaSY = (rightDestSY - rightSY);	// down counter
			rightDeltaSZ = (bigRightDestSZ - bigRightSZ) / rightDeltaSY;		
	}
	else
	{
			rightDeltaSX = 0;
			rightDeltaSY = 0;
			rightDeltaSZ = 0;
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
				
				colour,
				contextPixels,
				contextZBuffer,
				contextWidth	
		);
		
		// *********** move by delta *******
		bigLeftSX += leftDeltaSX;
		leftSY++;
		bigLeftSZ += leftDeltaSZ;
				
		bigRightSX += rightDeltaSX;
		// rightSY++;
		bigRightSZ += rightDeltaSZ;
		
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
			
			// find a new destination
			leftDestN2V = leftDestN2V.anticlockwise;
			leftDestSY 			= leftDestN2V.intSY;
			
			leftDeltaSY 		= leftDestSY - leftSY;
			
			if(leftDeltaSY > 0)
			{
				bigLeftDestSX = leftDestN2V.intSX << SHIFT;
				bigLeftDestSZ = ((long)leftDestN2V.intSZ) << ZSHIFT;
				leftDeltaSX = (bigLeftDestSX - bigLeftSX)/leftDeltaSY;
				leftDeltaSZ = (bigLeftDestSZ - bigLeftSZ)/leftDeltaSY;	
			}
			else
			{
//				bigLeftDestSX = leftDestN2V.intSX << SHIFT;
//				bigLeftDestSZ = ((long)leftDestN2V.intSZ) << ZSHIFT;
				leftDeltaSX = 0;
				leftDeltaSY = 0;
				leftDeltaSZ = 0;
			}
		}
		
		// *********** handle if we reach right destination ******************
		while(rightDeltaSY <= 0)
		{
			rightN2V		= rightDestN2V;

			bigRightSX		= bigRightDestSX;
			rightSY 		= rightDestSY;
			bigRightSZ		= bigRightDestSZ;			
			
			// find a new destination
			rightDestN2V = rightDestN2V.clockwise;
			
			rightDestSY = rightDestN2V.intSY;
			rightDeltaSY = rightDestSY - rightSY;

//			bigRightDestSX = rightDestN2V.intSX << SHIFT;
//			bigRightDestSZ = ((long)rightDestN2V.intSZ) << ZSHIFT;
			
			if(rightDeltaSY > 0)
			{
				bigRightDestSX = rightDestN2V.intSX << SHIFT;
				bigRightDestSZ = ((long)rightDestN2V.intSZ) << ZSHIFT;

				rightDeltaSX = (bigRightDestSX - bigRightSX)/rightDeltaSY;
				rightDeltaSZ = (bigRightDestSZ - bigRightSZ)/rightDeltaSY;	
			}
			else
			{
				rightDeltaSX = 0;
				rightDeltaSY = 0;
				rightDeltaSZ = 0;
			}
		}
	}//end of while loop
}

//*****************************************************************************
//*****************************************************************************
// ****************************** Render Line *********************************
//*****************************************************************************
//*****************************************************************************

private final void renderLine(
		final int 	bigLeftSX, 
		long 		bigLeftSZ, 
		final int 	indexOffset,
		
		final int 	bigRightSX,
		final long 	bigRightSZ,

		final int 	colour,
		final int[] contextPixels,
		final int[] contextZBuffer,
		final int contextWidth
		)
{
	int lineStart 	= bigLeftSX >> SHIFT;
	int lineFinish 	= bigRightSX >> SHIFT;	
	
	int lineLength = lineFinish - lineStart;
	
	// calculate zDelta
	long zDelta;
	if(lineLength > 0)
	{
		zDelta = (bigRightSZ - bigLeftSZ) / lineLength;
	}
	else
	{
		zDelta = 0;
	}
	
	while(lineLength >= 0)
	{
		// ***************** RENDER PIXEL ****************
		int pixelZ = (int)(bigLeftSZ >> ZSHIFT);
		int index = indexOffset + lineStart;
		
		if(pixelZ > contextZBuffer[index])
		{
			contextZBuffer[index] = pixelZ;
			contextPixels[index] = colour;
		}
		
		// ***********************************************
		bigLeftSZ += zDelta;
		lineStart++;
		lineLength--;	
	}
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
		final Nitrogen2Vertex stopN2V,
		
		final int[] polyData,
		final TexMap textureMap,
		final float lightingValue	
		){}

public boolean isTextured(){return false;}

public boolean allowsHLP(){return false;}

}// end of class
