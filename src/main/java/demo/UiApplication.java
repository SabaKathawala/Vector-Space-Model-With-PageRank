package demo;

import java.util.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import search_engine.Engine;
import search_engine.RedisHelper;

@SpringBootApplication
@RestController
public class UiApplication {

	@RequestMapping("/resource")
	public Map<String,Object> home() {
		Map<String,Object> model = new HashMap<String,Object>();
		model.put("id", "Hello");
		model.put("content", "Type your query");
		return model;
	}

  static class Result {
    String title;
    String address;

    public Result() {}

    public Result(String title, String address) {
      this.title = title;
      this.address = address;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getAddress() {
      return address;
    }

    public void setAddress(String address) {
      this.address = address;
    }
  }

  @RequestMapping("/query/{query}/{type}")
  public Map<String,Object> query(@PathVariable String query, @PathVariable String type) {
    Set<String> set = Engine.retrieveDocuments(query, type);
    RedisHelper redisHelper = RedisHelper.getInstance();

    Map<String,Object> model = new HashMap<String,Object>();
    List<Result> result = new ArrayList<>();
    for(String link: set) {
      String title = redisHelper.getValue(link + "-title");
      result.add(new Result(title, link));
    }
    model.put("id", "Query : " + query);
    model.put("content", "Type :: " + type);
    model.put("results", result);
    return model;
  }


	public static void main(String[] args) {
		SpringApplication.run(UiApplication.class, args);
	}

}
