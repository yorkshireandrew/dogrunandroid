package com.bombheadgames.nitrogen2;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author andrew
 */

//import java.awt.Graphics2D;
//import java.awt.Image;
//import java.awt.Component;
//import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;

import com.bombheadgames.dogrunandroid.LunarThread;

public class TexMap implements Serializable{
	private static final long serialVersionUID = 3915774142992302906L;

	private static Map<String,TexMap> textures;
	
	private String resourceName;
	public transient int[] tex;
    public transient int w, h;
    
    static{ textures = new HashMap<String,TexMap>();}
    
    TexMap(){}
    
    final static TexMap getTexture(String st, Context context) throws NitrogenCreationException
    {
    	if(textures.containsKey(st))
    	{
    		return(textures.get(st));
    	}
    	return(new TexMap(st, context));
    }


    /** altered constructor that reads files rather than embedded resources (but acts as though it was from a resource if serialised to disk)*/
 /*
    public TexMap(String resourcePathx, String filex) throws NitrogenCreationException
    { 	
    	String fileName = resourcePathx + filex;
    	System.out.println("TexMap (2pram) =" + fileName);
    	Drawable d = AndroidResourceIndex.getDrawable(fileName);
    	if(d == null)throw new NitrogenCreationException("TexMap resource " + fileName + " could not be found");
    	Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        h = bitmap.getWidth();
        w = bitmap.getHeight();
        tex = new int[h*w];
 //       bitmap.getPixels(pixels, offset, stride, x, y, width, height);
        bitmap.getPixels(tex, 0, w, 0, 0, w, h);
        
        // In order to refer to (jar file) resources instead of files 
        // (for live environment)ensure we write the file string in unix, 
        // without the file path that ContentGenerator adds to the start
        resourceName = toUnix(filex);   
    }
    */

    
    private TexMap(String st, Context context) throws NitrogenCreationException
    { 	
    	String fileName = st;
    	//System.out.println("TexMap (2pram) =" + fileName);
    	Drawable d = AndroidResourceIndex.getDrawable(fileName, context);
    	if(d == null)throw new NitrogenCreationException("TexMap resource " + fileName + " could not be found");
    	Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        h = bitmap.getWidth();
        w = bitmap.getHeight();
        tex = new int[h*w];
 //       bitmap.getPixels(pixels, offset, stride, x, y, width, height);
        bitmap.getPixels(tex, 0, w, 0, 0, w, h);
        
        // In order to refer to (jar file) resources instead of files 
        // (for live environment)ensure we write the file string in unix, 
        // without the file path that ContentGenerator adds to the start
        resourceName = toUnix(st);   
    }
    
    final int getRGB(int x, int y)
    {
        return(tex[(x+y*w)]);
    }
    
    final int[] getTex()
    {
        return tex;
    }
    
    final int getWidth()
    {
        return w;
    }

    final int getHeight()
    {
        return h;
    }
      
    final private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	in.defaultReadObject();
    	if(textures.containsKey(resourceName))
    		{
    			TexMap loadedTexture = textures.get(resourceName);
    			tex = loadedTexture.tex;
       			h = loadedTexture.h;
       			w = loadedTexture.w;
       		    return; // we already have loaded the texture
    		}
//    	URL url = getClass().getResource(resourceName);
    	//System.out.println("TRYING TO LOAD TexMap url:" + resourceName);
    	Context context = LunarThread.getContext();
    	if(context == null)System.out.println("OH NO NO CONTEXT!");
    	Drawable d = AndroidResourceIndex.getDrawable(resourceName, context);
    	if(d == null)System.out.println("TexMap resource " + resourceName + " could not be found");
    	Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        w = bitmap.getWidth();
        h = bitmap.getHeight();
        tex = new int[h*w];
        bitmap.getPixels(tex, 0, w, 0, 0, w, h);
        resourceName = toUnix(resourceName);
        //System.out.println("" + resourceName + ":" + w + ":" + h);
        
//        String blah = "";
//       for(int x = 0; x < w; x++)
//        {
//        	blah += Integer.toHexString(tex[x]) + ",";
//        }
//        System.out.println(blah);
        
        textures.put(resourceName, this);
    }
    
    /** Purges the collection of loaded textures, All SharedImmutableSubItems that use textures must be reloaded */ 
    final public void purgeTextures()
    {
    	// create a new empty hashmap
    	textures = new HashMap<String,TexMap>();
    }
    
    private String toUnix(String in)
    {
    	String retval = in.replace('\\', '/');
    	return retval;
    }
}
