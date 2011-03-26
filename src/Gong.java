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
   private EnvelopePlayer envPlayer2;

    public Gong()
    {
        super();
		  MOD_DEPTH = 8;
        MOD_RATE = 15;

		  BASE_FREQ /= 2;
        //set characteristics
        scale = minorScale;        
        harmonics = addHarmonics(highHarmonics, oddHarmonics); 

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
			
			// envelope for triangle wave
			envPlayer2 = new EnvelopePlayer();  
  	   	// define shape of envelope as an array of doubles
			double[] data2 =
			{
				0.0, 1.0,  
				1.0, 0.0,
         	1.0, 1.0
				//2.0, 0.0  
			};
			envData2 = new SynthEnvelope( data2 );
	
	 
        mixer = new SynthMixer(harmonics.length, 2);    
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
		  for(int i = 0; i < oddHarmonics.length; i++)
      {
        SineOscillator sineOsc = new SineOscillator();
        sineInputs.add(sineOsc);
        freqMods.add(sineOsc.frequency);

        //stereo wavves
	      mixer.connectInput( i + highHarmonics.length, sineOsc.output, 0 );
	      mixer.setGain( i + highHarmonics.length, 0, amplitude / Math.pow(i+1,2));
	      mixer.setGain( i + highHarmonics.length, 1, amplitude / Math.pow(i+1,2));
	
			  envPlayer2.output.connect( sineOsc.amplitude );

        sineOsc.amplitude.set(amplitude);  //triangle
      }

		  envPlayer.start();
		  envPlayer2.start();   
    }
    
    public void start()  
    {
       System.out.println("start GONG");
       isPlaying = true;
		 
		 //for sine wave
		 envPlayer.envelopePort.clear(); // clear the queue        
       envPlayer.envelopePort.queueLoop( envData );  // queue an envelope
		 
		 //for triangle wave
		 envPlayer2.envelopePort.clear(); // clear the queue        
       envPlayer2.envelopePort.queueLoop( envData2 );  // queue an envelope
		 
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
		  
		  //for(int i = 0;
		  
        
        for(SynthInput freqMod : freqMods)
        {
            //overtone offset
            //double scaleOffset = getScaleIntervalFromOffset(scale, (int)inc + overtones[i]);
				
				freqMod.set(freq * harmonics[i]);      
            //harmonic offset
				/*
				if(i < highHarmonics.length)
	            freqMod.set(freq * highHarmonics[i]);
				else
					freqMod.set(freq * oddHarmonics[i - highHarmonics.length]);
				*/	
            i++;
        }
    }   
    
    
}

