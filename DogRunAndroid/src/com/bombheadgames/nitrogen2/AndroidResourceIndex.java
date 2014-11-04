package com.bombheadgames.nitrogen2;

import java.util.HashMap;

import com.bombheadgames.dogrunandroid.LunarLander;
import com.bombheadgames.dogrunandroid.R;
import java.util.Map;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class AndroidResourceIndex {
	
	static Map<String, Integer> resourceMap;
	static Map<String, Integer> typeKey;
	
	static{
		resourceMap = new HashMap<String, Integer>();		
		resourceMap.put("res/clouds.PNG", R.raw.clouds);
	}
	
	static Drawable getDrawable(String path)
	{
		int i = resourceMap.get(path);
		Resources r = LunarLander.getTheResources();
		return r.getDrawable(i);
	}

	

}
