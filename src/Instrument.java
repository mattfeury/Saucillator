

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
    protected LinkedList<Instrument> extraneous = new LinkedList<Instrument>();
    
    protected SynthEnvelope envData = new SynthEnvelope( new double[]{0.0, 1.0} ); //no envelope by default
    protected EnvelopePlayer envPlayer = new EnvelopePlayer();
    protected boolean customEnvelope = false;
    
    protected int BASE_FREQ = 440;
    protected SynthScope scope;
    protected SynthMixer mixer;
    
    protected int[] scale;
    public static int[] chromaticScale = {0,1,2,3,4,5,6,7,8,9,10,11};
    public static int[] majorScale = {0,2,4,5,7,9,11};
    public static int[] minorScale = {0,2,3,5,7,8,10};
    public static int[] minorBluesScale = {0,3,5,6,7,10,12};
    
    protected int[] harmonics;
    public static int[] oddHarmonics = {1,3,5,7,9,11,13,15,17};
    public static int[] evenHarmonics = {1,2,4,6,8,10,12,14,16};
    public static int[] allHarmonics = {1,2,3,4,5,6,7,8,9,10};
	  public static int[] highHarmonics = {1,15,16,17,18,19,20,21,22};
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

	public int[] addHarmonics(int[] firstHarmonics, int[] secondHarmonics) {
		int[] newHarmonics = new int[firstHarmonics.length + secondHarmonics.length];
		System.arraycopy(secondHarmonics, 0, newHarmonics, 0, secondHarmonics.length);
		System.arraycopy(firstHarmonics, 0, newHarmonics, secondHarmonics.length, firstHarmonics.length);
		return newHarmonics;
	}

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

    public void setEnvelopeData(SynthEnvelope envData)
    {
      customEnvelope = true;
      this.envData = envData;
//      envPlayer.envelopePort.clear(); // clear the queue         
//       envPlayer.envelopePort.queueLoop(envData );  // queue an envelope
      
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

      for(Instrument extra : extraneous)
        extra.disableLFOs();
      
      for(LFO lfo : lfos)
      {
        lfo.disconnect();
      }
    }

    public void updateLFOs()
    {
      for(Instrument extra : extraneous)
        extra.updateLFOs();

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

    public void changeScale(int[] scale)
    {
      this.scale = scale;
    }

    public SynthMixer getMixer()
    {
      return mixer;
    }

    public void resetEnvelope()
    {
      envData = new SynthEnvelope( new double[]{0.0, 1.0} );
    }

    public void connectEnvelope()
    {
      if(customEnvelope) return; //if it is custom, assume the creater takes care of all this

      envPlayer.output.disconnect();
      for(SynthOscillator osc : sineInputs)
        envPlayer.output.connect( osc.amplitude );

      envPlayer.start();   
    }
      
    
    public void startScope()
    {      
      scope = new SynthScope();
      
      scope.createProbe( mixer.getOutput(0), "", KaossTest.darkGreenTest );
      scope.finish();
      scope.getWaveDisplay().setBackground( Color.black );
      scope.getWaveDisplay().setForeground( Color.green );

      connectEnvelope();
      makeLFOs(true); //this is hacky. move it elsewhere. but it should be called at the end of the constructor

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
