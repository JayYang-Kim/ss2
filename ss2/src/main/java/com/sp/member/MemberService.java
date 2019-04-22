package com.sp.member;

import java.util.List;

public interface MemberService {
	public Member readMember(String userId);
	
	public void insertMember(Member dto) throws Exception ;
	
	public void updateMember(Member dto) throws Exception;
	public int updateLastLogin(String userId);
	
	public int deleteMember(String userId);
	
	public int insertAuthority(Member dto);
	public int updateAuthority(Member dto);
	public List<Member> listAuthority(String userId);
}
