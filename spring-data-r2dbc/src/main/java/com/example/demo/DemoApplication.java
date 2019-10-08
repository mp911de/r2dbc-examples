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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.domain.Persistable;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.r2dbc.repository.query.Query;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
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
	public static class PersonController {

		final PersonRepository personRepository;
		final PersonEventRepository personEventRepository;
		final DatabaseClient databaseClient;

		public PersonController(PersonRepository personRepository,
				PersonEventRepository personEventRepository,
				DatabaseClient databaseClient) {
			this.personRepository = personRepository;
			this.personEventRepository = personEventRepository;
			this.databaseClient = databaseClient;
		}

		@GetMapping("/")
		public Flux<Person> findAll() {
			return this.personRepository.findAll();
		}

		@GetMapping("events")
		public Flux<PersonEvent> findAllEvents() {
			return this.personEventRepository.findAll();
		}

		@GetMapping("by-name/{lastname}")
		public Flux<Person> findAllByLastName(@PathVariable String lastName) {
			return this.personRepository.findAllByLastName(lastName);
		}

		@PostMapping(value = "create/{firstName}/{lastName}")
		public Mono<Void> create(@PathVariable String firstName, @PathVariable String lastName) {

			Person person = new Person(firstName, lastName);
			PersonEvent event = new PersonEvent(firstName, lastName, "CREATED");

			return personRepository.save(person)
					.then(personEventRepository.save(event).then());
		}
	}

	interface PersonRepository extends ReactiveCrudRepository<Person, Integer> {

		@Query("SELECT * FROM person WHERE last_name = :lastname")
		Flux<Person> findAllByLastName(String lastname);
	}

	interface PersonEventRepository extends ReactiveCrudRepository<PersonEvent, Integer> {
	}

	@Table
	static class Person {

		@Id
		Integer id;

		String firstName;
		String lastName;

		public Person(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}

		public Integer getId() {
			return this.id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getFirstName() {
			return this.firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return this.lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
	}

	@Table
	static class PersonEvent implements Persistable<Integer> {

		@Id
		Integer id;

		String firstName;
		String lastName;
		String action;

		@PersistenceConstructor
		public PersonEvent(Integer id, String firstName, String lastName, String action) {
			this.id = id;
			this.firstName = firstName;
			this.lastName = lastName;
			this.action = action;
		}

		public PersonEvent(String firstName, String lastName, String action) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.action = action;
		}

		public Integer getId() {
			return this.id;
		}

		@Override
		public boolean isNew() {
			return true;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getFirstName() {
			return this.firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return this.lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public String getAction() {
			return this.action;
		}

		public void setAction(String action) {
			this.action = action;
		}
	}

}
