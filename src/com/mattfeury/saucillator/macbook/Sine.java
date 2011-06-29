package com.mattfeury.saucillator.macbook;

/*
 * Sine instrument class. Creates a basic sine wave.
 *
 * @author theChillwavves  
 * 
 */

import java.util.*;
import com.softsynth.jsyn.*;

public class Sine extends BasicJSynInstrument {

  public Sine() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InstantiationException
  {
    super(SineOscillator.class, noHarmonics);
  }  
}
