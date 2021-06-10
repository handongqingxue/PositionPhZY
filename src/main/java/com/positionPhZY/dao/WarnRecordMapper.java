package com.positionPhZY.dao;

import java.util.List;

import com.positionPhZY.entity.WarnRecord;

public interface WarnRecordMapper {

	List<WarnRecord> select();

	int add(WarnRecord warnRecord);

}
