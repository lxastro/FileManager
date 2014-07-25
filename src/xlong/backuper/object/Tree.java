package xlong.backuper.object;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.DataFormatException;

/**
 * A tree object contains a map of blob nicknames to blob objects
 *  and a map of tree nicknames to other tree objects.
 * <p>
 * A tree is similar to a directory.
 * It can contain several blob objects and several subtrees.
 * A subtree is another tree object.
 * Both the blobs and subtrees are identified by its SHA-1 checksum.
 * Both the blobs and subtrees have their nickname in this tree.
 * These name may be different in different tree objects.
 * Nicknames of blobs cannot duplicate.
 * Nicknames of trees cannot duplicate.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public final class Tree extends BackupObject {
	
	/** for serialization. */
	private static final long serialVersionUID = -8205593088522114084L;
	
	/** for INDENT. */
	private static final String INDENT = "    ";
	
	/** the map of blob nicknames to blob objects. */
	private TreeMap<String, Blob> blobs;
	/** the map of tree nicknames to tree objects. */
	private TreeMap<String, Tree> trees;
	
	/**
	 * Default constructor. Initialize maps.
	 */
	private Tree() {
		blobs = new TreeMap<>();
		trees = new TreeMap<>();
		calChecksum();
	}	
	
	/**
	 * Get blobs map.
	 * @return blobs map
	 */
	public TreeMap<String, Blob> getBlobs() {
		return blobs;
	}
	
	/**
	 * Get trees map.
	 * @return trees map
	 */
	public TreeMap<String, Tree> getTrees() {
		return trees;
	}
	
	/**
	 * Get the object with give nickname path.
	 * If exist both tree and blob, return blob.
	 * If not exist return null.
	 * @param path the nickname path.
	 * @return the backup object
	 */
	public BackupObject get(final Path path) {
		if (path.getNameCount() == 1) {
			if (blobs.containsKey(path.toString())) {
				return blobs.get(path.toString());
			}
			if (trees.containsKey(path.toString())) {
				return trees.get(path.toString());
			}
			return null;
		}
		Path first = path.getName(0);
		if (trees.containsKey(first.toString())) {
			return trees.get(first.toString()).
					get(first.relativize(path));
		}
		return null;
	}
	
	/**
	 * Create a tree contains given directory.
	 * 
	 * @param dirPath the directory to contain
	 * @return the tree. If fail, return null.
	 * @throws IOException if an I/O error occurs
	 */
	private static Tree create(final Path dirPath) 
			throws IOException {
		System.out.println("Create Tree for " + dirPath);
		Tree tree = null;
		if (Files.isDirectory(dirPath)) {
			tree = new Tree();
			DirectoryStream<Path> paths = Files.newDirectoryStream(dirPath);
			for (Path p:paths) {
				if (Files.isDirectory(p)) {
					Tree subtree = Tree.create(p);
					tree.add(subtree, dirPath.relativize(p));
				} else {
					Blob blob = Blob.create(p);
					tree.add(blob, dirPath.relativize(p));
				}
            }
			tree.calChecksum();
		}
		return tree;
	}
	
	/**
	 * Create a tree using treeBuilder.
	 * @param tb the treeBuilder
	 * @return the tree
	 * @throws IOException IOException
	 */
	public static Tree create(final TreeBuilder tb) 
			throws IOException {
		Tree tree = new Tree();
		if (tb.getMap().size() > 0) {
			for (Entry<String, String> en:tb.getMap().entrySet()) {
				Path dirPath = Paths.get(en.getKey());
				if (Files.isDirectory(dirPath)) {
					Tree subtree = Tree.create(dirPath);
					tree.add(subtree, Paths.get(en.getValue()));
				} else {
					Blob blob = Blob.create(dirPath);
					tree.add(blob, Paths.get(en.getValue()));
				}
			}
		}
		return tree;
	}
	
	/**
	 * Add a new blob to this tree.
	 * If the path already exist, fail.
	 * 
	 * @param blob the blob to add
	 * @param path the nickname path of the blob to add, can be a path.
	 * @return totally success or not
	 */
	private boolean add(final Blob blob, final Path path) {
		boolean flag = true;
		if (path.getNameCount() == 1) {
			if (blobs.containsKey(path.toString())) {
				flag = false;
			} else {
				blobs.put(path.toString(), blob);
			}	
		} else {
			Path first = path.getName(0);
			if (!trees.containsKey(first.toString())) {
				add(new Tree(), first);
			}
			if (!trees.get(first).add(blob, first.relativize(path))) {
				flag = false;
			}
		}
		calChecksum();
		return flag;
	}
	
	/**
	 * Add a new tree to this tree.
	 * If the path already exist, fail.
	 * 
	 * @param tree the tree to add
	 * @param path the nickname path of the tree to add, can be a path.
	 * @return success or not
	 */
	private boolean add(final Tree tree, final Path path) {
		boolean flag = true;
		if (path.getNameCount() == 1) {
			if (trees.containsKey(path.toString())) {
				flag = false;
			} else {
				trees.put(path.toString(), tree);
			}
		} else {
			Path first = path.getName(0);
			if (!trees.containsKey(first.toString())) {
				add(new Tree(), first);
			}
			if (!trees.get(first).add(tree, first.relativize(path))) {
				flag = false;
			}	
		}
		calChecksum();
		return flag;
	}
	
	/**
	 * Restore the tree to the given directory.
	 * If a file in the tree already exist,
	 * this method will rewrite the file.
	 *  
	 * @param outFileDir the directory to restore this tree
	 * @return totally success or not.
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public boolean restore(final Path outFileDir) 
			throws IOException {
		System.out.println("Restore Tree to " + outFileDir);
		boolean flag = true;
		Files.createDirectories(outFileDir);
		for (Entry<String, Blob> en:blobs.entrySet()) {
			Blob blob = en.getValue();
			Path nick = Paths.get(en.getKey());
			if (!blob.restore(outFileDir.resolve(nick))) {
				flag = false;
			}
		}
		for (Entry<String, Tree> en:trees.entrySet()) {
			Tree tree = en.getValue();
			Path nick = Paths.get(en.getKey());
			if (!tree.restore(outFileDir.resolve(nick))) {
				flag = false;
			}
		}
		return flag;
	}
	
	/**
	 * Restore the tree according to treeRestorer.
	 * If a file not in the tree already exist,
	 * this method will not delete the file.
	 * If a file in the tree already exist,
	 * this method will rewrite the file.
	 *  
	 * @param tr the treeRestorer
	 * @return totally success or not.
	 * @throws IOException if the file is not found or the cannot be read.
	 * @throws DataFormatException  if the file in the blob is not a blob.
	 */
	public boolean restore(final TreeRestorer tr) 
			throws IOException, DataFormatException {
		boolean flag = true;
		for (Entry<String, String> en:tr.getMap().entrySet()) {
			Path nick = Paths.get(en.getKey());
			Path path = Paths.get(en.getValue());
			BackupObject o = get(nick);
			if (!o.restore(path)) {
				flag = false;
			}
		}
		return flag;
	}
	
	/**
	 * Converts tree to string.
	 * @return the string
	 */
	@Override
	public String toString() {
		return "Tree " + getChecksum() + "\n";
	}	
	
	/**
	 * Get the string representation of blobs and their names.
	 * @param depth the depth of the blob
	 * @return the string contains blobs and their names
	 */
	public String listBlobs(final int depth) {
		String s = "";
		String dp = "";
		for (int i = 0; i < depth; i++) {
			dp += INDENT;
		}
		for (Entry<String, Blob> en:blobs.entrySet()) {
			Blob blob = en.getValue();
			Path nick = Paths.get(en.getKey());
			s += dp + nick + ": " + blob.toString();
		}
		return s;
	}
	
	/**
	 * Get the string representation of blobs and their names.
	 * @return the string contains blobs and their names
	 */
	public String listBlobs() {
		return listBlobs(0);
	}
	
	/**
	 * Get the string representation of trees and their names.
	 * @param depth the depth of the tree
	 * @return the string contains trees and their names
	 */
	public String listTrees(final int depth) {
		String s = "";
		String dp = "";
		for (int i = 0; i < depth; i++) {
			dp += INDENT;
		}
		for (Entry<String, Tree> en:trees.entrySet()) {
			Tree tree = en.getValue();
			Path nick = Paths.get(en.getKey());
			s += dp + nick + ": " + tree.toString();
		}
		return s;
	}

	/**
	 * Get the string representation of trees and their names.
	 * @return the string contains trees and their names
	 */
	public String listTrees() {
		return listTrees(0);
	}

	/**
	 * Get the String contains both blobs and trees and their names.
	 * @param depth the depth of the tree
	 * @return the String contains both blobs and trees and their names
	 */
	public String list(final int depth) {
		return listBlobs(depth) + listTrees(depth);
	}
	
	/**
	 * Get the String contains both blobs and trees and their names.
	 * @return the String contains both blobs and trees and their names
	 */
	public String list() {
		return toString() + listBlobs(1) + listTrees(1);
	}
	
	/**
	 * Get the String contains blobs and blobs in subtrees and their names.
	 * @param depth the depth of the tree
	 * @return the String contains blobs and blobs in subtrees and their names
	 */	
	public String listAll(final int depth) {
		String s = listBlobs(depth);
		String dp = "";
		for (int i = 0; i < depth; i++) {
			dp += INDENT;
		}
		for (Entry<String, Tree> en:trees.entrySet()) {
			Tree tree = en.getValue();
			Path nick = Paths.get(en.getKey());
			s += dp + nick + ": " + tree.toString();
			s += dp + "{\n";
			s += tree.listAll(depth + 1);
			s += dp + "}\n";
		}		
		return s;
	}
	
	/**
	 * Get the String contains blobs and blobs in subtrees and their names.
	 * @return the String contains blobs and blobs in subtrees and their names
	 */	
	public String listAll() {
		String s = toString();
		s += "{\n";
		s += listAll(1);
		s += "}\n";
		return s;
	}
}
