package com.communicator.config.auth;

import com.communicator.entity.AppUser;
import com.communicator.services.utils.JWTUtils;
import com.communicator.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWTAuthFilter is a custom filter that checks for a valid JWT token in the Authorization header
 * of HTTP requests.
 * It extends {@link org.springframework.web.filter.OncePerRequestFilter}, meaning it will be
 * invoked once per request.
 * This filter is used to authenticate users based on the JWT token.
 *
 * <p>The filter performs the following steps:</p>
 * <ol>
 *   <li>Extracts the token from the Authorization header (if it exists).</li>
 *   <li>Validates the token and checks if it contains the phone number.</li>
 *   <li>If the token is valid, it retrieves the corresponding {@link AppUser}
 *   from the database
 *   and sets the user in the
 *   {@link org.springframework.security.core.context.SecurityContext}.</li>
 *   <li>Proceeds with the filter chain.</li>
 * </ol>
 *
 * <p>Annotations used:</p>
 * <ul>
 *   <li>{@link org.springframework.stereotype.Component}: Marks this class as a Spring component
 *   so it can be discovered
 *   and registered by Spring.</li>
 *   <li>{@link org.springframework.beans.factory.annotation.Autowired}: Used for dependency
 *   injection of JWT utilities and
 *   user service objects.</li>
 * </ul>
 *
 * @author Communicator
 * @see JWTUtils
 * @see com.communicator.services.UserService
 */
@Component
public class JWTAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private UserService userService;

    /**
     * Filters HTTP requests by checking the presence of a valid JWT token in the "Authorization"
     * header.
     * If the token is valid, the user is authenticated by setting an authentication object in the
     * {@link org.springframework.security.core.context.SecurityContext}.
     *
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain to proceed with the request
     * @throws ServletException if an error occurs during the servlet operation
     * @throws IOException      if an input or output exception occurs
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final int tokenStartIndex = 7;
        String token = authorizationHeader.substring(tokenStartIndex);
        String phoneNumber = jwtUtils.extractPhoneNumber(token);

        if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            AppUser appUser = userService.findByPhoneNumber(phoneNumber);


            if (jwtUtils.isTokenValid(token, appUser)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(appUser, null,
                                appUser.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
