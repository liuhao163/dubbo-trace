# dubbo-trace
提供dubbo的链路跟踪,如果開啟功能请在spring-boot启动时候增加注解@EnableTraceAutoConfigurationProperties

```java
dubbo.trace.enabled=true
dubbo.trace.connectTimeout=1000
dubbo.trace.readTimeout=1000
dubbo.trace.zipkinUrl=http://127.0.0.1:9411 #配置zipkin的地址
```

# provider配置
```java
    <dubbo:service interface="xxxxx" version="xxxx" filter="traceProviderFilter"
                   ref="xxxxServiceImpl" />
```

# consumer配置
```java
 <dubbo:reference id="xxxxx" interface="xxxxx" filter="traceConsumerFilter" version="xxxxx"/>
```