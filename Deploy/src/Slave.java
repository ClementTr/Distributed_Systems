import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

public class Slave {
		
	private static Scanner input;
	private static String output;

	public static void main(String[] args) throws InterruptedException, IOException{ 
		String mode = args[0].toString();
		String word = args[1];
		String file_to_map;
		
		if(mode.equals("0")) {
			file_to_map= args[1];
			mode0(file_to_map);
		} else {
			String number_files = args[2];
			file_to_map= args[3];
			ArrayList<String> files_to_split = new ArrayList<String>();
			for ( int j=3; j<args.length; j++ ) {
				files_to_split.add(args[j]);
			}
			mode1(files_to_split, word, number_files);
		}
	}
	
	public static void mode0(String file) throws FileNotFoundException, UnsupportedEncodingException {
		TreeMap<String, Integer> wordCounts = new TreeMap<String, Integer>();
		char int_char = file.charAt(file.length() - 5);
		File repo = new File("/tmp/tailleur/maps/");
		repo.mkdirs();
		PrintWriter writer = new PrintWriter("/tmp/tailleur/maps/UM" + int_char + ".txt", "UTF-8");
		input = new Scanner(new File(file));
		while (input.hasNext()) {
			String next = input.next().toLowerCase();
	         /*if (!wordCounts.containsKey(next)) {
	        	   wordCounts.put(next, 1);
	        	   writer.println(next + " 1");
	         }*/
			wordCounts.put(next, 1);
     	    writer.println(next + " 1");
		}
        writer.close();
        output = "";
        for (String word : wordCounts.keySet()) {
            output += " " + word;
        }
        System.out.println(output);
	}
	
	
	public static void mode1(ArrayList<String> files_to_split, String word, String number_files) throws IOException {
		int cpt = 0;
		int my_number_files = Integer.parseInt(number_files);
		
		File repo = new File("/tmp/tailleur/splits/");
		repo.mkdirs();
		PrintWriter writer = new PrintWriter("/tmp/tailleur/splits/SM" + my_number_files + ".txt", "UTF-8");
		for ( int j=0; j<files_to_split.size(); j++ ) {
			input = new Scanner(new File(files_to_split.get(j)));
			while (input.hasNext()) {
				String next = input.next().toLowerCase();
		         if (next.contains(word)) {
		        	   writer.println(word + " 1");
		        	   cpt += 1;
		         }
			}
		}
        writer.close();
        
        File repo1 = new File("/tmp/tailleur/reduces/");
        if (my_number_files == 0) {
        		repo1.mkdirs();
        		PrintWriter writer1 = new PrintWriter("/tmp/tailleur/reduces/RM.txt", "UTF-8");
        		writer1.println(word + " " + cpt);
            writer1.close();
        } else {
        		File writer1 = new File("/tmp/tailleur/reduces/RM.txt");
        		FileWriter my_writer = new FileWriter(writer1, true);
        		my_writer.write(word + " " + cpt + "\n");
        		my_writer.close();
        }
	}
}
