package edu.lehigh.cse.dan.wyatt.factoryrunner;

import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * Whenever a level is created, it will have a BulletFactory. The BulletFactory
 * has three key parts:
 *
 * - It defines the Bullet type
 *
 * - It manages a pool of bullets
 *
 * - It handles shooting bullets
 *
 * @author spear
 */
public class Bullet extends PhysicsSprite
{
    /**
     * create a bullet with circular physics
     */
    private Bullet(float x, float y, float width, float height, TiledTextureRegion ttr)
    {
        // configure basic object
        super(x, y, width, height, ttr, PhysicsSprite.TYPE_BULLET);
        // configure physics
        setCirclePhysics(10000, 0, 0, BodyType.DynamicBody, true, true, false);
    }

    /**
     * Standard collision detection routine
     *
     * Since we have a careful ordering scheme, this only triggers on hitting an
     * obstacle, which makes the bullet disappear, or on hitting a bullet, which
     * is a bit funny because one of the two bullets will live.
     *
     * @param other
     *            The other entity involved in the collision
     */
    void onCollide(PhysicsSprite other)
    {
        setVisible(false);
        physBody.setActive(false);
    }

    /**
     * Sound to play when this bullet hits something
     */
    static Sound hitSound;

    /**
     * A collection of all the available bullets
     */
    static Bullet bulletPool[];

    /**
     * The number of bullets in the pool
     */
    static int bulletPoolSize;

    /**
     * Position of next available bullet in the pool
     */
    static int bulletNext;

    /**
     * The x velocity of any bullet we shoot
     */
    static float bulletVelocityX;

    /**
     * The y velocity of any bullet we shoot
     */
    static float bulletVelocityY;

    /**
     * When shooting, we start from the top left corner of the shooter, and then
     * use this to determine the initial x position of the bullet
     */
    static float _offsetX;

    /**
     * When shooting, we start from the top left corner of the shooter, and then
     * use this to determine the initial y position of the bullet
     */
    static float _offsetY;

    /**
     * The physics system requires us to communicate with it via Vector2
     * objects. Rather than pool them and constantly be re-making them, we can
     * just keep a few around for our own purposes. This is for setting the
     * velocity of a bullet.
     */
    static final Vector2 bulletVelocity = new Vector2();

    /**
     * The physics system requires us to communicate with it via Vector2
     * objects. Rather than pool them and constantly be re-making them, we can
     * just keep a few around for our own purposes. This is for setting the
     * position of a bullet.
     */
    static final Vector2 bulletPosition = new Vector2();

    /**
     * The physics system requires us to communicate with it via Vector2
     * objects. Rather than pool them and constantly be re-making them, we can
     * just keep a few around for our own purposes. This is for setting forces
     * on a bullet, so that we can negate gravity
     */
    static final Vector2 bulletForce = new Vector2();

    /**
     * Describe the behavior of bullets in a scene.
     *
     * You must call this if you intend to use bullets in your scene.
     *
     * @param size
     *            number of bullets that can be shot at once
     *
     * @param width
     *            width of a bullet
     *
     * @param height
     *            height of a bullet
     *
     * @param name
     *            name image to use for bullets
     *
     * @param velocityX
     *            x velocity of bullets
     *
     * @param velocityY
     *            y velocity of bullets
     */
    static void configBullets(int size, int width, int height, String name, float velocityX, float velocityY,
            float offsetX, float offsetY)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        // set up the pool
        bulletPool = new Bullet[size];
        for (int i = 0; i < size; ++i) {
            bulletPool[i] = new Bullet(-100, -100, width, height, ttr);
            bulletPool[i].setVisible(false);
            bulletPool[i].physBody.setBullet(true);
            bulletPool[i].physBody.setActive(false);
            Level.current.attachChild(bulletPool[i]);
        }
        bulletNext = 0;
        bulletPoolSize = size;
        // record vars that describe how the bullet behaves
        bulletVelocityX = velocityX;
        bulletVelocityY = velocityY;
        _offsetX = offsetX;
        _offsetY = offsetY;
        // set up the bullet force. This is meant to counteract the effect of
        // gravity a little bit.
        bulletForce.x = -5 * Level._initXGravity;
        bulletForce.y = -5 * Level._initYGravity;
    }

    /**
     * Shoot a bullet
     *
     * @param xx
     *            x coordinate of the top left corner of the shooter
     *
     * @param yy
     *            y coordinate of the top left corner of the shooter
     */
    static void shoot(float xx, float yy)
    {
        // is there an available bullet?
        if (bulletPool[bulletNext].isVisible())
            return;
        // calculate offset for starting position of bullet
        float x = xx + _offsetX;
        float y = yy + _offsetY;
        // get the next bullet
        Bullet b = bulletPool[bulletNext];
        bulletNext = (bulletNext + 1) % bulletPoolSize;
        // put the bullet on the screen and place it in the physics world
        b.setPosition(x, y);
        bulletPosition.x = x;
        bulletPosition.y = y;
        bulletPosition.mul(1 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
        b.physBody.setActive(true);
        b.physBody.setTransform(bulletPosition, 0);
        // give the bullet velocity
        bulletVelocity.x = bulletVelocityX;
        bulletVelocity.y = bulletVelocityY;
        b.physBody.setLinearVelocity(bulletVelocity);
        b.physBody.applyForce(bulletForce, b.physBody.getLocalCenter());
        // make the bullet visible
        b.setVisible(true);
    }

    /**
     * Specify a sound to play when the bullet hits something (e.g., an enemy)
     *
     * @param name
     *            Name of the sound file to play
     */
    static void setHitSound(String name)
    {
        Sound s = Media.getSound(name);
        hitSound = s;
    }
}
