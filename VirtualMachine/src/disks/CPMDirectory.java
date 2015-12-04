package disks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class CPMDirectory {

	private HashMap<Integer, CPMDirectoryEntry> dirEntries;
	private boolean bigDisk;
	private int maxEntries;
	private int directoryBlockCount;
	private int sectorsPerBlock;
	private int bytesPerSector;
	private int trackOffset; // ofs
	public int sectorsOffset;
	private int maxBlocks;

	private int directoryEntryNumber;

	private HashMap<Integer, Boolean> allocationTable;

	int entriesPerSector;
	int entriesPerBlock;

	public CPMDirectory() {
		this("F5DD", true); // Default to 3.5" and 5" disks
	}

//	public CPMDirectory(boolean bigDisk, int maxEntries, int sectorsPerBlock, int bytesPerSector, int trackOffset,
//			int maxBlocks) {
//		this.bigDisk = bigDisk;
//		this.maxEntries = maxEntries;
//		this.sectorsPerBlock = sectorsPerBlock;
//		this.bytesPerSector = bytesPerSector;
//		this.trackOffset = trackOffset;
//		this.maxBlocks = maxBlocks;
//		entriesPerSector = bytesPerSector / Disk.DIRECTORY_ENTRY_SIZE;
//		entriesPerBlock = sectorsPerBlock * entriesPerSector;
//		dirEntries = new HashMap<Integer, CPMDirectoryEntry>();
//		allocationTable = new HashMap<Integer, Boolean>();
//
//		for (int i = 0; i < maxEntries; i++) {
//			dirEntries.put(i, CPMDirectoryEntry.emptyDirectoryEntry());
//		}
//	}

	public CPMDirectory(String diskExtention, boolean bootDisk) {
		for (DiskLayout diskLayout : DiskLayout.values()) {
			if (diskLayout.fileExtension.equals(diskExtention)) {
				diskLayout.setBootDisk(bootDisk);
				this.bigDisk = diskLayout.isBigDisk();
				this.maxEntries = diskLayout.getDRM() + 1;
				this.sectorsPerBlock = diskLayout.sectorsPerBlock;
				this.bytesPerSector = diskLayout.bytesPerSector;
				this.trackOffset = diskLayout.getOFS();
				this.maxBlocks = diskLayout.getDSM()+1;
				this.directoryBlockCount = diskLayout.directoryBlockCount;
				this.sectorsOffset =diskLayout.getDirectoryStartSector();
				break;
			}

		}
		entriesPerSector = bytesPerSector / Disk.DIRECTORY_ENTRY_SIZE;
		entriesPerBlock = sectorsPerBlock * entriesPerSector;
		dirEntries = new HashMap<Integer, CPMDirectoryEntry>();
		initializeAllocationTable();

		for (int i = 0; i < maxEntries; i++) {
			dirEntries.put(i, CPMDirectoryEntry.emptyDirectoryEntry());
		}
	}

	public int getDirectoryBlockNumber(int directoryEntryNumber) {
		return directoryEntryNumber / entriesPerBlock;
	}

	public int getPhysicalSectorNumber(int directoryEntryNumber) {
		return (directoryEntryNumber / entriesPerSector) + this.sectorsOffset;
	}

	// where in the physical record this belongs
	public int getLogicalRecordIndex(int directoryEntryNumber) {
		return directoryEntryNumber % entriesPerSector;
	}

	public void addEntry(byte[] rawEntry, int directoryEntryNumber) {
		dirEntries.put(directoryEntryNumber, new CPMDirectoryEntry(rawEntry, bigDisk));
		allocateBlocks(directoryEntryNumber);
	}

	public int addEntry(byte[] rawEntry) {
		int entryLocation = getNextAvailableEntry();
		addEntry(rawEntry, entryLocation);
		return entryLocation;
	}

	public void removeEntry(int directoryEntryNumber) {
		System.out.printf("removeEntry - directoryEntryNumber = %d",directoryEntryNumber);

		ArrayList<Integer> blockList = getFilesBlocks(directoryEntryNumber);
		System.out.printf("removeEntry - blockList.size() = %d",blockList.size());

		for (Integer block : blockList) {
			System.out.printf("removeEntry - block = %d",block);
			allocationTable.remove(block);
		}//
		dirEntries.get(directoryEntryNumber).markAsDeleted();
	}

	public void deleteFile(String fullName) {
		String[] parts = fullName.split(Disk.PERIOD);
		String name = padEntryField(parts[0].trim().toUpperCase(), Disk.NAME_MAX);
		String type = padEntryField(parts[0].trim().toUpperCase(), Disk.TYPE_MAX);
		String target = name + type;
		ArrayList<Integer> targetDirectories = getDirectoryEntries(target);
		for (Integer de : targetDirectories) {
			removeEntry(de);
		}// for
	}

	public ArrayList<Integer> getFilesBlocks(int directoryEntryNumber) {
		return dirEntries.get(directoryEntryNumber).getAllocatedBlocks();
	}

	public void allocateBlocks(int directoryEntryNumber) {
		ArrayList<Integer> blockList = getFilesBlocks(directoryEntryNumber);
		for (Integer block : blockList) {
			allocationTable.put(block, true);
		}//
		return;
	}

	public void allocateBlocksX(int directoryEntryNumber) {

	}

	public void deAllocateBlocks(int directoryEntryNumber) {
		ArrayList<Integer> blockList = getFilesBlocks(directoryEntryNumber);
		for (Integer block : blockList) {
			allocationTable.remove(block);
		}//
		return;
	}

	public int getActiveEntryCount() {
		Collection<CPMDirectoryEntry> entries = dirEntries.values();
		return (int) entries.stream().filter(entry -> entry.isEmpty() != true).count();
	}

	public int getAvailableEntryCount() {
		Collection<CPMDirectoryEntry> entries = dirEntries.values();
		return (int) entries.stream().filter(entry -> entry.isEmpty() == true).count();
	}

	private int getNextAvailableEntry() {
		int ans = -1;
		for (int i = 0; i < dirEntries.size(); i++) {
			if (dirEntries.get(i).isEmpty()) {
				ans = i;
				break;
			}// if
		}// for
		return ans;
	}

	public int getAllocatedBlockCount() {
		return allocationTable.size();
	}

	public int getAvailableBlockCount() {
		return maxBlocks - getAllocatedBlockCount();
	}

	private ArrayList<Integer> getDirectoryEntries(String target) {
		HashMap<Integer, Integer> targetEntries = new HashMap<Integer, Integer>();

		for (int i = 0; i < maxEntries; i++) {
			if (dirEntries.get(i).getNameAndType11().equals(target)) {
				((List<Integer>) targetEntries).add(dirEntries.get(i).getActualExtentNumber(), i);
			}// if
		}// for

		// we have all entries that match the name & type with the directory index & extent number
		ArrayList<Integer> ans = new ArrayList<Integer>();
		Set<Integer> extentNumbers = targetEntries.keySet();
		for (Integer extentNuber : extentNumbers) {
			ans.add(targetEntries.get(extentNuber)); // get the indexes in order
		}
		return ans;
	}

	private String padEntryField(String field, int fieldLength) {
		String result = field.trim().toUpperCase();
		result = result.length() > fieldLength ? result.substring(0, fieldLength) : result;
		result = String.format("%-" + fieldLength + "s", result);
		return result;
	}

	private void initializeAllocationTable() {
		if (allocationTable != null) {
			allocationTable = null;
		}
		
		allocationTable = new HashMap<Integer, Boolean>();
		for (int i = 0; i < this.directoryBlockCount; i++) {
			allocationTable.put(i, true);
		}

	}

	public void refreshAllocationTable() {
		initializeAllocationTable();
		for (int i = 0; i < maxEntries; i++) {
			if (!dirEntries.get(i).isEmpty()) {
				allocateBlocks(i);
			}

		}

	}

}
