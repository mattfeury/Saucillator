

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
    protected LinkedList<SynthInput> freqMods;
    protected ArrayList<LFO> lfos;
    
    
    protected int BASE_FREQ = 440;
    protected SynthScope scope;
    protected SynthMixer mixer;
    
    protected int[] scale;
    public static int[] majorScale = {0,2,4,5,7,9,11};
    public static int[] minorScale = {0,2,3,5,7,8,10};
    
    protected int[] harmonics;
    public static int[] oddHarmonics = {1,3,5,7,9,11,13,15,17,19,21,23,25,27,29};
    public static int[] evenHarmonics = {1,2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50};
    public static int[] allHarmonics = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
    public static int[] noHarmonics = {1};

    
    public static int MOD_DEPTH = 0; //(0-2000) maybe
    public static int MOD_RATE = 0; //in hz (0-20)
    public double LAG_LIFE = 0; //lag between freqs (used after LFO creation) (0-10)

    //state variables?
    private boolean LFO_INIT = false; //we don't prolly need these
    private boolean LFO_ENABLED = false;


    protected float amplitude = 1.0f; //amplitude for the fundamental
    
    public Instrument() { //constructor needs: scale, freq, amp?
      sineInputs = new LinkedList<SynthOscillator>();
      freqMods   = new LinkedList<SynthInput>();
    }
    
    abstract void start();
    abstract void stop();
    abstract void makeTimbre();
    abstract void adjustFrequencyByOffset(int offset);

    public void makeLFOs(boolean enable)
    {
      //LFO
      lfos = new ArrayList<LFO>();
      for(SynthOscillator osc : sineInputs)
      {
        LFO lfo = new LFO(osc.frequency);
        lfos.add(lfo);
      }
      LFO_INIT = true;      
      if(enable) enableLFOs();
      else  disableLFOs();
    }

    public void enableLFOs()
    {
      LFO_ENABLED = true;
      freqMods.clear();
      for(LFO lfo : lfos)
      {
        lfo.connect();
        freqMods.add(lfo.getFreqMod());
      }
      updateLFOs();
    }

    public void disableLFOs()
    {
      LFO_ENABLED = false;
      for(LFO lfo : lfos)
      {
        lfo.disconnect();
      }
    }

    public void updateLFOs()
    {
      for(LFO lfo : lfos)
        lfo.update();
    }

    public void stopLFOs()
    {
      for(LFO lfo : lfos)
        lfo.disconnect();
    }

    public boolean isLfoEnabled()
    {
      return LFO_ENABLED;
    }

    public SynthMixer getMixer()
    {
      return mixer;
    }
    
    public void startScope()
    {      
      scope = new SynthScope();
      
      scope.createProbe( mixer.getOutput(0), "", KaossTest.darkGreenTest );
      scope.finish();
      scope.getWaveDisplay().setBackground( Color.black );
      scope.getWaveDisplay().setForeground( Color.green );

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

    class LFO
    {
      private SineOscillator myLFO;
      private AddUnit mySum;
      private ExponentialLag myLag;
      private SynthInput frequency;

      public LFO(SynthInput frequency)
      {
        this.frequency = frequency;
        myLFO = new SineOscillator();
        mySum = new AddUnit();
        myLag = new ExponentialLag();

        /* LFO and Lag are added together to calculate new frequency. */
        myLag.output.connect( mySum.inputB );
        myLFO.output.connect( mySum.inputA );

        myLag.input.setSignalType( Synth.SIGNAL_TYPE_OSC_FREQ );
        myLag.current.setSignalType( Synth.SIGNAL_TYPE_OSC_FREQ );
        myLFO.amplitude.setSignalType( Synth.SIGNAL_TYPE_OSC_FREQ );
        update();

        mySum.start();
    		myLag.start();
		    myLFO.start();
      }

      public SynthInput getFreqMod()
      {
        return myLag.input;
      }

      /*
       * It's not actually ever disconnected. Just sounds "invisible" by resetting to 0
       */
      public void disconnect()
      {
        myLag.halfLife.set(0);
        myLFO.amplitude.set(0); //mod depth
        myLFO.frequency.set(0); //mod rate      
      }

      public void connect()
      { 
        update();
        mySum.output.connect(frequency);
      }

      public void update()
      {
        myLag.halfLife.set(LAG_LIFE); //freq lag        
        myLFO.amplitude.set(MOD_DEPTH); //mod depth
        myLFO.frequency.set(MOD_RATE); //mod rate   
      }


    }

}
