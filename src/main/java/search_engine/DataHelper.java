package search_engine;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

public class DataHelper {
  public static void main(String[] args) {
      writeLinks();
      writePageRank();
      writeInvertedIndex();
  }

  private static void writeInvertedIndex() {
    RedisHelper redisHelper = RedisHelper.getInstance();
    Set<String> keys = redisHelper.getKeys("*-docList");
    try (PrintWriter writer = new PrintWriter(new FileWriter("Inverted Index.txt"))) {
      writer.write("Vocabulary Size: " + keys.size());
      int i=1;
      for(String key: keys) {
        Set<String> documents = redisHelper.getSet(key);
        writer.write(i++ + ": " + key + "\n");
        writer.write(documents + "\n\n");
      }
      writer.flush();
    } catch (IOException ioE) {
      System.out.println(ioE);
    }
  }

  private static void writePageRank() {
    RedisHelper redisHelper = RedisHelper.getInstance();
    Set<String> links = redisHelper.getSet("crawledLinks");
    try (PrintWriter writer = new PrintWriter(new FileWriter("PageRank.txt"))) {
      int i=1;
      for(String link: links) {
        double score = Double.valueOf(redisHelper.getValue("pageRank-" + link));
        writer.write(i++ + ": " + link + " -  " + score + "\n");
      }
      writer.flush();
    } catch (IOException ioE) {
      System.out.println(ioE);
    }
  }

  private static void writeLinks() {
      RedisHelper redisHelper = RedisHelper.getInstance();
      Set<String> links = redisHelper.getSet("crawledLinks");
      try (PrintWriter writer = new PrintWriter(new FileWriter("Crawled Links.txt"))) {
        int i=1;
        for(String link: links) {
         writer.write(i++ + ": " + link + "\n");
        }
        writer.flush();
      } catch (IOException ioE) {
        System.out.println(ioE);
      }
  }
}
