package com.positionPhZY.dao;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY.entity.*;

public interface DutyMapper {

	int add(Duty duty);

	int getCountById(@Param("id")Integer id);

	int edit(Duty duty);

}
