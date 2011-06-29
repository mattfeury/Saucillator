package com.mattfeury.saucillator.macbook;

/*
 *  Leah's Instrument #1: The Gong
 */

import java.util.*;
import com.softsynth.jsyn.*;

import java.awt.*;
import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;


import com.softsynth.jsyn.view102.SynthScope;

/*
 *  This is the Gong class. This instrument utilizes sine waves and a triangle wave to create a 
 *  sound inspired by a gong. 
 *  @author Leah Downey
 */
public class Gong extends Instrument {

  private SynthEnvelope envData;
  private EnvelopePlayer envPlayer;
	
  private SynthEnvelope envData2;

  private float baseFreq;

  /*
   *  The method Gong takes in Instruments and adds them to a linked list defined in 
   *  Instrument.  Here the various variables from Instrument are set.  Also, makeTimbre()
   *  and startScope() are called.
   */
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
   
  /*
   *  In makeTimbre the details of the envelopes are defined using arrays.  The sine oscillators that 
   *  create the sine waves for each of the harmonics are created.  The output from the 
   *  sine oscillators is added to the linked list sineInputs and is connected to a 
   *  SynthMixer.  As a Triangle is passed into Gong() as an argument, it is also 
   *  connected to the mixer.  Here, the envelope starts.
   */   
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
    };
    envData = new SynthEnvelope( data );
    
    // for the triangle
    double[] data2 =
    {
      0.0, 1.0,  
      1.0, 0.0,
      1.0, 1.0  
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

    //triangle 
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
  
  /*
   *  This method manages the envelopes for both the sine waves and the triangle wave.  
   */
  public void start()  
  {
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
  
  /*
   *  This gets called when the gong is no longer playing.  The triangle wave and the sine 
   *  oscillators are stopped.
   */
  public void stop()
  {
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
    baseFreq = BASE_FREQ / 2;
      
    for(Instrument extra : extraneous)       
      extra.adjustFrequencyByOffset(offset);
    
    int i = 0;
    double scaleOffset = getScaleIntervalFromOffset(scale, offset);    
    int freq = (int)(Math.pow(2,((scaleOffset) / 12)) * baseFreq);
  
    for(SynthInput freqMod : freqMods)
    {
      //harmonic offset
      freqMod.set(freq * harmonics[i]);              
      i++;
    }
  }   
    
    
}
