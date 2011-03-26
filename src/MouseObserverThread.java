
public class MouseObserverThread extends Thread {
    MouseObservable obs;

    public MouseObserverThread(MouseObservable obs)
    {
      this.obs = obs;
    }

    // This method is called when the thread runs    
    public void run() {
      try { while(true) {
		  //System.out.println(obs);
        obs.readMouse();
        Thread.sleep(100);
      }    } catch (Exception e) {
e.printStackTrace();
		}
      
    }
} 
