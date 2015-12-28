package codeSupport;

import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.ListModel;

class CodeFragmentModel extends AbstractListModel<CodeFragment> implements ListModel<CodeFragment>, Serializable {
	static final long serialVersionUID = 1L;
	ArrayList<CodeFragment> codeFragements;

	public CodeFragmentModel() {
		codeFragements = new ArrayList<CodeFragment>();
		codeFragements.add(new CodeFragment(0X0000, 0X000, CodeFragment.RESERVED));
		codeFragements.add(new CodeFragment(0XFFFF, 0XFFFF, CodeFragment.RESERVED));
	}// Constructor

	public ArrayList<CodeFragment> getAllFragments() {
		return codeFragements;
	}

	@Override
	public CodeFragment getElementAt(int index) {
		return codeFragements.get(index);
	}// getElementAt

	public boolean addItem(CodeFragment codeFragment) {
		boolean result = true;
		int startLocNew = codeFragment.startLoc;
		int endLocNew = codeFragment.endLoc;
		String typeNew = codeFragment.type;

		int containerIndex = this.withinFragement(startLocNew);

		if (containerIndex != -1) {// there is a container
			CodeFragment cfContainer = this.getElementAt(containerIndex);
			CodeFragment cfToAdd;
			int containerStartLoc = cfContainer.startLoc;
			int containerEndLoc = cfContainer.endLoc;
			String containerType = cfContainer.type;
			if (containerIndex < this.getSize()) {
				this.removeItem(containerIndex);
			}// if last element

			if ((startLocNew != containerStartLoc) & (endLocNew != containerEndLoc)) {
				cfToAdd = new CodeFragment(containerStartLoc, startLocNew - 1, containerType);
				codeFragements.add(insertAt(containerStartLoc), cfToAdd);
				cfToAdd = new CodeFragment(startLocNew, endLocNew, typeNew);
				codeFragements.add(insertAt(startLocNew), cfToAdd);
				cfToAdd = new CodeFragment(endLocNew + 1, containerEndLoc, containerType);
				codeFragements.add(insertAt(endLocNew + 1), cfToAdd);
			} else if (startLocNew == containerStartLoc) {
				cfToAdd = new CodeFragment(startLocNew, endLocNew, typeNew);
				codeFragements.add(insertAt(startLocNew), cfToAdd);
				if (endLocNew != containerEndLoc) {
					cfToAdd = new CodeFragment(endLocNew + 1, containerEndLoc, containerType);
					codeFragements.add(insertAt(endLocNew + 1), cfToAdd);
				}// inner if
			} else if (endLocNew == containerEndLoc) {
				cfToAdd = new CodeFragment(containerStartLoc, startLocNew - 1, containerType);
				codeFragements.add(insertAt(containerStartLoc), cfToAdd);
				cfToAdd = new CodeFragment(startLocNew, containerEndLoc, typeNew);
				codeFragements.add(insertAt(startLocNew), cfToAdd);
			} else {
				result = false;
			}// if else

		} else { // NO container
			codeFragements.add(insertAt(startLocNew), codeFragment);
		}// if - container ?
		// listCodeFragments.updateUI();
		return result;
	}// addItem

	public void removeItem(int index) {
		codeFragements.remove(index);
		// listCodeFragments.updateUI();
		return;
	}// removeItem

	private int insertAt(int location) {
		int loc = codeFragements.size();
		for (int i = 0; i < codeFragements.size(); i++) {
			if (location < codeFragements.get(i).startLoc) {
				loc = i;
				break;
			}// if
		}// for
		return loc;
	}// insertAt

	public void clear() {
		codeFragements.clear();
	}// clear

	/**
	 * 
	 * @param location
	 * @return fragment that contains location , or -1 for no container
	 */
	private int withinFragement(int location) {
		int lowerIndex = -1;

		for (int i = 0; i < codeFragements.size(); i++) {
			if (location <= codeFragements.get(i).startLoc) {
				lowerIndex = (i == 0) ? 0 : i - 1;
				break;
			}// if
		}// for - find lower boundary

		if (lowerIndex == -1) {
			return -1; // not within any fragment
		}//
		int loLoc;
		int hiLoc = codeFragements.get(lowerIndex).startLoc;

		for (int i = lowerIndex + 1; i < codeFragements.size(); i++) {// lowerIndex + 1
			loLoc = hiLoc;
			hiLoc = codeFragements.get(i).startLoc;
			if ((location >= loLoc) & (location <= hiLoc)) {
				if (location <= codeFragements.get(i - 1).endLoc) {
					return i - 1;
				}// if
			} else if (location > hiLoc) {
				break; // too far
			}// if between lo & hi
		}// for
		return -1;
	}// withinFragement

	@Override
	public int getSize() {
		return codeFragements.size();
	} // getSize

}// class CodeFragmentModel