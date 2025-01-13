package io.kestra.plugin.fastapi;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.uri.UriTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

import java.util.Map;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Insert data inside a ServiceNow table."
)
@Plugin(
    examples = {
        @Example(
            title = "Create an incident.",
            full = true,
            code = """
                   id: servicenow_post
                   namespace: company.team

                   tasks:
                     - id: post
                       type: io.kestra.plugin.servicenow.Post
                       domain: "snow_domain"
                       username: "snow_username"
                       password: "snow_password"
                       clientId: "snow_client_id"
                       clientSecret: "snow_client_secret"
                       table: incident
                       data:
                         short_description: "API Create Incident..."
                         requester_id: f8266e2adb16fb00fa638a3a489619d2
                         requester_for_id: a7ec77cbdefac300d322d182689619dc
                         product_id: 01a2e3c1db15f340d329d18c689ed922
                   """
        )
    }
)
public class Get extends AbstractFastApi implements RunnableTask<Get.Output> {
    @NotNull
    @NotEmpty
    @Schema(
        title = "FastApi endpoint."
    )
    @PluginProperty(dynamic = true)
    private String table;

    @NotNull
    @NotEmpty
    @Schema(
        title = "The data to insert."
    )
    private Property<Map<String, Object>> data;

    @Override
    public Get.Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();

        HttpResponse<GetResult> request = this.request(
            runContext,
            HttpRequest
                .create(
                    HttpMethod.GET,
                    UriTemplate.of("/{endpoint}")
                        .expand(Map.of(
                            "table", runContext.render(this.table)
                        ))
                )
                .body(runContext.render(data).asMap(String.class, Object.class)),
            Argument.of(GetResult.class)
        );

        if (request.getBody().isEmpty()) {
            throw new IllegalStateException("Empty body on '" + request + "'");
        }

        logger.info("Get done with result '{}'", request.getBody().orElse(null));

        return Output.builder()
            .result(request.getBody().get().getResult())
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "The result data.."
        )
        private Map<String, Object> result;
    }

    @Data
    @NoArgsConstructor
    public static class GetResult {
        Map<String, Object> result;
    }
}
