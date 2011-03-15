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
import vavi.sensor.accelerometer.Accelerometer;
import vavi.sensor.accelerometer.macbook.MacbookAccelerometer;
import javax.swing.SwingUtilities;
public class KaossTest {

    public KaossTest()
    {
        MacbookAccelerometer acc = new MacbookAccelerometer();
        try {
            Synth.startEngine(0);  
        } catch(Exception e) {
          System.out.println(e);
        }

        
        //scope monitor
        Instrument i = new Sawtooth();
        InstrumentController ic = new InstrumentController(i, acc);

        //finger monitor
        new SwingTest(i.getScope());

        //
      //  Instrument s = new SingingSaw();
      //  InstrumentController ic2 = new InstrumentController(s);
    }
    
    public static void main(String[] args) {
        KaossTest k = new KaossTest();

        

        System.out.println("CTRL-C to exit.");
        try { while(true) {Thread.sleep(5000); }    } catch (Exception e) {}
    }
}

