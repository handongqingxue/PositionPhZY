package com.positionPhZY.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.positionPhZY.dao.*;
import com.positionPhZY.entity.*;
import com.positionPhZY.service.*;

@Service
public class ChildAreaServiceImpl implements ChildAreaService {

	@Autowired
	private ChildAreaMapper childAreaDao;

	@Override
	public int add(List<ChildArea> childAreaList) {
		// TODO Auto-generated method stub
		int count=0;
		for (ChildArea childArea : childAreaList) {
			if(childAreaDao.getCountById(childArea.getId())==0)
				count+=childAreaDao.add(childArea);
			else
				count+=childAreaDao.edit(childArea);
		}
		return count;
	}

	@Override
	public List<ChildArea> querySelectData() {
		// TODO Auto-generated method stub
		return childAreaDao.querySelectData();
	}
}