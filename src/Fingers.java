/*

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;


class Fingers implements Observer {
	private static final int MAX_FINGER_BLOBS = 20;
	
	private int width, height;
	TouchpadObservable tpo;
	Finger blobs[] = new Finger[MAX_FINGER_BLOBS];
	
    public Fingers(int width, int height) {	
		this.width = width;
		this.height=height;
		tpo = TouchpadObservable.getInstance();
		tpo.addObserver(this);
	}

	// Multitouch update event 
	public void update( Observable obj, Object arg ) {
		
		// The event 'arg' is of type: com.alderstone.multitouch.mac.touchpad.Finger
		Finger f = (Finger) arg;
		int id = f.getID();
		if (id <= MAX_FINGER_BLOBS)
			blobs[id-1]= f;
	}	

	public void update() {	   
	}
 
	public void draw(Graphics g) {
	   
	   ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	   for (int i=0; i<MAX_FINGER_BLOBS;i++) {
		   Finger f = blobs[i];
		   if (f != null && f.getState() == FingerState.PRESSED) {
			   
			   int x     = (int) (width  * (f.getX()));
			   int y     = (int) (height * (1-f.getY()));
			   int xsize = (int) (10*f.getSize() * (f.getMajorAxis()/2));
			   int ysize = (int) (10*f.getSize() * (f.getMinorAxis()/2));
			   int ang   = f.getAngle();

			   Ellipse2D ellipse = new Ellipse2D.Float(0,0, xsize, ysize);
			   
			   AffineTransform at = AffineTransform.getTranslateInstance(0,0);			   
			   at.translate(x-xsize/2, y-ysize/2);
			   at.rotate((Math.PI/180)*-ang, xsize/2, ysize/2);  // convert degrees to radians
			   
			   g.setColor(Color.PINK);
			   ((Graphics2D) g).fill(at.createTransformedShape(ellipse));
			   
			   g.setColor(Color.DARK_GRAY);
			   g.drawString("" + i, x,y);
		   }
	   }
	}
}
*/
