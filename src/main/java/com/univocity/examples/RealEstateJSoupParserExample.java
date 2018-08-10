/*
 * Copyright (c) 2018 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.examples;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DEMONSTRATES HOW TO PARSE SITE VIA JSOUP LIBRARY, STEP BY STEP.
 *
 * Runs against this Real Estate website: https://harcourts.co.za/
 */
public class RealEstateJSoupParserExample {

	/**
	 * Basic site URL.
	 */
	private static final String HARCOURTS_CO_ZA_URL = "https://harcourts.co.za";

	/**
	 * Entry point to application.
	 *
	 * @param args			- console arguments
	 *
	 * @throws IOException -
	 */
	public static void main(String... args) throws IOException {
		String locationCode = "22008";

		String url = String.format(HARCOURTS_CO_ZA_URL + "/Property/Residential?search=&location=%s&proptype=&min=&max=&minbed=&maxbed=&formsearch=true&page=1", locationCode);

		Document mainHtmlDocument = Jsoup.connect(url).get();

		// Lists the links in the first page of results.
		//List<String> informationStrings = step1GetSearchResultLinks(htmlDocument);

		// Lists the links on 3 pages of results.
		//List<String> informationStrings = step2AddPagination(htmlDocument);

		// Visits each link of each page and lists the data collected from them.
		//List<String> informationStrings = step3FollowTheLinks(mainHtmlDocument);

		// Will download HTML and resources into a "realEstate" folder in your Downloads dir in the FIRST RUN only. Run it multiple times and it will NOT visit a second time.
		List<String> informationStrings = step4SavePagesLocally(url, locationCode);

		for (String link : informationStrings) {
			System.out.println(link);
		}
	}

	/**
	 * Returns the links of all properties in the first page of results.
	 *
	 * @param htmlDocument - source JSoup HTML document
	 *
	 * @return List<String>
	 */
	private static List<String> step1GetSearchResultLinks(Document htmlDocument) {
		Elements linkElements = htmlDocument.select("div#galleryView > ul > li > div.listingContent > h2 > a");

		List<String> links = new ArrayList<>();

		for (Element linkElement : linkElements) {
			links.add(linkElement.attr("href"));
		}

		return links;
	}

	/**
	 * Returns the links of all properties in 3 pages of results.
	 *
	 * @param htmlDocument - source JSoup HTML document
	 *
	 * @throws IOException -
	 *
	 * @return List<String>
	 */
	private static List<String> step2AddPagination(Document htmlDocument) throws IOException {
		Elements linkElements = htmlDocument.select("div#galleryView > ul > li > div.listingContent > h2 > a");

		List<String> links = new ArrayList<>();

		for (Element linkElement : linkElements) {
			links.add(linkElement.attr("href"));
		}

		Elements pageLinkElements = htmlDocument.select("div#pager > ul > li.pagerCount > a");

		// Just to get only results from first 3 pages.
		for (int pageIndex = 0; pageIndex < 2; pageIndex++) {
			String nextPageLink = pageLinkElements.get(pageIndex).attr("href");

			Document nextPageDocument = Jsoup.connect("https://harcourts.co.za" + nextPageLink).get();

			linkElements = nextPageDocument.select("div#galleryView > ul > li > div.listingContent > h2 > a");

			for (Element linkElement : linkElements) {
				links.add(linkElement.attr("href"));
			}
		}

		return links;
	}

	/**
	 * Follows the links of all properties and captures their data.
	 *
	 * @param htmlDocument - source JSoup HTML document
	 *
	 * @throws IOException -
	 *
	 * @return List<String>
	 */
	private static List<String> step3FollowTheLinks(Document htmlDocument) throws IOException {
		Elements linkElements = htmlDocument.select("div#galleryView > ul > li > div.listingContent > h2 > a");

		List<String> pagesInfos = new ArrayList<>();

		for (Element linkElement : linkElements) {
			String detailPageLink = linkElement.attr("href");

			Document detailPageDocument = Jsoup.connect(HARCOURTS_CO_ZA_URL + detailPageLink).get();

			Element listingNumberElement = detailPageDocument.select("div.listingInfo > span > strong:contains(Listing Number:)").first();
			String listingNumber = listingNumberElement.parent().text();

			Element addressElement = detailPageDocument.select("div#listingDetail > div#detailTitle > h2.detailAddress").first();
			String address = addressElement.text();

			Element priceElement = detailPageDocument.select("div#listingDetail > div#detailTitle > div.propFeatures h3#listingViewDisplayPrice").first();
			String price = priceElement.text();

			Element bedroomsElement = detailPageDocument.select("div#listingDetail > div#detailTitle > div.propFeatures > ul#detailFeatures > li.bdrm > span").first();
			String bedroomsNumber = "";

			if (Objects.nonNull(bedroomsElement)) {
				bedroomsNumber = bedroomsElement.text();
			}

			Element bathroomsElement = detailPageDocument.select("div#listingDetail > div#detailTitle > div.propFeatures > ul#detailFeatures > li.bthrm > span").first();
			String bathroomsNumber = "";

			if (Objects.nonNull(bathroomsElement)) {
				bathroomsNumber = bathroomsElement.text();
			}

			Element landSizeElement = detailPageDocument.select("div.property-information > ul > li > span:contains(Land Size:)").first();
			String landSize = "";

			if (Objects.nonNull(landSizeElement)) {
				landSize = landSizeElement.parent().text();
			}

			Element propertyTypeElement = detailPageDocument.select("div.property-information > ul > li > span:contains(Property Type:)").first();
			String propertyType = propertyTypeElement.parent().text();

			String listingInfoString = detailPageLink + "," +
					listingNumber + "," +
					address + "," +
					price + "," +
					bedroomsNumber + "," +
					bathroomsNumber + "," +
					landSize + "," +
					propertyType + ",";

			pagesInfos.add(listingInfoString);
		}

		Elements pageLinkElements = htmlDocument.select("div#pager > ul > li.pagerCount > a");

		// Just to get only results from first 3 pages.
		for (int pageIndex = 0; pageIndex < 2; pageIndex++) {
			String nextPageLink = pageLinkElements.get(pageIndex).attr("href");

			Document nextPageDocument = Jsoup.connect(HARCOURTS_CO_ZA_URL + nextPageLink).get();

			linkElements = nextPageDocument.select("div#galleryView > ul > li > div.listingContent > h2 > a");

			for (Element linkElement : linkElements) {
				String detailPageLink = linkElement.attr("href");


				Document detailPageDocument = Jsoup.connect(HARCOURTS_CO_ZA_URL + detailPageLink).get();

				Element listingNumberElement = detailPageDocument.select("div.listingInfo > span > strong:contains(Listing Number:)").first();
				String listingNumber = listingNumberElement.parent().text();

				Element addressElement = detailPageDocument.select("div#listingDetail > div#detailTitle > h2.detailAddress").first();
				String address = addressElement.text();

				Element priceElement = detailPageDocument.select("div#listingDetail > div#detailTitle > div.propFeatures h3#listingViewDisplayPrice").first();
				String price = priceElement.text();

				Element bedroomsElement = detailPageDocument.select("div#listingDetail > div#detailTitle > div.propFeatures > ul#detailFeatures > li.bdrm > span").first();
				String bedroomsNumber = "";

				if (Objects.nonNull(bedroomsElement)) {
					bedroomsNumber = bedroomsElement.text();
				}

				Element bathroomsElement = detailPageDocument.select("div#listingDetail > div#detailTitle > div.propFeatures > ul#detailFeatures > li.bthrm > span").first();
				String bathroomsNumber = "";

				if (Objects.nonNull(bathroomsElement)) {
					bathroomsNumber = bathroomsElement.text();
				}

				Element landSizeElement = detailPageDocument.select("div.property-information > ul > li > span:contains(Land Size:)").first();
				String landSize = "";

				if (Objects.nonNull(landSizeElement)) {
					landSize = landSizeElement.parent().text();
				}

				Element propertyTypeElement = detailPageDocument.select("div.property-information > ul > li > span:contains(Property Type:)").first();
				String propertyType = propertyTypeElement.parent().text();

				String listingInfoString = detailPageLink + "," +
						listingNumber + "," +
						address + "," +
						price + "," +
						bedroomsNumber + "," +
						bathroomsNumber + "," +
						landSize + "," +
						propertyType + ",";

				pagesInfos.add(listingInfoString);
			}
		}

		return pagesInfos;
	}

	/**
	 * Save all pages to files on local computer in Downloads/realEstate_Jsoup folder.
	 *
	 * @param url			- first (start) URL
	 * @param locationCode 	- location code
	 *
	 * @throws IOException -
	 *
	 * @return List<String>
	 */
	private static List<String> step4SavePagesLocally(String url, String locationCode) throws IOException {
		final Connection.Response response = Jsoup.connect(url).execute();
		final Document htmlDocument = response.parse();

		File downloadsDirectory = new File(System.getProperty("user.home"), "Downloads");
		File realEstateJsoupDirectory = new File(downloadsDirectory, "realEstate_Jsoup");

		if (!realEstateJsoupDirectory.exists()) {
			realEstateJsoupDirectory.mkdirs();
		}

		ZonedDateTime currentDateTime = ZonedDateTime.now();

		String currentDateStr = currentDateTime.getYear() + "-" + currentDateTime.getMonthValue() + "-" + currentDateTime.getDayOfMonth();

		File currentDayDirectory = new File(realEstateJsoupDirectory, currentDateStr);

		if (currentDayDirectory.exists()) {
			return new ArrayList<>();
		}

		currentDayDirectory.mkdirs();

		saveHtmlFile(currentDayDirectory, locationCode + "_0001.html", htmlDocument);

		File firstPageResultsDirectory = new File(currentDayDirectory, locationCode + "_0001");
		firstPageResultsDirectory.mkdirs();

		Elements linkElements = htmlDocument.select("div#galleryView > ul > li > div.listingContent > h2 > a");

		List<String> pagesInfos = new ArrayList<>();

		for (Element linkElement : linkElements) {
			String detailPageLink = linkElement.attr("href");

			Document detailPageDocument = Jsoup.connect(HARCOURTS_CO_ZA_URL + detailPageLink).get();

			Element listingNumberElement = detailPageDocument.select("div.listingInfo > span > strong:contains(Listing Number:)").first();
			String listingNumber = listingNumberElement.parent().text();

			Element addressElement = detailPageDocument.select("div#listingDetail > div#detailTitle > h2.detailAddress").first();
			String address = addressElement.text();

			Element priceElement = detailPageDocument.select("div#listingDetail > div#detailTitle > div.propFeatures h3#listingViewDisplayPrice").first();
			String price = priceElement.text();

			Element bedroomsElement = detailPageDocument.select("div#listingDetail > div#detailTitle > div.propFeatures > ul#detailFeatures > li.bdrm > span").first();
			String bedroomsNumber = "";

			if (Objects.nonNull(bedroomsElement)) {
				bedroomsNumber = bedroomsElement.text();
			}

			Element bathroomsElement = detailPageDocument.select("div#listingDetail > div#detailTitle > div.propFeatures > ul#detailFeatures > li.bthrm > span").first();
			String bathroomsNumber = "";

			if (Objects.nonNull(bathroomsElement)) {
				bathroomsNumber = bathroomsElement.text();
			}

			Element landSizeElement = detailPageDocument.select("div.property-information > ul > li > span:contains(Land Size:)").first();
			String landSize = "";

			if (Objects.nonNull(landSizeElement)) {
				landSize = landSizeElement.parent().text();
			}

			Element propertyTypeElement = detailPageDocument.select("div.property-information > ul > li > span:contains(Property Type:)").first();
			String propertyType = propertyTypeElement.parent().text();

			String listingInfoString = detailPageLink + "," +
					listingNumber + "," +
					address + "," +
					price + "," +
					bedroomsNumber + "," +
					bathroomsNumber + "," +
					landSize + "," +
					propertyType + ",";

			pagesInfos.add(listingInfoString);

			String listingFileName = listingNumber.replaceAll("Listing Number: ", "").trim() + ".html";
			saveHtmlFile(firstPageResultsDirectory, listingFileName, detailPageDocument);
		}

		Elements pageLinkElements = htmlDocument.select("div#pager > ul > li.pagerCount > a");

		// Just to get only results from first 3 pages.
		for (int pageIndex = 0; pageIndex < 2; pageIndex++) {
			String nextPageLink = pageLinkElements.get(pageIndex).attr("href");

			Document nextPageDocument = Jsoup.connect(HARCOURTS_CO_ZA_URL + nextPageLink).get();

			int directoryIndex = pageIndex + 2;

			saveHtmlFile(currentDayDirectory, locationCode + "_000" + directoryIndex + ".html", nextPageDocument);

			File pageResultsDirectory = new File(currentDayDirectory, locationCode + "_000" + directoryIndex);
			pageResultsDirectory.mkdirs();

			linkElements = nextPageDocument.select("div#galleryView > ul > li > div.listingContent > h2 > a");

			for (Element linkElement : linkElements) {
				String detailPageLink = linkElement.attr("href");


				Document detailPageDocument = Jsoup.connect(HARCOURTS_CO_ZA_URL + detailPageLink).get();

				Element listingNumberElement = detailPageDocument.select("div.listingInfo > span > strong:contains(Listing Number:)").first();
				String listingNumber = listingNumberElement.parent().text();

				Element addressElement = detailPageDocument.select("div#listingDetail > div#detailTitle > h2.detailAddress").first();
				String address = addressElement.text();

				Element priceElement = detailPageDocument.select("div#listingDetail > div#detailTitle > div.propFeatures h3#listingViewDisplayPrice").first();
				String price = priceElement.text();

				Element bedroomsElement = detailPageDocument.select("div#listingDetail > div#detailTitle > div.propFeatures > ul#detailFeatures > li.bdrm > span").first();
				String bedroomsNumber = "";

				if (Objects.nonNull(bedroomsElement)) {
					bedroomsNumber = bedroomsElement.text();
				}

				Element bathroomsElement = detailPageDocument.select("div#listingDetail > div#detailTitle > div.propFeatures > ul#detailFeatures > li.bthrm > span").first();
				String bathroomsNumber = "";

				if (Objects.nonNull(bathroomsElement)) {
					bathroomsNumber = bathroomsElement.text();
				}

				Element landSizeElement = detailPageDocument.select("div.property-information > ul > li > span:contains(Land Size:)").first();
				String landSize = "";

				if (Objects.nonNull(landSizeElement)) {
					landSize = landSizeElement.parent().text();
				}

				Element propertyTypeElement = detailPageDocument.select("div.property-information > ul > li > span:contains(Property Type:)").first();
				String propertyType = propertyTypeElement.parent().text();

				String listingInfoString = detailPageLink + "," +
						listingNumber + "," +
						address + "," +
						price + "," +
						bedroomsNumber + "," +
						bathroomsNumber + "," +
						landSize + "," +
						propertyType + ",";

				pagesInfos.add(listingInfoString);

				String listingFileName = listingNumber.replaceAll("Listing Number: ", "").trim() + ".html";
				saveHtmlFile(pageResultsDirectory, listingFileName, detailPageDocument);
			}
		}

		return pagesInfos;
	}

	/**
	 * Save {@link Document} object (HTML content) to file.
	 *
	 * @param parentDirectory	- parent directory
	 * @param fileName			- file name
	 * @param htmlDocument		- source HTML document
	 *
	 * @throws IOException -
	 */
	private static void saveHtmlFile(File parentDirectory, String fileName, Document htmlDocument) throws IOException {
		final File firstPageFile = new File(parentDirectory, fileName);

		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(firstPageFile));
		bufferedWriter.write(htmlDocument.outerHtml());

		bufferedWriter.close();
	}
}
