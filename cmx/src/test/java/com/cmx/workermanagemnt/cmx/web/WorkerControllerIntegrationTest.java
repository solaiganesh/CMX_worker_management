package com.cmx.workermanagemnt.cmx.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

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
class WorkerControllerIntegrationTest {

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
	void registersWorkerAndReturnsLocalizedResponse() throws Exception {
		String payload = """
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
				    "secondarySkills": ["Painting"],
				    "experienceYears": 5.5,
				    "skillLevel": "INTERMEDIATE",
				    "certifications": ["ITI Welding"],
				    "toolsOwned": ["Arc welder"],
				    "workType": "FULL_TIME",
				    "languagesSpoken": ["hi", "en"]
				  }
				}
				""";

		MvcResult registerResult = mockMvc.perform(post("/api/v1/workers/register")
						.contentType(MediaType.APPLICATION_JSON)
						.header("Accept-Language", "hi")
						.content(payload))
				.andExpect(status().isCreated())
				.andExpect(header().exists("X-Correlation-Id"))
				.andExpect(jsonPath("$.city").value("चेन्नई"))
				.andExpect(jsonPath("$.primarySkill").value("वेल्डिंग"))
				.andReturn();

		String id = objectMapper.readTree(registerResult.getResponse().getContentAsString()).get("id").asText();

		mockMvc.perform(get("/api/v1/workers/{id}", id)
						.header("Accept-Language", "en"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.city").value("Chennai"))
				.andExpect(jsonPath("$.primarySkill").value("Welding"));
	}

	@Test
	void returnsValidationErrorEnvelope() throws Exception {
		mockMvc.perform(post("/api/v1/workers/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.correlationId").isNotEmpty());
	}
}
