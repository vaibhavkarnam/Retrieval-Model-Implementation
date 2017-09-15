package hw3;

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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class Parser {

	public static void main(String args[]) {
		Parser parser = new Parser();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.println("Enter input filename: ");
			String filename = br.readLine();
			parser.parse(filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void parse(String filename) {
		Map<String, Map<String, Integer>> unigramDocMap = new HashMap<String, Map<String, Integer>>();
		Map<String, Map<String, Integer>> bigramDocMap = new HashMap<String, Map<String, Integer>>();
		Map<String, Map<String, Integer>> trigramDocMap = new HashMap<String, Map<String, Integer>>();

		String title;
		File file = new File(filename);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				File htmlFile = new File(line);
				if (!htmlFile.exists()) {
					continue;
				}
				org.jsoup.nodes.Document doc = Jsoup.parse(htmlFile, "UTF-8", "");
				org.jsoup.nodes.Element nList = doc.select("title").first();
				title = nList.ownText();
				title = title.replaceAll(" ", "");
				title = title.split("-Wikipedia")[0];
				File docFile = new File(title);
				if (docFile.exists()) {
					docFile = new File(title + "_" + System.currentTimeMillis());
				}
				file.createNewFile();
				
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
								}
							}
						}
					}
				}
				bw.close();
				unigramDocMap = getMap(title, unigramDocMap);
				getBigrams(title);
				bigramDocMap = getMap(title + "_bigram", bigramDocMap);
				getTrigrams(title);
				trigramDocMap = getMap(title + "_trigram", trigramDocMap);
			}
			br.close();

			printMap(unigramDocMap, "UnigramDocFreq.txt");
			printMap(bigramDocMap, "BigramDocFreq.txt");
			printMap(trigramDocMap, "TrigramDocFreq.txt");
			printTermFreqList(getTermFrequency(unigramDocMap), "UnigramTF.txt", "UnigramGraph.csv");
			printTermFreqList(getTermFrequency(bigramDocMap), "BigramTF.txt", "Bigramgraph.csv");
			printTermFreqList(getTermFrequency(trigramDocMap), "TrigramTF.txt", "TrigramGraph.csv");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, Map<String, Integer>> getMap(String fileName, Map<String, Map<String, Integer>> docMap) {
		Map<String, Integer> wordCount = new HashMap<String, Integer>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
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

	public void getBigrams(String fileName) {
		File file = new File(fileName);
		File bigramFile = new File(fileName + "_bigram");
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			BufferedWriter bw = new BufferedWriter(new FileWriter(bigramFile, true));
			String prev = br.readLine();
			String line;
			while ((line = br.readLine()) != null) {
				String newLine = line;
				bw.write(prev + " " + newLine);
				bw.newLine();
				prev = newLine;
			}
			bw.close();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getTrigrams(String fileName) {
		File file = new File(fileName);
		File bigramFile = new File(fileName + "_trigram");
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			BufferedWriter bw = new BufferedWriter(new FileWriter(bigramFile, true));
			String one = br.readLine();
			String two = br.readLine();
			String line;
			while ((line = br.readLine()) != null) {
				String three = line;
				bw.write(one + " " + two + " " + three);
				bw.newLine();
				one = two;
				two = three;
			}
			bw.close();
			br.close();
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

	public void printTermFreqList(List<WordMap> termFrequency, String fileName, String graphFile) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName), true));
			long total = 0;
			for (WordMap word : termFrequency) {
				bw.write(word.getWord() + "|" + word.getCount());
				bw.newLine();
				total += word.getCount();
			}
			bw.close();

			bw = new BufferedWriter(new FileWriter(new File(graphFile), true));
			int count = 1;
			for (WordMap word : termFrequency) {
				bw.write(Math.log10(count) + "," + Math.log10(word.getCount()));
				bw.newLine();
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<WordMap> getTermFrequency(Map<String, Map<String, Integer>> docMap) {
		List<WordMap> termFrequencyList = new ArrayList<WordMap>();
		Map<String, Integer> termFrequency = new TreeMap<String, Integer>();
		for (String docId : docMap.keySet()) {
			Map<String, Integer> wordCount = docMap.get(docId);
			for (String word : wordCount.keySet()) {
				if (termFrequency.containsKey(word)) {
					int count = termFrequency.get(word);
					termFrequency.put(word, count + wordCount.get(word));
				} else {
					termFrequency.put(word, wordCount.get(word));
				}
			}
		}

		for (String term : termFrequency.keySet()) {
			WordMap wordmap = new WordMap(term, termFrequency.get(term));
			termFrequencyList.add(wordmap);
		}
		quickSort(0, termFrequencyList.size() - 1, termFrequencyList);
		return termFrequencyList;
	}

	private void quickSort(int low, int high, List<WordMap> sortedList) {
		int i = low, j = high;
		if (low <= high) {
			double pivot = sortedList.get(low + (high - low) / 2).getCount();
			while (i <= j) {
				while (sortedList.get(i).getCount() > pivot) {
					i++;
				}
				while (sortedList.get(j).getCount() < pivot) {
					j--;
				}
				if (i <= j) {
					WordMap tmp = new WordMap(sortedList.get(i).getWord(), sortedList.get(i).getCount());
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
	}

	public static String removePunct(String text) {
		text = text.trim();
		text = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		;
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
						/*
						 * if (Character.isDigit(arr[i - 1])) { new_arr[j++] =
						 * arr[i]; } else { // Do nothing }
						 */
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
}