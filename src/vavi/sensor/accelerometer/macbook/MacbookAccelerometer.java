/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sensor.accelerometer.macbook;

import vavi.sensor.accelerometer.Accelerometer;


/**
 * MacbookAccelerometer. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2009/08/24 nsano initial version <br>
 */
public class MacbookAccelerometer implements Accelerometer {

    /** */
    private native int init();
    
    /** */
    public native int sense();

    /** */
    private native void destroy();
    
    public MacbookAccelerometer() {
        int r = init();
        switch (r) {
        case -1:
            throw new IllegalStateException("IOServiceGetMatchingServices returned error.");
        case -2:
            throw new IllegalStateException("No motion sensor available");
        case -3:
            throw new IllegalStateException("Could not open motion sensor device");
        case -4:
            throw new IllegalStateException("no coords");
        }
    }

    protected void finalize() throws Throwable {
        destroy();
    }

    public native int getX();

    public native int getY();

    public native int getZ();

    static {
        try {
            System.loadLibrary("SmsWrapper");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

/* */
