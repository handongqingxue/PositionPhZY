package com.positionPhZY.service;

import java.util.List;

import com.positionPhZY.entity.*;

public interface LocationRecordService {

	int add(List<LocationRecord> locationRecordList);

	List<LocationRecord> select(Long startTimeLong, Long endTimeLong);

}
