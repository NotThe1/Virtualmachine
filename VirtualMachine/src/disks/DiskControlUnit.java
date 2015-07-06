package disks;

import hardware.Core;
import hardware.MemoryEvent;
import hardware.MemoryTrapEvent;
import hardware.MemoryTrapListener;

import javax.swing.JOptionPane;

public class DiskControlUnit implements MemoryTrapListener {

	private int maxNumberOfDrives;
	private Core core;
	private DiskDrive[] drives;
	private int currentDrive;


	public DiskControlUnit(Core core, int maxNumberOfDrives) {
		this.core = core;
		core.addMemoryTrapListener(this);
		this.maxNumberOfDrives = maxNumberOfDrives;
		drives = new DiskDrive[maxNumberOfDrives];
	}// Constructor

	public DiskControlUnit(Core core) {
		this(core, 4);
	}// Constructor

	public void addDiskDrive(int index, String fileName) {
		if (drives[index] != null) {
			JOptionPane.showMessageDialog(null, "Already Mounted", "addDiskDrive",
					JOptionPane.WARNING_MESSAGE);
			return;
		}// if
		drives[index] = new DiskDrive(fileName);
		return;
	}// addDiskDrive

	public int getMaxNumberOfDrives() {
		return maxNumberOfDrives;
	}// getMaxNumberOfDrives

	public int getCurrentDrive() {
		return currentDrive;
	}//getCurrentDrive

	public void setCurrentDrive(int currentDrive) {
		if ((currentDrive >= 0) & (currentDrive < maxNumberOfDrives)) {
			this.currentDrive = currentDrive;
		}//if
	}//setCurrentDrive

	public DiskDrive[] getDrives() {
		return drives;
	}// getDrives

	@Override
	public void memoryTrap(MemoryTrapEvent mte) {
		int DiskControlTable = mte.getLocation();		//
		
System.out.printf("DCU: Message = %s%n",mte.getMessage());
System.out.printf("DCU: DiskControlTable: %04X%n",mte.getLocation());
System.out.printf("DCU: DCT value = %02X",core.read(DiskControlTable));
	}

}// class DiskControlUnit
