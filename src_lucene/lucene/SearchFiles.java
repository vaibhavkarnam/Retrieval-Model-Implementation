package lucene;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/** Simple command-line based search demo. */
public class SearchFiles {

	private SearchFiles() {
	}

	/** Simple command-line based search demo. */
	public static void main(String[] args) throws Exception {
		String usage = "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
		if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			System.out.println(usage);
			System.exit(0);
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter path to index: ");
		// String index = "E:\\MS Code\\Index";
		String index = br.readLine();
		String field = "contents";
		String queries = null;
		int repeat = 0;
		boolean raw = false;
		// regular search
		// String queryString = "computer";

		// wildcard query
		// String queryString = "te*t";

		// fuzzy query
		// String queryString = "roam~2";

		// phrase query
		// String queryString = "\"apache lucene\"~5";

		// boolean search
		// String queryString = "\"networks\" AND \"protocol\"";

		// boosted search
		System.out.println("Enter query file path: ");
		String queryFile = br.readLine();

		// String queryString = "global warming potential";
		Map<Integer, String> queryList = getQueryList(queryFile);
		for (int i : queryList.keySet()) {
			String queryString = queryList.get(i);
			int hitsPerPage = 100;
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_47);

			BufferedReader in = null;
			QueryParser parser = new QueryParser(Version.LUCENE_47, field, analyzer);

			Query query = parser.parse(queryString);

			System.out.println("Searching for: " + query.toString(field));
			searcher.search(query, null, 100);
			doSearch(i, in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);
			reader.close();
		}
	}

	public static Map<Integer, String> getQueryList(String queryFile) {
		Map<Integer, String> queryList = new TreeMap<Integer, String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(queryFile)));
			String line;
			while ((line = br.readLine()) != null) {
				String tmp[] = line.split("\\|");
				queryList.put(Integer.parseInt(tmp[0]), tmp[1]);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return queryList;
	}

	/**
	 * This demonstrates a typical paging search scenario, where the search
	 * engine presents pages of size n to the user. The user can then go to the
	 * next page if interested in the next hits.
	 * 
	 * When the query is executed for the first time, then only enough results
	 * are collected to fill 5 result pages. If the user wants to page beyond
	 * this limit, then the query is executed another time and all hits are
	 * collected.
	 * 
	 */
	public static void doSearch(int queryId, BufferedReader in, IndexSearcher searcher, Query query, int hitsPerPage, boolean raw,
			boolean interactive) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(queryId+ ".txt")));
		// Collect enough docs to show 5 pages
		TopDocs results = searcher.search(query, 5 * hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;

		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits + " total matching documents");

		int start = 0;
		int end = Math.min(numTotalHits, hitsPerPage);

		for (int i = start; i < end; i++) {
			Document doc = searcher.doc(hits[i].doc);
			String path = doc.get("path");
			if (path != null) {
				System.out.println((i + 1) + ". " + path);
				bw.write((i+1) + ". " + path);
				bw.newLine();
				String title = doc.get("title");
				if (title != null) {
					System.out.println("   Title: " + doc.get("title"));
				}
			} else {
				System.out.println((i + 1) + ". " + "No path for this document");
			}

		}
		bw.close();
	}
}