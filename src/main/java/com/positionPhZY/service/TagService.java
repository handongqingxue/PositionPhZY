package com.positionPhZY.service;

import java.util.List;

import com.positionPhZY.entity.Tag;

public interface TagService {

	int add(List<Tag> tagList);

	List<Tag> select();

}
