package com.itheima.pinda.auul.filter;

import cn.hutool.core.util.StrUtil;
import com.itheima.pinda.base.R;
import com.itheima.pinda.common.adapter.IgnoreTokenConfig;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;

/**
 * 基础 网关过滤器
 * @author f
 * @date 2023/5/17 22:50
 */
@Slf4j
public abstract class BaseFilter extends ZuulFilter {

    @Value("${server.servlet.context-path}")
    protected String zuulPrefix;

    /**
     * 判断当前请求uri是否需要忽略
     * @return boolean
     */
    protected boolean isIgnoreToken() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        String uri = request.getRequestURI();
        uri = StrUtil.subSuf(uri, zuulPrefix.length());
        uri = StrUtil.subSuf(uri, uri.indexOf("/", 1));

        boolean ignoreToken = IgnoreTokenConfig.isIgnoreToken(uri);
        return ignoreToken;
    }

    /**
     * 网关抛异常
     * @param errMsg            msg
     * @param errCode           code
     * @param httpStatusCode    statusCode
     */
    protected void errorResponse(String errMsg, int errCode, int httpStatusCode) {
        R tokenError = R.fail(errCode, errMsg);
        RequestContext ctx = RequestContext.getCurrentContext();

        ctx.setResponseStatusCode(httpStatusCode);
        ctx.addZuulResponseHeader("Content-Type", "application/json;charset-UTF-8");
        if (null == ctx.getResponseBody()) {
            ctx.setResponseBody(tokenError.toString());
            ctx.setSendZuulResponse(false);
        }
    }
}
