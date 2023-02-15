package com.cospina.springboot.reactor.app;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.cospina.springboot.reactor.app.model.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
//		iterableExample();
		flatMapExample();
	}

	public void flatMapExample() throws Exception {

		List<String> usersList = new ArrayList<>();
		usersList.add("Andres Guzman");
		usersList.add("Pedro Fulano");
		usersList.add("Maria Fulana");
		usersList.add("Juan Mengano");
		usersList.add("Bruce lee");
		usersList.add("Bruce Willis");

		Flux.fromIterable(usersList)
				.map(name -> new User(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
				.flatMap(user -> {
					if (user.getName().equalsIgnoreCase("bruce")) {
						return Mono.just(user);
					} else {
						return Mono.empty();
					}
				}).map(user -> {
					String name = user.getName().toLowerCase();
					user.setName(name);
					return user;
				}).subscribe(u -> log.info(u.toString()));
	}

	public void iterableExample() throws Exception {

		List<String> usersList = new ArrayList<>();
		usersList.add("Andres Guzman");
		usersList.add("Pedro Fulano");
		usersList.add("Maria Fulana");
		usersList.add("Juan Mengano");
		usersList.add("Bruce lee");
		usersList.add("Bruce Willis");

		Flux<String> names = Flux.fromIterable(usersList);
		/*
		 * Flux.just("Andres Guzman", "Pedro Fulano", "Maria Fulana", "Juan Mengano",
		 * "Bruce lee", "Bruce Willis");
		 */

		Flux<User> users = names
				.map(name -> new User(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
				.filter(user -> user.getName().toLowerCase().equals("bruce")).doOnNext(user -> {
					if (user == null) {
						throw new RuntimeException("Nombres no pueden ser vacios");
					}
					System.out.println(user.getName().concat(" ").concat(user.getLastName()));
				}).map(user -> {
					String name = user.getName().toLowerCase();
					user.setName(name);
					return user;
				});

		users.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), new Runnable() {

			@Override
			public void run() {
				log.info("Ha finalizado la ejecucion del observable con exito");
			}
		});
	}
}
