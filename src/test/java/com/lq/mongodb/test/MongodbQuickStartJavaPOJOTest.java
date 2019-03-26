package com.lq.mongodb.test;

import com.lq.mongodb.entity.Address;
import com.lq.mongodb.entity.Favorites;
import com.lq.mongodb.entity.User;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Updates.addEachToSet;
import static com.mongodb.client.model.Updates.set;

/**
 * ***************鸟欲高飞先振翅**************
 * Created with IntelliJ IDEA.
 * Description:  Mongodb基于原生的java POJO 进行骚操作
 *
 * @author: liqian
 * @Date: 2019-02-16
 * @Time: 16:19
 * ***************人求上进先读书**************
 */
public class MongodbQuickStartJavaPOJOTest {

    private static final Logger logger = LoggerFactory.getLogger(MongodbQuickStartJavaPOJOTest.class);

    // 数据库
    private MongoDatabase database;

    // 文档集合
    private MongoCollection<User> collection;

    // 连接客户端（内置连接池）
    private MongoClient client;

    @Before
    public void init() {
        // MongoDB 官方默认不支持POJO 来操作，所以我们需要处理POJO的编解码器
        // 编解码器 list集合
        List<CodecRegistry> codecRegistryList = new ArrayList<>();
        // list加入默认的编码器集合
        codecRegistryList.add(MongoClient.getDefaultCodecRegistry());
        // 获取 PojoCodecProvider
        PojoCodecProvider build = PojoCodecProvider.builder().automatic(true).build();
        // 生成一个POJO的编解码器
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(build);
        // 将POJO的编解码器 加入 list
        codecRegistryList.add(pojoCodecRegistry);

        // 通过编解码器的list 生成编解码器注册中心
        CodecRegistry registry = CodecRegistries.fromRegistries(codecRegistryList);

        // 把编解码器注册中心放入MongoClientOptions, MongoClientOptions相当于连接池的配置信息
        MongoClientOptions mongoClientOptions = MongoClientOptions.builder()
                .codecRegistry(registry).build();

        ServerAddress serverAddress = new ServerAddress("192.168.1.128", 27022);

        client = new MongoClient(serverAddress, mongoClientOptions);
        database = client.getDatabase("liqian");
        collection = database.getCollection("users", User.class);
    }

    @Test
    public void insert() {
        User user = new User();
        user.setUsername("杨红");
        user.setCountry("中国");
        user.setAge(24);
        user.setLenght(155f);
        user.setSalary(new BigDecimal(5000.00));

        Address address1 = new Address();
        address1.setACode("541236");
        address1.setAdd("春熙路");
        user.setAddress(address1);

        Favorites favorites1 = new Favorites();
        favorites1.setCites(Arrays.asList("云南", "广西"));
        favorites1.setMovies(Arrays.asList("红楼梦", "湘西怒晴记", "爱情保卫战"));
        user.setFavorites(favorites1);
        // insertOne 插入单条数据，insertMany可以插入多条
        collection.insertOne(user);
    }

    @Test
    public void findTest() {
        List<User> result = new ArrayList<>();
        // block接口专门用于处理查询出来的数据
        Block<User> blockPring  = new Block<User>() {
            @Override
            public void apply(User document) {
                logger.info(document.toString());
                result.add(document);
            }
        };

        // 1、db.users.find({ "favorites.cites" : { "$all" : [ "成都" , "南充"]}})
        // 封装条件，定义数据过滤器
        Bson all = all("favorites.cites", Arrays.asList("成都", "南充"));
        // 执行查询
        FindIterable<User> find = collection.find(all);
        find.forEach(blockPring);
        logger.info("查询集合大小 :{}", result.size());
        result.removeAll(result);

        // 2、db.users.find({ "$and" : [ { "username" : { "$regex" : ".*s.*"}} , { "$or" : [ { "country" : "English"} , { "country" : "USA"}]}]})
        String regexStr = ".*李.*";
        // 定义过滤器，根据正则表达式筛选用户名
        Bson regex = regex("username", regexStr);
        // 定义过滤器，筛选国家
        Bson or = or(eq("country", "中国"), eq("country", "USA"));
        Bson and = and(regex, or);
        // 查询数据
        FindIterable<User> find2 = collection.find(and);
        find2.forEach(blockPring);
        logger.info("查询集合大小 :{}", result.size());
    }

    @Test
    public void updateTest() {
        // 1、db.users.updateMany({ "username" : "李谦"},{ "$set" : { "age" : 20}},true)
        // 定义过滤器， 先拿取条件
        Bson eq = eq("username", "李谦");
        // 更新的字段，来自update包的静态导入, 将username="李谦" 的age修改为20岁
        Bson set = set("age", 20);
        // 修改数据
        UpdateResult updateResult = collection.updateMany(eq, set);
        logger.info("受影响的行数 :{} ", updateResult.getModifiedCount());

        // 2、将喜爱成都的城市相关数据添加他们更多的喜欢电影
        // db.users.updateMany({ "favorites.cites" : "成都"}, { "$addToSet" : { "favorites.movies" : { "$each" : [ "小电影2 " , "小电影3"]}}},true)
        Bson eqCity = eq("favorites.cites", "成都");
        Bson moviesSet = addEachToSet("favorites.movies", Arrays.asList("苍老师MV", "波多老师MV"));

        UpdateResult update = collection.updateMany(eqCity, moviesSet);
        logger.info("受影响的行数 :{} ", update.getModifiedCount());
    }

    @Test
    public void deleteTest() {
        // 1、 根据名称删除、定义过滤器
        Bson eq = eq("username", "张飞");
        // 执行删除
        DeleteResult deleteResult = collection.deleteOne(eq);
        logger.info("受影响的行数 :{} ", deleteResult.getDeletedCount());

        // 2、指定年龄范围删除 ，删除年龄为 35-70岁的人
        Bson gt = gt("age", 35);
        Bson lt = lt("age", 70);
        Bson and = and(gt, lt);
        DeleteResult deleteResult1 = collection.deleteMany(and);
        logger.info("受影响的行数 :{} ", deleteResult1.getDeletedCount());

    }


}
