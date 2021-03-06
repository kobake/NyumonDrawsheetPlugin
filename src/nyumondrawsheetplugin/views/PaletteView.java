package nyumondrawsheetplugin.views;


import java.net.URL;

import nyumondrawsheetplugin.Activator;
import nyumondrawsheetplugin.editors.DrawSheetEditor;
import nyumondrawsheetplugin.util.DrawSheetColorTable;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
//import org.eclipse.core.internal.runtime.Activator;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class PaletteView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "nyumondrawsheetplugin.views.PaletteView";

	private TableViewer viewer;
	private Action selectAction;
	/*
	private Action action1;
	private Action action2;
	*/
	private Action doubleClickAction;
	
	/*
	static final RGB[] rgbs = {
		new RGB(0, 0, 0),
		new RGB(0, 0, 255),
		new RGB(255, 0, 0)
	};
	*/
	static final String[] colorNames = DrawSheetColorTable.getColorNames();

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return PaletteView.colorNames;
			//return new String[] { "One", "Two", "Three" };
		}
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			Activator activator = Activator.getDefault();
			String filename = "icons/" + getText(obj).toLowerCase() + ".gif";
			@SuppressWarnings("deprecation")
			URL url = activator.find(new Path(filename));
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
			Image img = descriptor.createImage();
			return img;
			// return PlatformUI.getWorkbench().
			// 		getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public PaletteView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "NyumonDrawsheetPlugin.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				PaletteView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(selectAction);
		// manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(selectAction);
		// Other plug-ins can contribute there actions here
		// manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(selectAction);
	}

	private void makeActions() {
		selectAction = new Action() {
			public void run() {
				//showMessage("Action 1 executed");
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				for(int i = 0; i < colorNames.length; i++){
					if(colorNames[i].equals(obj.toString())){
						selectColor(i);
					}
				}
			}
		};
		selectAction.setText("せれくと");
		selectAction.setToolTipText("色のせんたく");
		//selectAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
		//	getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		Activator activator = Activator.getDefault();
		String filename = "icons/palette.gif";
		@SuppressWarnings("deprecation")
		URL url = activator.find(new Path(filename));
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		selectAction.setImageDescriptor(descriptor);
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				//showMessage("Double-click detected on "+obj.toString());
				for(int i = 0; i < colorNames.length; i++){
					if(colorNames[i].equals(obj.toString())){
						selectColor(i);
					}
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	/*
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"ぱれっとビューだよ",
			message);
	}
	*/
	
	private void selectColor(int index){
		//Color color = new Color(Display.getCurrent(), rgbs[index]);
		DrawSheetEditor currentEditor = (DrawSheetEditor)getSite().getPage().getActiveEditor();
		//GC gc = currentEditor.getGC();
		//gc.setForeground(color);
		currentEditor.setColorNumber(index);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}