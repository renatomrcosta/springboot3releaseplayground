package com.xunfos.springboot3releaseplayground

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.core.publisher.Mono

@SpringBootApplication
class Springboot3releaseplaygroundApplication(private val httpBinClient: HttpBinClient) :
    ApplicationListener<ApplicationReadyEvent> {
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        println("application started!")
        val anything = httpBinClient.anything("httpbin.org")
        println(anything.block())
        println("started ok!")
    }
}

fun main(args: Array<String>) {
    runApplication<Springboot3releaseplaygroundApplication>(*args)
}

@Configuration
class Config {
    @Bean
    fun httpServiceProxyFactory(webClientBuilder: WebClient.Builder): HttpServiceProxyFactory =
        HttpServiceProxyFactory.builder()
            .clientAdapter(WebClientAdapter.forClient(
                webClientBuilder
                    .filter { request, next ->
                        println("Request was ${request.url()}")
                        next.exchange(request)
                    }
                    .build()
            ))
            .build()

    @Bean
    fun HttpBinClient(httpServiceProxyFactory: HttpServiceProxyFactory): HttpBinClient =
        httpServiceProxyFactory.createClient(HttpBinClient::class.java)
}

@HttpExchange(url = "{httpbin}")
interface HttpBinClient {
    @GetExchange("/anything")
    fun anything(@PathVariable("httpbin") httpbin: String): Mono<String>
}
