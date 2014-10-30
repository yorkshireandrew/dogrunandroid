package com.bombheadgames.nitrogen2;

/** responsible for delegating rendering to other Nitrogen2 renderer classes */
public class Nitrogen2PolygonRenderer {
	
	static final Nitrogen2Vertex[] workingN2Vs;
	static final int WORKING_N2V_SIZE = 20;
	static int workingN2Vindex;
	
	static{
		// instantiate working vertexes
		workingN2Vs = new Nitrogen2Vertex[WORKING_N2V_SIZE];
		for(int x = 0; x < WORKING_N2V_SIZE; x++)
		{
			workingN2Vs[x] = new Nitrogen2Vertex();
		}
	}
	
	/** adds flags if the polygon is textured and HLP */

	
	/** Calculates flags for if the polygon is un-textured and if the polygon is HLP. 
	 * Creates circular LLL of appropriate Nitrogen2Vertexes (N2Vs). 
	 * Finds topmost N2V then left and right ones at the same SY, and the lowest.
	 * (static field stopN2V). Removes any twists that can be caused by rounding.
	 * N2Vs are then passed as parameters to the appropriate method on the render 
	 */
	final static void process
	(
		final NitrogenContext context,		
		final Vertex start, 	
		final Renderer renderer,
		final int[] polyData,
		final TexMap textureMap,
		final float lightingValue,
		final boolean useHLP
	){	
		context.polygonsRendered++;
		// ******* calculate flags **********
		boolean polygonUntextured = true;
		boolean polygonIsHLP = false;
		if(renderer.isTextured())
		{
			polygonUntextured = false;
			if (	useHLP 
					&& renderer.allowsHLP()
					&& isHLP(start, context.qualityOfHLP))polygonIsHLP = true;
		}
		
		// ********* create LLL of appropriate N2Vs ******************
		Nitrogen2Vertex topmostN2V;	
		int index = 0;
		int minSY;
		int maxSY;
		
		Nitrogen2Vertex[] workingN2VsL = workingN2Vs; // cache locally for speed

		// create N2V for passed in Vertex
		Nitrogen2Vertex startN2V = workingN2VsL[index++];
		start.calculateScreenSpaceCoordinate(context);
		minSY = startN2V.initialize(start, polygonUntextured, polygonIsHLP);
		topmostN2V = startN2V;
		
		Nitrogen2Vertex stopN2V = startN2V;
		maxSY = minSY; // initialise maxSY to startN2V.SY
		
		// step round anticlockwise creating N2Vs and clockwise references
		// also remember the N2V with lowest screen Y coordinate
		Vertex next = start.anticlockwise;
		Nitrogen2Vertex previousOne = startN2V;
		while(next != start)
		{
			Nitrogen2Vertex nextN2V = workingN2VsL[index++];
			next.calculateScreenSpaceCoordinate(context);
			int nextSY = nextN2V.initialize(next, polygonUntextured, polygonIsHLP);
			if(nextSY <= minSY)
			{
				topmostN2V = nextN2V;
				minSY = nextSY;
			}
			if(nextSY > maxSY)
			{
				stopN2V = nextN2V;
				maxSY = nextSY;
			}
			nextN2V.clockwise = previousOne;
			previousOne = nextN2V;
			next = next.anticlockwise;
		}
		startN2V.clockwise = previousOne; // complete the loop of clockwise references
		// step round clockwise creating the anticlockwise references
		Nitrogen2Vertex previousN2V = startN2V;
		Nitrogen2Vertex nextN2V = startN2V.clockwise;
		while(nextN2V != startN2V)
		{
			nextN2V.anticlockwise = previousN2V;
			previousN2V = nextN2V;
			nextN2V = nextN2V.clockwise;	
		}
		
		startN2V.anticlockwise = previousN2V;
		
		// ****** Find left and rightmost N2Vs as same SY ******
		// ****** Also remove twists ***************************
		Nitrogen2Vertex leftN2V = findLeftN2V(topmostN2V);
		removeLeftTwists(leftN2V, stopN2V);
		Nitrogen2Vertex leftDestN2V = leftN2V.anticlockwise;
		Nitrogen2Vertex rightN2V = findRightN2V(topmostN2V);
		removeRightTwists(rightN2V, stopN2V);
		Nitrogen2Vertex rightDestN2V = rightN2V.clockwise;

		if(leftN2V == stopN2V)return;
		
		// ******* Call appropriate renderer method ************
		if(polygonIsHLP)
		{
			renderer.renderHLP(
					context,
					leftN2V,leftDestN2V,
					rightN2V,rightDestN2V,
					stopN2V,
					polyData,
					textureMap,
					lightingValue
					);
		}
		else
		{
			renderer.render(
					context,
					leftN2V,leftDestN2V,
					rightN2V,rightDestN2V,
					stopN2V,
					polyData,
					textureMap,
					lightingValue
					);
		}
	}
	
	/** finds leftmost Nitrogen2Vertex with the same screen Y as parameter */
	final static Nitrogen2Vertex findLeftN2V(Nitrogen2Vertex in)
	{
		int inSY = in.intSY;
		Nitrogen2Vertex retval = in;
		Nitrogen2Vertex testN2V = in.anticlockwise;
		while((testN2V.intSY == inSY)&&(testN2V != in))
		{
			if(testN2V.intSX < retval.intSX)retval = testN2V;
			testN2V = testN2V.anticlockwise;
		}
		return retval;	
	}
	
	/** finds rightmost Nitrogen2Vertex with the same screen Y as parameter */
	final static Nitrogen2Vertex findRightN2V(Nitrogen2Vertex in)
	{
		int inSY = in.intSY;
		Nitrogen2Vertex retval = in;
		Nitrogen2Vertex testN2V = in.clockwise;
		while((testN2V.intSY == inSY)&&(testN2V != in))
		{
			if(testN2V.intSX > retval.intSX)retval = testN2V;
			testN2V = testN2V.clockwise;
		}
		return retval;	
	}
	
	/** remove any twists in left side caused by rounding */
	final static void removeLeftTwists(Nitrogen2Vertex inN2V, Nitrogen2Vertex stopN2V)
	{	
		while(inN2V != stopN2V)
		{
			Nitrogen2Vertex nextN2V = inN2V.anticlockwise;
			
			if(nextN2V.intSY < inN2V.intSY)
			{
				// undo twist
				Nitrogen2Vertex cw = inN2V.clockwise;
				Nitrogen2Vertex acw = nextN2V.anticlockwise;
				
				cw.anticlockwise = nextN2V;
				nextN2V.clockwise = cw;
				
				acw.clockwise = inN2V;
				inN2V.anticlockwise = acw;
				
				inN2V.clockwise = nextN2V;
				nextN2V.anticlockwise = inN2V;
				
				inN2V = nextN2V;
			}
			else
			{
				inN2V = nextN2V;
			}
		}
	}
	
	/** remove any twists in left side caused by rounding */
	final static void removeRightTwists(Nitrogen2Vertex inN2V, Nitrogen2Vertex stopN2V)
	{	
		while(inN2V != stopN2V)
		{
			Nitrogen2Vertex nextN2V = inN2V.clockwise;
			
			if(nextN2V.intSY < inN2V.intSY)
			{
				// undo twist
				Nitrogen2Vertex cw = nextN2V.clockwise;
				Nitrogen2Vertex acw = inN2V.anticlockwise;
				
				acw.clockwise = nextN2V;
				nextN2V.anticlockwise = acw;
				
				cw.anticlockwise = inN2V;
				inN2V.clockwise = cw;
				
				inN2V.anticlockwise = nextN2V;
				nextN2V.clockwise = inN2V;
				
				inN2V = nextN2V;
			}
			else
			{
				inN2V = nextN2V;
			}
		}
	}
	
	/** Static method to test if a vertex loop LLL has a high-level-perspective (HLP) */
	final static boolean isHLP(Vertex start, float qualityLevel)
	{
		
		float min = start.vs_z;
		float max = start.vs_z;	
		Vertex next = start.anticlockwise;	
		while(next != start)
		{
			float z = next.vs_z;
			if(z > max) max = z;
			if(z < min) min = z;
			next = next.anticlockwise;	
		}		
		float thresh = max * qualityLevel;
		return(min < thresh);
	}
}
