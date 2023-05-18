package com.itheima.pinda.authority.config;

import com.itheima.pinda.authority.biz.service.common.OptLogService;
import com.itheima.pinda.log.entity.OptLogDTO;
import com.itheima.pinda.log.event.SysLogListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.function.Consumer;

/**
 * 日志自动配置
 * @author f
 * @date 2023/5/17 21:32
 */
@EnableAsync
@Configuration
public class SysLogConfiguration {

    @Bean
    public SysLogListener sysLogListener(OptLogService optLogService) {
        Consumer<OptLogDTO> consumer = optLogDTO -> optLogService.save(optLogDTO);
        return new SysLogListener(consumer);
    }
}
