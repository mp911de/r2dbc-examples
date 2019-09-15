/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.demo;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.r2dbc.postgresql.Notification;
import io.r2dbc.postgresql.PostgresqlConnection;
import io.r2dbc.postgresql.PostgresqlResult;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableR2dbcRepositories(considerNestedRepositories = true)
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@RestController
	class LoginController {

		final LoginEventRepository repository;
		final PostgresqlConnection connection;

		LoginController(LoginEventRepository repository, ConnectionFactory connectionFactory) {
			this.repository = repository;
			this.connection = Mono.from(connectionFactory.create())
					.cast(PostgresqlConnection.class).block();
		}

		@PostConstruct
		private void postConstruct() {
			connection.createStatement("LISTEN login_event_notification").execute()
					.flatMap(PostgresqlResult::getRowsUpdated).subscribe();
		}

		@PreDestroy
		private void preDestroy() {
			connection.close().subscribe();
		}

		@PostMapping("/login/{username}")
		Mono<Void> login(@PathVariable String username) {
			return repository.save(new LoginEvent(username, LocalDateTime.now())).then();
		}

		@GetMapping(value = "/login-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
		Flux<CharSequence> getStream() {
			return connection.getNotifications().map(Notification::getParameter);
		}

	}

	interface LoginEventRepository extends ReactiveCrudRepository<LoginEvent, Integer> {

	}

	@Table
	class LoginEvent {

		@Id
		Integer id;

		String username;

		LocalDateTime loginTime;

		public LoginEvent(String username, LocalDateTime loginTime) {
			this.username = username;
			this.loginTime = loginTime;
		}

	}

}
