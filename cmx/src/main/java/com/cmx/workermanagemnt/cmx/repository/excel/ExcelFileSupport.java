package com.cmx.workermanagemnt.cmx.repository.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cmx.workermanagemnt.cmx.config.StorageProperties;
import com.cmx.workermanagemnt.cmx.exception.StorageException;

@Component
public class ExcelFileSupport {

	private static final Logger log = LoggerFactory.getLogger(ExcelFileSupport.class);
	static final String LIST_DELIMITER = "\\|";

	private final StorageProperties storageProperties;

	public ExcelFileSupport(StorageProperties storageProperties) {
		this.storageProperties = storageProperties;
	}

	public Path getBasicInfoPath() {
		return Path.of(storageProperties.getExcel().getBasePath(), storageProperties.getExcel().getBasicInfoFile());
	}

	public Path getSkillsPath() {
		return Path.of(storageProperties.getExcel().getBasePath(), storageProperties.getExcel().getSkillsFile());
	}

	public Path getRatingsPath() {
		return Path.of(storageProperties.getExcel().getBasePath(), storageProperties.getExcel().getRatingsFile());
	}

	public void initializeWorkbooks() {
		initializeWorkbook(getBasicInfoPath(), WorkerBasicInfoHeaders.HEADERS);
		initializeWorkbook(getSkillsPath(), WorkerSkillsHeaders.HEADERS);
		initializeWorkbook(getRatingsPath(), WorkerRatingHeaders.HEADERS);
	}

	public synchronized void withWorkbook(Path path, WorkbookCallback callback) {
		try {
			Workbook workbook = openWorkbook(path);
			try {
				callback.execute(workbook);
				writeWorkbook(path, workbook);
			}
			finally {
				workbook.close();
			}
		}
		catch (IOException ex) {
			throw new StorageException("Failed to access Excel file: " + path, ex);
		}
	}

	public synchronized <T> T readWorkbook(Path path, WorkbookReader<T> reader) {
		try {
			if (!Files.exists(path)) {
				return reader.read(null);
			}
			try (Workbook workbook = openWorkbook(path)) {
				return reader.read(workbook);
			}
		}
		catch (IOException ex) {
			throw new StorageException("Failed to read Excel file: " + path, ex);
		}
	}

	static String joinList(List<String> values) {
		if (values == null || values.isEmpty()) {
			return "";
		}
		return String.join("|", values);
	}

	static List<String> splitList(String value) {
		if (value == null || value.isBlank()) {
			return new ArrayList<>();
		}
		return Arrays.stream(value.split(LIST_DELIMITER))
				.map(String::trim)
				.filter(part -> !part.isEmpty())
				.collect(Collectors.toCollection(ArrayList::new));
	}

	static String cellString(Cell cell) {
		if (cell == null) {
			return "";
		}
		return switch (cell.getCellType()) {
			case STRING -> cell.getStringCellValue().trim();
			case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
			case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
			case FORMULA -> cell.getStringCellValue().trim();
			default -> "";
		};
	}

	static void setCell(Row row, int index, String value) {
		Cell cell = row.createCell(index);
		cell.setCellValue(value != null ? value : "");
	}

	static LocalDate parseDate(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return LocalDate.parse(value.trim());
		}
		catch (DateTimeParseException ex) {
			throw new StorageException("Invalid date value in Excel: " + value, ex);
		}
	}

	static Instant parseInstant(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return Instant.parse(value.trim());
	}

	static BigDecimal parseDecimal(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return new BigDecimal(value.trim());
	}

	static int findColumnIndex(Row headerRow, String headerName) {
		for (Cell cell : headerRow) {
			if (headerName.equalsIgnoreCase(cellString(cell))) {
				return cell.getColumnIndex();
			}
		}
		return -1;
	}

	static int appendRow(Sheet sheet) {
		return sheet.getLastRowNum() + 1;
	}

	private void initializeWorkbook(Path path, List<String> headers) {
		try {
			Files.createDirectories(path.getParent());
			if (Files.exists(path) && Files.size(path) > 0) {
				return;
			}
			try (Workbook workbook = new XSSFWorkbook()) {
				Sheet sheet = workbook.createSheet("data");
				Row headerRow = sheet.createRow(0);
				for (int i = 0; i < headers.size(); i++) {
					setCell(headerRow, i, headers.get(i));
				}
				writeWorkbook(path, workbook);
			}
			log.info("Initialized Excel workbook at {}", path);
		}
		catch (IOException ex) {
			throw new StorageException("Failed to initialize Excel file: " + path, ex);
		}
	}

	private Workbook openWorkbook(Path path) throws IOException {
		if (!Files.exists(path)) {
			return new XSSFWorkbook();
		}
		try (InputStream inputStream = Files.newInputStream(path)) {
			return new XSSFWorkbook(inputStream);
		}
	}

	private void writeWorkbook(Path path, Workbook workbook) throws IOException {
		Files.createDirectories(path.getParent());
		try (OutputStream outputStream = Files.newOutputStream(path)) {
			workbook.write(outputStream);
		}
	}

	@FunctionalInterface
	public interface WorkbookCallback {
		void execute(Workbook workbook) throws IOException;
	}

	@FunctionalInterface
	public interface WorkbookReader<T> {
		T read(Workbook workbook) throws IOException;
	}

	static final class WorkerBasicInfoHeaders {
		static final List<String> HEADERS = Collections.unmodifiableList(List.of(
				"id", "full_name", "mobile_number", "email", "profile_photo_url", "date_of_birth",
				"gender", "city", "state", "address", "pincode", "latitude", "longitude",
				"primary_language", "availability_status", "created_at", "updated_at"));

		private WorkerBasicInfoHeaders() {
		}
	}

	static final class WorkerSkillsHeaders {
		static final List<String> HEADERS = Collections.unmodifiableList(List.of(
				"user_id", "primary_skill", "secondary_skills", "experience_years", "skill_level",
				"certifications", "tools_owned", "work_type", "languages_spoken", "portfolio_images"));

		private WorkerSkillsHeaders() {
		}
	}

	static final class WorkerRatingHeaders {
		static final List<String> HEADERS = Collections.unmodifiableList(List.of(
				"id", "worker_id", "score", "reviewer_name", "comment", "created_at"));

		private WorkerRatingHeaders() {
		}
	}
}
