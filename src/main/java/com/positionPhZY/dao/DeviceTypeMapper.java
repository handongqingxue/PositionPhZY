package com.positionPhZY.dao;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY.entity.*;

public interface DeviceTypeMapper {

	int getCountById(@Param("id")String id);

	int add(DeviceType deviceType);

	int edit(DeviceType deviceType);

}
