package cc.openstrata.platform.config;

import cc.openstrata.platform.web.ConsumerAccessFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor);
    }

    /** RBAC gate for the CONSUMER role (EU-05). Runs before the dispatcher. */
    @Bean
    public FilterRegistrationBean<ConsumerAccessFilter> consumerAccessFilter() {
        FilterRegistrationBean<ConsumerAccessFilter> bean =
            new FilterRegistrationBean<>(new ConsumerAccessFilter());
        bean.setOrder(1);
        return bean;
    }
}
