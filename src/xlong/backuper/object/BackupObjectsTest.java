package xlong.backuper.object;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.DataFormatException;

import org.junit.Test;

/**
 * Test backup objects.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public class BackupObjectsTest {

	/**
	 *  other test.
	 */
	@Test
	public final void testother() {
		Path p = Paths.get("data");
		p = p.toAbsolutePath();
		System.out.println(p.toString());
		System.out.println(p.getNameCount());
		System.out.println(p.getName(0));
		System.out.println(p.getName(1));
		System.out.println(p.getRoot().toString());
	}
	
	/**
	 *  test backup objects.
	 */
	@Test
	public final void test() {
		
		Path tbpath = Paths.get("data/����.tb");
		Path trpath0 = Paths.get("data/�����Զ�.tr");
		Path trpath = Paths.get("data/����.tr");
		
		// test treeBuilder.create
		TreeBuilder tb = null;
		try {
			tb = TreeBuilder.create(tbpath);
		} catch (IOException e) {
			fail("�Ҳ���treeBuilder�����ļ�");
			e.printStackTrace();
		}
		
		// test treeBuilder.save
		String tbsha1 = null;
		try {
			tbsha1 = tb.save();
		} catch (IOException e1) {
			fail("����treeBuilderʧ��");
			e1.printStackTrace();
		}
		
		// test treeBuilder.load
		try {
			tb = (TreeBuilder) TreeBuilder.load(tbsha1);
		} catch (IOException e1) {
			fail("����treeBuilderʧ��");
			e1.printStackTrace();
		}
		
		System.out.println(tb.list());
		
		// test Tree.create
		Tree tree = null;
		try {
			tree = Tree.create(tb);
		} catch (IOException e) {
			fail("����treeʧ��");
			e.printStackTrace();
		}

		// test tree.save
		String treesha1 = null;
		try {
			treesha1 = tree.save();
		} catch (IOException e1) {
			fail("����treeʧ��");
			e1.printStackTrace();
		}
		
		// test tree.load
		try {
			tree = (Tree) Tree.load(treesha1);
		} catch (IOException e1) {
			fail("����treeʧ��");
			e1.printStackTrace();
		}
		
		System.out.println(tree.listAll());
		
		// test treeBuilder.getRestorer
		TreeRestorer tr = tb.getRestorer();
		System.out.println(tr.list());
		
		// test treeRestorer.restore
		try {
			tr.restore(trpath0);
		} catch (IOException e) {
			fail("����Ĭ��treeRestorerʧ��");
			e.printStackTrace();
		}
		
		// test treeRestorer.create
		try {
			tr = TreeRestorer.create(trpath);
		} catch (IOException e) {
			fail("�Ҳ���treeRestore�����ļ�");
			e.printStackTrace();
		}		
		System.out.println(tr.list());
		
		// test tree.restore
		try {
			tree.restore(tr);
		} catch (IOException | DataFormatException e) {
			fail("����treeʧ��");
			e.printStackTrace();
		}
		
		
	}

}
