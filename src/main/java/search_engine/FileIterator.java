package search_engine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * author: Saba
 * date: September 16, 2018
 *
 * comment: a regular FileIterator
 */

public class FileIterator {

    BufferedReader br = null;
    String line;

    FileIterator() {

    }
    FileIterator(String filePath) {
        try {
            br = new BufferedReader(new FileReader(filePath));
        } catch(IOException ioe) {

        }
    }

    /**
     *
     * @return string containing line read from file
     */

    public String next() {
        //System.out.println(line);
        return line;
    }

    /**
     *
     * @return true/false if file read complete
     */

    public boolean hasNext() {
        try {
            line = br.readLine();
        } catch (IOException ie) {
            return false;
        }
        finally {
            if(line == null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    return false;
                }
            }
        }
        return line != null;
    }
}
