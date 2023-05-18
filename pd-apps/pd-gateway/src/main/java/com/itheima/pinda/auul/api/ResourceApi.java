package com.itheima.pinda.auul.api;

import com.itheima.pinda.authority.dto.auth.ResourceQueryDTO;
import com.itheima.pinda.authority.entity.auth.Resource;
import com.itheima.pinda.base.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author f
 * @date 2023/5/17 22:36
 */
@FeignClient(name = "${pinda.feign.authorit-server:pd-auth-server}", fallback = ResourceApiFallback.class)
public interface ResourceApi {

    /**
     * 获取所有需要鉴权的资源
     * @return list
     */
    @GetMapping("/resource/list")
    public R<List> list();

    /**
     * 查询当前登录用户拥有的资源权限
     * @param dto dto
     * @return    list
     */
    @GetMapping("/resource")
    public R<List<Resource>> visible(ResourceQueryDTO dto);
}
