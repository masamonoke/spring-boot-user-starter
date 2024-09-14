package com.masamonoke.userstarter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masamonoke.userstarter.model.Role;
import com.masamonoke.userstarter.model.User;
import com.masamonoke.userstarter.repo.UserRepo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;


// tokens created based on milliseconds and between register and auth not enough time passed and tokens will be equal that is error
// because of that sleep is needed
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
class AuthControllerTest {
	private MockMvc mockMvc;
	@Autowired
	private WebApplicationContext context;
	@Autowired
	private ObjectMapper objectMapper;
	private static User user;
	@Autowired
	private UserRepo userRepo;

	@BeforeAll
	public static void createUser() {
		user = User.builder()
			.username("testuser")
			.email("testuser@test.com")
			.password("1234")
			.role(Role.USER)
			.build();
	}

	@AfterEach
	public void deleteUser() {
		userRepo.deleteAll();
	}

	@BeforeEach
	public void setupContext() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	}

	@Test
	@WithMockUser
    void testRegisterUser() throws Exception {
		var json = objectMapper.writeValueAsString(user);
		var res = mockMvc.perform(post("/api/v1/auth/register").content(json).contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk()).andReturn();
		assertEquals(200, res.getResponse().getStatus());
	}

	@Test
    void testSecuredEndpointWithNonAuthorizedUser() throws Exception {
		var res = mockMvc.perform(get("/api/v1/secured_test")).andReturn();
		assertEquals(403, res.getResponse().getStatus());
	}

	private MvcResult registerUser(String json) throws Exception {
		var res = mockMvc.perform(post("/api/v1/auth/register").content(json).contentType(MediaType.APPLICATION_JSON)).andReturn();
		assertEquals(200, res.getResponse().getStatus());

		return res;
	}

	@Test
	public void testLoginThenGetSecuredEndpoint() throws Exception {
		var json = objectMapper.writeValueAsString(user);
		registerUser(json);

        Thread.sleep(1000);
		var credentials = """
			{
				"username": "testuser",
				"password": "1234"
			}
		""";
		var res = mockMvc.perform(post("/api/v1/auth/authenticate").contentType(MediaType.APPLICATION_JSON).content(credentials))
			.andReturn();
		assertEquals(200, res.getResponse().getStatus());

		var mapper = new ObjectMapper();
		var jsonToken = res.getResponse().getContentAsString();
		var tokensMap = mapper.readValue(jsonToken, HashMap.class);
		var accessToken = tokensMap.get("access_token");
		res = mockMvc.perform(get("/api/v1/secured_test").header("Authorization", "Bearer " + accessToken)).andReturn();
		assertEquals(200, res.getResponse().getStatus());
	}

	@Test
	public void testLoginThenLogoutThenTryGetSecuredPoint() throws Exception {
		var json = objectMapper.writeValueAsString(user);
		var res = registerUser(json);

		var mapper = new ObjectMapper();
		var jsonToken = res.getResponse().getContentAsString();
		var tokensMap = mapper.readValue(jsonToken, HashMap.class);
		var accessToken = tokensMap.get("access_token");

		res = mockMvc.perform(get("/api/v1/secured_test").header("Authorization", "Bearer " + accessToken)).andReturn();
		assertEquals(200, res.getResponse().getStatus());

		res = mockMvc.perform(post("/api/v1/auth/logout").content(json).header("Authorization", "Bearer " + accessToken).contentType(MediaType.APPLICATION_JSON))
			.andReturn();
		assertEquals(200, res.getResponse().getStatus());

		res = mockMvc.perform(get("/api/v1/secured_test").header("Authorization", "Bearer " + accessToken)).andReturn();
		assertEquals(403, res.getResponse().getStatus());
	}

	@Test
	public void testRefreshToken() throws Exception {
		var json = objectMapper.writeValueAsString(user);
		var res = mockMvc.perform(post("/api/v1/auth/register").content(json).contentType(MediaType.APPLICATION_JSON))
			.andReturn();
		assertEquals(200, res.getResponse().getStatus());

		Thread.sleep(1000);
		var mapper = new ObjectMapper();
		var jsonToken = res.getResponse().getContentAsString();
		var tokensMap = mapper.readValue(jsonToken, HashMap.class);
		var accessToken = tokensMap.get("access_token");
		var refreshToken = tokensMap.get("refresh_token");

		res = mockMvc.perform(get("/api/v1/secured_test").header("Authorization", "Bearer " + accessToken)).andReturn();
		assertEquals(200, res.getResponse().getStatus());

		res = mockMvc.perform(post("/api/v1/auth/refresh").header("Authorization", "Bearer " + refreshToken)).andReturn();
		assertEquals(200, res.getResponse().getStatus());

		jsonToken = res.getResponse().getContentAsString();
		tokensMap = mapper.readValue(jsonToken, HashMap.class);
		accessToken = tokensMap.get("access_token");
		res = mockMvc.perform(get("/api/v1/secured_test").header("Authorization", "Bearer " + accessToken)).andReturn();
		assertEquals(200, res.getResponse().getStatus());
	}

	@Test
	public void logoutTest() throws Exception {
		var json = objectMapper.writeValueAsBytes(user);
		var res = mockMvc.perform(post("/api/v1/auth/register").content(json).contentType(MediaType.APPLICATION_JSON)).andReturn();
		assertEquals(200, res.getResponse().getStatus());

		Thread.sleep(1000);
		var mapper = new ObjectMapper();
		var jsonToken = res.getResponse().getContentAsString();
		var tokensMap = mapper.readValue(jsonToken, HashMap.class);
		var accessToken = tokensMap.get("access_token");

		res = mockMvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + accessToken)).andReturn();
		assertEquals(200, res.getResponse().getStatus());

		res = mockMvc.perform(get("/api/v1/secured_test").header("Authorization", "Bearer " + accessToken)).andReturn();
		assertEquals(403, res.getResponse().getStatus());
	}
}
