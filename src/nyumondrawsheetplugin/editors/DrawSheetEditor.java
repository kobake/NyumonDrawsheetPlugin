package nyumondrawsheetplugin.editors;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;


import nyumondrawsheetplugin.util.DrawSheetColorTable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.ide.IDE;

/**
 * An example showing how to create a multi-page editor.
 * This example has 3 pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class DrawSheetEditor extends MultiPageEditorPart implements IResourceChangeListener{
	ArrayList<int[]> lineList = new ArrayList<int[]>();
	private IDocument doc;
	int m_colorNumber;
	static RGB[] rgbs = DrawSheetColorTable.getRGBs();
	
	private static final String OWNER_TITLE = "OWNER:";
	private static final String OWNER_PROPERTY = "OWNER";
	private static final String DEFAULT_OWNER = "不明な作者";
	private Label m_ownerLabel;

	/** The text editor used in page 0. */
	private TextEditor editor;

	/** The font chosen in page 1. */
	private Canvas canvas;
	private int currentx, currenty;
	static int DRAWING;
	static GC gc;
	
	//private Font font;

	/** The text widget used in page 2. */
	//private StyledText text;
	/**
	 * Creates a multi-page editor example.
	 */
	public DrawSheetEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	
	public void restoreLabel(){
		try{
			IFile iFile = ((FileEditorInput)getEditorInput()).getFile();
			String owner = iFile.getPersistentProperty(new QualifiedName("", OWNER_PROPERTY));
			if(owner == null || owner.length() <= 0){
				owner = DEFAULT_OWNER;
			}
			m_ownerLabel.setText(OWNER_TITLE + owner);
		}
		catch(CoreException ex){
			m_ownerLabel.setText("ほげエラー");
		}
	}

	/**
	 * Creates page 0 of the multi-page editor,
	 * which contains a text editor.
	 */
	void createPage0() {
		try {
			editor = new TextEditor();
			try{
				// 今編集しているファイル
				IFile iFile = ((FileEditorInput)getEditorInput()).getFile();
				File file = iFile.getLocation().toFile();

				// 読む
				BufferedReader br = new BufferedReader(new FileReader(file));
				while(br.ready()){
					String line = br.readLine();
					String[] dotstrs = line.split(",");
					int[] dots = new int[5];
					for(int i = 0; i < 5; i++){
						dots[i]= Integer.parseInt(dotstrs[i]); 
					}
					lineList.add(dots);
				}
				br.close();
			}
			catch(Exception ex){
			}
			
			int index = addPage(editor, getEditorInput());
			setPageText(index, editor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating nested text editor",
				null,
				e.getStatus());
		}
	}
	
	/**
	 * Creates page 1 of the multi-page editor,
	 * which allows you to change the font used in page 2.
	 */
	void createPage1() {

		Composite composite = new Composite(getContainer(), SWT.NONE);
		/*
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		layout.numColumns = 2;
		*/
		RowLayout rl = new RowLayout();
		composite.setLayout(rl);

		/*
		Button fontButton = new Button(composite, SWT.NONE);
		GridData gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 2;
		fontButton.setLayoutData(gd);
		fontButton.setText("Change Font...");
		
		fontButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				setFont();
			}
		});
		*/
		canvas = new Canvas(composite, SWT.NONE);
		canvas.setLayoutData(new RowData(300, 200));
		
		Color color = new Color(Display.getCurrent(), new RGB(225, 225, 255));
		canvas.setBackground(color);
		
		m_ownerLabel = new Label(composite, SWT.WRAP);
		m_ownerLabel.setLayoutData(new RowData(200, 60));
		restoreLabel();
		
		gc = new GC(canvas);
		
		DRAWING = 0;
		m_colorNumber = 0;
		
		// リスナ設定
		canvas.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseDown(MouseEvent e) {
				if(DRAWING == 0){
					DRAWING = 1;
					currentx = e.x;
					currenty = e.y;
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if(DRAWING == 1){
					lineToDoc();
					DRAWING = 0;
				}
			}
		});
		
		// リスナ設定：マウス移動
		canvas.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				if(DRAWING == 1){
					// 座標
					int lastx = currentx;
					int lasty = currenty;
					currentx = e.x;
					currenty = e.y;
					
					// 色
					Color fcolor = new Color(Display.getCurrent(), rgbs[m_colorNumber]);
					gc.setForeground(fcolor);
					
					// 描画
					gc.drawLine(lastx, lasty, currentx, currenty);
					
					// リスト構造への格納
					lineList.add(new int[]{
						m_colorNumber, lastx, lasty, currentx, currenty
					});
				}
			}
		});
		
		// リスナ設定：フォーカス
		canvas.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				restoreCanvas();
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		});

		int index = addPage(composite);
		setPageText(index, "きゃんばす");
	}
		
	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		createPage0();
		createPage1();
		setActivePage(1); // 起動時に開くページの設定
		restoreCanvas();
	}
	
	/**
	 * The <code>MultiPageEditorPart</code> implementation of this 
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}
	/**
	 * Saves the multi-page editor's document as another file.
	 * Also updates the text for page 0's tab, and updates this multi-page editor's input
	 * to correspond to the nested editor's.
	 */
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}
	/* (non-Javadoc)
	 * Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}
	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
		throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
	}
	/* (non-Javadoc)
	 * Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	private void lineToDoc(){
		doc = editor.getDocumentProvider().getDocument(getEditorInput());
		String textstr = new String();
		Iterator<int[]> it = lineList.iterator();
		while(it.hasNext()){
			int[] dots = it.next();
			for(int i = 0; i < 4; i++){
				textstr += Integer.toString(dots[i]) + ","; 
			}
			textstr += dots[4] + "\n";
		}
		doc.set(textstr);
	}
	public void restoreCanvas(){
		canvas.update();
		gc = new GC(canvas);
		
		Iterator<int[]> it = lineList.iterator();
		while(it.hasNext()){
			int[] dots = it.next();
			
			// 最初の要素は色データ
			Color fcolor = new Color(Display.getCurrent(), rgbs[dots[0]]);
			gc.setForeground(fcolor);
			
			// 次からの要素が座標データ
			gc.drawLine(dots[1], dots[2], dots[3], dots[4]);
		}
		restoreLabel();
	}
	public void clearCanvas(){
		canvas.redraw();
	}
	public void clearLineList(){
		lineList = new ArrayList<int[]>();
	}
	
	/**
	 * Calculates the contents of page 2 when the it is activated.
	 */
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 0) {
			lineToDoc();
		}
		else if(newPageIndex == 1){
		}
	}
	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event){
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE){
			Display.getDefault().asyncExec(new Runnable(){
				public void run(){
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i<pages.length; i++){
						if(((FileEditorInput)editor.getEditorInput()).getFile().getProject().equals(event.getResource())){
							IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
							pages[i].closeEditor(editorPart,true);
						}
					}
				}            
			});
		}
	}
	
	public GC getGC(){
		return gc;
	}
	public void setColorNumber(int number){
		m_colorNumber = number;
	}
}
