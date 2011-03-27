//
//  Instrument that combines square and sine waves

import java.util.*;
import com.softsynth.jsyn.*;

public class Cuomo extends Instrument {

    
    public Cuomo(Instrument... extras)
    {
        super();

        //set characteristics
        scale = majorScale;
		    harmonics = new int[]{}; //Add sine and square harmonics

        for(Instrument i : extras)
          extraneous.add(i);    
        
        //make timbre and start        
        makeTimbre();
        startScope();    
    }
    
    public void makeTimbre()
    {
       mixer = new SynthMixer(harmonics.length + extraneous.size(), 2);  
       //this can go away since there are no custom harmonics. i'll leave it here in case you want to play around with more
       for(int i = 0; i < harmonics.length; i++)
       {
         SineOscillator sineOsc = new SineOscillator();
         sineInputs.add(sineOsc);
         freqMods.add(sineOsc.frequency);

         //stereo wavves
         mixer.connectInput( i, sineOsc.output, 0 );
         mixer.setGain( i, 0, amplitude );
         mixer.setGain( i, 1, amplitude );

         sineOsc.amplitude.set(1 / harmonics[i]); //Full amplitude for sine harmonic, 1/n for the rest  
       }

       int i =0;
       for(Instrument extra : extraneous)
       {
         SynthMixer extraMixer = extra.getMixer();
         mixer.connectInput( harmonics.length + i, extraMixer.getOutput(0), 0);
         mixer.setGain( harmonics.length + i, 0, amplitude * 2.0 / 3.0 );
         mixer.setGain( harmonics.length + i, 1, amplitude * 2.0 / 3.0 );
         extraMixer.start();
         i++;
       }
        
    }
    
    public void start()  
    {
       System.out.println("start");
       isPlaying = true;

       envPlayer.envelopePort.clear(); // clear the queue         
       envPlayer.envelopePort.queueLoop(envData );  // queue an envelope

       for(Instrument extra : extraneous)
         extra.start();

       for(SynthOscillator sineOsc : sineInputs)
         sineOsc.start();
    }
    
    public void stop()
    {
       System.out.println("stop");
       isPlaying = false;

       for(Instrument extra : extraneous)       
         extra.stop();

       for(SynthOscillator sineOsc : sineInputs)
         sineOsc.stop();
    }
    
    public void adjustFrequencyByOffset(int offset) {
        //harmonic mode
        for(Instrument extra : extraneous)       
          extra.adjustFrequencyByOffset(offset);

        int i = 0;
        double scaleOffset = getScaleIntervalFromOffset(scale, offset);    
        int freq = (int)(Math.pow(2,((scaleOffset) / 12)) * BASE_FREQ);
        
        for(SynthInput freqMod : freqMods)
        {
            //harmonic offset
            freqMod.set(freq * harmonics[i]);
            i++;
        }
    }   
    
    
}

