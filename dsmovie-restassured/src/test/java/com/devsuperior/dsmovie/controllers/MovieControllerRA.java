package com.devsuperior.dsmovie.controllers;

import com.devsuperior.dsmovie.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

public class MovieControllerRA {

	private String adminUsername, adminPassword, adminToken, clientUsername, clientPassword, clientToken, invalidToken, movieTitle;
	private Long existingId, nonExistingId;
	private Map<String, Object> postMovieInstance;

	@BeforeEach
	void setUp() throws Exception{
		baseURI = "http://localhost:8080";
		existingId = 6L;
		nonExistingId = 999L;

		adminUsername = "maria@gmail.com";
		adminPassword = "123456";
		clientUsername = "ana@gmail.com";
		clientPassword = "123456";
		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		invalidToken = clientToken + "loiehw";

		postMovieInstance = new HashMap<>();
		postMovieInstance.put("title", "Test Movie");
		postMovieInstance.put("score", 0.0);
		postMovieInstance.put("count", 0);
		postMovieInstance.put("image", "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg");

		movieTitle = "Star Wars";
	}
	
	@Test
	public void findAllShouldReturnOkWhenMovieNoArgumentsGiven() {
		given()
		.when()
			.get("/movies")
		.then()
			.statusCode(HttpStatus.OK.value())
			.body("content[0].id", is(1))
			.body("content[0].title", equalTo("The Witcher"))
			.body("content[0].score", is(4.33F))
			.body("content[0].count", is(3))
			.body("content[0].image", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg"))
			.body("pageable.pageNumber", is(0))
			.body("pageable.pageSize", is(20));
	}
	
	@Test
	public void findAllShouldReturnPagedMoviesWhenMovieTitleParamIsNotEmpty() {
		given()
		.when()
			.get("/movies?title={movieTitle}", movieTitle)
		.then()
				.statusCode(HttpStatus.OK.value())
				.body("content[0].id", is(10))
				.body("content[0].title", equalTo("Rogue One: Uma História Star Wars"))
				.body("content[0].score", is(0.0F))
				.body("content[0].count", is(0))
				.body("content[0].image", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/6t8ES1d12OzWyCGxBeDYLHoaDrT.jpg"))
				.body("content.title", hasItems("Star Wars: A Guerra dos Clones", "Star Wars: Episódio I - A Ameaça Fantasma"))
				.body("pageable.pageNumber", is(0))
				.body("pageable.pageSize", is(20));
	}
	
	@Test
	public void findByIdShouldReturnMovieWhenIdExists() {
		given()
			.header("Content-Type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
		.when()
			.get("/movies/{id}", existingId)
		.then()
			.statusCode(HttpStatus.OK.value())
			.body("id", is(6))
			.body("title", equalTo("Django Livre"))
			.body("score", is(0.0F))
			.body("count", is(0))
			.body("image", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/2oZklIzUbvZXXzIFzv7Hi68d6xf.jpg"));
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {
		given()
		.when()
			.get("/movies/{id}", nonExistingId)
		.then()
			.statusCode(HttpStatus.NOT_FOUND.value());
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankTitle() throws JSONException {
		postMovieInstance.put("title", "");
		JSONObject newMovie = new JSONObject(postMovieInstance);
		given()
			.header("Content-Type", "application/json")
			.header("Authorization", "Bearer "+adminToken)
			.body(newMovie)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/movies")
		.then()
			.statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
			.body("errors.fieldName", hasItems("title"))
			.body("errors.message", hasItems("Campo requerido", "Tamanho deve ser entre 5 e 80 caracteres"));


	}
	
	@Test
	public void insertShouldReturnForbiddenWhenClientLogged() throws Exception {
		JSONObject newMovie = new JSONObject(postMovieInstance);
		given()
			.header("Content-Type", "application/json")
			.header("Authorization", "Bearer "+clientToken)
			.body(newMovie)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/movies")
		.then()
			.statusCode(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	public void insertShouldReturnUnauthorizedWhenInvalidToken() throws Exception {
		JSONObject newMovie = new JSONObject(postMovieInstance);
		given()
			.header("Content-Type", "application/json")
			.header("Authorization", "Bearer "+invalidToken)
			.body(newMovie)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/movies")
		.then()
			.statusCode(HttpStatus.UNAUTHORIZED.value());
	}
}
