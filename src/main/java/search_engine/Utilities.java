package search_engine;

import java.io.File;
import java.util.List;

/**

 * author: Saba
 * date: September 19, 2018
 */

public class Utilities {

    public static void readFiles(File filePath, List<File> listOfFiles) {
        for (File fileEntry : filePath.listFiles()) {
            if (fileEntry.isDirectory()) {
                readFiles(fileEntry, listOfFiles);
            } else {
                listOfFiles.add(fileEntry);
            }
        }
    }
}
