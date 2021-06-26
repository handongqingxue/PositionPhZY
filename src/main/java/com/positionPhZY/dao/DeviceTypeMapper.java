package com.positionPhZY.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY.entity.*;

public interface DeviceTypeMapper {

	int getCountById(@Param("id")String id);

	int add(DeviceType deviceType);

	int edit(DeviceType deviceType);

	List<DeviceType> select();

}
