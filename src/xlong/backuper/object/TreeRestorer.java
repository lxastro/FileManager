package xlong.backuper.object;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;


/**
 * A treeRestorer contains a map from nickname to directories and files.
 * <p>
 * TreeRestorer is a help classes the method
 * {@link Tree#restore(String)} use to restore tree.
 * TreeRestorer can only be created by a treeBuilder.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public class TreeRestorer {

	/** the map. */
	private TreeMap<String, String> map;
	
	/**
	 * Default Constructor. Initialize map.
	 */
	TreeRestorer() {
		map = new TreeMap<String, String>();
	}

	/**
	 * Gets map.
	 * @return map
	 */
	public final TreeMap<String, String> getMap() {
		return map;
	}
	
	/**
	 * Add a map from path to nickname.
	 * Please make sure the map does not have ambiguous.
	 * @param path the path
	 * @param nick the nickname
	 * @return success or not
	 */
	final boolean add(final String nick, final String path) {
		if (map.containsKey(nick) || map.containsValue(path)) {
			return false;
		}
		map.put(nick, path);
		return true;
	}
	
	/**
	 * To string method.
	 * @return string
	 */
	@Override
	public final String toString() {
		String s = "";
		for (Entry<String, String> en:map.entrySet()) {
			s += en.getKey() + " -> " + en.getValue() + "\n";
		}
		return s;
	}

	/**
	 * Save this treeRestorer.
	 * @param fileName the file to save in
	 * @throws IOException IOException
	 */
	public final void save(final String fileName) 
			throws IOException {
		BufferedWriter out = new
				BufferedWriter(new FileWriter(fileName));
		out.write(toString());
		out.close();
	}
	
	/**
	 * Load this treeRestorer.
	 * @param fileName the file to load
	 * @return the treeRestorer
	 * @throws IOException IOException
	 */
	public static final TreeRestorer load(final String fileName)
			throws IOException {
		TreeRestorer tr = new TreeRestorer();
		BufferedReader in = 
				new BufferedReader(new FileReader(fileName));
		String line = null;
		while ((line = in.readLine()) != null) {
			String[] ss = line.split("->");
			if (ss.length == 2) {
				tr.add(ss[0].trim(), ss[1].trim());
			}
		}
		in.close();
		return tr;
	}
	
	/**
	 * Testing code.
	 * @param args args
	 */
	public static void main(final String[] args) {
		TreeRestorer tr = null;
		try {
			tr = TreeRestorer.load("data/treeRestore.tr");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(tr.toString());
		
		Tree tree = null;
		try {
			tree = Tree.create("data/treeBuilderRestore");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(tree.toString());
		//System.out.println(tree.listAll());
		
		try {
			tree.restore(tr);
		} catch (IOException | DataFormatException e) {
			e.printStackTrace();
		}
	}
	
	
}
