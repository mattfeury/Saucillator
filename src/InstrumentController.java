//
// Controls an instrument based on trackpad movement.
// This should probably be modified to control multiple instruments

//import java.util.*;
  
import com.softsynth.jsyn.*;
import com.softsynth.jsyn.view102.SynthScope;

public class InstrumentController {

    private Instrument instrument;

    public InstrumentController(Instrument i)
    {
        this.instrument = i;
        //i.start();
    }

    public void start()
    {
      instrument.start();
    }

    public void stop()
    {
      instrument.stop();
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
    
}

