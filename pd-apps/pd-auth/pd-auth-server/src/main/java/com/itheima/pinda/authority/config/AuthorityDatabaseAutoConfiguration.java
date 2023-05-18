package com.itheima.pinda.authority.config;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.itheima.pinda.database.datasource.BaseDatabaseConfiguration;
import com.itheima.pinda.database.properties.DatabaseProperties;
import com.p6spy.engine.spy.P6DataSource;
import com.sun.xml.bind.v2.runtime.unmarshaller.Intercepter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import javax.sql.DataSource;
import java.util.List;

/**
 * 数据库配置类
 * @author f
 * @date 2023/5/14 21:27
 */
@Configuration
@Slf4j
@MapperScan(basePackages = {"com.itheima.pinda"}, annotationClass = Repository.class
        , sqlSessionFactoryRef = AuthorityDatabaseAutoConfiguration.DATABASE_PREFIX + "SqlSessionFactory")
@EnableConfigurationProperties({MybatisPlusProperties.class, DatabaseProperties.class})
public class AuthorityDatabaseAutoConfiguration extends BaseDatabaseConfiguration {

    /** 每个数据源配置不同即可 */
    final static String DATABASE_PREFIX = "master";

    public AuthorityDatabaseAutoConfiguration(MybatisPlusProperties properties
            , DatabaseProperties databaseProperties
            , ObjectProvider<Interceptor[]> interceptorsProvider
            , ObjectProvider<TypeHandler[]> typeHandlersProvider
            , ObjectProvider<LanguageDriver[]> languageDriverProvider
            , ResourceLoader resourceLoader
            , ObjectProvider<DatabaseIdProvider> databaseIdProvider
            , ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizer
            , ObjectProvider<List<MybatisPlusPropertiesCustomizer>> mybatisPlusPropertiesCustomizerProvider
            , ApplicationContext applicationContext) {
        super(properties, databaseProperties, interceptorsProvider, typeHandlersProvider, languageDriverProvider
                , resourceLoader, databaseIdProvider, configurationCustomizer, mybatisPlusPropertiesCustomizerProvider, applicationContext);
    }

    /**
     * 数据源信息
     * @return dataSource
     */
    @Bean(name = DATABASE_PREFIX + "DruidDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DataSource druidDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = DATABASE_PREFIX + "DataSource")
    public DataSource dataSource(@Qualifier(DATABASE_PREFIX + "DruidDataSource") DataSource dataSource) {
        if (ArrayUtil.contains(DEV_PROFILES, this.profiles)) {
            return new P6DataSource(dataSource);
        }else {
            return dataSource;
        }
    }

    /**
     * mybatis sqlSession
     * @param dataSource dataSource
     * @return           sqlSessionFactory
     * @throws Exception ex
     */
    @Bean(DATABASE_PREFIX + "SqlSessionFactory")
    public SqlSessionFactory getSqlSessionFactory(@Qualifier(DATABASE_PREFIX + "DataSource") DataSource dataSource) throws Exception {
        return super.sqlSessionFactory(dataSource);
    }

    /**
     * 数据源事务管理器
     * @param dataSource dataSource
     * @return           TM
     */
    @Bean(name = DATABASE_PREFIX + "TransactionManager")
    public DataSourceTransactionManager dsTransactionManager(@Qualifier(DATABASE_PREFIX + "DataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 事务拦截器
     * @param transactionManager TM
     * @return                   TMI
     */
    @Bean(DATABASE_PREFIX + "TransactionInterceptor")
    public TransactionInterceptor transactionInterceptor(@Qualifier(DATABASE_PREFIX + "TransactionManager")PlatformTransactionManager transactionManager) {
        return new TransactionInterceptor(transactionManager, this.transactionAttributeSource());
    }

    /**
     * 事务
     * @param transactionManager TM
     * @param ti                 TI
     * @return                   advisor
     */
    @Bean(DATABASE_PREFIX + "Advisor")
    public Advisor getAdvisor(@Qualifier(DATABASE_PREFIX + "TransactionManager") PlatformTransactionManager transactionManager, @Qualifier(DATABASE_PREFIX + "TransactionInterceptor") TransactionInterceptor ti) {
        return super.txAdviceAdvisor(ti);
    }
}
