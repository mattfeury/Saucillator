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

public class Gong extends Instrument {

  private SynthEnvelope envData;
  private EnvelopePlayer envPlayer;
	
	private SynthEnvelope envData2;

  private float baseFreq;

  public Gong(Instrument... extras)
  {
    super();

    //set characteristics
    MOD_DEPTH = 8;
    MOD_RATE = 15;
    harmonics = highHarmonics; //just the high harmonics since we import the triangle
    customEnvelope = true;

    for(Instrument i : extras)
      extraneous.add(i);    
    
    //make timbre and start        
    makeTimbre();
    startScope();
  }
      
  public void makeTimbre()
  {
    // envelope for sine wave
    envPlayer = new EnvelopePlayer();  
    // define shape of envelope as an array of doubles
    double[] data =
    {
      0.0, 0.0,  
      1.0, 0.9,
        1.0, 0.0
      //2.0, 1.0  
    };
    envData = new SynthEnvelope( data );
    
    // for the triangle
    double[] data2 =
    {
      0.0, 1.0,  
      1.0, 0.0,
        1.0, 1.0
      //2.0, 0.0  
    };
    envData2 = new SynthEnvelope( data2 );

 
    mixer = new SynthMixer(harmonics.length + extraneous.size(), 2);    
    //highHarmonics
    for(int i = 0; i < highHarmonics.length; i++)
    {
      SineOscillator sineOsc = new SineOscillator();
      sineInputs.add(sineOsc);
      freqMods.add(sineOsc.frequency);

      //stereo wavves
      mixer.connectInput( i, sineOsc.output, 0 );
      mixer.setGain( i, 0, amplitude / (i+1));
      mixer.setGain( i, 1, amplitude / (i+1));

      envPlayer.output.connect( sineOsc.amplitude );
    
      sineOsc.amplitude.set(amplitude); //sawtooth and square
    }

    //triangle most likely
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

    envPlayer.start();
  }
  
  public void start()  
  {
    System.out.println("start GONG");
    isPlaying = true;
   
    //for sine wave
    envPlayer.envelopePort.clear(); // clear the queue        
    envPlayer.envelopePort.queueLoop( envData );  // queue an envelope
    
    for(Instrument extra : extraneous) {
      extra.setEnvelopeData(envData2);      
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
    baseFreq = BASE_FREQ / 2;
      
    for(Instrument extra : extraneous)       
      extra.adjustFrequencyByOffset(offset);
    
    //harmonic mode
    int i = 0;
    double scaleOffset = getScaleIntervalFromOffset(scale, offset);    
    int freq = (int)(Math.pow(2,((scaleOffset) / 12)) * baseFreq);
  
    for(SynthInput freqMod : freqMods)
    {
      //overtone offset
      //double scaleOffset = getScaleIntervalFromOffset(scale, (int)inc + overtones[i]);
      freqMod.set(freq * harmonics[i]);              
      i++;
    }
  }   
    
    
}

