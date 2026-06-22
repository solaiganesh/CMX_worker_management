package com.cmx.workermanagemnt.cmx.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import com.cmx.workermanagemnt.cmx.config.StorageProperties;
import com.cmx.workermanagemnt.cmx.repository.excel.ExcelFileSupport;

@SpringBootTest
@AutoConfigureMockMvc
class WorkerSearchRatingIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StorageProperties storageProperties;

	@Autowired
	private ExcelFileSupport excelFileSupport;

	@BeforeEach
	void setUp(@TempDir java.nio.file.Path tempDir) {
		storageProperties.getExcel().setBasePath(tempDir.toString());
		excelFileSupport.initializeWorkbooks();
	}

	@Test
	void searchFilterAndRateWorker() throws Exception {
		String registerPayload = """
				{
				  "basicInfo": {
				    "fullName": "Rajesh Kumar",
				    "mobileNumber": "+919876543210",
				    "email": "rajesh@example.com",
				    "dateOfBirth": "1990-05-15",
				    "gender": "MALE",
				    "city": "Chennai",
				    "state": "Tamil Nadu",
				    "address": "12 MG Road",
				    "pincode": "600001",
				    "primaryLanguage": "hi",
				    "availabilityStatus": "AVAILABLE"
				  },
				  "skills": {
				    "primarySkill": "Welding",
				    "experienceYears": 5.5,
				    "skillLevel": "INTERMEDIATE",
				    "workType": "FULL_TIME",
				    "languagesSpoken": ["hi", "en"]
				  }
				}
				""";

		mockMvc.perform(post("/api/v1/workers/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registerPayload))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/workers/search")
						.contentType(MediaType.APPLICATION_JSON)
						.header("Accept-Language", "hi")
						.content("""
								{
								  "city": "Chennai",
								  "minExperience": 3,
								  "minAge": null,
								  "maxAge": null
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.items[0].fullName").value("Rajesh Kumar"))
				.andExpect(jsonPath("$.items[0].city").value("चेन्नई"))
				.andExpect(jsonPath("$.items[0].experienceYears").value(5.5))
				.andExpect(jsonPath("$.items[0].averageRating").value(0.0));

		mockMvc.perform(post("/api/v1/workers/ratings")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "workerPhone": "+919876543210",
								  "score": 4,
								  "reviewerName": "ABC Contractors"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.averageRating").value(4.0))
				.andExpect(jsonPath("$.ratingCount").value(1));

		mockMvc.perform(post("/api/v1/workers/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "minRating": 4.0
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.items[0].averageRating").value(4.0));
	}

	@Test
	void emptySearchReturnsAllWorkers() throws Exception {
		String registerPayload = """
				{
				  "basicInfo": {
				    "fullName": "Priya Sharma",
				    "mobileNumber": "+919876543211",
				    "email": "priya@example.com",
				    "dateOfBirth": "1992-03-20",
				    "gender": "FEMALE",
				    "city": "Chennai",
				    "state": "Tamil Nadu",
				    "address": "12 MG Road",
				    "pincode": "600001",
				    "primaryLanguage": "hi",
				    "availabilityStatus": "AVAILABLE"
				  },
				  "skills": {
				    "primarySkill": "Welding",
				    "experienceYears": 2,
				    "skillLevel": "INTERMEDIATE",
				    "workType": "FULL_TIME",
				    "languagesSpoken": ["hi"]
				  }
				}
				""";

		mockMvc.perform(post("/api/v1/workers/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registerPayload))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/workers/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(1));
	}

	@Test
	void searchBySkillMatchesPrimaryOrSecondary() throws Exception {
		registerWorker("Rajesh Kumar", "+919876543210", "rajesh@example.com", "Welding", null);
		registerWorker("Priya Sharma", "+919876543211", "priya@example.com", "Plumbing", null);
		registerWorker("Amit Singh", "+919876543212", "amit@example.com", "Painting", List.of("Welding"));

		mockMvc.perform(post("/api/v1/workers/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "skill": "Welding"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(2));

		mockMvc.perform(post("/api/v1/workers/search")
						.contentType(MediaType.APPLICATION_JSON)
						.header("Content-Language", "hi")
						.content("""
								{
								  "skill": "वेल्डिंग"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(2));

		mockMvc.perform(post("/api/v1/workers/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "skill": "Plumbing"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.items[0].fullName").value("Priya Sharma"));
	}

	private void registerWorker(String fullName, String mobile, String email, String primarySkill,
			List<String> secondarySkills) throws Exception {
		String secondaryJson = secondarySkills == null ? "[]"
				: secondarySkills.stream().map(skill -> "\"" + skill + "\"").reduce((left, right) -> left + ", " + right)
						.map(skills -> "[" + skills + "]").orElse("[]");
		String payload = """
				{
				  "basicInfo": {
				    "fullName": "%s",
				    "mobileNumber": "%s",
				    "email": "%s",
				    "dateOfBirth": "1990-05-15",
				    "gender": "MALE",
				    "city": "Chennai",
				    "state": "Tamil Nadu",
				    "address": "12 MG Road",
				    "pincode": "600001",
				    "primaryLanguage": "hi",
				    "availabilityStatus": "AVAILABLE"
				  },
				  "skills": {
				    "primarySkill": "%s",
				    "secondarySkills": %s,
				    "experienceYears": 5,
				    "skillLevel": "INTERMEDIATE",
				    "workType": "FULL_TIME",
				    "languagesSpoken": ["hi"]
				  }
				}
				""".formatted(fullName, mobile, email, primarySkill, secondaryJson);

		mockMvc.perform(post("/api/v1/workers/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isCreated());
	}

	@Test
	void rejectsRatingWithoutWorkerIdentifier() throws Exception {
		mockMvc.perform(post("/api/v1/workers/ratings")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "score": 4
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}
}
