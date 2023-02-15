package com.cospina.springboot.reactor.app;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.cospina.springboot.reactor.app.model.Comments;
import com.cospina.springboot.reactor.app.model.User;
import com.cospina.springboot.reactor.app.model.UserComments;

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
//		flatMapExample();
//		toStringExample();
//		collectListExample();
		userCommentsFlatMapExample();
	}

	public void userCommentsFlatMapExample() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("Jhon", "Doe"));

		Mono<Comments> commentsUserMono = Mono.fromCallable(() -> {
			Comments comments = new Comments();
			comments.addComment("Hola pepe que tal");
			comments.addComment("Mañana voy a la playa");
			comments.addComment("Estoy tomando el curso de spring");
			return comments;
		});
		userMono.flatMap(user -> commentsUserMono.map(comments -> new UserComments(user, comments)))
				.subscribe(userComments -> log.info(userComments.toString()));
	}

	public void collectListExample() throws Exception {

		List<User> usersList = new ArrayList<>();
		usersList.add(new User("Andres", "Guzman"));
		usersList.add(new User("Pedro", "Fulano"));
		usersList.add(new User("Maria", "Fulana"));
		usersList.add(new User("Juan", "Mengano"));
		usersList.add(new User("Bruce", "lee"));
		usersList.add(new User("Bruce", "Willis"));

		Flux.fromIterable(usersList).collectList().subscribe(list -> {
			list.forEach(item -> log.info(item.toString()));
		});
	}

	public void toStringExample() throws Exception {

		List<User> usersList = new ArrayList<>();
		usersList.add(new User("Andres", "Guzman"));
		usersList.add(new User("Pedro", "Fulano"));
		usersList.add(new User("Maria", "Fulana"));
		usersList.add(new User("Juan", "Mengano"));
		usersList.add(new User("Bruce", "lee"));
		usersList.add(new User("Bruce", "Willis"));

		Flux.fromIterable(usersList)
				.map(user -> user.getName().toUpperCase().concat(" ").concat(user.getLastName().toUpperCase()))
				.flatMap(name -> {
					if (name.contains("bruce".toUpperCase())) {
						return Mono.just(name);
					} else {
						return Mono.empty();
					}
				}).map(name -> {
					return name.toLowerCase();
				}).subscribe(u -> log.info(u.toString()));
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
