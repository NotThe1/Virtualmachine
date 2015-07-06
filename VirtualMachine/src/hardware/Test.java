package hardware;

import javax.swing.JOptionPane;

public class Test {

	public static void main(String[] args) {
		Core core;
		int memSize = 128;
		int protectedMem = 0;
		core = new Core(memSize, protectedMem);

		core.addMemoryAccessErrorListener(new MemoryAccessErrorListener() {
			@Override
			public void memoryAccessError(MemoryAccessErrorEvent me) {
				System.err.printf("%n%nFatal memory error%n%n");
				System.err.printf(String.format("Location: %s%n",
						me.getLocation()));
				System.err.printf(String.format("%s%n", me.getMessage()));
				// System.exit(-1);
			}
		});

		core.addMemoryTrapListener(new MemoryTrapListener() {

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

		});
		byte value = (byte) 0XFF;
		int loc = memSize - 3;
		core.addTrapLocation(loc, Core.TRAP.IO);
		//core.setTrapEnabled(true);
		core.write(loc,value );

	}// main

}// class Test
