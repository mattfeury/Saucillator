package com.mattfeury.saucillator.macbook;

/**
 * Mouse observable class reads mouse input.
 * 
 * @author theChillwavves
 */

import java.util.Observable;
import java.awt.*;

public class MouseObservable extends Observable
{
   private int y = 0;
   private int x = 0;

   public void readMouse()
   {
      int newY = MouseInfo.getPointerInfo().getLocation().y;
      int newX = MouseInfo.getPointerInfo().getLocation().x;
      if(newY != y || newX != x) {
        x = newX;
        y = newY;
        setChanged();
        notifyObservers(new Dimension(x,y));
      }
   }
}
