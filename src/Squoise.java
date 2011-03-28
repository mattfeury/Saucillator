
import java.util.*;
import com.softsynth.jsyn.*;

import java.awt.*;
import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;


import com.softsynth.jsyn.view102.SynthScope;

public class Squoise extends Instrument {

    public Squoise()
    {
        super();

        //set characteristics
        scale = chromaticScale;        
        harmonics = addHarmonics(noHarmonics, oddHarmonics); //for noise and square

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
            mixer.setGain( i, 0, 0.5);
            mixer.setGain( i, 1, 0.5);

            
            sineOsc.amplitude.set(amplitude / (i+1)); //for square
          }   
			 
			 com.softsynth.jsyn.RedNoise noiseOsc = new com.softsynth.jsyn.RedNoise();
      	 sineInputs.add(noiseOsc);
      	 freqMods.add(noiseOsc.frequency);
      	 mixer.connectInput( harmonics.length, noiseOsc.output, 0 );
      	 mixer.setGain( harmonics.length, 0, .2);
     	    mixer.setGain( harmonics.length, 1, .2);
      	 noiseOsc.amplitude.set(1.0); 
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
        
        
        //harmonic mode
        int i = 0;
        double scaleOffset = getScaleIntervalFromOffset(scale, offset);    
        int freq = (int)(Math.pow(2,((scaleOffset) / 12)) * BASE_FREQ);
        
        for(SynthInput freqMod : freqMods)
        {
            //overtone offset
            //double scaleOffset = getScaleIntervalFromOffset(scale, (int)inc + overtones[i]);
            
            //harmonic offset
            freqMod.set(freq * harmonics[i]);
            i++;
        }
    }   
    
    
}

