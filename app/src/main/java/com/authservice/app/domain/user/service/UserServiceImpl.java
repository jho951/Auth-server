package com.authservice.app.domain.user.service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.PersistenceException;

import com.authservice.app.domain.auth.entity.Auth;
import com.authservice.app.domain.auth.repository.AuthRepository;
import com.authservice.app.domain.user.entity.User;
import com.authservice.app.domain.user.dto.UserRequest;
import com.authservice.app.domain.user.dto.UserResponse;
import com.authservice.app.domain.user.entity.UserSocial;
import com.authservice.app.common.base.constant.ErrorCode;
import com.authservice.app.common.base.exception.GlobalException;
import com.authservice.app.domain.user.repository.UserRepository;
import com.authservice.app.domain.user.repository.UserSocialRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final PasswordEncoder passwordEncoder;
	private final AuthRepository authRepository;
	private final UserRepository userRepository;
	private final UserSocialRepository userSocialRepository;

	/**
	 * @param dto 회원가입 유저 정보
	 * @return user 테이블에 유저 정보 저장
	 */
	private User saveUser(UserRequest.UserCreateRequest dto) {
		User user = dto.toUserEntity();
		return userRepository.save(user);
	}

	/**
	 * @param dto,user 회원가입 유저 정보, user 엔티티
	 * @return auth 테이블에 유저 인증 정보 저장
	 */
	private Auth saveUserAuth(UserRequest.UserCreateRequest dto, User user) {
		Auth auth = Auth.builder()
			.user(user)
			.passwordHash(passwordEncoder.encode(dto.getPassword()))
			.build();
		return authRepository.save(auth);
	}

	/**
	 * @param dto,user 회원가입 유저 정보, user 엔티티
	 * @return user_social 테이블에 유저 정보 저장
	 */
	private UserSocial saveUserSocial(UserRequest.UserCreateRequest dto, User user) {
		UserSocial userSocial = dto.toSocialEntity(user);
		return userSocialRepository.save(userSocial);
	}

	/**
	 * @param dto user 생성
	 * @return UserResponse.UserCreateResponse
	 */
	@Transactional
	@Override
	public UserResponse.UserCreateResponse create(UserRequest.UserCreateRequest dto) {
		try {
			User user = saveUser(dto);
			saveUserAuth(dto, user);
			UserSocial userSocial = saveUserSocial(dto, user);
			return UserResponse.UserCreateResponse.from(user, userSocial);
		} catch (DataIntegrityViolationException e) {
			throw new GlobalException(ErrorCode.BAD_REQUEST_SAMPLE_DATA);
		} catch (PersistenceException e) {
			throw new GlobalException(ErrorCode.INVALID_REQUEST_DATA);
		}
	}


	// /**
	//  * @param dto user 수정
	//  * @return UserResponse.UserUpdateResponse
	//  */
	// @Transactional
	// @Override
	// public UserResponse.UserUpdateResponse update(UserRequest.UserUpdateRequest dto) {
	// 	try {
	// 		User user = userRepository.findById(dto.getId())
	// 			.orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
	// 	} catch (DataIntegrityViolationException e) {
	// 		log.warn("사용자 생성 중 무결성 제약 위반: {}", e.getMessage());
	// 		throw new GlobalException(ErrorCode.BAD_REQUEST_SAMPLE_DATA);
	// 	} catch (PersistenceException e) {
	// 		log.error("사용자 생성 중 DB 예외 발생", e);
	// 		throw new GlobalException(ErrorCode.INVALID_REQUEST_DATA);
	// 	}
	// }
}
