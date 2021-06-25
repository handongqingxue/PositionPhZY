package com.positionPhZY.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.positionPhZY.dao.*;
import com.positionPhZY.entity.*;
import com.positionPhZY.service.*;

@Service
public class DutyServiceImpl implements DutyService {

	@Autowired
	private DutyMapper dutyDao;

	@Override
	public int add(List<Duty> dutyList) {
		// TODO Auto-generated method stub
		int count=0;
		for (Duty duty : dutyList) {
			count=dutyDao.getCountById(duty.getId());
			if(count==0)
				count=dutyDao.add(duty);
			else
				count=dutyDao.edit(duty);
		}
		return count;
	}
}
