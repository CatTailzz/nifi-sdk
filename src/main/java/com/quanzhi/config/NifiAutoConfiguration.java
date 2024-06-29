package com.quanzhi.config;

import com.quanzhi.client.NifiClient;
import com.quanzhi.service.NifiService;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @description:
 * @author：CatTail
 * @date: 2024/6/27
 * @Copyright: https://github.com/CatTailzz
 */
@Configuration
@PropertySource("classpath:application.properties")
public class NifiAutoConfiguration {

    @Value("${nifi.url}")
    private String nifiUrl;

    @Value("${nifi.username}")
    private String username;

    @Value("${nifi.password}")
    private String password;

    @Bean
    public NifiClient nifiClient() throws Exception {
        return new NifiClient(nifiUrl, username, password);
    }

    @Bean
    public NifiService nifiService(NifiClient nifiClient) {
        return new NifiService(nifiClient);
    }
}
