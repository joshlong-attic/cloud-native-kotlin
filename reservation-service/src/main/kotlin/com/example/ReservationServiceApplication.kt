package com.example

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.data.rest.core.annotation.RestResource
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.messaging.SubscribableChannel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.stream.Stream


interface ServiceChannels {

    @Input
    fun input(): SubscribableChannel
}

/*
@MessageEndpoint
open class ReservationProcessor(val rr: ReservationRepository) {

    @ServiceActivator(inputChannel = "input")
    open fun handle(rs: String) = rr.save(Reservation(reservationName = rs))
}
*/

@EnableDiscoveryClient
@EnableBinding(ServiceChannels::class)
@SpringBootApplication
open class ReservationServiceApplication {

    @Bean
    open fun integration(sc: ServiceChannels, rr: ReservationRepository) =
            IntegrationFlows
                    .from(sc.input())
                    .handle(String::class.java, { p, h ->
                        rr.save(Reservation(reservationName = p))
                        null
                    })
                    .get()

    @Bean
    open fun clr(rr: ReservationRepository) = CommandLineRunner {
        rr.deleteAll()
        arrayOf("Josh", "George", "Eddie", "Mia", "Michelle", "Illya")
                .forEach { rr.save(Reservation(reservationName = it)) }
        rr.findAll().forEach { System.out.println(it) }
    }
}

@RestController
@RefreshScope
open class MessageRestController(@Value("\${message}") var value: String) {

    @GetMapping("/message")
    open fun message() = this.value
}

fun main(args: Array<String>) {
    SpringApplication.run(ReservationServiceApplication::class.java, *args)
}

@RepositoryRestResource
interface ReservationRepository : MongoRepository<Reservation, String> {

    @RestResource(path = "by-name")
    fun findByReservationName(reservationName: String): Stream<Reservation>
}

@Document
data class Reservation(@Id var id: String? = null, var reservationName: String? = null)