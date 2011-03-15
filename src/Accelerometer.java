/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sensor.accelerometer;


/**
 * Accelerometer. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080715 nsano initial version <br>
 */
public interface Accelerometer {

    /** */
    int sense();

    /** */
    int getX();

    /** */
    int getY();

    /** */
    int getZ();
}

/* */
