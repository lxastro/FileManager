package xlong.backuper.object;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.zip.DataFormatException;

import org.junit.Test;

import xlong.backuper.util.SHA1Util;

/**
 * JUnit test class for Blob class.
 *
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public class BlobTest {

	/**
	 * Test method
	 * {@link xlong.backuper.object.Blob#create(String)} 
	 * and
	 * {@link xlong.backuper.object.Blob#restore(Blob, String)}
	 * for small file.
	 */
	@Test
	public final void test() {
		Blob blob;
		String oriFile = "data/test";
		String resFile = oriFile + "_res";
		String checksumOri = null;
		String checksumRes = null;
		try {
			blob = Blob.create(oriFile);
			Blob.restore(blob, resFile);
			checksumOri = SHA1Util.sha1Checksum(oriFile);
			checksumRes = SHA1Util.sha1Checksum(resFile);
		} catch (IOException | DataFormatException e) {
			org.junit.Assert.fail();
			e.printStackTrace();
		}
		assertEquals(checksumOri, checksumRes);
	}

	/**
	 * Test method
	 * {@link xlong.backuper.object.Blob#create(String)} 
	 * and
	 * {@link xlong.backuper.object.Blob#restore(Blob, String)}
	 * for big file.
	 */
	@Test
	public final void testBig() {
		Blob blob;
		String oriFile = "data/test2";
		String resFile = oriFile + "_res";
		String checksumOri = null;
		String checksumRes = null;
		try {
			blob = Blob.create(oriFile);
			Blob.restore(blob, resFile);
			checksumOri = SHA1Util.sha1Checksum(oriFile);
			checksumRes = SHA1Util.sha1Checksum(resFile);
		} catch (IOException | DataFormatException e) {
			org.junit.Assert.fail();
			e.printStackTrace();
		}
		assertEquals(checksumOri, checksumRes);
	}
	
	/**
	 * Test method for
	 * {@link xlong.backuper.object.Blob#toString()} 
	 * and
	 * {@link xlong.backuper.object.Blob#get(String)}.
	 */
	@Test
	public final void testString() {
		Blob blob;
		String oriFile = "data/test";
		String resFile = oriFile + "_res";
		String checksumOri = null;
		String checksumRes = null;
		try {
			blob = Blob.create(oriFile);
			String s = blob.toString();
			System.out.println(s);
			blob = Blob.get(s);
			Blob.restore(blob, resFile);
			checksumOri = SHA1Util.sha1Checksum(oriFile);
			checksumRes = SHA1Util.sha1Checksum(resFile);
		} catch (IOException | DataFormatException e) {
			org.junit.Assert.fail();
			e.printStackTrace();
		}
		assertEquals(checksumOri, checksumRes);
	}
}
