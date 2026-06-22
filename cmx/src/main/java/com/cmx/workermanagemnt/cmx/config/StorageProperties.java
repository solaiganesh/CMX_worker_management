package com.cmx.workermanagemnt.cmx.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cmx.storage")
public class StorageProperties {

	private String type = "excel";
	private Excel excel = new Excel();

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Excel getExcel() {
		return excel;
	}

	public void setExcel(Excel excel) {
		this.excel = excel;
	}

	public static class Excel {

		private String basePath = "./data";
		private String basicInfoFile = "workers_basic_info.xlsx";
		private String skillsFile = "workers_skills.xlsx";
		private String ratingsFile = "workers_ratings.xlsx";
		private String availabilityFile = "workers_availability.xlsx";

		public String getBasePath() {
			return basePath;
		}

		public void setBasePath(String basePath) {
			this.basePath = basePath;
		}

		public String getBasicInfoFile() {
			return basicInfoFile;
		}

		public void setBasicInfoFile(String basicInfoFile) {
			this.basicInfoFile = basicInfoFile;
		}

		public String getSkillsFile() {
			return skillsFile;
		}

		public void setSkillsFile(String skillsFile) {
			this.skillsFile = skillsFile;
		}

		public String getRatingsFile() {
			return ratingsFile;
		}

		public void setRatingsFile(String ratingsFile) {
			this.ratingsFile = ratingsFile;
		}

		public String getAvailabilityFile() {
			return availabilityFile;
		}

		public void setAvailabilityFile(String availabilityFile) {
			this.availabilityFile = availabilityFile;
		}
	}
}
