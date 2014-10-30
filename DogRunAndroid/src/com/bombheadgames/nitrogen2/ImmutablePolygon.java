package com.bombheadgames.nitrogen2;

import java.io.Serializable;

/** Encapsulates the immutable details about a polygon so that they can be shared across several Item instances */
public class ImmutablePolygon implements Serializable{
	private static final long serialVersionUID = 3401874576594754016L;
	
	/** index of vertexes of the polygon in the Items vertexes array */
	int[] vertexIndexArray;
	
	/** The vertex data to be associated with the polygons vertexes */
	PolygonVertexData[] polygonVertexDataArray;
	
	/** Information to pass to the renderer, for example the polygons colour */
	int[] polyData;
	
	/** The renderer triplet to use to render the polygon */
	RendererTriplet rendererTriplet;
	
	/** The texture map */
	TexMap textureMap;
	
	/** The backside index to use to determine which side is being viewed */
	int backsideIndex;
	
	/** True if the polygon uses backside culling */
	boolean isBacksideCulled;
	
	/** True if the polygon is transparent, and gets rendered during a transparent render pass */
	boolean isTransparent;
	
	public ImmutablePolygon(
			int[] vertexIndexArray,
			PolygonVertexData[] polygonVertexDataArray,
			int[] polyData,
			RendererTriplet rendererTriplet,
			TexMap textureMap,
			int backsideIndex,
			boolean isBacksideCulled,
			boolean isTransparent)
			{
				this.vertexIndexArray = vertexIndexArray;
				this.polygonVertexDataArray = polygonVertexDataArray;
								
				this.polyData=polyData;
				this.rendererTriplet = rendererTriplet;
				this.textureMap=textureMap;
				this.backsideIndex=backsideIndex;
				this.isBacksideCulled=isBacksideCulled;
				this.isTransparent=isTransparent;
			}
	
	/** Selects a Renderer from the Polygons RendererTriplet based on the whichRenderer parameter 
	 * @return The selected renderer*/
	final Renderer getRenderer(final int whichRenderer, final boolean isPicking)
	{
		if(isPicking)return(RendererTriplet.pickingRenderer);
		return(rendererTriplet.getRenderer(whichRenderer));
	}
}
