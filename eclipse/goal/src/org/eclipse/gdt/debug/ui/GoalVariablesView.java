package org.eclipse.gdt.debug.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.LazyModelPresentation;
import org.eclipse.debug.internal.ui.VariablesViewModelPresentation;
import org.eclipse.debug.internal.ui.viewers.model.VirtualFindAction;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewActionProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputRequestor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ViewerInputService;
import org.eclipse.debug.internal.ui.views.DebugModelPresentationContext;
import org.eclipse.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.debug.internal.ui.views.variables.SelectionDragAdapter;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.dltk.internal.debug.core.model.ScriptStackFrame;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.texteditor.IUpdate;

@SuppressWarnings({ "restriction", "rawtypes", "unchecked" })
// COPIED CLASS FROM DLTK WITH SOME MODIFICATIONS
public abstract class GoalVariablesView extends AbstractDebugView
		implements IDebugContextListener, IDebugExceptionHandler, IModelChangedListener, IViewerUpdateListener {
	public enum GoalVariableType {
		EVALUATION, BELIEFS, GOALS, PERCEPTS, MAILS
	}

	private static final String COL_VAR_NAME = "org.eclipse.debug.ui.VARIALBE_COLUMN_PRESENTATION.COL_VAR_NAME";

	/**
	 * Selection provider wrapping an exchangeable active selection provider. Sends
	 * out a selection changed event when the active selection provider changes.
	 * Forwards all selection changed events of the active selection provider.
	 */
	private static class SelectionProviderWrapper implements ISelectionProvider {
		private final ListenerList fListenerList = new ListenerList(ListenerList.IDENTITY);
		private final ISelectionChangedListener fListener = this::fireSelectionChanged;
		private ISelectionProvider fActiveProvider;

		private SelectionProviderWrapper(final ISelectionProvider provider) {
			setActiveProvider(provider);
		}

		private void setActiveProvider(final ISelectionProvider provider) {
			if (this.fActiveProvider == provider || this == provider) {
				return;
			}
			if (this.fActiveProvider != null) {
				this.fActiveProvider.removeSelectionChangedListener(this.fListener);
			}
			if (provider != null) {
				provider.addSelectionChangedListener(this.fListener);
			}
			this.fActiveProvider = provider;
			fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));
		}

		private void dispose() {
			this.fListenerList.clear();
			setActiveProvider(null);
		}

		private void fireSelectionChanged(final SelectionChangedEvent event) {
			final Object[] listeners = this.fListenerList.getListeners();
			for (final Object listener2 : listeners) {
				final ISelectionChangedListener listener = (ISelectionChangedListener) listener2;
				listener.selectionChanged(event);
			}
		}

		/*
		 * @see org.eclipse.jface.viewers.ISelectionProvider#
		 * addSelectionChangedListener
		 * (org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		@Override
		public void addSelectionChangedListener(final ISelectionChangedListener listener) {
			this.fListenerList.add(listener);
		}

		/*
		 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
		 */
		@Override
		public ISelection getSelection() {
			if (this.fActiveProvider != null) {
				return this.fActiveProvider.getSelection();
			}
			return StructuredSelection.EMPTY;
		}

		/*
		 * @see org.eclipse.jface.viewers.ISelectionProvider#
		 * removeSelectionChangedListener
		 * (org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		@Override
		public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
			this.fListenerList.remove(listener);
		}

		/*
		 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse
		 * .jface.viewers.ISelection)
		 */
		@Override
		public void setSelection(final ISelection selection) {
			if (this.fActiveProvider != null) {
				this.fActiveProvider.setSelection(selection);
			}
		}
	}

	private Composite fParent;

	/**
	 * The model presentation used as the label provider for the tree viewer
	 */
	private VariablesViewModelPresentation fModelPresentation;

	/**
	 * Viewer input service used to translate active debug context to viewer input.
	 */
	private ViewerInputService fInputService;

	private final Map fGlobalActionMap = new HashMap();

	/**
	 * Viewer input requester used to update the viewer once the viewer input has
	 * been resolved.
	 */
	private final IViewerInputRequestor fRequester = update -> {
		if (!update.isCanceled()) {
			viewerInputUpdateComplete(update);
		}
	};

	private String PREF_STATE_MEMENTO = "pref_state_memento.";

	/**
	 * Selection provider registered with the view site.
	 */
	private SelectionProviderWrapper fSelectionProvider;

	/**
	 * Presentation context for this view.
	 */
	private IPresentationContext fPresentationContext;

	/**
	 * @return The variable type to display in this GOAL view
	 */
	protected abstract GoalVariableType getVariableType();

	/**
	 * Have we ever been visible
	 */
	private boolean firstVisible = false;

	/**
	 * Remove myself as a selection listener and preference change listener.
	 *
	 * @see IWorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		DebugUITools.removePartDebugContextListener(getSite(), this);
		final TreeModelViewer viewer = getVariablesViewer();
		if (viewer != null) {
			viewer.removeModelChangedListener(this);
			viewer.removeViewerUpdateListener(this);
		}
		if (this.fPresentationContext != null) {
			this.fPresentationContext.dispose();
			this.fPresentationContext = null;
		}
		this.fInputService.dispose();
		this.fSelectionProvider.dispose();
		super.dispose();
	}

	/**
	 * Called when the viewer input update is completed. Unlike
	 * {@link #setViewerInput(Object)}, it allows overriding classes to examine the
	 * context for which the update was calculated.
	 *
	 * @param update Completed viewer input update.
	 */
	private void viewerInputUpdateComplete(final IViewerInputUpdate update) {
		setViewerInput(update.getInputElement());
		updateAction(FIND_ACTION);
	}

	/**
	 * Sets the input to the viewer
	 *
	 * @param context the object context
	 */
	private void setViewerInput(Object context) {
		final Object current = getViewer().getInput();
		if (current == null && context == null) {
			return;
		} else if (current != null && current.equals(context)) {
			return;
		} else {
			showViewer();
			if (context instanceof ScriptStackFrame) {
				context = new GoalScriptStackFrame(getVariableType(), (ScriptStackFrame) context);
			}
			getViewer().setInput(context);
			getVariablesViewer().setVisibleColumns(new String[] { COL_VAR_NAME });
			updateObjects();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(Composite)
	 */
	@Override
	public Viewer createViewer(final Composite parent) {
		this.fParent = parent;
		getModelPresentation();

		final TreeModelViewer variablesViewer = createTreeViewer(parent);
		this.fInputService = new ViewerInputService(variablesViewer, this.fRequester);

		this.fSelectionProvider = new SelectionProviderWrapper(variablesViewer);
		getSite().setSelectionProvider(this.fSelectionProvider);

		final IMemento memento = getMemento();
		if (memento != null) {
			variablesViewer.initState(memento);
		}

		variablesViewer.addModelChangedListener(this);
		variablesViewer.addViewerUpdateListener(this);

		initDragAndDrop(variablesViewer);

		return variablesViewer;
	}

	/**
	 * Initializes the drag and/or drop adapters for this view. Called from
	 * createViewer().
	 *
	 * @param viewer the viewer to add drag/drop support to.
	 */
	private void initDragAndDrop(final TreeModelViewer viewer) {
		viewer.addDragSupport(DND.DROP_COPY, new Transfer[] { LocalSelectionTransfer.getTransfer() },
				new SelectionDragAdapter(viewer));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite,
	 * org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.PREF_STATE_MEMENTO = this.PREF_STATE_MEMENTO + site.getId();
		final IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		final String string = store.getString(this.PREF_STATE_MEMENTO);
		if (string.length() > 0) {
			final ByteArrayInputStream bin = new ByteArrayInputStream(string.getBytes());
			final InputStreamReader reader = new InputStreamReader(bin);
			try {
				final XMLMemento stateMemento = XMLMemento.createReadRoot(reader);
				setMemento(stateMemento);
			} catch (final WorkbenchException e) {
			} finally {
				try {
					reader.close();
					bin.close();
				} catch (final IOException e) {
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.PageBookView#partDeactivated(org.eclipse.ui.
	 * IWorkbenchPart)
	 */
	@Override
	public void partDeactivated(final IWorkbenchPart part) {
		final String id = part.getSite().getId();
		if (id.equals(getSite().getId())) {
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			final OutputStreamWriter writer = new OutputStreamWriter(bout);
			try {
				final XMLMemento memento = XMLMemento.createWriteRoot("GoalVariablesViewMemento");
				saveViewerState(memento);
				memento.save(writer);

				final IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
				final String xmlString = bout.toString();
				store.putValue(this.PREF_STATE_MEMENTO, xmlString);
			} catch (final IOException e) {
			} finally {
				try {
					writer.close();
					bout.close();
				} catch (final IOException e) {
				}
			}
		}
		super.partDeactivated(part);
	}

	/**
	 * Saves the current state of the viewer
	 *
	 * @param memento the memento to write the viewer state into
	 */
	private void saveViewerState(final IMemento memento) {
		getVariablesViewer().saveState(memento);
	}

	/**
	 * Create and return the main tree viewer that displays variable.
	 *
	 * @param parent Viewer's parent control
	 * @return The created viewer.
	 */
	private TreeModelViewer createTreeViewer(final Composite parent) {
		final int style = getViewerStyle();
		this.fPresentationContext = new DebugModelPresentationContext(getPresentationContextId(), this,
				this.fModelPresentation);
		final TreeModelViewer variablesViewer = new TreeModelViewer(parent, style, this.fPresentationContext);

		variablesViewer.getControl().addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(final FocusEvent e) {
				GoalVariablesView.this.fSelectionProvider.setActiveProvider(variablesViewer);
				setGlobalActions();
			}

			@Override
			public void focusLost(final FocusEvent e) {
				// Do not reset the selection provider with the provider proxy.
				// This should allow toolbar actions to remain active when the
				// view is de-activated but still visible.
				clearGlobalActions();
				getViewSite().getActionBars().updateActionBars();
			}
		});
		DebugUITools.addPartDebugContextListener(getSite(), this);

		return variablesViewer;
	}

	private void setGlobalActions() {
		for (final Object element : this.fGlobalActionMap.entrySet()) {
			final Map.Entry entry = (Map.Entry) element;
			final String actionID = (String) entry.getKey();
			IAction action = getOverrideAction(actionID);
			if (action == null) {
				action = (IAction) entry.getValue();
			}
			setAction(actionID, action);
		}
		getViewSite().getActionBars().updateActionBars();
	}

	/**
	 * Save the global actions from action bar
	 */
	@Override
	protected void createContextMenu(final Control menuControl) {
		super.createContextMenu(menuControl);
		final IActionBars actionBars = getViewSite().getActionBars();
		if (!this.fGlobalActionMap.containsKey(SELECT_ALL_ACTION)) {
			setGlobalAction(IDebugView.SELECT_ALL_ACTION, actionBars.getGlobalActionHandler(SELECT_ALL_ACTION));
		}
		if (!this.fGlobalActionMap.containsKey(COPY_ACTION)) {
			setGlobalAction(COPY_ACTION, actionBars.getGlobalActionHandler(COPY_ACTION));
		}
		if (!this.fGlobalActionMap.containsKey(PASTE_ACTION)) {
			setGlobalAction(PASTE_ACTION, actionBars.getGlobalActionHandler(PASTE_ACTION));
		}
	}

	private void clearGlobalActions() {
		for (final Object element : this.fGlobalActionMap.keySet()) {
			final String id = (String) element;
			setAction(id, null);
		}
		getViewSite().getActionBars().updateActionBars();
	}

	/**
	 * Returns the presentation context id for this view.
	 *
	 * @return context id
	 */
	private String getPresentationContextId() {
		return IDebugUIConstants.ID_VARIABLE_VIEW;
	}

	/**
	 * Returns the style bits for the viewer.
	 *
	 * @return SWT style
	 */
	private int getViewerStyle() {
		return SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL | SWT.FULL_SELECTION;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	@Override
	protected String getHelpContextId() {
		return IDebugHelpContextIds.VARIABLE_VIEW;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	@Override
	protected void createActions() {
		final IAction action = new VirtualFindAction(getVariablesViewer());
		setGlobalAction(FIND_ACTION, action);
	}

	/**
	 * Adds the given action to the set of global actions managed by this variables
	 * view.
	 *
	 * @param actionID Action ID that the given action implements.
	 * @param action   Action implementation.
	 */
	private void setGlobalAction(final String actionID, final IAction action) {
		this.fGlobalActionMap.put(actionID, action);
	}

	@Override
	public IAction getAction(final String actionID) {
		// Check if model overrides the action. Global action overrides are
		// checked in setGlobalActions() so skip them here.
		if (!this.fGlobalActionMap.containsKey(actionID)) {
			final IAction overrideAction = getOverrideAction(actionID);
			if (overrideAction != null) {
				return overrideAction;
			}
		}
		return super.getAction(actionID);
	}

	private IAction getOverrideAction(final String actionID) {
		final Viewer viewer = getViewer();
		if (viewer != null) {
			final IViewActionProvider actionProvider = (IViewActionProvider) DebugPlugin.getAdapter(viewer.getInput(),
					IViewActionProvider.class);
			if (actionProvider != null) {
				final IAction action = actionProvider.getAction(getPresentationContext(), actionID);
				return action;
			}
		}
		return null;
	}

	@Override
	public void updateObjects() {
		super.updateObjects();
		setGlobalActions();
		getViewSite().getActionBars().updateActionBars();
	}

	/**
	 * Configures the toolBar.
	 *
	 * @param tbm The toolbar that will be configured
	 */
	@Override
	protected void configureToolBar(final IToolBarManager tbm) {
		tbm.add(new Separator(getClass().getSimpleName()));
		tbm.add(new Separator(IDebugUIConstants.RENDER_GROUP));
	}

	/**
	 * Adds items to the tree viewer's context menu including any extension defined
	 * actions.
	 *
	 * @param menu The menu to add the item to.
	 */
	@Override
	protected void fillContextMenu(final IMenuManager menu) {
		menu.add(new Separator(IDebugUIConstants.EMPTY_VARIABLE_GROUP));
		menu.add(new Separator(IDebugUIConstants.VARIABLE_GROUP));
		menu.add(getAction(FIND_ACTION));
		menu.add(new Separator());
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.EMPTY_NAVIGATION_GROUP));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * @return the model presentation to be used for this view
	 */
	private IDebugModelPresentation getModelPresentation() {
		if (this.fModelPresentation == null) {
			this.fModelPresentation = new VariablesViewModelPresentation();
		}
		return this.fModelPresentation;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(Class)
	 */
	@Override
	public Object getAdapter(final Class required) {
		if (IDebugModelPresentation.class.equals(required)) {
			return getModelPresentation();
		} else {
			return super.getAdapter(required);
		}
	}

	/**
	 * If possible, calls the update method of the action associated with the given
	 * ID.
	 *
	 * @param actionId the ID of the action to update
	 */
	private void updateAction(final String actionId) {
		IAction action = getAction(actionId);
		if (action == null) {
			action = (IAction) this.fGlobalActionMap.get(actionId);
		} else if (action instanceof IUpdate) {
			((IUpdate) action).update();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.ui.AbstractDebugView#getDefaultControl()
	 */
	@Override
	protected Control getDefaultControl() {
		if (getVariablesViewer() != null) {
			return getVariablesViewer().getControl();
		} else {
			return this.fParent;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.internal.ui.views.IDebugExceptionHandler#
	 * handleException (org.eclipse.debug.core.DebugException)
	 */
	@Override
	public void handleException(final DebugException e) {
		showMessage(e.getMessage());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextListener
	 * #contextEvent
	 * (org.eclipse.debug.internal.ui.contexts.provisional.DebugContextEvent)
	 */
	@Override
	public void debugContextChanged(final DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			contextActivated(event.getContext());
		}
	}

	/**
	 * Updates actions and sets the viewer input when a context is activated.
	 *
	 * @param selection New selection to activate.
	 */
	private void contextActivated(final ISelection selection) {
		if (isAvailable() && selection instanceof IStructuredSelection) {
			final Object source = ((IStructuredSelection) selection).getFirstElement();
			this.fInputService.resolveViewerInput(source);
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(DoubleClickEvent)
	 */
	@Override
	public void doubleClick(final DoubleClickEvent event) {
		final String selected = event.getSelection().toString();
		MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Variable View",
				selected.substring(1, selected.length() - 1));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.ui.IDebugView#getPresentation(String)
	 */
	@Override
	public IDebugModelPresentation getPresentation(final String id) {
		if (getViewer() instanceof StructuredViewer) {
			final IDebugModelPresentation lp = getModelPresentation();
			if (lp instanceof DelegatingModelPresentation) {
				return ((DelegatingModelPresentation) lp).getPresentation(id);
			}
			if (lp instanceof LazyModelPresentation) {
				if (((LazyModelPresentation) lp).getDebugModelIdentifier().equals(id)) {
					return lp;
				}
			}
		}
		return null;
	}

	/**
	 * @return the presentation context of the viewer
	 */
	private IPresentationContext getPresentationContext() {
		return getVariablesViewer().getPresentationContext();
	}

	/**
	 * Returns the active debug context for this view based on the view's site IDs.
	 *
	 * @return Active debug context for this view.
	 */
	private ISelection getDebugContext() {
		final IViewSite site = (IViewSite) getSite();
		final IDebugContextService contextService = DebugUITools.getDebugContextManager()
				.getContextService(site.getWorkbenchWindow());
		return contextService.getActiveContext(site.getId(), site.getSecondaryId());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesVisible()
	 */
	@Override
	protected void becomesVisible() {
		super.becomesVisible();
		if (!this.firstVisible) {
			final ISelection selection = getDebugContext();
			contextActivated(selection);
			this.firstVisible = true;
		}
	}

	/**
	 * @return the tree model viewer displaying variables
	 */
	private TreeModelViewer getVariablesViewer() {
		return (TreeModelViewer) getViewer();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelChangedListener
	 * #modelChanged (org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	@Override
	public void modelChanged(final IModelDelta delta, final IModelProxy proxy) {
		updateAction(FIND_ACTION);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.
	 * IViewerUpdateListener #updateComplete(org.eclipse.debug.internal.ui.viewers
	 * .provisional.IAsynchronousRequestMonitor)
	 */
	@Override
	public void updateComplete(final IViewerUpdate update) {
		final IStatus status = update.getStatus();
		if (!update.isCanceled()) {
			if (status != null && !status.isOK()) {
				showMessage(status.getMessage());
			} else {
				showViewer();
			}
			if (TreePath.EMPTY.equals(update.getElementPath())) {
				updateAction(FIND_ACTION);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.
	 * IViewerUpdateListener #updateStarted(org.eclipse.debug.internal.ui.viewers.
	 * provisional.IAsynchronousRequestMonitor)
	 */
	@Override
	public void updateStarted(final IViewerUpdate update) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.
	 * IViewerUpdateListener#viewerUpdatesBegin()
	 */
	@Override
	public void viewerUpdatesBegin() {
		final IWorkbenchSiteProgressService progressService = getSite().getAdapter(IWorkbenchSiteProgressService.class);
		if (progressService != null) {
			progressService.incrementBusy();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.
	 * IViewerUpdateListener#viewerUpdatesComplete()
	 */
	@Override
	public void viewerUpdatesComplete() {
		final IWorkbenchSiteProgressService progressService = getSite().getAdapter(IWorkbenchSiteProgressService.class);
		if (progressService != null) {
			progressService.decrementBusy();
		}
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (getViewer() != null) {
			getViewer().getControl().setFocus();
		}
	}
}