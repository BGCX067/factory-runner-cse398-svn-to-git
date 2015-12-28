package edu.lehigh.cse.dan.wyatt.factoryrunner;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.HorizontalAlign;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.MotionEvent;

/**
 * Manage all aspects of displaying levels. This includes creating them,
 * handling win and loss, and also drawing the menus. This also handles locking
 * and unlocking levels.
 * 
 * @author spear
 */
public class MenuManager
{
	
    /**
     * Modes of the game: we can be showing the main screen, the help screens,
     * the level chooser, or a playable level
     */
    enum Modes {
        SPLASH, HELP, CHOOSE, PLAY, HIGHSCORE
    };

    /**
     * A title for this game, to display on the splash screen
     */
    private static String _title = "Factory Runner";

    /**
     * The current level being played
     */
    private static int _currLevel;

    /**
     * The current mode of the program
     */
    private static Modes _mode;

    /**
     * A font for drawing white text
     */
    private Font menuFont;
    
    /**
     * A font for drawing dark text
     */
    private Font darkMenuFont;

    /**
     * Track the current help scene being displayed
     */
    private int _currHelp;

    /**
     * An invisible image. We overlay this on buttons so that we have more
     * control of the size and shape of the touchable region.
     */
    static TiledTextureRegion ttrInvis;

    /**
     * The name of the file that stores how many levels are unlocked
     */
    static private final String LOCKFILE = "LOCKFILE";

    /**
     * ID of the highest level that is unlocked
     */
    static private int unlocklevel = 1;
    
    /**
     * The name of the file that stores the high score
     */
    static private final String SCOREFILE = "SCOREFILE";

    /**
     * ID of the highest score that has been achieved
     */
    static private int highscore = 0;

    /**
     * Reinitialize the camera
     */
    static private void reinitCamera()
    {
        // center the camera
        Framework.self().myCamera.setChaseEntity(null);
        Framework.self().myCamera.setBoundsEnabled(false);
        Framework.self().myCamera.setCenter(Framework.self().getCameraWidth() / 2,
                Framework.self().getCameraHeight() / 2);
        Framework.self().myCamera.setZoomFactorDirect(1);
        Framework.self().myCamera.setHUD(new HUD());
    }

    /**
     * Initialize the manager to start the program
     */
    MenuManager()
    {
        // get number of unlocked levels
        readUnlocked();
        readScore();

        // set the default display mode
        _currLevel = -1;
        _mode = Modes.SPLASH;
        _currHelp = 0;

        // load an invisible png for use in menus, and configure a font
        BitmapTextureAtlas bta = new BitmapTextureAtlas(2, 2, TextureOptions.DEFAULT);
        ttrInvis = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bta, Framework.self(), "invis.png",
                0, 0, 1, 1);
        Framework.self().getEngine().getTextureManager().loadTexture(bta);
        
   
        
        bta = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
        menuFont = new Font(bta, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32, true, Color.WHITE);
        Framework.self().getTextureManager().loadTexture(bta);
        Framework.self().getFontManager().loadFont(menuFont);
        
        bta = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
        darkMenuFont = new Font(bta, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32, true, Color.BLACK);
        Framework.self().getTextureManager().loadTexture(bta);
        Framework.self().getFontManager().loadFont(darkMenuFont);
    }

    /**
     * Advance to the next help scene
     */
    void nextHelp()
    {
        if (_currHelp < Framework.self().getHelpScenes()) {
            _mode = Modes.HELP;
            _currHelp++;
            Framework.self().configureHelpScene(_currHelp);
            Framework.self().getEngine().setScene(Framework.self().helpScene.current);
        }
        else {
            _currHelp = 0;
            _mode = Modes.SPLASH;
            Framework.self().getEngine().setScene(this.drawSplash());
        }
    }
    

    /**
     * Create a scene to display
     * 
     * This is the interface that the Framework employs to know what to draw on
     * the screen.
     * 
     * @return either the splash scene, or the currently playable scene
     */
    Scene display()
    {
        if (_mode == Modes.SPLASH)
            return drawSplash();
        Framework.self().configureLevel(1);
        return Level.current;
    }

    /**
     * Handle back button presses by changing the screen that is being displayed
     * 
     * @return always true, since we always handle the event
     */
    boolean onBack()
    {
        // if we're looking at main menu, then exit
        if (_mode == Modes.SPLASH) {
            Framework.self().finish();
            return true;
        }
        // if we're looking at the chooser or help, switch to the splash screen or in high score mode
        if (_mode == Modes.CHOOSE || _mode == Modes.HELP || _mode == Modes.HIGHSCORE) {
            _mode = Modes.SPLASH;
            _currHelp = 0;
            Framework.self().getEngine().setScene(drawSplash());
            return true;
        }
        // ok, we're looking at a game scene... switch to chooser
        _mode = Modes.CHOOSE;
        Framework.self().getEngine().setScene(drawChooser());
        return true;
    }

    /**
     * Create the level chooser
     * 
     * Note that portrait mode won't look good, and that the control buttons are
     * invisible and a bit odd
     * 
     * @return the level chooser scene, so that it can be drawn
     */
    Scene drawChooser()
    {
        if (Level.music != null && Level.music.isPlaying())
            Level.music.pause();
        Scene s = new Scene();
        reinitCamera();

        // figure out if we are portrait or landscape mode, so we can place
        // things accordingly
        boolean landscape = Framework.self().getCameraOrientation() == ScreenOrientation.LANDSCAPE;
        int cw = Framework.self().getCameraWidth();
        
        //Set background image of this scene
        TiledTextureRegion ttr = Media.getImage("metal_bg.png");
        AnimatedSprite a = new AnimatedSprite(0,0,Framework.self().getCameraWidth(), Framework.self().getCameraHeight(), ttr);
        s.attachChild(a);
        
        //Write text on top of screen
        Text title = new Text(20, 20, darkMenuFont, "Select a level:", HorizontalAlign.CENTER);
        s.attachChild(title);

        // draw some reasonably large buttons for the levels
        int bWidth = 50;
        int bHeight = 50;
        int cols = landscape ? 5 : 4;
        int hGutter = (cw - (cols * bWidth)) / (cols + 1);
        int vGutter = 80;
        int cur_x = hGutter;
        int cur_y = -bHeight;
        
        for (int i = 0; i < Framework.self().getNumLevels(); ++i) {
            if (i % cols == 0) {
                cur_y = cur_y + bHeight + vGutter;
                cur_x = hGutter;
            }
            // Draw a bounding rectangle
            //Rectangle r = new Rectangle(cur_x, cur_y, bWidth, bHeight);
            TiledTextureRegion plainbox = Media.getImage("plainbox.png");
            AnimatedSprite r = new AnimatedSprite(cur_x,cur_y,bWidth, bWidth, plainbox);
            //r.attachChild(a);
            //r.setColor(0, 0, 1);
            s.attachChild(r);
            // for unlocked levels, draw an inner, touchable rectangle
            final int level = (i + 1);
            if (level <= unlocklevel) {
                //Rectangle r2 = new Rectangle(cur_x + 2, cur_y + 2, bWidth - 4, bHeight - 4) {
                AnimatedSprite r2 = new AnimatedSprite(cur_x + 2,cur_y + 2,bWidth - 4, bWidth - 4, plainbox){
                    // When this sprite is pressed, we change the level to 1 and
                    // switch
                    // to the level picker mode, then we display the appropriate
                    // scene
                    @Override
                    public boolean onAreaTouched(TouchEvent e, float x, float y)
                    {
                        // change modes
                        _mode = Modes.PLAY;
                        // now draw the chooser screen
                        //
                        // NB: we can't just switch right away, because we need
                        // to
                        // give the user a little time to stop pressing the
                        // screen!
                        Framework.self().getEngine().getScene()
                                .registerUpdateHandler(new TimerHandler(0.25f, false, new ITimerCallback() {
                                    @Override
                                    public void onTimePassed(TimerHandler th)
                                    {
                                        Framework.self().getEngine().clearUpdateHandlers();
                                        _currLevel = level;
                                        Framework.self().configureLevel(level);
                                        Framework.self().getEngine().setScene(Level.current);
                                        if (Level.music != null)
                                            Level.music.play();
                                    }
                                }));
                        // NB: we return true because we are acting on account
                        // of the
                        // touch, so we don't want to propoagate the touch to an
                        // underlying entity
                        return true;
                    }
                };
                //r2.setColor(0, 0, 0);
                s.attachChild(r2);
                s.registerTouchArea(r2);
            }

            // draw the level number
            Text t = new Text(cur_x, cur_y, menuFont, "" + level, HorizontalAlign.CENTER);
            int h = (int) t.getHeight();
            int w = (int) t.getWidth();
            t.setPosition(cur_x + bWidth / 2 - w / 2, cur_y + bHeight / 2 - h / 2);
            s.attachChild(t);

            // for locked levels, cover the number with a semi-transparent
            // rectangle to gray it out
            if (level > unlocklevel) {
                Rectangle r2 = new Rectangle(cur_x + 2, cur_y + 2, bWidth - 4, bHeight - 4);
                r2.setColor(0, 0, 0);
                r2.setAlpha(0.5f);
                s.attachChild(r2);
                s.registerTouchArea(r2);
            }

            cur_x = cur_x + bWidth + hGutter;
        }

        /*These buttons have been commented out
         * Currently there is no need but would be if more levels where added
         * */
        // put some control buttons on the scene to scroll the menu up and down
        HUD h = new HUD();
        Framework.self().myCamera.setHUD(h);

        AnimatedSprite upBtn = new AnimatedSprite(0, 0, Framework.self().getCameraWidth(), 20, ttrInvis) {
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    SmoothCamera c = Framework.self().myCamera;
                    c.setCenter(c.getCenterX(), c.getCenterY() - 10);
                    return true;
                }
                return false;
            }
        };
        //h.attachChild(upBtn);
        //h.registerTouchArea(upBtn);
        

        AnimatedSprite downBtn = new AnimatedSprite(0, Framework.self().getCameraHeight() - 20, Framework.self()
                .getCameraWidth(), 20, ttrInvis) {
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    SmoothCamera c = Framework.self().myCamera;
                    c.setCenter(c.getCenterX(), c.getCenterY() + 10);
                    return true;
                }
                return false;
            };
        };
        //h.attachChild(downBtn);
        //h.registerTouchArea(downBtn);

        return s;
    }

    /**
     * Draw a splash screen
     * 
     * @return A scene that can be drawn by andEngine
     */
    Scene drawSplash()
    {
        // create a blank scene
        Scene s = new Scene();
        reinitCamera();
        
        TiledTextureRegion ttr = Media.getImage("metal_bg.png");
        AnimatedSprite a = new AnimatedSprite(0,0,Framework.self().getCameraWidth(), Framework.self().getCameraHeight(), ttr);
        s.attachChild(a);
        
        ttr = Media.getImage("splash.png");
        a = new AnimatedSprite(0,0,Framework.self().getCameraWidth(), Framework.self().getCameraHeight(), ttr);
        s.attachChild(a);
        
        // Print the name of the game at the top of the scene. You might want a
        // full-screen graphic instead
        /*Text title = new Text(0, 20, darkMenuFont, _title, HorizontalAlign.CENTER);
        title.setPosition(Framework.self().getCameraWidth() / 2 - title.getWidth() / 2, title.getY());
        s.attachChild(title);*/

        // This is a bit of a trick. We're going to print three buttons: "Play",
        // "Help", and "Quit". We could do it by having text that is pressable,
        // or by having custom graphics for each button. The latter looks
        // better, but isn't general for our framework. The former works badly
        // because pressable text doesn't work nicely. Instead, we'll draw text,
        // and then cover it with an invisible image that is pressable. That
        // way, the pressable image will have the desired effect, without
        // needing custom graphics.
        //
        // NB: When debugging, it helps to *not* have invisible images ;)
        //
        // NB: we don't know how wide text is until after we make it, so
        // centering is hard

        // cache a few things to make the subsequent code cleaner
        final Engine _engine = Framework.self().getEngine();
        float screenWidth = Framework.self().getCameraWidth();

        // draw the "PLAY" button
        /*Text t = new Text(0, 100, darkMenuFont, "Play", HorizontalAlign.CENTER);
        float w = t.getWidth();
        float x = screenWidth / 2 - w / 2;
        t.setPosition(x, 100);*/
        AnimatedSprite as = new AnimatedSprite(0, 260, 230, 50, ttrInvis) {
            // When this sprite is pressed, we change the level to 1 and switch
            // to the level picker mode, then we display the appropriate scene
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                // change modes
                _mode = Modes.CHOOSE;
                _currLevel = 1;
                // now draw the chooser screen
                //
                // NB: we can't just switch right away, because we need to
                // give the user a little time to stop pressing the screen!
                _engine.getScene().registerUpdateHandler(new TimerHandler(0.25f, false, new ITimerCallback() {
                    @Override
                    public void onTimePassed(TimerHandler th)
                    {
                        _engine.clearUpdateHandlers();
                        _engine.setScene(drawChooser());
                    }
                }));
                // NB: we return true because we are acting on account of the
                // touch, so we don't want to propoagate the touch to an
                // underlying entity
                return true;
            }
        };
        // put the button and text on the screen, and make the button pressable
        s.attachChild(as);
        //s.attachChild(t);
        s.registerTouchArea(as);
        
        //Draw the "High Score Mode" button
        Text t = new Text(0, 10, darkMenuFont, "High Score is: " + highscore, HorizontalAlign.CENTER);
        float w = t.getWidth();
        float x = screenWidth / 2 - w / 2;
        t.setPosition(x, 150);
        as = new AnimatedSprite(0, 190, 480, 60, ttrInvis) {
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                // change modes
                _mode = Modes.HIGHSCORE;
                _currLevel = 0;
                _engine.getScene().registerUpdateHandler(new TimerHandler(0.25f, false, new ITimerCallback() {
                    @Override
                    public void onTimePassed(TimerHandler th)
                    {
                        _engine.clearUpdateHandlers();
                        Framework.self().configureInfiniteLevel(5);
                    }
                }));
                return true;
            }
        };
        s.attachChild(as);
        s.registerTouchArea(as);
        s.attachChild(t);
        

        // only draw a help button if the game has help scenes
        if (Framework.self().getHelpScenes() > 0) {
            //t = new Text(0, 200, darkMenuFont, "Help", HorizontalAlign.CENTER);
            //w = t.getWidth();
            //x = screenWidth / 2 - w / 2;
            //t.setPosition(x, 200);
            as = new AnimatedSprite(240, 260, 240, 50, ttrInvis) {
                @Override
                public boolean onAreaTouched(TouchEvent e, float x, float y)
                {
                    // change modes
                    _mode = Modes.HELP;
                    _currLevel = 0;
                    _engine.getScene().registerUpdateHandler(new TimerHandler(0.25f, false, new ITimerCallback() {
                        @Override
                        public void onTimePassed(TimerHandler th)
                        {
                            _engine.clearUpdateHandlers();
                            nextHelp();
                        }
                    }));
                    return true;
                }
            };
            s.attachChild(as);
            s.registerTouchArea(as);
            //s.attachChild(t);
        }

        // draw the quit button
        //t = new Text(0, 250, darkMenuFont, "Quit", HorizontalAlign.CENTER);
        //w = t.getWidth();
        //x = screenWidth / 2 - w / 2;
        //t.setPosition(x, 250);
        as = new AnimatedSprite(380, 0, 75, 20, ttrInvis) {
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                // 'finish' to end this Activity
                Framework.self().finish();
                return true;
            }
        };
        s.attachChild(as);
        s.registerTouchArea(as);
        //s.attachChild(t);


        return s;
    }

    /**
     * When a level ends in failure, this is how we shut it down, print a
     * message, and then let the user resume it
     * 
     * @param deathText
     *            Text to print when the level is lost
     */
    void loseLevel(String deathText)
    {
        if (Level.loseSound != null)
            Level.loseSound.play();

        Hero.hideAll();
        // dim out the screen by putting a slightly transparent black rectangle
        // on the HUD
        Rectangle r = new Rectangle(0, 0, Framework.self().getCameraWidth(), Framework.self().getCameraHeight());
        r.setColor(0, 0, 0);
        r.setAlpha(0.9f);
        Controls.hud.attachChild(r);
        Controls.timeractive = false;
        Controls.resetHUD();

        /*Now immediately after death the level starts again
         * There is no punishment for death other than restarting the level so this will not cause frustration if the user is not ready and dies
         * However, it does increase addiction, as the user does not have a chance to stop playing after death
         * 
         */
        if (!(_mode == Modes.HIGHSCORE)){
        	Framework.self().getEngine().getScene()
            .registerUpdateHandler(new TimerHandler(0.25f, false, new ITimerCallback() {
                @Override
                public void onTimePassed(TimerHandler th)
                {
                    Framework.self().getEngine().clearUpdateHandlers();
                    Framework.self().myCamera.setHUD(new HUD());
                    Framework.self().configureLevel(_currLevel);
                    Framework.self().getEngine().setScene(Level.current);
                    if (Level.music != null)
                        Level.music.play();
                }
            }));
        }
        /*If it is high score mode, then it displays the high score and
         * forces the user to press the button to restart the level
         */
        else{
        	Text t;
        	if (Controls.getSecondsSurvived() > highscore){
        		highscore = Controls.getSecondsSurvived();
        		saveScore();
        		t = new Text(35, 80, menuFont, "NEW HIGH SCORE!!!!!!!!!!!");
        		Controls.hud.attachChild(t);
        	}
        	t = new Text(30, 120, menuFont, "You survived: " + Controls.getSecondsSurvived() + " seconds") {
                // When this sprite is pressed, we change the level to 1 and switch
                // to the level picker mode, then we display the appropriate scene
                @Override
                public boolean onAreaTouched(TouchEvent e, float x, float y)
                {
                    // now draw the chooser screen
                    //
                    // NB: we can't just switch right away, because we need to
                    // give the user a little time to stop pressing the screen!
                    Framework.self().getEngine().getScene()
                            .registerUpdateHandler(new TimerHandler(0.25f, false, new ITimerCallback() {
                                @Override
                                public void onTimePassed(TimerHandler th)
                                {
                                    Framework.self().getEngine().clearUpdateHandlers();
                                    Framework.self().myCamera.setHUD(new HUD());
                                    Framework.self().configureInfiniteLevel(5);
                                    //Framework.self().getEngine().setScene(Level.current);
                                    if (Level.music != null)
                                        Level.music.play();
                                }
                            }));
                    // NB: we return true because we are acting on account of the
                    // touch, so we don't want to propoagate the touch to an
                    // underlying entity
                    return true;
                }
            };
            Controls.hud.attachChild(t);
            Controls.hud.registerTouchArea(t);
        }
        /*
        Text t = new Text(100, 100, menuFont, deathText) {
            // When this sprite is pressed, we change the level to 1 and switch
            // to the level picker mode, then we display the appropriate scene
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                // now draw the chooser screen
                //
                // NB: we can't just switch right away, because we need to
                // give the user a little time to stop pressing the screen!
                Framework.self().getEngine().getScene()
                        .registerUpdateHandler(new TimerHandler(0.25f, false, new ITimerCallback() {
                            @Override
                            public void onTimePassed(TimerHandler th)
                            {
                                Framework.self().getEngine().clearUpdateHandlers();
                                Framework.self().myCamera.setHUD(new HUD());
                                Framework.self().configureLevel(_currLevel);
                                Framework.self().getEngine().setScene(Level.current);
                                if (Level.music != null)
                                    Level.music.play();
                            }
                        }));
                // NB: we return true because we are acting on account of the
                // touch, so we don't want to propoagate the touch to an
                // underlying entity
                return true;
            }
        };
        Controls.hud.attachChild(t);
        Controls.hud.registerTouchArea(t);
        */
    }

    /**
     * When a level is won, this is how we end the scene and allow a transition
     * to the next level
     */
    void winLevel()
    {
    	
        if (Level.winSound != null)
            Level.winSound.play();

        if (unlocklevel == _currLevel) {
            unlocklevel++;
            saveUnlocked();
        }

        Hero.hideAll();
        // dim out the screen by putting a slightly transparent black rectangle
        // on the HUD
        Rectangle r = new Rectangle(0, 0, Framework.self().getCameraWidth(), Framework.self().getCameraHeight());
        r.setColor(0, 0, 0);
        r.setAlpha(0.9f);
        Controls.resetHUD();
        Controls.hud.attachChild(r);

        Controls.timeractive = false;
        
        /*Adding in new game flow. Having consecutive text screens is unnecessary
         * Now the next level starts upon completing the previous.
         */
        if (!(_currLevel == Framework.self().getNumLevels())) {
        	_currLevel++;
            if (Level.music != null && Level.music.isPlaying())
                Level.music.pause();
            Framework.self().getEngine().clearUpdateHandlers();
            Framework.self().myCamera.setHUD(new HUD());
            Framework.self().configureLevel(_currLevel);
            Framework.self().getEngine().setScene(Level.current);
            if (Level.music != null)
                Level.music.play();
        }
        else{
        Text t = new Text(100, 100, menuFont, (_currLevel == Framework.self().getNumLevels()) ? "You Won!"
                : "Click to start next level") {
            // When this sprite is pressed, we change the level to 1 and switch
            // to the level picker mode, then we display the appropriate scene
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                // now draw the chooser screen
                //
                // NB: we can't just switch right away, because we need to
                // give the user a little time to stop pressing the screen!
                Framework.self().getEngine().getScene()
                        .registerUpdateHandler(new TimerHandler(0.25f, false, new ITimerCallback() {
                            @Override
                            public void onTimePassed(TimerHandler th)
                            {
                                if (_currLevel == Framework.self().getNumLevels()) {
                                    if (Level.music != null && Level.music.isPlaying())
                                        Level.music.pause();
                                    Framework.self().getEngine().clearUpdateHandlers();
                                    Framework.self().getEngine().setScene(drawChooser());
                                }
                                else {
                                    _currLevel++;
                                    if (Level.music != null && Level.music.isPlaying())
                                        Level.music.pause();
                                    Framework.self().getEngine().clearUpdateHandlers();
                                    Framework.self().myCamera.setHUD(new HUD());
                                    Framework.self().configureLevel(_currLevel);
                                    Framework.self().getEngine().setScene(Level.current);
                                    if (Level.music != null)
                                        Level.music.play();
                                }
                            }
                        }));
                // NB: we return true because we are acting on account of the
                // touch, so we don't want to propoagate the touch to an
                // underlying entity
                return true;
            }
        };
        Controls.hud.attachChild(t);
        Controls.hud.registerTouchArea(t);
        }
    }

    /**
     * save the value of 'unlocked' so that the next time we play, we don't have
     * to start at level 0
     */
    private void saveUnlocked()
    {
        // to write a file, we create a fileoutputstream using the file name.
        // Then we make a dataoutputstream from the fileoutputstream.
        // Then we write to the dataoutputstream
        try {
            FileOutputStream fos = Framework.self().openFileOutput(LOCKFILE, Context.MODE_PRIVATE);
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeInt(unlocklevel);
            dos.close();
            fos.close();
        }
        catch (IOException e) {
        }
    }

    /**
     * read the current value of 'unlocked' to know how many levels to unlock
     */
    private void readUnlocked()
    {
    	//unlocklevel = 5;
        //return;
    	
       
    	// try to open the file. If we can't, then just set unlocked to 1 and
        // return. Otherwise, read the int and update unlocklevel
        try {
            // set the initial value of unlocked
            unlocklevel = 1;

            // open the file and read the int
            FileInputStream fos = Framework.self().openFileInput(LOCKFILE);
            DataInputStream dos = new DataInputStream(fos);
            unlocklevel = dos.readInt();
            fos.close();
        }
        catch (IOException e) {
            unlocklevel = 1;
            return;
        }
    }
    
    /**
     * save the high score whenever it is set
     */
    private void saveScore()
    {
        // to write a file, we create a fileoutputstream using the file name.
        // Then we make a dataoutputstream from the fileoutputstream.
        // Then we write to the dataoutputstream
        try {
            FileOutputStream fos = Framework.self().openFileOutput(SCOREFILE, Context.MODE_PRIVATE);
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeInt(highscore);
            dos.close();
            fos.close();
        }
        catch (IOException e) {
        }
    }
    
    /**
     * read the current high score
     */
    private void readScore()
    {

    	// try to open the file. If we can't, then just set highscore to 0 and
        // return. Otherwise, read the int and update highscore
        try {
            // set the initial value of unlocked
            highscore = 0;

            // open the file and read the int
            FileInputStream fos = Framework.self().openFileInput(SCOREFILE);
            DataInputStream dos = new DataInputStream(fos);
            highscore = dos.readInt();
            fos.close();
        }
        catch (IOException e) {
            highscore = 0;
            return;
        }
    }
}
