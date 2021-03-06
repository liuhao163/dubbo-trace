package com.ericliu.dubbo.trace.zipkin.filter;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.ericliu.dubbo.trace.zipkin.context.TraceContext;
import com.ericliu.dubbo.trace.zipkin.utils.IdUtils;
import com.ericliu.dubbo.trace.zipkin.utils.NetworkUtils;
import com.google.common.base.Stopwatch;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
* 服务端日志过滤器
* 作者：姜敏
* 版本：V1.0
* 创建日期：2017/4/13
* 修改日期:2017/4/13
*/
@Activate
public class TraceProviderFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    private Span startTrace(Map<String, String> attaches,Invocation invocation) {

        Long traceId = Long.valueOf(attaches.get(TraceContext.TRACE_ID_KEY));
        Long parentSpanId = Long.valueOf(attaches.get(TraceContext.SPAN_ID_KEY));

        TraceContext.start();
        TraceContext.setTraceId(traceId);
        TraceContext.setSpanId(parentSpanId);

        Span providerSpan = new Span();

        long id = IdUtils.get();
        providerSpan.setId(id);
        providerSpan.setParent_id(parentSpanId);
        providerSpan.setTrace_id(traceId);
        providerSpan.setName(TraceContext.getTraceConfig().getApplicationName()+"-provider:"+invocation.getMethodName());
        long timestamp = System.currentTimeMillis()*1000;
        providerSpan.setTimestamp(timestamp);

        providerSpan.addToAnnotations(
                Annotation.create(timestamp, TraceContext.ANNO_SR,
                        Endpoint.create(
                                TraceContext.getTraceConfig().getApplicationName(),
                                NetworkUtils.ip2Num(NetworkUtils.getSiteIp()),
                                TraceContext.getTraceConfig().getServerPort() )));

        TraceContext.addSpan(providerSpan);
        return providerSpan;
    }

    private void endTrace(Span span, Stopwatch watch) {

        span.addToAnnotations(
                Annotation.create(System.currentTimeMillis()*1000, TraceContext.ANNO_SS,
                        Endpoint.create(
                                span.getName(),
                                NetworkUtils.ip2Num(NetworkUtils.getSiteIp()),
                                TraceContext.getTraceConfig().getServerPort())));

        span.setDuration(watch.stop().elapsed(TimeUnit.MICROSECONDS));
        com.ericliu.dubbo.trace.zipkin.filter.TraceAgent traceAgent=new TraceAgent(TraceContext.getTraceConfig().getZipkinUrl());

        traceAgent.send(TraceContext.getSpans());

    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if(!TraceContext.getTraceConfig().isEnabled()){
            return invoker.invoke(invocation);
        }

        Map<String, String> attaches = invocation.getAttachments();
        if (!attaches.containsKey(TraceContext.TRACE_ID_KEY)){
            return invoker.invoke(invocation);
        }
        Stopwatch watch = Stopwatch.createStarted();
        Span providerSpan= this.startTrace(attaches,invocation);

        Result result = invoker.invoke(invocation);

        this.endTrace(providerSpan,watch);

        return result;

    }
}
