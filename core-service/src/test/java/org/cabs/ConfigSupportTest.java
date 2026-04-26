package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.cabs.aop.logging.LoggingAspect;
import org.cabs.config.ApplicationProperties;
import org.cabs.config.CRLFLogConverter;
import org.cabs.config.DateTimeFormatConfiguration;
import org.cabs.config.DatabaseConfiguration;
import org.cabs.config.JacksonConfiguration;
import org.cabs.config.LocaleConfiguration;
import org.cabs.config.LoggingAspectConfiguration;
import org.cabs.config.SecurityConfiguration;
import org.cabs.config.SecurityJwtConfiguration;
import org.cabs.management.SecurityMetersService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import tech.jhipster.config.JHipsterProperties;

class ConfigSupportTest {

    @Test
    void applicationPropertiesShouldInstantiate() {
        assertNotNull(new ApplicationProperties());
    }

    @Test
    void databaseConfigurationShouldInstantiate() {
        assertNotNull(new DatabaseConfiguration());
    }

    @Test
    void dateTimeFormatConfigurationShouldRegisterIsoFormatters() {
        DateTimeFormatConfiguration configuration = new DateTimeFormatConfiguration();
        DefaultFormattingConversionService service = new DefaultFormattingConversionService(false);

        configuration.addFormatters(service);

        String printed = service.convert(Instant.parse("2026-04-26T10:15:30Z"), String.class);
        assertTrue(printed.contains("2026-04-26T10:15:30Z"));
    }

    @Test
    void jacksonConfigurationShouldCreateExpectedModules() {
        JacksonConfiguration configuration = new JacksonConfiguration();

        assertNotNull(configuration.javaTimeModule());
        assertNotNull(configuration.jdk8TimeModule());
        assertTrue(
            configuration.hibernate6Module().isEnabled(
                com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS
            )
        );
    }

    @Test
    void localeConfigurationShouldCreateEnglishCookieResolverAndLanguageInterceptor() {
        LocaleConfiguration configuration = new LocaleConfiguration();

        LocaleResolver resolver = configuration.localeResolver();
        assertTrue(resolver instanceof CookieLocaleResolver);
        MockHttpServletRequest request = new MockHttpServletRequest();
        Locale resolvedLocale = resolver.resolveLocale(request);
        assertEquals(Locale.ENGLISH, resolvedLocale);

        org.springframework.web.servlet.config.annotation.InterceptorRegistry registry =
            new org.springframework.web.servlet.config.annotation.InterceptorRegistry();
        configuration.addInterceptors(registry);
        List<Object> interceptors = ReflectionTestUtils.invokeMethod(registry, "getInterceptors");
        Object first = interceptors.get(0);
        if (first instanceof MappedInterceptor mapped) {
            first = mapped.getInterceptor();
        }
        assertTrue(first instanceof LocaleChangeInterceptor);
        assertEquals("language", ((LocaleChangeInterceptor) first).getParamName());
    }

    @Test
    void loggingAspectConfigurationShouldCreateAspect() {
        LoggingAspectConfiguration configuration = new LoggingAspectConfiguration();
        LoggingAspect aspect = configuration.loggingAspect(new MockEnvironment());
        assertNotNull(aspect);
    }

    @Test
    void securityConfigurationShouldCreatePasswordEncoderAndMvcBuilder() {
        JHipsterProperties properties = new JHipsterProperties();
        SecurityConfiguration configuration = new SecurityConfiguration(properties);

        PasswordEncoder passwordEncoder = configuration.passwordEncoder();
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);

        org.springframework.web.servlet.handler.HandlerMappingIntrospector introspector =
            new org.springframework.web.servlet.handler.HandlerMappingIntrospector();
        assertNotNull(ReflectionTestUtils.invokeMethod(configuration, "mvc", introspector));
    }

    @Test
    void securityJwtConfigurationShouldCreateEncoderDecoderAndConverter() {
        SecurityJwtConfiguration configuration = new SecurityJwtConfiguration();
        ReflectionTestUtils.setField(
            configuration,
            "jwtKey",
            "ODI3ZDg3M2IwNGM1NGQ1ZGI5MTE1YWU2ODEzMGM3NGExMTNiNGZiODRhZTQ4Mzg1NjRhZjUwMDYzODU4Nzc4YWFhZjI3ZGEyYWY1Yjc1ODAyMzE1MzNmNzUwZmUyODliNmZhMTBlNGJmYzExMGY1MTdhNmVmZGJiOTA1OGFiZDk="
        );

        JwtEncoder encoder = configuration.jwtEncoder();
        assertNotNull(encoder);

        SecurityMetersService meters = Mockito.mock(SecurityMetersService.class);
        JwtDecoder decoder = configuration.jwtDecoder(meters);
        assertNotNull(decoder);

        JwtAuthenticationConverter converter = configuration.jwtAuthenticationConverter();
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "HS512")
            .subject("alice")
            .claim("auth", List.of("ROLE_ADMIN"))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
        var authentication = converter.convert(jwt);
        assertNotNull(authentication);
        assertEquals("alice", authentication.getName());
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void securityJwtDecoderShouldTrackMalformedToken() {
        SecurityJwtConfiguration configuration = new SecurityJwtConfiguration();
        ReflectionTestUtils.setField(
            configuration,
            "jwtKey",
            "ODI3ZDg3M2IwNGM1NGQ1ZGI5MTE1YWU2ODEzMGM3NGExMTNiNGZiODRhZTQ4Mzg1NjRhZjUwMDYzODU4Nzc4YWFhZjI3ZGEyYWY1Yjc1ODAyMzE1MzNmNzUwZmUyODliNmZhMTBlNGJmYzExMGY1MTdhNmVmZGJiOTA1OGFiZDk="
        );
        SecurityMetersService meters = Mockito.mock(SecurityMetersService.class);
        JwtDecoder decoder = configuration.jwtDecoder(meters);

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> decoder.decode("not-a-jwt"));
    }

    @Test
    void crlfLogConverterShouldKeepSafeMarkerAndSafeLoggerButSanitizeOthers() {
        TestableCRLFLogConverter converter = new TestableCRLFLogConverter();
        converter.setOptionList(List.of("red"));

        ILoggingEvent safeMarkerEvent = Mockito.mock(ILoggingEvent.class);
        when(safeMarkerEvent.getMarkerList()).thenReturn(List.of(CRLFLogConverter.CRLF_SAFE_MARKER));
        when(safeMarkerEvent.getLoggerName()).thenReturn("custom.logger");
        assertEquals("hello\nworld", converter.callTransform(safeMarkerEvent, "hello\nworld"));

        ILoggingEvent safeLoggerEvent = Mockito.mock(ILoggingEvent.class);
        when(safeLoggerEvent.getMarkerList()).thenReturn(null);
        when(safeLoggerEvent.getLoggerName()).thenReturn("org.hibernate.SQL");
        assertEquals("hello\nworld", converter.callTransform(safeLoggerEvent, "hello\nworld"));

        ILoggingEvent unsafeEvent = Mockito.mock(ILoggingEvent.class);
        when(unsafeEvent.getMarkerList()).thenReturn(null);
        when(unsafeEvent.getLoggerName()).thenReturn("app.logger");
        String transformed = converter.callTransform(unsafeEvent, "a\nb\rc\td");
        assertTrue(transformed.contains("_"));
        assertTrue(converter.callIsLoggerSafe(safeLoggerEvent));
        assertTrue(!converter.callIsLoggerSafe(unsafeEvent));
        assertEquals("x", converter.callToAnsiString("x", org.springframework.boot.ansi.AnsiColor.DEFAULT));
    }

    private static class TestableCRLFLogConverter extends CRLFLogConverter {
        String callTransform(ILoggingEvent event, String in) {
            return super.transform(event, in);
        }

        boolean callIsLoggerSafe(ILoggingEvent event) {
            return super.isLoggerSafe(event);
        }

        String callToAnsiString(String in, org.springframework.boot.ansi.AnsiElement element) {
            return super.toAnsiString(in, element);
        }
    }
}
