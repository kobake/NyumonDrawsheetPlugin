package nyumondrawsheetplugin.editors;

import java.net.URL;


import nyumondrawsheetplugin.Activator;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * Manages the installation/deinstallation of global actions for multi-page editors.
 * Responsible for the redirection of global actions to the active editor.
 * Multi-page contributor replaces the contributors for the individual editors in the multi-page editor.
 */
public class DrawSheetEditorContributor extends MultiPageEditorActionBarContributor {
	private IEditorPart activeEditorPart;
	private Action clearAction;
	private Action restoreAction;
	/**
	 * Creates a multi-page contributor.
	 */
	public DrawSheetEditorContributor() {
		super();
		createActions();
	}
	/**
	 * Returns the action registed with the given text editor.
	 * @return IAction or null if editor is null.
	 */
	protected IAction getAction(ITextEditor editor, String actionID) {
		return (editor == null ? null : editor.getAction(actionID));
	}
	/* (non-JavaDoc)
	 * Method declared in AbstractMultiPageEditorActionBarContributor.
	 */

	public void setActivePage(IEditorPart part) {
		if (activeEditorPart == part)
			return;

		activeEditorPart = part;

		IActionBars actionBars = getActionBars();
		if (actionBars != null) {

			ITextEditor editor = (part instanceof ITextEditor) ? (ITextEditor) part : null;

			actionBars.setGlobalActionHandler(
				ActionFactory.DELETE.getId(),
				getAction(editor, ITextEditorActionConstants.DELETE));
			actionBars.setGlobalActionHandler(
				ActionFactory.UNDO.getId(),
				getAction(editor, ITextEditorActionConstants.UNDO));
			actionBars.setGlobalActionHandler(
				ActionFactory.REDO.getId(),
				getAction(editor, ITextEditorActionConstants.REDO));
			actionBars.setGlobalActionHandler(
				ActionFactory.CUT.getId(),
				getAction(editor, ITextEditorActionConstants.CUT));
			actionBars.setGlobalActionHandler(
				ActionFactory.COPY.getId(),
				getAction(editor, ITextEditorActionConstants.COPY));
			actionBars.setGlobalActionHandler(
				ActionFactory.PASTE.getId(),
				getAction(editor, ITextEditorActionConstants.PASTE));
			actionBars.setGlobalActionHandler(
				ActionFactory.SELECT_ALL.getId(),
				getAction(editor, ITextEditorActionConstants.SELECT_ALL));
			actionBars.setGlobalActionHandler(
				ActionFactory.FIND.getId(),
				getAction(editor, ITextEditorActionConstants.FIND));
			actionBars.setGlobalActionHandler(
				IDEActionFactory.BOOKMARK.getId(),
				getAction(editor, IDEActionFactory.BOOKMARK.getId()));
			actionBars.updateActionBars();
		}
	}
	private void createActions() {
		clearAction = new Action() {
			public void run() {
				// 編集中のエディタを取得
				DrawSheetEditor currentEditor = 
						(DrawSheetEditor)getPage().getActiveEditor();
				currentEditor.clearLineList();
				currentEditor.clearCanvas();
				//MessageDialog.openInformation(null, "NyumonDrawsheetPlugin", "Sample Action Executed");
			}
		};
		clearAction.setText("全消去しますよ");
		clearAction.setToolTipText("お絵かきをクリアするのです");
		clearAction.setImageDescriptor(getIconImageDescriptor("nofrog.gif"));
		
		restoreAction = new Action(){
			@Override
			public void run() {
				DrawSheetEditor currentEditor = (DrawSheetEditor)getPage().getActiveEditor();
				currentEditor.restoreCanvas();
			}
		};
		restoreAction.setText("強制復元");
		restoreAction.setToolTipText("お絵かきを復元するのです");
		restoreAction.setImageDescriptor(getIconImageDescriptor("frog.gif"));
	}
	public void contributeToMenu(IMenuManager manager) {
		IMenuManager menu = new MenuManager("DrawSheet");
		manager.prependToGroup(IWorkbenchActionConstants.MB_ADDITIONS, menu);
		menu.add(clearAction);
		menu.add(restoreAction);
	}
	public void contributeToToolBar(IToolBarManager manager) {
		manager.add(new Separator());
		manager.add(clearAction);
		manager.add(restoreAction);
	}
	public ImageDescriptor getIconImageDescriptor(String filename){
		Activator activator = Activator.getDefault();
		String filePathName = "icons/" + filename;
		@SuppressWarnings("deprecation")
		URL url = activator.find(new Path(filePathName));
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		return descriptor;
	}
}
