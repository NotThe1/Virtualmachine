package disks;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;

import memory.Core;

public class Test1 {
	private Core core;
	private int currentLocation;

	// MakeNewDisk mnd;

	/**
	 * @wbp.parser.entryPoint
	 */
	public static void main(String[] args) {

		new Test1().doIt7(); // test Random.nextInt(bound);
		//new Test1().doIt6(); // test Random.nextInt(bound);
		// new Test1().doIt5(); // Check DCU & core w/ trap
		// new Test1().doIt4(); // invoke make new disk to check return value
		// new Test1().doIt3(); //check out Random().nextBytes()
		// new Test1().doIt2(); // simple read of a disk
		// new Test1().doIt1(); // list all elements of DiskLayout- to check validity

	}// main
	private void doIt7(){
		byte[] rawData = new byte[]{(byte) 0x00,(byte) 0x41,(byte) 0x42,(byte) 0x43,(byte) 0x44,
		                          (byte) 0x45,(byte) 0x00,(byte) 0x31,(byte) 0x32,(byte) 0x33,(byte) 0x34,(byte) 0x35};
		System.out.printf("rawData.length = %s%n",rawData.length);
		String strData = new String(rawData);
		System.out.printf("strData = %s%n",strData);
		System.out.printf("strData.length() = %s%n",strData.length());
		byte[] testByte = new byte[5];
		System.out.printf("--%s--%n",new String(testByte));
		
	}
	
	private void doIt6(){
		int iterations = 30;
		int bound = 50;
		Random random = new Random();
		for (int i = 0; i < iterations;i++){
			System.out.printf("bound = %d, value = %d%n"
					+ "", bound,random.nextInt(bound));
		}
		
	}

	private void doIt5() {
		core = new Core(4096);
		DiskControlUnit dcu = new DiskControlUnit(core);
		dcu.addDiskDrive(0, "C:\\Users\\admin\\git\\Virtualmachine\\VirtualMachine\\Disks\\EightDS.F8DS");
		System.out.printf("%nIn doIt5()%n");
		int controlByteLocation8 = 0X0040;
		int controlTableLocation = 0X0100;

		currentLocation = controlTableLocation;
		
		loadMemory(0X0200,128,(byte)0X55);
		
		writeNextByte((byte) 0X02); // 01 => read /02 => write
		writeNextByte((byte) 0X00); // unit
		writeNextByte((byte) 0X00); // head
		writeNextByte((byte) 0X00); // track
		writeNextByte((byte) 0X01); // sector
		writeNextByte((byte) 0X00); // lo byteCount byte count = 00FF
		writeNextByte((byte) 0X02); // hi byteCount
		writeNextByte((byte) 0X00); // lo DMA DMA = 00C8
		writeNextByte((byte) 0X02); // hi DMA

		core.write(0X0041, (byte) 00);
		core.write(0X0042, (byte) 01); // point at the controlTable
		core.write(controlByteLocation8, (byte) 0X80); // Set the Control byte to start IO
		
		
		
//		core.write(0X0040,(byte) 0X02); // 01 => read /02 => write
//		core.write(0X0041, (byte) 00);
//		core.write(0X0042, (byte) 01); // point at the controlTable
//		core.write(controlByteLocation8, (byte) 0X80); // Set the Control byte to start IO
//		
		System.out.printf("Test1: Status: %02X - %02X%n",core.readForIO(0X0043), core.readForIO(0X0044));
		System.out.printf("Test1: Value:  %02X, + 128: = %02X%n", core.readForIO(0X0200),core.readForIO(0X027F));

		dcu = null;
	}// doIt5()
	
	private void loadMemory(int location,int length, byte value){
		for (int i = 0; i < length; i++){
			core.write(location++, value);
		}//for
	}//loadMemory

	private void writeNextByte(byte value) {
		core.write(currentLocation++, value);
	}//

	private void doIt4() {
		System.out.printf("Disk = %s%n", MakeNewDisk.makeNewDisk());
	}// doIt4()

	private void doIt3() {
		byte[] sourceData = new byte[15];
		new Random().nextBytes(sourceData);
		for (int i = 0; i < sourceData.length; i++) {
			System.out.printf("i: %2d, value: %02X%n", i, sourceData[i]);
		}// for
	}// doIt3()

	private void doIt2() {
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
	}// doIt2()

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
		}// for
	}// doIt1()

}// class Test1
