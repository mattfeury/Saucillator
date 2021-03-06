package com.mattfeury.saucillator.macbook;

/*
 * Red Noise instrument class. Creates gritty sounding noise similar to 
 * white noise. Uses noise oscillator from JSyn.
 *
 * http://www.softsynth.com/jsyn/docs/autodocs/com/softsynth/jsyn/RedNoise.html
 *
 * @author theChillwavves  
 * 
 */

import java.util.*;
import com.softsynth.jsyn.*;

public class RedNoise extends Instrument {

    public RedNoise()
    {
        super();

        //set characteristics
        harmonics = noHarmonics; //sine

        //make timbre and start        
        makeTimbre();
        startScope();        
    }
    
    public void makeTimbre()
    {
        mixer = new SynthMixer(harmonics.length, 2);      
        for(int i = 0; i < harmonics.length; i++)
          {
            com.softsynth.jsyn.RedNoise noiseOsc = new com.softsynth.jsyn.RedNoise();
            sineInputs.add(noiseOsc);
            freqMods.add(noiseOsc.frequency);

            //stereo wavves
            mixer.connectInput( i, noiseOsc.output, 0 );
            mixer.setGain( i, 0, amplitude * 2 );
            mixer.setGain( i, 1, amplitude * 2 ); //it's quiet

            noiseOsc.amplitude.set(amplitude);  //noise
          }
    }
    
    public void start()  
    {
       System.out.println("start");
       isPlaying = true;

       envPlayer.envelopePort.clear(); // clear the queue        
       envPlayer.envelopePort.queueLoop(envData );  // queue an envelope
              
       for(SynthUnit sineOsc : sineInputs)
         sineOsc.start();
    }
    
    public void stop()
    {
       System.out.println("stop");
       isPlaying = false;
       for(SynthUnit sineOsc : sineInputs)
         sineOsc.stop();
    }
    
    public void adjustFrequencyByOffset(int offset) {
        
        int i = 0;
        double scaleOffset = getScaleIntervalFromOffset(scale, offset);    
        int freq = (int)(Math.pow(2,((scaleOffset) / 12)) * BASE_FREQ * 2);
        
        for(SynthInput freqMod : freqMods)
        {
            //harmonic offset
            freqMod.set(freq * harmonics[i]);
            i++;
        }

    }   
    
    
}

