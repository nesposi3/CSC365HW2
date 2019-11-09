package com.nesposi3.Utils;

import com.nesposi3.BTree;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CacheUtils {
    public static final String DIRECTORY_NAME = "storage/btrees/";
    public final static String BASE_URI = "https://en.wikipedia.org";
    public static final String URL_BEGINNING = "httpsenwikipediaorgwiki";
    /**
     * Removes special characters from the input string
     *
     * @param url The string to be transformed
     * @return
     */
    public static String generateFileName(String url) {
        String removePunctPattern = "[\\.\\/:]";
        return url.replaceAll(removePunctPattern, "");
    }

    /**
     * Goes through control file and adds files to cache based on links from those files
     *
     * @throws IOException
     * @throws ParseException
     */
    public static void initialize() throws IOException, ParseException {
        File links = new File("links.txt");
        Scanner file = new Scanner(links);
        //This pattern excludes all files, special wikipedia pages, and disambiguation pages
        Pattern urlPattern = Pattern.compile("\\/wiki\\/((?!((Wikipedia:)|(File:)|(Help:))).)*(?<!(_\\(disambiguation\\)))");
        while (file.hasNextLine()) {
            String line = file.nextLine();
            Document doc = (getWebsiteDocument(line));
            Elements linkElements = doc.select("a");
            int numLinks = 0;
            int i = 0;
            writeDocToBtree(generateFileName(line),doc);
            while (numLinks < 10 && i < linkElements.size()) {
                Element e = linkElements.get(i);
                i++;
                String link = (e.attr("href"));
                Matcher m = urlPattern.matcher(link);
                if (m.matches()) {
                    numLinks++;
                    String flink = BASE_URI + (e.attr("href"));
                    Document linkedDoc = getWebsiteDocument(flink);
                    writeDocToBtree(generateFileName(flink),linkedDoc);
                }
            }
        }
    }

    /**
     * Takes in a url, creates and stores an html file from the url
     * Checks when files stored in cache were last updated, if later than web, redownload
     *
     * @param url The url for the website to be downloaded
     * @return The jsoup Document created by the method
     * @throws IOException
     * @throws ParseException
     */
    public static Document getWebsiteDocument(String url) throws IOException, ParseException {
        String fileName = generateFileName(url);
        File f = new File("storage/html/" + fileName + ".html");
        if (f.exists()) {
            //file exists, check when local file last modified
            long localMod = f.lastModified();
            // Check when website was last modified
            Connection.Response conn = Jsoup.connect(url).execute();
            String dString = conn.header("Last-Modified");
            // Pattern based on format of HTTP last modified header
            SimpleDateFormat format = new SimpleDateFormat("EEE',' dd MMM YYYY HH':'mm':'ss zz");
            Date date = format.parse(dString);
            long webMod = date.getTime();
            // If website modified after local, get page again
            if (webMod > localMod) {
                f.delete();
                f.createNewFile();
                Document doc = Jsoup.connect(url).get();
                String text = doc.outerHtml();
                BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                writer.write(text);
                writer.close();
                return doc;
            } else {
                return Jsoup.parse(f, "UTF-8", "");
            }
        } else {
            f.createNewFile();
            //file doesn't exist, download
            Document doc = Jsoup.connect(url).get();
            String text = doc.outerHtml();
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            writer.write(text);
            writer.close();
            return doc;
        }
    }

    public static BTree writeDocToBtree(String name,Document document) throws IOException {

        BTree bTree = new BTree(DIRECTORY_NAME+name);
        //Maps word hash with frequencies
        HashMap<Long,Integer> map = new HashMap<>();
        String content = document.text();
        String delimiters ="[ .!?@\\[\\]/()\\-—,\"\']";
        String[] words = content.split(delimiters);
        for (int i = 0; i <words.length ; i++) {
            // Use the 64 bit hashing function
            Long hashedWord = ClusteringUtils.stringHash64(words[i]);
            //Check if word exists, if yes, increment value
            if(map.containsKey(hashedWord)){
                map.put(hashedWord,map.get(hashedWord)+1);
            }else{
                map.put(hashedWord,1);
            }
        }
        // Add vals to bTree
        map.forEach(bTree::insert);
        return bTree;
    }
    public static String titleFromFileName(String name){
        return  name.split(URL_BEGINNING)[1];
    }
}

