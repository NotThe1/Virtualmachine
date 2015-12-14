package disks;

public class DiskMetrics {

	public DiskMetrics() {
		// TODO Auto-generated constructor stub
	}

	private static final Object[] f3DD = new Object[] { 2, 80, 9, 512, 4, 2, true, "F3DD", "3.5\"  DD   720 KB" };
	private static final Object[] f3HD = new Object[] { 2, 80, 18, 512, 4, 2, true, "F3HD", "3.5\"  HD   1.44 MB" };
	private static final Object[] f3ED = new Object[] { 2, 80, 36, 512, 4, 2, true, "F3ED", "3.5\"  ED   2.88 MB" };

	private static final Object[] f5DD = new Object[] { 2, 40, 9, 512, 4, 2, true, "F5DD", "5.25\" DD   360 KB" };
	private static final Object[] f5HD = new Object[] { 2, 80, 15, 512, 4, 2, true, "F5HD", "5.25\" HD   1.2 MB" };
	private static final Object[] f8SS = new Object[] { 1, 77, 26, 128, 8, 2, true, "F8SS", "8\"    SS   256 KB" };
	private static final Object[] f8DS = new Object[] { 2, 77, 26, 128, 8, 2, true, "F8DS", "8\"    DS   512 KB" };

	private static final Object[] allFileTypes = new Object[] { f5DD, f5HD, f8SS, f8DS, f3DD, f3HD, f3ED };

	public int heads;
	public int tracksPerHead;
	public int sectorsPerTrack;
	public int bytesPerSector;
	public String fileExtension;
	public String descriptor;
	public int sectorsPerBlock;
	public int directoryBlockCount;
	public int directoryStartSector;

	private DiskMetrics(int heads, int tracksPerHead, int sectorsPerTrack, int bytesPerSector,
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

	public static String[] getDiskTypes() {
		String[] fileTypes = new String[allFileTypes.length];
		Object[] fileType;
		for (int i = 0; i < allFileTypes.length; i++) {
			fileType = (Object[]) allFileTypes[i];
			fileTypes[i] = (String) fileType[7];
		}
		return fileTypes;
	}

	public static String[] getDiskDescriptionss() {
		String[] fileTypes = new String[allFileTypes.length];
		Object[] fileType;
		for (int i = 0; i < allFileTypes.length; i++) {
			fileType = (Object[]) allFileTypes[i];
			fileTypes[i] = (String) fileType[8];
		}
		return fileTypes;
	}

	public static DiskMetrics diskMetric(String diskType) {
		Object[] setupValues;
		switch (diskType.trim().toUpperCase()) {
		case Disk.TYPE_3DD:
			setupValues = f3DD;
			break;
		case Disk.TYPE_3ED:
			setupValues = f3ED;
			break;
		case Disk.TYPE_3HD:
			setupValues = f3HD;
			break;
		case Disk.TYPE_5DD:
			setupValues = f5DD;
			break;
		case Disk.TYPE_5HD:
			setupValues = f5HD;
			break;
		case Disk.TYPE_8DS:
			setupValues = f8DS;
			break;
		case Disk.TYPE_8SS:
			setupValues = f8SS;
			break;
		default:
			return null;
		}// switch
		int in0 = (int) setupValues[0];
		int in1 = (int) setupValues[1];
		int in2 = (int) setupValues[2];
		int in3 = (int) setupValues[3];
		int in4 = (int) setupValues[4];
		int in5 = (int) setupValues[5];

		return new DiskMetrics((int) setupValues[0], (int) setupValues[1], (int) setupValues[2], (int) setupValues[3],
				(int) setupValues[4], (int) setupValues[5], (boolean) setupValues[6],
				(String) setupValues[7], (String) setupValues[8]);
	}

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
	
	public int getSectorsPerBlock(){
		return this.sectorsPerBlock;
	}

	private void setSectorsPerBlock(int sectorsPerBlock) {
		this.sectorsPerBlock = sectorsPerBlock <= 0 ? 1 : sectorsPerBlock;
		this.blockSizeInBytes = this.sectorsPerBlock * this.bytesPerSector;
	}

	// Directory Block count
	private void setDirectoryBlockCount(int directoryBlockCount) {
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
			ans = (this.sectorsPerTrack * this.heads);
		}
		return ans;
	}

	public int getDirectorysLastSector() {
		return (getDirectoryStartSector() + (directoryBlockCount * this.sectorsPerBlock) - 1);
	}

	public int getMaxDirectoryEntries() {
		return (directoryBlockCount * blockSizeInBytes) / Disk.DIRECTORY_ENTRY_SIZE;
	}
	
	public int getBytesPerBlock(){
		return this.blockSizeInBytes;
	}

}
