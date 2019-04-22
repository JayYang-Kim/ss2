package com.sp.bbs;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("bbs.boardController")
public class BoardController {
	 
	@RequestMapping(value="/bbs/list")
	public String list() {
		return ".bbs.list";
	}
}
