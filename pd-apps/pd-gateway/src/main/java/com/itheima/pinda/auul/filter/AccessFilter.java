package com.itheima.pinda.auul.filter;

import cn.hutool.core.util.StrUtil;
import com.itheima.pinda.authority.dto.auth.ResourceQueryDTO;
import com.itheima.pinda.auul.api.ResourceApi;
import com.itheima.pinda.base.R;
import com.itheima.pinda.common.constant.CacheKey;
import com.itheima.pinda.context.BaseContextConstants;
import com.itheima.pinda.exception.code.ExceptionCode;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import net.oschina.j2cache.CacheChannel;
import net.oschina.j2cache.CacheObject;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * 权限验证过滤器
 * @author f
 * @date 2023/5/18 21:11
 */
@Component
@Slf4j
public class AccessFilter extends BaseFilter{

    @Resource
    private CacheChannel cacheChannel;

    @Resource
    private ResourceApi resourceApi;

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 10;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        if (isIgnoreToken()) {
            return null;
        }

        RequestContext requestContext = RequestContext.getCurrentContext();
        String requestURI = requestContext.getRequest().getRequestURI();
        requestURI = StrUtil.subSuf(requestURI, zuulPrefix.length());
        requestURI = StrUtil.subSuf(requestURI, requestURI.indexOf("/", 1));
        String method = requestContext.getRequest().getMethod();
        String permission = method + requestURI;

        CacheObject resourceNeed2AuthObject = cacheChannel.get(CacheKey.RESOURCE, CacheKey.RESOURCE_NEED_TO_CHECK);
        List<String> resourceNeed2Auth = (List<String>)resourceNeed2AuthObject.getValue();
        if (null == resourceNeed2Auth) {
            resourceNeed2Auth = resourceApi.list().getData();
            if (null != resourceNeed2Auth) {
                cacheChannel.set(CacheKey.RESOURCE, CacheKey.RESOURCE_NEED_TO_CHECK, resourceNeed2Auth);
            }
        }

        if (null != resourceNeed2Auth) {
            long count = resourceNeed2Auth.stream().filter((String r) -> {
                return permission.startsWith(r);
            }).count();
            if (0 == count) {
                errorResponse(ExceptionCode.UNAUTHORIZED.getMsg(), ExceptionCode.UNAUTHORIZED.getCode(), 200);
                return null;
            }
        }

        String userId = requestContext.getZuulRequestHeaders().get(BaseContextConstants.JWT_KEY_USER_ID);
        CacheObject cacheObject = cacheChannel.get(CacheKey.USER_RESOURCE, userId);
        List<String> userResource = (List<String>)cacheObject.getValue();
        if (null == userResource) {
            ResourceQueryDTO resourceQueryDTO = new ResourceQueryDTO();
            resourceQueryDTO.setUserId(new Long(userId));
            R<List<com.itheima.pinda.authority.entity.auth.Resource>> result = resourceApi.visible(resourceQueryDTO);
            if (null != result.getData()) {
                List<com.itheima.pinda.authority.entity.auth.Resource> userResourceList = result.getData();
                userResource = userResourceList.stream().map((com.itheima.pinda.authority.entity.auth.Resource r) -> {
                    return r.getMethod() + r.getUrl();
                }).collect(Collectors.toList());
                cacheChannel.set(CacheKey.USER_RESOURCE, userId, userResource);
            }
        }

        long count = userResource.stream().filter((String r) -> {
            return permission.startsWith(r);
        }).count();

        if (count > 0) {
            return null;
        }else {
            log.warn("用户{}没有访问{}资源的权限", userId, method + requestURI);
            errorResponse(ExceptionCode.UNAUTHORIZED.getMsg(), ExceptionCode.UNAUTHORIZED.getCode(), 200);
        }
        return null;
    }
}
