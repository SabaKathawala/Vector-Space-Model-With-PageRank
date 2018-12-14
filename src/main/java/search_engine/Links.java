package search_engine;

import java.util.HashSet;
import java.util.Set;

public class Links {
  Set<String> inLinks;
  Set<String> outLinks;

  Links() {
    inLinks = new HashSet<>();
    outLinks = new HashSet<>();
  }

  Links(Set<String> inLinks, Set<String> outLinks) {
    this.inLinks = inLinks;
    this.outLinks = outLinks;
  }
}
