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
import java.awt.Color;

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
    private boolean useMultitouch = false;
    private boolean DISPLAY = true;
    private boolean CONTROLLER_PENDING = false;

    //most of these are sucky. but we are using some for now
    public static Color darkBrownTest = new Color(166, 65, 8);
    public static Color lightBrownTest = new Color(242, 204, 133);
    public static Color darkGreenTest = new Color(37,89,59);
    public static Color lightGreenTest = new Color(100, 126, 41);
    public static Color brownTest = new Color(223,167,73);

//    public static SynthContext context = new SynthContext(); //we should prob implement this

    public KaossTest()
    {

      //start synth & instruments
      try {
          Synth.requestVersion( 144 );
          Synth.startEngine(0);  
          System.out.println("go");
          SynthObject.enableDeletionByGarbageCollector(true); //formerly static
      } catch(Exception e) {
        System.out.println(e);
      }
 
      if(System.getProperty("os.name").toLowerCase().contains("mac"))
        useMultitouch = true;

      
      Instrument i = new SingingSaw();

      //start input devices based on support
      if(useMultitouch) {
        tpo = TouchpadObservable.getInstance();
        acc = new MacbookAccelerometer();    

        fingersPressed = new LinkedList<Integer>();

        tpo.addObserver(this);
          changeController(i);      
      } else {
        mouseObs = new MouseObservable();
      
        Thread thread = new MouseObserverThread(mouseObs);
        thread.start(); 

        changeController(i);
        mouseObs.addObserver(this); //start observing
      }
      //start display
      if(DISPLAY)
        display = new SwingTest(this, controller.getScope());
      
    }

    public InstrumentController changeController(Instrument i)
    {
      CONTROLLER_PENDING = true;
      if(controller != null) controller.kill();
      SynthObject.deleteAll();

      i.makeLFOs(true);
      InstrumentController newController = new InstrumentController(i);
      newController.start();

      if(! fingersPressed.isEmpty())
        newController.startInstrument();

      controller = newController;
      CONTROLLER_PENDING = false;      
      return controller;
    }

    public void changeInstrument(Instrument i)
    {
      controller.changeInstrument(i);
    }

    // Touchpad Multitouch update event handler, called on single MT Finger event
    public void update( Observable obj, Object arg ) {

      if(CONTROLLER_PENDING) return;

      //check macbook?
      if(useMultitouch)
      {
        Finger f = (Finger) arg;
        updateViaFinger(f);
      } else {
        //degrade to mouse position on screen. this sucks but it's a quick and stable alternative
        updateViaMouse((Dimension)arg);
      } 

    }

    public void updateViaFinger(Finger f)
    {
      //update display
      if(DISPLAY && display != null)
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
      if(! fingersPressed.contains(id)) { //finger pressed. 
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
      
      updateViaAccelerometer();

      //boolean fingerIsController = fingersPressed.getFirst().equals(id);
      //if(! fingerIsController) return; //only use control finger for points

      int whichFinger = fingersPressed.indexOf(id) + 1;
      //System.out.println(whichFinger);
      //depends on nth finger
      switch(whichFinger)
      {
        case 0:
          return;
        case 1:
          updateLowpass((int)(x * 2000));
          updateFrequency((int)(y * TRACKPAD_GRID_SIZE));        
          break;
        case 2:
          updateModRate((int)(x*20));
          updateModDepth((int)(y*1000)); //this is cool except it is rarely zero
          break;
        //default:
          

      }

    }

    public void updateViaMouse(Dimension mouse)
    {
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

      if(mouse.height == 0 && mouse.width == 0 && controller.isPlaying()) { //corner, turn off
        controller.stop();
        return;
      } 
      if(! controller.isPlaying()) {
        controller.startInstrument();
      }

      float x = (float)mouse.width / screen.width;
      float y = ((float)screen.height - mouse.height) / screen.height;
      //update display
      if(DISPLAY)
      {
        //int frame, double timestamp, int id, int state, float size,   float x, float y, float dx, float dy, float angle, float majorAxis, float minorAxis
        Finger f = new Finger(0,0, 1, 4, 1.5f, x, y, 0.0f, 0.0f, 3.14f / 2, 9.0f, 9.0f);
        display.updateFinger(f);

      }

      updateFrequency((int)(y * TRACKPAD_GRID_SIZE));
      updateModRate((int)(x * 20));

    }

    public void updateViaAccelerometer()
    {

      int sense = acc.sense();
      int aX = acc.getX();
      int aY = acc.getY();
      int aZ = acc.getZ();

      //pan by accelerometer
      updatePan((double)(acc.getX() % 100) / 100);
    }
    

    public void updatePan(double pan)
    {
      //controller.pan(pan);
    }

    public void updateFrequency(int y)
    {
      controller.changeFrequency(y);
    } 

    public void updateLowpass(int lowpass)
    {
      controller.lowpass(lowpass);
    }  

    public void updateModRate(int rate)
    {
      controller.updateModRate(rate);
    } 

    public void updateModDepth(int depth)
    {
      controller.updateModDepth(depth);
    }
    
    /*
     * Returns a value from 0 - max inclusive based on a percentage (float from 0.0 - 1.0)
     *
     * this is a stupid method. you just multiple the numbers. oy vay. don't use this.
     */
    public int scaleToRange(float scale, double max)
    {
      int scaled = (int)((scale * max)); 
      return scaled;
    }
    
    public static void main(String[] args) {
        KaossTest k = new KaossTest();   

        System.out.println("CTRL-C to exit.");
    }
}

