
import java.net.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import javax.net.ssl.SSLException;
import javax.print.Doc;

public class WebCrawler {

    //Vars
    ArrayList arList = new ArrayList();
    Set<String> hashS = new HashSet<String>();
    HashMap<String, Integer> curCount = new HashMap<>();
    HashMap<String, Integer> outlinkCount = new HashMap<>();
    List<String> englishWordList = Files.readAllLines( new File( "C:\\Users\\adamv\\Documents\\WebCrawler\\englishWordList.txt" ).toPath(), Charset.defaultCharset() );
    List<String> spanishhWordList = Files.readAllLines( new File( "C:\\Users\\adamv\\Documents\\WebCrawler\\spanishWordList.txt" ).toPath(), Charset.defaultCharset() );
    List<String> frenchWordList = Files.readAllLines( new File( "C:\\Users\\adamv\\Documents\\WebCrawler\\frenchWordList.txt" ).toPath(), Charset.defaultCharset() );


    String lang;
    String siteTemp;
    int innerLinkCounter;
    int htmlCounter;

    //Constructor
    public WebCrawler(String site, String lan) throws IOException {
        lang = lan;
        siteTemp = site;
        arList.add(site);
        runEverything();
    }

    //Methods

    //Call Everything Method
    public void runEverything() throws IOException {
        int counter = 0;

        //CHANGE SAMPLE SIZE ADJUSTING THE 2 VALUES BELOW ON FORLOOPS (CURRENTLY SET TO 1000)
            for(int i = 0; arList.size() < 1000; i++){
                    addToArrayList(arList.get(i).toString());
                System.out.println("Checking Site:" + i);
            }

        for(int j=0; j<1000; j++) {
            hashS.add(arList.get(j).toString());     // add it to set..
        }
        //Create hashset from arrayList
        Iterator iterator = hashS.iterator();
        //Convert to string, add url to Hashset, pull text, add to hashmap
        while (iterator.hasNext() && counter<1000) {
           String temp = iterator.next().toString();
            addToHshMap(temp);
            numOfOutlinks(temp);
            getHTML(temp);
            counter++;
            System.out.println("Adding to HashMap, Site:" + counter);
        }
        writeCSV(curCount);
        writeCSVNum(outlinkCount);
        //Double check clear counters and sets
        clearSets();
    }

    //Add sites to ArrayList
    public void addToArrayList(String site) throws IOException {
        Document doc;
        Elements links = null;
        try {
            doc = Jsoup.connect(site).timeout(10000).get();
            links = doc.getElementsByTag("a");


        for (Element link : links) {
            String linkAdd = link.attr("href");
            //Added a language check in the link to confirm right site language being crawled
            if(linkAdd.startsWith("https://" + lang)) {
                //Need to add a counter to add number of links for report csv
                if(!arList.contains(linkAdd) && !linkAdd.startsWith("https://france")) {
                    arList.add(linkAdd);
                    System.out.println("Added Site: " + linkAdd);
                }
            }

            }

        } catch (HttpStatusException | NullPointerException | SocketTimeoutException | UnknownHostException e) {
            System.out.println("Invalid website");
        }
    }

    //Record Number of Outlinks
    public void numOfOutlinks(String site) throws IOException {
        Document docu;
        Elements links = null;
        try {
            docu = Jsoup.connect(site).timeout(10000).get();
            links = docu.getElementsByTag("a");

            for (Element link : links) {
                String linkAdd = link.attr("href");
                //Added a language check in the link to confirm right site language being crawled
                if(linkAdd.startsWith("https://")) {
                    //Need to add a counter to add number of links for report csv
                   innerLinkCounter++;
                }
            }
            outlinkCount.put(site, innerLinkCounter);
            innerLinkCounter = 0;
        } catch (HttpStatusException | NullPointerException | SocketTimeoutException | UnknownHostException e) {
            System.out.println("Invalid website");
        }
    }

    //Add Frequency to HashMap
    public void addToHshMap(String aS) throws IOException {
        Document doc = null;
        try {
            doc=Jsoup.connect(aS).get();


        String text= doc.text();
        text.replace('-', ' ');
        System.out.println(text);
        String[] words = text.trim().split("\\s+");
        for (String word: words) {
                word = word.toLowerCase();

                if (word.length() == 0) {
                    continue;
                }
                char punctuationEnd = word.charAt(word.length() - 1);
                char punctuationFront = word.charAt(0);
                if (punctuationEnd < 97 || punctuationEnd > 122) {
                    word = word.substring(0, word.length() - 1);
                }
                if (word.length() == 0) {
                    continue;
                }
                if (punctuationFront < 97 || punctuationFront > 122) {
                    word = word.substring(1, word.length());
                }
                if (word.length() == 0) {
                    continue;
                }
                //Checks against local dictionaries to confirm words are valid
                if ((englishWordList.contains(word) && lang == "en") || (frenchWordList.contains(word) && lang == "fr") || (spanishhWordList.contains(word) && lang == "es")) {
                curCount.put(word, curCount.getOrDefault(word, 0) + 1);
            }
        }
        for (String key: curCount.keySet()) {
            System.out.println(key + " " + curCount.get(key));
        }
        } catch (NullPointerException | IOException e) {
            System.out.println("Invalid website");
        }
    }

    //Code for printing into csv
    public void writeCSV(HashMap<String,Integer> words) {
        try {
    //For the next line put the filepath into the parameter
            FileWriter fw = new FileWriter("C:\\Users\\adamv\\Documents\\WebCrawler\\" + lang + "\\WordCount.csv", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            for (String entry : words.keySet()) {
                Integer value = words.get(entry);
    //Added lang tag like professor talked about in class
                pw.println(entry + "," + value + "," + lang);
            }
            pw.flush();
            pw.close();
        } catch (Exception e) {
            System.out.println("Wrong");
        }
    }

    //Code for printing into csv
    public void writeCSVNum(HashMap<String,Integer> words) {
        try {
            //For the next line put the filepath into the parameter
            FileWriter fw = new FileWriter("C:\\Users\\adamv\\Documents\\WebCrawler\\" + lang + "\\NumberOutlinks.csv", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            for (String entry : words.keySet()) {
                Integer value = words.get(entry);
                //Added lang tag like professor talked about in class
                pw.println(entry + "," + value + "," + lang);
            }
            pw.flush();
            pw.close();
        } catch (Exception e) {
            System.out.println("Wrong");
        }
    }

    //Method for Getting Full HTML
        public void getHTML(String url) {
            try {
                URL web = new URL(url);
                HttpURLConnection con = (HttpURLConnection) web.openConnection();
                System.out.println(con.getResponseCode());
                if (con.getResponseCode() == 200) {
                    InputStream im = con.getInputStream();
                    StringBuffer sb = new StringBuffer();
                    BufferedReader br = new BufferedReader(new InputStreamReader(im));
                    //Put in your custom file path
                    FileOutputStream fo = new FileOutputStream("C:\\Users\\adamv\\Documents\\WebCrawler\\" + lang +  "\\HTML-" + htmlCounter + ".txt");
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fo));
                    String line = br.readLine();
                    htmlCounter++;
                    while (line != null) {
                        System.out.println(line);
                        bw.write(line);
                        bw.newLine();
                        bw.flush();
                        line = br.readLine();

                    }
                }
            } catch (Exception e) {
                System.out.println("Wrong");
            }
        }

        //Double check clear counters and sets
        public void clearSets(){
        hashS = null;
        arList = null;
        outlinkCount = null;
        curCount = null;
        innerLinkCounter = 0;
        lang = null;
        }


        public static void main(String[] args) throws IOException {

        //Create Objects (3 lang) (comment out ones not in use, run one object at a time)
           WebCrawler englishLang = new WebCrawler("https://en.wikipedia.org/wiki/Main_Page", "en");
           System.out.println("Ran # 1..");
           WebCrawler espanolLang = new WebCrawler("https://es.wikipedia.org/wiki/Wikipedia:Portada", "es");
           System.out.println("Ran # 2..");
           WebCrawler frenchLang = new WebCrawler("https://fr.wikipedia.org/wiki/Wikip%C3%A9dia:Accueil_principal", "fr");
           System.out.println("Ran # 3..");





}

}
