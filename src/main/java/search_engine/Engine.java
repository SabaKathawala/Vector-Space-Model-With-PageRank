package search_engine;

import search_engine.TextProcessor.Token;

import java.util.*;

public class Engine {

  public static String query;

  public static Set<String> retrieveDocuments(String query, String type) {
    Engine.query = query;
    switch (type) {
      case "Tfidf": return tfidf(query);

      case "PageRank": return pageRank(query);

      case "QD-PageRank": return qdPageRank(query);

      case "Query-Expansion-Tfidf": return queryExpansionTfIdf(query);

      case "Query-Expansion-QD": return queryExpansionQD(query);
    }
    return tfidf(query);
  }

  private static Set<String> queryExpansionTfIdf(String query) {
      String expandedQuery = WordNetHelper.getExpandedQuery(query);
      System.out.println(expandedQuery);
      return tfidf(expandedQuery);
  }

  private static Set<String> queryExpansionQD(String query) {
    String expandedQuery = WordNetHelper.getExpandedQuery(query);
    System.out.println(expandedQuery);
    return qdPageRank(expandedQuery);
  }

  public static Set<String> tfidf(String query) {
    TextProcessor textProcessor = new TextProcessor(true, "", true,
      true, TextProcessor.SplitToken.SPACE);
    //create graph
    RedisHelper redisHelper = RedisHelper.getInstance();
    List<Token> tokens = textProcessor.process(query);
    Map<String, Double> relevantDocuments = getDocumentsWithQueryWords(tokens);
    Set<String> rankedDocuments = getRankedDocuments(relevantDocuments, 3000, true, query);
    return rankedDocuments;
  }

  public static Set<String> pageRank(String query) {
    TextProcessor textProcessor = new TextProcessor(true, "", true,
      true, TextProcessor.SplitToken.SPACE);
    //create graph
    RedisHelper redisHelper = RedisHelper.getInstance();
    Set<String> crawledLinks = redisHelper.getSet("crawledLinks");

    Map<String, Double> pageRankScore = new HashMap<>();
    for(String link: crawledLinks) {
      double score = Double.valueOf(redisHelper.getValue("pageRank-" + link));
      pageRankScore.put(link, score);
    }
    List<Token> tokens = textProcessor.process(query);
    Set<String> relevantDocuments = getDocumentsWithQueryWords(tokens).keySet();
    Map<String, Double> relevantPageRank = new HashMap<>();
    for(String link: relevantDocuments) {
      relevantPageRank.put(link, pageRankScore.get(link));
    }
    Set<String> rankedDocuments = getRankedDocuments(relevantPageRank, 3000, true, query);
    return rankedDocuments;
  }

  public static Set<String> qdPageRank(String query) {
    TextProcessor textProcessor = new TextProcessor(true, "", true,
      true, TextProcessor.SplitToken.SPACE);
    //create graph
    RedisHelper redisHelper = RedisHelper.getInstance();
    Set<String> crawledLinks = redisHelper.getSet("crawledLinks");

    Map<String, Links> graph = createGraph(redisHelper, crawledLinks);
    List<Token> tokens = textProcessor.process(query);
    System.out.println("Calculating Topical PageRank");
    Map<String, Double> relevantDocuments = getDocumentsWithQueryWords(tokens);
    Set<String> rankedDocuments = getRankedDocuments(relevantDocuments, 200, false, null);
    Map<String, Double> topicalPageRank = Scorer.topicalPageRank(graph, rankedDocuments, 0.85, tokens);
    //System.out.println(rankDocuments(topicalPageRank));
    return getRankedDocuments(topicalPageRank, 200, true, query);
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

  private static Set<String> getRankedDocuments(Map<String, Double> relevantDocuments, int k, boolean titleTest,
                                                String query) {
    //TreeMap to get sort cosine similarity in decreasing order
    Map<Double, Set<String>> invertMap = new TreeMap<>(Collections.reverseOrder());
    Set<String> rankedDocuments = new LinkedHashSet<>();


    for(Map.Entry<String, Double> entry: relevantDocuments.entrySet()) {
      String url = entry.getKey();
      double tfidf = entry.getValue();
      if(invertMap.containsKey(tfidf)) {
        invertMap.get(tfidf).add(url);
      } else {
        Set<String> links = new HashSet<>();
        links.add(url);
        invertMap.put(tfidf, links);
      }
    }

    // retrieve documents in order of decreasing cosine similarity
    Collection<Set<String>> invertedEntries = invertMap.values();

    // add the ids of document in rankedDocuments
    if(titleTest) {
      Iterator<Set<String>> setIterator = invertedEntries.iterator();
      while(setIterator.hasNext()) {
        Iterator<String> iterator = setIterator.next().iterator();
        while(iterator.hasNext()) {
          String url = iterator.next();
          if (checkTitle(Engine.query, url, true)) {
            rankedDocuments.add(url);
            iterator.remove();
          }
        }
      }
    }

    for(Set<String> links: invertedEntries) {
      for(String link: links) {
        rankedDocuments.add(link);
        if(rankedDocuments.size() == k) {
          return rankedDocuments;
        }
      }
    }
    return rankedDocuments;
  }

  private static boolean checkTitle(String tokens, String url, boolean all) {
    RedisHelper redisHelper = RedisHelper.getInstance();
    String title = redisHelper.getValue(url+"-title");
    if(all) {
      for(String token: tokens.split(" ")) {
        if(!title.toLowerCase().contains(token.toLowerCase())) {
          return false;
        }
      }
      return true;
    }
    for(String token: tokens.split(" ")) {
      if(title.toLowerCase().contains(token.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   * @param queryTerms
   * @return mapping of documents to a map with query tokens and its weight in inverted index in that document

   **/

  private static Map<String, Double> getDocumentsWithQueryWords(
    List<Token> queryTerms) {

    // map of inverted index for query words
    Map<String, Double> relevantDocuments = new HashMap<>();
    RedisHelper redisHelper = RedisHelper.getInstance();
    // for each token in query

    for (Token token : queryTerms) {

      // if token exists in inverted index
      // Note: all tokens must exist in the inverted index but I noticed that a few didn't so had to add this condition
      Set<String> docs = redisHelper.getSet(token.val+"-docList");
      for (String doc : docs) {
        String val = redisHelper.getValue("tfidf-" + token.val + "-" + doc);
        double tfidf = Double.valueOf(val);
        if (!relevantDocuments.containsKey(token.val)
          || relevantDocuments.get(token.val) < tfidf) {
          relevantDocuments.put(doc, tfidf);
        }
      }
    }
    return relevantDocuments;
  }

}
