package br.com.rotaflex.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
            .info(new Info()
                    .title("RotaFlex - Servicos de Frete")
                    .description("This is a sample JWT authentication service. You can find out more about JWT at [https://jwt.io/](https://jwt.io/). For this sample, you can use the `admin` or `client` users (password: admin and client respectively) to test the authorization filters. Once you have successfully logged in and obtained the token, you should click on the right top button `Authorize` and introduce it with the prefix \"Bearer \".")
                    .version("1.0.0")
                    .license(new io.swagger.v3.oas.models.info.License()
                            .name("MIT License")
                            .url("http://opensource.org/licenses/MIT"))
                    .contact(new Contact()
                            .email("mauriurraco@gmail.com"))
            )
            .components(new Components()
                    .addSecuritySchemes("Authorization", new SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .in(SecurityScheme.In.HEADER)
                            .name("Authorization"))
            );
  }
}
