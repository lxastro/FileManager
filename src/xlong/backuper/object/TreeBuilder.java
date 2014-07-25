package xlong.backuper.object;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.DataFormatException;

import xlong.backuper.util.CompressionUtil;
import xlong.backuper.util.SHA1Util;

/**
 * A treeBuilder contains a map from directories and files to nicknames.
 * <p>
 * TreeBuilder is a help classes the method
 * {@link Tree#create()} use to build new tree.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public class TreeBuilder implements Serializable {
	/** for serialization. */
	private static final long serialVersionUID = 1742196086528443955L;
	
	/** the map. */
	private TreeMap<String, String> map;
	
	/**
	 * Default constructor. Initialize map.
	 */
	public TreeBuilder() {
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
	public final boolean add(final String path, final String nick) {
		if (map.containsKey(path) || map.containsValue(nick)) {
			return false;
		}
		map.put(path, nick);
		return true;
	}
	/**
	 * Save this treeBuilder.
	 * @return SHA-1 checksum of this treeBuilder
	 * @throws IOException IOException
	 */
	public final String save() throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bs);   
        os.writeObject(this);
        String content = new String(bs.toByteArray(), "ISO-8859-1");
        String checksum = SHA1Util.sha1Checksum(content, true);
        
		Path outFilePath = Paths.get(BackupObject.checksumToPath(checksum));
		Files.createDirectories(outFilePath.getParent());
        CompressionUtil.compress(content, outFilePath.toString(), true);
        return checksum;
	}
	
	/**
	 * Load this treeBuilder.
	 * @param checksum the checksum
	 * @return the treeBuilder
	 * @throws IOException IOException
	 * @throws ClassNotFoundException ClassNotFoundException 
	 * @throws DataFormatException DataFormatException
	 */
	public static final TreeBuilder load(final String checksum)
			throws IOException, ClassNotFoundException, DataFormatException {
		String filePath = BackupObject.checksumToPath(checksum);
		String content = CompressionUtil.decompress(filePath);	
		System.out.println(content);
		
		ByteArrayInputStream bi = 
				new ByteArrayInputStream(content.getBytes("ISO-8859-1"));
        ObjectInputStream oi = new ObjectInputStream(bi);   
        TreeBuilder tb = (TreeBuilder) oi.readObject();
        
        return tb;
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
	 * Get the treeRestorer corresponds to this treeBuilder.
	 * @return the treeRestorer
	 */
	public final TreeRestorer getRestorer() {
		TreeRestorer tr = new TreeRestorer();
		for (Entry<String, String> en:map.entrySet()) {
			tr.add(en.getValue(), en.getKey());
		}
		return tr;
	}
	
	/**
	 * Testing code.
	 * @param args args
	 */
	public static void main(final String[] args) {
		TreeBuilder tb = new TreeBuilder();
		tb.add("data/tree/dir", "backup1/folder/tree");
		tb.add("data/tree/newdir", "backup2/folder/tree");
		tb.add("data/test", "backup1/file/test");
		
		// test save and load
		String cs = null;
		try {
			cs = tb.save();
			System.out.println(cs);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			tb = TreeBuilder.load(cs);
		} catch (ClassNotFoundException | IOException | DataFormatException e) {
			e.printStackTrace();
		}
		System.out.println(tb.toString());
		
		//test create tree
		Tree tree = null;
		try {
			tree = Tree.create(tb);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(tree.toString());
		System.out.println(tree.listAll());
		try {
			tree.restore("data/treeBuilderRestore");
		} catch (IOException | DataFormatException e) {
			e.printStackTrace();
		}
		
		//test Restorer
		TreeRestorer tr = tb.getRestorer();
		try {
			tr.save("data/treeRestore.tr");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(tr.toString());
	}
	
	
	
}
