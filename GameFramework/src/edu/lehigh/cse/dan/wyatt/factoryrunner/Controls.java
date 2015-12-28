package edu.lehigh.cse.dan.wyatt.factoryrunner;

import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

import com.badlogic.gdx.math.Vector2;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.MotionEvent;

/**
 * Controls are entities that are placed on a heads-up display so that they can
 * be touched at any time during gameplay. We use controls to move the
 * character, make it jump/duck/shoot, to zoom in and out, and to display
 * stopwatches, countdowns, strength meters, and goodie counts
 * 
 * @author spear
 */
public class Controls
{

    /**
     * Configure the font that is used by methods of this class
     */
    static void config()
    {
        BitmapTextureAtlas bta = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
        hudFont = new Font(bta, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32, true, Color.WHITE);
        Framework.self().getTextureManager().loadTexture(bta);
        Framework.self().getFontManager().loadFont(hudFont);
    }

    /**
     * Font to use to display control-related messages
     */
    static Font hudFont = null;

    /**
     * heads-up display where we place buttons
     */
    static HUD hud;

    /**
     * A flag for disabling timers (e.g., when the game is over)
     */
    static boolean timeractive;

    /**
     * Store the duration between when the program started and when the current
     * level started, so that we can reuse the timer from one level to the next
     */
    private static float timerDelta;
    private static int secondsSurvived;

    /**
     * Controls is a pure static class, and should never be constructed
     * explicitly
     */
    private Controls()
    {
    }

    /**
     * When we win or lose a game, we need to reset the HUD to get rid of all
     * the buttons currently on the HUD
     */
    static void resetHUD()
    {
        hud = new HUD();
        Framework.self().myCamera.setHUD(hud);
    }

    /**
     * Add a countdown timer to the screen. When time is up, the level ends in
     * defeat
     * 
     * @param timeout
     *            Starting value of the timer
     */
    public static void addCountdown(float timeout)
    {
        // figure out how much time between right now, and when the program
        // started.
        timerDelta = Framework.self().getEngine().getSecondsElapsedTotal();

        // record how many seconds to complete this level
        final float countdownFrom = timeout;

        // turn on the timer
        timeractive = true;

        // make the text object to display
        final ChangeableText elapsedText = new ChangeableText(220, 10, hudFont, "", "XXXX".length());

        // set up an autoupdate for the time every .05 seconds
        TimerHandler HUDTimer = new TimerHandler(1 / 20.0f, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler)
            {
                // get the elapsed time for this level
                float newtext = Framework.self().getEngine().getSecondsElapsedTotal() - timerDelta;
                newtext = countdownFrom - newtext;
                // figure out if time is up
                if (newtext < 0) {
                    newtext = 0;
                    Framework.self().menuManager.loseLevel("Time Up");
                }
                // update the text
                if (timeractive)
                    elapsedText.setText("" + (int) newtext);
            }
        });
        Level.current.registerUpdateHandler(HUDTimer);

        // Add the text to the HUD
        Framework.self().myCamera.getHUD().attachChild(elapsedText);
    }

    /**
     * Add a button that moves the hero downward
     * 
     * @param x
     *            X coordinate of top left corner of the button
     * @param y
     *            Y coordinate of top left corner of the button
     * @param width
     *            Width of the button
     * @param height
     *            Height of the button
     * @param name
     *            Name of the image to use for this button
     * @param rate
     *            Rate at which the hero moves
     */
    static void addDownButton(int x, int y, int width, int height, String name, final int rate)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        AnimatedSprite s = new AnimatedSprite(x, y, width, height, ttr) {
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                Hero h = Hero.lastHero;
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    Vector2 v = h.physBody.getLinearVelocity();
                    v.y = rate;
                    h.physBody.setLinearVelocity(v);
                    return true;
                }
                if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    Vector2 v = h.physBody.getLinearVelocity();
                    v.y = 0;
                    h.physBody.setLinearVelocity(v);
                    return true;
                }
                return false;
            }

        };
        hud.attachChild(s);
        hud.registerTouchArea(s);
    }

    /**
     * Add a button that moves the hero upward
     * 
     * @param x
     *            X coordinate of top left corner of the button
     * @param y
     *            Y coordinate of top left corner of the button
     * @param width
     *            Width of the button
     * @param height
     *            Height of the button
     * @param name
     *            Name of the image to use for this button
     * @param rate
     *            Rate at which the hero moves
     */
    static void addUpButton(int x, int y, int width, int height, String name, final int rate)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        AnimatedSprite s = new AnimatedSprite(x, y, width, height, ttr) {
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                Hero h = Hero.lastHero;
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    Vector2 v = h.physBody.getLinearVelocity();
                    v.y = -1 * rate;
                    h.physBody.setLinearVelocity(v);
                    return true;
                }
                if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    Vector2 v = h.physBody.getLinearVelocity();
                    v.y = 0;
                    h.physBody.setLinearVelocity(v);
                    return true;
                }
                return false;
            }

        };
        hud.attachChild(s);
        hud.registerTouchArea(s);
    }

    /**
     * Add a button that moves the hero left
     * 
     * @param x
     *            X coordinate of top left corner of the button
     * @param y
     *            Y coordinate of top left corner of the button
     * @param width
     *            Width of the button
     * @param height
     *            Height of the button
     * @param name
     *            Name of the image to use for this button
     * @param rate
     *            Rate at which the hero moves
     */
    static void addLeftButton(int x, int y, int width, int height, String name, final int rate)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        AnimatedSprite s = new AnimatedSprite(x, y, width, height, ttr) {
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                Hero h = Hero.lastHero;
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    Vector2 v = h.physBody.getLinearVelocity();
                    v.x = -1 * rate;
                    h.physBody.setLinearVelocity(v);
                    if (h.reverseFace) {
                        h._ttr.setFlippedHorizontal(true);
                    }

                    return true;
                }
                if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    Vector2 v = h.physBody.getLinearVelocity();
                    v.x = 0;
                    h.physBody.setLinearVelocity(v);

                    return true;
                }
                return false;
            }
        };
        hud.attachChild(s);
        hud.registerTouchArea(s);
    }

    /**
     * Add a button that moves the hero right
     * 
     * @param x
     *            X coordinate of top left corner of the button
     * @param y
     *            Y coordinate of top left corner of the button
     * @param width
     *            Width of the button
     * @param height
     *            Height of the button
     * @param name
     *            Name of the image to use for this button
     * @param rate
     *            Rate at which the hero moves
     */
    static void addRightButton(int x, int y, int width, int height, String name, final int rate)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        AnimatedSprite s = new AnimatedSprite(x, y, width, height, ttr) {
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                Hero h = Hero.lastHero;
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    Vector2 v = h.physBody.getLinearVelocity();
                    v.x = rate;
                    h.physBody.setLinearVelocity(v);

                    if (h.reverseFace) {
                        h._ttr.setFlippedHorizontal(false);
                    }
                    return true;
                }
                if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    Vector2 v = h.physBody.getLinearVelocity();
                    v.x = 0;
                    h.physBody.setLinearVelocity(v);
                    return true;
                }
                return false;
            }
        };
        hud.attachChild(s);
        hud.registerTouchArea(s);
    }

    /**
     * Add a count of the current number of goodies
     * 
     * @param max
     *            If this is > 0, then the message wil be of the form XX/max
     *            instead of just XX
     */
    public static void addGoodieCount(int max)
    {
        // turn on the timer
        timeractive = true;

        final String suffix = (max > 0) ? "/" + max + " Goodies" : " Goodies";

        // make the text object to display
        final ChangeableText elapsedText = new ChangeableText(220, 280, hudFont, "", "XXX/XXX Goodies".length());

        // set up an autoupdate for the time every .05 seconds
        TimerHandler HUDTimer = new TimerHandler(1 / 20.0f, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler)
            {
                // get elapsed time for this level
                String newtext = "" + Goodie.goodiescollected + suffix;

                // update the text
                if (timeractive)
                    elapsedText.setText(newtext);
            }
        });
        Level.current.registerUpdateHandler(HUDTimer);

        // add the text to the hud
        Framework.self().myCamera.getHUD().attachChild(elapsedText);
    }

    /**
     * Add a button that puts the hero into headbutt mode when depressed, and
     * regular mode when released
     * 
     * @param x
     *            X coordinate of top left corner of the button
     * @param y
     *            Y coordinate of top left corner of the button
     * @param width
     *            Width of the button
     * @param height
     *            Height of the button
     * @param name
     *            Name of the image to use for this button
     */
    static void addHeadbuttButton(int x, int y, int width, int height, String name)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        AnimatedSprite s = new AnimatedSprite(x, y, width, height, ttr) {
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                Hero h = Hero.lastHero;
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    h.headbuttOn();
                    return true;
                }
                if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    h.headbuttOff();
                    return true;
                }
                return false;
            }
        };
        hud.attachChild(s);
        hud.registerTouchArea(s);
    }

    /**
     * Add a button to make the hero jump
     * 
     * @param x
     *            X coordinate of top left corner of the button
     * @param y
     *            Y coordinate of top left corner of the button
     * @param width
     *            Width of the button
     * @param height
     *            Height of the button
     * @param name
     *            Name of the image to use for this button
     */
    static void addJumpButton(int x, int y, int width, int height, String name)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        AnimatedSprite s = new AnimatedSprite(x, y, width, height, ttr) {
            float lastTouch;

            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                float now = Framework.self().getEngine().getSecondsElapsedTotal();
                if (now < lastTouch + 0.25)
                    return false;
                Hero.lastHero.jump();
                return true;
            }
        };
        hud.attachChild(s);
        hud.registerTouchArea(s);
    }

    /**
     * Add a button to make the hero shoot
     * 
     * @param x
     *            X coordinate of top left corner of the button
     * @param y
     *            Y coordinate of top left corner of the button
     * @param width
     *            Width of the button
     * @param height
     *            Height of the button
     * @param name
     *            Name of the image to use for this button
     */
    static void addShootButton(int x, int y, int width, int height, String name)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        AnimatedSprite s = new AnimatedSprite(x, y, width, height, ttr) {
            float lastTouch;

            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                float now = Framework.self().getEngine().getSecondsElapsedTotal();
                if (now < lastTouch + 0.25)
                    return false;
                Bullet.shoot(Hero.lastHero.getX(), Hero.lastHero.getY());
                return true;
            }
        };
        hud.attachChild(s);
        hud.registerTouchArea(s);
    }

    /**
     * Add a stopwatch for tracking how long a level takes
     */
    static public void addStopwatch()
    {
        // figure out how much time between right now, and when the program
        // started
        timerDelta = Framework.self().getEngine().getSecondsElapsedTotal();

        // turn on the timer
        timeractive = true;

        // make the text object to display
        final ChangeableText elapsedText = new ChangeableText(20, 10, hudFont, "", "XXXXXXXXXXXXXXXXXXXXXXX".length());

        // set up an autoupdate for the time every .05 seconds
        TimerHandler HUDTimer = new TimerHandler(1 / 20.0f, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler)
            {
                // get elapsed time for this level
                float newtext = Framework.self().getEngine().getSecondsElapsedTotal() - timerDelta;
                // update the text
                if (timeractive)
                    elapsedText.setText("Survived: " + (int) newtext + "s");
                secondsSurvived = (int) newtext;
            }
        });
        Level.current.registerUpdateHandler(HUDTimer);

        // add the text to the hud
        Framework.self().myCamera.getHUD().attachChild(elapsedText);
    }
    
    static public int getSecondsSurvived(){
    	return secondsSurvived;
    }
    

    /**
     * Display a strength meter
     */
    static public void addStrengthMeter()
    {
        // turn on the timer
        timeractive = true;

        // make the text object to display
        final ChangeableText elapsedText = new ChangeableText(220, 280, hudFont, "", "XXXX Strength".length());

        // set up an autoupdate for the time every .05 seconds
        TimerHandler HUDTimer = new TimerHandler(1 / 20.0f, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler)
            {
                // get elapsed time for this level
                String newtext = "" + Hero.lastHero.strength;

                // update the text
                if (timeractive)
                    elapsedText.setText(newtext + " Strength");
            }
        });
        Level.current.registerUpdateHandler(HUDTimer);

        // add the text to the hud
        Framework.self().myCamera.getHUD().attachChild(elapsedText);
    }

    /**
     * Display a zoom in button
     * 
     * @param x
     *            X coordinate of top left corner of the button
     * @param y
     *            Y coordinate of top left corner of the button
     * @param width
     *            Width of the button
     * @param height
     *            Height of the button
     * @param name
     *            Name of the image to use for this button
     * @param maxZoom
     *            Maximum zoom. 4 is usually a good default
     */
    static void addZoomInButton(int x, int y, int width, int height, String name, final float maxZoom)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        AnimatedSprite s = new AnimatedSprite(x, y, width, height, ttr) {
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    float curr_zoom = Framework.self().myCamera.getZoomFactor();
                    if (curr_zoom < maxZoom)
                        Framework.self().myCamera.setZoomFactor(curr_zoom * 2);
                }
                return true;
            }
        };
        hud.attachChild(s);
        hud.registerTouchArea(s);
    }

    /**
     * Display a zoom out button
     * 
     * @param x
     *            X coordinate of top left corner of the button
     * @param y
     *            Y coordinate of top left corner of the button
     * @param width
     *            Width of the button
     * @param height
     *            Height of the button
     * @param name
     *            Name of the image to use for this button
     * @param minZoom
     *            Minimum zoom. 0.25f is usually a good default
     */
    static void addZoomOutButton(int x, int y, int width, int height, String name, final float minZoom)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        AnimatedSprite s = new AnimatedSprite(x, y, width, height, ttr) {
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    float curr_zoom = Framework.self().myCamera.getZoomFactor();
                    if (curr_zoom > minZoom)
                        Framework.self().myCamera.setZoomFactor(curr_zoom / 2);
                }
                return true;
            }
        };
        hud.attachChild(s);
        hud.registerTouchArea(s);
    }
}
