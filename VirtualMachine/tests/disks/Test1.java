package disks;

import hardware.Core;


import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Random;


import javax.swing.JFileChooser;


public class Test1 {

	// MakeNewDisk mnd;

	public static void main(String[] args) {
		
		new Test1().doIt5();	// Check DCU & core w/ trap
		//new Test1().doIt4();	// invoke make new disk to check return value
		//new Test1().doIt3();  //check out Random().nextBytes()
		//new Test1().doIt2();	// simple read of a disk
		//new Test1().doIt1();	// list all elements of DiskLayout- to check validity

	}//main
	
	private void doIt5(){
		Core core = new Core(1024);
		DiskControlUnit dcu = new DiskControlUnit(core);
		int location = 0X0040;		//disk control table
		byte value = (byte) 0XFF;	// set high-order bit to start I/O
		core.write(location, value);
		System.out.printf("%nIn doIt5()%n");
		
}
	private void doIt4(){
		System.out.printf("Disk = %s%n", MakeNewDisk.makeNewDisk());
}
	
	private void doIt3(){
		byte[] sourceData = new byte[15];
		new Random().nextBytes(sourceData);
		for (int i = 0;i < sourceData.length;i++){
			System.out.printf("i: %2d, value: %02X%n",i,sourceData[i]);
		}
	}
	
	private void doIt2(){
		Path sourcePath = Paths.get(".", "Disks");
		String fp = sourcePath.resolve(".").toString();
		JFileChooser chooser = new JFileChooser(fp);
		if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
			return; // do nothing
		}// if
		DiskDrive mdd = new DiskDrive(chooser.getSelectedFile().getPath());
		mdd.setCurrentAbsoluteSector(100);
		byte[] ans = mdd.read();
System.out.printf("marker 1%n");
		int a = 0;
				
	}

	private void doIt1() {

		for (DiskLayout diskLayout : DiskLayout.values()) {
			int bps = diskLayout.bytesPerSector;
			long tb = diskLayout.getTotalBytes();
			String d = diskLayout.descriptor;
			String fe = diskLayout.fileExtension;
			System.out.printf("%s,\t%s bps: %d, tb %d - %d KB%n", d, fe, bps, tb, tb / 1024);
			int heads = diskLayout.heads;
			int tracks = diskLayout.tracksPerHead;
			int sectors = diskLayout.sectorsPerTrack;
			long capacity = (heads * tracks * sectors * bps) / 1024;

			System.out.printf("%s,\t%3d - %3d - %3d - - %10d %4d KB , Capacity: %4d KB%n%n%n", d,
					heads, tracks, sectors, bps, tb, tb / 1024, capacity);

		}

	}

}// class Test1
