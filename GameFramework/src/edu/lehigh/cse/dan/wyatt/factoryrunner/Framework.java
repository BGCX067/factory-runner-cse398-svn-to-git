/*
 * [TODO: Compatibility] missing behaviors from student games: deathanimation, gameoveranimation, jumpanimation, regularanimation
 *
 * [TODO: Feature] vertical parallax?
 * 
 * [TODO: Feature] Invincible animation?
 * 
 * [TODO: Refactor] have a single timer for everything in ControlFactory?
 * 
 * [TODO: BugFix] bullets and gravity don't interact nicely... it's OK for now, but not ideal
 */
package edu.lehigh.cse.dan.wyatt.factoryrunner;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;

import android.view.KeyEvent;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

/**
 * Every game must extend this Framework to gain access to all of the components
 * of the GameFramework. See SampleActivity for an example.
 * 
 * @author spear
 */
abstract public class Framework extends BaseGameActivity implements IAccelerometerListener, ContactListener,
        IOnSceneTouchListener
{
    /**
     * To use the framework, you must override this to define your camera height
     */
    abstract public int getCameraHeight();

    /**
     * To use the framework, you must override this to define your camera width
     */
    abstract public int getCameraWidth();

    /**
     * To use the framework, you must override this to define your camera
     * orientation
     */
    abstract public ScreenOrientation getCameraOrientation();

    /**
     * To use the framework, you must override this to explain how to configure
     * each level
     */
    public abstract void configureLevel(int whichLevel);

    /**
     * To use the framework, you must override this to indicate the number of
     * levels
     */
    public abstract int getNumLevels();

    /**
     * If you want to use trigger objects, you must override this to define
     * their behavior
     */
    public void onTrigger(int score, int id)
    {
    }

    /**
     * To use the framework, you must override this to indicate the number of
     * help scenes
     */
    public abstract int getHelpScenes();

    /**
     * To use the framework, you must override this to explain how to configure
     * each help scene
     */
    public abstract void configureHelpScene(int whichScene);
    
    public abstract void configureInfiniteLevel(int speed, int x);
    public abstract void configureInfiniteLevel(int speed);

    /**
     * The camera for this game
     * 
     * NB: The only reason we keep this around as a member is to avoid casting
     * it to a SmoothCamera whenever we use it
     */
    SmoothCamera myCamera;

    /**
     * A reference to the currently active Framework
     * 
     * NB: This is like a singleton, except that Android Activities don't have
     * visible constructors
     */
    private static Framework _self;

    /**
     * Accessor for pseudo-singleton pattern
     * 
     * @return The "singleton" Framework for the current game
     */
    static Framework self()
    {
        return _self;
    }

    /**
     * The level-picker and menu system are managed through this
     */
    MenuManager menuManager;

    /**
     * The help scene is managed through this
     */
    HelpScene helpScene;

    /**
     * Handle key presses by dispatching to the appropriate handler. Right now
     * we only deal with the back button.
     */
    @Override
    public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent)
    {
        // if the back key was pressed down, draw the appropriate menu or
        // quit
        if ((pKeyCode == KeyEvent.KEYCODE_BACK) && (pEvent.getAction() == KeyEvent.ACTION_DOWN)) {
            return menuManager.onBack();
        }
        // fall-back case for other key events
        return super.onKeyDown(pKeyCode, pEvent);
    }

    /**
     * Main collision-detection routine: when a contact occurs, this dispatches
     * appropriately so that the more important entity manages the collision
     */
    @Override
    public void beginContact(Contact contact)
    {
        // get the two objects' userdata
        final Object a = contact.getFixtureA().getBody().getUserData();
        final Object b = contact.getFixtureB().getBody().getUserData();

        // NB: we can't actually do this work on the local thread; we need to
        // defer it and run it on the update thread. Otherwise, box2d might
        // crash.
        this.runOnUpdateThread(new Runnable() {
            @Override
            public void run()
            {
                // print a diagnostic message
                String msg1 = a == null ? "null" : a.toString();
                String msg2 = b == null ? "null" : b.toString();
                Debug.d("Collision: " + msg1 + " hit " + msg2);

                // we only do more if both are GFObjects
                if (!(a instanceof PhysicsSprite) || !(b instanceof PhysicsSprite))
                    return;

                // filter so that the one with the smaller type handles the
                // collision
                PhysicsSprite gfoA = (PhysicsSprite) a;
                PhysicsSprite gfoB = (PhysicsSprite) b;
                if (gfoA.myType > gfoB.myType)
                    gfoB.onCollide(gfoA);
                else
                    gfoA.onCollide(gfoB);
                // at this point, we should check for win/loss
            }
        });

    }

    /**
     * Unused collision detection routine
     */
    @Override
    public void endContact(Contact contact)
    {
    }

    /**
     * Unused collision detection routine
     */
    @Override
    public void postSolve(Contact contact, ContactImpulse impulse)
    {
    }

    /**
     * Unused collision detection routine
     */
    @Override
    public void preSolve(Contact contact, Manifold oldManifold)
    {
    }

    /**
     * whenever the tilt of the phone changes, this will be called automatically
     */
    @Override
    public void onAccelerometerChanged(AccelerometerData info)
    {
        Level.onAccelerometerChanged(info);
    }

    /**
     * When the game is loaded, turn on vibration support
     */
    @Override
    public void onLoadComplete()
    {
        mEngine.enableVibrator(this);
    }

    /**
     * turn off music when the game pauses. Without this, phone calls will
     * suffer from music still playing
     */
    @Override
    protected void onPause()
    {
        // if this gets called before onLoadEngine, tune won't be
        // configured, and the app could crash. To avoid problems,
        // we check if tune is not null first.
        if (Level.music != null && Level.music.isPlaying())
            Level.music.pause();
        super.onPause();
    }

    /**
     * When the activity is un-paused, restart the music
     */
    @Override
    protected void onResume()
    {
        if (Level.music != null)
            Level.music.resume();
        super.onResume();
    }

    /*
     * Configure the game engine
     */
    @Override
    public Engine onLoadEngine()
    {
        _self = this;
        // configure the camera.
        myCamera = new SmoothCamera(0, 0, getCameraWidth(), getCameraHeight(), Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY, 1f);

        // define the resolution
        RatioResolutionPolicy resolution = new RatioResolutionPolicy(getCameraWidth(), getCameraHeight());

        // next define the basic info: fullscreen, landscape, resolution,
        // camera
        EngineOptions eo = new EngineOptions(true, getCameraOrientation(), resolution, myCamera);

        // to get better rendering performance. If you skip this line, your game
        // will probably look like crap
        eo.getRenderOptions().disableExtensionVertexBufferObjects();

        // indicate that we may use sound and background music
        eo.setNeedsMusic(true);
        eo.setNeedsSound(true);

        // Flickable objects benefit from this
        eo.getTouchOptions().setRunOnUpdateThread(true);

        // now make and return the engine
        return new Engine(eo);
    }

    /**
     * Load a scene. The menuManager will either draw a splash screen, a
     * chooser, or a playable level
     */
    @Override
    public Scene onLoadScene()
    {
        if (menuManager == null) {
            menuManager = new MenuManager();
            helpScene = new HelpScene();
            PopUpScene.config();
            Controls.config();
        }
        return menuManager.display();
    }

    /**
     * When the scene is touched, this will run and forward to a handler for
     * draggable and pokeable obstacles
     */
    @Override
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent te)
    {
        return Obstacle.handleSceneTouch(pScene, te);
    }

    /**
     * A pretty weak helper method. Since the enable/disable accelerometer
     * methods are protected, and we want to call them from another class, we
     * need this helper
     * 
     * @param active
     *            True if the accelerometer should be turned on
     */
    void configAccelerometer(boolean active)
    {
        if (active)
            enableAccelerometerSensor(this);
        else
            disableAccelerometerSensor();
    }

}
