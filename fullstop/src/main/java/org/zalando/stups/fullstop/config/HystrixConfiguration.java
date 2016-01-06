package org.zalando.stups.fullstop.config;

import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableHystrix
@EnableAspectJAutoProxy
public class HystrixConfiguration {
}
