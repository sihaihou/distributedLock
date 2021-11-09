package com.reyco.lock.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.expression.spel.ast.OpAnd;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.xdevapi.JsonArray;
import com.reyco.lock.model.Order;
import com.reyco.lock.model.Product;
import com.reyco.lock.service.TestService;

@RestController
@RequestMapping("api")
public class TestController {

	@Autowired
	private TestService testService;

	@Autowired
	private StringRedisTemplate redisTemplate;
	
	static Random random = new Random();

	@GetMapping("test")
	public String test() {
		return "test";
	}

	@GetMapping("test1")
	public Callable<Order> test1() {
		return new Callable<Order>() {
			@Override
			public Order call() throws Exception {
				testService.testLock1();
				Order order = new Order();
				order.setId(1);
				order.setNo("111111");
				order.setState(1);
				order.setDesc("desc");
				return order;
			}
		};
	}

	@GetMapping("mysqlLock")
	public String testMysqlLock() {
		testService.testLock();
		return "test";
	}

	@GetMapping("redisLock")
	public String testRedisLock() {
		testService.testLock1();
		return "test";
	}

	@GetMapping("test2")
	public void test2() {
		System.out.println("String类型的使用");
		System.out.println("数值的自增自减");
		ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
		opsForValue.set("counter", "1");
		System.out.println("set counter 1");
		Long counter = opsForValue.increment("counter");
		System.out.println("increment counter:" + counter);
		counter = opsForValue.decrement("counter");
		System.out.println("decrement counter:" + counter);
		System.out.println("--------------------------------");
		System.out.println();

		opsForValue.set("name", "123");
		Integer append = opsForValue.append("name", "123");
		System.out.println("append name:" + append);
		String name = opsForValue.get("name");
		System.out.println("append get name:" + name);
		System.out.println("--------------------------------");
		System.out.println();

		Boolean setIfAbsent = opsForValue.setIfAbsent("setIfAbsent", "setIfAbsentValue");
		System.out.println("setIfAbsent setIfAbsent:" + setIfAbsent);
		String setIfAbsentValue = opsForValue.get("setIfAbsent");
		System.out.println("setIfAbsent get setIfAbsent:" + setIfAbsentValue);
		System.out.println("--------------------------------");
		System.out.println();

		Boolean setIfPresent = opsForValue.setIfPresent("setIfPresent", "setIfPresentValue");
		System.out.println("setIfPresent setIfPresent:" + setIfPresent);
		String setIfPresentValue = opsForValue.get("setIfPresent");
		System.out.println("setIfPresent get setIfPresentValue:" + setIfPresentValue);
		System.out.println("--------------------------------");
		System.out.println();

		opsForValue.set("offset", "offset", 3);
		String offsetValue = opsForValue.get("offset");
		System.out.println("offset:" + offsetValue);
		System.out.println("--------------------------------");
		System.out.println();

		opsForValue.set("size", "中");
		Long sizeSize = opsForValue.size("size");
		System.out.println("size:" + sizeSize);
		System.out.println("--------------------------------");
		System.out.println();

		opsForValue.setBit("A", 1, true);
		opsForValue.setBit("A", 7, true);
		String AValue = opsForValue.get("A");
		System.out.println("A bit:" + AValue);
		
		opsForValue.setBit("B", 1, true);
		opsForValue.setBit("B", 6, true);
		String BValue = opsForValue.get("B");
		System.out.println("B bit:" + BValue);
		
		opsForValue.setBit("AB", 1, true);
		opsForValue.setBit("AB", 7, true);
		opsForValue.setBit("AB", 9, true);
		opsForValue.setBit("AB", 14, true);
		String ABValue = opsForValue.get("AB");
		System.out.println("AB bit:" + ABValue);
		System.out.println("--------------------------------");
		System.out.println();
		
		List<String> list = new ArrayList<>();
		list.add("A");
		list.add("B");
		list.add("AB");
		List<String> multiGet = opsForValue.multiGet(list);
		System.out.println("multiGet:" + multiGet);
		System.out.println("--------------------------------");
		System.out.println();
	}

	@GetMapping("test3")
	public void test3() {
		HashOperations<String, Object, Object> opsForHash = redisTemplate.opsForHash();
		opsForHash.put("map", "k1", "k1-value");
		opsForHash.put("map", "k2", "k2-value");
		opsForHash.put("map", "k3", "北京欢迎你");
		Map<Object, Object> entries = opsForHash.entries("map");
		System.out.println("测试hash的entries:" + entries);
		System.out.println("--------------------------------");
		System.out.println();
		
		Set<Object> keys = opsForHash.keys("map");
		System.out.println("测试hash的keys:" + keys);
		System.out.println("--------------------------------");
		System.out.println();
		
		Long lengthOfValue = opsForHash.lengthOfValue("map", "k3");
		System.out.println("测试hash的lengthOfValue:" + lengthOfValue);
		System.out.println("--------------------------------");
		System.out.println();
		
		Long size = opsForHash.size("map");
		System.out.println("测试hash的size:" + size);
		System.out.println("--------------------------------");
		System.out.println();
		
		List<Object> values = opsForHash.values("map");
		System.out.println("测试hash的values:" + values);
		System.out.println("--------------------------------");
		System.out.println();
	}
	@GetMapping("test4")
	public void test4() {
		ListOperations<String, String> opsForList = redisTemplate.opsForList();
		opsForList.leftPush("list", "0");
		String index = opsForList.index("list", 0);
		System.out.println("测试list的index:" + index);
		
		opsForList.leftPush("list", "1");
		index = opsForList.index("list", 0);
		System.out.println("测试list的index:" + index);
		
		opsForList.leftPush("list", "2");
		index = opsForList.index("list", 0);
		System.out.println("测试list的index:" + index);
		System.out.println("--------------------------------");
		System.out.println();
		
		Long size = opsForList.size("list");
		System.out.println("测试list的size:" + size);
		System.out.println("--------------------------------");
		System.out.println();
		
		List<String> range = opsForList.range("list", 0, 1);
		System.out.println("测试list的range:" + range);
		System.out.println("--------------------------------");
		System.out.println();
		
		//先进先出----队列模型
		/*String rightPop = opsForList.rightPop("list");
		System.out.println("测试list的rightPop:" + rightPop);
		rightPop = opsForList.rightPop("list");
		System.out.println("测试list的rightPop:" + rightPop);
		rightPop = opsForList.rightPop("list");
		System.out.println("测试list的rightPop:" + rightPop);
		System.out.println("--------------------------------");
		System.out.println();*/
		
		//先进后出----栈模型
		String leftPop = opsForList.leftPop("list");
		System.out.println("测试list的leftPop:" + leftPop);
		leftPop = opsForList.leftPop("list");
		System.out.println("测试list的leftPop:" + leftPop);
		leftPop = opsForList.leftPop("list");
		System.out.println("测试list的leftPop:" + leftPop);
		System.out.println("--------------------------------");
		System.out.println();
	}
	
	@GetMapping("test5")
	public void test5() {
		SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
		opsForSet.add("set1", "a","b","c");
		opsForSet.add("set2", "b","c","d");
		opsForSet.add("set3", "a","b","c","d");
		//差集
		Set<String> set1DifferenceSet2 = opsForSet.difference("set1", "set2");
		Set<String> set2DifferenceSet1 = opsForSet.difference("set2", "set1");
		System.out.println("set1DifferenceSet2:"+set1DifferenceSet2+",set2DifferenceSet1:"+set2DifferenceSet1);
		System.out.println("--------------------------------");
		System.out.println();
		
		//交集
		Set<String> intersect = opsForSet.intersect("set1", "set2");
		System.out.println("intersect:"+intersect);
		System.out.println("--------------------------------");
		System.out.println();
		
		//合集
		Set<String> union = opsForSet.union("set1", "set2");
		System.out.println("union:"+union);
		System.out.println("--------------------------------");
		System.out.println();
		
		List<String> keys = new ArrayList<>();
		Set<String> set1DifferenceKeys = opsForSet.difference("set1",keys);
		System.out.println("set1DifferenceKeys:"+set1DifferenceKeys);
		System.out.println("--------------------------------");
		System.out.println();
		
		//去重随机取
		Set<String> distinctRandomMembers = opsForSet.distinctRandomMembers("set3", 5);
		System.out.println("distinctRandomMembers:"+distinctRandomMembers);
		System.out.println("--------------------------------");
		System.out.println();
		
		//是否存在
		Boolean memberA = opsForSet.isMember("set1", "a");
		Boolean memberD = opsForSet.isMember("set1", "d");
		System.out.println("memberA:"+memberA+",memberD:"+memberD);
		System.out.println("--------------------------------");
		System.out.println();
		
		//取出集合
		Set<String> members = opsForSet.members("set1");
		System.out.println("members:"+members);
		System.out.println("--------------------------------");
		System.out.println();
		
		//移动
		/*Boolean move = opsForSet.move("set1", "a", "set2");
		System.out.println("move:"+move);
		System.out.println("--------------------------------");
		System.out.println();*/
		
		Long size = opsForSet.size("set1");
		System.out.println("size:"+size);
		System.out.println("--------------------------------");
		System.out.println();
		
		//随机取出并移除一个或多个
		/*String pop = opsForSet.pop("set1");
		System.out.println("pop:"+pop);
		System.out.println("--------------------------------");
		System.out.println();
		List<String> popCount = opsForSet.pop("set1",2);
		System.out.println("popCount:"+popCount);
		System.out.println("--------------------------------");
		System.out.println();*/
		
		//随机查询一个或多个
		String randomMember = opsForSet.randomMember("set1");
		System.out.println("randomMember:"+randomMember);
		System.out.println("--------------------------------");
		System.out.println();
		
		List<String> randomMembers = opsForSet.randomMembers("set1",5);
		System.out.println("randomMembers:"+randomMembers);
		System.out.println("--------------------------------");
		System.out.println();
	}
	
	@GetMapping("test6")
	public void test6() {
		ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();
		opsForZSet.add("zset", "a", 1.1);
		opsForZSet.add("zset", "b", 1.7);
		opsForZSet.add("zset", "c", 1.5);
		opsForZSet.add("zset", "d", 2.4);
		
		Double score = opsForZSet.score("zset","a");
		System.out.println("score:"+score);
		System.out.println("--------------------------------");
		System.out.println();
		
		Long rank = opsForZSet.rank("zset", "d");
		System.out.println("rank:"+rank);
		System.out.println("--------------------------------");
		System.out.println();
		
		Long zCard = opsForZSet.zCard("zset");
		System.out.println("zCard:"+zCard);
		System.out.println("--------------------------------");
		System.out.println();
		
		//取分值区间的数据
		Long count = opsForZSet.count("zset", 1.1, 1.4);
		System.out.println("count:"+count);
		System.out.println("--------------------------------");
		System.out.println();
		
		//取索引区间的数据
		Set<String> range = opsForZSet.range("zset", 1, 2);
		System.out.println("range:"+range);
		System.out.println("--------------------------------");
		System.out.println();
		
		Set<String> rangeByScore = opsForZSet.rangeByScore("zset", 1.0, 1.7, 0, 2);
		System.out.println("rangeByScore:"+rangeByScore);
		System.out.println("--------------------------------");
		System.out.println();
		
		Set<String> reverseRangeByScore = opsForZSet.reverseRangeByScore("zset", 1.0, 1.7, 0, 2);
		System.out.println("reverseRangeByScore:"+reverseRangeByScore);
		System.out.println("--------------------------------");
		System.out.println();
	}
	
	@GetMapping("test7")
	public void test7() {
		ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();
		Set<TypedTuple<String>> set = new HashSet<>();
		for(int i=1;i<=10;i++) {
			TypedTuple<String> t = new DefaultTypedTuple<String>("魅族"+i,(double) random.nextInt(100) );
			set.add(t);
		}
		opsForZSet.add("productHots", set);
		redisTemplate.expire("productHots", 600, TimeUnit.SECONDS);
	}
	@GetMapping("test8")
	public void test8(String name,int score) {
		ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();
		/*Product product = new Product();
		product.setId(id);
		product.setDesc("商品"+id);
		product.setGmtCreate("2021-10-27 16:36:00");
		product.setGmtModified("2021-10-27 16:36:00");
		product.setGmtShelf("gmtShelf"+id);*/
		if(opsForZSet.rank("productHots", name)!=null) {
			opsForZSet.incrementScore("productHots", name, score);
		}else {
			opsForZSet.add("productHots", name, (double) random.nextInt(100));
		}
	}
	@GetMapping("test9")
	public Object test9(int count) {
		ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();
		Set<TypedTuple<String>> reverseRangeWithScores = opsForZSet.reverseRangeWithScores("productHots", 0, count);
		List<Product> products = new ArrayList<>();
		for (TypedTuple<String> typedTuple : reverseRangeWithScores) {
			String productStr = typedTuple.getValue();
			Product product = JSONObject.parseObject(productStr, Product.class);
	        int qty = new Double(typedTuple.getScore()).intValue();
			product.setQty(qty);
			products.add(product);
		}
		return products;
	}
}
