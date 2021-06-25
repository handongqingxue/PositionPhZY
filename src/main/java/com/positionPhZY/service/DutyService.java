package com.positionPhZY.service;

import java.util.List;
import java.util.Map;

import com.positionPhZY.entity.*;

public interface DutyService {

	int add(List<Duty> dutyList);

	List<Map<String, Object>> summaryOnlineDuty();

}
