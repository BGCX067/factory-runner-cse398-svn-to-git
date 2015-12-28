package edu.lehigh.cse.dan.wyatt.factoryrunner;

import java.util.ArrayList;

import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.Debug;

import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

/**
 * Heroes are the focus of games. They must achieve a certain goal in order for
 * a level to complete successfully
 * 
 * Heroes default to moving by tilt, but can be made to have a fixed velocity
 * instead. They can shoot, jump, and headbutt (this can also be used to
 * simulate crawling, ducking, or rolling). They can be made invincible, they
 * have strength, and they can be made to only start moving when pressed. They
 * are also the focal point for almost all of the collision detection code.
 * 
 * @author spear
 */
public class Hero extends PhysicsSprite
{
	/** 
	 * Dimensions of main hero that sits on the left side of the screen
	 */
	public static final int HEIGHT = 40;
	public static final int WIDTH = 40;
	
    /**
     * Track the number of heroes that have been created
     */
    private static int heroesCreated;

    /**
     * Track the number of heroes that have been destroyed
     */
    private static int heroesDestroyed;

    /**
     * Store all heroes, so that we can hide them all at the end of a level
     */
    private static ArrayList<Hero> heroes = new ArrayList<Hero>();

    /**
     * Track the last hero that was created
     * 
     * In levels with only one hero (most games), this lets us keep track of the
     * hero to operate with when we jump, headbutt, shoot, etc
     */
    static Hero lastHero;

    /**
     * When the hero jumps, this specifies the amount of jump impulse in the X
     * dimension
     */
    private static int xJumpImpulse = 0;

    /**
     * When the hero jumps, this specifies the amount of jump impulse in the Y
     * dimension
     */
    private static int yJumpImpulse = 0;

    /**
     * Store each hero's image, so that we can flip it when the hero moves
     * backward
     */
    TiledTextureRegion _ttr;

    /**
     * Does the hero's image flip when the hero moves backwards?
     */
    boolean reverseFace = false;

    /**
     * Is the hero currently in headbutt mode?
     */
    private boolean headbutt = false;

    /**
     * Does the hero jump when we touch it?
     */
    private boolean isTouchJump = false;

    /**
     * Does the hero shoot a bullet when we touch it?
     */
    private boolean isTouchShoot = false;

    /**
     * Does the hero start moving when we touch it?
     */
    private boolean touchAndGo = false;

    /**
     * Strength of the hero
     * 
     * This determines how many collisions the hero can sustain before it dies.
     * The default is 1, and the default enemy power is 2, so that the default
     * behavior is for the hero to die on any collision with an enemy
     */
    int strength = 1;

    /**
     * Velocity in X dimension for this hero
     */
    private int xVelocity = 0;

    /**
     * Velocity in Y dimension for this hero
     */
    private int yVelocity = 0;

    /**
     * Time when the hero's invincibility runs out
     */
    private float invincibleUntil = 0;

    public int getXVelocity() {
		return xVelocity;
	}

	public int getYVelocity() {
		return yVelocity;
	}

	/**
     * Time of last touch of the hero, for limiting the frequency of shooting
     */
    private float lastTouch;

    /**
     * Track if the hero is in the air, so that it can't jump when it isn't
     * touching anything.
     * 
     * This does not quite work as desired, but is good enough for our demo
     */
    private boolean inAir = false;

    /**
     * Create a hero
     * 
     * This is an internal method. Use the addXXX methods instead
     * 
     * @param x
     *            X coordinate of top left
     * @param y
     *            Y coordinate of top left
     * @param width
     *            Width of the image
     * @param height
     *            Height of the image
     * @param ttr
     *            Image to display
     */
    private Hero(float x, float y, float width, float height, TiledTextureRegion ttr)
    {
        super(x, y, width, height, ttr, PhysicsSprite.TYPE_HERO);
     
        _ttr = ttr;

        // log that we made a hero
        heroesCreated++;
    }

    /**
     * Take the hero out of headbutt mode
     */
    void headbuttOff()
    {
        headbutt = false;
        physBody.setTransform(physBody.getPosition(), 0);
        this.setRotation(0);
    }

    /**
     * Put the hero in headbutt mode
     */
    void headbuttOn()
    {
        headbutt = true;
        physBody.setTransform(physBody.getPosition(), 3.14159f / 2);
        this.setRotation(90);
    }

    /**
     * Make the hero jump, unless it is in the air
     */
    void jump()
    {
        if (inAir)
            return;
        Vector2 v = physBody.getLinearVelocity();
        v.y += yJumpImpulse;
        v.x += xJumpImpulse;
        physBody.setLinearVelocity(v);
        inAir = true;
    }

    /**
     * Indicate that touching this hero should make it jump
     */
    void makeTouchJumpable()
    {
        isTouchJump = true;
        Level.current.registerTouchArea(this);
    }


    /**
     * Indicate that touching this hero should make it shoot
     */
    void makeTouchShoot()
    {
        isTouchShoot = true;
        Level.current.registerTouchArea(this);
    }

    /**
     * Code to run when the hero is touched
     * 
     * @param e
     *            The type of touch
     * @param x
     *            X coordinate of the touch
     * @param y
     *            Y coordinate of the touch
     */
    @Override
    public boolean onAreaTouched(TouchEvent e, float x, float y)
    {
        // swallow rapid touches
        float now = Framework.self().getEngine().getSecondsElapsedTotal();
        if (now < lastTouch + 0.25)
            return false;
        // jump?
        if (isTouchJump)
            jump();
        // start moving?
        if (touchAndGo) {
            setVelocity(xVelocity, yVelocity);
            // turn off touchAndGo, so we can't double-touch
            touchAndGo = false;
        }
        // shoot?
        if (isTouchShoot)
            Bullet.shoot(getX(), getY());
        return true;
    }

    /**
     * Describe what to do when a hero hits another entity.  This handles all the collisions
     * with pits, ramps, and killers.
     * 
     * @param other
     *            The other entity involved in this collision
     */
    void onCollide(PhysicsSprite other)
    {
        Debug.d("Collision with " + other + " of type " + other.myType);
        // play a sound when we hit this thing?
        if (other.sound != null)
            other.sound.play();
        
        // Collision with infinite level trigger
        if((other.myType & TYPE_INFINITE_TRIGGER) == TYPE_INFINITE_TRIGGER) {
        	int speed = other.myType & ~TYPE_INFINITE_TRIGGER;
        	Log.i("OO", "Collision with infinite trigger of speed " + speed);
        	Framework.self().configureInfiniteLevel(speed, (int) getX());
        }

        // logic for collisions with enemies
        if (other.myType == TYPE_ENEMY) {
            Enemy e = (Enemy) other;
            // can we kill it via invincibility?
            if (invincibleUntil > Framework.self().getEngine().getSecondsElapsedTotal()) {
                // kill the enemy
                e.setVisible(false);
                e.physBody.setActive(false);
                Enemy.enemiesDestroyed++;
                if ((Enemy.enemiesDestroyed == Enemy.enemiesCreated) && (Level.victoryType == Level.VICTORY.ENEMYCOUNT)) {
                    Framework.self().menuManager.winLevel();
                }
            }
            // kill by headbutting?
            else if (headbutt && e.killByHeadbutt) {
                // kill the enemy
                e.setVisible(false);
                e.physBody.setActive(false);
                Enemy.enemiesDestroyed++;
                if ((Enemy.enemiesDestroyed == Enemy.enemiesCreated) && (Level.victoryType == Level.VICTORY.ENEMYCOUNT)) {
                    Framework.self().menuManager.winLevel();
                }
            }
            // when we can't kill it by losing strength
            else if (e.damage >= strength) {
                // turn off physics updates for the hero, and hide him
                setVisible(false);
                physBody.setActive(false);
                // increase the number of dead heroes
                heroesDestroyed++;
                if (heroesDestroyed == heroesCreated) {
                	Level.pauseScreenRabbit(getX(), getY());
                    Framework.self().menuManager.loseLevel(e.killText);
                }
            }
            // when we can kill it by losing strength
            else {
                strength -= e.damage;
                // kill the enemy
                e.setVisible(false);
                e.physBody.setActive(false);
                Enemy.enemiesDestroyed++;
                if ((Enemy.enemiesDestroyed == Enemy.enemiesCreated) && (Level.victoryType == Level.VICTORY.ENEMYCOUNT)) {
                    Framework.self().menuManager.winLevel();
                }
            }
        }
        // collision with destination
        if (other.myType == PhysicsSprite.TYPE_DESTINATION) {
            Destination d = (Destination) other;
            // only do something if the hero has enough goodies and there's
            // room in the destination
            int currentGoodieScore = Goodie.goodiescollected;
            if ((currentGoodieScore >= d._activationScore) && (d._holding < d._capacity)) {
                // hide the hero, disable the hero's motion, and check if the
                // level is complete
                Destination.arrivals++;
                d._holding++;
                physBody.setActive(false);
                setVisible(false);
                if ((Level.victoryType == Level.VICTORY.DESTINATION) && (Destination.arrivals >= Level.victoryVal)) {
                    Framework.self().menuManager.winLevel();
                }
            }
        }
        // collision with obstacles
        if (other.myType == PhysicsSprite.TYPE_OBSTACLE) {
        	/* If killer is true, the user will lose the game */
        	boolean killer = false;
        	
            Obstacle o = (Obstacle) other;
            
            /* If the obstacle is a ramp and it's completely down on the floor,
             * make the user "jump".  Otherwise, kill the user.
             */
            if(o.isRamp()) {
            	if(o.getY() > Level.FLOOR_TOP-Obstacle.RAMP_HEIGHT-5)
            		travelTo(o.getX()+o.getWidth(),getY()-o.getHeight()-15);
            		//setVelocity(0, -9);
            	else
            		killer = true;
            }
            
            /* If the obstacle is the invisible one on top of a pit, kill
             * the user.
             */
            if(o.isPit()) {
            	//TODO: animate hero down into pit
            	killer = true;
            }
            
            if(o.isKiller() || killer) {
            	setVisible(false);
                physBody.setActive(false);
                Framework.self().menuManager.loseLevel("Try Again");
                Level.pauseScreenRabbit(o.getX(), o.getY());
            }
            
            // trigger obstacles cause us to run custom code
            if (o.isTrigger) {
                // check if trigger is activated, if so, disable it and run code
                if (o.triggerActivation <= Goodie.goodiescollected) {
                    o.setVisible(false);
                    o.physBody.setActive(false);
                    Framework.self().onTrigger(Goodie.goodiescollected, o.triggerID);
                }
            }
            // regular obstacles
            else {
                // damp obstacles to change the hero physics in funny ways
                if (o.isDamp) {
                    Vector2 v = physBody.getLinearVelocity();
                    v.x *= o.dampFactor;
                    v.y *= o.dampFactor;
                    physBody.setLinearVelocity(v);
                }
                // otherwise, it's probably a wall, so mark us not in the air so
                // we can do more jumps
                else {
                    inAir = false;
                }
            }
        }
        // ignore bullets
        if (other.myType == PhysicsSprite.TYPE_BULLET) {
            // demonstrate how to print debug messages to logcat
            Debug.d("hero collided with bullet");
        }
        // SVG are like regular obstacles: reenable jumps
        if (other.myType == PhysicsSprite.TYPE_SVG) {
            inAir = false;
        }
        // collect goodies
        if (other.myType == PhysicsSprite.TYPE_GOODIE) {
            Goodie g = (Goodie) other;
            // hide the goodie
            g.setVisible(false);
            g.physBody.setActive(false);
            // count this goodie
            Goodie.goodiescollected++;
            // update strength
            strength += g.strengthBoost;
            // deal with invincibility
            if (g.invincibilityDuration > 0) {
                float newExpire = Framework.self().getEngine().getSecondsElapsedTotal() + g.invincibilityDuration;
                if (newExpire > invincibleUntil)
                    invincibleUntil = newExpire;
            }
            // possibly win the level
            if ((Level.victoryType == Level.VICTORY.GOODIECOUNT) && (Level.victoryVal <= Goodie.goodiescollected)) {
                Framework.self().menuManager.winLevel();
            }
        }
    }

    /**
     * Indicate that this hero's image should be reversed when it is moving in
     * the negative x direction. This only applies to the last hero created
     */
    void setCanGoBackwards()
    {
        reverseFace = true;
    }

    /**
     * Give the hero more strength than the default, so it can survive more
     * collisions with enemies
     * 
     * @param amount
     *            The new strength of the hero
     */
    void setStrength(int amount)
    {
        strength = amount;
    }

    /**
     * Indicate that upon a touch, this hero should begin moving with a specific
     * velocity
     * 
     * @param x
     *            Velocity in X dimension
     * @param y
     *            Velocity in Y dimension
     */
    void setTouchAndGo(int x, int y)
    {
        touchAndGo = true;
        xVelocity = x;
        yVelocity = y;
        Level.current.registerTouchArea(this);
    }

    /**
     * Set the velocity of this hero
     * 
     * @param x
     *            Velocity in X dimension
     * @param y
     *            Velocity in Y dimension
     */
    void setVelocity(int x, int y)
    {
        xVelocity = x;
        yVelocity = y;
        Vector2 v = physBody.getLinearVelocity();
        v.y += y;
        v.x += x;
        physBody.setLinearVelocity(v);
    }
    
    void setAbsoluteVelocity(int x, int y)
    {
        xVelocity = x;
        yVelocity = y;
        Vector2 v = new Vector2(x, y);
        physBody.setLinearVelocity(v);
    }

    /**
     * Draw a hero on the screen
     * 
     * The hero will have a circle as its underlying shape, and it will rotate
     * due to physics. By default, it will move according to the tilt of the
     * phone. Note, too, that the last hero created is the most important one.
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the hero
     * @param height
     *            Height of the hero
     * @param name
     *            Name of the image to use for this hero
     * @param density
     *            Density of the obstacle. When in doubt, use 1
     * @param elasticity
     *            Elasticity of the obstacle. When in doubt, use 0
     * @param friction
     *            Friction of the obstacle. When in doubt, use 1
     * 
     * @return the hero, so it can be modified further
     */
    static Hero addHero(float x, float y, float width, float height, String name, float density, float elasticity,
            float friction)
    {
        // get the image
        TiledTextureRegion ttr = Media.getImage(name);
        // create a sprite
        Hero hero = new Hero(x, y, width, height, ttr);
        hero.setCirclePhysics(density, elasticity, friction, BodyType.DynamicBody, false, false, true);

        // add the hero to the scene
        Level.current.attachChild(hero);

        // add the hero to the list of entities that get moved when the phone
        // tilts
        Level.accelEntities.add(hero);
        heroes.add(hero);

        // let the camera follow this hero
        Framework.self().myCamera.setChaseEntity(hero);

        // save this as the most recent hero
        lastHero = hero;

        // return the hero, so it can be modified
        return hero;
    }

    /**
     * Draw a hero on the screen
     * 
     * The hero will have a circle as its underlying shape, and it will not
     * rotate due to physics. By default, it will move according to the tilt of
     * the phone. Note, too, that the last hero created is the most important
     * one.
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the hero
     * @param height
     *            Height of the hero
     * @param name
     *            Name of the image to use for this hero
     * @param density
     *            Density of the obstacle. When in doubt, use 1
     * @param elasticity
     *            Elasticity of the obstacle. When in doubt, use 0
     * @param friction
     *            Friction of the obstacle. When in doubt, use 1
     * @return the hero, so it can be modified further
     */
    static Hero addNoRotateHero(float x, float y, float width, float height, String name, float density,
            float elasticity, float friction)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        // create a sprite
        Hero hero = new Hero(x, y, width, height, ttr);
        // connect to physics world
        hero.setCirclePhysics(density, elasticity, friction, BodyType.DynamicBody, false, false, false);

        // add the hero to the scene
        Level.current.attachChild(hero);

        // add the hero to the list of entities that get moved when the phone
        // tilts
        Level.accelEntities.add(hero);
        heroes.add(hero);

        // Let the camera follow this hero
        Framework.self().myCamera.setChaseEntity(hero);

        // save this as the last hero created
        lastHero = hero;

        // return the hero, so it can be modified
        return hero;
    }

    /**
     * Draw a hero on the screen
     * 
     * The hero will have a Box as its underlying shape, and it will rotate due
     * to physics. By default, it will move according to the tilt of the phone.
     * Note, too, that the last hero created is the most important one.
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the hero
     * @param height
     *            Height of the hero
     * @param name
     *            Name of the image to use for this hero
     * @param density
     *            Density of the obstacle. When in doubt, use 1
     * @param elasticity
     *            Elasticity of the obstacle. When in doubt, use 0
     * @param friction
     *            Friction of the obstacle. When in doubt, use 1
     * 
     * @return the hero, so it can be modified further
     */
    static Hero addBoxHero(float x, float y, float width, float height, String name, float density, float elasticity,
            float friction)
    {
        // get the image
        TiledTextureRegion ttr = Media.getImage(name);
        // create a sprite
        Hero hero = new Hero(x, y, width, height, ttr);
        hero.setCirclePhysics(density, elasticity, friction, BodyType.DynamicBody, false, false, true);

        // add the hero to the scene
        Level.current.attachChild(hero);

        // add the hero to the list of entities that get moved when the phone
        // tilts
        Level.accelEntities.add(hero);
        heroes.add(hero);

        // let the camera follow this hero
        Framework.self().myCamera.setChaseEntity(hero);

        // save this as the most recent hero
        lastHero = hero;

        // return the hero, so it can be modified
        return hero;
    }

    /**
     * Draw a hero on the screen
     * 
     * The hero will have a box as its underlying shape, and it will not rotate
     * due to physics. By default, it will move according to the tilt of the
     * phone. Note, too, that the last hero created is the most important one.
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the hero
     * @param height
     *            Height of the hero
     * @param name
     *            Name of the image to use for this hero
     * @param density
     *            Density of the obstacle. When in doubt, use 1
     * @param elasticity
     *            Elasticity of the obstacle. When in doubt, use 0
     * @param friction
     *            Friction of the obstacle. When in doubt, use 1
     * @return the hero, so it can be modified further
     */
    static Hero addNoRotateBoxHero(float x, float y, float width, float height, String name, float density,
            float elasticity, float friction)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        // create a sprite
        Hero hero = new Hero(x, y, width, height, ttr);
        // connect to physics world
        hero.setBoxPhysics(density, elasticity, friction, BodyType.DynamicBody, false, false, false);

        // add the hero to the scene
        Level.current.attachChild(hero);

        // add the hero to the list of entities that get moved when the phone
        // tilts
        Level.accelEntities.add(hero);
        heroes.add(hero);

        // Let the camera follow this hero
        Framework.self().myCamera.setChaseEntity(hero);

        // save this as the last hero created
        lastHero = hero;

        // return the hero, so it can be modified
        return hero;
    }

    /**
     * Hide all heroes
     * 
     * This is called at the end of a level, so that the gameplay doesn't do odd
     * things after the game is over
     */
    static void hideAll()
    {
        for (Hero h : heroes) {
            h.setVisible(false);
            h.physBody.setActive(false);
        }
    }

    /**
     * Reset the Hero statistics whenever a new level is created
     */
    static void onNewLevel()
    {
        heroesCreated = 0;
        heroesDestroyed = 0;
        xJumpImpulse = 0;
        yJumpImpulse = 0;
        heroes.clear();
    }

    /**
     * Specify the X and Y force to apply to the hero whenever it is instructed
     * to jump
     * 
     * @param x
     *            Force in X direction
     * @param y
     *            Force in Y direction
     */
    static void setJumpImpulses(int x, int y)
    {
        xJumpImpulse = x;
        yJumpImpulse = y;
    }

    /**
     * Put something on the screen that is invisible and that moves at a fixed
     * velocity.
     * 
     * It is useful to draw one of these after drawing a hero with a fixed
     * velocity, so that the camera will follow this. Doing so keeps the hero
     * from being centered, which is sometimes desirable.
     * 
     * @param x
     *            Initial X coordinate
     * @param y
     *            Initial Y coordinate
     * @param xVelocity
     *            Speed in X direction
     * @param yVelocity
     *            Speed in Y direction
     */
    static void addRabbit(int x, int y, float xVelocity, float yVelocity)
    {
        AnimatedSprite s = new AnimatedSprite(x, y, 1, 1, MenuManager.ttrInvis);
        FixtureDef f = PhysicsFactory.createFixtureDef(0, 0, 0);
        Body b = PhysicsFactory.createCircleBody(Level.physics, s, BodyType.KinematicBody, f);
        Level.physics.registerPhysicsConnector(new PhysicsConnector(s, b, true, false));
        b.setLinearVelocity(xVelocity, yVelocity);
        Framework.self().myCamera.setChaseEntity(s);
    }
}
