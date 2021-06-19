package com.positionPhZY.service;

import java.util.List;

import com.positionPhZY.entity.Location;

public interface LocationService {

	int add(Location location);

	List<Location> selectSSDWCanvasData();

}
