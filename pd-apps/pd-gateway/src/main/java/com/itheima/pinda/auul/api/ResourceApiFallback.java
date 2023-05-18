package com.itheima.pinda.auul.api;

import com.itheima.pinda.authority.dto.auth.ResourceQueryDTO;
import com.itheima.pinda.authority.entity.auth.Resource;
import com.itheima.pinda.base.R;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 资源API熔断
 * @author f
 * @date 2023/5/17 22:38
 */
@Component
public class ResourceApiFallback implements ResourceApi{

    @Override
    public R<List> list() {
        return null;
    }

    @Override
    public R<List<Resource>> visible(ResourceQueryDTO dto) {
        return null;
    }
}
