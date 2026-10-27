package com.smge.smge.common.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

// @RestController fica sob /api; o resto cai no index.html pro React Router decidir a tela (SPA)
@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final String PREFIXO_API = "/api";

    private static final String PASTA_FRONT = "static/";

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(PREFIXO_API, HandlerTypePredicate.forAnnotation(RestController.class));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/" + PASTA_FRONT)
                .resourceChain(true)
                .addResolver(new SpaResourceResolver());
    }

    private static class SpaResourceResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String caminho, Resource pasta) throws IOException {

            // rota de API inexistente deve dar 404, nunca a página do front
            if (caminho.startsWith("api/") || caminho.equals("api")) {
                return null;
            }

            Resource arquivo = pasta.createRelative(caminho);
            if (arquivo.exists() && arquivo.isReadable()) {
                return arquivo;
            }

            // caminho com extensão (ex.: /logo.png) que não existe -> 404
            if (caminho.contains(".")) {
                return null;
            }

            Resource index = new ClassPathResource(PASTA_FRONT + "index.html");
            return index.exists() ? index : null;
        }
    }
}
