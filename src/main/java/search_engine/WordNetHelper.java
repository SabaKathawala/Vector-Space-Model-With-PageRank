package search_engine;


import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.IStemmer;
import edu.mit.jwi.morph.WordnetStemmer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordNetHelper {
  public static final String WORDNET_PATH = "/WordNet-3.0/dict";

  public static String getExpandedQuery(String query) {
    String pwd = System.getProperty("user.dir");
    Set<String> words = new HashSet<>();
    URL url=null;
    try {
      url = new URL("file", null, pwd+WORDNET_PATH);
      System.out.println(pwd);
      IDictionary dict = new Dictionary(url);
      dict.open();

      IStemmer stemmer = new WordnetStemmer(dict);
      for(String term: query.split("\\s+")) {
        words.add(term);
        List<String> stems = stemmer.findStems(term, POS.NOUN);
        for (String stem : stems) {
          IIndexWord idxWord = dict.getIndexWord(stem, POS.NOUN);
          if (idxWord != null) {
            for (IWordID wordID : idxWord.getWordIDs()) {
              IWord word = dict.getWord(wordID);
              ISynset synset = word.getSynset();
              for (IWord w : synset.getWords()) {
                words.add(w.getLemma());
              }
            }
          }
        }
      }
    } catch (MalformedURLException e) {
      System.out.println("WordNet Database File Not Found");
      e.printStackTrace();
    } catch (IOException ioE) {
      System.out.println("Dictionary couldn't be opened");
    } finally {
      StringBuilder sb = new StringBuilder();
      for(String word: words) {
        for(String split: word.split("_")) {
          sb.append(split + " ");
        }
      }
      return sb.toString().trim();
    }
  }
}
