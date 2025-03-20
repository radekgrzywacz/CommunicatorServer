package com.communicator.config.auth;

import com.communicator.entity.Token;
import com.communicator.repositories.TokenRepository;
import com.communicator.services.utils.JWTUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.util.List;

/**
 * CustomLogoutHandler is responsible for handling user logout by invalidating JWT tokens.
 * It implements the {@link org.springframework.security.web.authentication.logout.LogoutHandler}
 * interface.
 */
@Configuration
public class CustomLogoutHandler implements LogoutHandler {

    private final TokenRepository tokenRepository;
    private final JWTUtils jwtUtils;

    /**
     * Constructor for CustomLogoutHandler.
     *
     * @param tokenRepo    repository for managing tokens
     * @param jwtUtilsProp utility class for handling JWT operations
     */
    public CustomLogoutHandler(final TokenRepository tokenRepo, final JWTUtils jwtUtilsProp) {
        this.tokenRepository = tokenRepo;
        this.jwtUtils = jwtUtilsProp;
    }

    /**
     * Handles the logout process by removing all tokens associated with the authenticated user.
     *
     * @param request        the HTTP request
     * @param response       the HTTP response
     * @param authentication the authentication object of the logged-in user
     */
    @Override
    public void logout(final HttpServletRequest request, final HttpServletResponse response,
                       final Authentication authentication) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        final int tokenStartIndex = 7;
        String token = authHeader.substring(tokenStartIndex);
        String phoneNumber = jwtUtils.extractPhoneNumber(token);

        List<Token> storedTokens = tokenRepository.findAllTokensByPhoneNumber(phoneNumber)
                .orElseThrow();

        if (!storedTokens.isEmpty()) {
            tokenRepository.deleteAll(storedTokens);
        }
    }
}

