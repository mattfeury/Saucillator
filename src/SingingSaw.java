//
//  Class to test the Mac Mulittouch API mixed with JSyn.
//  Modifying some code shtuffs here

import java.util.*;
import com.softsynth.jsyn.*;

public class SingingSaw extends Instrument {

    int[] overtones = {0,2,4}; //this will give us the third and the fifth (index # in the scale)

    
  	private SynthEnvelope      envData;
    private EnvelopePlayer     envPlayer;

    public SingingSaw()
    {
        super();

        //set characteristics
        scale = majorScale;
        harmonics = noHarmonics;
        
        //make timbre and start        
        makeTimbre();
    }
        
    public void makeTimbre()
    {
      //envelope
			envPlayer = new EnvelopePlayer();  // create an envelope player
  		// define shape of envelope as an array of doubles
			double[] data =
			{
				0.2, 0.8,  // The envelope moves to 1.0 in 2 seconds.
				0.4, 1.0, 
				0.4, 0.0   
			};
			envData = new SynthEnvelope( data );

      //harmonics
      for(int i = 0; i < overtones.length; i++)
      {
        LineOut lineOut = new LineOut();        
        SineOscillator sineOsc = new SineOscillator();
        sineInputs.add(sineOsc);

        //stereo wavves
        sineOsc.output.connect( 0, lineOut.input, 0 );
        sineOsc.output.connect( 0, lineOut.input, 1 );
        envPlayer.output.connect( sineOsc.amplitude );
        
        lineOut.start();
        sineOsc.amplitude.set(1.0);
      }

      envPlayer.start();
    }
    
    public void start()  
    {
       System.out.println("start");
       isPlaying = true;
   		 envPlayer.envelopePort.queueLoop(0, envData );  // queue an envelope
       
       for(SineOscillator sineOsc : sineInputs)
         sineOsc.start();
    }
    
    public void stop()
    {
       System.out.println("stop");
       isPlaying = false;
       envPlayer.envelopePort.clear(); // clear the queue
       
       for(SineOscillator sineOsc : sineInputs)
         sineOsc.stop();
    }
    
    public void adjustFrequencyByOffset(int offset) {
        
        
        //harmonic mode
        int i = 0;
//        double scaleOffset = getScaleIntervalFromOffset(scale, offset);    
//        int freq = (int)(Math.pow(2,((scaleOffset) / 12)) * BASE_FREQ);
        
        for(SineOscillator sineOsc : sineInputs)
        {
            //overtone offset
            double scaleOffset = getScaleIntervalFromOffset(scale, (int)offset + overtones[i]);
            int freq = (int)(Math.pow(2,((scaleOffset) / 12)) * BASE_FREQ);
            
            sineOsc.frequency.set(freq);
           // System.out.println(i + ": " +freq);
            i++;
        }
    }   
    
    
}

