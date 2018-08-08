/*
 * Copyright (c) 2018 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.examples;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * DEMONSTRATES HOW YOU CAN CONFIGURE THE PARSER, STEP BY STEP.
 *
 * Runs against this Real Estate website: https://harcourts.co.za/
 */
public class RealEstateJSoupParserExample {

	public static void main(String... args) throws IOException {
		String locationCode = "22008";

		String url = String.format("https://harcourts.co.za/Property/Residential?search=&location=%s&proptype=&min=&max=&minbed=&maxbed=&formsearch=true&page=1", locationCode);

		Document htmlDocument = Jsoup.connect(url).get();

		// Lists the links in the first page of results.
		List<String> firstPageResultsLinks = step1GetSearchResultLinks(htmlDocument);

		// Lists the links on 3 pages of results.
//		HtmlEntityList entityList = step2AddPagination();

		// Visits each link of each page and lists the data collected from them.
//		HtmlEntityList entityList = step3FollowTheLinks();

		// Will download HTML and resources into a "realEstate" folder in your Downloads dir in the FIRST RUN only. Run it multiple times and it will NOT visit a second time.
//		HtmlEntityList entityList = step4SavePagesLocally();

		for (String link : firstPageResultsLinks) {
			System.out.println(link);
		}
	}

	/**
	 * Returns the links of all properties in the first page of results.
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
	 */
	private static void step2AddPagination() {
	}

	/**
	 * Follows the links of all properties and captures their data.
	 */
	private static void step3FollowTheLinks() {
	}

	private static void step4SavePagesLocally() {
	}
}
