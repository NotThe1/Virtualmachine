package disks;

public enum DiskLayout {
	F3DD(2, 80, 9, 512, "F3DD", "3.5\"  DD   720 KB"),
	F3HD(2, 80, 18, 512, "F3HD", "3.5\"  HD   1.44 MB"),
	F3ED(2, 80, 36, 512, "F3ED", "3.5\"  ED   2.88 MB"),
	F5DD(2, 40, 9, 512, "F5DD", "5.25\" DD   360 KB"),
	F5HD(2, 80, 15, 512, "F5HD", "5.25\" HD   1.2 MB"),
	F8SS(1, 77, 26, 128, "F8SS", "8\"    SS   256 KB"),
	F8DS(2, 77, 26, 128, "F8DS", "8\"    DS   512 KB");

	public final int heads;
	public final int tracksPerHead;
	public final int sectorsPerTrack;
	public final int bytesPerSector;
	public final String fileExtension;
	public final String descriptor;

	DiskLayout(int heads, int tracksPerHead, int sectorsPerTrack, int bytesPerSector,
			String fileExtension, String descriptor) {
		this.heads = heads;
		this.tracksPerHead = tracksPerHead;
		this.sectorsPerTrack = sectorsPerTrack;
		this.bytesPerSector = bytesPerSector;
		this.fileExtension = fileExtension;
		this.descriptor = descriptor;
	}// Constructor

	public long getTotalBytes() {
		return getTotalSectorsOnDisk() * bytesPerSector;
	}//

	public int getTotalSectorsOnDisk() {
		return heads * tracksPerHead * sectorsPerTrack;
	}//

	public int getTotalSectorsPerHead() {
		return tracksPerHead * sectorsPerTrack;
	}//

	// --------------------------------------------------------------------------
	// Disk Parameter Block CP/M 2.2
	// --------------------------------------------------------------------------
	
	// Boot disk
	private boolean bootDisk = true;
	public void setBootDisk(boolean state) {
		this.bootDisk = state;
	}
	public boolean isBootDisk() {
		return this.bootDisk;
	}

	// Block size
	private int blockSize;
	private int sectorsPerBlock;
	public void setSectorsPerBlock(int sectorsPerBlock) {
		this.sectorsPerBlock = sectorsPerBlock <= 0 ? 1 : sectorsPerBlock;
		this.blockSize = this.sectorsPerBlock * this.bytesPerSector;
	}

	//Directory Block count
	private int directoryBlockCount = 2;
	public void setDirectoryBlockCount(int directoryBlockCount){
		this.directoryBlockCount = directoryBlockCount <= 0 ? 1 : directoryBlockCount;
	}
	public int getDirectoryBlockCount(){
		return this.directoryBlockCount;
	}

	// Number of Logical(128-byte) Sectors per Physical Sectors
	public int getLSperPS() {
		return this.bytesPerSector / LOGICAL_SECTOR_SIZE;
	}

	// SPT- Number of Logical(128-byte) Sectors per Logical Track
	public int getSPT() {
		return (this.sectorsPerTrack * this.heads) * getLSperPS();
	}

	// BSH - Block Shift - block size is given by LOGICAL_SECTOR_SIZE * (2**BSH)
	public int getBSH() {
		int targetValue = this.blockSize / LOGICAL_SECTOR_SIZE;
		int value = 1;
		int bsh = 0;
		for (int i = 0; value <= targetValue; i++) {
			value *= 2;
			bsh = i;
		}
		return bsh;
	}
	
	// BLM - Block Mask - Block size = 128 * (BLM-1)
	public int getBLM() {
		return (blockSize / LOGICAL_SECTOR_SIZE) - 1;
	}
	
	//EXM - Extent Mask
	public int getEXM(){
		int sizefactor = (getDSM() < 256)? 1024:2048;
		return (blockSize/sizefactor) -1;
	}

	//DSM - Highest Block Number
	public int getDSM(){
		int totalPhysicalSectorsOnDisk = this.getTotalSectorsOnDisk();
		int totalPhysicalSectorsOnOFS = getOFS() * this.heads * this.sectorsPerTrack;
		return ((totalPhysicalSectorsOnDisk-totalPhysicalSectorsOnOFS)/sectorsPerBlock)-1;
	}
	
	//DRM - Highest Disk Entry Position
	public int getDRM(){
		int drm = (this.blockSize * this.getDirectoryBlockCount()) / DIRECTORY_ENTRY_SIZE;
		return drm -1;
	}
	
	// AL0 & AL1 -(AL01) Directory Allocation bits 
	public int getAL01(){
		
		int al = 0xFF0000 >>this.getDirectoryBlockCount();
		return al & 0xFFFF;
	}
	
	// OFS - Cylinder offset
	public int getOFS() {
		if (!bootDisk) {
			return 0;
		}
		float floatSize = (SYSTEM_LOGICAL_BLOCKS + 1) / (float) getSPT();
		return (int) Math.ceil(floatSize);
	}

	//CKS - Check Area Size
	public int getCKS(){
		return (getDRM() +1)/DIRECTORY_ENTRYS_PER_SECTOR;
	}

	// Constants
	private static final int LOGICAL_SECTOR_SIZE = 128;
	private static final int SYSTEM_SIZE = 0X2000;
	private static final int SYSTEM_LOGICAL_BLOCKS = SYSTEM_SIZE / LOGICAL_SECTOR_SIZE;
	private static final int DIRECTORY_ENTRY_SIZE = 32;
	private static final int DIRECTORY_ENTRYS_PER_SECTOR = LOGICAL_SECTOR_SIZE/DIRECTORY_ENTRY_SIZE;
	

}// enum DIskLayout
