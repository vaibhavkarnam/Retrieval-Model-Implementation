package hw3;

public class WordMap {
	private String word;
	private int wordCount;
	
	public WordMap(String word, int wordCount) {
		this.word = word;
		this.wordCount = wordCount;
	}
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getCount() {
		return wordCount;
	}
	public void setCount(int count) {
		this.wordCount = count;
	}
}
