
 

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;

import com.softsynth.jsyn.*;
import com.softsynth.jsyn.view102.SynthScope;

/**
 * General Purpose Digital Oscilloscope
 *
 * @author (C) 1997 Phil Burk, All Rights Reserved
 */

public class Oscilloscope extends JFrame 
{
	SynthContext         lineIn;
	SynthScope     scope;

	private static final int SURFACE_WIDTH = 800;
	private static final int SURFACE_HEIGHT = 600;

  /* Can be run as either an application or as an applet. */
    public Oscilloscope(SynthContext lineIn)
	{
    this.lineIn = lineIn;

		JPanel surface = new JPanel();
		surface.setPreferredSize(new Dimension(SURFACE_WIDTH, SURFACE_HEIGHT));
		this.setContentPane(surface);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		this.setTitle("kaoss");
		this.setVisible(true);


	}

/*
 * Setup synthesis.
 */
	public void start()
	{
		setLayout( new BorderLayout() );
				
/* Create an oscilloscope to show Line Input. */
		scope = new SynthScope();
		scope.createProbe( lineIn, 0, "Left", Color.red );
		scope.createProbe( lineIn, 1, "Right", Color.blue );
		scope.finish();
		scope.getWaveDisplay().setBackground( Color.white );
		scope.getWaveDisplay().setForeground( Color.black );
		add( "Center", scope );

/* Synchronize Java display. */
		getParent().validate();
		getToolkit().sync();

	}

	public void stop()
	{
		removeAll();
	}
}



