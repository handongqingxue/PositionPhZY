package com.positionPhZY.service.serviceImpl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.positionPhZY.dao.*;
import com.positionPhZY.entity.*;
import com.positionPhZY.service.*;
import com.positionPhZY.util.date.DateUtil;

@Service
public class WarnTriggerServiceImpl implements WarnTriggerService {

	@Autowired
	private WarnTriggerMapper warnTriggerDao;

	@Override
	public int add(List<WarnTrigger> warnTriggerList) {
		// TODO Auto-generated method stub
		int count=0;
		for (WarnTrigger warnTrigger : warnTriggerList) {
			count+=warnTriggerDao.add(warnTrigger);
		}
		return count;
	}

	@Override
	public List<WarnTrigger> select() {
		// TODO Auto-generated method stub
		return warnTriggerDao.select();
	}
}
