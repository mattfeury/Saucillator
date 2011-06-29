package com.mattfeury.saucillator.macbook;

/**
 * Abstract class for instruments. Contains data for harmonics, oscillators, LFOs, 
 * envelopes and scopes. All other instruments extend this.
 * 
 * @author theChillwavves
 *
 */

import java.util.*;

import java.awt.*;
import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;

import com.softsynth.jsyn.*;
import com.softsynth.jsyn.view102.SynthScope;

public abstract class Instrument
{
    protected boolean isPlaying;
    protected LinkedList<SynthOscillator> sineInputs; //the individual sine oscs to make our complex wavveform
    protected LinkedList<SynthInput> freqMods;
    protected ArrayList<LFO> lfos;
    protected LinkedList<Instrument> extraneous = new LinkedList<Instrument>();
    
    protected SynthEnvelope envData = new SynthEnvelope( new double[]{0.0, 1.0} ); //no envelope by default
    protected EnvelopePlayer envPlayer = new EnvelopePlayer();
    protected boolean customEnvelope = false;
    
    protected SynthScope scope;
    protected SynthMixer mixer;
    
    public static float BASE_FREQ = 440f;

    public static int[] chromaticScale = {0,1,2,3,4,5,6,7,8,9,10,11};
    public static int[] majorScale = {0,2,4,5,7,9,11};
    public static int[] minorScale = {0,2,3,5,7,8,10};
    public static int[] minorBluesScale = {0,3,5,6,7,10,12};
    public static int[] scale = minorScale; //default
    
    protected int[] harmonics;
    public static int[] oddHarmonics = {1,3,5,7,9,11,13,15,17};
    public static int[] evenHarmonics = {1,2,4,6,8,10,12,14,16};
    public static int[] allHarmonics = {1,2,3,4,5,6,7,8,9,10};
	  public static int[] highHarmonics = {1,15,16,17,18,19,20,21,22};
    public static int[] noHarmonics = {1};

    
    public int MOD_DEPTH = 0; //(0-2000) maybe
    public int MOD_RATE = 0; //in hz (0-20)
    public double LAG_LIFE = 0; //lag between freqs (used after LFO creation) (0-10)

    //state variables?
    private boolean LFO_INIT = false; 
    private boolean LFO_ENABLED = false;


    protected float amplitude = 0.8f; //amplitude for the fundamental
    
    public Instrument() {
      sineInputs = new LinkedList<SynthOscillator>();
      freqMods   = new LinkedList<SynthInput>();
    }
    
    abstract void start();
    abstract void stop();
    abstract void makeTimbre();
    abstract void adjustFrequencyByOffset(int offset);
    
    /*
     * Make LFOs for each oscillator in this instrument. They won't do anything until they're enabled.
     *
     * @param enable if true, the LFOs will be automatically connected to the sineOsc.
     */
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

    /*
     * Set a new envelope for this instrument
     */
    public void setEnvelopeData(SynthEnvelope envData)
    {
      customEnvelope = true;
      this.envData = envData;
    }

    /*
     * Connect the lfos to their simple oscillator and replace the freqMods array with the the timelag input.
     */
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

    /*
     * Disable all the lfos for this instrument
     */
    public void disableLFOs()
    {
      LFO_ENABLED = false;

      for(Instrument extra : extraneous)
        extra.disableLFOs();
      
      for(LFO lfo : lfos)
      {
        lfo.disconnect();
      }
    }

    /*
     * force an update for the LFOs
     */
    public void updateLFOs()
    {
      for(Instrument extra : extraneous)
        extra.updateLFOs();

      for(LFO lfo : lfos)
        lfo.update();
    }

    /*
     * Disconnect all the lfos
     */
    public void stopLFOs()
    {
      for(LFO lfo : lfos)
        lfo.disconnect();
    }

    /*
     * are lfos currently enabled?
     */
    public boolean isLfoEnabled()
    {
      return LFO_ENABLED;
    }

    /*
     * Get the mixer for this instrument. This is the mixer that sums all the various oscillators
     */
    public SynthMixer getMixer()
    {
      return mixer;
    }

    /*
     * Reset the envelope to a default. This is basically no envelope. A constant DB level.
     */
    public void resetEnvelope()
    {
      envData = new SynthEnvelope( new double[]{0.0, amplitude} );
    }

    /*
     * Connect the envelope to the amplitude of the oscillators
     */
    public void connectEnvelope()
    {
      if(customEnvelope) return; //if it is custom, assume the creater takes care of all this

      envPlayer.output.disconnect();
      for(SynthOscillator osc : sineInputs)
        envPlayer.output.connect( osc.amplitude );

      envPlayer.start();   
    }

    /*
     * get the current mod rate for the LFO
     */
    public int getModRate()
    {
      return MOD_RATE;
    }

    /*
     * get the current mod depth for the LFO
     */
    public int getModDepth()
    {
      return MOD_DEPTH;
    }
    
    /*
     * Set's the current mod rate for the current instrument.
     */
    public void updateModRate(int i)
    {
      MOD_RATE = i;
      for(Instrument extra : extraneous)
        extra.updateModRate(i);

    }

    /*
     * Set's the current mod depth for the current instrument.
     */    
    public void updateModDepth(int i)
    {
      MOD_DEPTH = i;
      for(Instrument extra : extraneous)
        extra.updateModDepth(i);
      
    }
      
    /*
     * start the oscilloscope.
     */
    public void startScope()
    {      
      scope = new SynthScope();
      
      scope.createProbe( mixer.getOutput(0), "", Saucillator.darkGreenTest );
      scope.finish();
      scope.getWaveDisplay().setBackground( Color.black );
      scope.getWaveDisplay().setForeground( Color.green );

      connectEnvelope();
      makeLFOs(true); //this is hacky. it should be moved elsewhere to its own method, but it should be called at the end of the constructor so this is a convenient place for it for now.
 
    }

    /*
     * fairly important method here. Given an integer offset calculate the actual offset
     * from a scale. 
     *
     * ie, given an offset of 1, we will find the offset for the first note in the scale
     */
    public static double getScaleIntervalFromOffset(int[] scale, int offset)
    {   
        return (scale[offset % scale.length] + 12*((int)((offset)/scale.length)));
    }

    /*
     * get the current scope
     */
    public SynthScope getScope()
    {
        return scope;
    }

    /*
     * is this instrument currently playing?
     */
    public boolean isPlaying()
    {
        return isPlaying;
    }

    /*
     * kill this instrument. stop and delete every SynthObject is uses. 
     * this probably doesn't get called anymore. but is here for safety's sake.
     */
    public void kill()
    {
      //stop();
      mixer.stop();
      mixer.delete();

      for(LFO lfo : lfos)
        lfo.delete();
      
      for(SynthOscillator osc : sineInputs)
      {
        osc.stop();
        osc.delete();
      }
    }

    /*
     * inner class that functions as an LFO. this adds a low frequency oscillator with a lag
     * and it will drive the frequency of the oscillators
     */
    class LFO
    {
      private SineOscillator myLFO;
      private AddUnit mySum;
      private ExponentialLag myLag;
      private SynthInput frequency;

      /*
       * create an LFO that drives the SynthInput passed.
       */
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

      /*
       * get the aspect of the LFO that we use to modify the actual frequency.
       */
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

      /*
       * connect the LFO to the frequency SynthInput
       */
      public void connect()
      { 
        update();
        mySum.output.connect(frequency);
      }

      /*
       * update the LFO settings based on the current instrument settings.
       */
      public void update()
      {
        myLag.halfLife.set(LAG_LIFE); //freq lag        
        myLFO.amplitude.set(MOD_DEPTH); //mod depth
        myLFO.frequency.set(MOD_RATE); //mod rate   
      }

      /*
       * delete the LFO.
       */
      public void delete()
      {
        myLag.stop();
        myLFO.stop();
        mySum.stop();
        
        myLag.delete();
        myLFO.delete();
        mySum.delete();
      }


    }

}
