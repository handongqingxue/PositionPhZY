package com.positionPhZY.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.positionPhZY.dao.*;
import com.positionPhZY.entity.*;
import com.positionPhZY.service.*;

@Service
public class DeviceTypeServiceImpl implements DeviceTypeService {

	@Autowired
	private DeviceTypeMapper deviceTypeDao;

	@Override
	public int add(List<DeviceType> deviceTypeList) {
		// TODO Auto-generated method stub
		int count=0;
		for (DeviceType deviceType : deviceTypeList) {
			if(deviceTypeDao.getCountById(deviceType.getId())==0)
				count+=deviceTypeDao.add(deviceType);
			else
				count+=deviceTypeDao.edit(deviceType);
		}
		return count;
	}
}
