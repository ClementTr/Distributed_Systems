import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Random;


public class Main {
	
	private static BufferedReader br_ip;
	private static Scanner input_spec;

	public static void main(String[] args) throws InterruptedException, IOException{ 	
		// GET AVAILABLE COMPUTERS //
		int nb_pc_desired = 3;
		ArrayList<String> available_computers = get_available_computers("pc_names.txt", nb_pc_desired);
		System.out.println("Available:" + available_computers);
		// DEPLOY SPLITS //
		deploy_splits(available_computers, nb_pc_desired);
		// DEPLOY SLAVES //
		deploy_slaves("available_pc.txt");
		// GET NECESSARY DICTIONARIES //
		TreeMap<String,String> dic_um_machines = get_dic_um_machines(available_computers);
		Map<String, ArrayList<String>> dic_words_um = get_dic_word_ums(available_computers);
		// SHUFFLE //
		Map<String, String> dic_word_machine = shuffle(available_computers, dic_words_um, dic_um_machines);
		// REDUCE //
		ArrayList<String> reduce_machines = reduce(dic_word_machine, dic_words_um, available_computers);
		System.out.println("REDUCE MACHINE: " + reduce_machines);
		// RECOVER ALL REDUCES //
		recover_reduce(reduce_machines);
	}
	
	public static void recover_reduce(ArrayList<String> available_computers) throws IOException, InterruptedException {
		System.out.println("Recover reduces");
		ArrayList<String> cmd = new ArrayList<String>(); 
		ArrayList<String> cmd1 = new ArrayList<String>(); 
		for ( int j=1; j<available_computers.size(); j++ ) {
			cmd.clear();
			cmd.add("scp");
			cmd.add("-r");
			cmd.add("-p");
			cmd.add("tailleur@" + available_computers.get(j) + ":/tmp/tailleur/reduces/RM.txt");
			cmd.add("tailleur@" + available_computers.get(0) + ":/tmp/tailleur/reduces/RM" + j + ".txt");
			My_Process proc = new My_Process(2, cmd);
			proc.launch_process();
		}
		cmd1.clear();
		cmd1.add("ssh");
		cmd1.add("tailleur@" + available_computers.get(0));
		cmd1.add("mv");
		cmd1.add("/tmp/tailleur/reduces/RM.txt");
		cmd1.add("/tmp/tailleur/reduces/RM0.txt");
		My_Process proc1 = new My_Process(2, cmd1);
		proc1.launch_process();
		System.out.println("Reduces goes to pc: " + available_computers.get(0));
	}
	
	
	public static ArrayList<String> reduce(Map<String, String> dic_word_machine, Map<String, ArrayList<String>> dic_words_um, ArrayList<String> available_computers) throws IOException, InterruptedException {
		System.out.println("Reduce");
		ArrayList<String> cmd = new ArrayList<String>(); 
		ArrayList<String> reduce_machines = new ArrayList<String>();
		Map<String, Integer> cpt_by_pc = new HashMap<String, Integer>();
		for ( int j=0; j<available_computers.size(); j++ ) {
			cpt_by_pc.put(available_computers.get(j), 0);
		}
		for (String word_txt : dic_word_machine.keySet()) {
			cmd.clear();
			cmd.add("ssh");
			cmd.add("tailleur@" + dic_word_machine.get(word_txt));
			cmd.add("java");
			cmd.add("-jar");
			cmd.add("/tmp/tailleur/slave.jar");
			cmd.add("1");
			cmd.add(word_txt);
			String cpt_string = Integer.toString(cpt_by_pc.get(dic_word_machine.get(word_txt)));
			cmd.add(cpt_string);
			for (int k=0; k < dic_words_um.get(word_txt).size(); k++) {
				cmd.add("/tmp/tailleur/maps/" + dic_words_um.get(word_txt).get(k));
			}
			System.out.println(cmd);
			My_Process proc = new My_Process(2, cmd);
			proc.launch_process();
			cpt_by_pc.put(dic_word_machine.get(word_txt), cpt_by_pc.get(dic_word_machine.get(word_txt)) + 1);
			if (!reduce_machines.contains(dic_word_machine.get(word_txt))) {
				reduce_machines.add(dic_word_machine.get(word_txt));
	        }
		}
		return reduce_machines;
	}
	
	
	public static Map<String, String> shuffle(ArrayList<String> available_computers, Map<String, ArrayList<String>> dic_words_um, TreeMap<String,String> dic_um_machines) throws IOException, InterruptedException {
		System.out.println("Doing Shuffle");
		Map<String, String> dic_word_machine = new HashMap<String, String>();
		ArrayList<String> cmd1 = new ArrayList<String>();
		Random rand = new Random();
		for ( String word_txt : dic_words_um.keySet() ) {
			int  n = rand.nextInt(2) + 0;
			for (int j=0 ; j<dic_words_um.get(word_txt).size(); j++) {
				cmd1.clear();
				cmd1.add("scp");
				cmd1.add("-r");
				cmd1.add("-p");
				cmd1.add("tailleur@" + dic_um_machines.get(dic_words_um.get(word_txt).get(j)) + ":/tmp/tailleur/maps/" + dic_words_um.get(word_txt).get(j));
				cmd1.add("tailleur@" + dic_um_machines.get(dic_words_um.get(word_txt).get(n)) + ":/tmp/tailleur/maps/");
				if (j==0) {
					System.out.println(word_txt + " goes to " + dic_um_machines.get(dic_words_um.get(word_txt).get(n)));
					dic_word_machine.put(word_txt, dic_um_machines.get(dic_words_um.get(word_txt).get(n)));
				}
				My_Process proc = new My_Process(2, cmd1);
				proc.launch_process();
			}
		}
		return dic_word_machine;
	}
	
	
	public static Map<String, ArrayList<String>> get_dic_word_ums(ArrayList<String> available_computers) throws IOException, InterruptedException {
		Map<String, ArrayList<String>> dic_word_um = new HashMap<String, ArrayList<String>>();
		ArrayList<String> cmd = new ArrayList<String>(); 
		for ( int j=0; j<available_computers.size(); j++ ) {
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
	        	  if (!dic_word_um.containsKey(next)) {
	            	  ArrayList<String> ar = new ArrayList<String>();
	            	  ar.add("UM" + j + ".txt");
	            	  dic_word_um.put(next, ar);
	           } else {
	        	   dic_word_um.get(next).add("UM" + j + ".txt");
	           }
	        }
	    }
		System.out.println(dic_word_um);
		System.out.println("Phase de map terminée");
		return dic_word_um;
		
	}
	
	
	public static TreeMap<String,String> get_dic_um_machines(ArrayList<String> available_computers) throws IOException, InterruptedException{
		System.out.println("Doing Map");
		ArrayList<String> cmd = new ArrayList<String>(); 
		TreeMap<String,String> dic_um_machines = new TreeMap<String,String>();
		for ( int j=0; j<available_computers.size(); j++ ) {
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
	
	
	public static void deploy_slaves(String path_text_file) throws IOException, InterruptedException{
		System.out.println("Create and deploy slaves");
		ArrayList<String> cmd = new ArrayList<String>(); 
		ArrayList<String> cmd1 = new ArrayList<String>(); 
		br_ip = new BufferedReader(new FileReader(path_text_file));		
		String line;
		while ((line = br_ip.readLine()) != null) {
			cmd.clear();
			cmd.add("ssh");
			cmd.add("tailleur@" + line + ".enst.fr");
			cmd.add("mkdir");
			cmd.add("-p");
			cmd.add("/tmp/tailleur");
			cmd1.clear();
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
	
	
	public static void deploy_splits(ArrayList<String> available_computers, int numb_pc_desired) throws IOException, InterruptedException{
		System.out.println("Deploy Splits in all available computers");
		ArrayList<String> cmd = new ArrayList<String>(); 
		ArrayList<String> cmd1 = new ArrayList<String>();		
		for(int i = 0; i < numb_pc_desired; i = i + 1) {
			for ( int j=0; j<available_computers.size(); j++ ) {
				String path_text_file = "/tmp/tailleur/splits/S" + i + ".txt";
				cmd.clear();
				cmd.add("ssh");
				cmd.add("tailleur@" + available_computers.get(j) + ".enst.fr");
				cmd.add("mkdir");
				cmd.add("-p");
				cmd.add("/tmp/tailleur/splits");
				cmd1.clear();
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
	}

	
	public static ArrayList<String> get_available_computers(String file, int numb_pc_desired) throws IOException, InterruptedException {
		System.out.println("Get "+ numb_pc_desired +" available computers..");
		PrintWriter writer = new PrintWriter("available_pc.txt", "UTF-8");
		ArrayList<String> available_computers = new ArrayList<String>();
		br_ip = new BufferedReader(new FileReader(file));
		ArrayList<String> cmd = new ArrayList<String>();
		String line;
		int cpt = 0;
		while ((line = br_ip.readLine()) != null & cpt < numb_pc_desired) {
			System.out.println("Connection: " + line + "..");
			cmd.clear();
			cmd.add("ssh");
			cmd.add("-o StrictHostKeyChecking=no");
			cmd.add("tailleur@" + line + ".enst.fr");
			cmd.add("echo 'available'");
			My_Process proc = new My_Process(2, cmd);
			proc.launch_process();
			try { 
              	if (proc.get_standard().equals("available")) {
    				  available_computers.add(line);
    				  writer.println(line);
    				  cpt += 1;
    			     }
			} catch (java.lang.NullPointerException e) {
				System.out.println(line + " not available");
			}
		}
		writer.close();
		if (cpt < numb_pc_desired) {
			System.out.println("! Not enough computers available !");
		}
		return available_computers;
	}
	
}
