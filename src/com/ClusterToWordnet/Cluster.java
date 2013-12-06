package com.ClusterToWordnet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a cluster compring a word, an id, and a list of specific words
 * 
 * @author carsten
 * 
 */
public class Cluster {
	private Token word;
	private List<Token> specificWords;

	private int id;
	private final static String properNounTag = "NP";
	private final static String fieldSeparator = "\t";
	private final static String wordSeparator = ", ";
	private final static Logger logger = Logger.getLogger(Cluster.class
			.getName());
	private final static String numericTag = "CD";
	private final static String numericPattern = "^[0-9\\.,:]+$";

	/**
	 * Constructs a Cluster object from a given input line in the shape
	 * 
	 * @param line
	 *            a String containing a cluster
	 * @throws IllegalArgumentException
	 *             if the input line cannot be parsed
	 */
	public Cluster(String line) throws IllegalArgumentException {
		parseLine(line);
	}

	/**
	 * Parses a line representing a cluster in the shape
	 * <word>#<pos><TAB><id><specificword1>#<pos1>, <specificword2>#<pos2>, ...
	 * 
	 * @param line
	 *            input line
	 * @throws IllegalArgumentException
	 *             if the line cannot be parsed
	 */
	private void parseLine(String line) throws IllegalArgumentException {
		String[] l = line.trim().split(fieldSeparator);
		if (l.length < 3)
			throw new IllegalArgumentException("Invalid line: " + line);
		setWord(new Token(l[0]));
		setId(new Integer(l[1]));
		specificWords = new ArrayList<>();

		for (String w : l[2].split(wordSeparator)) {
			try {
				specificWords.add(new Token(w));
			} catch (IllegalArgumentException e) {
				logger.fine(String.format("Line: %s\n%s", l[2],
						e.getLocalizedMessage()));
			}
		}
	}

	/**
	 * Reads a file containing one cluster per line and generates a list of
	 * Cluster objects
	 * 
	 * @param filename
	 *            file to read
	 * @param maxlines
	 *            skip if this number of lines has been read before end of file
	 * @return a List of Cluster objects
	 * @throws IOException
	 *             if the file cannot be read
	 */
	public static LinkedList<Cluster> readClusterFile(String filename,
			int maxlines) throws IOException {
		LinkedList<Cluster> clusters = new LinkedList<>();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		int linecounter = 0;

		while ((line = br.readLine()) != null
				&& (maxlines <= 0 || linecounter < maxlines)) {
			try {
				clusters.add(new Cluster(line));
				linecounter++;
			} catch (IllegalArgumentException e) {
				logger.warning(e.getLocalizedMessage());
			}
		}
		br.close();
		return clusters;
	}

	public boolean isNumber() {
		return getWord().getPos().equals(numericTag)
				&& getWord().getWord().matches(numericPattern);
	}

	public Token getWord() {
		return word;
	}

	public void setWord(Token word) {
		this.word = word;
	}

	public int getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Token> getSpecificWords() {
		return specificWords;
	}

	@Override
	public String toString() {
		return String.format("%s:%d", getWord(), getId());
	}

	public boolean isProperNoun() {
		return (getWord().getPos().equals(properNounTag ));
	}

}
