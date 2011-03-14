/*
 * A test for the basic idea behind our Kaoss Emulator. This class will see substantial changes.
 *
 * TODO:
 * This should be an instrument controller and observe trackpad changes.
 * Multiple instrument support
 * User input mode:
 *    -how to select instruments, etc (independent fingers / single finger)
 * 
 * Decide on axis effects (envelope, panning, EQ?)
 */

import com.softsynth.jsyn.*;

public class KaossTest {

    public KaossTest()
    {
        try {
            Synth.startEngine(0);   
        } catch(SynthException e) {
          System.out.println(e);
        }
        Instrument i = new Square();
        InstrumentController ic = new InstrumentController(i);
      //  Instrument s = new SingingSaw();
      //  InstrumentController ic2 = new InstrumentController(s);
    }
    
    public static void main(String[] args) {
        KaossTest k = new KaossTest();
        System.out.println("CTRL-C to exit.");
        try { while(true) {Thread.sleep(5000); }    } catch (Exception e) {}
    }
}

