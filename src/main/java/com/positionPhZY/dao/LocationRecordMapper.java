package com.positionPhZY.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY.entity.LocationRecord;

public interface LocationRecordMapper {

	int getCountById(@Param("id")Integer id);

	int add(LocationRecord locationRecord);

	List<LocationRecord> select(@Param("startTimeLong")Long startTimeLong, @Param("endTimeLong")Long endTimeLong);

}
