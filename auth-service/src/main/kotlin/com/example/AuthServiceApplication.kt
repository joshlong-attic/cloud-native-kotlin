package com.example

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@RestController
@EnableAuthorizationServer
@EnableDiscoveryClient
@EnableResourceServer
@SpringBootApplication
open class AuthServiceApplication {

    @RequestMapping("/user")
    open fun principal(p: Principal) = p

    @Bean
    open fun clr(ar: AccountRepository) = CommandLineRunner { args ->
        arrayOf("jlong,spring", "pwebb,boot", "dsyer,cloud")
                .map { it.split(',') }
                .forEach { ar.save(Account(username = it [0], password = it[1], active = true)) }
    }
}

@Configuration
open class OAuthConfiguration(val ar: AccountRepository, val authManager: AuthenticationManager) :
        AuthorizationServerConfigurerAdapter() {

    override fun configure(clients: ClientDetailsServiceConfigurer) {
        clients.inMemory()
                .withClient("html5").secret("secret").authorizedGrantTypes("password").scopes("openid")
    }

    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
        endpoints.authenticationManager(authManager)
    }
}

@Service
open class AccountUserDetailsService(val ar: AccountRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails =
            ar.findByUsername(username).map {
                User(it.username, it.password,
                        it.active, it.active, it.active, it.active,
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER"))
            }.orElseThrow { UsernameNotFoundException("couldn't find ${username}!") }
}

fun main(args: Array<String>) {
    SpringApplication.run(AuthServiceApplication::class.java, *args)
}

interface AccountRepository : JpaRepository <Account, Long> {

    fun findByUsername(username: String): Optional<Account>
}

@Entity
open class Account {

    @Id @GeneratedValue
    var id: Long? = null
    var username: String? = null
    var password: String? = null
    var active: Boolean = false

    constructor() { // why JPA why??
    }

    constructor (username: String? = null, password: String? = null, active: Boolean = false) {
        this.password = password
        this.username = username
        this.active = active
    }
}

