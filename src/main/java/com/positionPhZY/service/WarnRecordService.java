package com.positionPhZY.service;

import java.util.List;

import com.positionPhZY.entity.*;

public interface WarnRecordService {

	List<WarnRecord> select();

	List<WarnRecord> select(String todayDate, String nowTime);

	int add(List<WarnRecord> warnRecordList);

	List<WarnRecord> selectBarChartData(String startTime, String endTime, String flag);

}
