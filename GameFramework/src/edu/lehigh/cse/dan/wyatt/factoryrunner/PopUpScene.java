package edu.lehigh.cse.dan.wyatt.factoryrunner;

import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.CameraScene;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.Debug;

import android.graphics.Color;
import android.graphics.Typeface;

/**
 * PopUpScene is a simple mechanism for creating messages that fill the screen,
 * obscuring the game play. These can be used for very powerful pop-up menus,
 * though the current demo is quite simple.
 * 
 * @author spear
 */
public class PopUpScene
{
    /**
     * A font to use when putting messages on the screen
     */
    private static Font myFont = null;

    /**
     * Configure the fonts used by methods of this class
     */
    static void config()
    {
        BitmapTextureAtlas bta = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
        myFont = new Font(bta, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32, true, Color.WHITE);
        Framework.self().getTextureManager().loadTexture(bta);
        Framework.self().getFontManager().loadFont(myFont);
    }

    /**
     * Print a message on a black background, and wait for a screen touch
     * 
     * @param Message
     *            The message to display
     */
    static public void printMessageAndWait(String Message)
    {
        // we create a 'CameraScene' to which we can attach stuff
        final CameraScene child = new CameraScene();
        child.setCamera(Framework.self().myCamera);

        // Draw a rectangle that kills the popup when it is touched
        Rectangle r = new Rectangle(0, 0, Framework.self().getCameraWidth(), Framework.self().getCameraWidth()) {
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                // just clear the menu
                child.reset();
                Level.current.clearChildScene();
                return true;
            }
        };
        child.setBackgroundEnabled(true);
        r.setColor(1, 1, 1, 0.1f);
        child.registerTouchArea(r);
        child.attachChild(r);

        // put the message on the scene
        Text t = new Text(0, 0, myFont, Message);
        float w = t.getWidth();
        float h = t.getHeight();
        t.setPosition(Framework.self().getCameraWidth() / 2 - w / 2, Framework.self().getCameraHeight() / 2 - h / 2);
        child.attachChild(t);
        Level.current.setChildSceneModal(child);
    }

    /**
     * Print a message on a black background, and wait for a screen touch
     * 
     * @param Message
     *            The message to display
     * @param duration
     *            Time to display the message
     * @param upbox
     *            If set then display an upbox image with the message
     * @param downbox
     *            If set then display an downbox image with the message
     * @param trapdoor
     *            If set then display an trapdoor image with the message
     * @param ramp
     *            If set then display an ramp image with the message
     */
    static public void printTimedMessage(String Message, float duration, int upbox, int downbox, int trapdoor, int ramp)
    {
        // we create a 'CameraScene' to which we can attach stuff
        final CameraScene child = new CameraScene();
        child.setCamera(Framework.self().myCamera);

        
        // Draw a rectangle as the background
        Rectangle r = new Rectangle(0, 0, Framework.self().getCameraWidth(), Framework.self().getCameraWidth());
        r.setColor(1, 1, 1, 0.1f);
        child.attachChild(r);
        child.setBackgroundEnabled(true);

        // set a timer to remove the message
        child.registerUpdateHandler(new TimerHandler(duration, false, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler th)
            {
                Debug.d("hello there, skubber");
                // just clear the menu
                child.reset();
                Level.current.clearChildScene();
            }
        }));

        // put the message on the scene
        Text t = new Text(0, 0, myFont, Message);
        float w = t.getWidth();
        float h = t.getHeight();
        //t.setPosition(Framework.self().getCameraWidth() / 2 - w / 2, Framework.self().getCameraHeight() / 2 - h / 2);
        t.setPosition(Framework.self().getCameraWidth() / 2 - w / 2, Framework.self().getCameraHeight() / 2 - h / 2 - 35);
        child.attachChild(t);
        
        if (upbox == 1){
        	TiledTextureRegion ttr = Media.getImage("upbox.png");
        	AnimatedSprite a = new AnimatedSprite(Framework.self().getCameraWidth() / 2 - 120,Framework.self().getCameraHeight() / 2 + h / 2 - 30,40, 40, ttr);
        	child.attachChild(a);
        }
        if (downbox == 1){
        	TiledTextureRegion ttr = Media.getImage("downbox.png");
        	AnimatedSprite a = new AnimatedSprite(Framework.self().getCameraWidth() / 2 - 55,Framework.self().getCameraHeight() / 2 + h / 2 - 30,40, 40, ttr);
        	child.attachChild(a);
        }
        if (trapdoor == 1){
        	TiledTextureRegion ttr = Media.getImage("trapdoor.png");
        	AnimatedSprite a = new AnimatedSprite(Framework.self().getCameraWidth() / 2 + 15,Framework.self().getCameraHeight() / 2 + h / 2 - 15,40, 10, ttr);
        	child.attachChild(a);
        }
        if (ramp == 1){
        	TiledTextureRegion ttr = Media.getImage("ramp.png");
        	AnimatedSprite a = new AnimatedSprite(Framework.self().getCameraWidth() / 2 + 80,Framework.self().getCameraHeight() / 2 + h / 2 - 30,40, 40, ttr);
        	child.attachChild(a);
        }
        
        Level.current.setChildSceneModal(child);
    }
}
