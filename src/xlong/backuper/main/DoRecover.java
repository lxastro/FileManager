package xlong.backuper.main;

import java.util.ArrayList;

import xlong.backuper.manager.BackupManager;

/**
 * Recover latest backup. Remember modify setting/recover file
 * before run DoRecover.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public final class DoRecover {
	/**
	 * Constructor.
	 */
	private DoRecover() {
		
	}
	/**
	 * run.
	 * @param args args
	 */
	public static void main(final String[] args) {
		BackupManager bm = BackupManager.getManager();
		bm.loadRecoverSetting();
		ArrayList<String> blist = bm.getBackupList();
		if (blist.size() > 0) {
			String last = blist.get(blist.size() - 1);
			bm.recover(last);
		}
	}

}
