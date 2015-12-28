package edu.lehigh.cse.dan.wyatt.factoryrunner;

import java.util.Random;

import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * Enemies are things to be avoided or killed by the hero.
 * 
 * Every enemy can be killed via bullets. They can also be killed by colliding
 * with invincible heroes, or by colliding with a hero whose strength is >= the
 * enemy's strength, though that case results in the hero losing strength.
 * 
 * A level can require all enemies to be killed before the level can be won.
 * 
 * Note that goodies can move, using the standard Path interface of
 * PhysicsSprites, or by using tilt
 * 
 * @author spear
 */
public class Enemy extends PhysicsSprite
{
	/**
	 * Width and height of the standard enemy used after the ramp and above the trapdoor.
	 */
	public static final int HEIGHT = 70;
	public static final int WIDTH = 30;
	
    /**
     * Count the number of enemies that have been created
     */
    static int enemiesCreated;

    /**
     * Count the enemies that have been destroyed
     */
    static int enemiesDestroyed;

    /**
     * A random number generator for use when reproducing enemies
     */
    private static Random generator = new Random(7);

    /**
     * Image used for this enemy
     */
    private TiledTextureRegion _ttr;

    /**
     * Width of this enemy
     */
    private float _width;

    /**
     * Height of this enemy
     */
    private float _height;

    /**
     * Density of this enemy
     */
    private float _density;

    /**
     * Elasticity of this enemy
     */
    private float _elasticity;

    /**
     * Friction of this enemy
     */
    private float _friction;

    /**
     * Does this hero move?
     */
    private boolean isStationary;

    /**
     * Message to display when this enemy kills the last hero
     */
    String killText;

    /**
     * Number of remaining times to reproduce this enemy
     */
    private int reproductions = 0;

    /**
     * Interval between reproductions of this enemy
     */
    private float reproduceDuration = 0;

    /**
     * Number of enemies to create when this enemy reproduces
     */
    private int reproduceSpawn = 0;

    /**
     * Amount of damage this enemy does to a hero on a collision
     */
    int damage = 2;

    /**
     * Is the underlying physics a box?
     */
    boolean _isBox;

    /**
     * Can this enemy be killed by headbutting it
     */
    boolean killByHeadbutt = false;

    /**
     * Add a simple enemy, who uses a circle as its fixture
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of enemy
     * @param height
     *            Height of enemy
     * @param ttr
     *            The image to display
     * @param density
     *            Density of the obstacle. When in doubt, use 1
     * @param elasticity
     *            Elasticity of the obstacle. When in doubt, use 0
     * @param friction
     *            Friction of the obstacle. When in doubt, use 1
     * @param stationary
     *            Is the enemy stationary or can it move?
     * @param isBox
     *            Does the enemy have a box shape, instead of a circle?
     */
    private Enemy(float x, float y, float width, float height, TiledTextureRegion ttr, boolean stationary,
            float density, float elasticity, float friction, boolean isBox)
    {
        super(x, y, width, height, ttr, PhysicsSprite.TYPE_ENEMY);
        enemiesCreated++;
        // record information so we can reproduce this enemy if needed
        killText = "Try again";
        _ttr = ttr;
        _width = width;
        _height = height;
        _density = density;
        _elasticity = elasticity;
        _friction = friction;
        isStationary = stationary;
        _isBox = isBox;
        // connect sprite to physics world
        BodyType bt = stationary ? BodyType.StaticBody : BodyType.DynamicBody;
        if (isBox)
            setBoxPhysics(density, elasticity, friction, bt, false, false, true);
        else
            setCirclePhysics(density, elasticity, friction, bt, false, false, true);
    }

    /**
     * Collision behavior of enemies. Based on our PhysicsSprite numbering
     * scheme, the only concern is to ensure that when a bullet hits this enemy,
     * we kill the enemy and hide the bullet.
     * 
     * @param other
     *            The other entity involved in the collision
     */
    @Override
    void onCollide(PhysicsSprite other)
    {
        // only handle bullets
        if (other.myType == PhysicsSprite.TYPE_BULLET) {
            // play a sound?
            if (Bullet.hitSound != null)
                Bullet.hitSound.play();
            // kill this enemy
            enemiesDestroyed++;
            setVisible(false);
            physBody.setActive(false);
            // hide the bullet
            other.setVisible(false);
            other.physBody.setActive(false);
            // check if this wins the level
            if ((enemiesDestroyed == enemiesCreated) && (Level.victoryType == Level.VICTORY.ENEMYCOUNT)) {
                Framework.self().menuManager.winLevel();
            }
        }
    }

    /**
     * Indicate that this enemy can be killed by headbutting it
     */
    void setKillByHeadbutt()
    {
        killByHeadbutt = true;
        // make the enemy's physics body a sensor to prevent ricochets when the
        // hero kills this
        physBody.getFixtureList().get(0).setSensor(true);
    }

    /**
     * If this enemy kills the last hero of the board, this is the message that
     * will be displayed
     * 
     * @param message
     *            The message to display
     */
    void setKillText(String message)
    {
        killText = message;
    }

    /**
     * Indicate that the enemy should move with the tilt of the phone
     */
    void setMoveByTilting()
    {
        if (!isStationary && !isTilt) {
            Level.accelEntities.add(this);
            isTilt = true;
        }
    }

    /**
     * Set the amount of damage that this enemy does to a hero
     * 
     * @param amount
     *            Amount of damage. Default is 2, since heroes have a default
     *            strength of 1, so that the enemy kills the hero but does not
     *            disappear.
     */
    void setDamage(int amount)
    {
        damage = amount;
    }

    /**
     * Indicate that the enemy reproduces after an interval
     * 
     * @param numReproductions
     *            Number of times that the enemy can reproduce
     * @param timeBetweenReproductions
     *            Time that must pass before the next reproduction happens
     * @param reproductionsPerInterval
     *            Number of enemies to create at each interval
     */
    void setReproductions(int numReproductions, float timeBetweenReproductions, int reproductionsPerInterval)
    {
        // save fields
        reproductions = numReproductions;
        reproduceDuration = timeBetweenReproductions;
        reproduceSpawn = reproductionsPerInterval;
        // set up a timer to handle reproduction
        TimerHandler t = new TimerHandler(reproduceDuration, reproductions > 0, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler th)
            {
                // don't reproduce dead enemies
                if (isVisible() && reproductions > 0) {
                    reproductions--;
                    // use a random number generator to place the new enemies
                    for (int i = 0; i < reproduceSpawn; ++i) {
                        // get a number between 0 and 10
                        int dice = generator.nextInt(10);
                        // this will be the next x/y
                        int nextX, nextY;
                        // should we make an enemy that is really far away?
                        if (dice >= 9) {
                            nextX = generator.nextInt(200) - 50;
                            nextY = generator.nextInt(100) - 50;
                        }
                        else {
                            nextX = generator.nextInt(10) - 5;
                            nextY = generator.nextInt(10) - 5;
                        }
                        // now that we have deltas, add them to the current
                        // enemy's position, but don't draw enemies off scene
                        nextX += getX();
                        nextY += getY();
                        if (nextX < 0)
                            nextX = 0;
                        if (nextY < 0)
                            nextY = 0;
                        if (nextX > Level._width)
                            nextX = Level._width;
                        if (nextY > Level._height)
                            nextY = Level._height;
                        // make the new enemy exactly like this one
                        Enemy e = new Enemy(nextX, nextY, _width, _height, _ttr, isStationary, _density, _elasticity,
                                _friction, _isBox);
                        e.setKillText(killText);
                        e.setDamage(damage);
                        if (isTilt)
                            e.setMoveByTilting();
                        if (killByHeadbutt)
                            e.setKillByHeadbutt();
                        // The child can only reproduce as many times as its
                        // parent has left
                        if (reproductions > 0)
                            e.setReproductions(reproductions, reproduceDuration, reproduceSpawn);
                        Level.current.attachChild(e);
                    }
                    // NB: if a reproduce enemy has a path, the spawned ones
                    // won't. Same for if the reproduce enemy has a custom
                    // animation
                }
            }
        });
        Level.current.registerUpdateHandler(t);
    }

    /**
     * Add a simple enemy, who uses a circle as its fixture and who can move via
     * a path or tilt
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of enemy
     * @param height
     *            Height of enemy
     * @param name
     *            Name of image to display
     * @param density
     *            Density of the obstacle. When in doubt, use 1
     * @param elasticity
     *            Elasticity of the obstacle. When in doubt, use 0
     * @param friction
     *            Friction of the obstacle. When in doubt, use 1
     * 
     * @return the enemy, so we can modify its properties
     */
    static Enemy addMoveableEnemy(float x, float y, float width, float height, String name, float density,
            float elasticity, float friction)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        Enemy enemy = new Enemy(x, y, width, height, ttr, false, density, elasticity, friction, false);
        Level.current.attachChild(enemy);
        return enemy;
    }

    /**
     * Add a simple enemy, who uses a circle as its fixture and who doesn't move
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of enemy
     * @param height
     *            Height of enemy
     * @param name
     *            Name of image to display
     * @param density
     *            Density of the obstacle. When in doubt, use 1
     * @param elasticity
     *            Elasticity of the obstacle. When in doubt, use 0
     * @param friction
     *            Friction of the obstacle. When in doubt, use 1
     * 
     * @return the enemy, so we can modify its properties
     */
    static Enemy addStationaryEnemy(float x, float y, float width, float height, String name, float density,
            float elasticity, float friction)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        Enemy enemy = new Enemy(x, y, width, height, ttr, true, density, elasticity, friction, false);
        Level.current.attachChild(enemy);
        return enemy;
    }

    /**
     * Add a simple enemy, who uses a box as its fixture and who can move via a
     * path or tilt
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of enemy
     * @param height
     *            Height of enemy
     * @param name
     *            Name of image to display
     * @param density
     *            Density of the obstacle. When in doubt, use 1
     * @param elasticity
     *            Elasticity of the obstacle. When in doubt, use 0
     * @param friction
     *            Friction of the obstacle. When in doubt, use 1
     * 
     * @return the enemy, so we can modify its properties
     */
    static Enemy addMoveableBoxEnemy(float x, float y, float width, float height, String name, float density,
            float elasticity, float friction)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        Enemy enemy = new Enemy(x, y, width, height, ttr, false, density, elasticity, friction, true);
        Level.current.attachChild(enemy);
        return enemy;
    }

    /**
     * Add a simple enemy, who uses a box as its fixture and who doesn't move
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of enemy
     * @param height
     *            Height of enemy
     * @param name
     *            Name of image to display
     * @param density
     *            Density of the obstacle. When in doubt, use 1
     * @param elasticity
     *            Elasticity of the obstacle. When in doubt, use 0
     * @param friction
     *            Friction of the obstacle. When in doubt, use 1
     * 
     * @return the enemy, so we can modify its properties
     */
    static Enemy addStationaryBoxEnemy(float x, float y, float width, float height, String name, float density,
            float elasticity, float friction)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        Enemy enemy = new Enemy(x, y, width, height, ttr, true, density, elasticity, friction, true);
        Level.current.attachChild(enemy);
        return enemy;
    }

    /**
     * Reset statistics when a new level is created
     */
    static void onNewLevel()
    {
        enemiesCreated = 0;
        enemiesDestroyed = 0;
    }
}
