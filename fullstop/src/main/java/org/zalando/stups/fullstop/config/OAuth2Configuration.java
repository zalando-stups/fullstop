package org.zalando.stups.fullstop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.zalando.stups.oauth2.spring.security.expression.ExtendedOAuth2WebSecurityExpressionHandler;

import static org.springframework.security.config.http.SessionCreationPolicy.NEVER;

/**
 * @author  jbellmann
 */
@Configuration
@EnableResourceServer
public class OAuth2Configuration extends ResourceServerConfigurerAdapter {

    @Value("${spring.oauth2.resource.tokenInfoUri}")
    private String tokenInfoUri;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        // add support for #oauth2.hasUidScopeAndAnyRealm() expressions
        resources
                .expressionHandler(new ExtendedOAuth2WebSecurityExpressionHandler());
    }


    /**
     * Configure scopes for specific controller/httpmethods/roles here.
     */
    @Override
    public void configure(final HttpSecurity http) throws Exception {

        http.sessionManagement().sessionCreationPolicy(NEVER)

            // configure form login
            .and().formLogin().disable()

            // configure logout
                .logout().disable()

                .authorizeRequests()
                .antMatchers("/").access("#oauth2.hasUidScopeAndAnyRealm('/employees', '/services')")
                .antMatchers("/api/**").access("#oauth2.hasUidScopeAndAnyRealm('/employees', '/services')")
                .antMatchers("/s3/**").access("#oauth2.hasUidScopeAndAnyRealm('/employees', '/services')")
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/swagger-resources").permitAll()
                .antMatchers("/api-docs").permitAll();

    }

    @Bean
    public ResourceServerTokenServices customResourceTokenServices() {
// return new TokenInfoResourceServerTokenServices(tokenInfoUri, "what_here");
        return new BearerNoneTokenInfoResourceServerTokenServices(tokenInfoUri);
    }
}
