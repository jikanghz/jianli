package com.jianli.web.configuration;

import com.jianli.common.oss.Oss;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private Oss oss;

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).addPathPatterns("/upload/**").excludePathPatterns(
                "/css/**",
                "/image/**",
                "/js/**",
                "/upload/public/**"
        );
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        String localPath = oss.localPath;
        if(localPath.endsWith("upload/"))
        {
            localPath = localPath.substring(0,localPath.length()-7);
        }

        registry.addResourceHandler("/**").addResourceLocations("classpath:/resources/")
                .addResourceLocations("classpath:/static/").addResourceLocations("file:" + localPath);
        super.addResourceHandlers(registry);
    }
}
