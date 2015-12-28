package edu.lehigh.cse.dan.wyatt.factoryrunner;

import java.io.IOException;
import java.util.Hashtable;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.Debug;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * The MediaFactory provides a mechanism for registering all of our images and
 * sounds
 * 
 * @author spear
 */
public class Media
{
    /**
     * Store the sounds used by this game
     */
    static private Hashtable<String, Sound> sounds = new Hashtable<String, Sound>();

    /**
     * Store the music used by this game
     */
    static private Hashtable<String, Music> tunes = new Hashtable<String, Music>();

    /**
     * Store the images used by this game
     */
    static private Hashtable<String, TiledTextureRegion> images = new Hashtable<String, TiledTextureRegion>();

    /**
     * Retrieve a sound by name
     * 
     * @param name
     *            Name of the sound file to retrieve
     * 
     * @return a Sound object that can be used for sound effects
     */
    static Sound getSound(String name)
    {
        Debug.d("returning " + sounds.get(name));
        return sounds.get(name);
    }

    /**
     * Retrieve a music object by name
     * 
     * @param name
     *            Name of the music file to retrieve
     * 
     * @return a Music object that can be used to play background music
     */
    static Music getMusic(String name)
    {
        Debug.d("returning " + tunes.get(name));
        return tunes.get(name);
    }

    /**
     * Retrieve an image by name
     * 
     * @param name
     *            Name of the image file to retrieve
     * 
     * @return a TiledTextureRegion object that can be used to create
     *         AnimatedSprites
     */
    static TiledTextureRegion getImage(String name)
    {
        Debug.d("returning " + images.get(name));
        return images.get(name);
    }

    /**
     * Register an image file, so that it can be used later.
     * 
     * Images should be .png files. You can use Paint.NET to convert images as
     * needed. Note that images with internal animations do not work correctly.
     * You should use cell-based animation instead.
     * 
     * @param imageFileName
     *            the name of the image file (assumed to be in the "assets"
     *            folder). This should be of the form "image.png", and should be
     *            of type "png".
     */
    static public void registerImage(String imageFileName)
    {
        AssetManager am = Framework.self().getAssets();
        Bitmap b = null;
        try {
            b = BitmapFactory.decodeStream(am.open(imageFileName));
        }
        catch (Exception e) {
            Debug.d("Error accessing file!");
            return;
        }
        int width = b.getWidth();
        int height = b.getHeight();

        int btaWidth = roundUpToPowerOf2(width);
        int btaHeight = roundUpToPowerOf2(height);

        BitmapTextureAtlas bta = new BitmapTextureAtlas(btaWidth, btaHeight, TextureOptions.DEFAULT);
        TiledTextureRegion ttr = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bta, Framework.self(),
                imageFileName, 0, 0, 1, 1);
        images.put(imageFileName, ttr);
        Framework.self().getEngine().getTextureManager().loadTexture(bta);
    }

    /**
     * Register an animatable image file, so that it can be used later. The
     * difference between regular images and animatable images is that
     * animatable images have multiple rows and columns, for cell-based
     * animation.
     * 
     * Images should be .png files. You can use Paint.NET to convert images as
     * needed. Note that images with internal animations do not work correctly.
     * You should use cell-based animation instead.
     * 
     * @param imageFileName
     *            the name of the image file (assumed to be in the "assets"
     *            folder). This should be of the form "image.png", and should be
     *            of type "png".
     * @param cols
     *            If this image is for animation, and represents a grid of
     *            cells, then cols should be the number of columns in the grid.
     *            Otherwise, it should be 1.
     * @param rows
     *            If this image is for animation, and represents a grid of
     *            cells, then rows should be the number of rows in the grid.
     *            Otherwise, it should be 1.
     */
    static public void registerAnimatableImage(String imageFileName, int cols, int rows)
    {
        AssetManager am = Framework.self().getAssets();
        Bitmap b = null;
        try {
            b = BitmapFactory.decodeStream(am.open(imageFileName));
        }
        catch (Exception e) {
            Debug.d("Error accessing file!");
            return;
        }
        int width = b.getWidth();
        int height = b.getHeight();

        int btaWidth = roundUpToPowerOf2(width);
        int btaHeight = roundUpToPowerOf2(height);

        BitmapTextureAtlas bta = new BitmapTextureAtlas(btaWidth, btaHeight, TextureOptions.DEFAULT);
        TiledTextureRegion ttr = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bta, Framework.self(),
                imageFileName, 0, 0, cols, rows);
        images.put(imageFileName, ttr);
        Framework.self().getEngine().getTextureManager().loadTexture(bta);
    }

    /**
     * Register a music file, so that it can be used later.
     * 
     * Music should be in .ogg files. You can use Audacity to convert music as
     * needed.
     * 
     * @param musicFileName
     *            the name of the music file (assumed to be in the "assets"
     *            folder). This should be of the form "song.ogg", and should be
     *            of type "ogg".
     * @param loop
     *            either true or false, to indicate whether the song should
     *            repeat when it reaches the end
     */
    static public void registerMusic(String musicFileName, boolean loop)
    {
        try {
            Music m = MusicFactory.createMusicFromAsset(Framework.self().getEngine().getMusicManager(),
                    Framework.self(), musicFileName);
            m.setLooping(loop);
            tunes.put(musicFileName, m);
            Debug.d("" + tunes.get(musicFileName));
        }
        catch (final IOException e) {
            Debug.d("Error encountered while trying to load audio file " + musicFileName
                    + ".  Common causes include a misspelled file name, an incorrect path, "
                    + "or an invalid file type.");
        }
    }

    /**
     * Register a sound file, so that it can be used later.
     * 
     * Sounds should be .ogg files. You can use Audacity to convert sounds as
     * needed.
     * 
     * @param soundFileName
     *            the name of the sound file (assumed to be in the "assets"
     *            folder). This should be of the form "sound.ogg", and should be
     *            of type "ogg".
     */
    static public void registerSound(String soundFileName)
    {
        try {
            Sound s = SoundFactory.createSoundFromAsset(Framework.self().getEngine().getSoundManager(),
                    Framework.self(), soundFileName);
            sounds.put(soundFileName, s);
            Debug.d("" + sounds.get(soundFileName));
        }
        catch (IOException e) {
            Debug.d("Error encountered while trying to load audio file " + soundFileName
                    + ".  Common causes include a misspelled file name, an incorrect path, "
                    + "or an invalid file type.");
        }
    }

    /**
     * for a given integer X, this will return the smallest y such that x <= y
     * and y is a power of 2
     * 
     * @param x
     *            an integer
     * @return an integer that is the smallest power of 2 >= x
     * @note: taken from
     *        http://stackoverflow.com/questions/364985/algorithm-for-
     *        finding-the-smallest-power-of-two-thats-greater-or-equal-to-a-giv
     */
    static private int roundUpToPowerOf2(int x)
    {
        if (x < 0)
            return 0;
        --x;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x + 1;
    }
}
