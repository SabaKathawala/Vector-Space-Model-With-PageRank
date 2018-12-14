package search_engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * author: Saba Kathawala (650408125)
 * date: September 18, 2018
 *
 * Input: Documents that are read one by one from the collection
 * Output: List of Tokens to be added to the index
 *
 */

public class TextProcessor {

    public final SplitToken delimiter;
    private List<File> listOfFiles = new ArrayList<>();

    enum SplitToken {
        SPACE(" ");

        private String splitToken;

        SplitToken(String splitToken) {
            this.splitToken = splitToken;
        }

        public String getSplitToken() {
            return splitToken;
        }
    }

    private Set<String> pos;

    //remove everything other than lowercase and uppercase letters
    private static final String DEFAULT_REGEX = "[^a-zA-Z0-9]";

    private String regex;
    //set to store stop words
    private Set<String> stopWords;

    //object to hold a Stemmer
    Porter stemmer = null;

    boolean removePunctuation;
    boolean removeStopWords;

    public TextProcessor(boolean removePunctuation, String regex, boolean doesStem,
                  boolean removeStopWords, SplitToken delimiter) {

        this.removePunctuation = removePunctuation;
        if(regex == null || regex.isEmpty()) {
            this.regex = DEFAULT_REGEX;
        } else {
            this.regex = regex;
        }
        if (doesStem) {
            this.stemmer = new Porter();
        }

        this.removeStopWords = removeStopWords;

        this.stopWords = fillSet("src/stopwords.txt");

        this.pos = fillSet("src/pos.txt");

        this.delimiter = delimiter;
    }

    // to read all stopwords in a set
    private Set<String> fillSet(String filePath) {
        Set<String> words = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            while (line != null) {
                words.add(line.trim());
                line = br.readLine();
            }
        } catch (IOException ioe) {

        }
        return words;
    }

    static class Token {
        String val;

        public Token(String token) {
            this.val = token;
        }
    }

    /**
     *
     * @param fileType: type of file
     * @param filePath: File object
     * @return list of tokens in the file
     */


    public List<Token> process(String fileType, String filePath) {
        List<Token> listOfTokens = new ArrayList<>();
        FileIterator iterator = FileFactory.getInstance(fileType, filePath);
        while (iterator.hasNext()) {
            String line = iterator.next();
            listOfTokens.addAll(process(line));
        }
        return listOfTokens;
    }

    /**
     *
     * @param line: line to be processed
     * @return array of processed tokens in the line
     */
    public List<Token> process(String line) {
        String[] tokens = line.split(delimiter.getSplitToken());
        List<Token> processedTokens = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {

            String token = tokens[i];
            if(token.equals("collegeofdentistri")) {
                System.out.println("collegeofdentistri");
            }
            if (removePunctuation) {
                token = token.replaceAll(regex, " ");
            }

            //making everything lowercase
            token = token.toLowerCase().trim();

            if (!token.isEmpty()) {

                if (removeStopWords && stopWords.contains(token)) {
                    continue;
                }
                if (stemmer != null) {
                    token = stemmer.stripAffixes(token);
                }

                if (removeStopWords && stopWords.contains(token)) {
                    continue;
                }
                if(token.isEmpty()){
                    continue;
                }
                processedTokens.add(new Token(token));
            }
        }
        return processedTokens;
    }

}
