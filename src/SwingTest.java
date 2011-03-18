//  Copyright 2009 Wayne Keenan
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
//  Created by Wayne Keenan on 30/05/2009.
//
//  wayne.keenan@gmail.com
//

 

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;


import com.alderstone.multitouch.mac.touchpad.*;
import com.softsynth.jsyn.view102.WaveDisplay;
import com.softsynth.jsyn.view102.SynthScope;
import com.softsynth.jsyn.*;


class Fingers {
	private static final int MAX_FINGER_BLOBS = 20;
	
	private int width, height;
	Finger blobs[] = new Finger[MAX_FINGER_BLOBS];
	java.util.List<Finger> bloblist = new java.util.LinkedList<Finger>(); 
	
  public Fingers(int width, int height) {	
		this.width = width;
		this.height=height;
	}

  public void updateFinger(Finger f) {
		int id = f.getID();
		if (id <= MAX_FINGER_BLOBS)
			blobs[id-1]= f;
  }    
 
	public void draw(Graphics g) {
	   ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	   for (int i=0; i<MAX_FINGER_BLOBS;i++) {
		   Finger f = blobs[i];
		   if(f != null) bloblist.add(f);
		   for(Finger blob : bloblist) {
		   	if (f != null && f.getState() == FingerState.PRESSED) {
			   
				   int x     = (int) (width  * (blob.getX()));
				   int y     = (int) (height * (1-blob.getY()));
				   int xsize = (int) (10*blob.getSize() * (blob.getMajorAxis()/2));
				   int ysize = (int) (10*blob.getSize() * (blob.getMinorAxis()/2));
				   int ang   = blob.getAngle();


					Color pitchcolor = new Color(x % 255, y % 255, (x + y) % 255);
				   g.setColor(pitchcolor);
				   Ellipse2D ellipse = new Ellipse2D.Float(0,0, xsize, ysize);
			   
				   AffineTransform at = AffineTransform.getTranslateInstance(0,0);			   
				   at.translate(x-xsize/2, y-ysize/2);
				   at.rotate((Math.PI/180)*-ang, xsize/2, ysize/2);  // convert degrees to radians
			   
				   ((Graphics2D) g).fill(at.createTransformedShape(ellipse));
			   
				   g.setColor(Color.DARK_GRAY);
				   //g.drawString("" + i, x,y);
			   }
	   		}
			if(bloblist.size() > 10) bloblist.remove(0);
	   }
	}
}


public class SwingTest extends JFrame {  
	
	private static final int SURFACE_WIDTH = 800;
	private static final int SURFACE_HEIGHT = 600;
	private static final int UPDATE_RATE = 30;  // number of update per second
	private static final long UPDATE_PERIOD = 1000L / UPDATE_RATE;  // milliseconds
	
	private Fingers fingers;
	private SurfaceCanvas surface;
  private SynthScope scope;
  private SynthMixer mixer;
	
	public SwingTest(SynthScope scope) {
    this.scope = scope;

		surface = new SurfaceCanvas();
		surface.setPreferredSize(new Dimension(SURFACE_WIDTH, SURFACE_HEIGHT));

    Dialog scopeDialog;
  	scopeDialog = new Dialog( (Frame) getParent(), "JSyn Scope", false );
	  scopeDialog.add( "Center", scope );
		scopeDialog.reshape( 200,100, 500,400 );
		scopeDialog.show();
    
    this.setContentPane(surface);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		this.setTitle("kaoss");
		this.setVisible(true);



		surfaceStart();
	}

  public void updateFinger(Finger f)
  {
    fingers.updateFinger(f);
  }
	
	public void surfaceStart() {
		fingers = new Fingers(SURFACE_WIDTH, SURFACE_HEIGHT);
		
		Thread surfaceThread = new Thread() {
			public void run() {
				while (true) {
					surfaceUpdate();
					repaint();
					try {
						Thread.sleep(UPDATE_PERIOD);
					} catch (InterruptedException ex) { }
				}
			}
		};
		surfaceThread.start(); 
	}
	
	public void surfaceUpdate() {
	//	fingers.update();
	}
	
	class SurfaceCanvas extends JPanel {
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			setBackground(Color.WHITE);
			fingers.draw(g);
		}
	}

	
	
}

