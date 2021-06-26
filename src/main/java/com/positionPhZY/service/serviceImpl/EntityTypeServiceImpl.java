package com.positionPhZY.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.positionPhZY.dao.*;
import com.positionPhZY.entity.*;
import com.positionPhZY.service.*;

@Service
public class EntityTypeServiceImpl implements EntityTypeService {

	@Autowired
	private EntityTypeMapper entityTypeDao;

	@Override
	public int add(List<EntityType> entityTypeList) {
		// TODO Auto-generated method stub
		int count=0;
		for (EntityType entityType : entityTypeList) {
			if(entityTypeDao.getCountById(entityType.getId())==0)
				count+=entityTypeDao.add(entityType);
			else
				count+=entityTypeDao.edit(entityType);
		}
		return count;
	}
}
