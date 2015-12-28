package edu.lehigh.cse.dan.wyatt.factoryrunner;

import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

import android.graphics.Color;
import android.graphics.Typeface;

/**
 * Manage all aspects of drawing a help scene
 * 
 * @author spear
 */
public class HelpScene
{
    /**
     * This stores the scene that we use to display help
     */
    Scene current;

    /**
     * Font for writing on the help screen
     */
    private Font helpFont;
    
    private Font linkFont;

    /**
     * Constructor configures the fonts
     */
    HelpScene()
    {
        BitmapTextureAtlas bta = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
        helpFont = new Font(bta, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 16, true, Color.BLACK);
        Framework.self().getTextureManager().loadTexture(bta);
        Framework.self().getFontManager().loadFont(helpFont);
        
        linkFont = new Font(bta, Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC), 24, true, Color.BLUE);
        Framework.self().getTextureManager().loadTexture(bta);
        Framework.self().getFontManager().loadFont(linkFont);
    }

    /**
     * Reset the help scene so that we can make the next part of the help
     * message
     * 
     * @param red
     *            red component of help screen background
     * @param blue
     *            blue component of help screen background
     * @param green
     *            green component of help screen background
     */
    static void reset(float red, float blue, float green)
    {
        final HelpScene hs = Framework.self().helpScene;
        // make current a valid new scene
        hs.current = new Scene();
        // Draw a rectangle that covers the scene and that advances the help
        // system
        Rectangle r = new Rectangle(0, 0, Framework.self().getCameraWidth(), Framework.self().getCameraWidth()) {
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                // when the rectangle is touched, wait a bit before advancing
                // the screen, so that we don't read the press twice
                hs.current.registerUpdateHandler(new TimerHandler(0.25f, false, new ITimerCallback() {
                    @Override
                    public void onTimePassed(TimerHandler th)
                    {
                        Framework.self().menuManager.nextHelp();
                    }
                }));
                return true;
            }
        };
        r.setColor(red, green, blue);
        hs.current.registerTouchArea(r);
        hs.current.attachChild(r);
    }

    /**
     * Draw a picture on the current help scene
     * 
     * Note: the order in which this is called relative to other entities will
     * determine whether they go under or over this picture.
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the picture
     * @param height
     *            Height of this picture
     * @param name
     *            Name of the picture to display
     * @return the picture on the screen, so that it can be animated if need be
     */
    static AnimatedSprite addDecoration(int x, int y, int width, int height, String name)
    {
        TiledTextureRegion ttr = Media.getImage(name);
        AnimatedSprite s = new AnimatedSprite(x, y, width, height, ttr);
        Framework.self().helpScene.current.attachChild(s);
        return s;
    }

    /**
     * Print a message on the current help scene
     * 
     * @param x
     *            X coordinate of text
     * @param y
     *            Y coordinate of text
     * @param Message
     *            The message to display
     */
    static public void addText(int x, int y, String Message, int type)
    {
        // put the message on the scene
    	/*Font font;
    	if (type == 0)
    		font = Framework.self().helpScene.helpFont;
    	else
    		font = Framework.self().helpScene.linkFont;*/
 
    	Text t = new Text(x, y, Framework.self().helpScene.helpFont, Message);
        Framework.self().helpScene.current.attachChild(t);
    }
    
    /**
     * Print a message on the current help scene formatted as a link
     * 
     * @param x
     *            X coordinate of text
     * @param y
     *            Y coordinate of text
     * @param Message
     *            The message to display
     */
    static public void addLink(int x, int y, String Message)
    {
        // put the message on the scene
        Text t = new Text(x, y, Framework.self().helpScene.linkFont, Message);
        Framework.self().helpScene.current.attachChild(t);
    }
}
