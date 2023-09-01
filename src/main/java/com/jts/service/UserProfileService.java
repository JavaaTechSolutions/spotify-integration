package com.jts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jts.entity.UserDetails;
import com.jts.entity.UserDetailsRepository;

import se.michaelthelin.spotify.model_objects.specification.User;

@Service
public class UserProfileService {

	@Autowired
	private UserDetailsRepository userDetailsRepository;

	public UserDetails insertOrUpdateUserDetails(User user, String accessToken, String refreshToken) {
		return null;
		// Create Your logic
	}
}
