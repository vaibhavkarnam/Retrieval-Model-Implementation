
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class Task2 {
	Map<String, Integer> allTermList = new HashMap<String, Integer>();
	List<String> termSublist = new ArrayList<String>();

	public static void main(String args[]) {
		try {
			Task2 task2 = new Task2();
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter path to the file list: ");
			String filePath = br.readLine();
			List<String> docList = task2.getFileList(filePath);
			System.out.println("Enter query file: ");
			String queryFile = br.readLine();
			System.out.println("Enter doc freq file: ");
			String docFreqFile = br.readLine();
			Map<Integer, String> queryList = task2.getQueryList(queryFile);
			for (int i : queryList.keySet()) {
				Map<String, Integer> queryTerms = task2.getTerms(queryList.get(i));
				Map<String, Double> pageRanks = task2.applyPageRankAlgorithm(docList, queryTerms, docFreqFile);
				task2.sortByPageRank(pageRanks, i);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Map<Integer, String> getQueryList(String queryFile) {
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

	public List<String> getFileList(String filename) {
		List<String> fileList = new ArrayList<String>();
		// Get list from file
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
			String line;
			while ((line = br.readLine()) != null) {
				fileList.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileList;
	}

	public Map<String, Integer> getTerms(String query) {
		Map<String, Integer> terms = new HashMap<String, Integer>();
		// Split terms and store them in a HashMap
		String[] tmp = query.split(" ");
		for (String str : tmp) {
			if (terms.containsKey(str)) {
				terms.put(str, terms.get(str) + 1);
			} else {
				terms.put(str, 1);
			}
		}
		return terms;
	}

	/**
	 * Returns a string : list of documents|doc. frequency
	 * 
	 * @param term
	 * @param docFrequencyFile
	 * @return
	 */
	public String getDocumentFrequency(String term, String docFrequencyFile) {
		// get document frequency for the term from file
		String result = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(docFrequencyFile)));
			String line;
			while ((line = br.readLine()) != null) {
				String[] tmp = line.split("\\|");
				if (term.trim().equals(tmp[0].trim())) {
					result = tmp[1] + '|' + tmp[2];
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public int getTermFrequency(String term, String document) {
		// Get term frequency from a document
		int count = 0;
		File file = new File(System.getProperty("user.dir") + File.separator + "DocList" + File.separator + document);
		if (file.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;
				while ((line = br.readLine()) != null) {
					if (line.trim().equals(term.toLowerCase().trim())) {
						count++;
					}
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return count;
	}

	public int getDocumentLength(String document) {
		int docLength = 0;
		// calculate the total number of terms in the document
		try {
			File file = new File(
					System.getProperty("user.dir") + File.separator + "DocList" + File.separator + document);
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;
				while ((line = br.readLine()) != null) {
					docLength++;
				}
				br.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return docLength;
	}

	public double getDocAvg() {
		double avg = 0.0;
		// Calculate the average frequency of a term in the document
		int count = 0;
		for (String term : allTermList.keySet()) {
			count += allTermList.get(term);
		}
		avg = count / allTermList.size();
		return avg;
	}

	public void getTermList(List<String> fileList) {
		String newDir = System.getProperty("user.dir") + File.separator + "DocList";
		new File(newDir).mkdirs();
		String title;
		try {
			for (String line : fileList) {
				File htmlFile = new File(line);
				if (!htmlFile.exists()) {
					continue;
				}
				org.jsoup.nodes.Document doc = Jsoup.parse(htmlFile, "UTF-8", "");
				org.jsoup.nodes.Element nList = doc.select("title").first();
				title = nList.ownText();
				title = title.replaceAll(" ", "");
				title = title.replaceAll("[^\\x00-\\x7F]", "");
				title = title.split("-Wikipedia")[0];
				File docFile = new File(newDir + File.separator + title);

				BufferedWriter bw = new BufferedWriter(new FileWriter(docFile));
				org.jsoup.nodes.Element content = doc.getElementById("content");
				content.select("table").remove();
				content.select("ïmg").remove();
				content.select("script").remove();
				content.select("stlye").remove();
				if (content.getElementById("References") != null) {
					content.getElementById("References").remove();
				}
				if (content.getElementById("External_links") != null) {
					content.getElementById("External_links").remove();
				}

				if (content.getElementsByAttributeValue("class", "mw-editsection") != null) {
					content.getElementsByAttributeValue("class", "mw-editsection").remove();
				}
				if (content.getElementsByAttributeValue("class", "reflist") != null) {
					content.getElementsByAttributeValue("class", "reflist").remove();
				}
				if (content.getElementsByAttributeValue("class", "thumb tright") != null) {
					content.getElementsByAttributeValue("class", "thumb tright").remove();
				}
				if (content.getElementsByAttributeValue("class", "external free") != null) {
					content.getElementsByAttributeValue("class", "external free").remove();
				}

				Elements children = content.select("*");
				for (org.jsoup.nodes.Element child : children) {
					String textContent = child.ownText();
					if (textContent != null && !textContent.trim().equals("")) {
						if (!textContent.startsWith("http")) {
							textContent = textContent.toLowerCase();
							textContent = removePunct(textContent);
							String[] tmp = textContent.split(" ");
							for (String t : tmp) {
								t = t.trim();
								if (t != null && !t.equals("") && !t.contains("|")) {
									bw.write(t);
									bw.newLine();
									if (allTermList.containsKey(t)) {
										allTermList.put(t, allTermList.get(t) + 1);
									} else {
										allTermList.put(t, 1);
									}
								}
							}
						}
					}
				}
				bw.close();
				/*
				 * Map<String, Map<String, Integer>> unigramDocMap = new
				 * HashMap<String, Map<String, Integer>>(); unigramDocMap =
				 * getMap(title, unigramDocMap); printMap(unigramDocMap,
				 * "UnigramDocFreq.txt");
				 */
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printMap(Map<String, Map<String, Integer>> docMap, String fileName) {

		Map<String, Map<String, Integer>> docFrequencyMap = new TreeMap<String, Map<String, Integer>>();
		try {
			for (String str : docMap.keySet()) {
				String tmp = str.split("_")[0];
				Map<String, Integer> wordmap = docMap.get(str);
				for (String word : wordmap.keySet()) {
					if (docFrequencyMap.containsKey(word)) {
						Map<String, Integer> df = docFrequencyMap.get(word);
						df.put(tmp, wordmap.get(word));
						docFrequencyMap.put(word, df);
					} else {
						Map<String, Integer> df = new HashMap<String, Integer>();
						df.put(tmp, wordmap.get(word));
						docFrequencyMap.put(word, df);
					}
				}
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true));
			for (String word : docFrequencyMap.keySet()) {
				if (!word.equals("") || !word.startsWith("|")) {
					bw.write(word + "|");
					for (String str : docFrequencyMap.get(word).keySet()) {
						bw.write(str.trim() + " ");
					}
					bw.write("|" + docFrequencyMap.get(word).size());
					bw.newLine();
				}
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, Map<String, Integer>> getMap(String fileName, Map<String, Map<String, Integer>> docMap) {
		Map<String, Integer> wordCount = new HashMap<String, Integer>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					System.getProperty("user.dir") + File.separator + "DocList" + File.separator + fileName));
			String line;
			while ((line = br.readLine()) != null) {
				if (!wordCount.containsKey(line)) {
					int count = 1;
					wordCount.put(line, count);
				} else {
					int count = wordCount.get(line);
					count++;
					wordCount.put(line, count);
				}
			}
			br.close();
			docMap.put(fileName, wordCount);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return docMap;
	}

	public static String removePunct(String text) {
		text = text.trim();
		text = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		text = text.replaceAll("[^\\x00-\\x7F]", "");
		text = text.replaceAll(" - ", "");
		text = text.replaceAll("\\?", "");
		if (text.trim().equals("") || text.trim().equals("-")) {
			return "";
		}
		List<String> punctuations = Arrays.asList("/", "\\(", "\\)", "^", ";", "\"", ">", "<", "\'", "\\[", "\\]",
				"\\{", "\\}", "\\*", "&", "@", "!", "`", "~", "|", "\\+", "_", "\\?", "#", "=", "\\\\", "\\|");
		List<Character> punct2 = Arrays.asList('.', ',', '$', ':', '%', '-');
		char[] new_arr = null;
		for (String punct : punctuations) {
			text = text.replaceAll(punct, "");
		}
		char[] arr = text.toCharArray();
		for (char str : punct2) {
			new_arr = new char[arr.length];
			if (text.length() == 1) {
				return "";
			}
			for (int i = 0, j = 0; i < arr.length; i++) {
				if (arr[i] == str) {
					if (i == 0) {
						if (arr[i] == ',' || arr[i] == ':' || arr[i] == '%') {
							continue;
						}
						if (Character.isDigit(arr[i + 1])) {
							new_arr[j++] = arr[i];
						} else {
							// Do nothing
						}
					} else if (i == arr.length - 1) {
						// Do nothing
					} else {
						if (Character.isDigit(arr[i - 1]) || Character.isDigit(arr[i + 1])) {
							new_arr[j++] = arr[i];
						} else {
							if (str == '-') {
								if (Character.isDigit(arr[i + 1])) {
									new_arr[j++] = arr[i];
								}
							}
						}
					}
				} else {
					new_arr[j] = arr[i];
					j++;
				}
			}
			arr = new_arr;
		}
		return new String(new_arr).trim();
	}

	public Map<String, Double> applyPageRankAlgorithm(List<String> docList, Map<String, Integer> queryTerms,
			String docFreqFile) {
		Map<String, Double> rank = new HashMap<String, Double>();
		getTermList(docList);
		double avgDocLength = getDocAvg();
		for (String term : queryTerms.keySet()) {
			String docFreqStr = getDocumentFrequency(term, docFreqFile);
			String tmp[] = docFreqStr.split("\\|");
			if (tmp.length > 1) {
				int docFreq = Integer.parseInt(tmp[1]);
				int queryFreq = queryTerms.get(term);
				String docs[] = tmp[0].split(" ");

				for (String doc : docs) {

					int termFreq = getTermFrequency(term, doc);
					int docLength = getDocumentLength(doc);
					int noOfDocs = docList.size();
					BM25_Algorithm bm25 = new BM25_Algorithm();
					double score = bm25.score(termFreq, noOfDocs, docLength, avgDocLength, queryFreq, docFreq);
					if (rank.containsKey(doc)) {
						rank.put(doc, rank.get(doc) + score);
					} else {
						rank.put(doc, score);
					}
				}
			}
		}
		return rank;
	}

	private void quickSort(int low, int high, List<DocumentRank> sortedList) {
		int i = low, j = high;
		double pivot = sortedList.get(low + (high - low) / 2).getRank();
		while (i <= j) {
			while (sortedList.get(i).getRank() > pivot) {
				i++;
			}
			while (sortedList.get(j).getRank() < pivot) {
				j--;
			}
			if (i <= j) {
				DocumentRank tmp = new DocumentRank(sortedList.get(i).getDocumentId(), sortedList.get(i).getRank());
				sortedList.set(i, sortedList.get(j));
				sortedList.set(j, tmp);
				i++;
				j--;
			}
		}
		if (low < j)
			quickSort(low, j, sortedList);
		if (i < high)
			quickSort(i, high, sortedList);
	}

	public void sortByPageRank(Map<String, Double> pageRanks, int i) {
		List<DocumentRank> pageRankList = new LinkedList<DocumentRank>();
		for (String doc : pageRanks.keySet()) {
			DocumentRank docRank = new DocumentRank(doc, pageRanks.get(doc));
			pageRankList.add(docRank);
		}
		quickSort(0, pageRanks.size() - 1, pageRankList);
		int count = 1;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(i + ".txt")));
			for (DocumentRank doc : pageRankList) {
				if (count <= 100) {
					bw.write(i + "|" + "Q0" + "|" + doc.getDocumentId() + "|" + count++ + "|" + doc.getRank() + "|"
							+ "Vaibhav");
					bw.newLine();
				}
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}