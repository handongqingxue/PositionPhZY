package com.positionPhZY.service.serviceImpl;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.positionPhZY.dao.*;
import com.positionPhZY.entity.*;
import com.positionPhZY.service.*;

@Service
public class AreaServiceImpl implements AreaService {

	@Autowired
	private AreaMapper areaDao;

	@Override
	public void putInResult(JSONObject resultJO) {
		// TODO Auto-generated method stub
		org.json.JSONArray areaJA = resultJO.getJSONArray("result");
		JSONObject areaJO = areaJA.getJSONObject(0);
		Area area=areaDao.get();
		areaJO.put("picWidth", area.getPicWidth());
		areaJO.put("picHeight", area.getPicHeight());
		areaJO.put("virtualPath", area.getVirtualPath());
		areaJO.put("staffImgWidth",area.getStaffImgWidth());
		areaJO.put("staffImgHeight",area.getStaffImgHeight());
		areaJO.put("lineWidth",area.getLineWidth());
		areaJO.put("rectWidth",area.getRectWidth());
		areaJO.put("rectHeight",area.getRectHeight());
		areaJO.put("arSpace",area.getArSpace());
		areaJO.put("atSpace",area.getAtSpace());
		areaJO.put("fontSize",area.getFontSize());
		areaJO.put("fontMarginLeft",area.getFontMarginLeft());
	}
}