package xlong.backuper.util;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.junit.Test;

/**
 * JUnit test class for SHA1Util class.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public class SHA1Test {

	/**
	 * Test method for {@link xlong.LXBackuper.Util
	 * .SHA1#sha1Checksum(java.lang.String)}.
	 */
	@Test
	public final void testSha1ChecksumString() {
		String filePath = "doc/resources/background.gif";
		String sha = null;
		try {
			sha = SHA1Util.sha1Checksum(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String resSHA = "19ab0c0d7b0d3ce0b293453388a5faf8109da297";
		assertEquals(sha, resSHA);
	}

	/**
	 * Test method for {@link xlong.LXBackuper
	 * .Util.SHA1#sha1Checksum(java.lang.String, boolean)}.
	 */
	@Test
	public final void testSha1ChecksumStringBoolean() {
		String input = "doc/resources/background.gif";
		String shaT = null;
		shaT = SHA1Util.sha1Checksum(input, true);
		String resSHAT = "07262c761b8486faee07a26b1a440f1bccc21104";
		assertEquals(shaT, resSHAT);
	}

}
