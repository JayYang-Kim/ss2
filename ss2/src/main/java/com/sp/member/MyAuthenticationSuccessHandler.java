package com.sp.member;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

// 로그인 성공 후 세션 및 쿠키등의 처리를 담당
// SavedRequestAwareAuthenticationSuccessHandler
public class MyAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
	@Autowired
	private MemberService memberService;

	@Override
	// 로그인 성공 시 메소드로 정보를 보내줌
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		
		// 로그인 날짜 변경
		// getName : 로그인 성공한 아이디
		memberService.updateLastLogin(authentication.getName());
		
		// 로그인 정보 저장
		Member member = memberService.readMember(authentication.getName());
		
		SessionInfo info = new SessionInfo();
		info.setUserId(member.getUserId());
		info.setUserName(member.getUserName());
		
		session.setAttribute("member", info);
		
		super.onAuthenticationSuccess(request, response, authentication);
	}
}
