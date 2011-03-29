/*
 *  Leah's Instrument #2: Squoise
 */

import java.util.*;
import com.softsynth.jsyn.*;

import java.awt.*;
import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;


import com.softsynth.jsyn.view102.SynthScope;

/*
 *  This is the Squoise class.  This instrument is a marriage between the Square and the 
 *  Triangle waves.
 *  @author Leah Downey
 */
public class Squoise extends Instrument {

  /*
   *  The method Squoise takes in Instruments and adds them to a linked list defined in 
   *  Instrument.  Here the various variables from Instrument are set. Also, makeTimbre()
   *  and startScope() are called.
   */
  public Squoise(Instrument... extras)
  {
    super();

    //set characteristics
    harmonics = new int[]{}; 

    for(Instrument i : extras)
      extraneous.add(i);
    
    //make timbre and start
    makeTimbre();
    startScope();
  }
  
  /*
   *  In makeTimbre a SynthMixer is created and the output of extraMixer (for both the 
   *  the square and the noise waves) is connected to the mixer.  
   */    
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
  
  /*
   *  This method sets everything in motion.
   */
  public void start()
  {
    isPlaying = true;
    
    for(Instrument extra : extraneous) {
      extra.start();
    }
    
    for(SynthOscillator sineOsc : sineInputs)
      sineOsc.start();
  }
  
  /*
   *  This method stops all the motion.
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
   *  In this method, the frequency is defined and set depending on the harmonics.
   */
  public void adjustFrequencyByOffset(int offset) {
      
    for(Instrument extra : extraneous)
      extra.adjustFrequencyByOffset(offset);
    
    //harmonic mode
    int i = 0;
    double scaleOffset = getScaleIntervalFromOffset(scale, offset);
    int freq = (int)(Math.pow(2,((scaleOffset) / 12)) * (BASE_FREQ / 2));
  
    for(SynthInput freqMod : freqMods)
    {
      //overtone offset
      freqMod.set(freq * harmonics[i]);
      i++;
    }
  }
    
    
}