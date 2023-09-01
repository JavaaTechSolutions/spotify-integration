package com.jts.controller;

import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jts.config.SpotifyConfiguration;
import com.jts.entity.UserDetails;
import com.jts.entity.UserDetailsRepository;
import com.jts.service.UserProfileService;

import jakarta.servlet.http.HttpServletResponse;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.SavedAlbum;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.library.GetCurrentUsersSavedAlbumsRequest;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

@RestController
@RequestMapping("/api")
public class SpotifyController {

	@Value("${custom.server.ip}")
	private String customIp;
	
	@Autowired
	private UserProfileService userProfileService;

	@Autowired
	private SpotifyConfiguration spotifyConfiguration;
	
	@Autowired
	private UserDetailsRepository userDetailsRepository;
	
	@GetMapping("login")
	public String spotifyLogin() {
		SpotifyApi object = spotifyConfiguration.getSpotifyObject();
		
		AuthorizationCodeUriRequest authorizationCodeUriRequest = object.authorizationCodeUri()
				.scope("user-library-read")
				.show_dialog(true)
				.build();
		
		final URI uri = authorizationCodeUriRequest.execute();
		return uri.toString();
	}

	@GetMapping(value = "get-user-code")
	public void getSpotifyUserCode(@RequestParam("code") String userCode, HttpServletResponse response)	throws IOException {
		SpotifyApi object = spotifyConfiguration.getSpotifyObject();
		
		AuthorizationCodeRequest authorizationCodeRequest = object.authorizationCode(userCode).build();
		User user = null;
		
		try {
			final AuthorizationCodeCredentials authorizationCode = authorizationCodeRequest.execute();

			object.setAccessToken(authorizationCode.getAccessToken());
			object.setRefreshToken(authorizationCode.getRefreshToken());
			
			final GetCurrentUsersProfileRequest getCurrentUsersProfile = object.getCurrentUsersProfile().build();
			user = getCurrentUsersProfile.execute();

			userProfileService.insertOrUpdateUserDetails(user, authorizationCode.getAccessToken(), authorizationCode.getRefreshToken());
		} catch (Exception e) {
			System.out.println("Exception occured while getting user code: " + e);
		}

		response.sendRedirect(customIp + "/home?id="+user.getId());
	}
	
	@GetMapping(value = "home")
	public String home(@RequestParam String userId) {
		try {

			return userId;
		} catch (Exception e) {
			System.out.println("Exception occured while landing to home page: " + e);
		}

		return null;
	}
	
	@GetMapping(value = "user-saved-album")
	public SavedAlbum[] getCurrentUserSavedAlbum(@RequestParam String userId) {
		UserDetails userDetails = userDetailsRepository.findByRefId(userId);

		SpotifyApi object = spotifyConfiguration.getSpotifyObject();
		object.setAccessToken(userDetails.getAccessToken());
		object.setRefreshToken(userDetails.getRefreshToken());
		
		final GetCurrentUsersSavedAlbumsRequest getUsersTopArtistsRequest = object.getCurrentUsersSavedAlbums()
				.limit(50)
				.offset(0)
				.build();

		try {
			final Paging<SavedAlbum> artistPaging = getUsersTopArtistsRequest.execute();

			return artistPaging.getItems();
		} catch (Exception e) {
			System.out.println("Exception occured while fetching user saved album: " + e);
		}
		
		return new SavedAlbum[0];
	}

	@GetMapping(value = "user-top-songs")
	public Track[] getUserTopTracks(@RequestParam String userId) {
		UserDetails userDetails = userDetailsRepository.findByRefId(userId);
		
		SpotifyApi object = spotifyConfiguration.getSpotifyObject();
		object.setAccessToken(userDetails.getAccessToken());
		object.setRefreshToken(userDetails.getRefreshToken());
		
		final GetUsersTopTracksRequest getUsersTopTracksRequest = object.getUsersTopTracks()
				.time_range("medium_term")
				.limit(10)
				.offset(0)
				.build();

		try {
			final Paging<Track> trackPaging = getUsersTopTracksRequest.execute();

			return trackPaging.getItems();
		} catch (Exception e) {
			System.out.println("Exception occured while fetching top songs: " + e);
		}
		
		return new Track[0];
	}
}
