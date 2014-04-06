package nyumondrawsheetplugin.util;

import org.eclipse.swt.graphics.RGB;

public class DrawSheetColorTable {
	public static RGB[] getRGBs(){
		return new RGB[]{
			new RGB(0, 0, 0),
			new RGB(0, 0, 255),
			new RGB(255, 0, 0)
		};
	}
	public static String[] getColorNames(){
		return new String[]{
			"Black", "Blue", "Red"
		};
	}

}
