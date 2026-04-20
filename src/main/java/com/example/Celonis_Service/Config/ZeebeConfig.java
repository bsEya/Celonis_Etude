package com.example.Celonis_Service.Config;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeConfig {

    @Bean
    public ZeebeClient zeebeClient() {
        return ZeebeClient.newClientBuilder()
                .gatewayAddress("bd4312ae-4eef-499a-a7ff-deb045cda011.lhr-1.zeebe.camunda.io:443")
                .credentialsProvider(
                        new OAuthCredentialsProviderBuilder()
                                .clientId("4agKJTcJ~T1o2RyRA_2eufxYchBeu4_K")
                                .clientSecret("hzI3TSMxDEoizUhxye7ooZIWSX1rZtUQXGXAUap3Gyqc7o_4M3K11ydATuthqI1J")
                                .audience("zeebe.camunda.io")
                                .build()
                )
                .build();
    }
}