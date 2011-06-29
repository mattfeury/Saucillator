package com.mattfeury.saucillator.macbook;

/*
 * Square instrument class. Uses odd harmonics to create a square wave.
 *
 * @author theChillwavves  
 * 
 */

import java.util.*;
import com.softsynth.jsyn.*;

public class Square extends BasicJSynInstrument {

  public Square() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InstantiationException
  {
    super(SquareOscillatorBL.class, noHarmonics);
  }  
}
