package com.bombheadgames.nitrogen2;

import java.io.InputStream;
import java.util.HashMap;

import com.bombheadgames.dogrunandroid.R;

import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class AndroidResourceIndex {
	
	static Map<String, Integer> resourceMap;
	static Map<String, Integer> typeKey;
	
	static{
		resourceMap = new HashMap<String, Integer>();	
		resourceMap.put("/res/clouds1.nit", R.raw.clouds1);	
		resourceMap.put("/res/clouds10.nit", R.raw.clouds10);	
		resourceMap.put("/res/doghead4bignoculling.nit", R.raw.doghead4bignoculling);	
		resourceMap.put("/res/groundtex2.nit", R.raw.groundtex2);	
		resourceMap.put("/res/leftear1.nit", R.raw.leftear1);
		resourceMap.put("/res/rightear.nit", R.raw.rightear);
		resourceMap.put("/res/rightear1.nit", R.raw.rightear1);
		resourceMap.put("/res/tree3.nit", R.raw.tree3);	
		
		resourceMap.put("/res/clouds.PNG", R.drawable.clouds);
		resourceMap.put("/res/ground2.PNG", R.drawable.ground2);
		resourceMap.put("/res/leaf.PNG", R.drawable.leaf);
		resourceMap.put("/res/leaf2.PNG", R.drawable.leaf2);	
		resourceMap.put("/res/Trunk.PNG", R.drawable.trunk);
		resourceMap.put("/res/Trunk2.PNG", R.drawable.trunk2);
	}
	
	public static Drawable getDrawable(String path, Context context)
	{
		//System.out.println("ARI getDrawable:" + path);
		int i = resourceMap.get(path);
		Resources r = context.getResources();
		return r.getDrawable(i);
	}
	
	public static InputStream getInputStream(String path, Context context)
	{
		//System.out.println("ARI getInputStream:" + path);
		//if (resourceMap == null)System.out.println("ARI WOT NO RESOURCE MAP!");
		int i = resourceMap.get(path);
		Resources r = context.getResources();
		//if (r == null)System.out.println("WOT NO RESOURCES");
		return r.openRawResource(i);
	}

	

}
