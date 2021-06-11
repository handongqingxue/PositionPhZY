package com.positionPhZY.entity;

public class WarnTrigger {

	private Integer id;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getWarnType() {
		return warnType;
	}
	public void setWarnType(Integer warnType) {
		this.warnType = warnType;
	}
	private String name;
	private Integer warnType;
}
