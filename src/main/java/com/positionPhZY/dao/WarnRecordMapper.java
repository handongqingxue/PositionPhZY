package com.positionPhZY.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY.entity.WarnRecord;

public interface WarnRecordMapper {

	List<WarnRecord> select();

	List<WarnRecord> select(@Param("startTime")String startTime, @Param("endTime")String endTime);

	int add(WarnRecord warnRecord);

	List<WarnRecord> selectBarChartDateData(@Param("startDate")String startDate, @Param("endDate")String endDate);

	List<WarnRecord> selectBarChartWeekData(@Param("startDate")String startDate, @Param("endDate")String endDate);

	List<WarnRecord> selectBarChartMonthData(@Param("startDate")String startDate, @Param("endDate")String endDate);

	List<WarnRecord> selectPieChartData(@Param("startDate")String startDate, @Param("endDate")String endDate);

	int getCountById(@Param("id")Integer id);

}
