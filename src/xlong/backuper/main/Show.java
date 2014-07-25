package xlong.backuper.main;

import java.io.IOException;
import java.util.ArrayList;

import xlong.backuper.manager.BackupManager;
/**
 * Show current state.
 * And create a default backup and recover setting. 
 * Set the toShow to change the backup to show.
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
	
	/** the date of backup to recover. 	 
	 *  set null to get the lastone.  
	 */
	private static String toShow = null;
	/**
	 * run.
	 * @param args args
	 */
	public static void main(final String[] args) {
		BackupManager bm = BackupManager.getManager();
		
		ArrayList<String> blist = bm.getBackupList();
		if (blist.size() > 0) {
			if (toShow == null) {
				System.out.println("Last:");
				String last = blist.get(blist.size() - 1);
				System.out.println(last);
				bm.showBackup(last);
				try {
					bm.setTreeBuilder(bm.getBackup(last).getTreeBuilder());
					bm.setTreeRestorer(bm.getBackup(last)
							.getTreeBuilder().getRestorer());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println(toShow);
				bm.showBackup(toShow);
				try {
					bm.setTreeBuilder(bm.getBackup(toShow).getTreeBuilder());
					bm.setTreeRestorer(bm.getBackup(toShow)
							.getTreeBuilder().getRestorer());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		bm.createBackupSetting();
		bm.createRecoverSetting();
		System.out.println("History:");
		for (String s:bm.getBackupList()) {
			System.out.println(s + " " + bm.getBackup(s).getMessage());
		}
	}

}
