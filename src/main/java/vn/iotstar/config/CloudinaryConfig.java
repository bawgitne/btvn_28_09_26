package vn.iotstar.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {

        return new Cloudinary(Map.of(
                "cloud_name", (cloudName != null && !cloudName.isBlank()) ? cloudName : "dummy",
                "api_key", (apiKey != null && !apiKey.isBlank()) ? apiKey : "dummy",
                "api_secret", (apiSecret != null && !apiSecret.isBlank()) ? apiSecret : "dummy"
        ));
    }
}
