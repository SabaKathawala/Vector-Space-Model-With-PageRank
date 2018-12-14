package search_engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * author: Saba Kathawala (650408125)
 *
 * Contains methods to calculate PageRank and TFIDF scores
 *
 */
public class Scorer {

    public static Map<String, Double> pageRank(Map<String, Links> graph, double dampingFactor) {

        Map<String, Double> pageRank = new HashMap<>();
        double alphaInverse = 1-dampingFactor;
        double teleportConstant = alphaInverse/graph.size();

        Map<String, Double> temporaryPageRankScore = new HashMap<>();
        fill(graph.keySet(), pageRank, 1.0/graph.size());

        int i = 1;
        while(i<=10) {

            for(String link: graph.keySet()) {
                double currentScore = 0.0;
                //calculates inner sum
                for(String inLink: graph.get(link).inLinks) {
                    double outLinks = graph.get(inLink).outLinks.size();
                    if(outLinks == 0) {
                        currentScore += 0;
                    } else {
                        currentScore += pageRank.get(inLink)/outLinks;
                    }
                }
                currentScore *= dampingFactor;
                currentScore += teleportConstant;

                temporaryPageRankScore.put(link, currentScore);
            }
            pageRank = temporaryPageRankScore;
            //temporaryPageRankScore = new HashMap<>();
            i++;
        }
        return pageRank;

    }

    private static void fill(Set<String>  links, Map<String, Double> pageRankScore,
                      double initialValue) {
        for(String link: links) {
            pageRankScore.put(link, initialValue);
        }
    }

    public static Map<String, Double> topicalPageRank(Map<String, Links> graph, Set<String> relevantDocuments, double dampingFactor, List<TextProcessor.Token> queryTerms) {

        Map<String, Double> topicalPageRank = new HashMap<>();
        double alphaInverse = 1-dampingFactor;
        double tfidfOfAllLinks = getRelevanceScore(graph.keySet(), queryTerms);
        double teleportConstant = alphaInverse/tfidfOfAllLinks;

        Map<String, Double> temporaryPageRankScore = new HashMap<>();
        fill(relevantDocuments, topicalPageRank,1/tfidfOfAllLinks);

        int i = 1;
        int file = 1;
        while(i<=1) {

            for(String link: relevantDocuments) {
                System.out.println(file + ": " + link);
                file++;
                double currentScore = 0.0;
                //calculates inner sum
                double tfidfOfLink = getRelevanceScore(link, queryTerms);

                for(String inLink: graph.get(link).inLinks) {
                    Set<String> outLinks = graph.get(inLink).outLinks;
                    if(outLinks.size() == 0) {
                        currentScore += 0;
                    } else {
                        currentScore += tfidfOfLink/getRelevanceScore(outLinks, queryTerms);
                    }
                }
                currentScore *= dampingFactor;
                currentScore += tfidfOfLink*teleportConstant;

                temporaryPageRankScore.put(link, currentScore);
            }
            topicalPageRank = temporaryPageRankScore;
            //temporaryPageRankScore = new HashMap<>();
            i++;
        }
        return topicalPageRank;
    }

    public static double getRelevanceScore(Set<String> outLinks, List<TextProcessor.Token> queryTerms){
        RedisHelper redisHelper = RedisHelper.getInstance();
        double score = 0.0;
        for(String outLink: outLinks) {
            for (TextProcessor.Token term : queryTerms) {
                String val = redisHelper.getValue("tfidf-" + term.val + "-" + outLink);
                if(val != null) {
                    score += Double.valueOf(val);
                }
            }
        }
        return score;
    }

    public static double getRelevanceScore(String link, List<TextProcessor.Token> queryTerms){
        RedisHelper redisHelper = RedisHelper.getInstance();
        double score = 0.0;
        for (TextProcessor.Token term : queryTerms) {
            String val = redisHelper.getValue("tfidf-" + term.val + "-" + link);
            if(val != null) {
                score += Double.valueOf(val);
            }
        }
        return score;
    }
}
