package xlong.backuper.object;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.Map.Entry;


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
public final class TreeRestorer extends BackupObject {

	/** for serialization. */
	private static final long serialVersionUID = -6375350327761178575L;
	/** the map. */
	private TreeMap<String, String> map;
	/** separator. */
	private static final String SEPARATOR = ",>>>>,";
	
	/**
	 * Default Constructor. Initialize map.
	 */
	protected TreeRestorer() {
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
	public boolean add(final Path nick, final Path path) {
		if (map.containsKey(nick.toString()) 
				|| map.containsValue(path.toString())) {
			return false;
		}
		map.put(nick.toString(), path.toString());
		calChecksum();
		return true;
	}
	
	/**
	 * Remove a nick.
	 * @param nick the nick name
	 */
	public void delNick(final Path nick) {
		if (map.containsKey(nick.toString())) {
			map.remove(nick.toString());
		}
	}
	
	/**
	 * Create a treeRestorer with given setting file.
	 * 
	 * @param path the setting file
	 * @return the tree. If fail, return null.
	 * @throws IOException if an I/O error occurs
	 */
	public static TreeRestorer create(final Path path) throws IOException {
		TreeRestorer tr = new TreeRestorer();	
	    BufferedReader in = 
                new BufferedReader(
                new InputStreamReader(
                new FileInputStream(path.toString()), "GB2312"));
        String line = null;
        while ((line = in.readLine()) != null) {
            String[] ss = line.split(SEPARATOR);
            if (ss.length == 2) {
                tr.add(Paths.get(ss[0]), Paths.get(ss[1]));
            }
        }
        in.close();
		return tr;
	}
	
	/**
	 * Restore the treeRestorer to the given file.
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
	 * To string method.
	 * @return string
	 */
	@Override
	public String toString() {
		return "Tree Restorer " + getChecksum() + "\n";
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
