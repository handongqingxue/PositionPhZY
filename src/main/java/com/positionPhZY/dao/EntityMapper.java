package com.positionPhZY.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY.entity.*;

public interface EntityMapper {

	int add(Entity entity);

	List<Entity> querySelectData(@Param("entityType")String entityType);

}
