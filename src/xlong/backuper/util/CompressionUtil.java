package xlong.backuper.util;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * This class wrap ZLIB compression for convenience.
 * <p>
 * This class can just deal with small files (smaller than around 1.3G).
 * If the outFile is exist, methods in this class will replace the exist one.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public final class CompressionUtil {

	/**
	 * Private constructor to make sure no instance of this class will be
	 * created.
	 */
	private CompressionUtil() {
		// will not be called
	}
	
	/** Maximum number of bytes to read in each loop.*/
	private static final int MAXBYTE = 1024;
	
	/**
	 * Compress given input file and output to given output file.
	 * The output directory must exist.
	 * 
	 * @param inFile the input file name
	 * @param outFile the output file name
	 * @throws IOException if the file is not found or the cannot be read.
	 */
	public static void compress(
			final String inFile, 
			final String outFile) 
					throws IOException {
		
		byte[] data = toByteArray(inFile);
		
		Deflater deflater = new Deflater();
		deflater.setInput(data);

		BufferedOutputStream out = 
				new BufferedOutputStream(new FileOutputStream(outFile));
		
		deflater.finish();
		byte[] buffer = new byte[MAXBYTE];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			out.write(buffer, 0, count);
		}
		deflater.end();
		out.close();
	}

	/**
	 * Decompress given input file and output to given output file.
	 * The output directory must exist.
	 * 
	 * @param inFile the input file name
	 * @param outFile the output file name
	 * @throws IOException if the file is not found or the cannot be read.
	 * @throws DataFormatException if the file is not a ZLIB compress file.
	 */	
	public static void decompress(
			final String inFile, 
			final String outFile) 
					throws IOException, DataFormatException {
		
		byte[] data = toByteArray(inFile);
		Inflater inflater = new Inflater();
		inflater.setInput(data);

		BufferedOutputStream out = 
				new BufferedOutputStream(new FileOutputStream(outFile));
		
		byte[] buffer = new byte[MAXBYTE];
		while (!inflater.finished()) {
			int count = inflater.inflate(buffer);
			out.write(buffer, 0, count);
		}
		inflater.end();
		out.close();
	}
	
	/**
	 * Compress given string and output to given output file.
	 * The output directory must exist.
	 * 
	 * @param string the input string
	 * @param outFile the output file name
	 * @param flag a tag to differ from {@link #compress(String, String)}.
	 * 				  Sets to any if compressing a string.
	 * @throws IOException if the file is not found or the cannot be read.
	 */
	public static void compress(
			final String string, 
			final String outFile,
			final boolean flag) 
					throws IOException {
	
		byte[] data = string.getBytes("ISO-8859-1");
		
		Deflater deflater = new Deflater();
		deflater.setInput(data);

		BufferedOutputStream out = 
				new BufferedOutputStream(new FileOutputStream(outFile));
		
		deflater.finish();
		byte[] buffer = new byte[MAXBYTE];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			out.write(buffer, 0, count);
		}
		deflater.end();
		out.close();
	}

	/**
	 * Decompress given input file and output to a string.
	 * The output directory must exist.
	 * 
	 * @param inFile the input file name
	 * @return the output string
	 * @throws IOException if the file is not found or the cannot be read.
	 * @throws DataFormatException if the file is not a ZLIB compress file.
	 */	
	public static String decompress(
			final String inFile) 
					throws IOException, DataFormatException {
		
		byte[] data = toByteArray(inFile);
		Inflater inflater = new Inflater();
		inflater.setInput(data);
		
		StringBuilder outString = new StringBuilder();
		
		byte[] buffer = new byte[MAXBYTE];
		while (!inflater.finished()) {
			int count = inflater.inflate(buffer);
			outString.append(new String(buffer, 0, count, "ISO-8859-1"));
		}
		inflater.end();
		return outString.toString();
	}

	/**
	 * Reads a file and saves its content into a byte array.
	 * Method in Mapped File way.
	 * The MappedByteBuffer can improve performance when dealing with big file.
	 * @param filename the name of the file to read
	 * @return the byte array saves its content
	 * @throws IOException if the file is not found or the cannot be read.
	 */
	public static byte[] toByteArray(final String filename)throws IOException {
		
		RandomAccessFile raf = null;
		FileChannel fc = null;
		try {
			raf = new RandomAccessFile(filename, "r");
			fc = raf.getChannel();
			MappedByteBuffer byteBuffer = 
					fc.map(MapMode.READ_ONLY, 0, fc.size()).load();
			byte[] result = new byte[(int) fc.size()];
			if (byteBuffer.remaining() > 0) {
				byteBuffer.get(result, 0, byteBuffer.remaining());
			}	
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				fc.close();
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
