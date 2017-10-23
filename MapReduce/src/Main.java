import java.util.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.Files.readAllLines;

public class Main {
    public static String[] stop_words = {"↬", "à", ":", "-", "le", "la", "de", "des", "et", "les",
            "ou", "ne", "que", "qui", ";", "a", "ii", "°", "au", "aux", "du", "en", "où", "se", "sil",
            "y", "v", "sy", "en", "il", "est", "°", "", "”", "”v", "€", "", "r", "r-", "un", "une",
            "pour", "pas", "par", "l", "l-", "ce", "ces", "ainsi", "sa", "dans", "avec", "son", "sont",
            "sur", "leur", "si", "ses", "sous", "iii", "ier", "elle", "tout", "toute"};

    public static void main(String[] args) throws FileNotFoundException {

        // All Work
        /*long startTime_allWork = System.currentTimeMillis();
        //all_work_with_specific_text("data/CC-MAIN-20170322212949-00140-ip-10-233-31-227.ec2.internal.warc.wet");
        all_work_with_specific_text("data/input.txt");
        long endTime_allWork = System.currentTimeMillis();
        long totalTime_allWork = endTime_allWork - startTime_allWork;
        System.out.println("\n\n" + totalTime_allWork + "ms");*/
        // full result sequential : 220944ms = 3min40 in sequential


        // Just Top50
        /*long startTime_top50 = System.currentTimeMillis();
        TreeMap<String, Integer> wordCounts = get_word_count("data/deontologie_police_nationale.txt");
        TreeMap<String,Integer> sorted_map = get_occurences_sorted(wordCounts);
        TreeMap<String,Integer> sorted_map_top50 = get_top_50(sorted_map, wordCounts);
        for(Map.Entry<String,Integer> entry : sorted_map_top50.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println(key + "\t" + value);
        }
        long endTime_top50 = System.currentTimeMillis();
        long totalTime_top50 = endTime_top50 - startTime_top50;
        System.out.println("\n\n" + totalTime_top50 + "ms");*/


        // THREADS ! //
        long startTime_allWork = System.currentTimeMillis();
        Scanner input = new Scanner(new File("data/deontologie_police_nationale.txt"));
        ConcurrentHashMap<String, Integer> wordCounts = new ConcurrentHashMap<String, Integer>();


        Thread t1 = new Share(wordCounts, input);
        Thread t2 = new Share(wordCounts, input);
        Thread t3 = new Share(wordCounts, input);

        t1.run();
        t2.run();
        t3.run();

        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(wordCounts);
        long endTime_allWork = System.currentTimeMillis();
        long totalTime_allWork = endTime_allWork - startTime_allWork;
        System.out.println("\n\n" + totalTime_allWork + "ms");

        // Word count non trié gros fichier en sequentiel: 3,48621667 min
        // Word count non trié gros fichier avec 3 threads: 2,93243333 min
    }



    public static TreeMap<String, Integer> get_word_count(String filename) throws FileNotFoundException {
        Scanner input = new Scanner(new File(filename));
        Set<String> stopWordsSet = new HashSet<String>();
        Collections.addAll(stopWordsSet, stop_words);
        TreeMap<String, Integer> wordCounts = new TreeMap<String, Integer>();
        while (input.hasNext()) {
            String next = input.next().toLowerCase()
                    .replace(".", "")
                    .replace(",", "")
                    .replace("0", "")
                    .replace("1", "")
                    .replace("2", "")
                    .replace("3", "")
                    .replace("4", "")
                    .replace("5", "")
                    .replace("6", "")
                    .replace("7", "")
                    .replace("8", "")
                    .replace("9", "")
                    .replace("'", " ");
            if(!stopWordsSet.contains(next))
            {
                if (!wordCounts.containsKey(next)) {
                    wordCounts.put(next, 1);
                } else {
                    wordCounts.put(next, wordCounts.get(next) + 1);
                }
            }
        }
        return wordCounts;
    }


    public static void show_alphabetic_sort(TreeMap<String, Integer> wordCounts) throws FileNotFoundException {
        Map<String, Integer> map = new TreeMap<String, Integer>(wordCounts);
        Set set = map.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();
            System.out.print(me.getKey() + "\t" + me.getValue() + "\n");
        }
    }


    public static TreeMap<String,Integer> get_occurences_sorted(TreeMap<String, Integer> wordCounts) throws FileNotFoundException {
        ValueComparator bvc = new ValueComparator(wordCounts);
        TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
        sorted_map.putAll(wordCounts);
        return sorted_map;
    }

    public static TreeMap<String,Integer> get_top_50(TreeMap<String,Integer> sorted_map, TreeMap<String, Integer> wordCounts) {
        ValueComparator bvc = new ValueComparator(wordCounts);
        TreeMap<String, Integer> myNewMap = sorted_map.entrySet().stream()
                .limit(50)
                .collect(TreeMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
        TreeMap<String, Integer> top_50_sorted_map = new TreeMap<String, Integer>(bvc);
        top_50_sorted_map.putAll(myNewMap);
        return top_50_sorted_map;
    }


    public static void all_work_with_specific_text(String filename) throws FileNotFoundException {
        TreeMap<String, Integer> wordCounts = get_word_count(filename);
        System.out.println("Question 1");
        for (String word : wordCounts.keySet()) {
            int count = wordCounts.get(word);
            System.out.println(word + "\t" + count);
        }

        System.out.println("\n\n");
        System.out.println("Question 2");
        TreeMap<String,Integer> sorted_map = get_occurences_sorted(wordCounts);
        for(Map.Entry<String,Integer> entry : sorted_map.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }

        System.out.println("\n\n");
        System.out.println("Question 3");
        show_alphabetic_sort(wordCounts);

        System.out.println("\n\n");
        System.out.println("Question 4\n");
        System.out.println("just call forestier_mayotte.txt...");

        System.out.println("\n\n");
        System.out.println("Question 5\n");
        System.out.println("Add in stop_words list (special char)");

        System.out.println("\n\n");
        System.out.println("Question 6");
        TreeMap<String,Integer> sorted_map_top50 = get_top_50(sorted_map, wordCounts);
        for(Map.Entry<String,Integer> entry : sorted_map_top50.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println(key + "\t" + value);
        }

        System.out.println("\n\n");
        System.out.println("Question 7");
        System.out.println("Add in stop_words list (conjunctions / pronouns)");

        System.out.println("\n\n");
        System.out.println("Question 8");
        System.out.println("Same thing, can also use replace function as we did in order to deal with '.'");

        System.out.println("\n\n");
        System.out.println("Question 9");
        System.out.println("Question 10");
        System.out.println("Question 11");
        System.out.println("Same thing, can also use replace function as we did in order to deal with '.'");
    }
}

