package com.mattfeury.saucillator.macbook;

/*
 *  Matt's Instrument #1: Singing Saw
 */
import java.util.*;
import com.softsynth.jsyn.*;


import java.awt.*;
import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;


import com.softsynth.jsyn.view102.SynthScope;

/*
 * This instrument emulates the sound of a musical saw. To do so, we default the LFO to settings
 * that will create a light vibrato and also create a lag between notes that will cause a slow gradual
 * change between frequencies.
 * 
 * @author Matt Feury
 */
public class SingingSaw extends Instrument {

    int[] overtones = {0,2,4}; //this will give us the third and the fifth (index # in the scale)
    
  	private SynthEnvelope      envData;
    private EnvelopePlayer     envPlayer;

    /*
     * Create a SingingSaw instrument and define the custom LFO settings.
     */
    public SingingSaw()
    {
        super();
        LAG_LIFE = 0.4; //0.4
        MOD_DEPTH = 16;
        MOD_RATE = 9;
        customEnvelope = true;

        //set characteristics
        harmonics = noHarmonics;
 
        //make timbre and start        
        makeTimbre();
        startScope();
    }
    
    /*
     * Create the SingingSaw envelope. This will slowly rise in amplitude and peak for half a second
     * then come back down to normal amplitude.
     *
     * We use basic sine waves to create the timbre. Rather than setting them at harmonics, we set
     * them up as overtones, creating a chord (1-3-5). We also use RedNoise to simulate the texture
     * of the violin bow.
     */    
    public void makeTimbre()
    {
      //envelope
			envPlayer = new EnvelopePlayer();  // create an envelope player
  		// define shape of envelope as an array of doubles
			double[] data =
			{
				0.0, 0.0,  // The envelope moves to 1.0 in 2 seconds.
				0.8, 1.0,
        0.5, 1.5,
        0.3, 1.0
			};
			envData = new SynthEnvelope( data );

      //harmonics
      mixer = new SynthMixer(overtones.length + 1, 2);

      //sines
      for(int i = 0; i < overtones.length; i++)
      {
        SineOscillator sineOsc = new SineOscillator();
        sineInputs.add(sineOsc);
        freqMods.add(sineOsc.frequency);

        //stereo wavves
		    mixer.connectInput( i, sineOsc.output, 0 );

        mixer.setGain( i, 0, .5);
        mixer.setGain( i, 1, .5);
        
        envPlayer.output.connect( sineOsc.amplitude );
        
        sineOsc.amplitude.set(1.0);      
      }

      //red noise
      com.softsynth.jsyn.RedNoise noiseOsc = new com.softsynth.jsyn.RedNoise();
      sineInputs.add(noiseOsc);
      freqMods.add(noiseOsc.frequency);
      mixer.connectInput( overtones.length, noiseOsc.output, 0 );
      mixer.setGain( overtones.length, 0, 0.05);
      mixer.setGain( overtones.length, 1, 0.05);
      envPlayer.output.connect( noiseOsc.amplitude );
      noiseOsc.amplitude.set(1.0);      


      envPlayer.start();
    }

    /*
     * Start the singingSaw. Queue the envelope, not as a loop though.
     */
    public void start()  
    {
      isPlaying = true;
       
      envPlayer.envelopePort.clear(); // clear the queue        
      envPlayer.envelopePort.queue(envData );  // queue an envelope
       
      for(SynthOscillator sineOsc : sineInputs)
        sineOsc.start();
    }
    
    /*
     * Stop the oscillators.
     */
    public void stop()
    { 
      isPlaying = false;
      
      for(SynthOscillator sineOsc : sineInputs)
        sineOsc.stop();
    }
    
    /*
     * adjust the frequency for these oscillators and any they inherit
     *
     * this is a little different because we don't use harmonics. Rather we use overtones to create a chord.
     */    
    public void adjustFrequencyByOffset(int offset) {  
      //sine oscs
      for(int i = 0; i < overtones.length; i++)
      {
        //overtone offset
        double scaleOffset = getScaleIntervalFromOffset(scale, (int)offset + overtones[i]);
        int freq = (int)(Math.pow(2,((scaleOffset) / 12)) * BASE_FREQ);
        
        //System.out.println(freq);
        freqMods.get(i).set(freq);
      }
 
      //noise osc
      double scaleOffset = getScaleIntervalFromOffset(scale, (int)offset + 24);
      int freq = (int)(Math.pow(2,((scaleOffset) / 12)) * BASE_FREQ);
      
      freqMods.get(overtones.length).set(freq);
      
    }   
    
    
}

