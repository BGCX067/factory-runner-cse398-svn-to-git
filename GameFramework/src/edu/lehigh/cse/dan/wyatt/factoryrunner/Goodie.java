package edu.lehigh.cse.dan.wyatt.factoryrunner;

import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * Goodies serve two purposes. They are something to collect in order to
 * activate other components of the game (e.g., to win, to activate
 * destinations, and to activate trigger objects). They are also a mechanism of
 * updating the hero, for example by giving more strength or adding
 * invincibility.
 * 
 * Note that goodies can move, using the standard Path interface of
 * PhysicsSprites
 * 
 * @author spear
 */
public class Goodie extends PhysicsSprite
{
    /**
     * Count of the goodies that have been created in this level
     */
    static int goodiescreated;

    /**
     * Count of the goodies that have been collected in this level
     */
    static int goodiescollected;

    /**
     * How much strength does the hero get by collecting this goodie
     */
    int strengthBoost = 0;

    /**
     * How long will the hero be invincible if it collects this goodie
     */
    float invincibilityDuration = 0;

    /**
     * Create a basic goodie. This code should never be called directly. Use
     * addXXX methods instead
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the image
     * @param height
     *            Height of the image
     * @param ttr
     *            Image to display
     * @param stationary
     *            can the goodie move?
     */
    private Goodie(float x, float y, float width, float height, TiledTextureRegion ttr, boolean stationary)
    {
        super(x, y, width, height, ttr, PhysicsSprite.TYPE_GOODIE);

        goodiescreated++;

        // connect sprite to physics world
        BodyType bt = stationary ? BodyType.StaticBody : BodyType.DynamicBody;
        setCirclePhysics(1.0f, 0.3f, 0.6f, bt, false, true, true);
    }

    /**
     * Goodie collision is meaningless, so we leave this method blank
     */
    void onCollide(PhysicsSprite other)
    {
    }

    /**
     * Indicate how long the hero will be invincible after collecting this
     * goodie
     * 
     * @param duration
     *            duration for invincibility
     */
    void setInvincibilityDuration(float duration)
    {
        invincibilityDuration = duration;
    }

    /**
     * Indicate how much strength the hero gains by collecting this goodie
     * 
     * @param boost
     *            Amount of strength boost
     */
    void setStrengthBoost(int boost)
    {
        strengthBoost = boost;
    }

    /**
     * Add a simple Goodie who uses a circle as its fixture and who can be moved
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the image
     * @param height
     *            Height of the image
     * @param name
     *            Name of image file to use
     * 
     * @return The goodie, so that we can update its properties
     */
    static Goodie addMoveableGoodie(float x, float y, float width, float height, String name)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        Goodie Goodie = new Goodie(x, y, width, height, ttr, false);
        Level.current.attachChild(Goodie);
        return Goodie;
    }

    /**
     * Add a simple Goodie who uses a circle as its fixture and who doesn't move
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the image
     * @param height
     *            Height of the image
     * @param name
     *            Name of image file to use
     * 
     * @return The goodie, so that we can update its properties
     */
    static Goodie addStationaryGoodie(float x, float y, float width, float height, String name)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        Goodie Goodie = new Goodie(x, y, width, height, ttr, true);
        Level.current.attachChild(Goodie);
        return Goodie;
    }

    /**
     * Reset goodie statistics when a new level is created
     */
    static void onNewLevel()
    {
        goodiescreated = 0;
        goodiescollected = 0;
    }
}
