package edu.lehigh.cse.dan.wyatt.factoryrunner;

import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;

import android.util.Log;

/**
 * Demonstration of the GameFramework
 * 
 * Note: you must set your target to Android 2.2 or later
 * 
 * Note: you must also be sure that your manifest includes WAKE_LOCK and VIBRATE
 * permissions
 * 
 * @author spear
 */
public class SampleActivity extends Framework
{
    /**
     * Constants for dimensions of decorations
     */
	private static final int SHELF_HEIGHT = 125;
	private static final int SHELF_WIDTH = 161;
	private static final int I_BEAM_HEIGHT = 150;
	private static final int I_BEAM_WIDTH = 91;
	private static final int CHAINS_HEIGHT = 77;
	private static final int CHAINS_WIDTH = 200;
	private static final int BACKGROUND_WIDTH = 290;
	private static final int PARALLAX_FACTOR = 4;
	public static final int INFINITE_LEVEL_LENGTH = 2*BACKGROUND_WIDTH*PARALLAX_FACTOR;
	public static final int CAMERA_WIDTH = 460;
	
	/**
     * Every game must provide this to specify the height of the game scene
     */
    public int getCameraHeight()
    {
        return 320;
    }

    /**
     * Every game must provide this to specify the width of the game scene
     */
    public int getCameraWidth()
    {
        return CAMERA_WIDTH;
    }

    /**
     * Every game must provide this to specify the game orientation
     */
    public ScreenOrientation getCameraOrientation()
    {
        return ScreenOrientation.LANDSCAPE;
    }

    /**
     * Every game must provide this to specify the number of levels
     */
    public int getNumLevels()
    {
        return 10;
    }

    /**
     * Every game must declare how many help scenes it has
     */
    public int getHelpScenes()
    {
        return 2;
    }

    private float timeStart;
    /**
     * every game must provide this to load images and sounds when starting the
     * game
     */
    @Override
    public void onLoadResources()
    {
        Media.registerSound("badsound.ogg");
        Media.registerSound("goodsound.ogg");
        Media.registerMusic("gametune.ogg", true);
        Media.registerImage("greenball.png");
        Media.registerImage("blueball.png");
        Media.registerImage("redball.png");
        Media.registerImage("mustardball.png");
        Media.registerImage("purpleball.png");
        Media.registerImage("greyball.png");
        Media.registerImage("invis.png");
        Media.registerImage("red.png");
        Media.registerImage("back.png");
        Media.registerImage("mid.png");
        Media.registerImage("front.png");
        Media.registerAnimatableImage("winkie.png", 2, 1);
        
        // Art from Kevin
        Media.registerAnimatableImage("hero.png", 6, 1);
        Media.registerAnimatableImage("boss.png", 2, 1);
        Media.registerImage("background.png");
        Media.registerImage("chains.png");
        Media.registerImage("downbox.png");
        Media.registerImage("forklift.png");
        Media.registerImage("ground.png");
        Media.registerImage("ground_hole.png");
        Media.registerImage("i-beam.png");
        Media.registerImage("ramp.png");
        Media.registerImage("shelf.png");
        Media.registerImage("trapdoor.png");
        Media.registerImage("upbox.png");
        Media.registerImage("plainbox.png");
        Media.registerImage("level_select.png");
        Media.registerImage("metal_bg.png");
        Media.registerImage("splash_text.png");
        Media.registerImage("splash.png");
    }

    private void addDecorations(int start, int end, int num) {
    	if(num > 0)
	    	for(int x = start; x < end; x += (end-start)/num) {
	    		int rnd = (int) (Math.random()*3);
	    		Log.d("DECORATE", "rnd: " + rnd + ", x:" + x);
	    		switch(rnd) {
	    		case 0:
	    			Level.addDecoration(x, 0, CHAINS_WIDTH, CHAINS_HEIGHT, "chains.png");
	    			break;
	    		case 1:
	    			Level.addDecoration(x, 0, I_BEAM_WIDTH, I_BEAM_HEIGHT, "i-beam.png");
	    			break;
	    		case 2:
	    			Level.addDecoration(x, Level.FLOOR_TOP-SHELF_HEIGHT, SHELF_WIDTH, SHELF_HEIGHT, "shelf.png");
	    			break;
	    		}
	    	}
    }

    private void prepareStart(int length, String message, int upbox, int downbox, int trapdoor, int ramp) {
		Level.reset(length, getCameraHeight(), 0, 0, false, 0, 10);
		Level.addBackgroundLayer("background.png", -1f/PARALLAX_FACTOR, 0, 0);
		
		if(message != null) {
			PopUpScene.printTimedMessage(message, 2, upbox, downbox, trapdoor, ramp);
		}
		
		Obstacle.addSquareObstacle(0, Level.FLOOR_TOP, length, Level.FLOOR_HEIGHT, "ground.png", null, 0, 1, 0, 1);
		
		// Do not put decorations in the first or last frame of the game so
		// the infinite level transitions are less perceptible
		addDecorations(getCameraWidth(), length-getCameraWidth(), length/2000);
	}
    
    private void prepareEnd(int length, int speed){
    	// Add the hero here so it is on top of the decorators, etc.
		final Hero h = Hero.addHero(0, Level.FLOOR_TOP-Hero.HEIGHT, Hero.WIDTH, Hero.HEIGHT, "hero.png", 1, 0, 0);
		h.setVelocity(speed, 0);
		h.animate(120, true);
		Hero.addRabbit(200, 160, speed, 0);
    	
    	Destination.addDestination(length - 50, Level.FLOOR_TOP-Hero.HEIGHT, 20, Hero.HEIGHT, "invis.png", 1, 0);
        Level.setVictoryDestination(1);
    }
    
    private void prepareEnd(int length) {
    	prepareEnd(length, 5);
    }
    
    /**
     * Every game must provide this to actually create the levels
     */
    public void configureLevel(int whichLevel)
    {
    	
    	if (whichLevel == 1) {
    		
    		int length = 900;
    		prepareStart(length, "Swipe boxes up \n  to clear path", 1, 0, 0, 0);
    		
    		Obstacle.addUpboxObstacle(300);
    		Obstacle.addUpboxObstacle(500);
    		Obstacle.addUpboxObstacle(700);
    		
    		prepareEnd(length);	
    	}
    	
    	else if (whichLevel == 2) {
    		
    		int length = 900;
    		prepareStart(length, "Swipe boxes down \n     to cover pits", 0, 1, 0, 0);
    		
    		Obstacle.addDownboxObstacle(400);
    		Obstacle.addDownboxObstacle(600);
    		Obstacle.addDownboxObstacle(800);
    		
    		prepareEnd(length);		
		}

		else if (whichLevel == 3) {

			int length = 900;
			prepareStart(length, "Swipe trapdoor right \n      to kill enemy", 0, 0, 1, 0);

			Obstacle.addTrapdoorObstacle(300);
			Obstacle.addTrapdoorObstacle(500);
			Obstacle.addTrapdoorObstacle(700);

			prepareEnd(length);
		}

		else if (whichLevel == 4) {

			int length = 1300;
			prepareStart(length, "    Swipe ramps down to \n jump over enemies or pits", 0, 0, 0, 1);

			Obstacle.addRampObstacle(400, Obstacle.ENEMY_AFTER);
			Obstacle.addRampObstacle(700, Obstacle.ENEMY_AFTER);
			Obstacle.addRampObstacle(1000, Obstacle.ENEMY_AFTER);

			prepareEnd(length);
		}

		else if (whichLevel == 5) {

			int length = 1300;
			prepareStart(length, "Just boxes", 1, 1, 0, 0);

			Obstacle.addUpboxObstacle(300);
			Obstacle.addDownboxObstacle(400);
			Obstacle.addUpboxObstacle(600);
			Obstacle.addUpboxObstacle(700);
			Obstacle.addDownboxObstacle(900);
			Obstacle.addDownboxObstacle(1000);
			Obstacle.addUpboxObstacle(1100);

			prepareEnd(length);
		}
    	
		else if (whichLevel == 6) {

			int length = 1500;
			prepareStart(length, "All going down", 0, 1, 0, 1);

			Obstacle.addDownboxObstacle(400);
			Obstacle.addRampObstacle(500, Obstacle.ENEMY_AFTER);
			Obstacle.addDownboxObstacle(800);
			Obstacle.addDownboxObstacle(900);
			Obstacle.addRampObstacle(1000, Obstacle.ENEMY_AFTER);

			prepareEnd(length);
		}
    	
		else if (whichLevel == 7) {

			int length = 1600;
			prepareStart(length, "Ramps and traps", 0, 0, 1, 1);
			
			Obstacle.addTrapdoorObstacle(300);
			Obstacle.addRampObstacle(400, Obstacle.ENEMY_AFTER);
			Obstacle.addTrapdoorObstacle(550);
			Obstacle.addTrapdoorObstacle(700);
			Obstacle.addRampObstacle(800, Obstacle.ENEMY_AFTER);
			
			prepareEnd(length);
		}
    	
		else if (whichLevel == 8) {

			int length = 1100;
			prepareStart(length, "Let's see if you learned", 1, 1, 1, 1);

			Obstacle.addUpboxObstacle(300);
			Obstacle.addTrapdoorObstacle(400);
			Obstacle.addDownboxObstacle(500);
			Obstacle.addRampObstacle(600, Obstacle.ENEMY_AFTER);
			Obstacle.addDownboxObstacle(800);
			Obstacle.addUpboxObstacle(900);
			
			prepareEnd(length);
		}
    	
		else if (whichLevel == 9) {

			int length = 1800;
			prepareStart(length, "Get Ready", 1, 1, 1, 1);

			
			Obstacle.addTrapdoorObstacle(300);
			Obstacle.addDownboxObstacle(400);
			Obstacle.addUpboxObstacle(500);
			Obstacle.addDownboxObstacle(600);
			Obstacle.addTrapdoorObstacle(700);
			Obstacle.addRampObstacle(800, Obstacle.ENEMY_AFTER);
			Obstacle.addDownboxObstacle(1100);
			Obstacle.addDownboxObstacle(1200);
			Obstacle.addTrapdoorObstacle(1300);
			Obstacle.addUpboxObstacle(1400);
			Obstacle.addRampObstacle(1500, Obstacle.ENEMY_AFTER);
			
			prepareEnd(length);
		}
    	
		else if (whichLevel == 10) {

			int length = 5900;
			prepareStart(length, "Good Luck", 1, 1, 1, 1);

			Obstacle.addUpboxObstacle(300);
			Obstacle.addDownboxObstacle(400);
			Obstacle.addTrapdoorObstacle(600);
			Obstacle.addRampObstacle(700, Obstacle.ENEMY_AFTER);
			
			Obstacle.addTrapdoorObstacle(1100);
			Obstacle.addTrapdoorObstacle(1200);
			Obstacle.addRampObstacle(1400, Obstacle.ENEMY_AFTER);
			Obstacle.addDownboxObstacle(1600);
			Obstacle.addUpboxObstacle(1700);
			Obstacle.addTrapdoorObstacle(1800);
			Obstacle.addRampObstacle(1900, Obstacle.ENEMY_AFTER);
			Obstacle.addDownboxObstacle(2200);
			
			Obstacle.addDownboxObstacle(2400);
			Obstacle.addTrapdoorObstacle(2500);
			Obstacle.addUpboxObstacle(2600);
			Obstacle.addTrapdoorObstacle(2700);
			Obstacle.addDownboxObstacle(2800);
			
			Obstacle.addDownboxObstacle(3000);
			Obstacle.addTrapdoorObstacle(3100);
			Obstacle.addDownboxObstacle(3200);
			Obstacle.addTrapdoorObstacle(3300);
			Obstacle.addDownboxObstacle(3400);
			
			Obstacle.addRampObstacle(3600, Obstacle.ENEMY_AFTER);
			Obstacle.addUpboxObstacle(3800);
			Obstacle.addDownboxObstacle(3900);
			
			Obstacle.addRampObstacle(4000, Obstacle.ENEMY_AFTER);
			Obstacle.addTrapdoorObstacle(4200);
			
			Obstacle.addTrapdoorObstacle(4400);
			Obstacle.addDownboxObstacle(4500);
			Obstacle.addUpboxObstacle(4600);
			Obstacle.addDownboxObstacle(4700);
			Obstacle.addTrapdoorObstacle(4800);
			Obstacle.addRampObstacle(4900, Obstacle.ENEMY_AFTER);
			Obstacle.addDownboxObstacle(5200);
			Obstacle.addDownboxObstacle(5300);
			Obstacle.addTrapdoorObstacle(5400);
			Obstacle.addUpboxObstacle(5500);
			Obstacle.addRampObstacle(5600, Obstacle.ENEMY_AFTER);

			prepareEnd(length);
		} //else if (whichLevel == 11) {
			//configureInfiniteLevel();
		//}
    }


	public void configureInfiniteLevel(int speed) {
		configureInfiniteLevel(speed, 0);
		Controls.addStopwatch();
		timeStart = Framework.self().getEngine().getSecondsElapsedTotal();
	}
	
	public float getStartTime(){
		return timeStart;
	}

    @SuppressWarnings("unused")
	private void configureInfiniteLevel() {
    	configureInfiniteLevel(5);
	}
    
    /**
     * Configure the start or middle of an infinite level.  The length of the level
     * is defined by the constant INFINITE_LEVEL_LENGTH.  If +x+ is 0, then a start
     * message will automatically appear and the background will be set, etc.  If
     * +x+ is greater than 0 then obstacles before +x+ will be cleared to conserve
     * memory and more will be added.
     * 
     * @param speed
     * 		The new speed of the hero.  Faster is harder.
     * @param x
     * 		The x location to begin configuring the new level from.  0 is the
     * 		beginning
     */
    public void configureInfiniteLevel(int speed, int x) {
    	final int startX = x+getCameraWidth();
    	final int endX = startX + INFINITE_LEVEL_LENGTH;
    	Hero h = null;
    	
    	Log.i("OO", "Configuring infinite level of speed " + speed);

    	if(x == 0) {
	        getEngine().clearUpdateHandlers();
			Level.reset(Integer.MAX_VALUE, getCameraHeight(), 0, 0, false, 0, 10);
			Level.addBackgroundLayer("background.png", -1f/PARALLAX_FACTOR, 0, 0);
			PopUpScene.printTimedMessage("Get Ready!", 2, 1, 1, 1, 1);
			addDecorations(getCameraWidth(), endX, endX/2000);
			h = Hero.addHero(0, Level.FLOOR_TOP-Hero.HEIGHT, Hero.WIDTH, Hero.HEIGHT, "hero.png", 1, 0, 0);
    	} else {
    		// Draw the floor a little bit before because otherwise the clearEarlyObstacles will remove the old floor and make it ugly.
    		Obstacle.clearEarlyObstacles(x);
    		addDecorations(startX, endX, INFINITE_LEVEL_LENGTH/2000);
    	}
    	
		Obstacle.addSquareObstacle(x, Level.FLOOR_TOP, INFINITE_LEVEL_LENGTH + getCameraWidth()*2, Level.FLOOR_HEIGHT, "ground.png", null, 0, 1, 0, 1);    	
    	
		// false sense of security...
		if(speed < 8)
			Obstacle.addRandomObstacles(startX, endX, speed+1);
		else
			Obstacle.addRandomObstacles(startX, endX, (int) (Math.random()*4.5)+8);
    	Obstacle.addInfiniteTriggerObstacle(endX, speed+1);
    	
    	if(x == 0) {
			h.setVelocity(speed, 0);
			h.animate(120, true);
			Hero.addRabbit(200, 160, speed, 0);
    	}
    	
    	getEngine().setScene(Level.current);
    }
    
    

	/**
     * If a game uses Obstacles that are triggers, it must provide this to
     * specify what to do when such an obstacle is hit by a hero
     */
    public void onTrigger(int score, int id)
    {
        if (id < 5) {
            PopUpScene.printTimedMessage("Collect blue balls\nto activate the next\npurple ball", 1, 0, 0, 0, 0);
            Goodie.addStationaryGoodie(10, 10, 10, 10, "blueball.png");
            Obstacle o = Obstacle.addCircularObstacle(300, 55 + 50 * id, 10, 10, "purpleball.png", null, 0, 1, 0, 1);
            o.setTrigger(id + 1, id + 1);
        }
        else {
            PopUpScene.printTimedMessage("The destination is\nnow available", 1, 0, 0, 0, 0);
            Destination.addDestination(200, 200, 20, 20, "mustardball.png", 1, 0);
        }
    }

    /**
     * Draw help scenes
     * 
     * A real game would need to provide much better help
     */
    public void configureHelpScene(int whichScene)
    {
        if (whichScene == 1) {
            HelpScene.reset(0, 0, 0);
            
            HelpScene.addDecoration(0, 0, Framework.self().getCameraWidth(), Framework.self().getCameraHeight(), "metal_bg.png");
            HelpScene.addText(190, 10, "Characters:", 0);
            
            HelpScene.addDecoration(95, 50, 70, 70, "hero.png");
            HelpScene.addText(37, 130, "      I am your hero \n       I hate my job \n\n  Please help me get \n    through the day", 0);
            
            
            HelpScene.addDecoration(325, 50, 40, 70, "boss.png");
            HelpScene.addText(265, 130, "     I am your boss \n\n       My goal is to \n make you miserable", 0);

            HelpScene.addText(130, 280, "Click to see how to play", 1);
        }
        else if (whichScene == 2) {
        	HelpScene.reset(0, 0, 0);
        	HelpScene.addDecoration(0, 0, Framework.self().getCameraWidth(), Framework.self().getCameraHeight(), "metal_bg.png");
            HelpScene.addText(190, 10, "Controls:", 0);
            
            HelpScene.addDecoration(20, 50, 70, 70, "upbox.png");
            HelpScene.addText(100,50, "Touch box \n        + \n swipe up" , 1);
            HelpScene.addDecoration(250, 180, 70, 70, "downbox.png");
            HelpScene.addText(330,180, "Touch box \n        + \n swipe down" , 1);
            HelpScene.addDecoration(250, 50, 70, 70, "ramp.png");
            HelpScene.addText(330,50, "Touch ramp \n        + \n swipe down" , 1);
            HelpScene.addDecoration(20, 180, 30, 70, "boss.png");
            HelpScene.addText(100,180, "Touch trapdoor \n        + \n swipe right" , 1);
            HelpScene.addDecoration(20, 250, 35, 10, "trapdoor.png");
            HelpScene.addDecoration(55, 250, 35, 10, "ground.png");
            
        }
    }


}
