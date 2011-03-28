//
// Controls an instrument based on trackpad movement.
// This should probably be modified to control multiple instruments

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
  	private Instrument SAWTOOTH = new Sawtooth();
    private Instrument SINGINGSAW = new SingingSaw();    
    private Instrument CUOMO = new Cuomo(SQUARE, SINE); 
	  private Instrument GONG = new Gong(TRIANGLE);   
  	private Instrument MESSIER = new Messier(TRIANGLE, REDNOISE);      
    private Instrument SQUOISE = new Squoise(SQUARE, REDNOISE);      

    private	SampleFileStreamer streamer;
    
    private Instrument CURRENT_INSTRUMENT = SINE;
    private boolean init = false;

    private TunableFilter filter;
    private Reverb1 reverbUnit;
    private BusWriter busWriter;
    private SynthFilter effectsUnit;
    private PanUnit panUnit;
    private LineOut lineOut;
    private AddUnit effectsAdder, inputAdder;

    public boolean fxEnabled = false;
    public boolean verbEnabled = false;

    public InstrumentController()
    {
        changeInstrument(KaossTest.INSTRUMENT_SINE);
    }

    public void start()
    { 
      panUnit = new PanUnit();
      busWriter = new BusWriter();
      reverbUnit = new Reverb1();
      filter = new Filter_LowPass();
      effectsUnit = new DelayUnit( 0.5); 
      effectsAdder = new AddUnit();
      inputAdder = new AddUnit();
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
      lineOut.start();

      init = true;

      System.out.println("controller made");

    }

    public void toggleDelay()
    {
      fxEnabled = ! fxEnabled;
      effectsUnit.output.disconnect();

      if(fxEnabled) //if we want fx send them to the adder
        effectsUnit.output.connect(effectsAdder.inputB);
    }

    public void toggleReverb()
    {
      verbEnabled = ! verbEnabled;

      panUnit.input.disconnect();
      if(verbEnabled) {
        effectsAdder.output.connect( busWriter.input ); //connect to buswriter
        
        reverbUnit.busInput.connect(0, busWriter.busOutput, 0);
        reverbUnit.dryGain.connect(0, effectsAdder.output, 0);
        reverbUnit.output.connect(0, panUnit.input, 0);
      } else {
        effectsAdder.output.connect( panUnit.input ); //connect to pan
      }

    }

    public void connectMixer()
    {
      SynthMixer instrumentMix = CURRENT_INSTRUMENT.getMixer();
      
      instrumentMix.connectOutput( 0, inputAdder.inputA, 0 ); //connect instrument to filter (low pass)

      inputAdder.output.connect( filter.input );

      filter.output.connect(effectsUnit.input); //send filter mix like an aux send using fx adder
      filter.output.connect(effectsAdder.inputA); //and also the effects unit


      if(fxEnabled) //if we want fx send them to the adder
        effectsUnit.output.connect(effectsAdder.inputB);
    
      if(verbEnabled) {
        effectsAdder.output.connect( busWriter.input ); //connect to buswriter
        
        reverbUnit.busInput.connect(0, busWriter.busOutput, 0);
        reverbUnit.dryGain.connect(0, effectsAdder.output, 0);
        reverbUnit.output.connect(0, panUnit.input, 0);
      } else {
        effectsAdder.output.connect( panUnit.input ); //connect to pan
      }


      panUnit.output.connect( 0, lineOut.input, 0 ); //pan unit goes to line out, ala sound
      panUnit.output.connect( 1, lineOut.input, 1 );

      filter.Q.set(1.0);
      filter.frequency.set(20000);
      instrumentMix.start();
      
    }

    public void startInstrument()
    {
      CURRENT_INSTRUMENT.start();
    }      

    public void stop()
    {
      CURRENT_INSTRUMENT.stop();
    }

    public void changeInstrument(int id)
    {
      //disconnect whatever is right now
      if(init)
        filter.input.disconnect();
     
      CURRENT_INSTRUMENT.stop();
      switch(id)
      {
        case KaossTest.INSTRUMENT_SINE:
          CURRENT_INSTRUMENT = SINE;
          break;
        case KaossTest.INSTRUMENT_TRIANGLE:
          CURRENT_INSTRUMENT = TRIANGLE;
          CURRENT_INSTRUMENT.resetEnvelope();
          break;
        case KaossTest.INSTRUMENT_SQUARE:
          CURRENT_INSTRUMENT = SQUARE;
          break;
	     case KaossTest.INSTRUMENT_REDNOISE:
          CURRENT_INSTRUMENT = REDNOISE;
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
        instrumentMix.connectOutput( 0, filter.input, 0 ); //connect instrument to filter (low pass)
    }

    public void kill()
    {
      CURRENT_INSTRUMENT.kill();
    }

    public void disableLFO()
    {
      CURRENT_INSTRUMENT.disableLFOs();
      //controller.start();    
    }

    public void enableLFO()
    { 
      CURRENT_INSTRUMENT.enableLFOs();
    }

    public void updateLFO()
    {
      CURRENT_INSTRUMENT.updateLFOs();
    }

    public void stopLFO()
    {
      CURRENT_INSTRUMENT.stopLFOs();
    }

    public void updateModRate(int rate)
    {
      CURRENT_INSTRUMENT.MOD_RATE = rate;
      //System.out.println("MOd "+rate);
      updateLFO();
    }

    public void updateModDepth(int depth)
    {
      CURRENT_INSTRUMENT.MOD_DEPTH = depth;
      //System.out.println("depth "+depth);      
      updateLFO();
    }

    public Instrument getInstrument()
    {
      return CURRENT_INSTRUMENT;
    }

    public SynthScope getScope()
    {
      return CURRENT_INSTRUMENT.getScope();
    }

    public boolean isPlaying()
    {
      return CURRENT_INSTRUMENT.isPlaying();
    }

    public void changeScale(int[] scale)
    {
      SAWTOOTH.changeScale(scale);
      SINE.changeScale(scale);
      TRIANGLE.changeScale(scale);
      SQUARE.changeScale(scale);
      SINGINGSAW.changeScale(scale);
      CUOMO.changeScale(scale);
  		GONG.changeScale(scale);
  	   MESSIER.changeScale(scale);
		SQUOISE.changeScale(scale);
    }

    public void changeFrequency(int offset)
    {
      CURRENT_INSTRUMENT.adjustFrequencyByOffset(offset);
    }

    public void changeAmplitude()
    {
    }

    public void lowpass(int freq)
    {
      filter.frequency.set(freq);
    }

    public void pan(double absolutePan)
    {
      //System.out.println(absolutePan);
      panUnit.pan.set(absolutePan);      
    }

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

    public void iThinkItsTheSauceBoss()
    {
      streamer.startStream();
			      
    }
    
}

