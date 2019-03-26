package com.lq.mongodb.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.Date;

/**
 *  @author liqian
 *	MongoDB数据库的的uers集合和pojo的类名称  不相同，则需要指定@Document(collection="users")
 *  2019年2月16日18:56:48
 */
@Document(collection="users")
@Data
public class User {
	
	private ObjectId id;

	/**
	 * 可以指定数据库字段
	 */
	@Field
	private String username;
	
	private String country;
	
	private Address address;
	
	private Favorites favorites;
	
	private Integer age;
	
	private BigDecimal salary;
	
	private Float lenght;

	private Date createTime;

}
