

import java.util.*;

import java.awt.*;
import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;

import com.softsynth.jsyn.*;
import com.softsynth.jsyn.view102.SynthScope;



/**
 * Abstract class Instrument - write a description of the class here
 * 
 * @author (your name here)
 * @version (version number or date here)
 */
public abstract class Instrument
{
    protected boolean isPlaying;
    protected LinkedList<SynthOscillator> sineInputs; //the individual sine oscs to make our complex wavveform
    
    protected int BASE_FREQ = 440;
    protected SynthScope scope;
    protected SynthMixer mixer;
    protected LineOut lineOut;

    
    protected int[] scale;
    public static int[] majorScale = {0,2,4,5,7,9,11};
    public static int[] minorScale = {0,2,3,5,7,8,10};
    
    protected int[] harmonics;
    public static int[] oddHarmonics = {1,3,5,7,9,11,13,15,17,19,21,23,25,27,29};
    public static int[] evenHarmonics = {1,2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50};
    public static int[] allHarmonics = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
    public static int[] noHarmonics = {1};

    protected float amplitude = 1.0f; //amplitude for the fundamental
    
    public Instrument() { //constructor needs: scale, freq, amp?
      sineInputs = new LinkedList<SynthOscillator>();                
    }
    
    abstract void start();
    abstract void stop();
    abstract void makeTimbre();
    abstract void adjustFrequencyByOffset(int offset);
    
    public void connectMixerToLineOut()
    {
        lineOut = new LineOut();                  
        mixer.connectOutput( 0, lineOut.input, 0 );
        mixer.connectOutput( 1, lineOut.input, 1 );
        lineOut.start();
        mixer.start();

        startScope();          
    }

    public void startScope()
    {      
      scope = new SynthScope();
      
      scope.createProbe( mixer.getOutput(0), "Square", Color.blue );
      scope.finish();
      scope.getWaveDisplay().setBackground( Color.white );
      scope.getWaveDisplay().setForeground( Color.black );

    }

    public static double getScaleIntervalFromOffset(int[] scale, int offset)
    {   
        return (scale[offset % scale.length] + 12*((int)((offset)/scale.length)));
    }

    public SynthScope getScope()
    {
        return scope;
    }

    public boolean isPlaying()
    {
        return isPlaying;
    }
}
