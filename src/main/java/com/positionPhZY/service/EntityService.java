package com.positionPhZY.service;

import java.util.List;

import com.positionPhZY.entity.*;

public interface EntityService {

	int add(List<Entity> entityList);

	List<Entity> querySelectData(String entityType);

}
