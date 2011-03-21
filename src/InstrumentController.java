//
// Controls an instrument based on trackpad movement.
// This should probably be modified to control multiple instruments

//import java.util.*;
  
import com.softsynth.jsyn.*;
import com.softsynth.jsyn.view102.SynthScope;

public class InstrumentController {

    private Instrument instrument;
    private TunableFilter filter;
    private SynthFilter effectsUnit;
    private PanUnit panUnit;
    private LineOut lineOut;
    private AddUnit effectsAdder;

    public static boolean fxEnabled = false;

    public InstrumentController(Instrument i)
    {
        this.instrument = i;
        //i.start();
    }

    public void start()
    {
      SynthMixer instrumentMix = instrument.getMixer();
      
      panUnit = new PanUnit();
      filter = new Filter_LowPass();
      effectsUnit = new DelayUnit(0.5); 
      effectsAdder = new AddUnit();
      lineOut = new LineOut();        
  
      instrumentMix.connectOutput( 0, filter.input, 0 ); //connect instrument to filter (low pass)

      filter.output.connect(effectsUnit.input); //send filter mix like an aux send using fx adder
      filter.output.connect(effectsAdder.inputA); //and also the effects unit


      if(fxEnabled) //if we want fx send them to the adder
        effectsUnit.output.connect(effectsAdder.inputB);

      effectsAdder.output.connect( panUnit.input); //connect to pan unit

      panUnit.output.connect( 0, lineOut.input, 0 ); //pan unit goes to line out, ala sound
      panUnit.output.connect( 1, lineOut.input, 1 );

      filter.Q.set(1.0);
      filter.frequency.set(20000);


      lineOut.start();
      effectsAdder.start();
      panUnit.start();
      effectsUnit.start();
      filter.start();
      instrumentMix.start();
      System.out.println("controller made");

    }

    public void startInstrument()
    {
      instrument.start();
    }      

    public void stop()
    {
      instrument.stop();
    }

    public void disableLFO()
    {
      instrument.disableLFOs();
      //controller.start();    
    }

    public void enableLFO()
    { 
      instrument.enableLFOs();
    }

    public void updateLFO()
    {
      instrument.updateLFOs();
    }

    public void stopLFO()
    {
      instrument.stopLFOs();
    }

    public void updateModRate(int rate)
    {
      instrument.MOD_RATE = rate;
      updateLFO();
    }

    public Instrument getInstrument()
    {
      return instrument;
    }

    public SynthScope getScope()
    {
      return instrument.getScope();
    }

    public boolean isPlaying()
    {
      return instrument.isPlaying();
    }

    public void changeFrequency(int offset)
    {
      instrument.adjustFrequencyByOffset(offset);
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
    
}

