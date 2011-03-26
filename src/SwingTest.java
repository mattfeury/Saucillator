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
import java.awt.event.*;


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
		   	if (blob != null && blob.getState() == FingerState.PRESSED) {
			   
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


public class SwingTest extends JFrame implements KeyListener {  
	
	private static final int SURFACE_WIDTH = 800;
	private static final int SURFACE_HEIGHT = 600;
	private static final int UPDATE_RATE = 30;  // number of update per second
	private static final long UPDATE_PERIOD = 1000L / UPDATE_RATE;  // milliseconds
	
	private Fingers fingers;
	private SurfaceCanvas surface;
  	private SynthScope scope;
  	private SynthMixer mixer;
  	private JPanel content, container, controls;
  	private KaossTest kaoss; //to control audio cause this class may need to be a keyboard listener
  	private Color bgColor = Color.BLACK;
  	private Color fgText = KaossTest.lightGreenTest;
	private Color instrumText = KaossTest.darkBrownTest;
	private Color instrumSelText = KaossTest.lightBrownTest;
  	private Font headerFont = new Font("Helvetica", Font.BOLD, 26);

	private JLabel header = new JLabel("SAUCILLATOR");
	private JLabel oneLabel = new JLabel("1 Sine");
	private JLabel twoLabel = new JLabel("2 Triangle");
	private JLabel threeLabel = new JLabel("3 Square");
	private JLabel fourLabel = new JLabel("4 Noise");
	private JLabel fiveLabel = new JLabel("5 Sawtooth");
	private JLabel sixLabel = new JLabel("6 Singing Saw");
	private JLabel sevenLabel = new JLabel("7 Cuomo");
	private JLabel eightLabel = new JLabel("8 Messier");
	private JLabel nineLabel = new JLabel("9 Gong");

	public SwingTest(KaossTest kaoss, SynthScope scope) {
    this.kaoss = kaoss;
    this.setFocusable(true);   // Allow this panel to get focus.
    this.addKeyListener(this); // listen to our own key events.

    //panels
    container = new JPanel(); //holds all
	container.setPreferredSize(new Dimension(SURFACE_WIDTH + 200, SURFACE_HEIGHT+400));
    container.setLayout(new BorderLayout());

    makeControls();

    content = new JPanel(); //holds scope, fingers
	content.setPreferredSize(new Dimension(SURFACE_WIDTH, SURFACE_HEIGHT+400));
    content.setLayout(new BorderLayout());

	surface = new SurfaceCanvas(); //holds fingers. inside content
	surface.setPreferredSize(new Dimension(SURFACE_WIDTH, SURFACE_HEIGHT));
    surface.setBackground( bgColor );

    content.add(surface, BorderLayout.CENTER);
    newScope(scope);
    
    container.add(content, BorderLayout.CENTER);
    container.add(controls, BorderLayout.WEST);

    this.setContentPane(container);
	this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	this.pack();
	this.setTitle("kaoss");
	this.setVisible(true);



	surfaceStart();
	}

  public void setupScope()
  {
    scope.getWaveDisplay().setBackground( bgColor );
    scope.getWaveDisplay().setForeground( bgColor );

    scope.hideControls();
    scope.setPreferredSize(new Dimension(SURFACE_WIDTH, 250)); //this is a pretty strange number
  }

  public void newScope(SynthScope scope)
  {
    if(this.scope != null)
      content.remove(this.scope);

    this.scope = scope;
    setupScope();
    content.add(this.scope, BorderLayout.SOUTH);
    content.validate();
  }

  public void makeControls()
  {
    controls = new JPanel(); //future sidebar?
	controls.setPreferredSize(new Dimension(200, SURFACE_HEIGHT+400));
    controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
    controls.setBackground( bgColor );

    header.setFont(headerFont);
    header.setForeground(fgText);
    header.setAlignmentX(Component.CENTER_ALIGNMENT);
    controls.add(header);

    oneLabel.setFont(headerFont);
    oneLabel.setForeground(instrumText);
    oneLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    controls.add(oneLabel);
	
    twoLabel.setFont(headerFont);
    twoLabel.setForeground(instrumText);
    twoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    controls.add(twoLabel);

    threeLabel.setFont(headerFont);
    threeLabel.setForeground(instrumText);
    threeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    controls.add(threeLabel);

    fourLabel.setFont(headerFont);
    fourLabel.setForeground(instrumText);
    fourLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    controls.add(fourLabel);

    fiveLabel.setFont(headerFont);
    fiveLabel.setForeground(instrumSelText);
    fiveLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    controls.add(fiveLabel);

	sixLabel.setFont(headerFont);
    sixLabel.setForeground(instrumText);
    sixLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    controls.add(sixLabel);

	sevenLabel.setFont(headerFont);
    sevenLabel.setForeground(instrumText);
    sevenLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    controls.add(sevenLabel);
	
	eightLabel.setFont(headerFont);
    eightLabel.setForeground(instrumText);
    eightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    controls.add(eightLabel);

	nineLabel.setFont(headerFont);
    nineLabel.setForeground(instrumText);
    nineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    controls.add(nineLabel);
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
	
  public void updateControls(int id)
 {
	oneLabel.setForeground(instrumText);
	twoLabel.setForeground(instrumText);
	threeLabel.setForeground(instrumText);
	fourLabel.setForeground(instrumText);
	fiveLabel.setForeground(instrumText);
	sixLabel.setForeground(instrumText);
	sevenLabel.setForeground(instrumText);
	eightLabel.setForeground(instrumText);
	nineLabel.setForeground(instrumText);
	
 	if (id == 1) oneLabel.setForeground(instrumSelText);
	else if (id == 2) twoLabel.setForeground(instrumSelText);
	else if (id == 3) threeLabel.setForeground(instrumSelText);
	else if (id == 4) fourLabel.setForeground(instrumSelText);
	else if (id == 5) fiveLabel.setForeground(instrumSelText);
	else if (id == 6) sixLabel.setForeground(instrumSelText);
	else if (id == 7) sevenLabel.setForeground(instrumSelText);
	else if (id == 8) eightLabel.setForeground(instrumSelText);
	else if (id == 9) nineLabel.setForeground(instrumSelText);
	else fiveLabel.setForeground(instrumSelText);
 }

  public int getInstrumentIdFromChar(char c)
  {
    if(c == Character.forDigit(KaossTest.INSTRUMENT_SINE,10) )
      return KaossTest.INSTRUMENT_SINE;
    else if(c == Character.forDigit(KaossTest.INSTRUMENT_TRIANGLE,10) )
      return KaossTest.INSTRUMENT_TRIANGLE;
    else if(c == Character.forDigit(KaossTest.INSTRUMENT_SQUARE,10) )
      return KaossTest.INSTRUMENT_SQUARE;
    else if(c == Character.forDigit(KaossTest.INSTRUMENT_SINGINGSAW,10) )
      return KaossTest.INSTRUMENT_SINGINGSAW;
    else if(c == Character.forDigit(KaossTest.INSTRUMENT_GONG,10) )
      return KaossTest.INSTRUMENT_GONG;
  	else if(c == Character.forDigit(KaossTest.INSTRUMENT_REDNOISE,10) )
    	return KaossTest.INSTRUMENT_REDNOISE;
    else if(c == Character.forDigit(KaossTest.INSTRUMENT_SAWTOOTH,10) )
      return KaossTest.INSTRUMENT_SAWTOOTH;
	  else if(c == Character.forDigit(KaossTest.INSTRUMENT_CUOMO,10) )
    	return KaossTest.INSTRUMENT_CUOMO;
	  else if(c == Character.forDigit(KaossTest.INSTRUMENT_MESSIER,10) )
    	return KaossTest.INSTRUMENT_MESSIER;
    else
      return KaossTest.INSTRUMENT_CUOMO;

  }

  /*
   * Use this to change freq or whatnot
   */
  public void keyPressed(KeyEvent e) 
  { 
    System.out.println("pre change: "+Synth.getObjectCount()); 
    char c = e.getKeyChar();
    int id = getInstrumentIdFromChar(c);
    switch(c)
    {
      case 'n':
        kaoss.changeScale(Instrument.minorScale);
        break;
      case 'm':
        kaoss.changeScale(Instrument.majorScale);
        break;
      case 'c':
        kaoss.changeScale(Instrument.chromaticScale);
        break;
      default:
        kaoss.changeInstrument(id);
    	updateControls(id);
    }

    System.out.println("post change: "+Synth.getObjectCount()); 
  }
    

  public void keyReleased(KeyEvent e) {}
  public void keyTyped(KeyEvent e)   {}
  
	
	public void surfaceUpdate() {

	}
	
	class SurfaceCanvas extends JPanel {
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			setBackground(bgColor);
			fingers.draw(g);
		}
	}

	
	
}

