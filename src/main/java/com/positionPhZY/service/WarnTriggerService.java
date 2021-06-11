package com.positionPhZY.service;

import java.util.List;

import com.positionPhZY.entity.*;

public interface WarnTriggerService {

	int add(List<WarnTrigger> warnTriggerList);

	List<WarnTrigger> select();

}
