package com.ClusterToWordnet.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ClusterToWordnet.Cluster;
import com.ClusterToWordnet.ClusterMapper;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;

public class TestClusterMapper {

	ClusterMapper mapper;
	private final static String wordnetdir = "/usr/share/wordnet";
	String nounindexfile = "/usr/share/wordnet/index.noun";

	@Before
	public void setUp() {
		mapper = new ClusterMapper(wordnetdir);
	}

	@Test
	public void testGetSynsets() {
		String word1 = "player";
		String pos1 = "NN";
		SynsetType type1 = SynsetType.NOUN;
		SynsetType typeFalse = SynsetType.ADJECTIVE;
		int synsets1 = 5;
		String[] wordForms1 = { "player", "participant" };

		// Test wordform only
		Synset[] synsets = mapper.getDatabase().getSynsets(word1);
		assertEquals(synsets1, synsets.length);
		assertArrayEquals(wordForms1, synsets[0].getWordForms());

		// Test automatically generated SynsetType
		synsets = mapper.getDatabase().getSynsets(word1,
				ClusterMapper.tag2Type(pos1));
		assertEquals(synsets1, synsets.length);
		assertArrayEquals(wordForms1, synsets[0].getWordForms());

		// Test manually set synset type
		synsets = mapper.getDatabase().getSynsets(word1, type1);
		assertEquals(synsets1, synsets.length);
		assertArrayEquals(wordForms1, synsets[0].getWordForms());

		synsets = mapper.getDatabase().getSynsets(word1, typeFalse);
		assertEquals(0, synsets.length);
	}

	@Test
	public void testInSynonyms() {
		Cluster cluster1 = new Cluster(
				"player#NN\t0\tsuperstar#NN, actor#NN, leaguer#NN, championship#NN, cricketer#NN, umpire#NN, rider#NN, staffs#NN, improviser#NN, stealer#NN, gymnast#NN, teammate#NN, cornerbacks#NN, applicant#NN");
		Synset[] synsets = mapper.getDatabase().getSynsets(
				cluster1.getWord().getWord());
		double[] counts = { 1.0 / 15.0, 1.0 / 15.0, 2.0 / 15.0, 1.0 / 15.0,
				1.0 / 15.0 };

		for (int i = 0; i < synsets.length; i++) {
			assertEquals(counts[i],
					ClusterMapper.inSynset(cluster1, synsets[i]), 0.00001);
		}
	}

	private List<String[]> readDefinitions(String filename) throws IOException {
		BufferedReader correctBR = new BufferedReader(new InputStreamReader(
				this.getClass().getClassLoader().getResourceAsStream(filename)));
		List<String[]> definitions = new ArrayList<>();
		String definition;
		while ((definition = correctBR.readLine()) != null) {
			if (definition.isEmpty()) {
				definitions.add(null);
			} else {
				definitions.add(definition.split("\t"));
			}
		}
		return definitions;
	}

	private List<Cluster> readClusters(String filename) {
		List<Cluster> clusters = new ArrayList<>();
		Reader testfile = new InputStreamReader(this.getClass()
				.getClassLoader().getResourceAsStream(filename));

		try {
			clusters = ClusterMapper.readClusterReader(testfile, 0,
					Integer.MAX_VALUE);
		} catch (IOException e) {
			System.err.println(e.getLocalizedMessage());
			fail();
		}
		return clusters;
	}

	@Test
	public void testMapRandom() throws IOException {
		int count = 0; // correct results
		double expectedCorrect = 0.4;
		List<Cluster> clusters = readClusters("clusters_random.10.feats");
		List<String[]> definitions = readDefinitions("clusters_random.10.definitions");

		for (int i = 0; i < clusters.size(); i++) {
			Cluster cluster = clusters.get(i);
			String[] correctDefinitions = definitions.get(i);
			Synset result = mapper.mapSingle(cluster);

			if (result == null && correctDefinitions == null) {
				count++;
				System.out.println(String.format("Mapping correct for %s.",
						cluster));
			} else if (result != null && correctDefinitions != null) {
				for (String correct : correctDefinitions) {
					if (correct.equals(result.getDefinition())) {
						count++;
						System.out.println(String.format(
								"Mapping correct for %s.", cluster));
						break;
					}
				}
			}
		}
		double correct = (double) count / (double) clusters.size();
		System.out.println(String.format(
				"Correct mappings: %d out of %d (%.2f)", count,
				clusters.size(), correct));
		assertTrue(correct >= expectedCorrect);
	}

	@Test
	public void testMapRandomNoNP() throws IOException {
		int count = 0; // correct results
		double expectedCorrect = 0.4;
		List<Cluster> clusters = readClusters("clusters_random_nonp.10.feats");
		List<String[]> definitions = readDefinitions("clusters_random_nonp.10.definitions");

		for (int i = 0; i < clusters.size(); i++) {
			Cluster cluster = clusters.get(i);
			String[] correctDefinitions = definitions.get(i);
			Synset result = mapper.mapSingle(cluster);

			if (result == null && correctDefinitions == null) {
				count++;
				System.out.println(String.format("Mapping correct for %s.",
						cluster));
			} else if (result != null && correctDefinitions != null) {
				for (String correct : correctDefinitions) {
					if (correct.equals(result.getDefinition())) {
						count++;
						System.out.println(String.format(
								"Mapping correct for %s.", cluster));
						break;
					}
				}
			}
		}
		double correct = (double) count / (double) clusters.size();
		System.out.println(String.format(
				"Correct mappings: %d out of %d (%.2f)", count,
				clusters.size(), correct));
		assertTrue(correct >= expectedCorrect);
	}

	@Test
	public void testNumberMap() {
		Cluster cluster_70 = new Cluster(
				"70#CD\t0\t29#CD, twelve#CD, eighteen#CD, 650#CD, seventeen#CD, 230#CD, 58#CD, 65#CD, 63#CD, 66#CD, 126#CD, 55#CD, 39#CD, 69#CD, 128#CD, 67#CD, 22#CD, 40#CD, 6,000#CD, 30#CD, 1,000#CD, 81#CD, 28#CD, 200#CD, 13,000#CD, 97#CD, 51#CD, 100#CD, 700#CD, 41#CD, 2,500#CD, 240#CD, 18#CD, 95#CD, 79#CD, 40,000#CD, 19#CD, 117#CD, four#CD, 2,000#CD, 96#CD, 82#CD, 24#CD, 180#CD, 46#CD, 16#CD, 750#CD, 44#CD, 100,000#CD, 350#CD, hundred#CD, 160#CD, 15#CD, 50,000#CD, 78#CD, 110#CD, 220#CD, 3,500#CD, 26#CD, 800#CD, three#CD, 1,400#CD, 600#CD, 550#CD, 99#CD, 54#CD, 72#CD, 33#CD, 5,500#CD, 34#CD, 80#CD, 104#CD, 900#CD, 87#CD, seven#CD, 76#CD, 60#CD, 20#CD, 170#CD, 500#CD, ten#CD, 11,000#CD, 1,800#CD, 62#CD, 94#CD, two#CD, 12,000#CD, 73#CD, 1,100#CD, 1,600#CD, fourteen#CD, 14#CD, 93#CD, 3,400#CD, 48#CD, 113#CD, 270#CD, 50#CD, 250#CD, 17#CD, 10#CD, 3,000#CD, 56#CD, 61#CD, 85#CD, 120#CD, 130#CD, 90#CD, 1,300#CD, 2,300#CD, 30,000#CD, 53#CD, 15,000#CD, 14,000#CD, six#CD, 52#CD, 5,000#CD, 400#CD, 6,500#CD, 64#CD, five#CD, 118#CD, eleven#CD, 9,000#CD, 25,000#CD, 86#CD, 13#CD, 183#CD, 115#CD, fifty#CD, dozen#NN, 47#CD, 74#CD, 27#CD, 92#CD, 2,700#CD, 10,000#CD, 125#CD, 2,100#CD, thirty#CD, 49#CD, 114#CD, 460#CD, 43#CD, 1,200#CD, 91#CD, eight#CD, 122#CD, 23#CD, twenty#CD, 390#CD, thousand#CD, 4,000#CD, 25#CD, 57#CD, 260#CD, 107#CD, 7,000#CD, 36#CD, 20,000#CD, 12#CD, 190#CD, 1,900#CD, 105#CD, 32#CD, 75#CD, 84#CD, 225#CD, 68#CD, 150#CD, 21#CD, 37#CD, 450#CD, 77#CD, 83#CD, 2,800#CD, 71#CD, 300#CD, 42#CD, 31#CD, 88#CD, one#CD, 27,000#CD, 98#CD, 2,200#CD, 45#CD, 89#CD, 135#CD, 11#CD, 1,500#CD, 140#CD, 175#CD, 280#CD, 35#CD, 1,700#CD, 38#CD, 59#CD, 2,600#CD, nine#CD, 8,000#CD");
		String definition_70 = "the cardinal number that is the product of ten and seven";

		Synset result = mapper.mapSingle(cluster_70);
		assertEquals(definition_70, result.getDefinition());
	}

	@Test
	public void testTag2Type() {
		String tag1 = "NN";
		SynsetType type1 = SynsetType.NOUN;
		String tag4 = "NP";
		SynsetType type4 = SynsetType.NOUN;
		String tag2 = "JJ";
		SynsetType type2 = SynsetType.ADJECTIVE;
		String tag3 = "RB";
		SynsetType type3 = SynsetType.ADVERB;

		assertEquals(type1, ClusterMapper.tag2Type(tag1));
		assertEquals(type2, ClusterMapper.tag2Type(tag2));
		assertEquals(type3, ClusterMapper.tag2Type(tag3));
		assertEquals(type4, ClusterMapper.tag2Type(tag4));
	}

	@Test
	public void testEndsWith() {
		String word1 = "base";
		String[] results1 = new String[] { "air_base", "army_base",
				"first_base", "home_base", "knowledge_base", "navy_base",
				"prisoner's_base", "rocket_base", "second_base",
				"subdata_base", "tax_base", "third_base" };

		try {
			List<String> results = ClusterMapper.compoundsEndWith(word1,
					nounindexfile);
			assertArrayEquals(results1,
					results.toArray(new String[results.size()]));
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void testTokenizeAll() {
		Synset synset_airbase = mapper.getDatabase().getSynsets("air base")[0];
		String[] tokens_airbase = new String[] { "air", "base", "air",
				"station" };

		ArrayList<String> synset_airbase_tokenized = (ArrayList<String>) ClusterMapper
				.tokenizeAll(synset_airbase.getWordForms());
		assertArrayEquals(tokens_airbase, synset_airbase_tokenized.toArray());
	}
}
