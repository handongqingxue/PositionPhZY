package com.positionPhZY.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.positionPhZY.dao.*;
import com.positionPhZY.entity.Location;
import com.positionPhZY.service.*;

@Service
public class LocationServiceImpl implements LocationService {

	@Autowired
	private LocationMapper locationDao;

	@Override
	public int add(Location location) {
		// TODO Auto-generated method stub
		int count=locationDao.getCountByUid(location.getUid());
		if(count==0)
			locationDao.add(location);
		else
			count=locationDao.edit(location);
		return count;
	}

	@Override
	public List<Location> selectSSDWCanvasData() {
		// TODO Auto-generated method stub
		return locationDao.selectSSDWCanvasData();
	}
}
