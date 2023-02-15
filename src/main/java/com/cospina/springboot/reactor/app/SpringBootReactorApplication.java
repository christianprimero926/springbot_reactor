package com.cospina.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

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
//		userCommentsFlatMapExample();
//		userCommentsZipWithExample();
//		userCommentsZipWithForm2Example();
//		zipWithRangeExample();
//		exampleInterval();
//		exampleDelayElements();
//		exampleIntervalInfinite();
		exampleIntervalFromCreate();
	}

	public void exampleIntervalFromCreate() {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				private Integer count = 0;

				@Override
				public void run() {
					emitter.next(++count);
					if (count == 10) {
						timer.cancel();
						emitter.complete();
					}
					if (count == 5) {
						emitter.error(new InterruptedException("Error, se ha detenido el flux en 5"));
					}
				}
			}, 1000, 1000);
		})
//		.doOnNext(next -> log.info(next.toString()))
//		.doOnComplete(() -> log.info("Hemos terminado!"))
				.subscribe(next -> log.info(next.toString()), error -> log.error(error.getMessage()),
						() -> log.info("Hemos terminado!"));
	}

	public void exampleIntervalInfinite() throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1)).doOnTerminate(latch::countDown).flatMap(i -> {
			if (i >= 5) {
				return Flux.error(new InterruptedException("Solo hasta 5"));
			}
			return Flux.just(i);
		}).map(i -> "Hola " + i).retry(2).subscribe(s -> log.info(s), e -> log.error(e.getMessage()));

		latch.await();
	}

	public void exampleDelayElements() throws InterruptedException {
		Flux<Integer> range = Flux.range(1, 12).delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));

		range.blockLast();

//		range.subscribe();
//		Thread.sleep(13000);
	}

	public void exampleInterval() {
		Flux<Integer> range = Flux.range(1, 12);
		Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));

		range.zipWith(delay, (ra, de) -> ra).doOnNext(i -> log.info(i.toString())).blockLast();
	}

	public void zipWithRangeExample() {
		Flux<Integer> ranges = Flux.range(0, 4);
		Flux.just(1, 2, 3, 4).map(i -> (i * 2))
				.zipWith(ranges, (uno, dos) -> String.format("Primer Flux: %d Segundo Flux: %d", uno, dos))
				.subscribe(text -> log.info(text));
	}

	public void userCommentsZipWithForm2Example() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("Jhon", "Doe"));

		Mono<Comments> commentsUserMono = Mono.fromCallable(() -> {
			Comments comments = new Comments();
			comments.addComment("Hola pepe que tal");
			comments.addComment("Mañana voy a la playa");
			comments.addComment("Estoy tomando el curso de spring");
			return comments;
		});
		Mono<UserComments> userZipComments = userMono.zipWith(commentsUserMono).map(tuple -> {
			User u = tuple.getT1();
			Comments c = tuple.getT2();
			return new UserComments(u, c);
		});

		userZipComments.subscribe(userComments -> log.info(userComments.toString()));
	}

	public void userCommentsZipWithExample() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("Jhon", "Doe"));

		Mono<Comments> commentsUserMono = Mono.fromCallable(() -> {
			Comments comments = new Comments();
			comments.addComment("Hola pepe que tal");
			comments.addComment("Mañana voy a la playa");
			comments.addComment("Estoy tomando el curso de spring");
			return comments;
		});
		Mono<UserComments> userZipComments = userMono.zipWith(commentsUserMono,
				(user, userComments) -> new UserComments(user, userComments));

		userZipComments.subscribe(userComments -> log.info(userComments.toString()));
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
