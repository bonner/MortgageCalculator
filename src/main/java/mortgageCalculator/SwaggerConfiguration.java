package mortgageCalculator;

import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("mortgageCalculator"))
                .build().apiInfo(metaData()).useDefaultResponseMessages(false);
    }
    
    private ApiInfo metaData() {
        ApiInfo apiInfo = new ApiInfo("mortgageCalculator Application's REST API",
                "Below are the list of REST API references for the mortgageCalculator Application",
                "1.0", "Terms of service",
                new Contact("Michael-Bonner", "https://github.com/bonner/MortgageCalculator",
                        "bonner.mike@gmail.com"),
                "APACHE LICENSE, VERSION 2.0", "https://www.apache.org/licenses/LICENSE-2.0.html",
                new ArrayList<VendorExtension>());
        return apiInfo;
    }
    
}
