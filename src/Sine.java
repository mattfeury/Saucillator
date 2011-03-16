//
//  Class to test the Mac Mulittouch API mixed with JSyn.
//  Modifying some code shtuffs here

import java.util.*;
import com.softsynth.jsyn.*;

public class Sine extends Instrument {

    public Sine()
    {
        super();

        //set characteristics
        scale = majorScale;
        harmonics = noHarmonics; //square & triangle

        //make timbre and start        
        makeTimbre();
        connectMixerToLineOut();        
    }
    
    public void makeTimbre()
    {
        mixer = new SynthMixer(harmonics.length, 2);      
        for(int i = 0; i < harmonics.length; i++)
          {
            SineOscillator sineOsc = new SineOscillator();
            sineInputs.add(sineOsc);

            //stereo wavves
            mixer.connectInput( i, sineOsc.output, 0 );
            mixer.setGain( i, 0, amplitude );
            mixer.setGain( i, 1, amplitude );

            sineOsc.amplitude.set(amplitude);  //triangle
          }
    }
    
    public void start()  
    {
       System.out.println("start");
       isPlaying = true;
       for(SineOscillator sineOsc : sineInputs)
         sineOsc.start();
    }
    
    public void stop()
    {
       System.out.println("stop");
       isPlaying = false;
       for(SineOscillator sineOsc : sineInputs)
         sineOsc.stop();
    }
    
    public void adjustFrequencyByOffset(int offset) {
        
        
        //harmonic mode
        int i = 0;
        double scaleOffset = getScaleIntervalFromOffset(scale, offset);    
        int freq = (int)(Math.pow(2,((scaleOffset) / 12)) * BASE_FREQ);
        
        for(SineOscillator sineOsc : sineInputs)
        {
            //harmonic offset
            sineOsc.frequency.set(freq * harmonics[i]);
            i++;
        }
    }   
    
    
}

