import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Random;



public class Main {
	
	private static BufferedReader br_ip;
	private static BufferedReader br_hostname;
	private static Scanner input_spec;

	public static void main(String[] args) throws InterruptedException, IOException{ 	
		//is_alive("pc_names.txt");
		//create_and_copy_splits("pc_names.txt");
		/*for(int i = 0; i < 1; i = i + 1) {
	         String path = "/tmp/tailleur/splits/S" + i + ".txt";
	         deploy_map(path, "pc_names.txt");
	     }*/
		ArrayList<String> available_computers = get_available_computers("pc_names.txt");
		ArrayList<String> my_n_available = get_n_available_computers(3, available_computers);
		System.out.println("Available:" + my_n_available);
		deploy_each_split(my_n_available);
		create_and_copy_slave("available_pc.txt");
		TreeMap<String,String> dic_um_machines = get_dic_um_machines(my_n_available);
		Map<String, ArrayList<String>> dic_key_um = get_key_dic(my_n_available);
		shu(my_n_available, dic_key_um, dic_um_machines);
		
	}
	
	public static void shu(ArrayList<String> available_computers, Map<String, ArrayList<String>> dic_key_um, TreeMap<String,String> dic_um_machines) throws IOException, InterruptedException {
		System.out.println("Doing Shuffle");
		ArrayList<String> cmd = new ArrayList<String>();
		TreeMap<String,Integer> dic_key_comp = new TreeMap<String,Integer>();
		Random rand = new Random();
		System.out.println(dic_key_um);
		for ( String um_txt : dic_key_um.keySet() ) {
			System.out.println(um_txt);
			int  n = rand.nextInt(2) + 0;
			dic_key_comp.put(um_txt, n); // n correspond au numéro de l'ordinateur auquel le mot sera affecté
			for (int j=0 ; j<dic_key_um.get(um_txt).size(); j++) {
				System.out.println(dic_key_um.get(um_txt).get(j));
				System.out.println("Connection:" + available_computers.get(n) + "..");
				cmd.clear();
				cmd.add("scp");
				cmd.add("-r");
				cmd.add("-p");
				//" scp -r -p /tmp/tailleur/local.txt tailleur@c130-28:/tmp/tailleur/local.txt"
				cmd.add("tailleur@" + dic_um_machines.get(dic_key_um.get(um_txt).get(j)) + ":/tmp/tailleur/maps/" + dic_key_um.get(um_txt).get(j));
				// tailleur@c130-28:/tmp/tailleur/local.txt"
				cmd.add("tailleur@" + dic_um_machines.get(dic_key_um.get(um_txt).get(n)) + ":/tmp/tailleur/maps/");
				System.out.println(cmd);
				My_Process proc = new My_Process(2, cmd);
				proc.launch_process();/*
				System.out.println("UM" + j + " - " + available_computers.get(j));
				String um_name = "UM" + j;*/
			}
		}
		
	}
	
	
	public static TreeMap<String,String> get_dic_um_machines(ArrayList<String> available_computers) throws IOException, InterruptedException{
		System.out.println("Doing Map");
		ArrayList<String> cmd = new ArrayList<String>(); 
		TreeMap<String,String> dic_um_machines = new TreeMap<String,String>();
		for ( int j=0; j<available_computers.size(); j++ ) {
			System.out.println("Connection:" + available_computers.get(j) + "..");
			cmd.clear();
			cmd.add("ssh");
			cmd.add("tailleur@" + available_computers.get(j));
			cmd.add("java");
			cmd.add("-jar");
			cmd.add("/tmp/tailleur/slave.jar");
			cmd.add("0");
			cmd.add("/tmp/tailleur/splits/S" + j + ".txt");
			My_Process proc = new My_Process(2, cmd);
			proc.launch_process();
			System.out.println("UM" + j + " - " + available_computers.get(j));
			String um_name = "UM" + j + ".txt";
			dic_um_machines.put(um_name, available_computers.get(j));
		}
		System.out.println(dic_um_machines);
		return dic_um_machines;
	}
	
	public static Map<String, ArrayList<String>> get_key_dic(ArrayList<String> available_computers) throws IOException, InterruptedException {
		Map<String, ArrayList<String>> dic_key_um = new HashMap<String, ArrayList<String>>();
		ArrayList<String> cmd = new ArrayList<String>(); 
		for ( int j=0; j<available_computers.size(); j++ ) {
			System.out.println("Connection:" + available_computers.get(j) + "..");
			cmd.clear();
			cmd.add("ssh");
			cmd.add("tailleur@" + available_computers.get(j));
			cmd.add("java");
			cmd.add("-jar");
			cmd.add("/tmp/tailleur/slave.jar");
			cmd.add("0");
			cmd.add("/tmp/tailleur/splits/S" + j + ".txt");
			My_Process proc = new My_Process(2, cmd);
			proc.launch_process();
			String k = proc.get_standard();
	        input_spec = new Scanner(k);
	        while (input_spec.hasNext()) {
	        	  String next = input_spec.next().toLowerCase();
	        	  if (!dic_key_um.containsKey(next)) {
	            	  ArrayList<String> ar = new ArrayList<String>();
	            	  //ar.add(available_computers.get(j)); // Computer ip instead of um name
	            	  ar.add("UM" + j + ".txt");
	            	  dic_key_um.put(next, ar);
	            } else {
	            	//dic_key_um.get(next).add(available_computers.get(j)); // Computer ip instead of um name
	            	dic_key_um.get(next).add("UM" + j + ".txt");
	            }
	        }
	    }
		System.out.println(dic_key_um);
		System.out.println("Phase de map terminée");
		return dic_key_um;
		
	}
	
	public static void deploy_each_split(ArrayList<String> my_n_available) throws IOException, InterruptedException {
		System.out.println("Deploy Splits in all available computers");
		for(int i = 0; i < 3; i = i + 1) {
	         String path = "/tmp/tailleur/splits/S" + i + ".txt";
	         deploy_splits(path, my_n_available);
	     }
	}
	
	
	public static void deploy_splits(String path_text_file, ArrayList<String> available_computers) throws IOException, InterruptedException{
		ArrayList<String> cmd = new ArrayList<String>(); 
		ArrayList<String> cmd1 = new ArrayList<String>();		
		for ( int j=0; j<available_computers.size(); j++ ) {
			System.out.println("Connection:" + available_computers.get(j) + "..");
			cmd.clear();
			cmd1.clear();
			cmd.add("ssh");
			cmd.add("tailleur@" + available_computers.get(j) + ".enst.fr");
			cmd.add("mkdir");
			cmd.add("-p");
			cmd.add("/tmp/tailleur/splits");
			cmd1.add("scp");
			cmd1.add("-r");
			cmd1.add("-p");
			cmd1.add(path_text_file);
			cmd1.add("tailleur@" + available_computers.get(j) + ":/tmp/tailleur/splits/");
			My_Process proc = new My_Process(2, cmd);
			proc.launch_process();
			My_Process proc1 = new My_Process(2, cmd1);
			proc1.launch_process();
		}
	}
	
	public static ArrayList<String> get_n_available_computers(int n, ArrayList<String> my_available_computers) throws FileNotFoundException, UnsupportedEncodingException{
		System.out.println("Get " + n + " available computers");
		ArrayList<String> n_available = new ArrayList<String>();
		PrintWriter writer = new PrintWriter("available_pc.txt", "UTF-8");
		for(int i = 0; i < n; i++) {
			n_available.add(my_available_computers.get(i));
			writer.println(my_available_computers.get(i));
		}
		writer.close();
		return n_available;
	}
	
	public static ArrayList<String> get_available_computers(String file) throws IOException, InterruptedException {
		System.out.println("Get all available computers");
		ArrayList<String> cmd = new ArrayList<String>();
		ArrayList<String> available_computers = new ArrayList<String>();
		br_ip = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br_ip.readLine()) != null) {
			System.out.println("Connection:" + line + "..");
			cmd.clear();
			cmd.add("ssh");
			cmd.add("-o StrictHostKeyChecking=no");
			cmd.add("tailleur@" + line + ".enst.fr");
			cmd.add("echo 'available'");
			My_Process proc = new My_Process(2, cmd);
			proc.launch_process();
			// Si il est pas dispo obligatoire sinon on a une erreur
			try { 
              	if (proc.get_standard().equals("available")) {
    				  available_computers.add(line);
    			     }
			} catch (java.lang.NullPointerException e) {
				System.out.println(line + " not available");
			}
		}
		
		return available_computers;
	}
	
	
	public static void create_and_copy_splits(String path_text_file, String path_ip_pc_file) throws IOException, InterruptedException{
		ArrayList<String> cmd = new ArrayList<String>(); 
		ArrayList<String> cmd1 = new ArrayList<String>();
		
		br_ip = new BufferedReader(new FileReader(path_ip_pc_file));
		String line;
		while ((line = br_ip.readLine()) != null) {
			System.out.println("Connection:" + line + "..");
			cmd.clear();
			cmd1.clear();
			cmd.add("ssh");
			cmd.add("tailleur@" + line + ".enst.fr");
			cmd.add("mkdir");
			cmd.add("-p");
			cmd.add("/tmp/tailleur/splits");
			cmd1.add("scp");
			cmd1.add("-r");
			cmd1.add("-p");
			cmd1.add(path_text_file);
			cmd1.add("tailleur@" + line + ":/tmp/tailleur/splits/");
			My_Process proc = new My_Process(2, cmd);
			proc.launch_process();
			My_Process proc1 = new My_Process(2, cmd1);
			proc1.launch_process();
		}
	}
	
	
	public static void create_and_copy_slave(String path_text_file) throws IOException, InterruptedException{
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
