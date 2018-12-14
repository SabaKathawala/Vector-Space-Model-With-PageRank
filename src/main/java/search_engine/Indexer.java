package search_engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * author: Saba Kathawala (650408125)
 * date: September 18, 2018
 *
 * Input: documents that need to be indexed for tokens (TextProcessor)
 * Output: An inverted index for fast access
 */


public class Indexer {

    private String fileType;
    TextProcessor textProcessor;

    class TokenInfo {
        int df; //document frequency
        //double idf; // inverse document frequency
        Map<String, DocumentInfo> docToTermFrequencyMap; //maps document id to DocumentInfo

        public TokenInfo(int df, Map<String, DocumentInfo> docToTermFrequencyMap) {
            this.df = df;
            //this.idf = Math.log(1330.0/df);
            this.docToTermFrequencyMap = docToTermFrequencyMap;
        }
    }

    class DocumentInfo {
        int tf; //term frequency
        String name;    //document name
        int maxTermFreq;    //max term frequency in the document for normalization

        public DocumentInfo(int tf) {
            this.tf = tf;
           // this.name = name;
            this.maxTermFreq = 1;
        }
    }

    //maps tokens to TokenInfo
    private Map<String, TokenInfo> invertedIndex;

    public Map<String, TokenInfo> getInvertedIndex() {
        return invertedIndex;
    }

    public Map<Integer, Double> getDocumentLength() {
        return documentLength;
    }

    //maps document id to its length
    private Map<Integer, Double> documentLength;

    protected Set<String> listOfFiles;

    public Indexer(TextProcessor textProcessor, String fileType,  Set<String> listOfFiles) {
        this.textProcessor = textProcessor;
        this.listOfFiles = listOfFiles;
        this.invertedIndex = new HashMap<>();
        this.fileType = fileType;

        fillTokens();
        this.documentLength = new HashMap<>();


        //findDocumentLengths();
    }

    public void add(String token, String url) {

        // if token already present
        if (this.invertedIndex.containsKey(token)) {
            TokenInfo tInfo = this.invertedIndex.get(token);

            // if document already present
            if (tInfo.docToTermFrequencyMap.containsKey(url)) {
                DocumentInfo dInfo = tInfo.docToTermFrequencyMap.get(url);

                // increase term frequency of token
                dInfo.tf++;

                // update term with max frequency
                if(dInfo.tf > dInfo.maxTermFreq) {
                    dInfo.maxTermFreq = dInfo.tf;
                }
            }
            // else create DocumentInfo object and add to map
            else {
                DocumentInfo dInfo = new DocumentInfo(1);
                tInfo.docToTermFrequencyMap.put(url, dInfo);
                tInfo.df++;
                //tInfo.idf = Math.log(1330.0/tInfo.df)/Math.log(2);
            }
        }
        // create document map and add document info
        // create token Info and add map to it
        // add token to inverted index
        else {
            Map<String, DocumentInfo> docToTermFrequencyMap = new HashMap<>();
            DocumentInfo dInfo = new DocumentInfo(1);
            docToTermFrequencyMap.put(url, dInfo);
            TokenInfo tInfo = new TokenInfo(1, docToTermFrequencyMap);
            //tInfo.idf = Math.log(1330.0)/Math.log(2);
            invertedIndex.put(token, tInfo);
        }
    }

    public void fillTokens() {
        int i=1;
            for(String name: listOfFiles) {
            List<TextProcessor.Token> tokens = textProcessor.process(fileType, name);
            String url = name.replaceAll("-html", "");
            for (TextProcessor.Token token : tokens) {
                add(token.val, url);
            }
            System.out.println("Tokens processed for file: " + i++ + " " + tokens.size() + " " + url);
        }
    }
}
