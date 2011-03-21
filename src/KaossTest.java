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

public class KaossTest implements Observer {

    // Class that is resposible for registering with the Trackpad
    // and notifies registered clients of Touchpad Multitouch Events
    private TouchpadObservable tpo;    
    private MouseObservable mouseObs;

    // Tracks how many fingers are pressed and what value
    private LinkedList<Integer> fingersPressed;

    private MacbookAccelerometer acc;

    private InstrumentController controller;
    private SwingTest display;

    //GLOBALS
    public static int TRACKPAD_GRID_SIZE = 12;
    private final boolean useMultitouch = true;
    private final boolean DISPLAY = true;

    public KaossTest()
    {

      //start synth & instruments
      try {
          Synth.requestVersion( 144 );
          Synth.startEngine(0);  
      } catch(Exception e) {
        System.out.println(e);
      }

      controller = new InstrumentController(new Square());
      //TRACKPAD_GRID_SIZE = 100; //for singing saw only

      //start display
      if(DISPLAY)
        display = new SwingTest(controller.getScope());
      
      //start input devices based on support
      if(useMultitouch) {
        tpo = TouchpadObservable.getInstance();
        acc = new MacbookAccelerometer();    

        fingersPressed = new LinkedList<Integer>();

        tpo.addObserver(this);  
        controller.start();      
      } else {
        mouseObs = new MouseObservable();
      
        Thread thread = new MouseObserverThread(mouseObs);
        thread.start(); 

        controller.start();
        mouseObs.addObserver(this); //start observing
      }
      
      System.out.println(System.getProperty("os.name"));
      
    }

    // Touchpad Multitouch update event handler, called on single MT Finger event
    public void update( Observable obj, Object arg ) {

      //check macbook?
      if(useMultitouch)
      {
       
        // The event 'arg' is of type: com.alderstone.multitouch.mac.touchpad.Finger
        Finger f = (Finger) arg;

        
        //accel?
        int sense = acc.sense();
        int aX = acc.getX();
        int aY = acc.getY();
        int aZ = acc.getZ();
       //  System.out.println(sense + " , " + aX + " , " + aY + " , " + aZ);
        //controller.pan((double)(acc.getX() % 100) / 100);

        //update display
        if(DISPLAY)
          display.updateFinger(f);
 
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
            controller.startInstrument();

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
          int yProper = scaleToRange(y,TRACKPAD_GRID_SIZE);        
          int lowpass = scaleToRange(x,2000);

          controller.changeFrequency(yProper);
          controller.lowpass(lowpass);
          //System.out.println(lowpass + "   " + x);
        }
      } else {
        //degrade to mouse position on screen. this sucks but it's a quick and stable alternative
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension mouse = (Dimension)arg;

        if(mouse.height == 0 && mouse.width == 0 && controller.isPlaying()) { //corner, turn off
          controller.stop();
          return;
        } 
        if(! controller.isPlaying()) {
          controller.start();
        }
      

        float yPercentageFromBottom = ((float)screen.height - mouse.height) / screen.height; //value from 0-1 of the y position. bottom is 0, 1 is top.
        int yProper = scaleToRange(yPercentageFromBottom, TRACKPAD_GRID_SIZE);
        controller.changeFrequency(yProper);

        
      } 

    }   
    
    /*
     * Returns a value from 0 - max inclusive based on a percentage (float from 0.0 - 1.0)
     */
    public int scaleToRange(float scale, double max)
    {
      int scaled = (int)((scale * 100) / (100 / max)); 
      return scaled;
    }
    
    public static void main(String[] args) {
        KaossTest k = new KaossTest();   

        System.out.println("CTRL-C to exit.");
    }
}

