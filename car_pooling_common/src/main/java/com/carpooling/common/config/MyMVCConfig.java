package com.carpooling.common.config;


import com.carpooling.common.interceptor.IPInterceptor;
import com.carpooling.common.interceptor.LoginInterceptor;
import com.carpooling.common.interceptor.MockInter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author LiangHanSggg
 * @desc MVC配置文件
 * @date 2023-05-21 15:22
 */
@Configuration
public class MyMVCConfig implements WebMvcConfigurer {

    @Resource
    LoginInterceptor loginInterceptor;

    @Resource
    MockInter mockInter;

    @Resource
    IPInterceptor ipInterceptor;

    @Autowired
    ObjectMapper objectMapper;

    // 默认登录，电话号验证
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        临时注释掉
//        registry.addInterceptor(loginInterceptor)
//                .addPathPatterns("/**")
//                .excludePathPatterns("/user/login", "/druid/**", "/mock/**");

//        registry.addInterceptor(mockInter)
//                .addPathPatterns("/mock/**");
//        使用模拟的登录拦截
        registry.addInterceptor(mockInter)
                .addPathPatterns("/**");

        registry.addInterceptor(ipInterceptor)
                .addPathPatterns("/user/login", "/druid/**");

    }


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowCredentials(false)
                .allowedMethods("GET", "POST", "DELETE", "PUT")
                .allowedHeaders("*")
                .maxAge(3600 * 24);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        messageConverter.setObjectMapper(objectMapper);
        converters.add(0, messageConverter);
    }


}

