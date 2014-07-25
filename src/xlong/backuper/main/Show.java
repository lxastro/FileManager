package xlong.backuper.main;

import java.util.ArrayList;

import xlong.backuper.manager.BackupManager;

/**
 * Show current state.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public final class Show {
	/**
	 * Constructor.
	 */
	private Show() {
		
	}
	/**
	 * run.
	 * @param args args
	 */
	public static void main(final String[] args) {
		BackupManager bm = BackupManager.getManager();
		System.out.println("History:");
		ArrayList<String> blist = bm.getBackupList();
		for (String s:bm.getBackupList()) {
			System.out.println(s);
		}
		if (blist.size() > 0) {
			System.out.println("\nLast:");
			String last = blist.get(blist.size() - 1);
			bm.showBackup(last);
		}
	}

}
