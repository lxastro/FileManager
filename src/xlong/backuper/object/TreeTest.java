package xlong.backuper.object;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.zip.DataFormatException;

import org.junit.Test;

import xlong.backuper.util.SHA1Util;

/**
 * JUnit test class for Tree class.
 *
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public class TreeTest {

	/**
	 * Test methods in Tree class.
	 */
	@Test
	public final void test() {
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
		
		String c0 = null;
		String c1 = null;
		String c2 = null;
		String c3 = null;
		String c4 = null;
		try {
			c0 = SHA1Util.sha1Checksum(oriFile);
			c1 = SHA1Util.sha1Checksum(treeDir + "/file1");
			c2 = SHA1Util.sha1Checksum(treeDir + "/file2");
			c3 = SHA1Util.sha1Checksum(treeDir + "/dir/file3");
			c4 = SHA1Util.sha1Checksum(treeDir + "/dir/file4");
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(c0, c1);
		assertEquals(c0, c2);
		assertEquals(c0, c3);
		assertEquals(c0, c4);
	}

}
