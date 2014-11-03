/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Applet;

// 

/**
 *
 * @author andrew
 */


import java.awt.*;  // needed for Dimension class
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;

import javax.swing.*;
import javax.swing.event.*;

import com.bombheadgames.nitrogen2.Item;
import com.bombheadgames.nitrogen2.ItemFactory_Default;
import com.bombheadgames.nitrogen2.NitrogenContext;
import com.bombheadgames.nitrogen2.SharedImmutableSubItem;
import com.bombheadgames.nitrogen2.Transform;

import java.applet.AudioClip;


final public class MyApplet extends JApplet{
	private static final long serialVersionUID = 1799576836511527595L;

	static final int TREE_COUNT 		= 100;
	static final int TREE_XMAX 			= 8000;
	static final int TREE_ZMAX 			= -8000;
	
	static final int APP_WIDTH 			= 330;
    static final int APP_HEIGHT 		=550;
    static final int LABEL_WIDTH 		= 80;
    
    static final int ANIMATION_DELAY 	= 25;
    static final int SPEED 				= 50;
    static final int SIDEWAYS_SPEED		= 50;
    static final int FATNESS			= 50;
    
    static final int DOGTURN_EXTENT 	= 400;
    
    static final int DOGWAG_ARRAY_LENGTH = 38;
    static final int[] DOGWAG_ARRAY		= {
    0, 		20, 	40, 	60,		80, 	100, 	120, 	140, 	160, 	180, 
    200, 	180,	160,	120,	100,	80,		60,		40,		20,		0,
    -20, 	-40, 	-60,	-80, 	-100, 	-120, 	-140, 	-160, 	-180, 
    -200, 	-180,	-160,	-120,	-100,	-80,	-60,	-40,	-20	
    };
    
    static final int[] EARWAG_ARRAY		= {
    0, 		0, 		0, 		0,		0, 		0,	0,	0,	20, 	40, 	60, 	80, 	100, 
    200, 	400,	600,	750,	600,	500,	400,	200,	150,	100,
    100, 	100, 	100,	100, 	100, 	100, 	100, 	100, 	100, 
    0, 		0,		0,		0,		0,		0	
    };
    
    static final int CRASH_MAX = 4;

    Transform root;
      
    // transforms for orientation
    Transform dogslant;

    // world
    Transform[] trees;
    int[] 		treeX;
    int[] 		treeZ;
    
    Transform	ground;
    Transform	clouds;
    int			groundX;
    int			groundZ;
    
    Transform	dogheadPosition;
    Transform	dogheadTurn;
    Transform	dogheadWaggle;
    Transform	dogheadLeftEar;
    Transform	dogheadRightEar;
    int dogrot = 0;
    int dogwaggle = 0;
    
    // tree SISI
    SharedImmutableSubItem treeSISI;
    SharedImmutableSubItem groundSISI;
    SharedImmutableSubItem cloudsSISI;
    SharedImmutableSubItem dogheadSISI;
    SharedImmutableSubItem dogheadLeftEarSISI;
    SharedImmutableSubItem dogheadRightEarSISI;
    
    //timer
    Timer timer;
    final JSlider dogslantSlider;
    final NitrogenContext nitrogenContext;
    int currentSpeed;
    JTextField	avTextField;
    JTextField	maxTextField;
    int average;
    int maximum;
    int averageCount;
    
    int crashcount;
    AudioClip[] crashes;
    AudioClip currentSound;
    
    public MyApplet()
    {
    	dogslantSlider = new JSlider(); 
        nitrogenContext = new NitrogenContext(300,300,1f,1f,0.05f,50000); 	
        average = 0;
        maximum = 0;
        averageCount = 0;
        crashcount = 0;
        
        crashes = new AudioClip[CRASH_MAX];
//        crashes[0] = getAudioClip(getCodeBase(),"res/crash1.wav");
//        crashes[1] = getAudioClip(getCodeBase(),"crash2.wav");
//        crashes[2] = getAudioClip(getCodeBase(),"crash3.wav");
//        crashes[3] = getAudioClip(getCodeBase(),"crash4.wav");

//       crashes[0] = getAudioClip(getCodeBase(), "/res/crash1.wav");

    }
    
    public void init(){
  	   	setName("Tree Run");
  	   	
  	   	// DONT PUT THESE IN THE CONSTRUCTOR
  	    System.out.println("image:" + getClass().getResource("/res/Trunk.PNG"));
  	   	System.out.println("documentbase:" + getDocumentBase());
  	   	System.out.println("codebase:" + getCodeBase());
  	    crashes[0] = getAudioClip(getClass().getResource("/res/crash1.wav"));
  	    crashes[1] = getAudioClip(getClass().getResource("/res/crash2.wav"));
  	    crashes[2] = getAudioClip(getClass().getResource("/res/crash3.wav"));
  	    crashes[3] = getAudioClip(getClass().getResource("/res/crash4.wav"));
  	    try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
  	   	
  	   	root = new Transform(
  			null,
  			1f, 0f, 0f, 0f,
  			0f, 1f, 0f, 0f,
  			0f, 0f, 1f, 0f);

  	   	dogslant = new Transform(root);
    	dogslant.setUnity();
    	
    	// create arrays for trees
    	trees = new Transform[TREE_COUNT];
    	treeX = new int[TREE_COUNT];
    	treeZ = new int[TREE_COUNT];
    	
    	// create transform for ground
    	groundX = 0;
    	groundZ = 0;
    	ground = new Transform(
   				dogslant,
   				1f, 0f, 0f, 0f,
   				0f, 1f, 0f, -160f,
   				0f, 0f, 1f, -8000f);
    	
    	clouds = new Transform(
   				dogslant,
   				1f, 0f, 0f, 0f,
   				0f, 1f, 0f, 0f,
   				0f, 0f, 1f, -6000f);
    	
    	dogheadPosition = new Transform(
   				root,
   				.5f, 0f, 0f, 0f,
   				0f, .5f, 0f, -120f,
   				0f, 0f, .5f, -120f);
    	

    	
    	dogheadTurn = new Transform(dogheadPosition);
    	dogheadTurn.setUnity();
    	dogheadWaggle = new Transform(dogheadTurn);
    	dogheadWaggle.setUnity();
    	Transform dogheadLeftEarPosition = new Transform(
    			dogheadWaggle,
   				0f, 0f, 1f, 0f,
   				0f, 1f, 0f, 116f,
   				-1f, 0f, 0f, -22f);
    	
    	Transform dogheadRightEarPosition = new Transform(
    			dogheadWaggle,
   				0f, 0f, 1f, 0f,
   				0f, 1f, 0f, 116f,
   				-1f, 0f, 0f, 022f);
    	
    	dogheadLeftEar = new Transform(dogheadLeftEarPosition);
    	dogheadLeftEar.setUnity();
    	dogheadLeftEar.setRoll(450);
    	dogheadRightEar = new Transform(dogheadRightEarPosition);
    	dogheadRightEar.setUnity();
    	dogheadRightEar.setRoll(-450);
    	
    	// create transforms to attach trees to
    	for(int tree = 0; tree < TREE_COUNT; tree++)
    	{
       		int treex = (int)(((double)TREE_XMAX) * (Math.random() - Math.random()));
       		int treez = (int)(((double)TREE_ZMAX)  * (Math.random() * 0.9f + 0.1f));
       	    Transform treeTransform = new Transform(dogslant);
       	    treeTransform.setUnity();
       	    orientTree(treeTransform, (int)(Math.random() * 4f));
       	    treeTransform.a14 = (float)treex;
       	    treeTransform.a34 = (float)(treez);
       	    treeTransform.setNeedsTotallyUpdating(); 	    
       	    trees[tree] = treeTransform;
    	}
    	
    	// create a nitrogen context to render stuff into
        nitrogenContext.contentGeneratorForcesNoPerspective = false;
        NitrogenContext.lightingAmbient = 0.4f;

    	// ************************************
    	// ************************************
    	//      CREATE SISI OBJECTS
    	// ************************************
    	// ************************************       
        System.out.println("creating SISI");   		
    	try
    	{
    		System.out.println("reading tree");
    		treeSISI 	= loadSISI("/res/tree3.nit");
    		System.out.println("reading ground");
    		groundSISI 	= loadSISI("/res/groundtex2.nit");
    		System.out.println("reading clouds");
    		cloudsSISI 	= loadSISI("/res/clouds10.nit");	
    		System.out.println("reading head");
    		dogheadSISI = loadSISI("/res/doghead4bignoculling.nit");
    		System.out.println("reading left ear");
    		dogheadLeftEarSISI = loadSISI("/res/leftear1.nit");	
    		System.out.println("reading right ear");
    		dogheadRightEarSISI = loadSISI("/res/rightear1.nit");	
    	}
    	catch(Exception e)
    	{
    		System.out.println("Error reading SISI files");
    		e.printStackTrace();
    		return;
    	}
    	
    	// put trees on transforms
    	Item.setItemFactory(new ItemFactory_Default());
    	for(int tree = 0; tree < TREE_COUNT; tree++)
    	{
    		Item treeItem 	= Item.createItem(treeSISI, trees[tree]);
    		treeItem.setVisibility(true);
    	}
    	
    	// put clouds and ground on transforms
		Item groundItem 	= Item.createItem(groundSISI, ground);
		groundItem.setVisibility(true);
		Item cloudsItem 	= Item.createItem(cloudsSISI, clouds);
		cloudsItem.setVisibility(true);
		Item dogheadItem 	= Item.createItem(dogheadSISI, dogheadWaggle);
		dogheadItem .setVisibility(true);
		
		//put ears on dog
		Item leftEarItem 	= Item.createItem(dogheadLeftEarSISI, dogheadLeftEar);
		leftEarItem.setVisibility(true);
		dogheadLeftEar.setNeedsTotallyUpdating();
		Item rightEarItem 	= Item.createItem(dogheadRightEarSISI, dogheadRightEar);
		rightEarItem.setVisibility(true);
		dogheadRightEar.setNeedsTotallyUpdating();

    	dogslantSlider.setModel(new DefaultBoundedRangeModel(0,1,-450,450));
    	dogslantSlider.setMinorTickSpacing(450);
    	dogslantSlider.setPaintTicks(true);
    	dogslantSlider.addChangeListener(
    			new ChangeListener()
    			{

					@Override
					public void stateChanged(ChangeEvent arg0) {
						dogslant.setRoll(dogslantSlider.getModel().getValue());
//				        nitrogenContext.cls(0xFF0000FF);  
//				        root.setNeedsTotallyUpdating();
//				        root.render(nitrogenContext);
//				        nitrogenContext.repaint();		
					}			
    			}
    	);
    	Box dogslantSliderBox = Box.createHorizontalBox();
    	dogslantSliderBox.add(new FixedWidthLabel("DOG SLANT", LABEL_WIDTH));
    	dogslantSliderBox.add(dogslantSlider);
    	dogslantSliderBox.add(Box.createHorizontalGlue());
    		
    	// ************************************
    	// ************************************
    	//         CREATE USER INTERFACE
    	// ************************************
    	// ************************************
    	
    	Box controls = Box.createVerticalBox();
    	controls.add(dogslantSliderBox);
    	
        avTextField = new JTextField();
        maxTextField = new JTextField();
        
        Box avBox = Box.createHorizontalBox();
        avBox.add(new JLabel("Average"));
        avBox.add(avTextField);
        avBox.add(Box.createHorizontalGlue());
        Box maxBox = Box.createHorizontalBox();
        
        maxBox.add(new JLabel("Max"));
        maxBox.add(maxTextField);
        maxBox.add(Box.createHorizontalGlue());
 //       controls.add(avBox);
 //       controls.add(maxBox);
    	
    	Box outerControls = Box.createHorizontalBox();
    	outerControls.add(Box.createHorizontalGlue());
    	outerControls.add(controls);
    	outerControls.add(Box.createHorizontalGlue());
    	
    	Box userInterfaceBox = Box.createVerticalBox();
    	userInterfaceBox.add(nitrogenContext);
    	userInterfaceBox.add(Box.createVerticalStrut(5));
    	userInterfaceBox.add(outerControls);
    	       
    	
        getContentPane().add(userInterfaceBox);
        getContentPane().validate();
        getContentPane().setVisible(true);
        System.out.println("Rendering");
        nitrogenContext.cls(0xFF0000FF); 
        root.setNeedsTotallyUpdating();
        root.render(nitrogenContext);
        nitrogenContext.repaint();
        
        currentSpeed = 0;
        timer = new Timer(ANIMATION_DELAY, new TimerHandler());
        timer.start();
        
    	// ************************************
    	// ************************************
    	//         END OF INIT METHOD
    	// ************************************
    	// ************************************
    }
    
    private SharedImmutableSubItem loadSISI(String resource) throws Exception
    {
    	ObjectInputStream source = null;
    	try{ 				
    		source = new ObjectInputStream(this.getClass().getResourceAsStream(resource));
    		return ((SharedImmutableSubItem) source.readObject());
    	}
    	catch (FileNotFoundException e) {
    		throw(e);	
    	}
		catch (IOException e) {
			throw(e);
		}
    	catch (ClassNotFoundException e) 
    	{
    		throw(e);
    	}
    	finally
    	{
    		if(source != null)source.close();
    	}
	}       	
 

    @Override
        public Dimension getMinimumSize()
    {
        System.out.printf("getMinimumSize");
        return new Dimension(APP_WIDTH,APP_HEIGHT);
    }

    @Override
    public Dimension getPreferredSize()
    {
        System.out.printf("getPreferredSize");
        return new Dimension(APP_WIDTH,APP_HEIGHT);
    }

    @Override
    public Dimension getMaximumSize()
    {
        System.out.printf("getMaximumSize");
        return new Dimension(APP_WIDTH,APP_HEIGHT);
    }
    
    private void orientTree(Transform t,int orientation)
    {
//    	int orientation = (int)(Math.random() * 4f);
    	if(orientation == 0)
    	{
    		t.a11 = 1f; t.a12=0f; t.a13=0f;
    		t.a21 = 0f; t.a22=1f; t.a23=0f;
    		t.a31 = 0f; t.a32=0f; t.a33=1f;
    	}
    	
    	if(orientation == 1)
    	{
    		t.a11 = 0f; t.a12=0f; t.a13=-1f;
    		t.a21 = 0f; t.a22=1f; t.a23=0f;
    		t.a31 = 1f; t.a32=0f; t.a33=0f;
    	}
    	
    	if(orientation == 2)
    	{
    		t.a11 = -1f; t.a12=0f; t.a13=0f;
    		t.a21 = 0f; t.a22=1f; t.a23=0f;
    		t.a31 = 0f; t.a32=0f; t.a33=-1f;
    	}
    	
    	if(orientation == 3)
    	{
    		t.a11 = 0f; t.a12=0f; t.a13=1f;
    		t.a21 = 0f; t.a22=1f; t.a23=0f;
    		t.a31 = -1f; t.a32=0f; t.a33=0f;
    	}
    }
    
    private class TimerHandler implements ActionListener
    {

		@Override
		public void actionPerformed(ActionEvent e) {
			int treeCount = MyApplet.TREE_COUNT;
			Transform[] trees = MyApplet.this.trees;
			int side = MyApplet.this.dogslantSlider.getModel().getValue();
			side = (-side * MyApplet.SIDEWAYS_SPEED)/450;
			boolean crash = false;
			long starttime = System.currentTimeMillis();
			for(int tree = 0; tree < treeCount; tree++)
			{
				Transform treeTrans = trees[tree];
				treeTrans.a34 += currentSpeed;
				treeTrans.a14 += side;
				if(treeTrans.a34 > 0){
					if((treeTrans.a14 > -MyApplet.FATNESS)&&(treeTrans.a14 < MyApplet.FATNESS))crash = true;
					treeTrans.a34 = MyApplet.TREE_ZMAX;
					treeTrans.a14 =(int)(((double)TREE_XMAX) * (Math.random() - Math.random()));
				}
//				treeTrans.setNeedsTranslationUpdating();
				treeTrans.setNeedsTotallyUpdating();
			}
			
			Transform groundT = ground;
			groundT.a14 += side;
			if(groundT.a14 > 2000)groundT.a14 -= 2000;
			if(groundT.a14 < -2000)groundT.a14 += 2000;
			groundT.a34 += currentSpeed;
			if(groundT.a34 > -6000)groundT.a34 -= 2000;
			groundT.setNeedsTranslationUpdating();
			
			int headturn = MyApplet.this.dogslantSlider.getModel().getValue();
			headturn = (headturn * MyApplet.DOGTURN_EXTENT) / 450;
			dogheadTurn.setTurn(-900 + headturn);
			dogrot += 100;
			if(dogrot > 1700)dogrot = -1700;
			dogheadTurn.setNeedsTotallyUpdating();
			
			dogwaggle += 1;
			if(dogwaggle == MyApplet.DOGWAG_ARRAY_LENGTH)dogwaggle = 0;
			dogheadWaggle.setRoll(MyApplet.DOGWAG_ARRAY[dogwaggle]);
			dogheadWaggle.setNeedsRotationUpdating();
			
			dogheadLeftEar.setRoll(MyApplet.EARWAG_ARRAY[dogwaggle]);
			dogheadLeftEar.setNeedsRotationUpdating();
			dogheadRightEar.setRoll(-MyApplet.EARWAG_ARRAY[dogwaggle]);
			dogheadRightEar.setNeedsRotationUpdating();
			
			
			if(crash)
			{
				currentSpeed = 0;
				if(currentSound != null)currentSound.stop();
				currentSound = crashes[crashcount];
				currentSound.play();
				crashcount++;
				if(crashcount >= CRASH_MAX)crashcount = CRASH_MAX - 1;
				
			}
			if(currentSpeed < 10)
			{
				currentSpeed++;
				nitrogenContext.cls(0xFFFFFFFF);
			}
			else
			{
				if(currentSpeed < MyApplet.SPEED)currentSpeed++;
				nitrogenContext.cls(0xFF0000FF);
				nitrogenContext.cls(0xFFA47D4C);
		        MyApplet.this.root.render(nitrogenContext);				
			}
	        nitrogenContext.repaint();
	        long endtime = System.currentTimeMillis();
	        int time = (int)(endtime - starttime);
	        average += time;
	        if(time > maximum)maximum = time;
	        
	        averageCount++;
	        if(averageCount == 5)
	        {
	        	average = average/5;
	        	avTextField.setText(Integer.toString(average));
	        	avTextField.repaint();
	        	maxTextField.setText(Integer.toString(maximum));
	        	maxTextField.repaint();
	        	maximum = 0;
	        	averageCount = 0;
	        }
		}	
    }
       
}// end of MyApplet
*/