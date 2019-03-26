package com.lq.mongodb.entity;

import lombok.Data;

import java.util.List;

@Data
public class Favorites {

	private List<String> movies;
	private List<String> cites;
}
