package com.positionPhZY.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY.entity.Location;

public interface LocationMapper {

	int add(Location location);

	int getCountByUid(@Param("uid")String uid);

	int edit(Location location);

	List<Location> selectSSDWCanvasData(@Param("floor")Integer floor);

}
