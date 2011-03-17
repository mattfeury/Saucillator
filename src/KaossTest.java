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

import java.util.*;
import java.awt.Dimension;
import java.awt.Toolkit;

import com.alderstone.multitouch.mac.touchpad.TouchpadObservable;
import com.alderstone.multitouch.mac.touchpad.Finger;
import com.alderstone.multitouch.mac.touchpad.FingerState;

import vavi.sensor.accelerometer.Accelerometer;
import vavi.sensor.accelerometer.macbook.MacbookAccelerometer;
import javax.swing.SwingUtilities;
import java.awt.MouseInfo;

public class KaossTest implements Observer {

    // Class that is resposible for registering with the Trackpad
    // and notifies registered clients of Touchpad Multitouch Events
    private TouchpadObservable tpo;    

    // Tracks how many fingers are pressed and what value
    private LinkedList<Integer> fingersPressed;

    private MacbookAccelerometer acc;

    private InstrumentController controller;

    //GLOBALS
    public static final int TRACKPAD_GRID_SIZE = 12;
    private final boolean useMultitouch = true;

    public KaossTest()
    {

        tpo = TouchpadObservable.getInstance();
        tpo.addObserver(this);
        fingersPressed = new LinkedList<Integer>();
        
        acc = new MacbookAccelerometer();
        try {
            Synth.startEngine(0);  
        } catch(Exception e) {
          System.out.println(e);
        }

        System.out.println(System.getProperty("os.name"));
        //scope monitor
        controller = new InstrumentController(new Sawtooth());

        //finger monitor
        new SwingTest(controller.getScope());
    }

    // Touchpad Multitouch update event handler, called on single MT Finger event
    public void update( Observable obj, Object arg ) {
         
      //accel?
      int sense = acc.sense();
      int aX = acc.getX();
      int aY = acc.getY();
      int aZ = acc.getZ();

      //        int aY 
     // System.out.println(sense + " , " + aX + " , " + aY + " , " + aZ);

      //check macbook?
      if(useMultitouch)
      {
       
        // The event 'arg' is of type: com.alderstone.multitouch.mac.touchpad.Finger
        Finger f = (Finger) arg;
 
        int     frame = f.getFrame();
        double  timestamp = f.getTimestamp();
        int     id = f. getID(); 
        FingerState     state = f.getState();
        float   size = f.getSize();
        float   angRad = f.getAngleInRadians();
        int     angle = f.getAngle();           // return in Degrees
        float   majorAxis = f.getMajorAxis();
        float   minorAxis = f.getMinorAxis();
        float   x = f.getX();
        float   y = f.getY();
        float   dx = f.getXVelocity();
        float   dy = f.getYVelocity();        

        //mark on / off 
        if(!fingersPressed.contains(id)) { //finger pressed. 
          //we were not tracking this finger. so let's add it to the queue.          
          System.out.println("now tracking: "+id);
          if(fingersPressed.isEmpty())
            controller.start();

          fingersPressed.add((Integer)id);
        }
        if(state.equals(FingerState.RELEASED) && controller.isPlaying() && fingersPressed.contains(id)) { //finger lifted
          System.out.println("finger lifted: "+id);
          fingersPressed.remove((Integer)id);
        }

        if(fingersPressed.isEmpty()) {
          System.out.println("no more fingers");
          controller.stop(); //stop
          return;
        }
        
        boolean fingerIsController = fingersPressed.getFirst().equals(id);
        if(fingerIsController) {
          int yProper = (int)((y * 100) / (100 / TRACKPAD_GRID_SIZE)); //inc is value from 0-12 inclusive        
          controller.changeFrequency(yProper);
        }
      } else {
        //degrade to mouse position on screen. this sucks but it's quick and stable
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//        MouseInfo.getPointerInfo().getLocation().x;
  //        MouseInfo.getPointerInfo().getLocation().y;


      } 

    }   
    
    
    public static void main(String[] args) {
        KaossTest k = new KaossTest();   

        System.out.println("CTRL-C to exit.");
			  try { while(true) {
          Thread.sleep(5000);
        }    } catch (Exception e) {}
    }
}

