package search_engine;

import redis.clients.jedis.Jedis;

import java.util.Set;

public class RedisHelper extends FileIterator{

    private static RedisHelper SINGLETON = null;
    Jedis redisHelper = null;
    String[] lines = null;
    int index = 0;

    private RedisHelper() {
        this.redisHelper = new Jedis("127.0.0.1", 6379);
        this.redisHelper.connect();
    }

    public static RedisHelper getInstance() {
        if(SINGLETON == null) {
            SINGLETON = new RedisHelper();
        }
        return SINGLETON;
    }
    public Set<String> getKeys(String pattern) {
        return this.redisHelper.keys(pattern);
    }

    public Set<String> getSet(String key) {
        return this.redisHelper.smembers(key);
    }

    public String getValue(String key) {
        return this.redisHelper.get(key);
    }

    public void addKey(String key, String value) {
        this.redisHelper.set(key, value);
    }

    public void addSet(String key, String value) {
      this.redisHelper.sadd(key, value);
    }

  public void setLines(String key) {
        String html = this.redisHelper.get(key);
        this.index = 0;
        this.lines =  html.split("\\n+");
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
        if(this.index < this.lines.length) {
            this.line = this.lines[this.index++];
            return true;
        }
        return false;
    }
}
