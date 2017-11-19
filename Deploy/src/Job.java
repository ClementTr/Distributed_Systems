import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ArrayBlockingQueue;

class Job implements Runnable {
	String my_cmd;
    InputStream is;
    ArrayBlockingQueue <String> ab_queue;
    String type;
    
    Job(InputStream is, String type, ArrayBlockingQueue<String> ab_queue, String cmd) {
        this.is = is;
        this.type = type;
        this.ab_queue = ab_queue;
        this.my_cmd = cmd;
    }

    
    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
            	  System.out.println(type + " > " + line);
            	  ab_queue.put(line);
            }
            ab_queue.put("OFF");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException e) {
			System.out.println(type + "> Timout: "+ this.my_cmd);
		}
        
    }
    
    public ArrayBlockingQueue<String> get_abqueue(){
    	    return this.ab_queue;
    }
}
