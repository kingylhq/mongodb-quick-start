package com.lq.mongodb.test;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.*;

/**
 * ***************鸟欲高飞先振翅**************
 * Created with IntelliJ IDEA.
 * Description:  Mongodb基于原生的java 进行骚操作
 *
 * @author: liqian
 * @Date: 2019-02-16
 * @Time: 14:24
 * ***************人求上进先读书**************
 */
public class MongodbQuickStartJavaDocTest {

    private static final Logger logger = LoggerFactory.getLogger(MongodbQuickStartJavaDocTest.class);

    // 数据库
    private MongoDatabase database;

    // 文档集合
    private MongoCollection<Document> collection;

    // 连接客户端（内置连接池）
    private MongoClient client;

    @Before
    public void init() {
        // 链接数据库
        client = new MongoClient("192.168.1.128", 27022);
        // 获取指定的数据库
        database = client.getDatabase("liqian");
        // 根据数据库获取指定的集合(表)
        collection = database.getCollection("users");
    }

    @Test
    public void insertTest() {
        // 封装数据的工具
        Document document1 = new Document();
        // 姓名
        document1.append("username", "张飞");
        // 国家
        document1.append("country", "China");

        // 地址(邮编和城市)
        Map<String, Object> address1 = new HashMap<>(16);
        address1.put("aCode", "205533");
        address1.put("add", "");
        document1.append("address",address1);

        // 最喜爱的
        Map<String, Object> favorites1 = new HashMap<>(16);
        favorites1.put("movies", Arrays.asList("英雄本色", "流浪地球"));
        favorites1.put("cites", Arrays.asList("成都", "重庆", "南充"));
        document1.append("favorites",favorites1);

        // 年龄
        document1.append("age", 61);
        // 身高
        document1.append("hight", 173);
        // 薪水
        document1.append("salary", new BigDecimal(4500.00));

        // 执行插入操作
        collection.insertOne(document1);
    }

    @Test
    public void findTest() {
        List<Document> result = new ArrayList<>();
        // block接口专门用于处理查询出来的数据
        Block<Document> blockPring  = new Block<Document>() {
            @Override
            public void apply(Document document) {
                logger.info(document.toJson());
                result.add(document);
            }
        };

        // 1、db.users.find({ "favorites.cites" : { "$all" : [ "成都" , "南充"]}})
        // 封装条件，定义数据过滤器
        Bson all = all("favorites.cites", Arrays.asList("成都", "南充"));
        // 执行查询
        FindIterable<Document> find = collection.find(all);
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
        FindIterable<Document> find2 = collection.find(and);
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
