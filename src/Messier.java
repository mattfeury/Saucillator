//
//  Instrument that combines square and sine waves

import java.util.*;
import com.softsynth.jsyn.*;

public class Messier extends Instrument {

    public Messier()
    {
        super();

        //set characteristics
        scale = majorScale;
		harmonics = oddHarmonics; //Add triangle harmonics and noise

        //make timbre and start        
        makeTimbre();
        startScope();        
    }
    
    public void makeTimbre()
    {
        mixer = new SynthMixer(harmonics.length + 1, 2);      
        for(int i = 0; i < harmonics.length; i++)
          {
            SineOscillator sineOsc = new SineOscillator();
            sineInputs.add(sineOsc);
            freqMods.add(sineOsc.frequency);

            //stereo wavves
            mixer.connectInput( i, sineOsc.output, 0 );
            mixer.setGain( i, 0, amplitude );
            mixer.setGain( i, 1, amplitude );

            sineOsc.amplitude.set(amplitude / Math.pow(i+1,2));; //Set amplitude for triangle harmonics 
          }
		//Add noise
		com.softsynth.jsyn.RedNoise noiseOsc = new com.softsynth.jsyn.RedNoise();
		sineInputs.add(noiseOsc);
        freqMods.add(noiseOsc.frequency);

        //stereo wavves
        mixer.connectInput(harmonics.length, noiseOsc.output, 0 );
        mixer.setGain( harmonics.length, 0, amplitude );
        mixer.setGain( harmonics.length, 1, amplitude );

        noiseOsc.amplitude.set(amplitude);  //noise
    }
    
    public void start()  
    {
       System.out.println("start");
       isPlaying = true;
       for(SynthOscillator sineOsc : sineInputs)
         sineOsc.start();
    }
    
    public void stop()
    {
       System.out.println("stop");
       isPlaying = false;
       for(SynthOscillator sineOsc : sineInputs)
         sineOsc.stop();
    }
    
    public void adjustFrequencyByOffset(int offset) {
		for(int i=0; i < harmonics.length; i++) 
		{
			double scaleOffset = getScaleIntervalFromOffset(scale, (int)offset + harmonics[i]);
			int freq = (int)(Math.pow(2,((scaleOffset) / 12)) * BASE_FREQ);
			
			freqMods.get(i).set(freq * harmonics[i]);
		}


    }   
    
    
}

