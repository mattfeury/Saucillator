/*
 * This is our instrument controller. It is main goodness for the sauce.
 *
 * Basically, we create all of our instruments on startup. This is a bit of a drain on CPU
 * but since we reuse simple instruments for our complex ones, it's not too bad. It turns out
 * it's much more exhaustive on the AudioEngine to be switching instruments at any given pace.
 *
 * Basically, we get the mixer from the current instrument and hook it up to the lineout. The
 * signal flow is as follows:
 *
 * 1. Instrument Mixer
 * 2. Low Pass Filter
 * 3. Delay (dry and wet)
 * 4. Reverb (dry and wet)
 * 5. Pan
 * 6. LineOut (this sends to the speakers)
 * 
 * @author theChillwavves
 */

import java.util.ArrayList;
  
import com.softsynth.jsyn.*;
import com.softsynth.jsyn.view102.SynthScope;
import java.io.*;
import com.softsynth.jsyn.circuits.Reverb1;


public class InstrumentController {

    private Instrument SINE = new Sine();
    private Instrument TRIANGLE = new Triangle();
    private Instrument SQUARE = new Square();
	  private Instrument REDNOISE = new RedNoise();
  	private Instrument SAWTOOTH = new Sawtooth(TRIANGLE, SQUARE);
    private Instrument SINGINGSAW = new SingingSaw();    
    private Instrument CUOMO = new Cuomo(SQUARE, SINE); 
	  private Instrument GONG = new Gong(TRIANGLE);   
  	private Instrument MESSIER = new Messier(TRIANGLE, REDNOISE);      
    private Instrument SQUOISE = new Squoise(SQUARE, REDNOISE);      

    private	SampleFileStreamer streamer;
    
    private Instrument CURRENT_INSTRUMENT = SINE;
    private boolean init = false;

    //this is for our signal flow to the lineout
    private TunableFilter filter;
    private Reverb1 reverbUnit;
    private BusWriter busWriter;
    private SynthFilter effectsUnit;
    private PanUnit panUnit;
    private LineOut lineOut;
    private AddUnit effectsAdder, inputAdder, outputAdder;

    //turn fx on or off
    public boolean fxEnabled = false; //this is delay
    public boolean verbEnabled = false; //reverb

    /*
     * Create the controller with the default instrument (sine).
     */
    public InstrumentController()
    {
        changeInstrument(KaossTest.INSTRUMENT_SINE);
    }

    /*
     * Create the pieces of our signal flow. connect them to the instrument mixer too.
     */
    public void start()
    { 
      panUnit = new PanUnit();
      busWriter = new BusWriter();
      reverbUnit = new Reverb1();
      filter = new Filter_LowPass();
      effectsUnit = new DelayUnit( 0.5); 
      effectsAdder = new AddUnit();
      inputAdder = new AddUnit();
      outputAdder = new AddUnit();
      lineOut = new LineOut();        

      makeSauce();
      connectMixer();

      inputAdder.start();
      filter.start();
      effectsUnit.start();
      reverbUnit.start();
      busWriter.start();
      panUnit.start();
      effectsAdder.start();
      outputAdder.start();
      lineOut.start();

      init = true;
    }

    /*
     * Turns on/off delay by [dis]connecting the wet mix from the signal flow
     */
    public void toggleDelay()
    {
      fxEnabled = ! fxEnabled;
      effectsUnit.output.disconnect();

      if(fxEnabled) //if we want fx send them to the adder
        effectsUnit.output.connect(effectsAdder.inputB);
    }

    /*
     * Tuirns on/off reverb by [dis]connecting the wet mix from the signal flow
     */
    public void toggleReverb()
    {
      verbEnabled = ! verbEnabled;

      reverbUnit.output.disconnect();

      if(verbEnabled)
        reverbUnit.output.connect(0, outputAdder.inputB, 0); 
    }

    /*
     * Get the current instrument's mixer and connect it to the lineout.
     */
    public void connectMixer()
    {
      SynthMixer instrumentMix = CURRENT_INSTRUMENT.getMixer();
      
      instrumentMix.connectOutput( 0, inputAdder.inputA, 0 ); //connect instrument to filter (low pass)

      inputAdder.output.connect( filter.input );

      filter.output.connect(effectsUnit.input); //send filter mix like an aux send using fx adder
      filter.output.connect(effectsAdder.inputA); //and also the effects unit

      if(fxEnabled) //if we want fx send them to the adder
        effectsUnit.output.connect(effectsAdder.inputB);
    
      effectsAdder.output.connect( busWriter.input ); //connect to buswriter  
      reverbUnit.busInput.connect(0, busWriter.busOutput, 0);

      effectsAdder.output.connect( outputAdder.inputA ); //connect to summer  
      if(verbEnabled) //if we want verb sent it to the adder
        reverbUnit.output.connect(0, outputAdder.inputB, 0);

      outputAdder.output.connect( panUnit.input ); //connect to pan

      panUnit.output.connect( 0, lineOut.input, 0 ); //pan unit goes to line out, ala sound
      panUnit.output.connect( 1, lineOut.input, 1 );

      filter.Q.set(1.0);
      filter.frequency.set(20000);
      instrumentMix.start();
      
    }

    /*
     * Starts the current instrument. 
     */
    public void startInstrument()
    {
      CURRENT_INSTRUMENT.start();
    }      

    /*
     * Stops the current instrument.
     */
    public void stop()
    {
      CURRENT_INSTRUMENT.stop();
    }

    /*
     * Disconnects the previous instrument from the signal flow.
     * Finds the id of the new instrument and reconnects it to the lineout.
     */
    public void changeInstrument(int id)
    {
      //disconnect whatever is right now
      if(init)
        inputAdder.inputA.disconnect();
     
      CURRENT_INSTRUMENT.stop();
      switch(id)
      {
        case KaossTest.INSTRUMENT_SINE:
          CURRENT_INSTRUMENT = SINE;
          CURRENT_INSTRUMENT.resetEnvelope();
          break;
        case KaossTest.INSTRUMENT_TRIANGLE:
          CURRENT_INSTRUMENT = TRIANGLE;
          CURRENT_INSTRUMENT.resetEnvelope();
          break;
        case KaossTest.INSTRUMENT_SQUARE:
          CURRENT_INSTRUMENT = SQUARE;
          CURRENT_INSTRUMENT.resetEnvelope();
          break;
        case KaossTest.INSTRUMENT_REDNOISE:
          CURRENT_INSTRUMENT = REDNOISE;
          CURRENT_INSTRUMENT.resetEnvelope();
          break;
        case KaossTest.INSTRUMENT_SAWTOOTH:
          CURRENT_INSTRUMENT = SAWTOOTH;
          break;
        case KaossTest.INSTRUMENT_SINGINGSAW:
          CURRENT_INSTRUMENT = SINGINGSAW;
          break;
        case KaossTest.INSTRUMENT_CUOMO:
          CURRENT_INSTRUMENT = CUOMO;
          break;
        case KaossTest.INSTRUMENT_GONG:
          CURRENT_INSTRUMENT = GONG;
          break;
        case KaossTest.INSTRUMENT_MESSIER:
          CURRENT_INSTRUMENT = MESSIER;
          break;
        case KaossTest.INSTRUMENT_SQUOISE:
          CURRENT_INSTRUMENT = SQUOISE;
          break;
      }

      SynthMixer instrumentMix = CURRENT_INSTRUMENT.getMixer();
      instrumentMix.start();
      updateLFO();

      if(init)
        instrumentMix.connectOutput( 0, inputAdder.inputA, 0 ); //connect instrument to filter (low pass)
    }

    /*
     * Kill the instrument. This is left over from when we tried to kill the instrument when we switch.
     * It doesn't work that well so we don't use it.
     */
    public void kill()
    {
      CURRENT_INSTRUMENT.kill();
    }

    /*
     * send a signal to the instrument to make its LFOs invisible
     */
    public void disableLFO()
    {
      CURRENT_INSTRUMENT.disableLFOs();
    }

    /*
     * Enable LFOs on the current instrument. These are generally taken care of when they are created.
     */
    public void enableLFO()
    { 
      CURRENT_INSTRUMENT.enableLFOs();
    }

    /*
     * Tells the current instrument to update its LFOs based on its parameters.
     */
    public void updateLFO()
    {
      CURRENT_INSTRUMENT.updateLFOs();
    }

    /*
     * Stop the lfos!
     */
    public void stopLFO()
    {
      CURRENT_INSTRUMENT.stopLFOs();
    }

    /*
     * Get's the current mod rate for the current instrument
     */
    public int getModRate()
    {
      return CURRENT_INSTRUMENT.getModRate();
    }

    /*
     * Get's the current mod depth for the current instrument
     */    
    public int getModDepth()
    {
      return CURRENT_INSTRUMENT.getModDepth();
    }

    /*
     * Set's the current mod rate for the current instrument.
     */
    public void updateModRate(int rate)
    {
      CURRENT_INSTRUMENT.updateModRate(rate);
      //System.out.println("MOd "+rate);
      updateLFO();
    }

    /*
     * Set's the current mod depth for the current instrument.
     */
    public void updateModDepth(int depth)
    {
      CURRENT_INSTRUMENT.updateModDepth(depth);
      //System.out.println("depth "+depth);      
      updateLFO();
    }

    /*
     * Get the current instrument and return it.
     */
    public Instrument getInstrument()
    {
      return CURRENT_INSTRUMENT;
    }

    /*
     * Get the current scope. This is called by the display to update its JPanel
     */
    public SynthScope getScope()
    {
      return CURRENT_INSTRUMENT.getScope();
    }

    /*
     * Is the current instrument playing?
     */
    public boolean isPlaying()
    {
      return CURRENT_INSTRUMENT.isPlaying();
    }

    /*
     * Change the scale for all instruments.
     */
    public void changeScale(int[] scale)
    {
      Instrument.scale = scale;
    }

    /*
     * Adjust the current note based on an offset (0-12) in a scale. 
     */
    public void changeFrequency(int offset)
    {
      CURRENT_INSTRUMENT.adjustFrequencyByOffset(offset);
    }

    /*
     * Set the lowpass frequency. 
     */
    public void lowpass(int freq)
    {
      filter.frequency.set(freq);
    }

    /*
     * Set the pan on the panunit. This should affect all signal since it's the last thing in the signal flow.
     */
    public void pan(double absolutePan)
    {
      panUnit.pan.set(absolutePan);      
    }

    /*
     * The secret sauce. Do not look at the recipe.
     */
    public void makeSauce()
    {
      try {
				// Load sample from a file.
				File sampleFile = new File("src/sauceboss.wav");
				streamer = new SampleFileStreamer(sampleFile);
			} catch (IOException exc) {
				exc.printStackTrace(System.err);
				throw new RuntimeException(exc.getMessage());
			}

			// Connect streamer to output.
			streamer.getOutput().connect(0, inputAdder.inputB, 0);
			
    }

    /*
     * Share the sauce. This doesn't work if you press it a lot frequently. 
     */
    public void iThinkItsTheSauceBoss()
    {
      streamer.startStream();			      
    }
    
}

