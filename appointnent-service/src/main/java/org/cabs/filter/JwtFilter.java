//package org.cabs.filter;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.Collections;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.cabs.util.JwtUtil;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//@Component
//@Slf4j
//public class JwtFilter extends OncePerRequestFilter {
//
//    private final JwtUtil jwtUtil;
//
//    public JwtFilter(JwtUtil jwtUtil) {this.jwtUtil = jwtUtil;}
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//        HttpServletResponse response,
//        FilterChain filterChain)
//        throws ServletException, IOException {
//
//        String authHeader = request.getHeader("Authorization");
//        log.info("Authorization header = {}", authHeader);
//
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7);
//
//            // validate token (shared secret or public key)
//            if (jwtUtil.validate(token)) {
//
//                String username = jwtUtil.extractUsername(token);
//
//                UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(
//                        username,
//                        null,
//                        Collections.emptyList()
//                    );
//
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//
//                log.info("User authenticated: {}", username);
//            }
//            log.info("Token valid = {}", jwtUtil.validate(token));
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}
