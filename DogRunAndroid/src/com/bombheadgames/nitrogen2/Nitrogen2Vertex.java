package com.bombheadgames.nitrogen2;

final public class Nitrogen2Vertex {
	static final float VIEWSPACE_XY_MULTIPLIER = 1000;
	static final float VIEWSPACE_Z_MULTIPLIER = 200;
	static final float TEXTURE_MULTIPLIER = 1 << 20;
	
	/** use floating point for clipping */
	float vsX;
	float vsY;
	float vsZ;
	  
	/** screen x coordinate */
    transient int intSX;
    /** screen y coordinate */
    transient int intSY;
    /** screen z coordinate (which may differ from the vertexes view-space z due to perspective projection)*/
    transient int intSZ;
    
    /** texture coordinates */
    transient int intTX;
    transient int intTY;
       
    /** next Nitrogen2PolygonRenderer in LLL moving clockwise */
    Nitrogen2Vertex clockwise;
    /** next Nitrogen2PolygonRenderer in LLL moving anticlockwise */
    Nitrogen2Vertex anticlockwise;

    /** initializes Nitrogen2Vertex from a vertex, appropriately 
     * for the polygon. passing back SY
     */
	public int initialize(Vertex v, boolean polygonUntextured,
			boolean polygonIsHLP) {
		
			if(polygonUntextured)
			{
				intSX 		= v.sx;
				int intSYL 	= v.sy;
				intSY 		= intSYL;
				intSZ 		= v.sz;
				return intSYL;
			}
			else
			{
				intSX 		= v.sx;
				int intSYL 	= v.sy;
				intSY 		= intSYL;
				intSZ 		= v.sz;
				
				intTX = (int)(v.aux1 * TEXTURE_MULTIPLIER);
				intTY = (int)(v.aux2 * TEXTURE_MULTIPLIER);
				
				if(polygonIsHLP)
				{
					vsX = v.vs_x;
					vsY = v.vs_y;
					vsZ = v.vs_z;	
				}
				return intSYL;
			}
	}
	
	public String toString()
	{
		String s = super.toString();
		s = s + "[" + intSX + "," + intSY +"]";
		return s;
	}
}
