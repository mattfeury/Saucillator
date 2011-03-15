//
//  Class to test the Mac Mulittouch API mixed with JSyn.
//  Modifying some code shtuffs here

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
        scale = majorScale;        
        harmonics = oddHarmonics; //square & triangle

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
            mixer.setGain( i, 0, amplitude / (i+1) );
            mixer.setGain( i, 1, amplitude / (i+1) );

            
            sineOsc.amplitude.set(amplitude / (i+1)); //sawtooth and square
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
            //overtone offset
            //double scaleOffset = getScaleIntervalFromOffset(scale, (int)inc + overtones[i]);
            
            //harmonic offset
            sineOsc.frequency.set(freq * harmonics[i]);
            i++;
        }
    }   
    
    
}

