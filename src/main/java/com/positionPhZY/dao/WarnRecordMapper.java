package com.positionPhZY.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY.entity.WarnRecord;

public interface WarnRecordMapper {

	List<WarnRecord> select();

	List<WarnRecord> select(@Param("startTime")String startTime, @Param("endTime")String endTime);

	int add(WarnRecord warnRecord);

	List<WarnRecord> selectBarChartDateData(@Param("startTime")String startTime, @Param("endTime")String endTime);

	List<WarnRecord> selectBarChartWeekData(@Param("startTime")String startTime, @Param("endTime")String endTime);

	List<WarnRecord> selectBarChartMonthData(@Param("startTime")String startTime, @Param("endTime")String endTime);

}
