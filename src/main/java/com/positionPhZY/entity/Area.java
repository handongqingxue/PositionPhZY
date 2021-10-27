package com.positionPhZY.entity;

public class Area {

	private Integer id;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Float getPicWidth() {
		return picWidth;
	}
	public void setPicWidth(Float picWidth) {
		this.picWidth = picWidth;
	}
	public Float getPicHeight() {
		return picHeight;
	}
	public void setPicHeight(Float picHeight) {
		this.picHeight = picHeight;
	}
	public String getVirtualPath() {
		return virtualPath;
	}
	public void setVirtualPath(String virtualPath) {
		this.virtualPath = virtualPath;
	}
	private Float picWidth;
	private Float picHeight;
	private String virtualPath;
}
