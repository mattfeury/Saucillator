import java.util.LinkedList;
import com.softsynth.jsyn.*;
/**
 * Abstract class Instrument - write a description of the class here
 * 
 * @author (your name here)
 * @version (version number or date here)
 */
public abstract class Instrument
{
    protected boolean isPlaying;
    protected LinkedList<SineOscillator> sineInputs; //the individual sine oscs to make our complex wavveform
    
    protected int BASE_FREQ = 440;
    
    protected int[] scale;
    public static int[] majorScale = {0,2,4,5,7,9,11};
    public static int[] minorScale = {0,2,3,5,7,8,10};
    
    protected int[] harmonics;
    public static int[] oddHarmonics = {1,3,5,7};
    public static int[] evenHarmonics = {1,2,4,6,8};
    public static int[] allHarmonics = {1,2,3,4,5,6,7,8};
    public static int[] noHarmonics = {1};

    protected float amplitude = 1.0f; //amplitude for the fundamental
    
    public Instrument() {
      sineInputs = new LinkedList<SineOscillator>();                
    }
    
    abstract void start();
    abstract void stop();
    abstract void makeTimbre();
    abstract void adjustFrequencyByOffset(int offset);
    
    public static double getScaleIntervalFromOffset(int[] scale, int offset)
    {   
        return (scale[offset % scale.length] + 12*((int)((offset)/scale.length)));
    }

    public boolean isPlaying()
    {
        return isPlaying;
    }
}
