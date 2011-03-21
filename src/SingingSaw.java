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
    private LinearLag lagUnit;

    public SingingSaw()
    {
        super();

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
				1.0, 1.0
//				0.4, 0.0   
			};
			envData = new SynthEnvelope( data );

      //harmonics
			lagUnit = new LinearLag(); //this doesn't work at all
  		lagUnit.time.set( 0.05 );


      mixer = new SynthMixer(overtones.length, 2);

      for(int i = 0; i < overtones.length; i++)
      {
        SineOscillator sineOsc = new SineOscillator();
        sineInputs.add(sineOsc);

        //lagUnit.output.connect( sineOsc.frequency );
        //DelayUnit delayUnit = new DelayUnit(0.3 * (i+1));
        //delayUnit.input.connect(sineOsc.output);
        //stereo wavves
			  mixer.connectInput( i, sineOsc.output, 0 );
        //mixer.connectInput( i+overtones.length, delayUnit.output, 0);

        mixer.setGain( i, 0, .7);
        mixer.setGain( i, 1, .7);
        //mixer.setGain( i + overtones.length, 0, .3);
        //mixer.setGain( i + overtones.length, 1, .3);
        
        
        envPlayer.output.connect( sineOsc.amplitude );
        
        sineOsc.amplitude.set(1.0);
        //delayUnit.start();
      }
      envPlayer.start();
      lagUnit.start();
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

    public void fadeOut()
    {
     /* double[] data =
			{
				0.0, 0.8,  // The envelope moves to 1.0 in 2 seconds.
				0.5, 0.0
			};
			SynthEnvelope envData = new SynthEnvelope( data );

      envPlayer.envelopePort.clear(); // clear the queue        
      envPlayer.envelopePort.queue(envData );  // queue an envelope

      try {
        Thread.sleep(500);
      } catch(Exception e) {
        System.out.println(e);
      }
*/
    }
    
    public void stop()
    { 
   //   fadeOut();
      System.out.println("stop");
      isPlaying = false;
      // envPlayer.envelopePort.clear(); // clear the queue
      
      for(SynthOscillator sineOsc : sineInputs)
        sineOsc.stop();
    }
    
    public void adjustFrequencyByOffset(int offset) {  
        int i = 0; 
        for(SynthOscillator sineOsc : sineInputs)
        {
            //overtone offset
            double scaleOffset = getScaleIntervalFromOffset(scale, (int)offset + overtones[i]);
            int freq = (int)(Math.pow(2,((scaleOffset) / 12)) * BASE_FREQ);
            
            //int freq = (int)((float)((float)offset / KaossTest.TRACKPAD_GRID_SIZE + 1) * BASE_FREQ);
            sineOsc.frequency.set(freq);
           // System.out.println(i + ": " +freq);
            i++;
        }
    }   
    
    
}

