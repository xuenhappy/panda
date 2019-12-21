package org.bamboo.nlp.panda.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class TestAhoCorasickDoubleArrayTrie {
	private DoubleArrayTrie<String> buildASimpleAhoCorasickDoubleArrayTrie()
    {
        // Collect test data set
        TreeMap<String, String> map = new TreeMap<String, String>();
        String[] keyArray = new String[]
                {
                        "hers",
                        "his",
                        "she",
                        "he"
                };
        for (String key : keyArray)
        {
            map.put(key, key);
        }
        // Build an AhoCorasickDoubleArrayTrie
        DoubleArrayTrie<String> acdat = new DoubleArrayTrie<String>();
        acdat.build(map);
        return acdat;
    }

    private void validateASimpleAhoCorasickDoubleArrayTrie(DoubleArrayTrie<String> acdat)
    {
        // Test it
        final String text = "uhers";
        acdat.parseText(text, new DoubleArrayTrie.IHit<String>()
        {
            @Override
            public void hit(int begin, int end, String value)
            {
                System.out.printf("[%d:%d]=%s\n", begin, end, value);
                assertEquals(text.substring(begin, end), value);
            }
        });
        // Or simply use
        List<DoubleArrayTrie.Hit<String>> wordList = acdat.parseText(text);
        System.out.println(wordList);
    }

    public void testBuildAndParseSimply() throws Exception
    {
        DoubleArrayTrie<String> acdat = buildASimpleAhoCorasickDoubleArrayTrie();
        validateASimpleAhoCorasickDoubleArrayTrie(acdat);
    }

    public void testBuildAndParseWithBigFile() throws Exception
    {
        // Load test data from disk
        Set<String> dictionary = loadDictionary("cn/dictionary.txt");
        final String text = loadText("cn/text.txt");
        // You can use any type of Map to hold data
        Map<String, String> map = new TreeMap<String, String>();
//        Map<String, String> map = new HashMap<String, String>();
//        Map<String, String> map = new LinkedHashMap<String, String>();
        for (String key : dictionary)
        {
            map.put(key, key);
        }
        // Build an AhoCorasickDoubleArrayTrie
        DoubleArrayTrie<String> acdat = new DoubleArrayTrie<String>();
        acdat.build(map);
        // Test it
        acdat.parseText(text, new DoubleArrayTrie.IHit<String>()
        {
            @Override
            public void hit(int begin, int end, String value)
            {
                assertEquals(text.substring(begin, end), value);
            }
        });
    }

    private static class CountHits implements DoubleArrayTrie.IHitCancellable<String>
    {
        private int count;
        private boolean countAll;

        CountHits(boolean countAll)
        {
            this.count = 0;
            this.countAll = countAll;
        }

        public int getCount()
        {
            return count;
        }

        @Override
        public boolean hit(int begin, int end, String value)
        {
            count += 1;
            return countAll;
        }
    }

    public void testMatches()
    {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("space", 1);
        map.put("keyword", 2);
        map.put("ch", 3);
        DoubleArrayTrie<Integer> trie = new DoubleArrayTrie<Integer>();
        trie.build(map);

        assertTrue(trie.matches("space"));
        assertTrue(trie.matches("keyword"));
        assertTrue(trie.matches("ch"));
        assertTrue(trie.matches("  ch"));
        assertTrue(trie.matches("chkeyword"));
        assertTrue(trie.matches("oooospace2"));
        assertFalse(trie.matches("c"));
        assertFalse(trie.matches(""));
        assertFalse(trie.matches("spac"));
        assertFalse(trie.matches("nothing"));
    }

    private void assertTrue(Object matches) {
		// TODO Auto-generated method stub
		
	}

	private void assertFalse(Object matches) {
		// TODO Auto-generated method stub
		
	}

	public void testFirstMatch()
    {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("space", 1);
        map.put("keyword", 2);
        map.put("ch", 3);
        DoubleArrayTrie<Integer> trie = new DoubleArrayTrie<Integer>();
        trie.build(map);

        DoubleArrayTrie.Hit<Integer> hit = trie.findFirst("space");
        assertEquals(0, hit.begin);
        assertEquals(5, hit.end);
        assertEquals(1, hit.value.intValue());

        hit = trie.findFirst("a lot of garbage in the space ch");
        assertEquals(24, hit.begin);
        assertEquals(29, hit.end);
        assertEquals(1, hit.value.intValue());

        assertNull(trie.findFirst(""));
        assertNull(trie.findFirst("value"));
        assertNull(trie.findFirst("keywork"));
        assertNull(trie.findFirst(" no pace"));
    }

    private void assertEquals(Object i, Object intValue) {
		// TODO Auto-generated method stub
		
	}

	private void assertNull(Object findFirst) {
		// TODO Auto-generated method stub
		
	}

	public void testCancellation() throws Exception
    {
        // Collect test data set
        TreeMap<String, String> map = new TreeMap<String, String>();
        String[] keyArray = new String[]
                {
                        "foo",
                        "bar"
                };
        for (String key : keyArray)
        {
            map.put(key, key);
        }
        // Build an AhoCorasickDoubleArrayTrie
        DoubleArrayTrie<String> acdat = new DoubleArrayTrie<String>();
        acdat.build(map);
        // count matches
        String haystack = "sfwtfoowercwbarqwrcq";
        CountHits cancellingMatcher = new CountHits(false);
        CountHits countingMatcher = new CountHits(true);
        System.out.println("Testing cancellation");
        acdat.parseText(haystack, cancellingMatcher);
        acdat.parseText(haystack, countingMatcher);
        assertEquals(cancellingMatcher.count, 1);
        assertEquals(countingMatcher.count, 2);
    }

    private String loadText(String path) throws IOException
    {
        StringBuilder sbText = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(path), "UTF-8"));
        String line;
        while ((line = br.readLine()) != null)
        {
            sbText.append(line).append("\n");
        }
        br.close();

        return sbText.toString();
    }

    private Set<String> loadDictionary(String path) throws IOException
    {
        Set<String> dictionary = new TreeSet<String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(path), "UTF-8"));
        String line;
        while ((line = br.readLine()) != null)
        {
            dictionary.add(line);
        }
        br.close();

        return dictionary;
    }

    private void runTest(String dictionaryPath, String textPath) throws IOException
    {
        Set<String> dictionary = loadDictionary(dictionaryPath);
        String text = loadText(textPath);
        // Build a AhoCorasickDoubleArrayTrie implemented by hankcs
        DoubleArrayTrie<String> ahoCorasickDoubleArrayTrie = new DoubleArrayTrie<String>();
        TreeMap<String, String> dictionaryMap = new TreeMap<String, String>();
        for (String word : dictionary)
        {
            dictionaryMap.put(word, word);  // we use the same text as the property of a word
        }
        ahoCorasickDoubleArrayTrie.build(dictionaryMap);
        // Let's test the speed of the two Aho-Corasick automata
        System.out.printf("Parsing document which contains %d characters, with a dictionary of %d words.\n", text.length(), dictionary.size());
       
        
        ahoCorasickDoubleArrayTrie.parseText(text, new DoubleArrayTrie.IHit<String>()
        {
            @Override
            public void hit(int begin, int end, String value)
            {

            }
        });
        
        
    }

    /**
     * Compare my AhoCorasickDoubleArrayTrie with robert-bor's aho-corasick, notice that robert-bor's aho-corasick is
     * compiled under jdk1.8, so you will need jdk1.8 to run this test<br>
     * To avoid JVM wasting time on allocating memory, please use -Xms512m -Xmx512m -Xmn256m .
     *
     * @throws Exception
     */
    public void testBenchmark() throws Exception
    {
        runTest("en/dictionary.txt", "en/text.txt");
        runTest("cn/dictionary.txt", "cn/text.txt");
    }

    public void testSaveAndLoad() throws Exception
    {
        DoubleArrayTrie<String> acdat = buildASimpleAhoCorasickDoubleArrayTrie();
        final String tmpPath = System.getProperty("java.io.tmpdir").replace("\\\\", "/") + "/acdat.tmp";
        System.out.println("Saving acdat to: " + tmpPath);
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tmpPath));
        out.writeObject(acdat);
        out.close();
        System.out.println("Loading acdat from: " + tmpPath);
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(tmpPath));
        acdat = (DoubleArrayTrie<String>) in.readObject();
        validateASimpleAhoCorasickDoubleArrayTrie(acdat);
    }

    public void testBuildEmptyTrie()
    {
        DoubleArrayTrie<String> acdat = new DoubleArrayTrie<String>();
        TreeMap<String, String> map = new TreeMap<String, String>();
        acdat.build(map);
        assertEquals(0, acdat.size());
    }

    public static void main(String[] args) {
    	TreeMap<String, String> map = new TreeMap<String, String>();
        String[] keyArray = new String[]
                {
                        "foo",
                        "bar",
                        "owe"
                };
        for (String key : keyArray)
        {
            map.put(key, key);
        }
        // Build an AhoCorasickDoubleArrayTrie
        DoubleArrayTrie<String> acdat = new DoubleArrayTrie<String>();
        acdat.build(map);
        // count matches
        String haystack = "sfwtfoowercwbarqwrcq";
      
        acdat.parseText(haystack, new DoubleArrayTrie.IHitCancellable<String>() {

			@Override
			public boolean hit(int begin, int end, String value) {
				System.out.println(begin+","+end+","+value);
				return true;
			}
        	
        });
		
	}
}
