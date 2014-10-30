package com.bombheadgames.nitrogen2;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author andrew
 */

import java.awt.Graphics2D;
import java.awt.Image;
//import java.awt.Component;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TexMap implements Serializable{
	private static final long serialVersionUID = 3915774142992302906L;

	private static Map<String,TexMap> textures;
	
	private String resourceName;
	public transient int[] tex;
    public transient int w, h;
    
    static{ textures = new HashMap<String,TexMap>();}
    
    TexMap(){}
    
    final static TexMap getTexture(String st) throws NitrogenCreationException
    {
    	if(textures.containsKey(st))
    	{
    		return(textures.get(st));
    	}
    	return(new TexMap(st));
    }

    /** altered constructor that reads files rather than embedded resources (but acts as though it was from a resource if serialised to disk)*/
    public TexMap(String resourcePathx, String filex) throws NitrogenCreationException
    { 	
    	String fileName = resourcePathx + filex;
    	File f = new File(fileName);
        if(!f.exists())throw new NitrogenCreationException("TexMap resource " + fileName + " could not be found");
    	Image ii = new javax.swing.ImageIcon(fileName).getImage();
        BufferedImage i = new BufferedImage(ii.getWidth(null),ii.getHeight(null),BufferedImage.TYPE_INT_ARGB);
        Graphics2D osg = i.createGraphics();
        osg.drawImage(ii, 0, 0, null);
        h = i.getHeight();
        w = i.getWidth();
        tex = i.getRGB(0, 0, w, h, null, 0, w);
        
        // In order to refer to (jar file) resources instead of files 
        // (for live environment)ensure we write the file string in unix, 
        // without the file path that ContentGenerator adds to the start
        resourceName = toUnix(filex);   
    }
    
    private TexMap(String st) throws NitrogenCreationException
    { 	
    	URL url = getClass().getResource(st);
        if(url == null)throw new NitrogenCreationException("TexMap resource " + st + " could not be found");
    	Image ii = new javax.swing.ImageIcon(getClass().getResource(st)).getImage();
        BufferedImage i = new BufferedImage(ii.getWidth(null),ii.getHeight(null),BufferedImage.TYPE_INT_ARGB);
        Graphics2D osg = i.createGraphics();
        osg.drawImage(ii, 0, 0, null);
        h = i.getHeight();
        w = i.getWidth();
        tex = i.getRGB(0, 0, w, h, null, 0, w);
        resourceName = st;   
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
    	URL url = getClass().getResource(resourceName);
        if(url == null)throw new IOException("TexMap resource " + resourceName + " could not be found");
    	Image ii = new javax.swing.ImageIcon(getClass().getResource(resourceName)).getImage();
        BufferedImage i = new BufferedImage(ii.getWidth(null),ii.getHeight(null),BufferedImage.TYPE_INT_ARGB);
        Graphics2D osg = i.createGraphics();
        osg.drawImage(ii, 0, 0, null);
        h = i.getHeight();
        w = i.getWidth();
        tex = i.getRGB(0, 0, w, h, null, 0, w);
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
