package com.bombheadgames.nitrogen2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;

/** An Item comprised of several polygons, TexMaps, Colour data, BSPPlanes 
 * that can be attached to a Transform object and thereby rendered into a 
 * NitrogenContext.
 * @author andrew
 *
 */
final public class Item implements Serializable{
	private static final long serialVersionUID = 1592586975370696804L;
	
	// Enumerations for whichRenderer
	static final int NEAR_RENDERER = 0;
	static final int MID_RENDERER = 1;
	static final int FAR_RENDERER = 2;
	
	// used for drawing collision vertexes
	static final float[] sinTable;
	static final float[] cosTable;
		
	/** Class used to source and sink the internal components of Items*/
	static ItemFactory itemFactory;

	/** Parent transform of this Item in the scene graph */
	private Transform parent = null;
	
	/** The immutable part of an Item that can be shared across many identical Items that differ only by scene graph position and visibility. */
	private SharedImmutableSubItem sisi;
		
	/** The Items computed Backsides. A Backsides consists of a position, view space coordinate and a direction vector, 
	 * they are used by polygons to determine visibility and which of their two faces is being viewed 
	 * serialisation is controlled by writeObject & readObject*/
	private transient Backside backsides[];
	
	/** The Items vertexes 
	 * serialisation is controlled by writeObject & readObject*/

	private transient Vertex vertexes[];
	
	/** The Items collision vertexes 
	 *serialisation is controlled by writeObject & readObject*/
	private boolean hasCollisionVertexes;
	public transient Vertex collisionVertexes[];
	
	// ************************************************
	// ********************** FLAGS *******************
	// ************************************************
	
	/** Set true if the item is visible, or false if the Item is invisible*/
	private boolean visibility = false;
	
	/** Set true by scene graph if rotation has occurred since last render call */
	private boolean rotationNeedsUpdate = true;
	
	/** Set true by scene graph if rotation or translation has occurred since last render call */
	private boolean translationNeedsUpdate = true;
	
	/** Enumerated value that determines which renderer to use based on distance */
	private int whichRendererOld = NEAR_RENDERER;
		
	// Which fustrum planes the item may touch, used to improve efficiency of polygon clipping
	// note: Items remain visible then clip entirely once they cross the fustrum farClip distance
	transient private boolean touchedNear;
	transient private boolean touchedRight;
	transient private boolean touchedLeft;
	transient private boolean touchedTop;
	transient private boolean touchedBottom;
	
	/** Flag to render using improved detail polygons. This is a state field used to apply hysteresis */
	private boolean isImprovedDetail = false;
	
	/** Flag to use hlp breaking. This is a state field used to apply hysteresis */
	private boolean isUsingHLP = false;
	
	/** Flag to use billboard Orientation. This is a state field used to apply hysteresis */
	private boolean isUsingBillboardOriention = false;
	
	/** name of the Item */
	private String name;
	
	/** package scope flag so we can tell if the Item was rendered if asked */
	boolean wasRendered = false;
	
	/** hack for ContentGenerator, so we can see if a backside faces the viewer */
	public Backside testBackside;
	
	/** package scope reference for use in factories LLL*/
	Item nextInList;
	
	/** initialise static sin and cosine tables */
	static{
		sinTable = new float[360];
		cosTable = new float[360];
		for(int i = 0; i < 360; i++)
		{
			double theta = (Math.PI / 180)*(double)i;
			sinTable[i] = (float)Math.sin(theta);
			cosTable[i] = (float)Math.cos(theta);
		}
	}
	
    /** default constructor used by factories to preallocate an Item */
    Item(){} 

    /** constructor used by factories to generate new Items on request
	 * @param in_sisi The SharedImmutableSubItem used to compose the Item
	 * @param t Transform the Item is to be attached to
	 * @param factory ItemFactory that shall provide the internal parts of the Item
	 * */
    Item(final SharedImmutableSubItem in_sisi, final Transform t)
    {
    	this();
    	this.initializeItem(in_sisi, t);
    }
    
    /** create an Item from the factory */
    final static public Item createItem(final SharedImmutableSubItem in_sisi, final Transform t)
    {
    	return (itemFactory.getItem(in_sisi, t));
    }
    
	public static ItemFactory getItemFactory() {
		return itemFactory;
	}

	public static void setItemFactory(ItemFactory itemFactory) {
		Item.itemFactory = itemFactory;
	}
	
    /** used by factories to re-use an Item
	 * @param in_sisi The SharedImmutableSubItem used to compose the Item
	 * @param t Transform the Item is to be attached to
	 */
	final void initializeItem(final SharedImmutableSubItem in_sisi, final Transform t)
	{
		if(t == null)return;
		
		parent = t;
		t.add(this);
		
		sisi = in_sisi;
		
		ItemFactory itemFactoryL = itemFactory;

		// create a blank backside array
		final ImmutableBackside[] inSISIImmutableBacksides = in_sisi.immutableBacksides;		
		final int backsideMax = inSISIImmutableBacksides.length;
		final Backside[] backsidesL = new Backside[backsideMax];
		for(int x = 0; x < backsideMax; x++)backsidesL[x] = itemFactoryL.getBackside(inSISIImmutableBacksides[x]);
		backsides = backsidesL;

		// create the vertexes array from the sisi ImmutableVertexs
		final ImmutableVertex[] inSISIImmutableVertexes = in_sisi.immutableVertexes;		
		final int vertexMax = inSISIImmutableVertexes.length;
		Vertex[] vertexesL = new Vertex[vertexMax];
		for(int x = 0; x < vertexMax; x++)vertexesL[x] = itemFactoryL.getVertex(inSISIImmutableVertexes[x]);
		vertexes = vertexesL;
		
		// create the collision vertexes array from the sisi ImmutableVertexs
		final ImmutableCollisionVertex[] inSISIImmutableCollisionVertexes = in_sisi.immutableCollisionVertexes;		
		final int collisionVertexMax = inSISIImmutableCollisionVertexes.length;
		final Vertex[] collisionVertexesL = new Vertex[collisionVertexMax];
		for(int x = 0; x < collisionVertexMax; x++)collisionVertexesL[x] = itemFactoryL.getVertex(inSISIImmutableCollisionVertexes[x]);	
		collisionVertexes = collisionVertexesL;
		if(collisionVertexMax > 0){hasCollisionVertexes = true;}
		else{hasCollisionVertexes = false;}
		
		// clear other flags
		visibility = false;
		rotationNeedsUpdate = true;
		translationNeedsUpdate = true;
		whichRendererOld = NEAR_RENDERER;
		isImprovedDetail = false;	
		isUsingHLP = false;
		isUsingBillboardOriention = false;
		name = null;
		wasRendered = false;
		nextInList = null;
	}
	
	final void copyFrom(Item i)
	{
		parent = i.parent;	
		sisi = i.sisi;
		backsides = i.backsides;
		vertexes = i.vertexes;
		collisionVertexes = i.collisionVertexes;
		visibility = i.visibility;
		rotationNeedsUpdate = i.rotationNeedsUpdate;
		translationNeedsUpdate = i.translationNeedsUpdate;
		whichRendererOld = i.whichRendererOld;
		isImprovedDetail = i.isImprovedDetail;	
		isUsingHLP = i.isUsingHLP;
		isUsingBillboardOriention = i.isUsingBillboardOriention;
		name = i.name;
		wasRendered = i.wasRendered;
		nextInList = i.nextInList;		
	}
	
	/** called to render the Item 
	 * @param context The NitrogenContext to render the Item in
	 * @param v11-v34 The orientation matrix computed by the scene graph (12 floating point values)*/
	void renderItem(

			NitrogenContext context,
			// position vector from scene graph
			float v11, float v12, float v13, float v14,
			float v21, float v22, float v23, float v24,
			float v31, float v32, float v33, float v34)
	{

		// return if the Item is not set visible
		if(visibility == false)return;
		
		// return if the Item is fustrum culled using context
		if(isItemFustrumCulled(v14,v24,v34,context))return;
		
		// far plane cull using the Items own farPlane
		final float itemDist = -v34;
		final SharedImmutableSubItem 	sisiL = sisi;
		if(itemDist > sisiL.farPlane)return;

		// DEBUG
		context.itemsRendered++;
		
		// remember we were rendered in case anyone asks
		wasRendered = true;
		
		// inform context we are being rendered for PickingRenderer
		context.currentItem = this;
		
		// which near-mid-far renderer to use for this render
		int whichRendererIsIt = whichRendererOld;	// the near-mid-far renderer to use for rendering this item
		
		if(translationNeedsUpdate)
		{
			if(!isUsingBillboardOriention)
			{
				if(itemDist > sisiL.billboardOrientationDistPlus)
				{			
					// Item has just become a billboard 
					// so orientate along the view-space axis
					v11=1;v12=0;v13=0;
					v21=0;v22=1;v23=0;
					v31=0;v32=0;v33=1;
					
					// ensure above change gets applied 
					rotationNeedsUpdate = true;
					
					// set the billboard flag
					isUsingBillboardOriention = true;
				}
			}
			else
			{
				// inhibit rotation updates from scene graph 
				rotationNeedsUpdate = false;
				
				if(itemDist < sisiL.billboardOrientationDist)
				{
					// ensure the passed in scene graph orientation is applied
					rotationNeedsUpdate = true;
					
					// clear the billboard flag
					isUsingBillboardOriention = false;		
				}
			}
			
			// see if renderer needs changing
			whichRendererIsIt = selectWhichRenderer(itemDist,sisiL);			
			
			// update other flags used during rendering
			updateRenderingFlags(itemDist,sisiL);
			
			// update item fustrum flags
			calculateItemFustrumFlags(v14,v24,v34,context, sisiL);
		}
				
		//Cache values needed for rendering locally
		final boolean 	touchedNearL 		= touchedNear;
		final boolean 	touchedRightL 		= touchedRight;
		final boolean 	touchedLeftL		= touchedLeft;
		final boolean 	touchedTopL			= touchedTop;
		final boolean 	touchedBottomL  	= touchedBottom;
		
		lazyComputeBacksidesAndVertexs();

		// Select the right number of polygons to render
		final int polyStart;
		final int polyFinish;
		if(isImprovedDetail)
		{
			polyStart = sisiL.improvedDetailPolyStart;
			polyFinish = sisiL.improvedDetailPolyFinish;
		}
		else
		{
			polyStart = sisiL.normalDetailPolyStart;
			polyFinish = sisiL.normalDetailPolyFinish;
		}
		
		ImmutablePolygon immutablePolygon;
		int backsideIndex;
		Backside backside;
		boolean immutablePolygonIsTransparentL;
		final boolean contextTransparencyPassL = context.transparencyPass;
		final boolean isUsingHLPL = isUsingHLP;
		
		Vertex[] vertexArray;
		int[] vertexIndexArray;
		
		/** True unless the Items SharedImmutableSubItem nearPlaneCrashBacksideOverride is true and the Item has also crashed into near Plane */
		final boolean noBacksideCullOverride = (!sisiL.nearPlaneCrashBacksideOverride) ||(!touchedNear);
		
		for(int x = polyStart; x < polyFinish; x++)
		{
			// added for Content Generator 
			context.currentPolygon = x;
			
			immutablePolygon = sisiL.immutablePolygons[x];
			
			// skip the polygon if its transparency is wrong for the pass
			immutablePolygonIsTransparentL = immutablePolygon.isTransparent;
			if(!contextTransparencyPassL &&  immutablePolygonIsTransparentL)continue;
			if( contextTransparencyPassL && !immutablePolygonIsTransparentL)continue;
			
			// calculate the polygons backside if necessary
			backsideIndex = immutablePolygon.backsideIndex;
			backside = backsides[backsideIndex];
			if(backside.translationNeedsUpdate)
			{
				backside.calculate(
						context,
						v11,v12,v13,v14,
						v21,v22,v23,v24,
						v31,v32,v33,v34);
			}
			
			if(backside.facingViewer() )
			{				
				// Calculate the vertexes, then Pass the polygon on to the next process.
				vertexIndexArray = immutablePolygon.vertexIndexArray;
				int vertexIndexArrayLength = vertexIndexArray.length;
				vertexArray = new Vertex[vertexIndexArrayLength];
				
				for(int q = 0; q < vertexIndexArrayLength; q++)
				{	
					Vertex v = vertexes[immutablePolygon.vertexIndexArray[q]];
					v.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
					PolygonVertexData pvd = immutablePolygon.polygonVertexDataArray[q];
					if(pvd != null)v.setAux(immutablePolygon.polygonVertexDataArray[q]);
					vertexArray[q]  = v;                   
				}

				Nitrogen2PolygonClipper.process(
						context,
						
						touchedNearL,
						touchedRightL,
						touchedLeftL,
						touchedTopL,
						touchedBottomL,	
						
						vertexArray,

						immutablePolygon.getRenderer(whichRendererIsIt, context.isPicking),
						immutablePolygon.polyData,
						immutablePolygon.textureMap,						
						backside.lightingValue,
						isUsingHLPL
				);

			}
			else
			{
				// Skip rendering the polygon if it is backside culled
				if(immutablePolygon.isBacksideCulled && noBacksideCullOverride)continue;
				
				vertexIndexArray = immutablePolygon.vertexIndexArray;
				int vertexIndexArrayLength = vertexIndexArray.length;
				vertexArray = new Vertex[vertexIndexArrayLength];
				/*
				// create array but fill backwards, to ensure anticlockwise ordering
				int to = vertexIndexArrayLength - 1;
				for(int q = 1; q < vertexIndexArrayLength; q++)
				{
					Vertex v = vertexes[immutablePolygon.vertexIndexArray[q]];
					v.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
					PolygonVertexData pvd = immutablePolygon.polygonVertexDataArray[q];
					if(pvd != null)v.setAux(immutablePolygon.polygonVertexDataArray[q]);
					vertexArray[to--]  = v;                   
				}
				
				// keep first vertex
				Vertex v = vertexes[immutablePolygon.vertexIndexArray[0]];
				v.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
				PolygonVertexData pvd = immutablePolygon.polygonVertexDataArray[0];
				if(pvd != null)v.setAux(immutablePolygon.polygonVertexDataArray[0]);
				vertexArray[0]  = v; 				
				*/
				
				// create array but fill backwards, to ensure anticlockwise ordering
				int to = vertexIndexArrayLength;
				for(int q = 0; q < vertexIndexArrayLength; q++)
				{
					Vertex v = vertexes[immutablePolygon.vertexIndexArray[q]];
					v.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
					PolygonVertexData pvd = immutablePolygon.polygonVertexDataArray[q];
					if(pvd != null)v.setAux(immutablePolygon.polygonVertexDataArray[q]);
					vertexArray[--to]  = v;                   
				}
				
				Nitrogen2PolygonClipper.process(
						context,
						touchedNearL,
						touchedRightL,
						touchedLeftL,
						touchedTopL,
						touchedBottomL,					
						vertexArray,

						immutablePolygon.getRenderer(whichRendererIsIt, context.isPicking),
						immutablePolygon.polyData,
						immutablePolygon.textureMap,						
						backside.lightingValue,
						isUsingHLPL
				);				
			} //end of backside facing viewer if-else
		} //end of polygon rendering loop	
	}
	
	//**************************** END OF RENDER *****************************
	
	/** Quick Optimistic Item fustrum culling using boundingRadius. Returns true only if the item lies completely outside the view-fustrum */
	final private boolean isItemFustrumCulled(final float x, final float y, final float z, final NitrogenContext context)
	{	
		final float boundingRadiusCache = sisi.boundingRadius;
		
		// calculate optimistic distance from viewpoint
		final float dist = boundingRadiusCache - z;
		final float allowedRightness = context.xClip * dist;
		final float allowedDownness = context.yClip * dist;
		
		// near clip
		if(dist < context.nearClip)return(true);
		
		// far clip
		if(dist > context.farClip)return(true); // far culled by context
		
		// right clip
		if((x - boundingRadiusCache) > allowedRightness)return (true);

		// left clip
		if((-x - boundingRadiusCache) > allowedRightness)return (true);

		// bottom clip
		if((y - boundingRadiusCache) > allowedDownness)return (true);

		// top clip
		if((-y - boundingRadiusCache) > allowedDownness)return (true);
		
		return  (false);
	}
	
	/** Calculates using boundingRadius if the Item touches any planes of the view-fustrum, then sets flags to improve the efficiency of polygon clipping. A Pessimistic approximation used. 
	 *  @param x x-coordinate of centre of Item
	 *  @param y y-coordinate of centre of Item
	 *  @param z z-coordinate of centre of Item
	 *  @param context The NitrogenContext object that defines the fustrum.
	 *  */
	final private void calculateItemFustrumFlags(final float x, final float y, final float z, final NitrogenContext context, final SharedImmutableSubItem sisiL)
	{
		final float sisiBoundingRadius = sisiL.boundingRadius;
		
		// calculate pessimistic distance from viewpoint
		final float dist = -z - sisiBoundingRadius;
		final float allowedRightness = context.xClip * dist;
		final float allowedDownness = context.yClip * dist;	
		
		// near clip
		if(dist < context.nearClip)
		{
			touchedNear = true;
		}
		else
		{touchedNear = false;}		
		
		// right clip
		if((x + sisiBoundingRadius) > allowedRightness)
		{
			touchedRight = true;			
		}
		else
		{touchedRight = false;}

		// left clip
		if((-x + sisiBoundingRadius) > allowedRightness)
		{
			touchedLeft = true;			
		}
		else
		{touchedLeft = false;}
		
		// bottom clip
		if((-y + sisiBoundingRadius) > allowedDownness)
		{
			touchedBottom = true;			
		}
		else
		{touchedBottom = false;}
		
		// top clip
		if((y + sisiBoundingRadius) > allowedDownness)
		{
			touchedTop = true;			
		}
		else
		{touchedTop = false;}
	}
	
	/** Examines rotationNeedsUpdate and translationNeedsUpdate flags then if necessary informs all the Items backsides and vertexs that they have moved 
	 * . Also as a side-effect it clears the aforementioned flags */
	final private void lazyComputeBacksidesAndVertexs()
	{
		Backside b;
		Vertex v;
		if(rotationNeedsUpdate)
		{
			final int backsidesLength = backsides.length;
			for(int x = 0; x < backsidesLength; x++)
			{
				b = backsides[x];
				b.rotationNeedsUpdate = true;
				b.translationNeedsUpdate = true;
			}
			
			final int vertexsLength = vertexes.length;
			for(int x = 0; x < vertexsLength; x++)
			{
				v = vertexes[x];
				v.rotationNeedsUpdate = true;
				v.translationNeedsUpdate = true;
			}
			
			if(hasCollisionVertexes)
			{
				Vertex cv;
				final int collisionVertexesLength = collisionVertexes.length;
				for(int x = 0; x < collisionVertexesLength; x++)
				{
					cv = collisionVertexes[x];
					cv.rotationNeedsUpdate = true;
					cv.translationNeedsUpdate = true;
				}
			}
			// clear the flag
			rotationNeedsUpdate = false;
			translationNeedsUpdate = false;
			return;
		}
		
		// Its just a translation
		if(translationNeedsUpdate)
		{
			final int backsidesLength = backsides.length;
			for(int x =0; x < backsidesLength; x++) backsides[x].translationNeedsUpdate = true;

			final int vertexsLength = vertexes.length;
			for(int x =0; x < vertexsLength; x++)vertexes[x].translationNeedsUpdate = true;

			if(hasCollisionVertexes)
			{
				Vertex cv;
				final int collisionVertexesLength = collisionVertexes.length;
				for(int x =0; x < collisionVertexesLength; x++)
				{
					cv = collisionVertexes[x];
					cv.translationNeedsUpdate = true;
				}
			}			
			// clear the flag
			translationNeedsUpdate = false;
		}
	}
	
	/** Updates the Items whichRenderer field, for example a different NEAR_RENDERER could be used up close to add interpolation and a different FAR_RENDERER could
	 * be used at a distance to render using a fixed colour instead of texture for speed and to reduce aliasing artifacts. </br></br>This method also providing some hysteresis to prevent flickering*/
	final private int selectWhichRenderer(final float dist, final SharedImmutableSubItem sisiL)
	{
		
		int whichRendererNew = whichRendererOld; 
		
		switch(whichRendererOld)
		{
			case NEAR_RENDERER:
				if(dist > sisiL.nearRendererDistPlus)whichRendererNew = MID_RENDERER;
				break;

			case MID_RENDERER:
				if(dist < sisiL.nearRendererDist)whichRendererNew = NEAR_RENDERER;
				if(dist > sisiL.farRendererDistPlus)whichRendererNew = FAR_RENDERER;
				break;
				
			case FAR_RENDERER:
				if(dist < sisiL.farRendererDist)whichRendererNew = MID_RENDERER;		
				break;
				
			default: whichRendererNew = NEAR_RENDERER;	
		}
		
		// ensure this Item's whichRenderer is updated if there has been a change
		whichRendererOld = whichRendererNew;
		return whichRendererNew;
	}
	
	final private void updateRenderingFlags(final float itemDist, final SharedImmutableSubItem sisi)
	{
		// see if isImprovedDetail needs changing
		if(isImprovedDetail)
		{
			if(itemDist > sisi.improvedDetailDistPlus)isImprovedDetail = false;
		}
		else
		{
			if(itemDist < sisi.improvedDetailDist)isImprovedDetail = true;
		}
		
		if(isUsingHLP)
		{
			if(itemDist > sisi.hlpBreakingDistPlus)isUsingHLP = false;
		}
		else
		{
			if(itemDist < sisi.hlpBreakingDist)isUsingHLP = true;
		}
	}
	

	
	
	/** Sets a flag on the Item informing it that it has translated (moved) but not rotated, so on next render it will re-compute
	 * all its vertex view-space coordinates etc to account for the translation, then clear the flag.
	 */
	public final void setNeedsTranslationUpdating()
	{
		translationNeedsUpdate = true;
	}
	
	/** Sets flags on the Item informing it that it has rotated and possibly translated (moved) so on next render it will re-compute
	 * all its vertex view-space coordinates etc to account for it, then clear the flags*/
	public final void setNeedsTotallyUpdating()
	{
		// this causes updating of the offset of the vertexes from the Items origin
		// It also results in the translationNeedsUpdate flag on all backsides and vertexs being set
		rotationNeedsUpdate = true;		
		translationNeedsUpdate = true;		//lets selectWhichRenderer know that it has to do something
	}
	
	final public void setVisibility(boolean in)
	{
		if(visibility == in)return;
		if(visibility)
		{
			// we are visible so make invisible
			if(parent != null)parent.decreaseVisibleChildrenBy(1);
			visibility = false;
		}
		else
		{
			// we are invisible so make visible
			if(parent != null)parent.increaseVisibleChildrenBy(1);
			visibility = true;			
		}
	}
	
	/** Access method 
	 * @return The parent of the called  Transform, or null if it is the scenegraph root*/
	final public Transform getParent() {return(parent);}
	
	/** Access method setting the parent of the called Transform, breaking any existing parental bond
	 * if it exists. 
	 * @param new_parent The Transform to be set as the parent of the called Transform*/
	final public void setParent(Transform new_parent) 
	{
		// Detach from existing parent 
		if(parent != null)
		{			
			parent.remove(this);			
		}
		
		if(new_parent != null)
		{
			// Attach to new parent
			parent = new_parent;
			parent.add(this);
		}
		else
		{
			parent = null;
		}
		
		// Ensure the resulting scene-graph branch gets updated on the next render
		setNeedsTotallyUpdating();
	}
	
	final public boolean isVisible(){return visibility;}

	final public String getName() {
		return name;
	}

	final public void setName(String name) {
		this.name = name;
	}
	
	/** returns true if the Item was rendered (not fustrum culled)
	 * since the last call to clearWasRenderedFlags() higher up the scene graph
	 */
	final public boolean wasRendered(){return wasRendered;}
	
	final public void calculateCollisionVertexes()
	{
		if(!hasCollisionVertexes)return;
		if(rotationNeedsUpdate || translationNeedsUpdate)
		{
			Transform parentL = parent;
			// ensure parent Transform is up to date
			parentL.updateViewSpace();			
			
			// mark the collisionVertexes for computation
			lazyComputeBacksidesAndVertexs();
			
			float pc11 = parentL.c11;
			float pc12 = parentL.c12;
			float pc13 = parentL.c13;
			float pc14 = parentL.c14;
			
			float pc21 = parentL.c21;
			float pc22 = parentL.c22;
			float pc23 = parentL.c23;
			float pc24 = parentL.c24;
			
			float pc31 = parentL.c31;
			float pc32 = parentL.c32;
			float pc33 = parentL.c33;
			float pc34 = parentL.c34;
			
			Vertex[] collisionVertexesL = collisionVertexes;
			int collisionVertexLength = collisionVertexesL.length;
			for(int i = 0; i < collisionVertexLength; i++)
			{
				collisionVertexesL[i].calculateViewSpaceCoordinates(pc11,pc12,pc13,pc14,pc21,pc22,pc23,pc24,pc31,pc32,pc33,pc34);
			}
		}
	}
	
	/** package scope helper so that factories can recycle an Items Backsides */
	final Backside[] getBacksides()
	{
		return backsides;
	}
	
	/** package scope helper so that factories can recycle an Items Vertexes */
	final Vertex[] getVertexes()
	{
		return vertexes;
	}
	
	/** package scope helper so that factories can recycle an Items Vertexes */
	final Vertex[] getCollisionVertexes()
	{
		return collisionVertexes;
	}
	
	final public void recycle()
	{
		// tell our parents we are going
		if(parent != null)parent.remove(this);
		itemFactory.recycle(this);
		
		// ensure garbage collector can collect 
		//any none recycled components of this Item
		backsides  = null;
		vertexes = null;
	}

	final private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException
	{
    	out.defaultWriteObject();
    	
    	// write the backsides
    	int backsideLength = backsides.length;
    	out.writeInt(backsideLength);

    	// write the vertexes
    	int vertexesLength = vertexes.length;
    	out.writeInt(vertexesLength);
    	
    	// write the collision vertexes
    	int collisionVertexesLength = collisionVertexes.length;
    	out.writeInt(collisionVertexesLength);

	}

    final private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
    	in.defaultReadObject();
    	
    	SharedImmutableSubItem sisiL = sisi;
    	
    	// read and generate the backsides
    	int backsideLength = in.readInt();
    	backsides = new Backside[backsideLength];
    	ImmutableBackside[] ibs = sisiL.immutableBacksides;
    	for(int i = 0 ; i < backsideLength; i++)
    	{
    		backsides[i] = itemFactory.getBackside(ibs[i]);
    	}
    	
    	// read and generate the vertexes
    	int vertexesLength = in.readInt();
    	vertexes = new Vertex[vertexesLength];
    	ImmutableVertex[] ivs = sisiL.immutableVertexes;
    	for(int i = 0 ; i < vertexesLength; i++)
    	{
    		vertexes[i] = itemFactory.getVertex(ivs[i]);
    	}
    	
    	// read and generate the collisionVertexes
    	int collisonVertexesLength = in.readInt();
    	collisionVertexes = new Vertex[collisonVertexesLength];
    	ImmutableCollisionVertex[] icvs = sisiL.immutableCollisionVertexes;
    	for(int i = 0 ; i < collisonVertexesLength; i++)
    	{
    		collisionVertexes[i] = itemFactory.getVertex(icvs[i]);
    	}   	
	}

	/** returns an Iterator<Vert> for enumerating the Items collision vertexes */
	final public Iterator<Vertex> getCollisionVertexIterator()
	{
		calculateCollisionVertexes();
		return (new Iterator<Vertex>(){
			private int index = 0;
			private int collisionVertexesMax = collisionVertexes.length;

			@Override
			public Vertex next() {
				return collisionVertexes[index++];
			}
			
			@Override
			public boolean hasNext() {
				
				// we only have vertexes if we are visible
				if(!isVisible())return false;
				if(index < collisionVertexesMax)return true;
				return false;			
			}

			@Override
			public void remove() {
				// TODO Auto-generated method stub		
			}		
		});
	}
	
	final public void calculateVertexes()
	{
			Transform parentL = parent;
			// ensure parent Transform is up to date
			parentL.updateViewSpace();			
			
			// mark the Vertexes for computation
			lazyComputeBacksidesAndVertexs();
			
			float pc11 = parentL.c11;
			float pc12 = parentL.c12;
			float pc13 = parentL.c13;
			float pc14 = parentL.c14;
			
			float pc21 = parentL.c21;
			float pc22 = parentL.c22;
			float pc23 = parentL.c23;
			float pc24 = parentL.c24;
			
			float pc31 = parentL.c31;
			float pc32 = parentL.c32;
			float pc33 = parentL.c33;
			float pc34 = parentL.c34;
			
			Vertex[] vertexesL = vertexes;
			int vertexLength = vertexesL.length;
			for(int i = 0; i < vertexLength; i++)
			{
				vertexesL[i].calculateViewSpaceCoordinates(pc11,pc12,pc13,pc14,pc21,pc22,pc23,pc24,pc31,pc32,pc33,pc34);
			}
	}
	

	
	/** ContentGenerator calls this to display all the Item Vertexes */
	final public void renderVertexes(NitrogenContext nc)
	{
		final int colour = 0xFFFFFFFF;
		Vertex[] vertexesL = vertexes;
		int vl = vertexesL.length;
		Vertex vertex;

		for(int index = 0; index < vl; index++)
		{
			vertex = vertexesL[index];
			vertex.rotationNeedsUpdate = true;
			vertex.translationNeedsUpdate = true;	
			
			// enforce near clip plane to prevent divide by zero
			if(-vertex.vs_z < nc.nearClip) return;
			
			vertex.calculateScreenSpaceCoordinate(nc);
			
			renderPixel(nc,vertex.sx,vertex.sy,colour);
			renderPixel(nc,vertex.sx+1,vertex.sy,colour);
			renderPixel(nc,vertex.sx-1,vertex.sy,colour);
			renderPixel(nc,vertex.sx,vertex.sy+1,colour);
			renderPixel(nc,vertex.sx,vertex.sy-1,colour);
		}
	}

	/** ContentGenerator calls this to display a specific Item Vertex*/
	final public void renderVertex(NitrogenContext nc, int index)
	{
		final int colour = 0xFFFF0000;	//red
		Vertex[] vertexesL = vertexes;
		Vertex vertex;
		if(index < 0)return;
		if(index >= vertexesL.length) return;
		vertex = vertexesL[index];
		
		// enforce near clip plane to prevent divide by zero
		if(-vertex.vs_z < nc.nearClip) return;
		
		vertex.rotationNeedsUpdate = true;
		vertex.translationNeedsUpdate = true;		
		vertex.calculateScreenSpaceCoordinate(nc);
		
		renderPixel(nc,vertex.sx,vertex.sy,colour);
		renderPixel(nc,vertex.sx+1,vertex.sy,colour);
		renderPixel(nc,vertex.sx-1,vertex.sy,colour);
		renderPixel(nc,vertex.sx,vertex.sy+1,colour);
		renderPixel(nc,vertex.sx,vertex.sy-1,colour);
	}
	
	final public Vertex getVertex(int index)
	{
		Vertex[] vertexesL = vertexes;
		Vertex vertex;
		if(index >= vertexesL.length) return null;
		vertex = vertexesL[index];
		return vertex;
	}
	
	void renderPixel(NitrogenContext context ,int x,int y,int colour)
	{
		
		int[] nitrogenContextPixels = context.pix;
		int nitrogenContextWidth = context.w;
		int nitrogenContextSize = context.s;	
		int pixelIndex = nitrogenContextWidth * y + x;
		if(
				(pixelIndex >= 0)
				&&(pixelIndex < nitrogenContextSize)
		)
		{
			nitrogenContextPixels[pixelIndex] = colour; 			
		}
	}
	
	/** For ContentGenerator. Finds the vertex with largest sz at approximately the given screen coordinates. Returns null if no suitable vertex is found */
	public int findNearestVertexAt(int x,int y, float nearplane, int size)
	{
		int retval = -1;
		int nearest_z = Integer.MIN_VALUE;
		
		Vertex[] vertexesL = vertexes;
		int vl = vertexesL.length;
		Vertex vertex;
		

		for(int index = 0; index < vl; index++)
		{
			vertex = vertexesL[index];
			
			// ignore vertexes wrong side of the near plane
			// their screen space coordinates will not be valid
			if(-vertex.vs_z < nearplane)continue;
			
			// ignore misses greater than a pixel
			if(vertex.sx > (x + size))continue;
			if(vertex.sx < (x - size))continue;
			if(vertex.sy > (y + size))continue;			
			if(vertex.sy < (y - size))continue;
			
			// remember it if its closest (most +ve z)
			if(vertex.sz > nearest_z)
			{
				nearest_z = vertex.sz;
				retval = index;
			}
		}
		return retval;
	}
	
	/** For ContentGenerator. Finds the vertex with largest sz at approximately the given screen coordinates. Returns null if no suitable vertex is found */
	public int findFurthestVertexAt(int x,int y, float nearplane, int size)
	{
		int retval = -1;
		int furthest_z = Integer.MAX_VALUE;
		
		Vertex[] vertexesL = vertexes;
		int vl = vertexesL.length;
		Vertex vertex;
		

		for(int index = 0; index < vl; index++)
		{
			vertex = vertexesL[index];
			
			// ignore vertexes wrong side of the near plane
			// their screen space coordinates will not be valid
			if(-vertex.vs_z < nearplane)continue;
			
			// ignore misses greater than a pixel
			if(vertex.sx > (x + size))continue;
			if(vertex.sx < (x - size))continue;
			if(vertex.sy > (y + size))continue;			
			if(vertex.sy < (y - size))continue;
			
			// remember it if its farthest (most -ve z)
			if(vertex.sz < furthest_z)
			{
				furthest_z = vertex.sz;
				retval = index;
			}
		}
		return retval;
	}
	
	/** For ContentGenerator. Finds the vertex with largest sz at approximately the given screen coordinates. Returns null if no suitable vertex is found */
	public int findNearestVertexTo(int x,int y, int z)
	{
		int retval = -1;
		float nearestDist = Float.MAX_VALUE;
		
		Vertex[] vertexesL = vertexes;
		int vl = vertexesL.length;
		Vertex vertex;
		

		for(int index = 0; index < vl; index++)
		{
			vertex = vertexesL[index];
			
			float dx = (vertex.getX() - x);
			float dy = (vertex.getY() - y);
			float dz = (vertex.getZ() - z);
			
			float dist = dx * dx + dy * dy + dz * dz;
						
			// remember it if its closest
			if(dist < nearestDist)
			{
				nearestDist = dist;
				retval = index;
			}
		}
		return retval;
	}
	
	/** Method added so content generator can find out if a backside faces the viewer */
	/** called to render the Item 
	 * @param context The NitrogenContext to render the Item in
	 * @param v11-v34 The orientation matrix computed by the scene graph (12 floating point values)*/
	void calculateTestBackside(

			NitrogenContext context,
			// position vector from scene graph
			float v11, float v12, float v13, float v14,
			float v21, float v22, float v23, float v24,
			float v31, float v32, float v33, float v34)
	{
		if(testBackside == null) return;	
				
		testBackside.rotationNeedsUpdate = true;
		testBackside.translationNeedsUpdate = true;

		testBackside.calculate(
						context,
						v11,v12,v13,v14,
						v21,v22,v23,v24,
						v31,v32,v33,v34);
	}
	
	public void renderCollisionVertexes(NitrogenContext nc)
	{
		final int colour = 0xFFFF0000;	//red
		
		if(!hasCollisionVertexes)return;
		if(collisionVertexes == null)return;
		
		Vertex[] collisionVertexesL =  collisionVertexes;
		int length = collisionVertexesL.length;
		
		for(int x = 0; x < length; x++)
		{
			Vertex v = collisionVertexesL[x];
			
			// enforce near clip plane to prevent divide by zero
			if(-v.vs_z < nc.nearClip) continue;
			
			v.rotationNeedsUpdate = true;
			v.translationNeedsUpdate = true;		
			v.calculateScreenSpaceCoordinate(nc);
			
			for(int i = 0; i < 360; i++)
			{
				float dx = v.radius * sinTable[i];
				float dy = v.radius * cosTable[i];
				int scr_x = v.sx + (int)dx;
				int scr_y = v.sy + (int)dy;
				renderPixel(nc,scr_x,scr_y,colour);
			}
		}		
	}
}
