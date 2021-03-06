package com.bombheadgames.nitrogen2;

import java.io.Serializable;

public class ImmutableVertex implements Serializable{
	private static final long serialVersionUID = 4421645019253980047L;

	// Item-space coordinates
	/** Item space x coordinate. The containing Items orientation transform gets applied to the (usually fixed) Item space coordinates of the vertex in order to generate the vertex's view-space coordinates. */
	public float is_x;
	/** Item space y coordinate. */
	public float is_y;
	/** Item space z coordinate. */
	public float is_z;
    
    public ImmutableVertex(
    		float is_x,
    		float is_y,
    		float is_z		
    		)
    {
    	this.is_x = is_x;
    	this.is_y = is_y;
    	this.is_z = is_z;
    }
    
	
}
