//
// Controls an instrument based on trackpad movement.
// This should probably be modified to control multiple instruments

import java.util.*;

import com.alderstone.multitouch.mac.touchpad.TouchpadObservable;
import com.alderstone.multitouch.mac.touchpad.Finger;
import com.alderstone.multitouch.mac.touchpad.FingerState;

import vavi.sensor.accelerometer.Accelerometer;
import vavi.sensor.accelerometer.macbook.MacbookAccelerometer;

import com.softsynth.jsyn.*;

public class InstrumentController implements Observer {

    // Class that is resposible for registering with the Trackpad
    // and notifies registered clients of Touchpad Multitouch Events
    TouchpadObservable tpo;    
    MacbookAccelerometer acc;
    Instrument instrument;

    public InstrumentController(Instrument i, MacbookAccelerometer acc)
    {
        //multitouch
        tpo = TouchpadObservable.getInstance();
        tpo.addObserver(this);
        this.instrument = i;
        this.acc = acc;
        i.start();
    }
    
    // Touchpad Multitouch update event handler, called on single MT Finger event
    public void update( Observable obj, Object arg ) {
        
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
        
        //accel?
        int sense = acc.sense();
        int aX = acc.getX();
        int aY = acc.getY();
        int aZ = acc.getZ();

        //        int aY 
        System.out.println(sense + " , " + aX + " , " + aY + " , " + aZ);


        //mark on / off
        if(state.equals(FingerState.PRESSED) && !instrument.isPlaying()) //finger pressed. start playback
            instrument.start();
        if(state.equals(FingerState.RELEASED) && instrument.isPlaying()) { //finger lifted. stop playback
            instrument.stop();

            //autoswitch instruments on release. this is for debugging purposes mostly
            //also it doesn't work
          /*  Random generator = new Random();
            switch(generator.nextInt(4))
            {
              case 0:
                instrument = new SingingSaw();
                System.out.println("singing saw");
                break;
              case 1:
                instrument = new Sawtooth();
                System.out.println("sawtooth");               
                break;
              case 2:
                instrument = new Square();
                System.out.println("square");                         
                break;
              default:
                instrument = new Triangle();
                System.out.println("triangle");
                break;
            }*/
            return;

        }
        

        int inc = (int)((y * 100) / (25/3)); //inc is value from 0-12 inclusive
        instrument.adjustFrequencyByOffset(inc);
        

/*        System.out.println( "frame="+frame + 
                           "\ttimestamp=" + timestamp + 
                           "\tid=" +  id + 
                           "\tstate=" + state +
                           "\tsize=" + size  +
                           "\tx,y=(" + x+ "," +  y+ 
                           ")\tdx,dy=(" + dx + "," + dy +")\t" +
                           "angle=" + angle  + 
                           "majAxis=" + majorAxis  +
                           "\tminAxis=" + minorAxis); */
    }   
}

