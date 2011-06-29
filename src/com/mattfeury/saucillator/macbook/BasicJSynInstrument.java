package com.mattfeury.saucillator.macbook;

/*
 * Basic abstract class for an instrument that runs off a JSyn Oscillator.
 *
 * @author theChillwavves  
 */

import java.util.*;
import com.softsynth.jsyn.*;

import java.awt.*;

import com.softsynth.jsyn.view102.SynthScope;

public abstract class BasicJSynInstrument extends Instrument {

  private Class<?> clazz;
    public BasicJSynInstrument(Class<?> clazz, int[] harmonics) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InstantiationException
    {
        super();

        this.clazz = clazz;

        //set characteristics
        this.harmonics = harmonics;

        //make timbre and start        
        makeTimbre();
        startScope();
    }
        
    public void makeTimbre() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InstantiationException
    {
      mixer = new SynthMixer(harmonics.length, 2);

      for(int i = 0; i < harmonics.length; i++)
      {
        SynthOscillator osc = (SynthOscillator)clazz.newInstance();
        
        sineInputs.add(osc);
        freqMods.add(osc.frequency);

        //stereo wavves
        mixer.connectInput( 0, osc.output, 0 );
        mixer.setGain( 0, 0, amplitude );
        mixer.setGain( 0, 1, amplitude );
        osc.amplitude.set(amplitude);
      }
    }
    
    public void start()  
    {
       System.out.println("start");
       isPlaying = true;

       envPlayer.envelopePort.clear(); // clear the queue        
       envPlayer.envelopePort.queueLoop(envData );  // queue an envelope       
       
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
            
      //harmonic mode
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

