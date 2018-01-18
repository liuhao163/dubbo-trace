package com.ericliu.dubbo.trace.zipkin.config;

import com.ericliu.dubbo.trace.zipkin.annotation.EnableTraceAutoConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import com.ericliu.dubbo.trace.zipkin.context.TraceContext;

import javax.annotation.PostConstruct;

/*
* 日志追踪自动配置开关
* 作者：姜敏
* 版本：V1.0
* 创建日期：2017/4/13
* 修改日期:2017/4/13
*/
@Configuration
@ConditionalOnBean(annotation = EnableTraceAutoConfigurationProperties.class)
@AutoConfigureAfter(SpringBootConfiguration.class)
@EnableConfigurationProperties(TraceConfig.class)
public class EnableTraceAutoConfiguration {

    @Autowired
    private TraceConfig traceConfig;

    @PostConstruct
    public void init() throws Exception {
        TraceContext.init(this.traceConfig);
    }
}
