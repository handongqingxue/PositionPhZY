package com.positionPhZY.dao;

import java.util.List;

import com.positionPhZY.entity.WarnTrigger;

public interface WarnTriggerMapper {

	int add(WarnTrigger warnTrigger);

	List<WarnTrigger> select();

}
