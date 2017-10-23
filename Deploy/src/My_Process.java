import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class My_Process {
	ArrayList<String>my_cmd;
	ArrayBlockingQueue<String> standard_output;
	ArrayBlockingQueue<String> error_output;
	int time_seconds;
	ProcessBuilder pb; 
	Process proc;
	Job error_Job;
	Job standard_Job;
	Thread error_reader;
	Thread standard_reader;
		
	public My_Process(int time_seconds, ArrayList<String>cmd) {
		standard_output = new ArrayBlockingQueue<String>(200);
		error_output = new ArrayBlockingQueue<String>(200);
		this.time_seconds = time_seconds;
		this.pb = new ProcessBuilder().command(cmd);
		this.my_cmd = cmd;
	}
	
	public String get_cmd(ArrayList<String> cmd) {
		String str_cmd = "";
		for (String d : cmd)
		{
			str_cmd += d + " ";
		}
		return str_cmd;
	}
	
	public void init_threads() {
		this.error_Job = new Job(this.proc.getErrorStream(), "ERROR", this.error_output, get_cmd(this.my_cmd));
		this.standard_Job = new Job(this.proc.getInputStream(), "OUTPUT", this.standard_output, get_cmd(this.my_cmd));
		this.standard_reader = new Thread(this.standard_Job);
		this.error_reader = new Thread(this.error_Job);
	}
	
	
	public void init_process() throws IOException {
		this.proc = this.pb.start();
		init_threads();
		this.standard_reader.start();
		this.error_reader.start();
	}
	
	public void launch_process() throws IOException, InterruptedException {
		init_process();
		
		String s_output = this.standard_Job.get_abqueue().poll(this.time_seconds, TimeUnit.SECONDS);
		String e_output = this.error_Job.get_abqueue().poll(this.time_seconds, TimeUnit.SECONDS);
		
		if (s_output != "OFF" && s_output != null){
			System.out.println(s_output);
		} else {
			this.standard_reader.interrupt();
			this.proc.destroy();
		}
		
		if (e_output != "OFF" && e_output != null){
			System.out.println(e_output);
		} else {
			this.error_reader.interrupt();
			this.proc.destroy();
		}
	}
	
	
}


