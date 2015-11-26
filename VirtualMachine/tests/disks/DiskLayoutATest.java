package disks;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;

public class DiskLayoutATest {
	DiskLayoutA disk8SS;
	DiskLayoutA disk8DS;
	DiskLayoutA disk5DD;
	DiskLayoutA disk5HD;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		disk8SS = DiskLayoutA.F8SS;
		disk8DS = DiskLayoutA.F8DS;
		disk5DD = DiskLayoutA.F5DD;
		disk5HD = DiskLayoutA.F5HD;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void simpleTest8SS() {
		int head = 1;
		int tph = 77;
		int spt = 26;
		int bps = 128;
		assertThat("simpleTest8SS - 1 ", head, equalTo(disk8SS.heads));
		assertThat("simpleTest8SS - 2 ", tph, equalTo(disk8SS.tracksPerHead));
		assertThat("simpleTest8SS - 3 ", spt, equalTo(disk8SS.sectorsPerTrack));
		assertThat("simpleTest8SS - 3 ", bps, equalTo(disk8SS.bytesPerSector));
		
		int tsod = head * tph * spt;
		assertThat("simpleTest8SS - 4 ", tsod, equalTo(disk8SS.getTotalSectorsOnDisk()));
		int tsph = disk8SS.tracksPerHead * disk8SS.sectorsPerTrack;
		assertThat("simpleTest8SS - 5 ", tsph, equalTo(disk8SS.getTotalSectorsPerHead()));
		long cap = tsod * disk8SS.bytesPerSector;
		assertThat("simpleTest8SS - 6 ", cap, equalTo(disk8SS.getTotalBytes()));
		
	}
	@Test
	public void simpleTest8DS() {
		int head = 2;
		int tph = 77;
		int spt = 26;
		int bps = 128;
		assertThat("simpleTest8DS - 1 ", head, equalTo(disk8DS.heads));
		assertThat("simpleTest8DS - 2 ", tph, equalTo(disk8DS.tracksPerHead));
		assertThat("simpleTest8DS - 3 ", spt, equalTo(disk8DS.sectorsPerTrack));
		assertThat("simpleTest8DS - 3 ", bps, equalTo(disk8DS.bytesPerSector));
		
		int tsod = head * tph * spt;
		assertThat("simpleTest8DS - 4 ", tsod, equalTo(disk8DS.getTotalSectorsOnDisk()));
		int tsph = disk8DS.tracksPerHead * disk8DS.sectorsPerTrack;
		assertThat("simpleTest8DS - 5 ", tsph, equalTo(disk8DS.getTotalSectorsPerHead()));
		long cap = tsod * disk8DS.bytesPerSector;
		assertThat("simpleTest8DS - 6 ", cap, equalTo(disk8DS.getTotalBytes()));


	}
	@Test
	public void simpleTest5DD() {
		int head = 2;
		int tph = 40;
		int spt = 9;
		int bps = 512;
		assertThat("simpleTest5DD - 1 ", head, equalTo(disk5DD.heads));
		assertThat("simpleTest5DD - 2 ", tph, equalTo(disk5DD.tracksPerHead));
		assertThat("simpleTest5DD - 3 ", spt, equalTo(disk5DD.sectorsPerTrack));
		assertThat("simpleTest5DD - 3 ", bps, equalTo(disk5DD.bytesPerSector));
		
		int tsod = head * tph * spt;
		assertThat("simpleTest5DD - 4 ", tsod, equalTo(disk5DD.getTotalSectorsOnDisk()));
		int tsph = disk5DD.tracksPerHead * disk5DD.sectorsPerTrack;
		assertThat("simpleTest5DD - 5 ", tsph, equalTo(disk5DD.getTotalSectorsPerHead()));
		long cap = tsod * disk5DD.bytesPerSector;
		assertThat("simpleTest5DD - 6 ", cap, equalTo(disk5DD.getTotalBytes()));

	}

	@Test
	public void simpleTest5HD() {
		int head = 2;
		int tph = 80;
		int spt = 15;
		int bps = 512;
		assertThat("simpleTest5HD - 1 ", head, equalTo(disk5HD.heads));
		assertThat("simpleTest5HD - 2 ", tph, equalTo(disk5HD.tracksPerHead));
		assertThat("simpleTest5HD - 3 ", spt, equalTo(disk5HD.sectorsPerTrack));
		assertThat("simpleTest5HD - 3 ", bps, equalTo(disk5HD.bytesPerSector));
		
		int tsod = head * tph * spt;
		assertThat("simpleTest5HD - 4 ", tsod, equalTo(disk5HD.getTotalSectorsOnDisk()));
		int tsph = disk5HD.tracksPerHead * disk5HD.sectorsPerTrack;
		assertThat("simpleTest5HD - 5 ", tsph, equalTo(disk5HD.getTotalSectorsPerHead()));
		long cap = tsod * disk5HD.bytesPerSector;
		assertThat("simpleTest5HD - 6 ", cap, equalTo(disk5HD.getTotalBytes()));

	}
	@Test
	public void testSPT(){
		int target = 26;
		assertThat("testSPT - 1 ", target, equalTo(disk8SS.getSPT()));
		target = 52;
		assertThat("testSPT - 2 ", target, equalTo(disk8DS.getSPT()));
		target = 72;
		assertThat("testSPT - 3 ", target, equalTo(disk5DD.getSPT()));
		target = 120;
		assertThat("testSPT - 4 ", target, equalTo(disk5HD.getSPT()));	
	}
	@Test
	public void testBSH(){
		int target = 3;int sectorsPerBlock = 8;
		disk8SS.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testBSH - 1 ", target, equalTo(disk8SS.getBSH()));
		
		 target = 3; sectorsPerBlock = 8;
		disk8DS.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testBSH - 2 ", target, equalTo(disk8DS.getBSH()));
		
		target = 4;sectorsPerBlock = 4;
		disk5DD.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testBSH - 3 ", target, equalTo(disk5DD.getBSH()));
		
		target = 4;sectorsPerBlock = 4;		
		disk5HD.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testBSH - 4 ", target, equalTo(disk5HD.getBSH()));	
	}
	@Test
	public void testBLM(){
		int target = 7;int sectorsPerBlock = 8;
		disk8SS.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testBLM - 1 ", target, equalTo(disk8SS.getBLM()));

		 target = 7; sectorsPerBlock = 8;
		disk8DS.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testBSH - 2 ", target, equalTo(disk8DS.getBLM()));
		
		target = 15;sectorsPerBlock = 4;
		disk5DD.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testBSH - 3 ", target, equalTo(disk5DD.getBLM()));
		
		target = 15;sectorsPerBlock = 4;		
		disk5HD.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testBSH - 4 ", target, equalTo(disk5HD.getBLM()));	
	}
	@Test
	public void testOFS(){
		int target = 3;
		assertThat("testOFS - 1 ", target, equalTo(disk8SS.getOFS()));

		 target = 2; 
		assertThat("testOFS - 2 ", target, equalTo(disk8DS.getOFS()));
	
		target = 1;
		assertThat("testOFS - 3 ", target, equalTo(disk5DD.getOFS()));
		
		target = 1;
		assertThat("testOFS - 4 ", target, equalTo(disk5HD.getOFS()));	
	}
	@Test
	public void testDSM(){
		int target = 239;int sectorsPerBlock = 8;
		disk8SS.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testDSM - 1 ", target, equalTo(disk8SS.getDSM()));

		 target = 486; sectorsPerBlock = 8;
		disk8DS.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testDSM - 2 ", target, equalTo(disk8DS.getDSM()));
		
		target = 174;sectorsPerBlock = 4;
		disk5DD.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testDSM - 3 ", target, equalTo(disk5DD.getDSM()));
		
		target = 591;sectorsPerBlock = 4;		
		disk5HD.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testDSM - 4 ", target, equalTo(disk5HD.getDSM()));	
	}
	@Test
	public void testDRM(){
		int target = 63;int sectorsPerBlock = 8;
		disk8SS.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testDSM - 1 ", target, equalTo(disk8SS.getDRM()));

		 target = 63; sectorsPerBlock = 8;
		disk8DS.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testDSM - 2 ", target, equalTo(disk8DS.getDRM()));
		
		target = 127;sectorsPerBlock = 4;
		disk5DD.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testDSM - 3 ", target, equalTo(disk5DD.getDRM()));
		
		target = 127;sectorsPerBlock = 4;		
		disk5HD.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testDSM - 4 ", target, equalTo(disk5HD.getDRM()));	
	}
	@Test
	public void testAL01(){
		int target = 0xC000;
		assertThat("testAL01 - 1 ", target, equalTo(disk8SS.getAL01()));

		 target = 0x8000;
		 disk8DS.setDirectoryBlockCount(1);
		assertThat("testAL01 - 2 ", target, equalTo(disk8DS.getAL01()));
		
		target = 0xC000;
		assertThat("testAL01 - 3 ", target, equalTo(disk5DD.getAL01()));
		
		target = 0xC000;
		assertThat("testAL01 - 4 ", target, equalTo(disk5HD.getAL01()));	
	}
	@Test
	public void testCKS(){
		int target = 16;int sectorsPerBlock = 8;
		disk8SS.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testCKS - 1 ", target, equalTo(disk8SS.getCKS()));

		 target = 16; sectorsPerBlock = 8;
		disk8DS.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testCKS - 2 ", target, equalTo(disk8DS.getCKS()));
		
		target = 32;sectorsPerBlock = 4;
		disk5DD.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testCKS - 3 ", target, equalTo(disk5DD.getCKS()));
		
		target = 32;sectorsPerBlock = 4;		
		disk5HD.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testCKS - 4 ", target, equalTo(disk5HD.getCKS()));	
	}
	@Test
	public void testEXM(){
		int target = 0;int sectorsPerBlock = 8;
		disk8SS.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testEXM - 1 ", target, equalTo(disk8SS.getEXM()));

// N/A
//		 target = 0; sectorsPerBlock = 8;
//		disk8DS.setSectorsPerBlock(sectorsPerBlock);
//		assertThat("testEXM - 2 ", target, equalTo(disk8DS.getEXM()));
		
		target = 1;sectorsPerBlock = 4;
		disk5DD.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testEXM - 3 ", target, equalTo(disk5DD.getEXM()));
		
		target = 0;sectorsPerBlock = 4;		
		disk5HD.setSectorsPerBlock(sectorsPerBlock);
		assertThat("testEXM - 4 ", target, equalTo(disk5HD.getEXM()));	
	}


}
