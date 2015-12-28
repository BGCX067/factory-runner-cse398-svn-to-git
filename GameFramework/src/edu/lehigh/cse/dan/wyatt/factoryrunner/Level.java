package edu.lehigh.cse.dan.wyatt.factoryrunner;

import java.util.ArrayList;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ParallaxBackground;
import org.anddev.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;

import android.util.Log;

import com.badlogic.gdx.math.Vector2;

/**
 * Levels are just Scenes, from the perspective of AndEngine. The framework
 * doesn't extend them at all, but instead uses this static collection of fields
 * and methods to describe all the aspects of a level that are useful to our
 * various entities.
 * 
 * @author spear
 */
public class Level
{
	/**
	 * Constants for location of the floor.
	 */
	public static final int FLOOR_HEIGHT = 80;
	public static final int FLOOR_TOP = 320-FLOOR_HEIGHT;
	
	/**
	 * Constant representing the top edge of things that are at the top of the
	 * screen such as upboxes and ramps.
	 */
	public static final int TOP_RAIL = 10;
	
    /**
     * these are the ways you can complete a level: you can reach the
     * destination, you can collect enough stuff, or you can get the number of
     * enemies down to 0
     */
    enum VICTORY {
        DESTINATION, GOODIECOUNT, ENEMYCOUNT
    };

    /**
     * A helper so that we don't need pools to handle the onAccelerometerChanged
     * use of Vector2 objects
     */
    static final Vector2 oacVec = new Vector2();

    /**
     * The current game scene
     */
    public static Scene current;

    /**
     * Sound to play when the level is won
     */
    static Sound winSound;

    /**
     * Sound to play when the level is lost
     */
    static Sound loseSound;

    /**
     * Background music for this level
     */
    static Music music;

    /**
     * Describes how a level is won
     */
    static VICTORY victoryType;

    /**
     * Supporting data for VICTORY
     * 
     * his is just the number of goodies to collect, or the number of heroes who
     * must reach destinations
     */
    static int victoryVal;

    /**
     * Maximum gravity the accelerometer can create in X dimension
     */
    static private int _xGravityMax;

    /**
     * Maximum gravity the accelerometer can create in Y dimension
     */
    static private int _yGravityMax;

    /**
     * Basic world gravity in X dimension. Usually 0.
     */
    static int _initXGravity;

    /**
     * Basic world gravity in Y dimension. Usually 0, unless we have a side
     * scroller with jumping
     */
    static int _initYGravity;

    /**
     * Width of this level
     */
    static int _width;

    /**
     * Height of this level
     */
    static int _height;

    /**
     * Background image for this level. It is "parallax", which means it can
     * scroll slower than the motion of the game.
     */
    static private ParallaxBackground background;

    /**
     * Scrolling rate of the background
     */
    static float backgroundScrollFactor = 1;

    /**
     * The physics world for this game
     */
    static FixedStepPhysicsWorld physics;

    /**
     * List of entities that change behavior based on tilt
     */
    static ArrayList<PhysicsSprite> accelEntities = new ArrayList<PhysicsSprite>();

    /**
     * Prevent this object from ever being created
     */
    private Level()
    {
    }

    /**
     * When there is a phone tilt, this is run to adjust the forces on objects
     * in the current level
     * 
     * @param info
     *            The accelerometer data
     */
    public static void onAccelerometerChanged(AccelerometerData info)
    {
        // get gravity from accelerometer
        float xGravity = info.getX();
        float yGravity = info.getY();

        // ensure -10 <= x <= 10
        xGravity = (xGravity > _xGravityMax) ? _xGravityMax : xGravity;
        xGravity = (xGravity < -_xGravityMax) ? -_xGravityMax : xGravity;

        // ensure -10 <= y <= 10
        yGravity = (yGravity > _yGravityMax) ? _yGravityMax : yGravity;
        yGravity = (yGravity < -_yGravityMax) ? -_yGravityMax : yGravity;

        // Send the new gravity information to the physics system by affecting
        // each object
        oacVec.set(xGravity, yGravity);
        for (PhysicsSprite gfo : accelEntities) {
            if (gfo.physBody.isActive())
                gfo.physBody.applyForce(oacVec, gfo.physBody.getWorldCenter());
        }

        // Special hack for changing the direction of the Hero
        Hero h = Hero.lastHero;
        if ((h != null) && (h.reverseFace)) {
            Hero.lastHero._ttr.setFlippedHorizontal(xGravity < 0);
        }
    }

    /**
     * Set the sound to play when the level is won
     * 
     * @param name
     *            Name of the sound file to play
     */
    static void setWinSound(String name)
    {
        Sound s = Media.getSound(name);
        winSound = s;
    }

    /**
     * Set the sound to play when the level is lost
     * 
     * @param name
     *            Name of the sound file to play
     */
    static void setLoseSound(String name)
    {
        Sound s = Media.getSound(name);
        loseSound = s;
    }

    /**
     * Set the background music for this level
     * 
     * @param name
     *            Name of the sound file to play
     */
    static void setMusic(String name)
    {
        Music m = Media.getMusic(name);
        music = m;
    }

    /**
     * Attach a background layer to this scene
     * 
     * @param name
     *            Name of the image file to display
     * @param factor
     *            scrolling factor for this layer. 0 means "dont' move".
     *            Negative value matches left-to-right scrolling, with larger
     *            values moving faster.
     * @param x
     *            Starting x coordinate of top left corner
     * @param y
     *            Starting y coordinate of top left corner
     */
    static void addBackgroundLayer(String name, float factor, int x, int y)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        if (background == null) {
            // we'll configure the background as black
            background = new ParallaxBackground(0, 0, 0);
            current.setBackground(background);
        }
        background.attachParallaxEntity(new ParallaxEntity(factor, new AnimatedSprite(x, y, ttr)));
    }

    /**
     * Set the rate at which the background scrolls
     * 
     * @param factor
     *            The new value to use. When in doubt, 20 is pretty good
     */
    static public void setBackgroundScrollFactor(float factor)
    {
        backgroundScrollFactor = factor;
    }

    /**
     * Set the background color for this level
     * 
     * @param r
     *            Red portion of background color
     * @param g
     *            Green portion of background color
     * @param b
     *            Blue portion of background color
     */
    static public void setBackgroundColor(float r, float g, float b)
    {
        if (background == null) {
            // configure the background based on the colors provided
            background = new ParallaxBackground(r, g, b);
            current.setBackground(background);
            background.setParallaxValue(0);
        }
    }

    /**
     * Reset the current level to a blank slate
     * 
     * This should be called whenever starting to create a new playable level
     * 
     * @param width
     *            Width of the new scene
     * @param height
     *            Height of the new scene
     * @param xGravityMax
     *            Max X force that the accelerometer can produce
     * @param yGravityMax
     *            Max Y force that the accelerometer can produce
     * @param useAccelerometer
     *            true or false, depending on whether the accelerometer should
     *            be used
     * @param initXGravity
     *            default gravity in the X dimension. Usually 0
     * @param initYGravity
     *            default gravity in the Y dimension. 0 unless the game is a
     *            side-scroller with jumping
     */
    static public void reset(int width, int height, int xGravityMax, int yGravityMax, boolean useAccelerometer,
            int initXGravity, int initYGravity)
    {
        // create a scene and a physics world
        current = new Scene();

        _xGravityMax = xGravityMax;
        _yGravityMax = yGravityMax;
        _initXGravity = initXGravity;
        _initYGravity = initYGravity;
        _width = width;
        _height = height;

        Framework.self().myCamera.setBoundsEnabled(true);
        Framework.self().myCamera.setBounds(0, width, 0, height);

        physics = new FixedStepPhysicsWorld(60, new Vector2(_initXGravity, _initYGravity), false) {
            @Override
            public void onUpdate(float pSecondsElapsed)
            {
                super.onUpdate(pSecondsElapsed);
                if (background != null)
                    background.setParallaxValue(Framework.self().myCamera.getCenterX() / backgroundScrollFactor);
            }
        };

        // clear the stuff we explicitly manage in the physics world
        accelEntities.clear();

        // set handlers and listeners
        current.registerUpdateHandler(physics);
        physics.setContactListener(Framework.self());

        // reset the factories
        Hero.onNewLevel();
        Enemy.onNewLevel();
        Destination.onNewLevel();
        Goodie.onNewLevel();
        Controls.resetHUD();

        Framework.self().configAccelerometer(useAccelerometer);

        setVictoryDestination(1);

        Framework.self().myCamera.setZoomFactorDirect(1);

        winSound = null;
        loseSound = null;
        music = null;
        background = null;
    }

    /**
     * Indicate that the level is won by having a certain number of heroes reach
     * destinations
     * 
     * @param howMany
     *            Number of heroes that must reach destinations
     */
    static public void setVictoryDestination(int howMany)
    {
        victoryType = VICTORY.DESTINATION;
        victoryVal = howMany;
    }

    /**
     * Indicate that the level is won by destroying all of the enemies
     */
    static public void setVictoryEnemyCount()
    {
        victoryType = VICTORY.ENEMYCOUNT;
    }

    /**
     * Indicate that the level is won by collecting enough goodies
     * 
     * @param howMany
     *            Number of goodies that must be collected to win the level
     */
    static public void setVictoryGoodies(int howMany)
    {
        victoryType = VICTORY.GOODIECOUNT;
        victoryVal = howMany;
    }

    /**
     * Draw a picture on the current level
     * 
     * Note: the order in which this is called relative to other entities will
     * determine whether they go under or over this picture.
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the picture
     * @param height
     *            Height of this picture
     * @param name
     *            Name of the picture to display
     */
    static void addDecoration(int x, int y, int width, int height, String name)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        AnimatedSprite s = new AnimatedSprite(x, y, width, height, ttr);
        current.attachChild(s);
    }

    /**
     * Stop the rabbit from moving the screen forward (i.e., pause the screen).
     * This is useful on the user's death, etc.
     * @param x	X location of the hero to center on.
     * @param y	Y location of the hero to center on.
     */
	public static void pauseScreenRabbit(float x, float y) {
		Hero.addRabbit((int) x + 200, (int) y, 0, 0);
	}
}