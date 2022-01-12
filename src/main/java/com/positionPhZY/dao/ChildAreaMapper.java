package com.positionPhZY.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY.entity.*;

public interface ChildAreaMapper {

	int add(ChildArea childArea);

	int edit(ChildArea childArea);

	List<ChildArea> querySelectData();

	int getCountById(@Param("id")Integer id);

}
