package com.positionPhZY.dao;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY.entity.Location;

public interface LocationMapper {

	int add(Location location);

	int getCountByUid(@Param("uid")String uid);

	int edit(Location location);

}