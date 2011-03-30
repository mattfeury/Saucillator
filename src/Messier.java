/*
 *  Mike's Instrument #2: Messier
 */
 
import java.util.*;
import com.softsynth.jsyn.*;

/*
 * Messier instrument class. Uses Triangle and Noise Instruments to create
 * a spacey sound. Uses an envelope with a short duration.
 * @author Mike Hirth  
 * 
 */

public class Messier extends Instrument {
      
    public Messier(Instrument... extras)
    {
        super();
        // set characteristics
        MOD_DEPTH = 20;
        MOD_RATE = 25;

		    harmonics = new int[]{};
        customEnvelope = true;

        for(Instrument i : extras)
          extraneous.add(i);    
        
        // make timbre and start        
        makeTimbre();
        startScope();    
    }
    /*
     *  Defines details of the envelopes. Creates sine oscillators and
     *  connects the harmonics to the mixer.
     */
    public void makeTimbre()
    {
      // envelope for sine wave
      envPlayer = new EnvelopePlayer();  
      // define shape of envelope as an array of doubles
      // quick envelope sounds like short bursts
      double[] data =
      {
        0.02, 1.0,  // take 0.02 seconds to go to value 1.0.
        0.03, 0.5,  
        0.02, 0.0   
      };
      envData = new SynthEnvelope( data );

       mixer = new SynthMixer(harmonics.length + extraneous.size(), 2);  
       
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
     *  Manages envelopes for both the triangle wave and the noise wave.  
     */
    public void start()  
    {
       System.out.println("start");
       isPlaying = true;

       envPlayer.envelopePort.clear(); // clear the queue         
       envPlayer.envelopePort.queueLoop(envData );  // queue an envelope

       for(Instrument extra : extraneous)
       {
         extra.setEnvelopeData(envData); 
         extra.start();
       }

       for(SynthOscillator sineOsc : sineInputs)
         sineOsc.start();
    }
    /*
     *  Stops the instruments and the sine 
     *  oscillators are stopped.
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
     *  Defines frequency and sets depending on the harmonics.
     */
    public void adjustFrequencyByOffset(int offset) {
        //harmonic mode
        for(Instrument extra : extraneous)       
          extra.adjustFrequencyByOffset(offset);

        int i = 0;
        double scaleOffset = getScaleIntervalFromOffset(scale, offset);    
        int freq = (int)(Math.pow(2,((scaleOffset) / 12)) * BASE_FREQ);
        
        for(SynthInput freqMod : freqMods)
        {
            //harmonic offset
            freqMod.set(freq * harmonics[i]);
            i++;
        }
    }   
    
    
}

