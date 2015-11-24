package disks;

public enum DiskLayoutA {
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

	DiskLayoutA(int heads, int tracksPerHead, int sectorsPerTrack, int bytesPerSector,
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

	private int blockSize;
	private int sectorsPerBlock;
	public void setSectorsPerBlock(int sectorsPerBlock){
		this.sectorsPerBlock=  sectorsPerBlock <0?1:sectorsPerBlock;
		this.blockSize = this.sectorsPerBlock * this.bytesPerSector;
	}

	// Number of Logical(128-byte) Sectors per Physical Sectors
	public int getLSperPS() {
		return this.bytesPerSector /LOGICAL_SECTOR_SIZE; 
	}

	// Number of Logical(128-byte) Sectors per Logical Track
	public int getSPT() {
		return (this.sectorsPerTrack * this.heads) * getLSperPS();
	}
	// Block Shift - block size is given by LOGICAL_SECTOR_SIZE * (2**BSH)
	public int getBSH(){
		int targetValue = this.blockSize/LOGICAL_SECTOR_SIZE;
		int value = 1;
		int bsh = 0;
		for( int i = 0; value <= targetValue;i++){
			value *= 2;
			bsh = i;
		}
		return bsh;
	}
	// Block Mask - Block size = 128 * (BLM-1)
	public int getBLM(){
		return (blockSize/LOGICAL_SECTOR_SIZE) -1;
	}
// Member methods
	
	//Constants
	private static final int LOGICAL_SECTOR_SIZE = 128;

}// enum DIskLayout
