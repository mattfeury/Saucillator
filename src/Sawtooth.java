/*
 *  Matt's Instrument #1: Super Sawtooth
 */

import java.util.*;
import com.softsynth.jsyn.*;

/*
 * What started as a simple sawtooth wave, I had to add my remaining channels (Square, Triangle)
 * to this in order to use all 4. So, the vision shifted from a simple sawtooth to something 
 * that I wanted to sound very artificial. I wanted it to be a strange mix of harmonics with
 * also a little bit of bitcrush in there to give it that artificial digital sound. Due to this
 * desire, no envelope was used.
 *
 * @author Matt Feury
 */
public class Sawtooth extends Instrument {

    public Sawtooth(Instrument... extras)
    {
        super();

        //set characteristics
        harmonics = allHarmonics; //sawtooth

        for(Instrument i : extras)
          extraneous.add(i);    

        //make timbre         
        makeTimbre();
        startScope();
    }

    /*
     *  Creates sine oscillators an their harmonics to create a sawtooth
     *  also connects the extra instruments to this mixer.
     */
    public void makeTimbre()
    {
        mixer = new SynthMixer(harmonics.length + extraneous.size(), 2);
        for(int i = 0; i < harmonics.length; i++)
        {
          SineOscillator sineOsc = new SineOscillator();
          sineInputs.add(sineOsc);
          freqMods.add(sineOsc.frequency);

          //stereo wavves
          mixer.connectInput( i, sineOsc.output, 0 );
          mixer.setGain( i, 0, amplitude / (i+1));
          mixer.setGain( i, 1, amplitude / (i+1));
          
          sineOsc.amplitude.set(amplitude); //sawtooth and square
        }
        int i =0;
        for(Instrument extra : extraneous)
        {
          SynthMixer extraMixer = extra.getMixer();
          mixer.connectInput( harmonics.length + i, extraMixer.getOutput(0), 0);
          mixer.setGain( harmonics.length + i, 0, amplitude * 0.5 );
          mixer.setGain( harmonics.length + i, 1, amplitude * 0.5 );
          extraMixer.start();
          i++;
        }

    }
    
    /*
     * Start all the oscillators and queue an envelope (in this case the default)
     */
    public void start()  
    {
       isPlaying = true;

       envPlayer.envelopePort.clear(); // clear the queue        
       envPlayer.envelopePort.queueLoop(envData );  // queue an envelope

       for(Instrument extra : extraneous)
         extra.start();       
              
       for(SynthOscillator sineOsc : sineInputs)
         sineOsc.start();
    }
    
    /*
     * stops the oscillators.
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
     * adjust the frequency for these oscillators and any they inherit
     */
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
            
            //harmonic offset
            freqMod.set(freq * harmonics[i]);
            i++;
        }
    }   
    
    
}

