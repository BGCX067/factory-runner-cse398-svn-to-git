package edu.lehigh.cse.dan.wyatt.factoryrunner;

import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * Destinations are objects that the hero must reach in order to complete a
 * level
 * 
 * @author spear
 */
public class Destination extends PhysicsSprite
{
    /**
     * Number of heroes who have arrived at any destination yet
     */
    static int arrivals;

    /**
     * number of heroes who can fit at /this/ destination
     */
    int _capacity;

    /**
     * number of heroes already in /this/ destination
     */
    int _holding;

    /**
     * number of goodies that must be collected before this destination accepts
     * any heroes
     */
    int _activationScore;

    /**
     * Create a destination
     * 
     * This should never be called directly.
     * 
     * @param x
     *            X coordinate of top left corner of this destination
     * @param y
     *            X coordinate of top left corner of this destination
     * @param width
     *            Width of this destination
     * @param height
     *            Height of this destination
     * @param ttr
     *            Image to display
     * @param capacity
     *            Number of heroes who can fit in this destination
     * @param activationScore
     *            Number of goodies that must be collected before this
     *            destination accepts any heroes
     * @param isStatic
     *            Can this destination move, or is it at a fixed location
     */
    private Destination(float x, float y, float width, float height, TiledTextureRegion ttr, int capacity,
            int activationScore, boolean isStatic)
    {
        super(x, y, width, height, ttr, PhysicsSprite.TYPE_DESTINATION);

        _capacity = capacity;
        _holding = 0;
        _activationScore = activationScore;

        BodyType bt = isStatic ? BodyType.StaticBody : BodyType.DynamicBody;
        setCirclePhysics(1.0f, 0.3f, 0.6f, bt, false, true, true);
    }

    /**
     * Destinations are the last collision detection entity, so their collision
     * detection code does nothing.
     * 
     * @param other
     *            Other object involved in this collision
     */
    void onCollide(PhysicsSprite other)
    {
    }

    /**
     * Add a simple destination that uses a circle as its fixture and that can
     * move around
     * 
     * @param x
     *            X coordinate of top left corner of this destination
     * @param y
     *            X coordinate of top left corner of this destination
     * @param width
     *            Width of this destination
     * @param height
     *            Height of this destination
     * @param name
     *            Name of the image to display
     * @param capacity
     *            Number of heroes who can fit in this destination
     * @param activationScore
     *            Number of goodies that must be collected before this
     *            destination accepts any heroes
     * @return the Destination, so that it can be manipulated further
     */
    static Destination addDestination(float x, float y, float width, float height, String name, int capacity,
            int activationScore)
    {
        // get the image
        TiledTextureRegion ttr = Media.getImage(name);
        // create a sprite
        Destination dest = new Destination(x, y, width, height, ttr, capacity, activationScore, true);
        // add the destination to the scene
        Level.current.attachChild(dest);
        // return the destination, so it can be modified
        return dest;
    }

    /**
     * Add a simple destination that uses a circle as its fixture and that can
     * move around
     * 
     * @param x
     *            X coordinate of top left corner of this destination
     * @param y
     *            X coordinate of top left corner of this destination
     * @param width
     *            Width of this destination
     * @param height
     *            Height of this destination
     * @param name
     *            Name of the image to display
     * @param capacity
     *            Number of heroes who can fit in this destination
     * @param activationScore
     *            Number of goodies that must be collected before this
     *            destination accepts any heroes
     * @return the Destination, so that it can be manipulated further
     */
    static Destination addMobileDestination(float x, float y, float width, float height, String name, int capacity,
            int activationScore)
    {
        // get the image
        TiledTextureRegion ttr = Media.getImage(name);
        // create a sprite
        Destination dest = new Destination(x, y, width, height, ttr, capacity, activationScore, false);
        // add the destination to the scene
        Level.current.attachChild(dest);
        // return the destination, so it can be modified
        return dest;
    }

    /**
     * Reset all Destinations (called when creating a new level)
     */
    static void onNewLevel()
    {
        arrivals = 0;
    }
}
