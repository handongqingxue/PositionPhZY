package com.positionPhZY.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY.entity.*;

public interface EntityTypeMapper {

	int add(EntityType entityType);

	int edit(EntityType entityType);

	int getCountById(@Param("id")String id);

	List<EntityType> select();

}
