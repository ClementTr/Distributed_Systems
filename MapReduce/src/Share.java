import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Share extends Thread {

    public static String[] stop_words = {"↬", "à", ":", "-", "le", "la", "de", "des", "et", "les",
            "ou", "ne", "que", "qui", ";", "a", "ii", "°", "au", "aux", "du", "en", "où", "se", "sil",
            "y", "v", "sy", "en", "il", "est", "°", "", "”", "”v", "€", "", "r", "r-", "un", "une",
            "pour", "pas", "par", "l", "l-", "ce", "ces", "ainsi", "sa", "dans", "avec", "son", "sont",
            "sur", "leur", "si", "ses", "sous", "iii", "ier", "elle", "tout", "toute"};

    public Scanner input = null;
    public ConcurrentHashMap wordcount = null;

    public Share(ConcurrentHashMap<String, Integer> wordcount, Scanner input) {
        this.wordcount = wordcount;
        this.input = input;
    }

    public void run() {
        this.find();
    }

    public void find() {
        Set<String> stopWordsSet = new HashSet<String>();
        Collections.addAll(stopWordsSet, stop_words);
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
            if (!stopWordsSet.contains(next)) {
                if (!this.wordcount.containsKey(next)) {
                    this.wordcount.put(next, 1);
                } else {
                    this.wordcount.put(next, (Integer) this.wordcount.get(next) + 1);
                }
            }
        }
    }


    /*public static TreeMap<String, Integer> get_word_count(String filename) throws FileNotFoundException {
        Scanner input = new Scanner(new File(filename));
        Set<String> stopWordsSet = new HashSet<String>();
        Collections.addAll(stopWordsSet, stop_words);
        TreeMap<String, Integer> wordCounts = new TreeMap<String, Integer>();
        while (input.hasNext()) synchronized () {
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
            if (!stopWordsSet.contains(next)) {
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
    }*/
}