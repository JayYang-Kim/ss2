package com.sp.member;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("member.memberController")
public class MemberController {
	@Autowired
	private MemberService service;
	@Autowired
	private BCryptPasswordEncoder bcryptEncoder;
	
	// 변경할 시작 부분 ----------------------------------------------------------------------
	@RequestMapping(value="/member/login", method=RequestMethod.GET)
	public String loginForm(String login_error, Model model) {
		String msg = "";
		boolean isLoginError = login_error != null;
		
		if(isLoginError) {
			msg = "아이디 또는 비밀번호가 일치하지 않습니다.";
		}
		
		model.addAttribute("message", msg);
		
		// 로그인 폼
		return ".member.login";
	}
	
	// 밑의 경로는 시큐리티로 처리
	/*@RequestMapping(value="/member/login_check", method=RequestMethod.POST)
	public String loginSubmit(
			@RequestParam String userId,
			@RequestParam String userPwd,
			Model model,
			HttpSession session
			) throws Exception {
		// 로그인 처리
		Member dto = service.readMember(userId);
		
		if(dto==null || (! dto.getUserPwd().equals(userPwd))) {
			model.addAttribute("message", "아이디 또는 패스워드가 일치하지 않습니다.");
			return ".member.login";
		}
		
		// 로그인 날짜 변경
		service.updateLastLogin(dto.getUserId());

		// 로그인 정보를 세션에 저장
		SessionInfo info = new SessionInfo();
		info.setUserId(dto.getUserId());
		info.setUserName(dto.getUserName());
		session.setAttribute("member", info);
		
		return "redirect:/";
	}	

	@RequestMapping(value="/member/logout")
	public String logout(HttpSession session) throws Exception {
		// 로그인 정보를 세션에서 삭제 한다.
		session.removeAttribute("member");
		session.invalidate();
		
		return "redirect:/";
	}*/
	
	// 권한이 없는 경우 화면
	@RequestMapping(value="/member/noAuthorized")
	public String noAuth() {
		return ".member.noAuthorized";
	}
	
	// 세션 만료 시 화면
	@RequestMapping(value="/member/expired")
	public String expired() {
		return ".member.expired";
	}
	
	// 변경할 끝 부분 ----------------------------------------------------------------------

	@RequestMapping(value="/member/member", method=RequestMethod.GET)
	public String createdForm(Model model) throws Exception {
		// 회원 가입 폼
		model.addAttribute("mode", "created");
		return ".member.member";
	}

	@RequestMapping(value="/member/member", method=RequestMethod.POST)
	public String createdSubmit(Member member, Model model) throws Exception {
		// 회원 가입
		
		// 패스워드 암호화
		String pwd = bcryptEncoder.encode(member.getUserPwd());
		member.setUserPwd(pwd);

		try {
			service.insertMember(member);
		}catch(Exception e) {
			model.addAttribute("message", "회원가입이 실패했습니다. 다른 아이디로 다시 가입하시기 바랍니다.");
			model.addAttribute("mode", "created");
			return ".member.member";
		}
		
		StringBuffer sb=new StringBuffer();
		sb.append(member.getUserName()+ "님의 회원 가입이 정상적으로 처리되었습니다.<br>");
		sb.append("메인화면으로 이동하여 로그인 하시기 바랍니다.<br>");
		
		model.addAttribute("title", "회원 가입");
		model.addAttribute("message", sb.toString());
		
		return ".member.complete";
	}
	
	@RequestMapping(value="/member/userIdCheck")
	@ResponseBody
	public Map<String, Object> userIdCheck(
			@RequestParam(value="userId") String userId
			) throws Exception {
		// 아이디 중복 검사
		
		Member member = service.readMember(userId);
		
		String passed = "true";
		if(member != null)
			passed = "false";
		
		Map<String, Object> map=new HashMap<>();
		map.put("passed", passed);
		return map;
	}
	
	@RequestMapping(value="/member/pwd", method=RequestMethod.GET)
	public String pwdForm(
			String dropout,
			Model model,
			HttpSession session
			) {
		// 패스워드 확인 폼
		// 밑의 경로는 시큐리티로 처리
		/*SessionInfo info=(SessionInfo)session.getAttribute("member");
		if(info==null) {
			return "redirect:/member/login";
		}*/
		
		if(dropout==null) {
			model.addAttribute("title", "정보수정");
			model.addAttribute("mode", "update");
		} else {
			model.addAttribute("title", "회원탈퇴");
			model.addAttribute("mode", "dropout");
		}
		return ".member.pwd";
	}
	
	@RequestMapping(value="/member/pwd", method=RequestMethod.POST)
	public String pwdSubmit(
			@RequestParam(value="userPwd") String userPwd,
			@RequestParam(value="mode") String mode,
			Model model,
			HttpSession session
	     ) {
		// 패스워드 검사
		
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		
		Member dto=service.readMember(info.getUserId());
		if(dto==null) {
			session.invalidate();
			return "redirect:/";
		}
		
		// 패스워드 암호화
		boolean bPwd = bcryptEncoder.matches(userPwd, dto.getUserPwd());
		
		if(bPwd) {
			if(mode.equals("update")) {
				model.addAttribute("dto", dto);
				model.addAttribute("mode", "update");
				model.addAttribute("title", "회원 정보 수정");
				return ".member.member";
			} else if(mode.equals("dropout")) {
				// 회원 탈퇴
				
				if(! info.getUserId().equals("admin"))
					service.deleteMember(info.getUserId());
				
				session.removeAttribute("member");
				session.invalidate();

				model.addAttribute("title", "회원 탈퇴");
				
				StringBuffer sb=new StringBuffer();
				sb.append(dto.getUserName()+ "님의 회원 탈퇴 처리가 정상적으로 처리되었습니다.<br>");
				sb.append("메인화면으로 이동 하시기 바랍니다.<br>");
				model.addAttribute("message", sb.toString());
				
				return ".member.complete";
			}
		}
		
		model.addAttribute("message", "패스워드가 일치하지 않습니다.");
		if(mode.equals("update")) {
			model.addAttribute("title", "정보 수정");
			model.addAttribute("mode", "update");
		} else {
			model.addAttribute("title", "회원 탈퇴");
			model.addAttribute("mode", "dropout");
		}
		return ".member.pwd";
	}
	
	// 수정완료
	@RequestMapping(value="/member/update", 
			method=RequestMethod.POST)
	public String updateSubmit(
			Member member,
			Model model,
			HttpSession session
			) throws Exception {	
		// 패스워드 암호화
		String pwd = bcryptEncoder.encode(member.getUserPwd());
		member.setUserPwd(pwd);
		
		service.updateMember(member);
		
		StringBuffer sb=new StringBuffer();
		sb.append(member.getUserName()+ "님의 회원정보가 정상적으로 변경되었습니다.<br>");
		sb.append("메인화면으로 이동 하시기 바랍니다.<br>");
		
		model.addAttribute("title", "회원 정보 수정");
		model.addAttribute("message", sb.toString());
		
		return ".member.complete";
	}
}
