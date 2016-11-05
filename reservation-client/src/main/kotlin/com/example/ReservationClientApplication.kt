package com.example

import com.google.common.util.concurrent.RateLimiter
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.netflix.feign.EnableFeignClients
import org.springframework.cloud.netflix.feign.FeignClient
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.context.annotation.Bean
import org.springframework.hateoas.Resources
import org.springframework.http.HttpStatus
import org.springframework.integration.annotation.Gateway
import org.springframework.integration.annotation.IntegrationComponentScan
import org.springframework.integration.annotation.MessagingGateway
import org.springframework.messaging.MessageChannel
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.util.*
import javax.servlet.*
import javax.servlet.http.HttpServletResponse

@EnableZuulProxy
@EnableDiscoveryClient
@EnableFeignClients
@EnableBinding(ClientChannels::class)
@IntegrationComponentScan
@EnableCircuitBreaker
@SpringBootApplication
@EnableResourceServer
open class ReservationClientApplication {

    @Bean
    @LoadBalanced
    open fun rt() = RestTemplate()

}

@MessagingGateway
interface ReservationWriter {

    @Gateway(requestChannel = "output")
    fun write(rn: String?)
}

fun main(args: Array<String>) {
    SpringApplication.run(ReservationClientApplication::class.java, *args)
}

//@Component
class RateLimitingFilter : Filter {

    val rateLimiter: RateLimiter = RateLimiter.create(1.0 / 30.0)

    override fun destroy() {
    }

    override fun doFilter(request: ServletRequest,
                          response: ServletResponse,
                          chain: FilterChain) {
        if (rateLimiter.tryAcquire()) {
            chain.doFilter(request, response)
        } else {
            HttpServletResponse::class.java.cast(response).status = HttpStatus.TOO_MANY_REQUESTS.value()
        }
    }

    override fun init(p0: FilterConfig?) {
    }
}

interface ClientChannels {

    @Output
    fun output(): MessageChannel
}

@FeignClient("reservation-service")
interface ReservationReader {

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "/reservations")
    fun read(): Resources<Reservation>
}

data class Reservation(var reservationName: String? = null)

@RestController
@RequestMapping("/reservations")
open class ReservationClientApiRestController(val rr: ReservationReader, val rw: ReservationWriter) {

    @PostMapping
    open fun write(@RequestBody reservation: Reservation) {
        rw.write(reservation.reservationName)
    }

    open fun fallback(): HashSet <String?> = HashSet()

    @HystrixCommand(fallbackMethod = "fallback")
    @GetMapping("/names")
    open fun names(): HashSet<String?> = rr.read().content.map { it.reservationName }.toHashSet()

    /*
    @GetMapping("/names")
    open fun names() =
             rt.exchange("http://reservation-service/reservations", HttpMethod.GET, null,
                     object : ParameterizedTypeReference<Resources<Reservation>>() {})
                     .body
                     .content
                     .map { it.reservationName }
                     .toCollection(HashSet())
 */

}