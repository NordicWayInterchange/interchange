package no.vegvesen.ixn.federation.server;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@EnableWebMvc
@Configuration
@ComponentScan({ "no.vegvesen.ixn.federation.server" })
public class NeighbourStrictWebConfig implements WebMvcConfigurer{

	@Override
	public void configureMessageConverters(
			List<HttpMessageConverter<?>> messageConverters) {
		messageConverters.add(new ByteArrayHttpMessageConverter());
		messageConverters.add(strictJsonMessageConverter());
	}

	static JacksonJsonHttpMessageConverter strictJsonMessageConverter() {
		JacksonJsonHttpMessageConverter strictJsonMessageConverter = new JacksonJsonHttpMessageConverter();
		ObjectMapper strictObjectMapper = new ObjectMapper();
		strictObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
		strictJsonMessageConverter.setObjectMapper(strictObjectMapper);
		return strictJsonMessageConverter;
	}
}