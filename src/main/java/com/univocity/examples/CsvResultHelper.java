package com.univocity.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.univocity.api.entity.html.HtmlParserResult;
import com.univocity.api.entity.html.HtmlRecord;
import com.univocity.parsers.csv.Csv;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

/**
 * Helper class to save results to CSV file.
 */
class CsvResultHelper {

	/**
	 * Save raw results map to '/{user.home}/Downloads/{parserName}.csv' file.
	 *
	 * @param parserName - parser name
	 * @param results	 - results map
	 */
	static void saveResults(String parserName, List<Map<String, String>> results) {
		File resultCsvFile = new File(System.getProperty("user.home") + "/Downloads/" + parserName + ".csv");

		System.out.println(parserName + " captured " + results.size() + " records. Results saved to " + resultCsvFile.getAbsolutePath());

		CsvWriterSettings csvWriterSettings = Csv.writeExcel();
		csvWriterSettings.setHeaders(results.get(0).keySet().toArray(new String[0]));
		csvWriterSettings.setHeaderWritingEnabled(true);
		csvWriterSettings.setNullValue("N/A");

		CsvWriter csvWriter = new CsvWriter(resultCsvFile, "windows-1252", csvWriterSettings);

		for (Map<String, String> record : results) {
			csvWriter.writeRow(record);
		}

		csvWriter.close();
	}

	/**
	 * Convert Univocity results to raw results (map) and save to '/{user.home}/Downloads/univocity-{entityName}.csv' file.
	 *
	 * @param result - univocity HtmlParserResult results object
	 */
	static void saveResults(HtmlParserResult result) {
		List<Map<String, String>> tmp = new ArrayList<>();

		for (HtmlRecord record : result.iterateRecords()) {
			tmp.add(record.fillFieldMap(new LinkedHashMap<>()));
		}
		
		saveResults("univocity-" + result.getEntityName(), tmp);
	}
}
