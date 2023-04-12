package org.opengroup.osdu.partition.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collections;
import java.util.Map;
import javax.servlet.ServletContext;

import io.swagger.v3.oas.models.tags.Tag;
import org.opengroup.osdu.partition.model.Property;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile("!noswagger")
@PropertySource("classpath:swagger.properties")
public class SwaggerConfiguration {

    @Value("${api.title}")
    private String apiTitle;

    @Value("${api.description}")
    private String apiDescription;

    @Value("${api.version}")
    private String apiVersion;

    @Value("${api.contact.name}")
    private String contactName;

    @Value("${api.contact.email}")
    private String contactEmail;

    @Value("${api.license.name}")
    private String licenseName;

    @Value("${api.license.url}")
    private String licenseUrl;

    @Bean
    public OpenAPI openApi(ServletContext servletContext) {
        Server server = new Server().url(servletContext.getContextPath());
        return new OpenAPI()
            .servers(Collections.singletonList(server))
            .info(new Info()
                .title(apiTitle)
                .description(apiDescription)
                .version(apiVersion)
                .contact(new Contact().name(contactName).email(contactEmail))
                .license(new License().name(licenseName).url(licenseUrl)))
            .addTagsItem(new Tag().name("partition-api").description("Partition API"))
            .addTagsItem(new Tag().name("info").description("Version info endpoint"))
            .components(new Components()
                    .addSchemas("Map", new Schema<Map<String, Property>>().addProperties("< * >", new ObjectSchema().$ref("#/components/schemas/Property")))
                        .addSecuritySchemes("Authorization",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("Authorization")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")))
            .addSecurityItem(
                new SecurityRequirement()
                    .addList("Authorization"));
    }
}
