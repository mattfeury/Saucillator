package com.mattfeury.saucillator.macbook;

/*
 * This is our main display for the Saucillator.
 *
 * It contains visual display for the current state of the instruments, 
 * fingers, and a scope.
 *
 * This class is also a keyboard listener so it sends messages back to the Sauce
 * to change instruments, settings.
 *
 * We took the Fingers class from the SwingTest demo that came with the
 * multitouch API. It's pretty sweet.
 * 
 * @author theChillwavves
 */
 
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

/*
 * Creates the GUI
 */
public class SauceDisplay extends JFrame implements KeyListener {  
	
  public static final int SURFACE_WIDTH = 800;
	public static final int SURFACE_HEIGHT = 600;
	public static final int UPDATE_RATE = 30;  // number of update per second
	public static final long UPDATE_PERIOD = 1000L / UPDATE_RATE;  // milliseconds
	
	private Fingers fingers;
	private SurfaceCanvas surface;
  private SynthScope scope;
  private SynthMixer mixer;
  private JPanel content, container, controls, instruments, knobs, extraControls;
  private Saucillator kaoss; //to control audio 
  private Color bgColor = Color.BLACK;
  private Color fgText = Saucillator.lightBrownTest;
	private Color instrumText = Saucillator.darkBrownTest;
	private Color instrumSelText = Saucillator.lightBrownTest;
	private Color knobText = Color.WHITE;
	private Color effectText = Saucillator.darkBrownTest;
	private Color effectSelText = Saucillator.lightBrownTest;
	private Color scaleText = Saucillator.darkBrownTest;
	private Color scaleSelText = Saucillator.lightBrownTest;
  private Font headerFont = new Font("Helvetica", Font.BOLD, 24);
  private Font instrumFont = new Font("Helvetica", Font.BOLD, 22);
  private Font knobFont = new Font("Helvetica", Font.BOLD, 14);
  private Font effectFont = new Font("Helvetica", Font.BOLD, 16);
  private Font scaleFont = new Font("Helvetica", Font.BOLD, 16);

	private JLabel header = new JLabel("SAUCILLATOR");
	private JLabel oneLabel = new JLabel("1: Sine");
	private JLabel twoLabel = new JLabel("2: Triangle");
	private JLabel threeLabel = new JLabel("3: Square");
	private JLabel fourLabel = new JLabel("4: Noise");
	private JLabel fiveLabel = new JLabel("5: Sawtooth");
	private JLabel sixLabel = new JLabel("6: Singing Saw");
	private JLabel sevenLabel = new JLabel("7: Cuomo");
	private JLabel eightLabel = new JLabel("8: Messier");
	private JLabel nineLabel = new JLabel("9: Gong");
	private JLabel zeroLabel = new JLabel("10: Squoise");

	private JLabel lowpassLabel = new JLabel("Low-Pass Filter");
	private JLabel pitchLabel = new JLabel("Pitch");
	private JLabel panLabel = new JLabel("Pan");
	private JLabel depthLabel = new JLabel("Mod Depth");
	private JLabel rateLabel = new JLabel("Mod Rate");

	private JLabel delayLabel = new JLabel("D: Delay");
	private JLabel reverbLabel = new JLabel("R: Reverb");

	private JLabel majorLabel = new JLabel("M: Major");
	private JLabel minorLabel = new JLabel("N: Minor");
	private JLabel chromaticLabel = new JLabel("C: Chromatic");
	private JLabel bluesLabel = new JLabel("B: Blues");

	private DKnob loKnob, pitchKnob, depthKnob, rateKnob, panKnob;

	private int keyCounter;
	
	private JLabel[] instrumLabels = new JLabel[]{oneLabel, twoLabel, threeLabel, fourLabel, fiveLabel, sixLabel, sevenLabel, eightLabel, nineLabel, zeroLabel};
  private JLabel[] knobLabels = new JLabel[]{lowpassLabel, pitchLabel, panLabel, depthLabel, rateLabel};
  private JLabel[] effectLabels = new JLabel[]{delayLabel, reverbLabel};
  private JLabel[] scaleLabels = new JLabel[]{majorLabel, minorLabel, chromaticLabel, bluesLabel};
  
  private String[] keys = new String[]{"A", "A#/Bb", "B", "C", "C#/Db", "D", "D#/Eb", "E", "F", "F#,Gb", "G", "G#/Gb"};
  private JLabel keyLabel = new JLabel("Up/Down Key: ");
  private JLabel key = new JLabel(keys[0]);
  
  private boolean fxEnabled = false;
  private boolean reverbEnabled = false;
  private static boolean trailsEnabled = false;
  
  /*
	 * Create the panels.
   * Java layouts make me cry.
	 */
	public SauceDisplay(Saucillator kaoss, SynthScope scope) {
    this.kaoss = kaoss;
    this.setFocusable(true);   // Allow this panel to get focus.
    this.addKeyListener(this); // listen to our own key events.

    //panels
    container = new JPanel(); //holds all
	  container.setPreferredSize(new Dimension(SURFACE_WIDTH + 250, SURFACE_HEIGHT+450));
    container.setLayout(new BorderLayout());

    content = new JPanel(); //holds scope, fingers, effect labels and scale labels
	  content.setPreferredSize(new Dimension(SURFACE_WIDTH, SURFACE_HEIGHT+450));
    content.setLayout(new BorderLayout());
    
    extraControls = new JPanel();
    extraControls.setPreferredSize(new Dimension(0, 50));
    extraControls.setLayout(new BoxLayout(extraControls, BoxLayout.X_AXIS));
    extraControls.setBackground( bgColor );
    
    makeControls();

	  surface = new SurfaceCanvas(); //holds fingers. inside content
	  surface.setPreferredSize(new Dimension(SURFACE_WIDTH, SURFACE_HEIGHT));
    surface.setBackground( bgColor );

    content.add(surface, BorderLayout.CENTER);
    newScope(scope);
    
    container.add(content, BorderLayout.CENTER);
    container.add(controls, BorderLayout.WEST);
    content.add(extraControls, BorderLayout.NORTH);

    this.setContentPane(container);
	  this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	  this.pack();
	  this.setTitle("kaoss");
	  this.setVisible(true);

    surfaceStart();
	}
  /*
	 * Set up the scope display
	 */
  public void setupScope()
  {
    scope.getWaveDisplay().setBackground( bgColor );
    scope.getWaveDisplay().setForeground( bgColor );

    scope.hideControls();
    scope.setPreferredSize(new Dimension(SURFACE_WIDTH, 200)); //this is a pretty strange number
  }
  /*
	 * Create new scope display for each instrument
	 */
  public void newScope(SynthScope scope)
  {
    if(this.scope != null)
      content.remove(this.scope);

    this.scope = scope;
    setupScope();
    content.add(this.scope, BorderLayout.SOUTH);
    //content.add(extraControls, BorderLayout.SOUTH);
    content.validate();
  }
  /*
	 * Set up left sidebar labels
	 */
  public void makeControls()
  {
    controls = new JPanel();
    instruments = new JPanel();
    controls.add(instruments);
     
	  controls.setPreferredSize(new Dimension(250, SURFACE_HEIGHT+400));
    controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
    controls.setBackground( bgColor );
    
    instruments.setPreferredSize(new Dimension(250, 350));  
    instruments.setLayout(new GridLayout(0,1));
    instruments.setBackground( bgColor );

    //do header separate    
    header.setFont(headerFont);
    header.setForeground(fgText);
    header.setAlignmentX(Component.LEFT_ALIGNMENT);
    instruments.add(header);

    instruments.add(Box.createVerticalGlue());

    //control labels
    int i = 1;
    for(JLabel label : instrumLabels)
    {
      label.setFont(instrumFont);

      if(Saucillator.INSTRUMENT_DEFAULT == i)
        label.setForeground(instrumSelText);
      else
        label.setForeground(instrumText);
      
      label.setAlignmentX(Component.LEFT_ALIGNMENT);
      instruments.add(label);
	    instruments.add(Box.createVerticalGlue());
      i++;
    }

    //knobs and knob labels
  	knobs = new JPanel();
	  knobs.setPreferredSize(new Dimension(250, 300));
    knobs.setLayout(new GridLayout(0,2));
    knobs.setBackground( bgColor );
  	controls.add(knobs);
  	
  	for(JLabel label : knobLabels)
    {
      label.setFont(knobFont);
      label.setForeground(knobText);
      label.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

  	knobs.add(loKnob = new DKnob());
  	knobs.add(lowpassLabel);
	  knobs.add(pitchKnob = new DKnob());
	  knobs.add(pitchLabel);
	  knobs.add(panKnob = new DKnob());
	  knobs.add(panLabel);
	  knobs.add(depthKnob = new DKnob());
	  knobs.add(depthLabel);
	  knobs.add(rateKnob = new DKnob());
	  knobs.add(rateLabel);
	  //Labels for the scales
	  for(JLabel label : scaleLabels)
	  {
	    label.setFont(scaleFont);
	    label.setForeground(scaleText);
	    label.setAlignmentX(Component.LEFT_ALIGNMENT);
	    extraControls.add(label);
	    extraControls.add(Box.createHorizontalGlue());
	  }
	  //Labels for reverb and delay
	  for(JLabel label : effectLabels)
	  {
	    label.setFont(effectFont);
	    label.setForeground(effectText);
	    label.setAlignmentX(Component.LEFT_ALIGNMENT);
	    extraControls.add(label);
	    extraControls.add(Box.createHorizontalGlue());
	  }
	  // Key label
	  key.setFont(effectFont);
    key.setForeground(effectText);
    keyLabel.setFont(effectFont);
    keyLabel.setForeground(effectText);
    extraControls.add(keyLabel);
	  extraControls.add(key);
	  extraControls.add(Box.createHorizontalGlue());
	  
	  // Set defaults extra labels
    minorLabel.setForeground(scaleSelText);
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
          requestFocus();
          requestFocusInWindow();
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
	/*
	 * Update labels on the controls
	 */
  public void updateControls(int id)
  {
	  for(JLabel label : instrumLabels) label.setForeground(instrumText);
	  if (id > 0) instrumLabels[id - 1].setForeground(instrumSelText);
	  else instrumLabels[instrumLabels.length-1].setForeground(instrumSelText); //Special case - 0 instrument comes last
  }
  
  public void updateLoKnob(int lowpass)
  {
  	loKnob.setValue((float)lowpass/(float)Saucillator.LOWPASS_MAX);
  }
  
  public void updatePitchKnob(int y)
  {
  	pitchKnob.setValue((float)y/(float)Saucillator.TRACKPAD_GRID_SIZE);
  }
  
  public void updatePanKnob(double pan)
  {
    panKnob.setValue((float)(pan + 1.0) / 2.0f);
  }
  
  public void updateDepthKnob(int depth)
  {
  	depthKnob.setValue((float)depth/(float)Saucillator.MOD_DEPTH_MAX);
  }
  
  public void updateRateKnob(int rate)
  {
  	rateKnob.setValue((float)rate/(float)Saucillator.MOD_RATE_MAX);
  } 

  /*
   * Use this to change freq or whatnot, handle input
   */
  public void keyPressed(KeyEvent e) 
  { 
    int code = e.getKeyCode();
    switch(code)
    {
      case KeyEvent.VK_N:
        kaoss.changeScale(Instrument.minorScale);
        for(JLabel label : scaleLabels) label.setForeground(scaleText);
        minorLabel.setForeground(scaleSelText);
        break;
      case KeyEvent.VK_M:
        kaoss.changeScale(Instrument.majorScale);
        for(JLabel label : scaleLabels) label.setForeground(scaleText);
        majorLabel.setForeground(scaleSelText);
        break;
      case KeyEvent.VK_C:
        kaoss.changeScale(Instrument.chromaticScale);
        for(JLabel label : scaleLabels) label.setForeground(scaleText);
        chromaticLabel.setForeground(scaleSelText);
        break;
      case KeyEvent.VK_B:
        kaoss.changeScale(Instrument.minorBluesScale);
        for(JLabel label : scaleLabels) label.setForeground(scaleText);
        bluesLabel.setForeground(scaleSelText);
        break;
      case KeyEvent.VK_D:
        kaoss.toggleDelay();
        fxEnabled = !fxEnabled;
        if(fxEnabled) delayLabel.setForeground(effectSelText);
        else delayLabel.setForeground(effectText);
        break;
      case KeyEvent.VK_R:
        kaoss.toggleReverb();
        reverbEnabled = !reverbEnabled;
        if (reverbEnabled) reverbLabel.setForeground(effectSelText);
        else reverbLabel.setForeground(effectText);
        break;
      case KeyEvent.VK_S:
        kaoss.sauceBoss();
        break;
      case KeyEvent.VK_UP:
        kaoss.changePitch(1);
        keyCounter++;
        //Change remainder operator into actual modulus
        key.setText(keys[((keyCounter % 12) + 12) % 12]);
        break;
      case KeyEvent.VK_DOWN:
        kaoss.changePitch(-1);
        keyCounter--;
        key.setText(keys[((keyCounter % 12) + 12) % 12]);
        break;
      default:
        int id = Character.getNumericValue(e.getKeyChar());
        if(id > -1 && id < instrumLabels.length) {
          kaoss.changeInstrument(id);
      	  updateControls(id);
        }
    }
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

class Fingers {
	private static final int MAX_FINGER_BLOBS = 12;
	
	private int width, height;
	Finger blobs[] = new Finger[MAX_FINGER_BLOBS];
	
  public Fingers(int width, int height) {	
		this.width = width;
		this.height=height;
	}

  public void updateFinger(Finger f) {
		int id = f.getID();
		if (id <= MAX_FINGER_BLOBS)
			blobs[id-1]= f;

  }

  /*
	 * Draw blobs for each finger. Create a trail for each blob.
	 */   
	public void draw(Graphics g) {
	   ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	   for (int i=0; i<MAX_FINGER_BLOBS;i++) {
		   Finger blob = blobs[i];
       if (blob != null && blob.getState() == FingerState.PRESSED) {
       
         int x     = (int) (width  * (blob.getX()));
         int y     = (int) (height * (1-blob.getY()));
         int xsize = (int) (10*blob.getSize() * (blob.getMajorAxis()/2));
         int ysize = (int) (10*blob.getSize() * (blob.getMinorAxis()/2));
         int ang   = blob.getAngle();

         //Scale x and y to go from 0 to 255 and make new color
         if (x > 0 && y > 0 && x < SauceDisplay.SURFACE_WIDTH && y < SauceDisplay.SURFACE_HEIGHT) {
           Color pitchcolor = new Color((int)Math.floor((x/(float)SauceDisplay.SURFACE_WIDTH)*255), 255 - (int)Math.floor((y/(float)SauceDisplay.SURFACE_HEIGHT)*255), 0);
           g.setColor(pitchcolor);
          
           Ellipse2D ellipse = new Ellipse2D.Float(0,0, xsize, ysize);
         
           AffineTransform at = AffineTransform.getTranslateInstance(0,0);			   
           at.translate(x-xsize/2, y-ysize/2);
           at.rotate((Math.PI/180)*-ang, xsize/2, ysize/2);  // convert degrees to radians
         
           ((Graphics2D) g).fill(at.createTransformedShape(ellipse));
         
           g.setColor(Color.DARK_GRAY);
         }
      }
	  }
  }
}

