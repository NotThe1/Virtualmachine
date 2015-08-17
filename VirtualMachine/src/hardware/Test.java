package hardware;


import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;

import memory.Core;
import memory.MemoryAccessErrorEvent;
import memory.MemoryAccessErrorListener;
import memory.MemoryTrapEvent;
import memory.MemoryTrapListener;
//import disks.DiskControlUnit;

public class Test implements MemoryTrapListener, MemoryAccessErrorListener{

	public static void main(String[] args) {
		new Test().doIt();
	}
	private void doIt(){
		Core core;
		int memSize = 128;
		int protectedMem = 0;
		core = new Core(memSize, protectedMem);

		core.addMemoryAccessErrorListener(this);

		core.addMemoryTrapListener(this);

		int loc = memSize - 3;
		core.addTrapLocation(loc, Core.TRAP.IO);

		
//		DiskControlUnit dcu = new DiskControlUnit(core);
		String fileLocation = ".";
		Path sourcePath = Paths.get(fileLocation, "Settings", "test.dat");
		core.removeMemoryAccessErrorListener(this);
		core.removeMemoryTrapListener(this);
		core.removeTrapLocation(loc, Core.TRAP.IO);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(sourcePath));
			oos.writeObject(core);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}



	}// main

	@Override
	public void memoryAccessError(MemoryAccessErrorEvent me) {
		System.err.printf("%n%nFatal memory error%n%n");
		System.err.printf(String.format("Location: %s%n",
				me.getLocation()));
		System.err.printf(String.format("%s%n", me.getMessage()));
		
	}

	@Override
	public void memoryTrap(MemoryTrapEvent mte) {
		System.out.printf(String.format("Memory Trapped%n"));
		System.out.printf(String.format("Location: %04X%n",
				mte.getLocation()));
		System.out.printf(String.format("Type: %s%n%n", mte.getTrap()
				.toString()));

		JOptionPane.showConfirmDialog(null, "choose one", "choose one",
				JOptionPane.YES_NO_OPTION);
		
	}

}// class Test
