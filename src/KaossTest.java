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
import java.awt.MouseInfo;

public class KaossTest {

    public KaossTest()
    {
        MacbookAccelerometer acc = new MacbookAccelerometer();
        try {
            Synth.startEngine(0);  
        } catch(Exception e) {
          System.out.println(e);
        }

        System.out.println(System.getProperty("os.name"));
        //scope monitor
        Instrument i = new Sine();
        InstrumentController ic = new InstrumentController(i, acc);

        //finger monitor
        new SwingTest(i.getScope());
    }
    
    public static void main(String[] args) {
        KaossTest k = new KaossTest();
        

        

        System.out.println("CTRL-C to exit.");
			  try { while(true) {
          Thread.sleep(100);
          System.out.println("("+MouseInfo.getPointerInfo().getLocation().x+", "+MouseInfo.getPointerInfo().getLocation().y+")"); 
        }    } catch (Exception e) {}
    }
}

