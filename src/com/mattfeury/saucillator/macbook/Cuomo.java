package com.mattfeury.saucillator.macbook;

/*
 *  Mike's Instrument #1: Cuomo
 */

import java.util.*;
import com.softsynth.jsyn.*;

/*
 * Cuomo instrument class. Uses Sine and Square Instruments to create
 * a sound similar to a synth lead. Uses no envelopes.
 * @author Mike Hirth  
 * 
 */

public class Cuomo extends Instrument {
  
    public Cuomo(Instrument... extras)
    {
        super();

        //set characteristics
		    harmonics = new int[]{}; //Add sine and square harmonics

        for(Instrument i : extras)
          extraneous.add(i);    
        
        //make timbre and start        
        makeTimbre();
        startScope();    
    }
    /*
     *  Defines details of the envelopes. Creates sine oscillators and
     *  connects the harmonics to the mixer.
     */
    public void makeTimbre()
    {
       mixer = new SynthMixer(harmonics.length + extraneous.size(), 2);  

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
    
    /*
     *  Manages envelopes for both the sine wave and the square wave.  
     */
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
    /*
     *  Stops the instruments and the sine 
     *  oscillators are stopped.
     */
    public void stop()
    {
       System.out.println("stop");
       isPlaying = false;

       for(Instrument extra : extraneous)       
         extra.stop();

       for(SynthOscillator sineOsc : sineInputs)
         sineOsc.stop();
    }
    /*
     *  Defines frequency and sets depending on the harmonics.
     */
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

