import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
	
	private static BufferedReader br_ip;
	private static BufferedReader br_hostname;

	public static void main(String[] args) throws InterruptedException, IOException{ 	
		//is_alive("pc_names.txt");
		create_and_copy("pc_names.txt");
	}
	
	
	public static void create_and_copy(String path_text_file) throws IOException, InterruptedException{
		ArrayList<String> cmd = new ArrayList<String>(); 
		ArrayList<String> cmd1 = new ArrayList<String>(); 
		
		br_ip = new BufferedReader(new FileReader(path_text_file));		
		
		String line;
		while ((line = br_ip.readLine()) != null) {
			System.out.println("Connection:" + line + "..");
			cmd.clear();
			cmd1.clear();
			cmd.add("ssh");
			cmd.add("tailleur@" + line + ".enst.fr");
			cmd.add("mkdir");
			cmd.add("-p");
			cmd.add("/tmp/tailleur");
			cmd1.add("scp");
			cmd1.add("-r");
			cmd1.add("-p");
			cmd1.add("/tmp/tailleur/slave.jar");
			cmd1.add("tailleur@" + line + ":/tmp/tailleur/slave.jar");
			My_Process proc = new My_Process(2, cmd);
			proc.launch_process();
			My_Process proc1 = new My_Process(2, cmd1);
			proc1.launch_process();
		}
	}
	
	public static void is_alive(String path_text_file) throws IOException, InterruptedException{
		ArrayList<String> cmd = new ArrayList<String>(); 
		br_hostname = new BufferedReader(new FileReader(path_text_file));
		String line;
		
		while ((line = br_hostname.readLine()) != null) {
			cmd.clear();
			cmd.add("ssh");
			cmd.add("tailleur@" + line + ".enst.fr");
			cmd.add("hostname");
			My_Process proc = new My_Process(2, cmd);
			proc.launch_process();
		}
	}

}
