package com.mattfeury.saucillator.macbook;

import java.util.*;
import com.softsynth.jsyn.*;

/*
 * Simple sawtooth
 * @author Matt Feury
 */
public class Sawtooth extends BasicJSynInstrument {

  public Sawtooth() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InstantiationException
  {
    super(SawtoothOscillatorDPW.class, noHarmonics);
  }  
}
