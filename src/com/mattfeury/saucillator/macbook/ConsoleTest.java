package com.mattfeury.saucillator.macbook;

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
//  ConsoleTest.java
//
//  Created by Wayne Keenan on 30/05/2009.
//
//  wayne.keenan@gmail.com
//
//  Class to test the Mac Mulittouch API via a console app.

 

import java.util.Observer;
import java.util.Observable;
import com.alderstone.multitouch.mac.touchpad.TouchpadObservable;
import com.alderstone.multitouch.mac.touchpad.Finger;
import com.alderstone.multitouch.mac.touchpad.FingerState;

public class ConsoleTest implements Observer {

	// Class that is resposible for registering with the Trackpad
	// and notifies registered clients of Touchpad Multitouch Events
	TouchpadObservable tpo;

	// Touchpad Multitouch update event handler, called on single MT Finger event
	
	public void update( Observable obj, Object arg ) {
		
		// The event 'arg' is of type: com.alderstone.multitouch.mac.touchpad.Finger
		Finger f = (Finger) arg;
		
		int		frame = f.getFrame();
		double	timestamp = f.getTimestamp();
		int		id = f. getID(); 
		FingerState		state = f.getState();
		float	size = f.getSize();
		float	angRad = f.getAngleInRadians();
		int		angle = f.getAngle();			// return in Degrees
		float	majorAxis = f.getMajorAxis();
		float	minorAxis = f.getMinorAxis();
		float	x = f.getX();
		float	y = f.getY();
		float	dx = f.getXVelocity();
		float	dy = f.getYVelocity();
		
		System.out.println( "frame="+frame + 
						   "\ttimestamp=" + timestamp + 
						   "\tid=" +  id + 
						   "\tstate=" + state +
						   "\tsize=" + size  +
						   "\tx,y=(" + x+ "," +  y+ 
						   ")\tdx,dy=(" + dx + "," + dy +")\t" +
						   "angle=" + angle  + 
						   "majAxis=" + majorAxis  +
						   "\tminAxis=" + minorAxis);
	}	

	public void run() {
		tpo = TouchpadObservable.getInstance();
		tpo.addObserver(this);
	}
	
	public static void main(String[] args) {
		
		ConsoleTest ct = new ConsoleTest();
		ct.run();
		System.out.println("CTRL-C to exit.");
		try { while(true) {Thread.sleep(5000); }	} catch (Exception e) {}
	}
}

