package search_engine;

import java.util.*;

public class PreProcessing {

  public static void preProcess() {
      //create inverted index
      RedisHelper redisHelper = RedisHelper.getInstance();
      Set<String> crawledLinks = redisHelper.getSet("crawledLinks");
      Map<String, Indexer.TokenInfo> invertedIndex = createIndex(crawledLinks);

      //calculate tf-idf from inverted index
      calculateTfIdf(invertedIndex, crawledLinks.size());

      //calculate PageRank
      calculatePageRank(crawledLinks);
  }

  public static Map<String, Indexer.TokenInfo> createIndex(Set<String> crawledLinks) {
    RedisHelper redisHelper = RedisHelper.getInstance();

    //get all keys for extracting HTML
    Set<String> html = redisHelper.getKeys("*-html");

    //create inverted index
    TextProcessor textProcessor = new TextProcessor(true, "", true,
      true, TextProcessor.SplitToken.SPACE);
    Indexer indexer = new Indexer(textProcessor, "REDIS", html);

    System.out.println("Adding anchors");
    //add anchor text to index
    for (String link : crawledLinks) {
      Set<String> anchorText = redisHelper.getSet(link + "-anchors");
      for (String line : anchorText) {
        List<TextProcessor.Token> tokens = textProcessor.process(line);
        for (TextProcessor.Token token : tokens) {
          indexer.add(token.val, link);
        }
      }
    }

    System.out.println("Adding title");
    //add title to index
    for (String link : crawledLinks) {
      String title = redisHelper.getValue(link + "-title");
      List<TextProcessor.Token> tokens = textProcessor.process(title);
      for (TextProcessor.Token token : tokens) {
        indexer.add(token.val, link);
      }
    }

    Map<String, Indexer.TokenInfo> invertedIndex = indexer.getInvertedIndex();
    //Add inverted index
    for (String token : invertedIndex.keySet()) {
      Map<String, Indexer.DocumentInfo> documentMap = invertedIndex.get(token).docToTermFrequencyMap;
      for (String doc : documentMap.keySet()) {
        redisHelper.addSet(token + "-docList", doc);
      }
    }

    return invertedIndex;
  }

  public static void calculatePageRank(Set<String> crawledLinks) {
    System.out.println("Caculating PageRank");
    RedisHelper redisHelper = RedisHelper.getInstance();
    Map<String, Links> graph = createGraph(redisHelper, crawledLinks);
    //calculate PageRank
    Map<String, Double> pageRankScore = Scorer.pageRank(graph, 0.85);
    //store PageRank score to Redis
    for (Map.Entry<String, Double> entry : pageRankScore.entrySet()) {
      redisHelper.addKey("pageRank-" + entry.getKey(), String.valueOf(entry.getValue()));
    }
  }
  private static Map<String, Links> createGraph(RedisHelper redisHelper, Set<String> crawledLinks) {
    Map<String, Links> graph = new HashMap<>();
    for (String currLink : crawledLinks) {

      Set<String> inLinks = redisHelper.getSet(currLink + "-inlinks");
      Set<String> outLinks = redisHelper.getSet(currLink + "-outlinks");
      Links links = new Links(inLinks, outLinks);
      graph.put(currLink, links);
    }
    return graph;
  }

  private static void calculateTfIdf(Map<String, Indexer.TokenInfo> invertedIndex, int totalDocs) {
    System.out.println("Calculating TFIDF");
    RedisHelper redisHelper = RedisHelper.getInstance();
    for (String token : invertedIndex.keySet()) {
      Map<String, Indexer.DocumentInfo> documentMap = invertedIndex.get(token).docToTermFrequencyMap;
      double df = documentMap.size();
      double idf = Math.log(totalDocs / df) / Math.log(2);
      for (Map.Entry<String, Indexer.DocumentInfo> doc : documentMap.entrySet()) {
        String url = doc.getKey();
        Indexer.DocumentInfo documentInfo = doc.getValue();
        int tf = documentInfo.tf;
        double tfidf = tf * idf;
        redisHelper.addKey("tfidf-" + token + "-" + url, String.valueOf(tfidf));
      }
    }
  }
}
