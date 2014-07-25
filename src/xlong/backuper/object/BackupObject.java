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

import xlong.backuper.util.CompressionUtil;
import xlong.backuper.util.SHA1Util;


/**
 * The abstract class of all kinds of backup object classes.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public abstract class BackupObject 
			implements Comparable<BackupObject>, Serializable {
	
	/** for serialization. */
	private static final long serialVersionUID = 4193229535711024873L;
	
	/** the directory to store objects. */
	private static String objectDir = "object";
	
	/**
	 * Sets the object directory.
	 * @param dir the object directory wants to set.
	 */
	public static final void setObjectDir(final String dir) {
		objectDir = dir;
	}
	
	/**
	 * Gets the object directory. 
	 * @return the object directory.
	 */
	public static final String getObjectDir() {
		return objectDir;
	}
	
	/**
	 * Converts checksum to name.
	 * The name of a object is "checksum[0-1]/checksum[2-39]".
	 * 
	 * @param checksum checksum
	 * @return name
	 */
	protected static final String checksumToName(final String checksum) {
		return (checksum.substring(0, 2) + "/" + checksum.substring(2));
	}
	
	/**
	 * Converts checksum to path.
	 * @param checksum checksum
	 * @return the path
	 */
	protected static final Path checksumToPath(final String checksum) {
		return Paths.get(getObjectDir() + "/" + checksumToName(checksum));
	}
	
	/** the SHA-1 checksum of this object. */
	private String checksum;
	
	/**
	 * Gets the checksum of this object.
	 * @return the checksum of this object.
	 */
	public final String getChecksum() {
		return checksum;
	}
	
	/**
	 * Gets the path of this object.
	 * @return the path of this object.
	 */
	public final Path getPath() {
		return checksumToPath(checksum);
	}

	/**
	 * Sets the checksum of this object.
	 * @param sha1Checksum the SHA-1 checksum to set
	 */
	protected final void setChecksum(final String sha1Checksum) {
		checksum = sha1Checksum;
	}
	

	/**
	 * Save this object to file system. Use serialization change the object
	 * into byte array.Then calculate, set and return the SHA1-checksum.
	 * Finally save the compress the string to a file.
	 * If the directory not exist, this method will create the directory.
	 * 
	 * @return SHA-1 checksum of this object
	 * @throws IOException if an I/O error occurs
	 */
	public final String save() throws IOException {
		// change to byte array
        byte[] bytesArray = toByteArray();
        // set the checksum
        setChecksum(SHA1Util.sha1Checksum(bytesArray));
        // save to file
		Path outFilePath = getPath();
		Files.createDirectories(outFilePath.getParent());
        CompressionUtil.compressByteArray(bytesArray, outFilePath);
        return checksum;
	}
	
	/**
	 * Load the backup object.
	 * If fail return null.
	 * 
	 * @param checksum the checksum
	 * @return the backup object
	 * @throws IOException if an I/O error occurs
	 */
	public static final BackupObject load(final String checksum)
			throws IOException {
		
		Path filePath = BackupObject.checksumToPath(checksum);
		byte[] bytesArray = CompressionUtil.decompressToByteArray(filePath);	
		
		ByteArrayInputStream bi = new ByteArrayInputStream(bytesArray);
        ObjectInputStream oi = new ObjectInputStream(bi);   

        try {
			return (BackupObject) oi.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Use serialization change the object into byte array.
	 * 
	 * @return the byte array
	 */
	protected final byte[] toByteArray() {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(bs);
			os.writeObject(this);
		} catch (IOException e) {
			e.printStackTrace();
		}   
        byte[] bytesArray = bs.toByteArray();
        return bytesArray;
	}
	
	/**
	 * Calculate the checksum of this tree.
	 * Notice Blob object must not use this method.
	 * The checksum of Blob can not modify.
	 */
	protected final void calChecksum() {
		byte[] byteArray = toByteArray();
		String newChecksum = SHA1Util.sha1Checksum(byteArray);
		setChecksum(newChecksum);		
	}
	
	
	/**
	 * Restore the object to the given directory.
	 * If a file is already exist,
	 * this method will rewrite the file.
	 * If the output directory not exist,
	 * this method will create the directory.
	 * 
	 * @param outFilePath the path of the file restores to
	 * @return totally success or not.
	 * @throws IOException if an I/O error occurs
	 */
	public abstract boolean restore(Path outFilePath) throws IOException;

	
	@Override
	public abstract String toString();
	
	/**
	 * Compares one object to another.
	 * Just compare their checksum
	 * 
	 * @param o the object compare to
	 * @return the compare result.
	 */
	@Override
	public final int compareTo(final BackupObject o) {
		return checksum.compareTo(o.checksum);
	}

}
