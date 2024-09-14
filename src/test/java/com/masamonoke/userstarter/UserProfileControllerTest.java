package com.masamonoke.userstarter;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masamonoke.userstarter.model.Role;
import com.masamonoke.userstarter.model.User;
import com.masamonoke.userstarter.repo.UserRepo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
class UserProfileControllerTest {
	private MockMvc mockMvc;
	@Autowired
	private WebApplicationContext context;
	@Autowired
	private ObjectMapper objectMapper;
	private static User user;
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private PasswordEncoder encoder;

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

	private String registerUser(User user) throws Exception {
		var json = objectMapper.writeValueAsString(user);
		var res = mockMvc.perform(post("/api/v1/auth/register").content(json).contentType(MediaType.APPLICATION_JSON)).andReturn();
		assertEquals(200, res.getResponse().getStatus());

		var jsonToken = res.getResponse().getContentAsString();
		return (String) objectMapper.readValue(jsonToken, HashMap.class).get("access_token");
	}

	@Test
    void getUserByIdTest() throws Exception {
		var accessToken = registerUser(user);
		var userToGet = userRepo.findAll().get(0);
		var res = mockMvc.perform(get("/api/v1/profile/user").param("id", userToGet.getId().toString()).header("Authorization", "Bearer " + accessToken))
			.andReturn();
		assertEquals(200, res.getResponse().getStatus());
	}

	@Test
	public void updateUsernameTest() throws Exception {
		var accessToken = registerUser(user);
		var userToEdit = userRepo.findAll().get(0);
		var username = "Sas";
		var res = mockMvc.perform(put("/api/v1/profile/user/username")
				.param("id", userToEdit.getId().toString()).param("username", username).header("Authorization", "Bearer " + accessToken))
			.andReturn();
		assertEquals(200, res.getResponse().getStatus());

		var returnedJson = res.getResponse().getContentAsString();
		var savedUsername = objectMapper.readValue(returnedJson, Map.class).get("username");
		assertEquals(username, savedUsername);
	}

	@Test
	public void updatePasswordTest() throws Exception {
		var accessToken = registerUser(user);
		var userToEdit = userRepo.findAll().get(0);
		var passwordToSave = "12345678";
		var data = String.format("""
			{
				"id": "%s",
				"password": "%s"
			}
		""", userToEdit.getId().toString(), passwordToSave);
		var res = mockMvc.perform(put("/api/v1/profile/user/password").contentType(MediaType.APPLICATION_JSON).content(data).header("Authorization", "Bearer " + accessToken))
			.andReturn();

		assertEquals(200, res.getResponse().getStatus());

		var returnedJson = res.getResponse().getContentAsString();
		var savedPassword = (String) objectMapper.readValue(returnedJson, Map.class).get("password");
		var isMatch = encoder.matches(passwordToSave, savedPassword);

		assertTrue(isMatch);
	}

	@Test
	public void deleteAccountAndRestore() throws Exception {
		var accessToken = registerUser(user);
		var userToDelete = userRepo.findAll().get(0);

		Assertions.assertTrue(userToDelete.isEnabled());

		var res = mockMvc.perform(delete("/api/v1/profile/user").param("id", userToDelete.getId().toString()).header("Authorization", "Bearer " + accessToken))
			.andReturn();

		// TODO: somehow mock email confirmation
		assertEquals(409, res.getResponse().getStatus());
	}
}
