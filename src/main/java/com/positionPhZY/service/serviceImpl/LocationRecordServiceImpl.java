package com.positionPhZY.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.positionPhZY.dao.*;
import com.positionPhZY.entity.*;
import com.positionPhZY.service.*;

@Service
public class LocationRecordServiceImpl implements LocationRecordService {

	@Autowired
	private LocationRecordMapper locationRecordDao;

	@Override
	public int add(List<LocationRecord> locationRecordList) {
		// TODO Auto-generated method stub
		int count=0;
		for (LocationRecord locationRecord : locationRecordList) {
			if(locationRecordDao.getCountById(locationRecord.getId())==0)
				count+=locationRecordDao.add(locationRecord);
		}
		return count;
	}

	@Override
	public List<LocationRecord> select(Long startTimeLong, Long endTimeLong) {
		// TODO Auto-generated method stub
		return locationRecordDao.select(startTimeLong,endTimeLong);
	}
}
