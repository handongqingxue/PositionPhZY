package com.positionPhZY.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/phone")
public class PhoneController {

	@RequestMapping(value="/goLogin")
	public String goLogin() {
		
		return "phone/login";
	}

	@RequestMapping(value="/goIndex")
	public String goIndex() {
		
		return "phone/index";
	}
}
