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


public class SingingSaw extends Instrument {

    int[] overtones = {0,2,4}; //this will give us the third and the fifth (index # in the scale)
    private int lastFreq = BASE_FREQ;
    
  	private SynthEnvelope      envData;
    private EnvelopePlayer     envPlayer;

    public SingingSaw()
    {
        super();
        LAG_LIFE = 0.2; //0.4
        MOD_DEPTH = 16;
        MOD_RATE = 9;

        //set characteristics
        scale = majorScale;
        harmonics = noHarmonics;
 
        //make timbre and start        
        makeTimbre();
        startScope();
    }
        
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

//				0.4, 0.0   
			};
			envData = new SynthEnvelope( data );

      //harmonics

//      KaossTest.context.startEngine(0);
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
      mixer.setGain( overtones.length, 0, .2);
      mixer.setGain( overtones.length, 1, .2);
      envPlayer.output.connect( noiseOsc.amplitude );
      noiseOsc.amplitude.set(1.0);      


      envPlayer.start();
    }
    
    public void start()  
    {
      System.out.println("start");
      isPlaying = true;
       
      envPlayer.envelopePort.clear(); // clear the queue        
      envPlayer.envelopePort.queue(envData );  // queue an envelope
       
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

