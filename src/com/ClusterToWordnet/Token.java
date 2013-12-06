package com.ClusterToWordnet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.smu.tspell.wordnet.SynsetType;

/**
 * Represents a token comprising a word and a part-of-speech tag
 * 
 * @author carsten
 * 
 */
public class Token {
	private String word;
	private String pos;
	// private final static String tagSeparator = "#";
	private final static Pattern tagPattern = Pattern
			.compile("(.+)#([A-Z\\$\\,]+)");

	public Token(String tokenString) throws IllegalArgumentException {
		parseToken(tokenString);
	}

	/**
	 * Parse a token and store the word/pos pair in respective fields
	 * 
	 * @param tokenString
	 *            a word/pos pair in the shape <word>#<pos>
	 * @throws IllegalArgumentException
	 *             if the given input string is not in the expected form
	 */
	private void parseToken(String tokenString) throws IllegalArgumentException {
		Matcher m = tagPattern.matcher(tokenString);
		if (m.matches()) {
			setWord(m.group(1));
			setPos(m.group(2));
		} else {
			throw new IllegalArgumentException(String.format(
					"Invalid token string: %s", tokenString));
		}
		// String[] w = tokenString.trim().split(tagSeparator);
		// if (w.length != 2) {
		// throw new IllegalArgumentException("Invalid Token string: "
		// + tokenString);
		// }
		// setWord(w[0]);
		// setPos(w[1]);
	}

	public SynsetType getSynsetType() {
		return ClusterMapper.tag2Type(getPos());
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos2) {
		this.pos = pos2;
	}

	@Override
	public String toString() {
		return String.format("%s#%s", getWord(), getPos());
	}
}
