package xlong.backuper.object;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * A treeBuilder contains a map from directories and files to nicknames.
 * <p>
 * TreeBuilder is a help classes the method
 * {@link Tree#create()} use to build new tree.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public final class TreeBuilder extends BackupObject implements Serializable {
	/** for serialization. */
	private static final long serialVersionUID = 1742196086528443955L;
	
	/** separator. */
	private static final String SEPARATOR = ",>>>>,";
	
	/** the map. */
	private TreeMap<String, String> map;
	
	/**
	 * Default constructor. Initialize map.
	 */
	public TreeBuilder() {
		map = new TreeMap<String, String>();
		calChecksum();
	}
	
	/**
	 * Gets map.
	 * @return map
	 */
	public TreeMap<String, String> getMap() {
		return map;
	}
	
	/**
	 * Add a map from path to nickname.
	 * Please make sure the map does not have ambiguous.
	 * @param path the path
	 * @param nick the nickname
	 * @return success or not
	 */
	public boolean add(final Path path, final Path nick) {
		if (map.containsKey(path.toString()) 
				|| map.containsValue(nick.toString())) {
			return false;
		}
		map.put(path.toString(), nick.toString());
		calChecksum();
		return true;
	}
	
	/**
	 * Remove a path, return its nick. If not exist return null.
	 * @param path file path
	 * @return nickname
	 */
	public String delPath(final Path path) {
		if (map.containsKey(path.toString())) {
			String nick = map.get(path.toString());
			map.remove(path.toString());
			return nick;
		} else {
			return null;
		}
	}
	
	/**
	 * Create a treeBuilder with given setting file.
	 * 
	 * @param path the setting file
	 * @return the tree. If fail, return null.
	 * @throws IOException if an I/O error occurs
	 */
	public static TreeBuilder create(final Path path) throws IOException {
		TreeBuilder tb = new TreeBuilder();	
	    BufferedReader in = 
                new BufferedReader(
                new InputStreamReader(
                new FileInputStream(path.toString()), "GB2312"));
        String line = null;
        while ((line = in.readLine()) != null) {
            String[] ss = line.split(SEPARATOR);
            if (ss.length == 2) {
                tb.add(Paths.get(ss[0]), Paths.get(ss[1]));
            }
        }
        in.close();
		return tb;
	}
	
	/**
	 * Restore the treeBuilder to the given file.
	 * If the file is already exist,
	 * this method will rewrite the file.
	 * If the output directory not exist,
	 * this method will create the directory.
	 * 
	 * @param outFilePath the path of the file restores to
	 * @return totally success or not.
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public boolean restore(final Path outFilePath) throws IOException {
	    BufferedWriter out = 
                new BufferedWriter(
                new OutputStreamWriter(
                new FileOutputStream(outFilePath.toString()), "GB2312"));
		for (Entry<String, String> en:map.entrySet()) {
			out.write(en.getKey() + SEPARATOR + en.getValue() + "\n");
		}
		out.close();
		return true;
	}
	
	/**
	 * Get the treeRestorer corresponds to this treeBuilder.
	 * @return the treeRestorer
	 */
	public TreeRestorer getRestorer() {
		TreeRestorer tr = new TreeRestorer();
		for (Entry<String, String> en:map.entrySet()) {
			tr.add(Paths.get(en.getValue()), Paths.get(en.getKey()));
		}
		return tr;
	}
	
	/**
	 * To string method.
	 * @return string
	 */
	@Override
	public String toString() {
		return "Tree Builder " + getChecksum() + "\n";
	}
	/**
	 * List maps.
	 * @return string
	 */
	public String list() {
		String s = toString();
		for (Entry<String, String> en:map.entrySet()) {
			s += en.getKey() + " -> " + en.getValue() + "\n";
		}
		return s;
	}
}
