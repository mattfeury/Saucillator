/*
 * Square instrument class. Uses odd harmonics to create a square wave.
 *
 * @author theChillwavves  
 * 
 */

import java.util.*;
import com.softsynth.jsyn.*;

import java.awt.*;
import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;

import com.softsynth.jsyn.view102.SynthScope;

public class Square extends Instrument {

    public Square()
    {
        super();

        //set characteristics
        harmonics = oddHarmonics; //square & triangle

        //make timbre and start        
        makeTimbre();
        startScope();
    }
        
    public void makeTimbre()
    {
        mixer = new SynthMixer(harmonics.length, 2);    
        for(int i = 0; i < harmonics.length; i++)
          {
            SineOscillator sineOsc = new SineOscillator();
            sineInputs.add(sineOsc);
            freqMods.add(sineOsc.frequency);

            //stereo wavves
            mixer.connectInput( i, sineOsc.output, 0 );
            mixer.setGain( i, 0, amplitude / (i+1));
            mixer.setGain( i, 1, amplitude / (i+1));

            
            sineOsc.amplitude.set(amplitude); //sawtooth and square
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

