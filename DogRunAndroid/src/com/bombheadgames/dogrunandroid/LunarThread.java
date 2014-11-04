package com.bombheadgames.dogrunandroid;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;




import android.content.Context;
//import Applet.ActionEvent;
//import Applet.ActionListener;
//import Applet.MyApplet;
// import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.SurfaceHolder;



import com.bombheadgames.nitrogen2.AndroidResourceIndex;
//imports from treerun applet
import com.bombheadgames.nitrogen2.Item;
import com.bombheadgames.nitrogen2.ItemFactory_Default;
import com.bombheadgames.nitrogen2.NitrogenContext;
import com.bombheadgames.nitrogen2.SharedImmutableSubItem;
import com.bombheadgames.nitrogen2.Transform;

	public class LunarThread extends Thread implements AnimationTimerListener{
		private static final int LEAN_RATE = 450;
		
        private static final int STATE_RUNNING = 1;
		private static final int STATE_PAUSE = 2;
		public static final int STATE_READY = 3;
		public static final int STATE_LOSE = 4;
		
		public static final int DIFFICULTY_EASY = 5;
		public static final int DIFFICULTY_MEDIUM = 6;
		public static final int DIFFICULTY_HARD = 7;	
		
		private Nitrogen2Border nitrogenBorder;
        
        /** Handle to the surface manager object we interact with */
        private SurfaceHolder mSurfaceHolder;
        private Handler mHandler;
        
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
        int 		dogrot = 0;
        int 		dogwaggle = 0;
        
        // tree SISI
        SharedImmutableSubItem treeSISI;
        SharedImmutableSubItem groundSISI;
        SharedImmutableSubItem cloudsSISI;
        SharedImmutableSubItem dogheadSISI;
        SharedImmutableSubItem dogheadLeftEarSISI;
        SharedImmutableSubItem dogheadRightEarSISI;
        
        int currentSpeed;
        RenderableText	avTextField;
        RenderableText	maxTextField;
        int average;
        int maximum;
        int averageCount;
        
        int crashcount;
        
        SoundPool sp = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        MediaPlayer[] crashes;
        MediaPlayer currentSound;
        
        AnimationTimer timer;

        /** The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN */
        private int mMode;

        /** Indicate whether the surface has been created & is ready to draw */
        private boolean mRun = false;
		private static Context mContext; // hack so TexMap can acquire resources
		private int mRotating = 0;
		private int mRotation = 0;

		private int hardness = DIFFICULTY_MEDIUM;

        public LunarThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler, LunarView lunarview) {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            lunarview.mHandler = handler;
            this.mHandler = handler;      
            lunarview.mContext = context;
            this.mContext = context;

            // cache handles to our key sprites & other drawables
            // mLanderImage = context.getResources().getDrawable(R.drawable.lander_plain);	
            average = 0;
            maximum = 0;
            averageCount = 0;
            crashcount = 0;
            
            treerunInit();
            
        }
          
        public void treerunInit(){
      	   	setName("Tree Run");
      	   	
      	   	// DONT PUT THESE IN THE CONSTRUCTOR
//      	    System.out.println("image:" + getClass().getResource("/res/Trunk.PNG"));
//      	   	System.out.println("documentbase:" + getDocumentBase());
//      	   	System.out.println("codebase:" + getCodeBase());
      	    //crashes[0] = getAudioClip(getClass().getResource("/res/crash1.wav"));
      	    
      	    crashes = new MediaPlayer[4];
 //           crashes[0] = MediaPlayer.create(mContext, com.bombhead.spaceinvaders2.R.raw.crash1.wav);
      	    
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
        	
        	nitrogenBorder = new Nitrogen2Border(1f, 1f, 1f, 0.5f, 50000f);
        	
        	// scale to an arbitrary size for now
        	nitrogenBorder.scaleToScreen(1, 1);
        	
        	NitrogenContext nitrogenContext = nitrogenBorder.getNitrogenContext();
        	setupNitrogenContext(nitrogenContext);

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

            System.out.println("Rendering");
            nitrogenContext.cls(0xFF0000FF); 
            root.setNeedsTotallyUpdating();
            root.render(nitrogenContext);
            
            currentSpeed = 0;
            timer = new AnimationTimer(20, 2000);
            timer.addListener(this);
            
        	// ************************************
        	// ************************************
        	//         END OF INIT METHOD
        	// ************************************
        	// ************************************
        }
        
        /** Ensure the nitrogen context is set-up */
        private void setupNitrogenContext(NitrogenContext nitrogenContext)
        {
            nitrogenContext.contentGeneratorForcesNoPerspective = false;
            NitrogenContext.lightingAmbient = 0.4f;
        }
        
        
        private SharedImmutableSubItem loadSISI(String resource) throws Exception
        {
        	ObjectInputStream source = null;
        	try{
        		System.out.println("loadSISI resource:" + resource);
        		
        		source = new ObjectInputStream(AndroidResourceIndex.getInputStream(resource, mContext));
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
        
        
        private void orientTree(Transform t,int orientation)
        {
//        	int orientation = (int)(Math.random() * 4f);
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
        
        /**
         * Starts the game, setting parameters for the current difficulty.
         */
        public void doStart() {
            synchronized (mSurfaceHolder) {
                setState(STATE_RUNNING);
            }
        }

        /**
         * Pauses the physics update & animation.
         */
        public void pause() {
            synchronized (mSurfaceHolder) {
                if (mMode == STATE_RUNNING) setState(STATE_PAUSE);
            }
        }

        /**
         * Restores game state from the indicated Bundle. Typically called when
         * the Activity is being restored after having been previously
         * destroyed.
         * 
         * @param savedState Bundle containing the game state
         */
        public synchronized void restoreState(Bundle savedState) {
            synchronized (mSurfaceHolder) {
                setState(STATE_PAUSE);

            }
        }

        @Override
        public void run() {
            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                    	if(timer != null)
                    	{
	                        if (mMode == STATE_RUNNING) 
	                        	timer.process();
	                        doDraw(c);
	                        timer.pad();
                    	}
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
        
        @Override
        public void animationTimerNotify(long interval)
        {
        	updatePhysics(interval);
        }


        /**
         * Dump game state to the provided Bundle. Typically called when the
         * Activity is being suspended.
         * 
         * @return Bundle with this view's state
         */
        public Bundle saveState(Bundle map) {
            synchronized (mSurfaceHolder) {
                if (map != null) {
 //                   map.putInt(KEY_DIFFICULTY, Integer.valueOf(mDifficulty));
                }
            }
            return map;
        }

        /**
         * Used to signal the thread whether it should be running or not.
         * Passing true allows the thread to run; passing false will shut it
         * down if it's already running. Calling start() after this was most
         * recently called with false will result in an immediate shutdown.
         * 
         * @param b true to run, false to shut down
         */
        public void setRunning(boolean b) {
            mRun = b;
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         * 
         * @see #setState(int, CharSequence)
         * @param mode one of the STATE_* constants
         */
        public void setState(int mode) {
            synchronized (mSurfaceHolder) {
                setState(mode, null);
            }
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         * 
         * @param mode one of the STATE_* constants
         * @param message string to add to screen or null
         */
        public void setState(int mode, CharSequence message) {
            /*
             * This method optionally can cause a text message to be displayed
             * to the user when the mode changes. Since the View that actually
             * renders that text is part of the main View hierarchy and not
             * owned by this thread, we can't touch the state of that View.
             * Instead we use a Message + Handler to relay commands to the main
             * thread, which updates the user-text View.
             */
            synchronized (mSurfaceHolder) {
                mMode = mode;

                if (mMode == STATE_RUNNING) {
  //                  Message msg = mHandler.obtainMessage();
  //                  Bundle b = new Bundle();
  //                  b.putString("text", "");
  //                  b.putInt("viz", View.INVISIBLE);
  //                  msg.setData(b);
  //                  mHandler.sendMessage(msg);
                } else {
  //
                }
            }
        }

        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
            	nitrogenBorder.scaleToScreen(width, height);
            }
        }

        /**
         * Resumes from a pause.
         */
        public void unpause() {
            // Move the real time clock up to now
            synchronized (mSurfaceHolder) {
            	if(timer != null)
            	{
            		timer.resume();
            	}
            }
            setState(STATE_RUNNING);
        }

        /** Handle key down */
        boolean doKeyDown(int keyCode, KeyEvent msg) {
            synchronized (mSurfaceHolder) {
            	
                boolean okStart = false;
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) okStart = true;
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) okStart = true;
                if (keyCode == KeyEvent.KEYCODE_S) okStart = true;

                if (okStart && (mMode != STATE_RUNNING))
                {
                	doStart();
                	return true;
                }
                
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_Q) {
                        mRotating  = -1;
                        return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_W)
                {
                        mRotating = 1;
                        return true;
                }
                return false;
            }
            
        }

        /** Handle key up */
        boolean doKeyUp(int keyCode, KeyEvent msg) {
            boolean handled = false;

            synchronized (mSurfaceHolder) {
                if (mMode == STATE_RUNNING) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                    {
                    	mRotating = 0;
                        handled = true;
                    }
                }
            }

            return handled;
        }

        private void doDraw(Canvas canvas) {    	
            System.out.println("Rendering");
            NitrogenContext nitrogenContext = nitrogenBorder.getNitrogenContext();
            if (nitrogenContext == null)return;
			if(currentSpeed < 10)
			{
				nitrogenContext.cls(0xFFFFFFFF);
			}
			else
			{
				//nitrogenContext.cls(0xFF0000FF);
				nitrogenContext.cls(0xFFA47D4C);
				root.render(nitrogenContext);				
			}
			nitrogenBorder.doDraw(canvas);		         
        }

        /**
         * Figures the lander state (x, y, fuel, ...) based on the passage of
         * realtime. Does not invalidate(). Called at the start of draw().
         * Detects the end-of-game and sets the UI to the next state.
         */
        private void updatePhysics(long elapsed) {

			int treeCount = LunarThread.TREE_COUNT;
			Transform[] trees = LunarThread.this.trees;
			
			// rotate the dog
			if(mRotating == 1)
			{
				mRotation += (LEAN_RATE * elapsed)/1000;
				if (mRotation > 450)mRotation = 450;
			}
			if(mRotating ==-1)
			{
				mRotation -= (LEAN_RATE * elapsed)/1000;
				if (mRotation < -450)mRotation = -450;
			}
			
			
			int side = mRotation;
			side = (-side * LunarThread.SIDEWAYS_SPEED)/450;
			boolean crash = false;
			long starttime = System.currentTimeMillis();
			for(int tree = 0; tree < treeCount; tree++)
			{
				Transform treeTrans = trees[tree];
				treeTrans.a34 += currentSpeed;
				treeTrans.a14 += side;
				if(treeTrans.a34 > 0){
					if((treeTrans.a14 > -LunarThread.FATNESS)&&(treeTrans.a14 < LunarThread.FATNESS))crash = true;
					treeTrans.a34 = LunarThread.TREE_ZMAX;
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
			
			int headturn = mRotation;
			headturn = (headturn * DOGTURN_EXTENT) / 450;
			dogheadTurn.setTurn(-900 + headturn);
			dogrot += 100;
			if(dogrot > 1700)dogrot = -1700;
			dogheadTurn.setNeedsTotallyUpdating();
			
			dogwaggle += 1;
			if(dogwaggle == LunarThread.DOGWAG_ARRAY_LENGTH)dogwaggle = 0;
			dogheadWaggle.setRoll(LunarThread.DOGWAG_ARRAY[dogwaggle]);
			dogheadWaggle.setNeedsRotationUpdating();
			
			dogheadLeftEar.setRoll(LunarThread.EARWAG_ARRAY[dogwaggle]);
			dogheadLeftEar.setNeedsRotationUpdating();
			dogheadRightEar.setRoll(-LunarThread.EARWAG_ARRAY[dogwaggle]);
			dogheadRightEar.setNeedsRotationUpdating();
			
			
			if(crash)
			{
				currentSpeed = 0;
				if(currentSound != null)currentSound.stop();
				currentSound = crashes[crashcount];
				//currentSound.play();
				crashcount++;
				if(crashcount >= CRASH_MAX)crashcount = CRASH_MAX - 1;
				
			}
			
			if(currentSpeed < 10)
			{
				currentSpeed++;
			}

	        long endtime = System.currentTimeMillis();
	        int time = (int)(endtime - starttime);
	        average += time;
	        if(time > maximum)maximum = time;
	        
	        averageCount++;
	        if(averageCount == 5)
	        {
	        	average = average/5;
	        	avTextField.setText(Integer.toString(average));
//	        	avTextField.repaint();
	        	maxTextField.setText(Integer.toString(maximum));
//	        	maxTextField.repaint();
	        	maximum = 0;
	        	averageCount = 0;
	        }
        }
        
    	public void setDifficulty(final int difficulty)
    	{
    		hardness  = difficulty;
    	}
    	
    	public static Context getContext()
    	{
    		return mContext;
    	}
    }