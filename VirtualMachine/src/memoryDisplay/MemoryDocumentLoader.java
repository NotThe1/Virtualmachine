package memoryDisplay;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import memory.Core;

public class MemoryDocumentLoader implements Runnable {

	private Core core;
	private Document doc;
	private int lineStart; // address / SIXTEEN
	private int numberOfLines;

	private final static int SIXTEEN = 16;
	private final static int LINE_LENGTH = 72;
	
	private StringBuilder lineBuilder;

	public MemoryDocumentLoader(Core core, Document doc, int lineStart, int numberOfLines) {
		this.core = core;
		this.doc = doc;
		this.lineStart = lineStart;
		this.numberOfLines = numberOfLines;
	}// Constructor

	@Override
	public void run() {
		lineBuilder = new StringBuilder();
		int currentAddress, currentDocumentIndex;
		for (int currentLine = lineStart; currentLine <= lineStart + numberOfLines; currentLine++) {
			currentAddress = currentLine * SIXTEEN;
			currentDocumentIndex = currentLine * LINE_LENGTH;

			try {
				doc.insertString(currentDocumentIndex, getDisplayForLine(currentAddress), null);
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}// try
		}// for
	}// run

	private String getDisplayForLine(int firstLocation) {
		int startLocation = firstLocation & 0XFFF0;
		lineBuilder.setLength(0);

		char[] printables = new char[SIXTEEN];		
		for (int i = 0; i < SIXTEEN; i++) {
			printables[i] = ((core.read(startLocation + i) >= 0X20) && core.read(startLocation + i) <= 0X7F) ?
					(char) core.read(startLocation + i) : '.';
		}//
		lineBuilder.append(String.format(
				"%04X: %02X %02X %02X %02X %02X %02X %02X %02X  %02X %02X %02X %02X %02X %02X %02X %02X ",
				startLocation,
				core.read(startLocation++), core.read(startLocation++), core.read(startLocation++),
				core.read(startLocation++), core.read(startLocation++), core.read(startLocation++),
				core.read(startLocation++), core.read(startLocation++), core.read(startLocation++),
				core.read(startLocation++), core.read(startLocation++), core.read(startLocation++),
				core.read(startLocation++), core.read(startLocation++), core.read(startLocation++),
				core.read(startLocation++)
				));

		lineBuilder.append(printables);
		lineBuilder.append("\n");
		return lineBuilder.toString();
	}// getDisplayForLine

}// class MemoryDocumentLoader
