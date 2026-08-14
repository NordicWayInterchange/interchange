package no.vegvesen.ixn.federation.adminserver;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

@EnableWebMvc
@Configuration
@ComponentScan({"no.vegvesen.ixn.federation.adminserver"})
public class AdminStrictWebConfig implements WebMvcConfigurer {
    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        builder.addCustomConverter(new ByteArrayHttpMessageConverter());
        builder.addCustomConverter(strictJsonMessageConverter());
    }

    static JacksonJsonHttpMessageConverter strictJsonMessageConverter() {
        JsonMapper strictObjectMapper = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .build();
        return new JacksonJsonHttpMessageConverter(strictObjectMapper);
    }
}
