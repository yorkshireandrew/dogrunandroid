package com.bombheadgames.nitrogen2;

import java.io.Serializable;

public class ImmutableBackside implements Serializable{
	private static final long serialVersionUID = -7547901468152037764L;

	// view-space offset from the Items origin
	public final float ix, iy, iz;
	
	// the backsides normal coordinates
	public final float inx, iny, inz;
	
	// Causes the backside to perform additional calculations used for lighting
	public final boolean calculateLighting;
	
	public ImmutableBackside(
			final float ix,
			final float iy,
			final float iz,
			final float inx,
			final float iny,
			final float inz,
			boolean calculateLighting)
			{
				this.ix = ix;
				this.iy = iy;
				this.iz = iz;
				this.inx = inx;
				this.iny = iny;
				this.inz = inz;
				this.calculateLighting = calculateLighting;
			}
	
	/** used by content generator to obtain a flipped immutable backside */
	public ImmutableBackside flippedImmutableBackside()
	{
		return new ImmutableBackside
				(
						ix,
						iy,
						iz,
						-inx,
						-iny,
						-inz,
						calculateLighting						
						);
	}
}
