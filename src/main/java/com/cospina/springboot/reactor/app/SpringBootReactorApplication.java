package com.cospina.springboot.reactor.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner{

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		Flux<String> names = Flux.just("Andres", "Pedro", "", "Juan")
				.doOnNext(e -> { 
					if (e.isEmpty()) {
						throw new RuntimeException("Nombres no pueden ser vacios");
					} else {
						System.out.println(e);
					}	
				});		
		
		names.subscribe(e -> log.info(e),
				error -> log.error(error.getMessage()));
	}

}