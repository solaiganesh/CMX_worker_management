package com.cmx.workermanagemnt.cmx.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.web.servlet.MvcResult;

import com.cmx.workermanagemnt.cmx.config.StorageProperties;
import com.cmx.workermanagemnt.cmx.repository.excel.ExcelFileSupport;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class WorkerAvailabilityIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

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
	void searchReturnsOnlyWorkersAvailableOnRequestedDate() throws Exception {
		registerWorker("Rajesh Kumar", "+919876543210", "rajesh@example.com");
		String priyaId = registerWorker("Priya Sharma", "+919876543211", "priya@example.com");

		mockMvc.perform(put("/api/v1/workers/" + priyaId + "/availability")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "entries": [
								    { "date": "2026-06-15", "available": true }
								  ]
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.entries[0].available").value(true));

		mockMvc.perform(post("/api/v1/workers/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "availableDate": "2026-06-15"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.items[0].fullName").value("Priya Sharma"));

		mockMvc.perform(post("/api/v1/workers/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "availableDate": "2026-06-16"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(0));
	}

	@Test
	void updatesExistingAvailabilityForSameDate() throws Exception {
		String workerId = registerWorker("Amit Verma", "+919876543212", "amit@example.com");

		mockMvc.perform(put("/api/v1/workers/" + workerId + "/availability")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "entries": [
								    { "date": "2026-06-20", "available": true }
								  ]
								}
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(put("/api/v1/workers/" + workerId + "/availability")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "entries": [
								    { "date": "2026-06-20", "available": false }
								  ]
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.entries[0].available").value(false));

		mockMvc.perform(get("/api/v1/workers/" + workerId + "/availability"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.entries.length()").value(1))
				.andExpect(jsonPath("$.entries[0].available").value(false));
	}

	private String registerWorker(String fullName, String mobile, String email) throws Exception {
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
				    "primarySkill": "Welding",
				    "experienceYears": 5,
				    "skillLevel": "INTERMEDIATE",
				    "workType": "FULL_TIME",
				    "languagesSpoken": ["hi"]
				  }
				}
				""".formatted(fullName, mobile, email);

		MvcResult result = mockMvc.perform(post("/api/v1/workers/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isCreated())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
	}
}
