package disks;

import javax.swing.JDialog;

import memory.Core;

public class TestDUIDialog {
	
	private DiskControlUnit dcu;

	public static void main(String[] args) {
		
		new TestDUIDialog().doIt();		// simple invocation of DUI	
		//new TestDUIDialog().doIt2();
	}
	
	private void doIt(){
		dcu = new DiskControlUnit(new Core(1024));
		DiskUserInterface duid = new DiskUserInterface(dcu);
		duid.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		duid.setVisible(true);
	}

}
