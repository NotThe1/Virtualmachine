package disks;

import java.util.EventListener;

public interface VDiskErrorListener extends EventListener {
	void vdiskError(VDiskErrorEvent vdee);
}//VDiskErrorListener
