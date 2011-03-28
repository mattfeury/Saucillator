
import java.util.*;
import com.softsynth.jsyn.*;

import java.awt.*;
import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;


import com.softsynth.jsyn.view102.SynthScope;

public class Squoise extends Instrument {

  public Squoise(Instrument... extras)
  {
    super();

    BASE_FREQ /= 2;
    //set characteristics
    scale = minorScale;        
    harmonics = new int[]{}; //just the high harmonics since we import the triangle

    for(Instrument i : extras)
      extraneous.add(i);    
    
    //make timbre and start        
    makeTimbre();
    startScope();
  }
      
  public void makeTimbre()
  {
 
    mixer = new SynthMixer(harmonics.length + extraneous.size(), 2);    

    //triangle most likely
    int i =0;
    for(Instrument extra : extraneous)
    {
      SynthMixer extraMixer = extra.getMixer();
      mixer.connectInput( harmonics.length + i, extraMixer.getOutput(0), 0);
		if(extra instanceof RedNoise)
		{
      	mixer.setGain( harmonics.length + i, 0, amplitude );
      	mixer.setGain( harmonics.length + i, 1, amplitude );
		} else {
      	mixer.setGain( harmonics.length + i, 0, amplitude * 0.5 );
      	mixer.setGain( harmonics.length + i, 1, amplitude * 0.5 );
		}
      extraMixer.start();
      i++;
    }

  }
  
  public void start()  
  {
    System.out.println("start GONG");
    isPlaying = true;
    
    for(Instrument extra : extraneous) {
      extra.start();
    }
    
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
      
    for(Instrument extra : extraneous)       
      extra.adjustFrequencyByOffset(offset);
    
    //harmonic mode
    int i = 0;
    double scaleOffset = getScaleIntervalFromOffset(scale, offset);    
    int freq = (int)(Math.pow(2,((scaleOffset) / 12)) * BASE_FREQ);
  
    for(SynthInput freqMod : freqMods)
    {
      //overtone offset
      //double scaleOffset = getScaleIntervalFromOffset(scale, (int)inc + overtones[i]);
      freqMod.set(freq * harmonics[i]);              
      i++;
    }
  }   
    
    
}

