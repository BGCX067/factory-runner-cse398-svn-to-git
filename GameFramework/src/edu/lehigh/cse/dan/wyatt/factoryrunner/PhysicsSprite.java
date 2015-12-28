package edu.lehigh.cse.dan.wyatt.factoryrunner;

import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.LoopEntityModifier;
import org.anddev.andengine.entity.modifier.PathModifier;
import org.anddev.andengine.entity.modifier.PathModifier.IPathModifierListener;
import org.anddev.andengine.entity.modifier.PathModifier.Path;
import org.anddev.andengine.entity.modifier.RotationModifier;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

/**
 * PhysicsSprite encapsulates most of the key features we desire of objects in a
 * game:
 * 
 * Motion: these can be stationary, can move based on the phone's tilt, or can
 * follow a path, though not all derivatives of a PhysicsSprite will admit all
 * of these options
 * 
 * Rotation: these can have a fixed rotation or not
 * 
 * Collision Detection: These have collision detection via a callback
 * 
 * PhysicsSprite derives from AnimatedSprite, which means that every
 * PhysicsSprite can employ cell-based animation.
 * 
 * @author spear
 */
abstract public class PhysicsSprite extends AnimatedSprite
{
    // Constants that help us disambiguate among the various sorts of
    // PhysicsSprites that the framework understands
    final static int TYPE_UNKNOWN = 0;
    final static int TYPE_HERO = 1;
    final static int TYPE_ENEMY = 2;
    final static int TYPE_GOODIE = 3;
    final static int TYPE_BULLET = 4;
    final static int TYPE_OBSTACLE = 5;
    final static int TYPE_SVG = 6;
    final static int TYPE_DESTINATION = 7;
    final static int TYPE_INFINITE_TRIGGER = 0xF000;

    /**
     * Type of this sprite; useful for disambiguation in collision detection
     */
    protected int myType = TYPE_UNKNOWN;

    /**
     * Physics body for this object
     */
    protected Body physBody = null;

    /**
     * Does this entity move by tilting the phone?
     */
    protected boolean isTilt = false;

    /**
     * Does this entity follow a path?
     */
    private boolean isPath = false;

    /**
     * Sound to play when a hero collides with this entity
     */
    protected Sound sound = null;

    /**
     * Set the sound to play when a hero collides with this entity
     * 
     * @param name
     *            Name of the sound file
     */
    void setSound(String name)
    {
        Sound s = Media.getSound(name);
        sound = s;
    }

    /**
     * Rather than pooling Vector2 objects, we keep one around for use when
     * dealing with paths
     */
    private Vector2 v = new Vector2();

    /**
     * Each descendant defines this to address any custom logic that we need to
     * deal with on a collision
     * 
     * @param other
     *            The other entity involved in the collision
     */
    abstract void onCollide(PhysicsSprite other);

    /**
     * Create the image for this entity, and set its type
     * 
     * Note that we don't do anything with the physics, since physics needs to
     * be customized
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
     *            Image to use
     * @param type
     *            Type of this entity
     */
    PhysicsSprite(float x, float y, float width, float height, TiledTextureRegion ttr, int type)
    {
        super(x, y, width, height, ttr);
        myType = type;
    }

    /**
     * Make this entity move according to a path
     * 
     * @param path
     *            The path to follow.
     * @param duration
     *            Time it takes to complete the path
     */
    void applyPath(Path path, float duration)
    {
        registerEntityModifier(new LoopEntityModifier(new PathModifier(duration, path)));
        isPath = true;
    }
    
    /**
     * Make this entity move according to a path but do not loop the path.  Perform the path once
     * and leave the object stationary at the last coordinate in the path.
     * 
     * @param path	The path to follow.
     * @param duration	Time it takes to complete the path
     */
    void applyPathOnce(Path path, float duration, IPathModifierListener listener)
    {
        registerEntityModifier(new PathModifier(duration, path, null, listener));
        isPath = true;
    }
    
    /**
     * Make this entity move according to a path but do not loop the path.  Perform the path once
     * and leave the object stationary at the last coordinate in the path.
     * 
     * @param path	The path to follow.
     * @param duration	Time it takes to complete the path
     */
    void applyPathOnce(Path path, float duration)
    {
        registerEntityModifier(new PathModifier(duration, path));
        isPath = true;
    }
    
    void travelTo(float x, float y) {
    	final int xVelocity, yVelocity;
    	final Hero thisHero;
    	float duration = 0.5f;
    	
    	if(myType == TYPE_HERO) {
    		thisHero = (Hero) this;
    		xVelocity = thisHero.getXVelocity();
    		yVelocity = thisHero.getYVelocity();
    		duration = Math.abs(getX()-x)*1.0f/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT/xVelocity;
    	} else {
    		thisHero = null;
    		xVelocity = 0;
    		yVelocity = 0;
    		duration = 0.5f;
    	}
    	
    	applyPathOnce(new Path(2).to(getX(), getY()).to(x, y), duration, new IPathModifierListener() {
			@Override
			public void onPathFinished(PathModifier arg0, IEntity arg1) {
				if(thisHero != null) {
					thisHero.setPath(false);
					thisHero.setAbsoluteVelocity(xVelocity, yVelocity);
				}
			}

			@Override
			public void onPathStarted(PathModifier arg0, IEntity arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPathWaypointFinished(PathModifier arg0, IEntity arg1,
					int arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPathWaypointStarted(PathModifier arg0, IEntity arg1,
					int arg2) {
				// TODO Auto-generated method stub
				
			}
    	});
    }

    /**
     * Make the entity constantly rotate. This is usually only useful for fixed
     * objects.
     * 
     * @param duration
     *            Time it takes to complete one rotation
     */
    void applyRotation(float duration)
    {
        registerEntityModifier(new LoopEntityModifier(new RotationModifier(duration, 0, 360)));
    }

    /**
     * Specify that this entity should have a rectangular physics shape
     * 
     * @param density
     *            Density of the entity
     * @param elasticity
     *            Elasticity of the entity
     * @param friction
     *            Friction of the entity
     * @param type
     *            Is this static or dynamic?
     * @param isBullet
     *            Is this a bullet
     * @param isSensor
     *            Is this a sensor?
     * @param canRotate
     *            Does the entity rotate when it experiences torque from a
     *            collision?
     */
    void setBoxPhysics(float density, float elasticity, float friction, BodyType type, boolean isBullet,
            boolean isSensor, boolean canRotate)
    {
        FixtureDef fd = PhysicsFactory.createFixtureDef(density, elasticity, friction, isSensor);
        physBody = PhysicsFactory.createBoxBody(Level.physics, this, type, fd);
        if (isBullet)
            physBody.setBullet(true);
        Level.physics.registerPhysicsConnector(new PhysicsConnector(this, physBody, true, canRotate));
        physBody.setUserData(this);
    }

    /**
     * Specify that this entity should have a circular physics shape
     * 
     * @param density
     *            Density of the entity
     * @param elasticity
     *            Elasticity of the entity
     * @param friction
     *            Friction of the entity
     * @param type
     *            Is this static or dynamic?
     * @param isBullet
     *            Is this a bullet
     * @param isSensor
     *            Is this a sensor?
     * @param canRotate
     *            Does the entity rotate when it experiences torque from a
     *            collision?
     */
    void setCirclePhysics(float density, float elasticity, float friction, BodyType type, boolean isBullet,
            boolean isSensor, boolean canRotate)
    {
        // define fixture
        FixtureDef fd = PhysicsFactory.createFixtureDef(density, elasticity, friction, isSensor);
        physBody = PhysicsFactory.createCircleBody(Level.physics, this, type, fd);
        if (isBullet)
            physBody.setBullet(true);
        Level.physics.registerPhysicsConnector(new PhysicsConnector(this, physBody, true, canRotate));
        physBody.setUserData(this);
    }

    /**
     * Move an entity's image. This has well-defined behavior, except that when
     * we apply a path to an entity, we need to move its physics body along with
     * the image.
     */
    @Override
    public void setPosition(float x, float y)
    {
        // if we don't have a path, use the default behavior
        if (!isPath) {
            super.setPosition(x, y);
            return;
        }
        // otherwise, move the body based on where the sprite just went
        this.mX = x;
        this.mY = y;
        v.x = (x + mWidth * 0.5f) / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
        v.y = (y + mHeight * 0.5f) / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
        physBody.setTransform(v, 0);
        v.x = 0;
        v.y = 0;
        physBody.setLinearVelocity(v);
        physBody.setAngularVelocity(0);
    }

	public boolean isPath() {
		return isPath;
	}

	public void setPath(boolean isPath) {
		this.isPath = isPath;
	}
}
