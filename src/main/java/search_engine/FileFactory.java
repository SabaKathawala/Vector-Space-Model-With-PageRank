package search_engine;

import search_engine.FileIterator;
import search_engine.RedisHelper;

/**
 * author: Saba
 * date: September 19, 2018
 *
 * comment: Factory to return type of FileIterator
 */

public class FileFactory {

    public static FileIterator getInstance(String fileType, String filePath) {
        switch (fileType) {
            case "REGULAR" :
                return new FileIterator(filePath);

//            case "SGML":
//                return new SGMLFileIterator(file);
            case "REDIS":
                FileIterator fi = RedisHelper.getInstance();
                ((RedisHelper) fi).setLines(filePath);
                return fi;

            default:
                return new FileIterator(filePath);
        }
    }
}
