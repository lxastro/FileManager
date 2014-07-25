package xlong.backuper.manager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.DataFormatException;

import xlong.backuper.object.Tree;
import xlong.backuper.object.TreeBuilder;
import xlong.backuper.object.TreeRestorer;

/**
 * A backup contains a treeBuilder, a tree, a time and a message.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public class Backup implements Serializable {

	/** for serialization. */
	private static final long serialVersionUID = -7994290094413122718L;
	
	/** the treeBuilder. */
	private String treeBuilder;
	
	/** the tree. */
	private String tree;
	
	/** the date. */
	private Date date;
	
	/** the message. */
	private String message;
	
	/**
	 * Constructor.
	 * @param intb the tree builder
	 * @param intree the tree
	 * @param inmessage the message
	 */
	public Backup(final String intb, 
			final String intree, final String inmessage) {
		date = new Date();
		tree = intree;
		treeBuilder = intb;
		message = inmessage;
	}
	
	/**
	 * gets date.
	 * @return date
	 */
	public final String getDate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
		return df.format(date);
	}
	
	/**
	 * gets message.
	 * @return message
	 */
	public final String getMessage() {
		return message;
	}
	
	/**
	 * gets tree.
	 * @return tree.
	 * @throws IOException if an I/O error occurs
	 */
	public final Tree getTree() throws IOException {
		return (Tree) Tree.load(tree);
	}
	
	/**
	 * gets treeBuilder.
	 * @return treeBuilder
	 * @throws IOException if an I/O error occurs
	 */
	public final TreeBuilder getTreeBuilder() throws IOException {
		return (TreeBuilder) TreeBuilder.load(treeBuilder);
	}
	
	/**
	 * Save this backup to file.
	 * If the directory not exist, this method will create it.
	 * 
	 * @param path the file save to
	 * @throws IOException if an I/O error occurs
	 */
	public final void save(final Path path) throws IOException {
		Files.createDirectories(path.getParent());
		FileOutputStream bs = new FileOutputStream(path.toString());
        ObjectOutputStream os = new ObjectOutputStream(bs);   
        os.writeObject(this);
        bs.close();
	}
	
	/**
	 * Load a backup.
	 * @param path the file to load
	 * @return the backup
	 * @throws IOException if an I/O error occurs
	 * @throws ClassNotFoundException 
	 * the file does not contains a backup instance 
	 */
	public static final Backup load(final Path path)
			throws IOException, ClassNotFoundException {
		FileInputStream fi = new FileInputStream(path.toString());
        ObjectInputStream oi = new ObjectInputStream(fi);   
        Backup bc = (Backup) oi.readObject();
        fi.close();
        return bc;
	}
	
	/**
	 * Recover the backup.
	 * @param tr the tree restorer
	 * @return success or not
	 */
	public final boolean recover(final TreeRestorer tr) {
		try {
			((Tree) Tree.load(tree)).restore(tr);
		} catch (IOException | DataFormatException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * To string method.
	 * @return string
	 */
	@Override
	public final String toString() {
		String s = "";
		try {
			s += "TreeBuilder:\n";
			s += ((TreeBuilder) TreeBuilder.load(treeBuilder)).list();
			s += "\n";

			s += "Tree: ";
			s += ((Tree) Tree.load(tree)).listAll();
			s += "\n";
			
			s += "Date: " + getDate() + "\n";
			s += "Message: " + message + "\n";
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
 		return s;
	}
	
}