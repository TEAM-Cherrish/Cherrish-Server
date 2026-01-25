package com.sopt.cherrish.global.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sopt.cherrish.domain.user.domain.model.User;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Spring Security의 UserDetails 구현체.
 *
 * <p>JWT 인증 후 SecurityContext에 저장되어 현재 인증된 사용자 정보를 제공합니다.
 * {@link CurrentUser} 어노테이션을 통해 컨트롤러에서 주입받을 수 있습니다.</p>
 */
@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

	private final Long userId;
	private final String name;
	private final Collection<? extends GrantedAuthority> authorities;

	/**
	 * User 엔티티로부터 UserPrincipal을 생성합니다.
	 *
	 * @param user 사용자 엔티티
	 * @return UserPrincipal 인스턴스
	 */
	public static UserPrincipal from(User user) {
		return new UserPrincipal(
			user.getId(),
			user.getName(),
			Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
		);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return String.valueOf(userId);
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
