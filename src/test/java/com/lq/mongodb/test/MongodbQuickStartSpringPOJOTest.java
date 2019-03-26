package com.lq.mongodb.test;

import com.lq.mongodb.entity.Address;
import com.lq.mongodb.entity.Favorites;
import com.lq.mongodb.entity.User;
import com.mongodb.WriteResult;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * ***************鸟欲高飞先振翅**************
 * Created with IntelliJ IDEA.
 * Description:  Mongodb基于 Spring POJO 进行骚操作
 *
 * @author: liqian
 * @Date: 2019-02-16
 * @Time: 17:02
 * ***************人求上进先读书**************
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class MongodbQuickStartSpringPOJOTest {

    private static final Logger logger = LoggerFactory.getLogger(MongodbQuickStartSpringPOJOTest.class);

    @Autowired
    private MongoOperations tempelate;

    @Test
    public void insert() {
        User user = new User();
        user.setUsername("李飞龙");
        user.setCountry("中国");
        user.setAge(24);
        user.setLenght(171f);
        user.setSalary(new BigDecimal(85000.00));

        Address address1 = new Address();
        address1.setACode("323233");
        address1.setAdd("四川成都");
        user.setAddress(address1);

        Favorites favorites1 = new Favorites();
        favorites1.setCites(Arrays.asList("云南", "广西"));
        favorites1.setMovies(Arrays.asList("红楼梦", "湘西怒晴记", "爱情保卫战"));
        user.setFavorites(favorites1);

        user.setCreateTime(new Date());
        // insertOne 插入单条数据，insertMany可以插入多条
        tempelate.insert(user);
    }

    @Test
    public void findTest() {

        // 1、根据用户 username查询
//        Criteria all = Criteria.where("favorites.cites").all(Arrays.asList("东莞"));
//
//        List<User> users = tempelate.find(Query.query(all), User.class);
//        if (!CollectionUtils.isEmpty(users)) {
//            users.forEach( obj -> {
//                System.out.println("输出对象："+obj.toString());
//            });
//        }

        // 2、根据名称模糊查询，或者国家为中国、USA的数据
        // 2、db.users.find({ "$and" : [ { "username" : { "$regex" : ".*s.*"}} , { "$or" : [ { "country" : "中国"} , { "country" : "USA"}]}]})
        String regexStr = ".*李.*";
        // 匹配username
        Criteria regex = Criteria.where("username").regex(regexStr);
        Criteria or1 = Criteria.where("country").is("中国");
        Criteria or2 = Criteria.where("country").is("USA");
        Criteria or = new Criteria().orOperator(or1, or2);

        // 将查询条件拼接在一起
        Query query = Query.query(new Criteria().andOperator(regex, or));
        List<User> users1 = tempelate.find(query, User.class);
        if (!CollectionUtils.isEmpty(users1)) {
            System.out.println("集合大小: "+users1.size());
            users1.forEach( obj -> {
                System.out.println("输出对象："+obj.toString());
            });
        }
    }

    @Test
    public void updateTest() {
        // 1、根据username = "关羽" 修改
        Criteria where = Criteria.where("username").is("关羽");
        Query query = Query.query(where);
        Update update = Update.update("age", 500);
        // 执行修改操作
        WriteResult writeResult = tempelate.updateMulti(query, update, User.class);
        System.out.println("操作记录数： "+writeResult.getN());

        // 2、根据某人喜欢的相关城市，给他对应的喜爱的电影多添加几部
        Criteria where1 = Criteria.where("favorites.cites").is("南充");
        Query query1 = Query.query(where1);
        Update update1 = new Update().addToSet("favorites.movies").each("成人IT视频", "成人MV");
        WriteResult writeResult1 = tempelate.updateMulti(query1, update1, User.class);
        System.out.println("数据："+writeResult1.getN());
    }

    @Test
    public void deleteTest() {
        // 1、指定 删除
//        Criteria where = Criteria.where("username").is("关羽");
//        Query query =  Query.query(where);
//        // 执行删除
//        WriteResult writeResult = tempelate.remove(query, User.class);
//        System.out.println("操作记录数： "+writeResult.getN());

        // 根据指定年龄范围就进行删除
        Criteria age1 = Criteria.where("age").gt(50);
        Criteria age2 = Criteria.where("age").lt(90);
        Query query1 = Query.query(new Criteria().andOperator(age1, age2));
        WriteResult remove = tempelate.remove(query1, User.class);
        System.out.println("操作记录数： "+remove.getN());
    }

    @Test
    public void findByPageTest() {
        int pageNo = 1;
        int pageSize = 10;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Criteria criteria0 = Criteria.where("username").regex(".*"+"李"+".*");
            Criteria criteria1 = Criteria.where("createTime").gte(sdf.parse("2019-03-26 00:00:00"));
            Criteria criteria2 = Criteria.where("createTime").lte(sdf.parse("2019-03-26 23:59:59"));
            // 组装分页查询条件
            Query query = Query.query(new Criteria().andOperator(criteria0, criteria1, criteria2));
            query.skip((pageNo - 1) * pageSize).limit(pageSize);
            long count = tempelate.count(query, User.class);
            System.out.println("总数 ： "+count);

            List<User> users = tempelate.find(query, User.class);
            if (!CollectionUtils.isEmpty(users)) {
                System.out.println("分页查询大小："+users.size());
            }

        } catch (ParseException e) {

        }

    }









}
