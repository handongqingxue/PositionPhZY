package com.positionPhZY.service;

import java.util.List;

import com.positionPhZY.entity.*;

public interface DeviceTypeService {

	int add(List<DeviceType> deviceTypeList);

	List<DeviceType> select();

}
