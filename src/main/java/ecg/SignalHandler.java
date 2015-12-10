package ecg;


import java.util.concurrent.BlockingQueue;

public class SignalHandler implements Runnable {

    private volatile boolean running = true;
    public void terminate() {
        running = false;
    }

    private BlockingQueue<Double> mQueue;

    public SignalHandler(BlockingQueue<Double> queue) {
        mQueue = queue;
    }

    @Override
    public void run() {
        while (running) {
            if (mQueue.isEmpty())
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            else {
                try {
                    Double d = mQueue.take();
                    System.out.println("SIGNAL HANDLER: " + String.valueOf(d));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
