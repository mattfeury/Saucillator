
 

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;

import com.softsynth.jsyn.*;
import com.softsynth.jsyn.view102.WaveDisplay;
import com.softsynth.jsyn.view102.SynthScope;

/**
 * General Purpose Digital Oscilloscope
 *
 * @author (C) 1997 Phil Burk, All Rights Reserved
 */

public class Oscilloscope extends JFrame 
{
  LineOut unitOut;
	SynthScope     scope;

	private static final int SURFACE_WIDTH = 800;
	private static final int SURFACE_HEIGHT = 600;

  /* Can be run as either an application or as an applet. */
    public Oscilloscope(LineOut unitOut)
	{
    this.unitOut = unitOut;

		JPanel surface = new JPanel();
		surface.setPreferredSize(new Dimension(SURFACE_WIDTH, SURFACE_HEIGHT));
		this.setContentPane(surface);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		this.setTitle("oscillo");
		this.setVisible(true);


	}

  public WaveDisplay getWaveDisplay()
  {
    return (scope != null) ? scope.getWaveDisplay() : null;
  }

/*
 * Setup synthesis.
 */
	public void start()
	{
		setLayout( new BorderLayout() );
				
/* Create an oscilloscope to show Line Input. */
		



/* Synchronize Java display. */
		getParent().validate();
		getToolkit().sync();

	}

	public void stop()
	{
		removeAll();
	}
}



