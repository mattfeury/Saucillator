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

    public final static int INSTRUMENT_SINE = 1;
    public final static int INSTRUMENT_TRIANGLE = 2;
    public final static int INSTRUMENT_SQUARE = 3;
  	public final static int INSTRUMENT_REDNOISE = 4;
	  public final static int INSTRUMENT_SAWTOOTH = 5;
  	public final static int INSTRUMENT_SINGINGSAW = 6;
	  public final static int INSTRUMENT_CUOMO = 7;
  	public final static int INSTRUMENT_MESSIER = 8;
	  public final static int INSTRUMENT_GONG = 9;
	  public final static int INSTRUMENT_SQUOISE = 0;

    public final static int INSTRUMENT_DEFAULT = INSTRUMENT_SINE;
    

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
          SynthObject.enableDeletionByGarbageCollector(true); //formerly static
      } catch(Exception e) {
        System.out.println(e);
      }
 
      if(System.getProperty("os.name").toLowerCase().contains("mac"))
        useMultitouch = true;

      //start input devices based on support
      if(useMultitouch) {
        tpo = TouchpadObservable.getInstance();
        acc = new MacbookAccelerometer();    

        fingersPressed = new LinkedList<Integer>();

        controller = new InstrumentController();
        controller.start();

        tpo.addObserver(this);
      } else {
        mouseObs = new MouseObservable();
      
        Thread thread = new MouseObserverThread(mouseObs);
        thread.start(); 

        controller = new InstrumentController();
        controller.start();

        mouseObs.addObserver(this); //start observing
      }
      //start display
      if(DISPLAY)
        display = new SwingTest(this, controller.getScope());
      
    }

    public void changeInstrument(int id)
    {
      CONTROLLER_PENDING = true;
      controller.changeInstrument(id);
      if(! useMultitouch || ! fingersPressed.isEmpty())
        controller.startInstrument();

      display.newScope(controller.getScope());
      
      CONTROLLER_PENDING = false;      
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

    float lDx = 0, hDx = 0, lDy = 0, hDy = 0;

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

      if(dx < lDx) lDx = dx;
      if(dx > hDx) hDx = dx;
      if(dy < lDy) lDy = dy;
      if(dy > hDy) hDy = dy;

      //System.out.println(lDx + "  ,  "  +hDx + "  / " + lDy + "  " + hDy);      
//-13.950837  ,  17.629349  / -15.644904  20.096266

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

	 	  if(display != null)    
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

    public void changeScale(int[] scale)
    {
      controller.changeScale(scale);
    }
    

    public void updatePan(double pan)
    {
      controller.pan(pan);
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

    public void toggleDelay()
    {
      controller.toggleDelay();
    }

    public void toggleReverb()
    {
      controller.toggleReverb();
    }

    public void sauceBoss()
    {
      controller.iThinkItsTheSauceBoss();
    }
        
    public static void main(String[] args) {
        KaossTest k = new KaossTest();   

        System.out.println("CTRL-C to exit.");
    }
}

