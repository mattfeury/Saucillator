package com.mattfeury.saucillator.macbook;

/*
 * Triangle instrument class.
 *
 * @author theChillwavves  
 * 
 */

import java.util.*;
import com.softsynth.jsyn.*;

public class Triangle extends BasicJSynInstrument {

  public Triangle() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InstantiationException
  {
    super(TriangleOscillator.class, noHarmonics);
  }  
}
