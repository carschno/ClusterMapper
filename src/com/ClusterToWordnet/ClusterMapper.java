package com.ClusterToWordnet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

/**
 * @author carsten
 * 
 */
public class ClusterMapper {
	private final static Logger logger = Logger.getLogger(ClusterMapper.class
			.getName());
	private final WordNetDatabase database;
	private final static Tokenizer tokenizer = SimpleTokenizer.INSTANCE;
	private final static boolean useMorphology = true;
	private final static Map<String, SynsetType> tagMap = new HashMap<String, SynsetType>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2678387766078406580L;

		{
			put("N", SynsetType.NOUN);
			put("CD", SynsetType.NOUN);
			put("JJ", SynsetType.ADJECTIVE);
			put("RB", SynsetType.ADVERB);
			put("V", SynsetType.VERB);
		}
	};
	private final static Map<SynsetType, String> indexMap = new HashMap<SynsetType, String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4307439873510059369L;

		{
			put(SynsetType.NOUN, "index.noun");
			put(SynsetType.ADJECTIVE, "index.adj");
			put(SynsetType.ADVERB, "index.adv");
			put(SynsetType.VERB, "index.verb");
		}
	};
	private final static String defaultDir = "/usr/share/wordnet";

	public ClusterMapper() {
		if (System.getenv("WNHOME") == null) {
			System.setProperty("wordnet.database.dir", defaultDir);
			logger.fine(String.format(
					"WNHOME not set, using Wordnet directory %s.", defaultDir));
		} else {
			System.setProperty("wordnet.database.dir", System.getenv("WNHOME"));
			logger.fine(String.format(
					"Using Wordnet directory %s as set in WNHOME",
					System.getenv("WNHOME")));
		}
		database = WordNetDatabase.getFileInstance();
	}

	public ClusterMapper(String wordnetdir) {
		System.setProperty("wordnet.database.dir", wordnetdir);
		database = WordNetDatabase.getFileInstance();
	}

	/**
	 * Compute the portion of the cluster's specific words that occur in the
	 * synset's synset, including the cluster word itself.
	 * 
	 * @param cluster
	 * @param synset
	 * @return
	 */
	public static double inSynset(Cluster cluster, Synset synset) {
		List<String> wordforms = Arrays.asList(synset.getWordForms());
		int count = wordforms.contains(cluster.getWord().getWord()) ? 1 : 0;

		for (Token specific : cluster.getSpecificWords()) {
			if (wordforms.contains(specific.getWord())) {
				count++;
				continue;
			}
		}
		// add one for the cluster word itself
		return (double) count
				/ (double) (cluster.getSpecificWords().size() + 1);
	}

	/**
	 * Compute the Jaccard similarity index for two collections of strings. The
	 * collections are converted into setsfor that purpose, i.e. duplicate
	 * tokens are silently removed.
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
	private static double jaccard(Collection<String> list1,
			Collection<String> list2) {
		double result;
		Set<String> intersection = new HashSet<>(list1);
		Set<String> union = new HashSet<>(list1);
		Set<String> set2 = new HashSet<>(list2);

		intersection.retainAll(set2);
		union.addAll(set2);
		if (union.isEmpty())
			result = 0.0;
		else
			result = (double) intersection.size() / (double) union.size();
		return result;
	}

	/**
	 * Compute the Sorensen similarity index for two collections of strings. The
	 * collections are converted into setsfor that purpose, i.e. duplicate
	 * tokens are silently removed.
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
	@SuppressWarnings(value = { "unused" })
	private static double sorensen(Collection<String> list1,
			Collection<String> list2) {
		double result;
		Set<String> intersection = new HashSet<>(list1);
		Set<String> set2 = new HashSet<>(list2);

		intersection.retainAll(set2);
		result = 2.0 * (double) intersection.size()
				/ (double) (list1.size() + list2.size());
		return result;
	}

	/**
	 * For a list of Token objects, extract their words and return them as a
	 * list.
	 * 
	 * @param tokens
	 * @return
	 */
	private static List<String> tokenWordsToStrings(Collection<Token> tokens) {
		List<String> words = new ArrayList<>(tokens.size());
		for (Token token : tokens) {
			words.add(token.getWord());
		}
		return words;
	}

	/**
	 * Tokenize the input string.
	 * 
	 * @param text
	 * @return a String array containing one token per entry.
	 */
	private static String[] tokenize(String text) {
		String[] tokens = tokenizer.tokenize(text);
		return tokens;
	}

	/**
	 * Expects a String array with each String representing a text passage. Each
	 * text passage is tokenized and all the tokens are concatenated to a list.
	 * 
	 * @param terms
	 *            a String array representing text passages
	 * @return a single list of tokens from the text passages
	 */
	public static List<String> tokenizeAll(String[] terms) {
		List<String> tokens = new ArrayList<>();
		for (String term : terms) {
			tokens.addAll(Arrays.asList(tokenize(term)));
		}
		return tokens;
	}

	/**
	 * Join two words of a multiword expressions, e.g. "air base" -> airbase.
	 * 
	 * @param terms
	 *            a String array, each String comprising possible multiple words
	 *            separated by a space
	 * @return the joined version of two words
	 */
	private static List<String> connectCompounds(String[] terms) {
		List<String> tokens = new ArrayList<>();
		for (String term : terms) {
			tokens.addAll(Arrays.asList(term.replace(" ", "")));
		}
		return tokens;

	}

	/**
	 * Find all the synsets containing any of the the cluster's specific words.
	 * If the cluster's word is numeric, it is replaced by the word 'number'.
	 * 
	 * @param cluster
	 * @return a list of synsets
	 */
	private List<Synset> candidateSynsets(Cluster cluster) {
		Token word = cluster.getWord();
		List<Synset> candidates = new ArrayList<>();

		// Start with exact matches
		Synset[] exact_matches = database.getSynsets(word.getWord(),
				word.getSynsetType(), useMorphology);
		if (cluster.isProperNoun()) {
			// do not try to find similar synsets for proper nouns.
			return Arrays.asList(exact_matches);
		}
		List<Synset> compounds = compoundsEndWith(word);
		candidates.addAll(Arrays.asList(exact_matches));
		candidates.addAll(compounds);

		if (candidates.isEmpty()) {
			Synset[] otherTypes = database.getSynsets(word.getWord(), null,
					useMorphology);
			candidates.addAll(Arrays.asList(otherTypes));
		}
		if (candidates.isEmpty()) {
			// find synsets containing any of the cluster's specific words
			for (Token specific : cluster.getSpecificWords()) {
				Synset[] specific_synsets = database.getSynsets(
						specific.getWord(), specific.getSynsetType(),
						useMorphology);
				candidates.addAll(Arrays.asList(specific_synsets));
			}
		}
		return candidates;
	}

	/**
	 * Computes the score for a mapping between the given cluster and the given
	 * synset.
	 * 
	 * @param cluster
	 * @param synset
	 * @return a score for the mapping between the two arguments
	 */
	private static double mappingScore(Cluster cluster, Synset synset) {
		// add wordforms, compounds both in a tokenized as well as in a single
		// string shape:
		List<String> wordforms = tokenizeAll(synset.getWordForms());
		wordforms.addAll(connectCompounds(synset.getWordForms()));

		List<String> specificWords = tokenWordsToStrings(cluster
				.getSpecificWords());
		List<String> examplesTokens = tokenizeAll(synset.getUsageExamples());

		double word_in_synset = jaccard(
				Arrays.asList(new String[] { cluster.getWord().getWord() }),
				wordforms);
		double specificWords_in_synset = jaccard(specificWords, wordforms);
		double specificWords_in_definition = jaccard(specificWords,
				Arrays.asList(tokenize(synset.getDefinition())));
		double specificWords_in_examples = jaccard(specificWords,
				examplesTokens);

		// TODO: find optimal weights
		double score = (word_in_synset + specificWords_in_synset
				* specificWords.size() + specificWords_in_definition
				* specificWords.size() + specificWords_in_examples
				* specificWords.size())
				/ (double) (1 + specificWords.size() * 3);
		logger.fine(String.format(
				"Scores for %s:%d and %s:\t%f, %f, %f, %f (acc: %f)", cluster
						.getWord().toString(), cluster.getId(), Arrays
						.asList(synset.getWordForms()), word_in_synset,
				specificWords_in_synset, specificWords_in_definition,
				specificWords_in_examples, score));

		// Print scores for debugging
		if (score == 0.0) {
			logger.finest(String.format(
					"Score for %s:%d mapping to synset %s: %f",
					cluster.getWord(), cluster.getId(),
					Arrays.asList(synset.getWordForms()), score));
		} else {
			logger.finer(String.format(
					"Score for %s:%d mapping to synset %s: %f",
					cluster.getWord(), cluster.getId(), synset.toString(),
					score));
		}
		return score;
	}

	/**
	 * Find the most probably mapping for the cluster for a given list of
	 * candidate synsets.
	 * 
	 * @param cluster
	 * @param candidates
	 * @return the most likely synset
	 */
	private static Map<Synset, Double> scores(Cluster cluster,
			List<Synset> candidates) {
		Map<Synset, Double> results = new HashMap<>(candidates.size());

		if (candidates.isEmpty()) {
			logger.fine(String.format("Empty candidate list for %s:%d",
					cluster.getWord(), cluster.getId()));
		} else {
			for (Synset synset : candidates) {
				results.put(synset, mappingScore(cluster, synset));
			}
		}
		return results;
	}

	/**
	 * Find the synset-score-mapping with highest score up to the top n
	 * positions. If synsets have equal score, they are included so that the
	 * output may contain more than n entries.
	 * 
	 * @param <T>
	 * 
	 * @param scores
	 *            a of synset-score-mappings
	 * @param the
	 *            top n synsets to find
	 * @return
	 */
	private static <T> Map<T, Double> maxScores(Map<T, Double> scores, int n) {
		final Map<T, Double> top;
		if (scores.size() <= n) {
			top = scores;
		} else {
			top = new HashMap<>(n);
			List<Double> sortedValues = new ArrayList<>(scores.values());
			Collections.sort(sortedValues);
			Double minimum = sortedValues.get(sortedValues.size() - n);
			for (T synset : scores.keySet()) {
				Double value = scores.get(synset);
				if (value >= minimum) {
					top.put(synset, value);
				}
			}
		}
		return top;
	}

	/**
	 * Find the top entry in the map according to value. If there are multiple
	 * entries with same score, the first one is chosen (arbitrarily).
	 * 
	 * @param scores
	 * @return
	 */
	private static <T> T max(Map<T, Double> scores) {
		T result = null;
		double max = Double.NEGATIVE_INFINITY;
		for (T score : scores.keySet()) {
			if (scores.get(score) > max) {
				result = score;
				max = scores.get(score);
			}
		}
		return result;
	}

	/**
	 * Find a synset the cluster should be mapped to to. At first, consider only
	 * synsets that contain the cluster's word. If there is none, consider
	 * synsets that contain any of the cluster's specific words.
	 * 
	 * @param cluster
	 * @return a Synset
	 */
	@SuppressWarnings(value = { "unused" })
	private Map<Synset, Double> mapMultiple(Cluster cluster, int top) {
		return maxScores(scores(cluster, candidateSynsets(cluster)), top);
	}

	/**
	 * Find synset to map the given cluster to.
	 * 
	 * @param cluster
	 * @return null if no matching synset can be found
	 */
	public Synset mapSingle(Cluster cluster) {
		return max(scores(cluster, candidateSynsets(cluster)));

	}

	/**
	 * Return a synset type for a part-of-speech tag.
	 * 
	 * @param tag
	 * @return a SynsetType or null if there is no mapping available
	 */
	public static SynsetType tag2Type(String tag) {
		for (String key : tagMap.keySet()) {
			if (tag.startsWith(key))
				return tagMap.get(key);
		}
		return null;
	}

	/**
	 * Public interface to access the Wordnet database.
	 * 
	 * @return a WordNetDatabase object
	 */
	public WordNetDatabase getDatabase() {
		return database;
	}

	/**
	 * Read the given Reader object line by line, expecting each line to
	 * represent a cluster.
	 * 
	 * @param reader
	 *            A reader object, typically generated from a cluster file
	 * @param startLine
	 *            the first cluster to consider
	 * @param endLine
	 *            abort when this cluster has been read
	 * @return a list of Cluster objects generated from the lines
	 * @throws IOException
	 *             if a low-level error occurs
	 */
	public static List<Cluster> readClusterReader(Reader reader, int startLine,
			int endLine) throws IOException {
		BufferedReader br = new BufferedReader(reader);

		List<Cluster> clusters = new LinkedList<>();
		String line;
		int lineCount = 0;

		while ((line = br.readLine()) != null && lineCount <= endLine) {
			lineCount++;
			if (lineCount > startLine) {
				try {
					Cluster cluster = new Cluster(line);
					clusters.add(cluster);
				} catch (IllegalArgumentException e) {
					logger.warning(String.format("Invalid line: %s:\n%s", line,
							e.getLocalizedMessage()));
				}
			}
		}
		br.close();
		logger.info(String.format("%d clusters read.", lineCount));
		logger.exiting(ClusterMapper.class.getName(), "readClusterFile");
		return clusters;

	}

	/**
	 * Generate an InputStreamReader for the given file and pass on to
	 * {@link#readClusterReader(Reader reader, int startLine, int endLine)}.
	 * 
	 * @param fileName
	 * @param encoding
	 * @param startLine
	 * @param endLine
	 * @return
	 * @throws IOException
	 */
	private static List<Cluster> readClusterFile(String fileName,
			String encoding, int startLine, int endLine) throws IOException {
		logger.info(String.format(
				"Reading cluster file '%s' from line %d to line %d...",
				fileName, startLine, endLine));
		return readClusterReader(new InputStreamReader(new FileInputStream(
				fileName), encoding), startLine, endLine);
	}

	/**
	 * Get a random sample of size n from the given list.
	 * 
	 * @param list
	 *            a list of generic type
	 * @param size
	 *            the number of list elemens to return
	 * @return a random list of the given size or smaller if the input list was
	 *         smaller.
	 */
	@SuppressWarnings(value = { "unused" })
	private static <T> List<T> randomSample(List<T> list, int size) {
		if (list.size() > size)
			Collections.shuffle(list);
		return list.subList(0, size);
	}

	/**
	 * Finds a list of synsets that contain a word that ends with the word
	 * specified in the given Token object. The method directly accesses the
	 * Wordnet dictionary index file matching the token's type and checks every
	 * entry for a match.
	 * 
	 * @param token
	 * @return
	 */
	private List<Synset> compoundsEndWith(Token token) {
		List<Synset> results = new ArrayList<>();
		List<String> compounds;

		String indexfile = System.getProperty("wordnet.database.dir")
				+ File.separator + indexMap.get(token.getSynsetType());
		try {
			compounds = compoundsEndWith(token.getWord(), indexfile);
		} catch (IOException e) {
			logger.warning(e.getLocalizedMessage());
			return results;
		}

		for (String compound : compounds) {
			results.addAll(Arrays.asList(database.getSynsets(
					compound.replace("_", " "), token.getSynsetType(),
					useMorphology)));
		}
		return results;
	}

	public static void main(String args[]) {
		String filename = null;
		String encoding = "UTF-8";
		int endLine = Integer.MAX_VALUE;
		int startLine = 0;
		List<Cluster> clusters = null;
		ClusterMapper mapper = new ClusterMapper();

		switch (args.length) {
		case 0:
			System.err.println("Usage:");
			System.err
					.println("ClusterMapper <clusterfile> [[<start_line>] <end_line>]");
			System.exit(1);
			break;
		case 1:
			filename = args[0];
			break;
		case 2:
			filename = args[0];
			endLine = new Integer(args[1]);
			break;
		case 3:
			filename = args[0];
			startLine = new Integer(args[1]);
			endLine = new Integer(args[2]);
		}

		// Read the cluster file
		try {
			clusters = readClusterFile(filename, encoding, startLine, endLine);
		} catch (IOException e) {
			logger.severe(e.getLocalizedMessage());
			System.exit(1);
		}

		// only use a random sample of the cluster list:
		// clusters = randomSample(clusters, 10);

		for (Cluster cluster : clusters) {
			// find top n results for cluster:
			/*
			 * int top = 3; // number of results to return Map<Synset, Double>
			 * synsets = mapper.mapMultiple(cluster, top); if
			 * (synsets.isEmpty()) { System.out.println(String.format(
			 * "Unable to find synset for cluster '%s' (id : %d).",
			 * cluster.getWord(), cluster.getId())); } else {
			 * System.out.println(String.format(
			 * "Top %d mapping candidate(s) for synset %s:\n%s\n", top, cluster,
			 * synsets)); }
			 */

			// find top result for cluster:
			Synset synset = mapper.mapSingle(cluster);
			System.out.println(String.format(
					"Mapping candidate(s) for synset %s:\n%s\n", cluster,
					synset));

		}
	}

	public static List<String> compoundsEndWith(String suffix, String indexfile)
			throws IOException {
		List<String> results = new LinkedList<>();
		BufferedReader br = new BufferedReader(new FileReader(indexfile));
		String line;
		while ((line = br.readLine()) != null) {
			String[] fields = line.split(" ");
			if (fields[0].endsWith("_" + suffix))
				results.add(fields[0]);
		}
		br.close();
		return results;
	}

}
