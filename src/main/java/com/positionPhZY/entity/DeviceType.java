package com.positionPhZY.entity;

public class DeviceType {

	private String css;
	public String getCss() {
		return css;
	}
	public void setCss(String css) {
		this.css = css;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getEngineMask() {
		return engineMask;
	}
	public void setEngineMask(Integer engineMask) {
		this.engineMask = engineMask;
	}
	public Boolean getLabelChecked() {
		return labelChecked;
	}
	public void setLabelChecked(Boolean labelChecked) {
		this.labelChecked = labelChecked;
	}
	private String icon;
	private String name;
	private String id;
	private Integer engineMask;
	private Boolean labelChecked;
}
