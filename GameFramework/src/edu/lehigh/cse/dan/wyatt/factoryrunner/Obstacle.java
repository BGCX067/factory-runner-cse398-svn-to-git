package edu.lehigh.cse.dan.wyatt.factoryrunner;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.PathModifier.Path;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.res.AssetManager;
import android.view.MotionEvent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * Obstacles are entities that change the hero's velocity upon a collision
 * 
 * There are many flavors of obstacles. They can have a physics shape that is
 * circular or square. They can have default collision behavior or custom
 * behavior. They can be moved by dragging. They can move by touching the object
 * and then touching a point on the screen. They can have "damp" behavior, which
 * is a way to do tricks with Physics (such as zoom strips or friction pads). A
 * method for drawing bounding boxes on the screen is also available, as is a
 * means of creating "trigger" obstacles that cause user-specified code to run
 * upon any collision. There is also a simple object type for loading SVG files,
 * such as those created by Inkscape.
 * 
 * @author spear
 */
public class Obstacle extends PhysicsSprite
{
    /**
     * When a sprite is poked, we record it here so that we know who to move on
     * the next screen touch
     */
    private static Obstacle currentSprite;
    
    /**
     * Store the last Obstacle that the user put his finger on.  This is only
     * updated by touch down events.  If the scene registers a Touch Up event,
     * then the position of this obstacle is used to determine the direction of
     * the swipe and then the respective methods of SwipeListener are called.
     */
    private static Obstacle lastTouched;
    
    /**
     * Width and height of the various puzzle obstacles.
     */
    public static final int BOX_WIDTH = 70;
    public static final int BOX_HEIGHT = 70;
    public static final int PIT_WIDTH = 70;
    public static final int PIT_HEIGHT = 70;
    public static final int RAMP_WIDTH = 70;
    public static final int RAMP_HEIGHT = 70;
    public static final int TRAPDOOR_WIDTH = 35;
    public static final int TRAPDOOR_HEIGHT = 10;
    public static final int FORKLIFT_WIDTH = 105;
    public static final int FORKLIFT_HEIGHT = 135;
    
    /**
     * Amount of padding around swipeable objects to provide a bigger
     * touch surface to the user.
     */
    private static final int TOUCH_PADDING = 8;

    /**
     * When a sprite is poked, remember the time, because rapid double-clicks
     * cause deletion
     */
    private static float lastPokeTime;
    
    /**
     * Remember the last time that an obstacle was touched in milliseconds.  When
     * a touch up event is detected, the current time can be compared to the
     * lastTouchTime and if they are close enough the system can assume that
     * the user performed a swipe gesture.
     */
	private static float lastTouchTime;

    /**
     * Rather than use a Vector2 pool, we'll keep a vector around for all poke
     * operations
     */
    private final static Vector2 pokeVector = new Vector2();

    /**
     * Tunable constant for how much time between pokes constitutes a
     * "double click"
     */
    private final static float pokeDeleteThresh = 0.5f;

    /**
     * Rather than use a Vector2 pool, we'll keep a vector around for drag
     * operations
     */
    private static final Vector2 dragVector = new Vector2();

    /**
     * Track if the obstacle has an active "dampening" factor for custom physics
     * tricks
     */
    boolean isDamp;

    /**
     * The dampening factor of this obstacle
     */
    float dampFactor;

    /**
     * Track if the object is draggable
     */
    private boolean isDrag = false;

    /**
     * Track if the object is pokable
     */
    private boolean isPoke = false;

    /**
     * Track if this is a "trigger" object that causes special code to run upon
     * any collision
     */
    boolean isTrigger = false;

    /**
     * Triggers can require a certain Goodie count in order to run, as
     * represented by this field
     */
    int triggerActivation = 0;

    /**
     * An ID for each trigger object, in case it's useful
     */
    int triggerID;
    
    /**
     * Number of pixels long a swipe must be for it to count
     * as a swipe.
     */
	private static final float swipeDelta = 65;
	
	/**
	 * Stores the SwipeListener to be called when a swipe originates
	 * on this obstacle.
	 */
	private SwipeListener swipeActionHandler;
	
	/**
	 * Is this a ramp obstacle?
	 */
	private boolean isRamp;
	
	/**
	 * Is this the invisible obstacle on top of a pit?
	 */
	private boolean isPit;
	
	/**
	 * Does the user die if the hero hits this obstacle?
	 * (Similar to an Enemy)
	 */
	private boolean isKiller;
	
	/**
	 * Constant to pass to Obstacle.addRampObstacle to signify
	 * that an enemy should be placed after the ramp.
	 */
	public static final int ENEMY_AFTER = 1;

    /**
     * Internal constructor to build an Obstacle.
     * 
     * This should never be invoked directly. Instead, use the 'addXXX' methods
     * of the Object class.
     * 
     * @param x
     *            X position of top left corner
     * @param y
     *            Y position of top left corner
     * @param width
     *            width of this Obstacle
     * @param height
     *            height of this Obstacle
     * @param ttr
     *            image to use for this Obstacle
     */
    private Obstacle(float x, float y, float width, float height, TiledTextureRegion ttr)
    {
    	super(x, y, width, height, ttr, PhysicsSprite.TYPE_OBSTACLE);
    	isRamp = false;
    	swipeActionHandler = new NoopSwipeListener(); // Default to no swipe actions.
    }
    
    /**
     * Internal constructor to build an Obstacle.
     * 
     * This should never be invoked directly. Instead, use the 'addXXX' methods
     * of the Object class.
     * 
     * @param x
     *            X position of top left corner
     * @param y
     *            Y position of top left corner
     * @param width
     *            width of this Obstacle
     * @param height
     *            height of this Obstacle
     * @param ttr
     *            image to use for this Obstacle
     * @param swipeActionHandler
     * 			  A SwipeListener to be called when a swipe originates on this object.
     */
    private Obstacle(float x, float y, float width, float height, TiledTextureRegion ttr, SwipeListener swipeActionHandler)
    {
    	super(x, y, width, height, ttr, PhysicsSprite.TYPE_OBSTACLE);
    	isRamp = false;
    	this.swipeActionHandler = swipeActionHandler;
    }

    /**
     * Call this on an Obstacle to make it draggable.
     * 
     * Be careful when dragging Obstacles. If they are small, they will be hard
     * to touch.
     */
    void enableDrag()
    {
        isDrag = true;
        Level.current.registerTouchArea(this);
        Level.current.setTouchAreaBindingEnabled(true);
    }

    /**
     * Call this on an Obstacle to rotate it
     * 
     * @param rotation
     *            amount to rotate the Obstacle (in degrees)
     */
    void rotate(float rotation)
    {
        // rotate it
        physBody.setTransform(physBody.getPosition(), rotation);
        setRotation(rotation);
    }

    /**
     * Call this on an Obstacle to make it pokeable
     * 
     * Poke the Obstacle, then poke the screen, and the Obstacle will move to
     * the location that was pressed. Poke the Obstacle twice in rapid
     * succession to delete the Obstacle.
     */
    void enablePoke()
    {
        isPoke = true;
        Level.current.registerTouchArea(this);
        Level.current.setTouchAreaBindingEnabled(true);
        Level.current.setOnSceneTouchListener(Framework.self());
    }

    /**
     * Call this on an Obstacle to give it a dampening factor.
     * 
     * A hero can glide over damp Obstacles. Damp factors can be negative to
     * cause a reverse direction, less than 1 to cause a slowdown (friction
     * pads), or greater than 1 to serve as zoom pads.
     * 
     * @param factor
     *            Value to multiply the hero's velocity when it is on this
     *            Obstacle
     */
    void setDamp(float factor)
    {
        // We have the fixtureDef for this object, but it's the Fixture that we
        // really need to modify. Find it, and set it to be a sensor
        physBody.getFixtureList().get(0).setSensor(true);
        // set damp info
        dampFactor = factor;
        isDamp = true;
    }

    /**
     * Make the object a trigger object, so that custom code will run when a
     * hero runs over (or under) it
     * 
     * @param activationGoodies
     *            Number of goodies that must be collected before this trigger
     *            works
     * @param id
     *            identifier for the trigger
     */
    void setTrigger(int activationGoodies, int id)
    {
        triggerID = id;
        isTrigger = true;
        triggerActivation = activationGoodies;
        physBody.getFixtureList().get(0).setSensor(true);
    }

    /**
     * Draw an obstacle on the screen. The obstacle's underlying physics shape
     * is circular.
     * 
     * All obstacle behaviors begin by creating an obstacle, using either this
     * or addSquareObstacle. Then, methods applied to the result can be used to
     * modify the Obstacle's behavior
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            X coordinate of top left corner
     * @param width
     *            Width of the obstacle
     * @param height
     *            Height of the obstacle
     * @param name
     *            Name of the image file to use
     * @param path
     *            a Path object to describe how the obstacle moves, or null if
     *            no path is desired
     * @param pathDuration
     *            The amount of time it takes for the path to complete, or 0 if
     *            the path is null
     * @param density
     *            Density of the obstacle. When in doubt, use 1
     * @param elasticity
     *            Elasticity of the obstacle. When in doubt, use 0
     * @param friction
     *            Friction of the obstacle. When in doubt, use 1
     * @return an obstacle, which can be customized further
     */
    static public Obstacle addCircularObstacle(int x, int y, int width, int height, String name, Path path,
            float pathDuration, float density, float elasticity, float friction)
    {
        // get image
        TiledTextureRegion ttr = Media.getImage(name);
        // make object
        Obstacle o = new Obstacle(x, y, width, height, ttr);
        // create physics
        BodyType bt = (path == null) ? BodyType.StaticBody : BodyType.DynamicBody;
        o.setCirclePhysics(density, elasticity, friction, bt, false, false, true);
        // set path
        if (path != null)
            o.applyPath(path, pathDuration);
        Level.current.attachChild(o);
        return o;
    }

    /**
     * Draw an obstacle on the screen. The obstacle's underlying physics shape
     * is rectangular.
     * 
     * All obstacle behaviors begin by creating an obstacle, using either this
     * or addCircularObstacle. Then, methods applied to the result can be used
     * to modify the Obstacle's behavior
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            X coordinate of top left corner
     * @param width
     *            Width of the obstacle
     * @param height
     *            Height of the obstacle
     * @param name
     *            Name of the image file to use
     * @param path
     *            a Path object to describe how the obstacle moves, or null if
     *            no path is desired
     * @param pathDuration
     *            The amount of time it takes for the path to complete, or 0 if
     *            the path is null
     * @param density
     *            Density of the obstacle. When in doubt, use 1
     * @param elasticity
     *            Elasticity of the obstacle. When in doubt, use 0
     * @param friction
     *            Friction of the obstacle. When in doubt, use 1
     * @return an obstacle, which can be customized further
     */
    static public Obstacle addSquareObstacle(int x, int y, int width, int height, String name, Path path,
            float pathDuration, float density, float elasticity, float friction)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        Obstacle o = new Obstacle(x, y, width, height, ttr);
        BodyType bt = (path == null) ? BodyType.StaticBody : BodyType.DynamicBody;
        o.setBoxPhysics(density, elasticity, friction, bt, false, false, true);
        if (path != null)
            o.applyPath(path, pathDuration);
        Level.current.attachChild(o);
        return o;
    }

	/**
	 * An UpboxSwipeListener is designed to handle events that originate on
	 * an upbox to make it move up to the top rail on an up swipe.  It then
	 * removes all swipe actions so once the upbox has been moved to the top
	 * it cannot be moved down.
	 */
    private static final class UpboxSwipeListener extends NoopSwipeListener {
    	private Obstacle box, toucharea;
    	
    	/**
    	 * Instantiate an UpboxSwipeListener.
    	 * @param toucharea The invisible obstacle that the user swipes.
    	 * @param box The box to move upon swipe.
    	 */
    	public UpboxSwipeListener(Obstacle toucharea, Obstacle box) {
    		this.box = box;
    		this.toucharea = toucharea;
    	}
    	
		@Override
		public void onSwipeUp() {
			super.onSwipeUp();
			
			// Move box to top rail.
			box.applyPathOnce(new Path(2).to(box.getX(), box.getY()).to(box.getX(), Level.FLOOR_TOP-FORKLIFT_HEIGHT+10-BOX_HEIGHT), 0.5f);
			
			// Disable swipe actions.
			toucharea.setActionSwipeListener(new NoopSwipeListener());
		}
    }

    /**
     * Add an upbox obstacle at the specified x location.  An upbox must be swiped up by the user
     * or it will kill the hero.
     * 
     * @param x	The x-coordinate of the left side of the upbox.
     * @return The Obstacle representing the upbox.
     */
    static public Obstacle addUpboxObstacle(int x)
    {
    	final int y = Level.FLOOR_TOP-BOX_HEIGHT;
    	
    	Level.addDecoration(x-(FORKLIFT_WIDTH-BOX_WIDTH)/2, Level.FLOOR_TOP-FORKLIFT_HEIGHT, FORKLIFT_WIDTH, FORKLIFT_HEIGHT, "forklift.png");
    	
    	final Obstacle toucharea = Obstacle.addSquareObstacle(x-TOUCH_PADDING, y-TOUCH_PADDING, BOX_WIDTH+TOUCH_PADDING*2, BOX_HEIGHT+TOUCH_PADDING*2, "invis.png", null, 0, 1, 0, 1);
	    toucharea.setDamp(1);
    	
    	final Obstacle ret = Obstacle.addSquareObstacle(x, y, BOX_WIDTH, BOX_HEIGHT, "upbox.png", null, 0, 1, 0, 1);
	    ret.isKiller = true;
		ret.setActionSwipeListener(new UpboxSwipeListener(toucharea, ret));
		return ret;
    }
    
    /**
     * A DownboxSwipeListener is designed to handle swipe events on a downbox.  When a downbox
     * is swiped down, it travels to the floor and the invisible pit enemy is made harmless.
     * 
     * Like an UpboxSwipeListener, it deactivates all handlers after one swipe.
     *
     */
    private static final class DownboxSwipeListener extends NoopSwipeListener {
    	private Obstacle box, pitkill, toucharea;
    	
    	/**
    	 * Instantiate a listener for a downbox.
    	 * @param toucharea The invisible obstacle that the user swipes.
    	 * @param box	The box to move upon swipe.
    	 * @param pitkill	The Obstacle to render harmless when you move it down
    	 */
    	public DownboxSwipeListener(Obstacle toucharea, Obstacle box, Obstacle pitkill) {
    		this.box = box;
    		this.pitkill = pitkill;
    		this.toucharea = toucharea;
    	}
    	
		@Override
		public void onSwipeDown() {
			super.onSwipeDown();
			box.applyPathOnce(new Path(2).to(box.getX(), box.getY()).to(box.getX(), Level.FLOOR_TOP + 2), 0.3f);
			toucharea.setActionSwipeListener(new NoopSwipeListener());
			pitkill.isPit = false;
		}
    }
    
    /**
     * Draw an obstacle on the screen. This obstacle is a box at the top of the screen
     * that must be moved down to cover a pit so the hero can run over it.
     * 
     * This obstacle creates the box that must be swiped down, a pit that box should be
     * dragged into and an invisible enemy that kills the hero if the pit has not been 
     * covered.
     * 
     * X is the only variable set. This is because the object sizes will be consistent
     * as will the y coordinate.
     * 
     * @param x
     *            X coordinate of top left corner. This is the location that the 
     *            obstacle will appear
     */
    static public Obstacle addDownboxObstacle(int x)
    {
    	final int y = Level.FLOOR_TOP-FORKLIFT_HEIGHT+10-BOX_HEIGHT;
    	
    	Level.addDecoration(x-(FORKLIFT_WIDTH-BOX_WIDTH)/2, Level.FLOOR_TOP-FORKLIFT_HEIGHT, FORKLIFT_WIDTH, FORKLIFT_HEIGHT, "forklift.png");
	    
    	final Obstacle toucharea = Obstacle.addSquareObstacle(x-TOUCH_PADDING, y-TOUCH_PADDING, BOX_WIDTH+TOUCH_PADDING*2, BOX_HEIGHT+TOUCH_PADDING*2, "invis.png", null, 0, 1, 0, 1);
	    final Obstacle hole = Obstacle.addSquareObstacle(x, Level.FLOOR_TOP, PIT_WIDTH, PIT_HEIGHT, "ground_hole.png", null, 0, 1, 0, 1);
	    final Obstacle pitkill = Obstacle.addSquareObstacle(x, Level.FLOOR_TOP-5, PIT_WIDTH, 5, "invis.png", null, 0, 1, 0, 1);
	    
	    final Obstacle ret = Obstacle.addSquareObstacle(x, y, BOX_WIDTH, BOX_HEIGHT, "downbox.png", null, 0, 1, 0, 1);
	    ret.isKiller = true;
	    
	    toucharea.setDamp(1);
	    pitkill.setDamp(1);
	    hole.setDamp(1);
	    ret.setDamp(1);
	    
	    pitkill.isPit = true;
		ret.setActionSwipeListener(new DownboxSwipeListener(toucharea, ret, pitkill));
		return ret;
    }
    
    /**
     * A RampSwipeListener is designed to handle swipe events on a ramp.  A swipe down
     * will bring the ramp down into position.  Unlike the other handlers, this does
     * not render the enemies harmless as the ramp will cause the hero to jump over the
     * enemy and avoid a collision.
     *
     */
    private static final class RampSwipeListener extends NoopSwipeListener {
		private final Obstacle ramp, toucharea;

		/**
		 * Instantiate a ramp.
		 * @param ramp The ramp to move upon swipe.
		 */
		private RampSwipeListener(Obstacle toucharea, Obstacle ramp) {
			this.toucharea = toucharea;
			this.ramp = ramp;
		}

		@Override
		public void onSwipeDown() {
			super.onSwipeDown();
			ramp.applyPathOnce(new Path(2).to(ramp.getX(), ramp.getY()).to(ramp.getX(), Level.FLOOR_TOP-ramp.getHeight()), 1);
			toucharea.setActionSwipeListener(new NoopSwipeListener());
		}
	}
    
    /**
     * Add a ramp obstacle at the specified location.  A ramp will be placed at the top of the screen that
     * the user can drag down.  Enemies, nothing, or other obstacles are placed after the ramp depending on
     * the after parameter.
     * 
     * @param x	The x location of the ramp.
     * @param after A constant representing what should be after the ramp.  Right now the only available
     * 				one is ENEMY_AFTER to have an enemy that kills the hero after the ramp.
     * @return The ramp obstacle itself.  The obstacle after the ramp is not returned.
     */
	static public Obstacle addRampObstacle(int x, int after) {
		int y = 0;
	    final Obstacle ramp = Obstacle.addSquareObstacle(x, y, RAMP_WIDTH, RAMP_HEIGHT, "ramp.png", null, 0, 1, 0, 1);
    	final Obstacle toucharea = Obstacle.addSquareObstacle(x-TOUCH_PADDING, y-TOUCH_PADDING, BOX_WIDTH+TOUCH_PADDING*2, BOX_HEIGHT+TOUCH_PADDING*2, "invis.png", null, 0, 1, 0, 1);
	    
    	toucharea.setDamp(1);
    	
	    /* Allow the hero to walk through the ramp.  Hero#onCollision will take care of killing the hero
	     * on contact with the ramp.  This is because the obstacle is square but the ramp image is a triangle,
	     * so the Hero must be able to pass through the obstacle if the ramp is down when he/she walks onto it.
	     */
    	ramp.setRotation(-45);
	    ramp.setDamp(0);
	    ramp.isDamp = false;
	    ramp.isRamp = true;
		ramp.setActionSwipeListener(new RampSwipeListener(toucharea, ramp));
		
		if(after == ENEMY_AFTER) {
			Enemy.addStationaryEnemy(x+RAMP_WIDTH+10, Level.FLOOR_TOP-Enemy.HEIGHT, Enemy.WIDTH, Enemy.HEIGHT, "boss.png", 1.0f, 0f, 1f);
		}
		
		return ramp;		
	}
	
	/**
	 * A TrapdoorSwipeListener handles swipe events for trapdoors.  When the trapdoor
	 * is swiped to the right, the enemy falls into the pit and all future swipes
	 * are ignored.
	 */
	private static final class TrapdoorSwipeListener extends NoopSwipeListener {
		/**
		 * The obstacle with the image of a trapdoor to open and close.
		 */
		private final Obstacle trapdoor;
		
		/**
		 * The invisible obstacle that gives the user a bigger area to swipe at
		 */
		private final Obstacle swipedoor;
		
		/**
		 * The enemy to move into the pit when the swipedoor is swiped.
		 */
		private final Enemy enemy;

		/**
		 * Instantiate a TrapdoorSwipeListener.
		 * @param trapdoor The obstacle with the image of a trapdoor to open and close.
		 * @param swipedoor The invisible obstacle that gives the user a bigger area to swipe at
		 * @param enemy The enemy to move into the pit when the swipedoor is swiped.
		 */
		private TrapdoorSwipeListener(Obstacle trapdoor, Obstacle swipedoor,
				Enemy enemy) {
			this.trapdoor = trapdoor;
			this.swipedoor = swipedoor;
			this.enemy = enemy;
		}

		@Override
		public void onSwipeRight() {
			super.onSwipeRight();
			enemy.applyPathOnce(new Path(2).to(enemy.getX(), enemy.getY()).to(enemy.getX(), enemy.getY()+Enemy.HEIGHT+10), 0.5f);
			trapdoor.applyPathOnce(new Path(3).to(trapdoor.getX(), trapdoor.getY()).to(trapdoor.getX() + trapdoor.getWidth(), trapdoor.getY()).to(trapdoor.getX(), trapdoor.getY()), 1);
			swipedoor.setActionSwipeListener(new NoopSwipeListener());
		}
	}
	/**
	 * Create a Trapdoor obstacle.  When the trapdoor is swiped to the right, the enemy falls
	 * into the pit.
	 * @param x The x-location of the left side of the trapdoor.  Note that the enemy stands 5px to the right of the start of
	 * 			the trapdoor.
	 * @return The Obstacle that has the image of the trapdoor that is opened and closed when the user swipes in
	 * 		   its vicinity.
	 */
	static public Obstacle addTrapdoorObstacle(int x) {
		int y = Level.FLOOR_TOP;
	    final Obstacle hole = Obstacle.addSquareObstacle(x, Level.FLOOR_TOP, PIT_WIDTH, PIT_HEIGHT, "ground_hole.png", null, 0, 1, 0, 1);
	    final Obstacle trapdoor = Obstacle.addSquareObstacle(x, y, TRAPDOOR_WIDTH, TRAPDOOR_HEIGHT, "trapdoor.png", null, 0, 1, 0, 1);
	    final Obstacle closedpart = Obstacle.addSquareObstacle(x + TRAPDOOR_WIDTH, y, PIT_WIDTH - TRAPDOOR_WIDTH, TRAPDOOR_HEIGHT, "ground.png", null, 0, 1, 0, 1);
	    final Enemy enemy = Enemy.addStationaryEnemy(x+5, y-Enemy.HEIGHT, Enemy.WIDTH, Enemy.HEIGHT, "boss.png", 1.0f, 0f, 1f);
	    final Obstacle swipedoor = Obstacle.addSquareObstacle(x-15, y-TRAPDOOR_WIDTH+10, TRAPDOOR_WIDTH+30, 10+60, "invis.png", null, 0, 1, 0, 1);
	    
	    closedpart.setDamp(1);
	    swipedoor.setDamp(1);
	    trapdoor.setDamp(1);
	    hole.setDamp(1);
		
	    swipedoor.setActionSwipeListener(new TrapdoorSwipeListener(trapdoor, swipedoor, enemy));
		
		return trapdoor;		
	}
	
	/**
	 * Create an invisible obstacle at the specified x location that, upon collision
	 * with the hero, will create a new level of the length and speed given.
	 * @param x
	 * @param sampleActivity 
	 */
	public static Obstacle addInfiniteTriggerObstacle(int x, int speed) {
		final Obstacle ret = Obstacle.addSquareObstacle(x-5, Level.FLOOR_TOP-Hero.HEIGHT, 5, Hero.HEIGHT, "invis.png", null, 0, 1, 0, 1);
		
		ret.setDamp(1);
		ret.isDamp = false;
		
		ret.myType = PhysicsSprite.TYPE_INFINITE_TRIGGER | speed;
		return ret;
	}

    /**
     * Draw a box on the scene
     * 
     * Note: the box is actually four narrow rectangles
     * 
     * @param x0
     *            X coordinate of top left corner
     * @param y0
     *            Y coordinate of top left corner
     * @param x1
     *            X coordinate of bottom right corner
     * @param y1
     *            Y coordinate of bottom right corner
     * @param name
     *            name of the image file to use when drawing the rectangles
     * @param density
     *            Density of the obstacle. When in doubt, use 1
     * @param elasticity
     *            Elasticity of the obstacle. When in doubt, use 0
     * @param friction
     *            Friction of the obstacle. When in doubt, use 1
     */
    static public void drawBoundingBox(float x0, float y0, float x1, float y1, String name, float density,
            float elasticity, float friction)
    {
        // get the image by name. Note that we could animate it ;)
        TiledTextureRegion ttr = Media.getImage(name);
        // draw four rectangles, give them physics and attach them to the scene
        Obstacle b = new Obstacle(x0, y1 - 1, x1, 1, ttr);
        b.setBoxPhysics(density, elasticity, friction, BodyType.StaticBody, false, false, false);
        Level.current.attachChild(b);
        Obstacle t = new Obstacle(x0, y0 + 1, x1, 1, ttr);
        t.setBoxPhysics(density, elasticity, friction, BodyType.StaticBody, false, false, false);
        Level.current.attachChild(t);
        Obstacle l = new Obstacle(x0, y0, 1, y1, ttr);
        l.setBoxPhysics(density, elasticity, friction, BodyType.StaticBody, false, false, false);
        Level.current.attachChild(l);
        Obstacle r = new Obstacle(x1 - 1, y0, 1, y1, ttr);
        r.setBoxPhysics(density, elasticity, friction, BodyType.StaticBody, false, false, false);
        Level.current.attachChild(r);
    }

    /**
     * When the scene is touched, we use this to figure out if we need to move a
     * PokeObject.  Swipe events are handled here by figuring out the last Obstacle
     * that fielded a touch down event and calculating the difference in X and Y
     * coordinates between the object and the user's current finger position.
     * 
     * @param pScene
     *            The scene that was touched
     * @param te
     *            A description of the touch event
     */
    static public boolean handleSceneTouch(final Scene pScene, final TouchEvent te)
    {
        // only do this if we have a valid scene, valid physics, a valid
        // currentSprite, and a down press
        if (Level.physics != null) {
            switch (te.getAction()) {
                case TouchEvent.ACTION_DOWN:
                    if (currentSprite != null) {
                        Framework.self().getEngine().vibrate(100);
                        // move the object
                        pokeVector.set(te.getX() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, te.getY()
                                / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
                        currentSprite.physBody.setTransform(pokeVector, currentSprite.physBody.getAngle());
                        currentSprite = null;
                        return true;
                    }
            }
        }
        
		if(Framework.self().getEngine().getSecondsElapsedTotal() - lastTouchTime < 0.3) {
    		float dx = te.getX() - lastTouched.mX;
    		float dy = te.getY() - lastTouched.mY;
    		
    		//Log.i("swipe", "swipe dx:" + dx + ", dy: " + dy);
    		
    		if(Math.abs(dy) > Math.abs(dx)) {
	    		if(dy > swipeDelta) {
	    			lastTouched.swipeActionHandler.onSwipeDown();
	    		} else if (dy < -swipeDelta) {
	    			lastTouched.swipeActionHandler.onSwipeUp();
	    		}
    		} else {
	    		if(dx > swipeDelta) {
	    			lastTouched.swipeActionHandler.onSwipeRight();
	    		} else if(dx < -swipeDelta) {
	    			lastTouched.swipeActionHandler.onSwipeLeft();
	    		}
    		}
    		
    		return true;
    	}
        
        return false;
    }

    /**
     * Load an SVG line drawing generated from Inkscape.
     * 
     * Note that not all Inkscape drawings will work as expected. See
     * SVGParser.java for more information.
     * 
     * @param name
     *            Name of the svg file to load. It should be in the assets
     *            folder
     * @param r
     *            red component of the color to use for all lines
     * @param g
     *            green component of the color to use for all lines
     * @param b
     *            blue component of the color to use for all lines
     * @param density
     *            density of all lines
     * @param elasticity
     *            elasticity of all lines
     * @param friction
     *            friction of all lines
     * @param stretchX
     *            Stretch the drawing in the X dimension by this percentage
     * @param stretchY
     *            Stretch the drawing in the Y dimension by this percentage
     * @param xposeX
     *            Shift the drawing in the X dimension. Note that shifting
     *            occurs before scaling, which may not be the expected behavior.
     * @param xposeY
     *            Shift the drawing in the Y dimension. Note that shifting
     *            occurs before scaling, which may not be the expected behavior.
     */
    static public void loadSVG(String name, float r, float g, float b, float density, float elasticity, float friction,
            float stretchX, float stretchY, float xposeX, float xposeY)
    {
        try {
            // create a SAX parser for SVG files
            final SAXParserFactory spf = SAXParserFactory.newInstance();
            final SAXParser sp = spf.newSAXParser();

            final XMLReader xmlReader = sp.getXMLReader();
            SVGParser Parser = new SVGParser();

            // make the color values visible to the addLine routine of the
            // parser
            Parser.lineR = r;
            Parser.lineG = g;
            Parser.lineB = b;

            // create the physics fixture in a manner that is visible to the
            // addLine
            // routine of the parser
            Parser.fixture = PhysicsFactory.createFixtureDef(density, elasticity, friction);

            // specify transpose and stretch information
            Parser.userStretchX = stretchX;
            Parser.userStretchY = stretchY;
            Parser.userTransformX = xposeX;
            Parser.userTransformY = xposeY;

            // start parsing!
            xmlReader.setContentHandler(Parser);
            AssetManager am = Framework.self().getAssets();
            InputStream inputStream = am.open(name);
            xmlReader.parse(new InputSource(new BufferedInputStream(inputStream)));
        }
        // if the read fails, just print a stack trace
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when this Obstacle is the dominant obstacle in a collision
     * 
     * Note: This Obstacle is /never/ the dominant obstacle in a collision,
     * since it is #6 or #7
     * 
     * @param other
     *            The other entity involved in this collision
     */
    void onCollide(PhysicsSprite other)
    {
    }

    /**
     * Whenever an Obstacle is touched, this code runs automatically.
     * 
     * User code should never call this directly.
     * 
     * @param e
     *            Nature of the touch (down, up, etc)
     * @param x
     *            X position of the touch
     * @param y
     *            Y position of the touch
     */
    @Override
    public boolean onAreaTouched(TouchEvent e, float x, float y)
    {
    	// Set Obstacle.lastTouched for future use.
		if(e.getAction() == TouchEvent.ACTION_DOWN) {
			Obstacle.lastTouched = this;
			Obstacle.lastTouchTime = Framework.self().getEngine().getSecondsElapsedTotal();
    	}
    	
        // if the object is a drag object, then move it according to the
        // location of the user's finger
        if (isDrag) {
            Framework.self().getEngine().vibrate(100);
            float newX = e.getX() - this.getWidth() / 2;
            float newY = e.getY() - this.getHeight() / 2;
            this.setPosition(newX, newY);
            dragVector.x = newX;
            dragVector.y = newY;
            physBody.setTransform(dragVector.mul(1 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT),
                    physBody.getAngle());
            return true;
        }
        // if the object is a poke object, things are a bit more complicated
        else if (isPoke) {
            // only act on depress, not on release or drag
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                Framework.self().getEngine().vibrate(100);
                float time = Framework.self().getEngine().getSecondsElapsedTotal();
                if (this == currentSprite) {
                    // double touch
                    if ((time - lastPokeTime) < pokeDeleteThresh) {
                        // hide sprite, disable physics, make not touchable
                        physBody.setActive(false);
                        Level.current.unregisterTouchArea(this);
                        this.setVisible(false);
                    }
                    // repeat single-touch
                    else {
                        lastPokeTime = time;
                    }
                }
                // new single touch
                else {
                    // record the active sprite
                    currentSprite = this;
                    lastPokeTime = time;
                }
            }
            return true;
        }
        // remember: returning false means that this handler didn't do anything,
        // so we should propagate the event to another handler
        return false;
    }
    
    public void setActionSwipeListener(SwipeListener swipeActionHandler) {
    	Level.current.registerTouchArea(this);
    	this.swipeActionHandler = swipeActionHandler;
    	Level.current.setOnSceneTouchListener(Framework.self());
    }
    
    /**
     * Returns if object is a ramp.
     */
	public boolean isRamp() {
		return isRamp;
	}
	
	/**
     * Returns if object is the hidden obstacle on top of a pit.
     */
	public boolean isPit() {
		return isPit;
	}
	
	/**
     * Returns if object is a killer. This is true if the object will kill
     * the hero if they collide.
     */
	public boolean isKiller() {
		return isKiller;
	}

	public static void addRandomObstacles(int startX, int endX, int num) {
    	if(num > 0)
	    	for(int x = startX; x < endX; x += (endX-startX)/num) {
	    		int rnd = (int) (Math.random()*4);
	    		switch(rnd) {
	    		case 0:
	    			addUpboxObstacle(x);
	    			break;
	    		case 1:
					addDownboxObstacle(x);
					break;
	    		case 2:
	    			addRampObstacle(x, ENEMY_AFTER);
	    			break;
	    		case 3:
	    			addTrapdoorObstacle(x);
	    		}
	    		
	    	}
	}
	
	private static void clearEarlyObstacles(int x, IEntity root) {
		// Start from the end in case detaching elements changes the ordering.
		for(int i = root.getChildCount()-1; i >= 0; i--) {
			IEntity child = root.getChild(i);
			
			if(child.getX() < x) {
				// Avoid detaching floors
				if(child.getX() % (SampleActivity.CAMERA_WIDTH + SampleActivity.INFINITE_LEVEL_LENGTH) != 0)
					child.detachSelf();
			} else {
				clearEarlyObstacles(x, child);
			}
		}
	}

	public static void clearEarlyObstacles(int x) {
		clearEarlyObstacles(x, Level.current);
	}


}