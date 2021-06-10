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
public class WarnRecordServiceImpl implements WarnRecordService {

	@Autowired
	private WarnRecordMapper warnRecordDao;

	@Override
	public List<WarnRecord> select() {
		// TODO Auto-generated method stub
		return warnRecordDao.select();
	}

	@Override
	public int add(List<WarnRecord> warnRecordList) {
		// TODO Auto-generated method stub
		int count=0;
		for (WarnRecord warnRecord : warnRecordList) {
			Long raiseTime = warnRecord.getRaiseTime();
			if(StringUtils.isEmpty(String.valueOf(raiseTime)))
				warnRecord.setRaiseTimeYMD(DateUtil.convertLongToString(raiseTime));
			Long startTime = warnRecord.getStartTime();
			if(StringUtils.isEmpty(String.valueOf(startTime)))
				warnRecord.setStartTimeYMD(DateUtil.convertLongToString(startTime));
			count+=warnRecordDao.add(warnRecord);
		}
		return count;
	}
}
