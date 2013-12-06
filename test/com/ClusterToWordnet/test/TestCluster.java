package com.ClusterToWordnet.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.LinkedList;

import org.junit.Test;

import com.ClusterToWordnet.Cluster;
import com.ClusterToWordnet.Token;

public class TestCluster {

	@Test
	public void test_token_parser() {
		String test1 = "player#NN";
		String word1 = "player";
		String pos1 = "NN";

		Token token = new Token(test1);
		assertEquals(word1, token.getWord());
		assertEquals(pos1, token.getPos());
	}

	@Test
	public void test_cluster_parser() {
		String line1 = "player#NN\t0\tdevice#NN, gadget#NN";
		String word1 = "player";
		String pos1 = "NN";
		int id1 = 0;
		int specificSize1 = 2;

		String line2 = "player#NN\t1\tsuperstar#NN, actor#NN, leaguer#NN, championship#NN, cricketer#NN, umpire#NN, rider#NN, staffs#NN, improviser#NN, stealer#NN, gymnast#NN, teammate#NN, cornerbacks#NN, applicant#N";
		String word2 = "player";
		String pos2 = "NN";
		int id2 = 1;
		int specificSize2 = 14;

		String line3 = "player#NN\t0\tdevice#NN, gadget#NN\tabc#def";
		String word3 = "player";
		String pos3 = "NN";
		int id3 = 0;
		int specificSize3 = 2;

		Cluster cluster = new Cluster(line1);
		assertEquals(id1, cluster.getId());
		assertEquals(word1, cluster.getWord().getWord());
		assertEquals(pos1, cluster.getWord().getPos());
		assertEquals(specificSize1, cluster.getSpecificWords().size());

		cluster = new Cluster(line2);
		assertEquals(id2, cluster.getId());
		assertEquals(word2, cluster.getWord().getWord());
		assertEquals(pos2, cluster.getWord().getPos());
		assertEquals(specificSize2, cluster.getSpecificWords().size());

		cluster = new Cluster(line3);
		assertEquals(id3, cluster.getId());
		assertEquals(word3, cluster.getWord().getWord());
		assertEquals(pos3, cluster.getWord().getPos());
		assertEquals(specificSize3, cluster.getSpecificWords().size());

	}

	@Test
	public void test_cluster_filereader() {
		String filename = "/home/carsten/tu/testClusters.feats";
		int filesize = 9;
		LinkedList<Cluster> clusters;

		try {
			clusters = Cluster.readClusterFile(filename, 0);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
			return;
		}
		Cluster cluster1 = clusters.getFirst();
		Cluster cluster9 = clusters.getLast();

		assertEquals(filesize, clusters.size());
		assertEquals(".21", cluster1.getWord().getWord());
		assertEquals("CD", cluster1.getWord().getPos());
		assertEquals(0, cluster1.getId());
		assertEquals(101, cluster1.getSpecificWords().size());

		assertEquals(".30", cluster9.getWord().getWord());
		assertEquals("CD", cluster9.getWord().getPos());
		assertEquals(0, cluster9.getId());
		assertEquals(34, cluster9.getSpecificWords().size());
	}

	@Test
	public void test_cluster_filereader_maxlines() {
		String filename = "/home/carsten/tu/testClusters.feats";
		LinkedList<Cluster> clusters;

		try {
			clusters = Cluster.readClusterFile(filename, 2);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
			return;
		}
		assertEquals(2, clusters.size());
	}
	
	@Test
	public void testIsNumber() {
		Cluster cluster1 = new Cluster(
				".21#CD\t0\t.66#CD, .08#CD, .13#CD, .14#CD, .82#CD, .85#CD, .1#CD, billion#CD, .04#CD, .50#NN, .68#CD, .12#CD, ,250#CD, .65#CD, .60#CD, .25#CD, .43#CD, .47#CD, .41#CD, .52#CD, .6#CD, .34#CD, .29#CD, ,000#CD, .19#CD, .55#CD, .39#CD, .95#CD, .05#CD, .62#CD, .24#CD, .16#CD, ,200#CD, .46#CD, .32#CD, ,300#CD, .20#CD, .78#CD, .48#CD, .44#CD, .7#CD, ,400#CD, .11#CD, .67#CD, ,500#CD, .70#CD, .99#CD, ,700#CD, .17#CD, $#$, .9#CD, .49#CD, .45#CD, ,800#CD, ,600#CD, .07#CD, ,900#CD, .4#CD, .50#CD, .03#CD, .42#CD, .37#CD, .15#CD, .26#CD, .00#CD, .92#CD, .18#CD, .76#CD, .5#CD, .56#CD, .22#CD, .8#CD, .33#CD, .06#CD, .02#CD, .2#CD, .28#CD, .31#CD, .59#CD, ,100#CD, .30#CD, oooo#NP, .35#CD, .38#CD, .27#CD, .75#CD, .01#CD, .09#CD, .63#CD, .40#CD, .3#CD, .72#CD, .54#CD, .10#CD, sen#CD, .80#CD, .36#CD, .64#CD, .58#CD, .90#CD, .23#CD");
		Cluster cluster2 = new Cluster(
				".21#CD\t1\teuros#NN, pence#NN, reais#NN");
		Cluster cluster3 = new Cluster(
				".21#NN\t1\teuros#NN, pence#NN, reais#NN");
		Cluster cluster4 = new Cluster(
				"four#CD\t1\teuros#NN, pence#NN, reais#NN");

		assertTrue(cluster1.isNumber());
		assertTrue(cluster2.isNumber());
		assertFalse(cluster3.isNumber());
		assertFalse(cluster4.isNumber());
	}
}
