package disks;

public enum DiskLayout {

	// F3DD(2, 80, 9, 512, "F3DD", "3.5\"  DD   720 KB"),
	// F3HD(2, 80, 18, 512, "F3HD", "3.5\"  HD   1.44 MB"),
	// F3ED(2, 80, 36, 512, "F3ED", "3.5\"  ED   2.88 MB"),
	F5DD(2, 40, 9, 512, 4, 2, true, "F5DD", "5.25\" DD   360 KB"),
	F5HD(2, 80, 15, 512, 4, 2, true, "F5HD", "5.25\" HD   1.2 MB"),
	F8SS(1, 77, 26, 128, 8, 2, true, "F8SS", "8\"    SS   256 KB"),
	F8DS(2, 77, 26, 128, 8, 2, true, "F8DS", "8\"    DS   512 KB");

	public final int heads;
	public final int tracksPerHead;
	public final int sectorsPerTrack;
	public final int bytesPerSector;
	public final String fileExtension;
	public final String descriptor;
	public int sectorsPerBlock;
	public int directoryBlockCount;
	public int directoryStartSector;

	DiskLayout(int heads, int tracksPerHead, int sectorsPerTrack, int bytesPerSector,
			int sectorsPerBlock, int directoryBlockCount, boolean bootDisk, String fileExtension, String descriptor) {
		this.heads = heads;
		this.tracksPerHead = tracksPerHead;
		this.sectorsPerTrack = sectorsPerTrack;
		this.bytesPerSector = bytesPerSector;
		this.fileExtension = fileExtension;
		this.descriptor = descriptor;
		setDirectoryBlockCount(directoryBlockCount);
		setSectorsPerBlock(sectorsPerBlock);
		setBootDisk(bootDisk);
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

	public boolean isBigDisk() {
		return this.getDSM() > 255 ? true : false;
	}

	// Block size
	private int blockSizeInBytes;

	public void setSectorsPerBlock(int sectorsPerBlock) {
		this.sectorsPerBlock = sectorsPerBlock <= 0 ? 1 : sectorsPerBlock;
		this.blockSizeInBytes = this.sectorsPerBlock * this.bytesPerSector;
	}

	// Directory Block count
	public void setDirectoryBlockCount(int directoryBlockCount) {
		this.directoryBlockCount = directoryBlockCount <= 0 ? 1 : directoryBlockCount;
	}

	public int getDirectoryBlockCount() {
		return this.directoryBlockCount;
	}

	// Number of Logical(128-byte) Sectors per Physical Sectors
	public int getLSperPS() {
		return this.bytesPerSector / Disk.LOGICAL_SECTOR_SIZE;
	}

	// SPT- Number of Logical(128-byte) Sectors per Logical Track
	public int getSPT() {
		return (this.sectorsPerTrack * this.heads) * getLSperPS();
	}

	// BSH - Block Shift - block size is given by disk.LOGICAL_SECTOR_SIZE * (2**BSH)
	public int getBSH() {
		int targetValue = this.blockSizeInBytes / Disk.LOGICAL_SECTOR_SIZE;
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
		return (blockSizeInBytes / Disk.LOGICAL_SECTOR_SIZE) - 1;
	}

	// EXM - Extent Mask
	public int getEXM() {
		int sizefactor = (getDSM() < 256) ? 1024 : 2048;
		return (blockSizeInBytes / sizefactor) - 1;
	}

	// DSM - Highest Block Number
	public int getDSM() {
		int totalPhysicalSectorsOnDisk = this.getTotalSectorsOnDisk();
		int totalPhysicalSectorsOnOFS = getOFS() * this.heads * this.sectorsPerTrack;
		return ((totalPhysicalSectorsOnDisk - totalPhysicalSectorsOnOFS) / sectorsPerBlock) - 1;
	}

	// DRM - Highest Disk Entry Position
	public int getDRM() {
		int drm = (this.blockSizeInBytes * this.getDirectoryBlockCount()) / Disk.DIRECTORY_ENTRY_SIZE;
		return drm - 1;
	}

	// AL0 & AL1 -(AL01) Directory Allocation bits
	public int getAL01() {

		int al = 0xFF0000 >> this.getDirectoryBlockCount();
		return al & 0xFFFF;
	}

	// OFS - Cylinder offset
	public int getOFS() {
		int ans = 0;
		if (bootDisk) {
			float floatSize = (Disk.SYSTEM_LOGICAL_BLOCKS + 1) / (float) getSPT();
			ans = (int) Math.ceil(floatSize);
		}
		return ans;
	}

	// CKS - Check Area Size
	public int getCKS() {
		return (getDRM() + 1) / Disk.DIRECTORY_ENTRYS_PER_LOGICAL_SECTOR;
	}

	public int getDirectoryStartSector() {
		int ans = 0;
		if (bootDisk) {
			ans = this.getOFS() * this.getSPT();
		}
		return ans;
	}

	public int getMaxDirectoryEntries() {
		return (directoryBlockCount * blockSizeInBytes) / Disk.DIRECTORY_ENTRY_SIZE;
	}

}// enum DIskLayout
