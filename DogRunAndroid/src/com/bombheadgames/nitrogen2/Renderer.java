/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bombheadgames.nitrogen2;

/**
 *
 * @author andrew
 * 
 * 
 */
public interface Renderer {

			public void render(
					final NitrogenContext context,	
					
					final Nitrogen2Vertex leftN2V,
					final Nitrogen2Vertex leftDestN2V,
					
					final Nitrogen2Vertex rightN2V,
					final Nitrogen2Vertex rightDestN2V,			
					final Nitrogen2Vertex stopN2V,
					final int[] polyData,
					final TexMap textureMap,
					final float lightingValue	
					);
			
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
					);
            
            public boolean isTextured();
            public boolean allowsHLP();
            

}
