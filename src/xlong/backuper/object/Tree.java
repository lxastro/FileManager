package xlong.backuper.object;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.DataFormatException;

import xlong.backuper.util.CompressionUtil;
import xlong.backuper.util.SHA1Util;

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
	
	/** the map of blob nicknames to blob objects. */
	private TreeMap<String, Blob> blobs;
	/** the map of tree nicknames to tree objects. */
	private TreeMap<String, Tree> trees;
	
	/**
	 * Default constructor. Initialize lists.
	 */
	private Tree() {
		blobs = new TreeMap<>();
		trees = new TreeMap<>();
		calName();
	}	
	
	/**
	 * Calculate the name of this tree.
	 * <p>
	 * Sort blobs, trees, blobNames and treeNames by their names.
	 * Calculate SHA-1 checksum of this tree using string of list method.
	 * Set the name of this tree to "checksum[0-1]/checksum[2-39]".
	 * The blobs and trees list with their name cannot have duplicate.
	 */
	private void calName() {
		String checksum = null;
		checksum = SHA1Util.sha1Checksum(list(), true);
		setName(checksumToName(checksum));		
	}
	
	/**
	 * Get blobs map.
	 * @return blobs list
	 */
	public TreeMap<String, Blob> getBlobs() {
		return blobs;
	}
	
	/**
	 * Get trees list.
	 * @return trees list
	 */
	public TreeMap<String, Tree> getTrees() {
		return trees;
	}

	/**
	 * Get the string contains blobs and their names.
	 * @return the string contains blobs and their names
	 */
	public String listBlobs() {
		String s = "";
		for (Entry<String, Blob> en:blobs.entrySet()) {
			Blob blob = en.getValue();
			String nick = en.getKey();
			s += "blob " + blob.toString() + " " + nick + "\n";
		}
		return s;
	}
	
	/**
	 * Get the string contains trees and their names.
	 * @return the string contains trees and their names
	 */
	public String listTrees() {
		String s = "";
		for (Entry<String, Tree> en:trees.entrySet()) {
			Tree tree = en.getValue();
			String nick = en.getKey();
			s += "tree " + tree.toString() + " " + nick + "\n";
		}
		return s;
	}

	/**
	 * Get the object with give nickname.
	 * @param nick the nickname, may be path.
	 * @return the object
	 */
	public BackupObject get(final String nick) {
		Path path = Paths.get(nick);
		if (path.getNameCount() == 1) {
			if (blobs.containsKey(nick)) {
				return blobs.get(nick);
			}
			if (trees.containsKey(nick)) {
				return trees.get(nick);
			}
			return null;
		}
		String first = path.getName(0).toString();
		if (trees.containsKey(first)) {
			return trees.get(first).
					get(path.subpath(1, path.getNameCount()).toString());
		}
		return null;
	}
	
	/**
	 * Get the String contains both blobs and trees and their names.
	 * @return the String contains both blobs and trees and their names
	 */
	public String list() {
		return listBlobs() + listTrees();
	}
	
	/**
	 * Get  the String contains blobs and blobs in subtrees and their names.
	 * @return the String contains blobs and blobs in subtrees and their names
	 */	
	public String listAll() {
		String s = listBlobs();
		for (Entry<String, Tree> en:trees.entrySet()) {
			Tree tree = en.getValue();
			String nick = en.getKey();
			s += "tree " + tree.toString() + " " + nick + "{\n";
			s += tree.listAll() + "}\n";
		}		
		return s;
	}
	
	/**
	 * Converts tree to string. 
	 * @return only the SHA-1 checksum
	 */
	@Override
	public String toString() {
		return getName().substring(0, 2) + getName().substring(2 + 1);
	}
	
	/**
	 * Create a empty tree.
	 * @return a empty tree
	 */
	public static Tree create() {
		return new Tree();
	}
	
	/**
	 * Create a tree contains given directory.
	 * 
	 * @param dir the directory to contain
	 * @return the tree. If fail, return null.
	 * @throws IOException IOException.
	 */
	public static Tree create(final String dir) 
			throws IOException {
		Tree tree = null;
		Path dirPath = Paths.get(dir);
		if (Files.isDirectory(dirPath)) {
			tree = Tree.create();
			DirectoryStream<Path> paths = Files.newDirectoryStream(dirPath);
			for (Path p:paths) {
				if (Files.isDirectory(p)) {
					Tree subtree = Tree.create(p.toString());
					tree.add(subtree, p.getFileName().toString());
				} else {
					Blob blob = Blob.create(p.toString());
					tree.add(blob, p.getFileName().toString());
				}
            } 
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
		Tree tree = null;
		if (tb.getMap().size() > 0) {
			tree = Tree.create();
			for (Entry<String, String> en:tb.getMap().entrySet()) {
				Path dirPath = Paths.get(en.getKey());
				if (Files.isDirectory(dirPath)) {
					Tree subtree = Tree.create(dirPath.toString());
					tree.add(subtree, en.getValue());
				} else {
					Blob blob = Blob.create(dirPath.toString());
					tree.add(blob, en.getValue());
				}
			}
		}
		return tree;
	}
	
	/**
	 * Add a new blob to this tree.
	 * 
	 * @param blob the blob to add
	 * @param nick the nickname of the blob to add, can be a path.
	 * @return totally success or not
	 */
	public boolean add(final Blob blob, final String nick) {
		boolean flag = true;
		Path path = Paths.get(nick);
		if (path.getNameCount() == 1) {
			if (blobs.containsKey(nick)) {
				flag = false;
			} else {
				blobs.put(nick, blob);
				calName();
			}	
		} else {
			String first = path.getName(0).toString();
			if (!trees.containsKey(first)) {
				add(Tree.create(), first);
			}
			if (trees.get(first).add(blob, 
					path.subpath(1, path.getNameCount()).toString())) {
				flag = false;
			}
			calName();
		}
		return flag;
	}
	
	/**
	 * Add a new tree to this tree.
	 * 
	 * @param tree the tree to add
	 * @param nick the nickname of the tree to add, can be a path.
	 * @return success or not
	 */
	public boolean add(final Tree tree, final String nick) {
		boolean flag = true;
		Path path = Paths.get(nick);
		if (path.getNameCount() == 1) {
			if (trees.containsKey(nick)) {
				flag = false;
			} else {
				trees.put(nick, tree);
				calName();
			}
		} else {
			String first = path.getName(0).toString();
			if (!trees.containsKey(first)) {
				add(Tree.create(), first);
			}
			if (trees.get(first).add(tree, 
					path.subpath(1, path.getNameCount()).toString())) {
				flag = false;
			}
			calName();			
		}
		return flag;
	}
	
	/**
	 * Restore the tree to the given directory.
	 * If a file in the tree already exist,
	 * this method will rewrite the file.
	 *  
	 * @param dir the directory to restore this tree
	 * @return totally success or not.
	 * @throws IOException if the file is not found or the cannot be read.
	 * @throws DataFormatException  if the file in the blob is not a blob.
	 */
	public boolean restore(final String dir) 
			throws IOException, DataFormatException {
		boolean flag = true;
		System.out.println("Restore to " + dir);
		Path outFileDir = Paths.get(dir);
		Files.createDirectories(outFileDir);
		for (Entry<String, Blob> en:blobs.entrySet()) {
			Blob blob = en.getValue();
			String nick = en.getKey();
			if (!blob.restore(dir + "/" + nick)) {
				flag = false;
			}
		}
		for (Entry<String, Tree> en:trees.entrySet()) {
			Tree tree = en.getValue();
			String nick = en.getKey();
			if (!tree.restore(dir + "/" + nick)) {
				flag = false;
			}
		}
		return flag;
	}
	
	/**
	 * Restore the tree according to treeRestorer.
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
		TreeMap<String, String> map = tr.getMap();
		for (Entry<String, String> en:map.entrySet()) {
			String nick = en.getKey();
			String path = en.getValue();
			BackupObject o = get(nick);
			if (!o.restore(path)) {
				flag = false;
			}
		}
		return flag;
	}
	
	
	/**
	 * Save this tree object into file system.
	 * If this tree object already exist, do nothing.
	 * @return SHA-1 checksum of this tree
	 * @throws IOException  if the file is not found or the cannot be read.
	 */
	public String save() throws IOException {
		Path outFilePath = Paths.get(getObjectDir() + "/" + getName());
		
		// Create output file directory.
		Files.createDirectories(outFilePath.getParent());
		if (!Files.exists(outFilePath)) {
			System.out.println(
					"Compress tree " + getName()
					+ " to " + outFilePath);
			CompressionUtil.compress(list(), 
					outFilePath.toString(), true);
		}
		// Save subtrees.
		for (Entry<String, Tree> en:trees.entrySet()) {
			Tree tree = en.getValue();
			tree.save();
		}	
		return toString();
	}
	
	/**
	 * Load tree object with given checksum.
	 * @param checksum checksum of the tree.
	 * @return the loaded tree
	 * @throws IOException if the file is not found or the cannot be read.
	 * @throws DataFormatException  if the file in the blob is not a blob.
	 */
	public static Tree load(final String checksum)
			throws IOException, DataFormatException {
		String name = checksumToName(checksum);
		String inFileName = getObjectDir() + "/" + name;
		//System.out.println(inFileName);
		String list = CompressionUtil.decompress(inFileName);
		//System.out.println(list);
		Tree tree = Tree.create();
		String[] objects = list.split("\n");
		for (String s: objects) {
			String[] parts = s.split(" ");
			if (parts.length > 2) {
				if (parts[0].equals("blob")) {
					Blob blob = Blob.get(parts[1]);
					String nick = parts[2];
					tree.add(blob, nick);
				} else {
					Tree subtree = Tree.load(parts[1]);
					String nick = parts[2];
					tree.add(subtree, nick);
				}
			}
		}
		return tree;
	}
	
	/**
	 * Testing code.
	 * @param args args
	 */
	public static void main(final String[] args) {
		String oriFile = "data/test";
		String treeDir = "data/tree";
		Blob b1 = null;
		Blob b2 = null;
		try {
			b1 = Blob.create(oriFile);
			b2 = Blob.create(oriFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Tree t = Tree.create();
		System.out.println(t.toString());
		System.out.println(t.list());
		
		t.add(b1, "file1");
		t.add(b2, "file2");
		System.out.println(t.toString());
		System.out.println(t.list());	
		
		Tree t2 = Tree.create();
		t2.add(b1, "file3");
		t2.add(b1, "file4");
		t2.add(Tree.create(), "emptyDir1");
		t2.add(Tree.create(), "emptyDir2");
		t.add(t2, "dir");
		System.out.println(t.toString());
		System.out.println(t.list());	
	
		String checksum = null;
		try {
			checksum = t.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			t = Tree.load(checksum);
		} catch (IOException | DataFormatException e1) {
			e1.printStackTrace();
		}
		
		try {
			t.restore(treeDir);
		} catch (IOException | DataFormatException e) {
			e.printStackTrace();
		}
		
		Tree newTree = null;
		try {
			newTree = Tree.create(treeDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		t.add(newTree, "newdir/newdir2/FullTree");
		
		System.out.println(t.toString());
		System.out.println(t.list());

		try {
			t.restore(treeDir);
		} catch (IOException | DataFormatException e) {
			e.printStackTrace();
		}

	}
}
