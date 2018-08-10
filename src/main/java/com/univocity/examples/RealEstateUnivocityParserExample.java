/*
 * Copyright (c) 2018 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.examples;

import com.univocity.api.entity.html.*;
import com.univocity.api.entity.html.builders.*;
import com.univocity.api.net.*;
import com.univocity.parsers.common.*;

import java.util.*;

/**
 * DEMONSTRATES HOW YOU CAN CONFIGURE THE PARSER, STEP BY STEP.
 *
 * Runs against this Real Estate website: https://harcourts.co.za/
 */
public class RealEstateUnivocityParserExample {

	public static void main(String... args) {
		String locationCode = "22008";

		/* UNCOMMENT ONE LINE AFTER THE OTHER AND RUN THE PARSER AGAIN **/
		/* EACH STEP EXPLORES ADDITIONAL CONFIGURATIONS **/

		com.univocity.parsers.html.Main.runLicenseManager();

		// Lists the links in the first page of results.
		//HtmlEntityList entityList = step1GetSearchResultLinks();

		// Lists the links on 3 pages of results.
		//HtmlEntityList entityList = step2AddPagination();

		// Visits each link of each page and lists the data collected from them.
		//HtmlEntityList entityList = step3FollowTheLinks();

		// Will download HTML and resources into a "realEstate" folder in your Downloads dir in the FIRST RUN only. Run it multiple times and it will NOT visit a second time.
		HtmlEntityList entityList = step4SavePagesLocally();

		String url = "https://harcourts.co.za/Property/Residential?search=&location={LOCATION_CODE}&proptype=&min=&max=&minbed=&maxbed=&formsearch=true&page=1";

		UrlReaderProvider urlReaderProvider = new UrlReaderProvider(url);
		HtmlParser htmlParser = new HtmlParser(entityList);

		urlReaderProvider.getRequest().setUrlParameter("LOCATION_CODE", locationCode);

		HtmlParserResult htmlParserResult = htmlParser.parse(urlReaderProvider).get("houses");

		for (String[] row : htmlParserResult.getRows()) {
			System.out.println(Arrays.toString(row));
		}
	}

	/**
	 * Returns the links of all properties in the first page of results.
	 */
	private static HtmlEntityList step1GetSearchResultLinks() {
		// The first step is to create a list of entities.
		HtmlEntityList entityList = new HtmlEntityList();

		// Configure the "houses" entity. It handles the search results that list all houses available.
		HtmlEntitySettings houses = entityList.configureEntity("houses");

		// For each search result, visit the page with individual details of the property.
		houses.addField("propertyDetailsLink") //"houses" will have a field named "propertyDetailsLink" with the URL of each property details
				.match("div").id("galleryView")
				.match("div").classes("listingContent")
				.matchNext("h2").matchNext("a").getAttribute("href");

		return entityList;
	}

	/**
	 * Returns the links of all properties in 3 pages of results.
	 */
	private static HtmlEntityList step2AddPagination() {
		// Configure parser to fetch CSS and javascript files to make downloaded pages look nice
		// the first step is to create a list of entities
		HtmlEntityList entityList = new HtmlEntityList();

		/* ADDING PAGINATOR HERE **/
		// Configure the paginator. It finds the link that points to the next page of results
		HtmlPaginator htmlPaginator = entityList.getPaginator();
		htmlPaginator.setNextPage().match("li").classes("pagerNext").matchFirst("a").getAttribute("href");

		// Makes the pagination run to up to 2 additional pages of results
		htmlPaginator.setFollowCount(2);

		/* THE REST OF THE CODE IS AS IT WAS BEFORE **/

		// Configure the "houses" entity. It handles the search results that list all houses available.
		HtmlEntitySettings houses = entityList.configureEntity("houses");

		// For each search result, visit the page with individual details of the property.
		houses.addField("propertyDetailsLink") //"houses" will have a field named "propertyDetailsLink" with the URL of each property details
				.match("div").id("galleryView")
				.match("div").classes("listingContent")
				.matchNext("h2").matchNext("a").getAttribute("href");

		return entityList;
	}

	/**
	 * Follows the links of all properties and captures their data.
	 */
	private static HtmlEntityList step3FollowTheLinks() {
		//Configure parser to fetch CSS and javascript files to make downloaded pages look nice
		//the first step is to create a list of entities
		HtmlEntityList entityList = new HtmlEntityList();

		/* ADDING PAGINATOR HERE LIKE WE DID BEFORE **/
		// Configure the paginator. It finds the link that points to the next page of results
		HtmlPaginator paginator = entityList.getPaginator();
		paginator.setNextPage().match("li").classes("pagerNext").matchFirst("a").getAttribute("href");

		// Makes the pagination run to up to 2 additional pages of results
		paginator.setFollowCount(2);

		// Configure the "houses" entity. It handles the search results that list all houses available.
		HtmlEntitySettings houses = entityList.configureEntity("houses");

		/* NOW WE ACTUALLY FOLLOW THE LINKS AND COLLECT DATA IN EACH ONE OF THEM **/

		// For each search result, visit the page with individual details of the property.
		HtmlLinkFollower houseDetails = houses.addField("propertyDetailsLink") //"houses" will have a field named "propertyDetailsLink" with the URL of each property details
				.match("div").id("galleryView")
				.match("div").classes("listingContent")
				.matchNext("h2").matchNext("a").getAttribute("href")
				.followLink(); // this creates a link follower. It will open each link automatically for us.

		// configure the link follower to join the data collected from each visited page with each record of the "houses" entity.
		houseDetails.setNesting(Nesting.JOIN);

		// captures the property details we are interested in.
		houseDetails.addField("id").match("strong").withExactText("Listing Number:").getFollowingText();
		houseDetails.addField("address").match("h2").classes("detailAddress").getText();
		houseDetails.addField("price").match("h3").id("listingViewDisplayPrice").getText();

		// creates a path to the "features" section of the page
		PartialPath info = houseDetails.newPath().match("ul").id("detailFeatures");
		info.addField("bedrooms").match("li").classes("bdrm").matchNext("span").getText();
		info.addField("bathrooms").match("li").classes("bthrm").matchNext("span").getText();

		// creates a path to the "property information" section of the page
		info = houseDetails.newPath().match("div").classes("property-information").match("li").matchNext("span").classes("heading");
		info.addField("landSize").matchCurrent().withText("Land size").getFollowingText();
		info.addField("propertyType").matchCurrent().withText("Property type").getFollowingText();

		return entityList;
	}

	private static HtmlEntityList step4SavePagesLocally() {
		/* THIS IS NEW - CONFIGURE PARSER TO FETCH PAGE RESOURCES **/
		//Configure parser to fetch CSS and javascript files to make downloaded pages look nice
		FetchOptions options = new FetchOptions();
		options.setSharedResourceDir("{user.home}/cache");
		//skip images otherwise it will download a lot of photos of each house.
		options.setDownloadHandler(context -> {
			if ("jpg".equals(context.targetFileExtension())) {
				context.skipDownload();
			}
		});

		//the first step is to create a list of entities
		HtmlEntityList entityList = new HtmlEntityList();

		/* HERE WE CONFIGURE THE PARSER TO FETCH PAGE RESOURCES **/

		//the parser config applies to all entities of the list, let's configure the parser.
		HtmlParserSettings parserSettings = entityList.getParserSettings();

		//here we configure the parser to fetch resources according to the FetchOptions set above.
		//AFTER the files are downloaded, the parser will run against the stored files.
		entityList.getParserSettings().fetchResourcesBeforeParsing(options);

		/* store everything under the `Downloads` folder in your home dir **/
		parserSettings.setDownloadContentDirectory("{user.home}/Downloads/realEstate/");

		/* CONFIGURE PARSER TO STORE HTML OF EACH PAGE OF RESULTS - where the links are listed **/
		// the parser will store files in a directory organized by date.
		// The $location parameter is the location code provided in the URL
		// The {page, 4} parameter is the page number - results are paginated.
		parserSettings.setFileNamePattern("{date, yyyy-MM-dd}/{$location}_{page, 4}.html"); // The "final" path of each page of results will be: {user.home}/Downloads/realEstate/{date, yyyy-MM-dd}/{$location}_{page, 4}.html

		// If you re-run the process on the same date we don't want to not overwrite files already
		// downloaded the day - disabling overwriting will just run on the stored files if they exist.
		// If there's MORE stuff to download then it will download the additional pages.
		parserSettings.setDownloadOverwritingEnabled(false);

		/* CONFIGURE PAGINATOR AS BEFORE **/

		// Configure the paginator. It finds the link that points to the next page of results
		HtmlPaginator paginator = entityList.getPaginator();
		paginator.setNextPage().match("li").classes("pagerNext").matchFirst("a").getAttribute("href");

		// Makes the pagination run to up to 2 additional pages of results
		paginator.setFollowCount(2);

		/* CONFIGURE ENTITIES AS BEFORE **/

		// Configure the "houses" entity. It handles the search results that list all houses available.
		HtmlEntitySettings houses = entityList.configureEntity("houses");

		// For each search result, visit the page with individual details of the property.
		HtmlLinkFollower houseDetails = houses.addField("propertyDetailsLink") //"houses" will have a field named "propertyDetailsLink" with the URL of each property details
				.match("div").id("galleryView")
				.match("div").classes("listingContent")
				.matchNext("h2").matchNext("a").getAttribute("href")
				.followLink(); // this creates a link follower. It will open each link automatically for us.

		// configure the link follower to join the data collected from each visited page with each record of the "houses" entity.
		houseDetails.setNesting(Nesting.JOIN);

		/* CONFIGURE PARSER TO STORE HTML OF EACH LINK WE VISITED **/
		// stores the HTML of each link visited. The file name will be the third element of the URL. If url is "/Property/307634/EST6886/Springfield", file will be "EST6886.html"
		houseDetails.getParserSettings().setFileNamePattern("{parent}/{url, 2}.html");

		// captures the property details we are interested in.
		houseDetails.addField("id").match("strong").withExactText("Listing Number:").getFollowingText();
		houseDetails.addField("address").match("h2").classes("detailAddress").getText();
		houseDetails.addField("price").match("h3").id("listingViewDisplayPrice").getText();

		// creates a path to the "features" section of the page
		PartialPath info = houseDetails.newPath().match("ul").id("detailFeatures");
		info.addField("bedrooms").match("li").classes("bdrm").matchNext("span").getText();
		info.addField("bathrooms").match("li").classes("bthrm").matchNext("span").getText();

		// creates a path to the "property information" section of the page
		info = houseDetails.newPath().match("div").classes("property-information").match("li").matchNext("span").classes("heading");
		info.addField("landSize").matchCurrent().withText("Land size").getFollowingText();
		info.addField("propertyType").matchCurrent().withText("Property type").getFollowingText();

		return entityList;
	}
}
