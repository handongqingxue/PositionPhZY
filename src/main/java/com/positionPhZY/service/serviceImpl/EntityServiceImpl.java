package com.positionPhZY.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.positionPhZY.dao.*;
import com.positionPhZY.entity.*;
import com.positionPhZY.service.*;

@Service
public class EntityServiceImpl implements EntityService {

	@Autowired
	private EntityMapper entityDao;

	@Override
	public int add(List<Entity> entityList) {
		// TODO Auto-generated method stub
		int count=0;
		for (Entity entity : entityList) {
			if(entityDao.getCountById(entity.getId())==0)
				count+=entityDao.add(entity);
			else
				count+=entityDao.edit(entity);
		}
		return count;
	}

	@Override
	public List<Entity> querySelectData(String entityType) {
		// TODO Auto-generated method stub
		return entityDao.querySelectData(entityType);
	}
}
