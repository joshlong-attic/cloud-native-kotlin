package com.example

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.dataflow.server.EnableDataFlowServer

@SpringBootApplication
@EnableDataFlowServer
open class DataflowServiceApplication

fun main(args: Array<String>) {
    SpringApplication.run(DataflowServiceApplication::class.java, *args)
}

