package org.eclipse.gdt.debug.history;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.internal.debug.core.model.ScriptStackFrame;
import org.eclipse.gdt.debug.dbgp.DebuggerCollection;
import org.eclipse.gdt.debug.dbgp.LocalDebugger;
import org.eclipse.gdt.parser.GoalSourceParser;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import goal.preferences.LoggingPreferences;
import goal.tools.history.EventStorage;
import goal.tools.history.events.AbstractEvent;
import languageTools.program.ProgramMap;
import languageTools.program.agent.AgentId;

public class GoalHistoryView extends ViewPart implements IDebugContextListener {
	public final static String VIEW_ID = "org.eclipse.gdt.HistoryView";
	private static Map<Integer, AbstractEvent> filtered;

	public static void setFilter(final Map<Integer, AbstractEvent> filter) {
		filtered = filter;
	}

	public static void clearFilter() {
		filtered = null;
	}

	private class HistoryState extends Composite {
		private final LocalDebugger debugger;
		private final AgentId agent;
		private final ProgramMap map;
		private final List<String> columns;
		private final EventList<Map<String, String>> rows;
		private final Map<String, Integer> index;
		private NatTable natTable;
		private EventStorage data;

		HistoryState(final Composite parent, final LocalDebugger debugger, final AgentId agent) throws Exception {
			super(parent, SWT.NONE);
			this.debugger = debugger;
			this.agent = agent;
			this.map = (debugger == null) ? null : GoalSourceParser.getMap(debugger.getPath());
			this.columns = new LinkedList<>();
			this.rows = GlazedLists.eventList(new LinkedList<Map<String, String>>());
			this.index = new HashMap<>();
			create();
		}

		private EventStorage getHistory() {
			if (this.data == null && this.debugger != null && LoggingPreferences.getEnableHistory()) {
				try {
					final File file = new File(this.debugger.getHistoryState(this.agent));
					if (file.isFile()) {
						this.data = new EventStorage(file);
					}
				} catch (final Exception ignore) {
				}
			}
			return this.data;
		}

		private void create() {
			setLayout(new GridLayout());
			GridDataFactory.fillDefaults().grab(true, true).applyTo(this);

			final ConfigRegistry configRegistry = new ConfigRegistry();
			final GlazedListsGridLayer<Map<String, String>> gridLayer = new GlazedListsGridLayer<>(this.rows,
					getColumnPropertyAccessor(), getColumnHeaderDataProvider(), getRowHeaderDataProvider(),
					configRegistry);

			this.natTable = new NatTable(this, gridLayer, false);
			this.natTable.setConfigRegistry(configRegistry);
			this.natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
			this.natTable.addConfiguration(new HeaderMenuConfiguration(this.natTable));
			final SelectionLayer selectionLayer = ((DefaultBodyLayerStack) ((GridLayer) this.natTable.getLayer())
					.getBodyLayer()).getSelectionLayer();
			this.natTable.getUiBindingRegistry().registerDoubleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE),
					new IMouseAction() {
						@Override
						public void run(final NatTable natTable, final MouseEvent event) {
							try {
								final int column = LayerUtil.convertColumnPosition(natTable,
										natTable.getColumnPositionByX(event.x), selectionLayer);
								HistoryState.this.debugger.getAgentState(HistoryState.this.agent).deinitialize();
								GoalHistoryView.this.context.resume();
								HistoryState.this.debugger.historyStepTo(HistoryState.this.agent, column);
								GoalHistoryView.this.context.suspend();
							} catch (final Exception e) {
								DLTKCore.error(e);
							}
						}
					});
			this.natTable.addOverlayPainter(getAutoResizeOverlay());
			this.natTable.configure();
			GridDataFactory.fillDefaults().grab(true, true).applyTo(this.natTable);

			update();
		}

		private IOverlayPainter getAutoResizeOverlay() {
			return new IOverlayPainter() {
				private final HashSet<Integer> colset = new HashSet<>();

				@Override
				public void paintOverlay(final GC gc, final ILayer layer) {
					for (int i = 0; i < HistoryState.this.natTable.getColumnCount(); i++) {
						if (HistoryState.this.natTable.isColumnPositionResizable(i) == false) {
							continue;
						}
						final int pos = HistoryState.this.natTable.getColumnIndexByPosition(i);
						if (this.colset.contains(pos)) {
							continue;
						}

						this.colset.add(pos);

						final InitializeAutoResizeColumnsCommand columnCommand = new InitializeAutoResizeColumnsCommand(
								HistoryState.this.natTable, i, HistoryState.this.natTable.getConfigRegistry(),
								new GCFactory(HistoryState.this.natTable));
						HistoryState.this.natTable.doCommand(columnCommand);
					}
				}
			};
		}

		@Override
		public void update() {
			List<AbstractEvent> history = null;
			if (filtered != null) {
				history = new ArrayList<>(filtered.values());
			} else {
				EventStorage storage = getHistory();
				if (storage != null) {
					history = storage.getAll();
				}
			}
			if (history == null) {
				return;
			}
			this.index.clear();
			this.columns.clear();
			this.rows.clear();
			for (int i = 0; i < history.size(); ++i) {
				final String index = Integer.toString(i);
				final AbstractEvent event = history.get(i);
				final List<String> lookup = (event == null) ? new ArrayList<>(0) : event.getLookupData(this.map);
				for (final String signature : lookup) {
					final Integer existing = this.index.get(signature);
					if (existing == null) {
						this.index.put(signature, this.rows.size());
						final Map<String, String> row = new HashMap<>();
						row.put(index, getDescription(event));
						this.rows.add(row); // TODO: sorting on type?! (i.e.
											// action signatures first)
					} else {
						final Map<String, String> row = this.rows.get(existing);
						row.put(index, getDescription(event));
					}
				}
				if (!this.columns.contains(index)) {
					this.columns.add(index);
				}
			}
			this.natTable.refresh();
		}

		private String getDescription(AbstractEvent event) {
			return event.getClass().getSimpleName().substring(0, 1);
		}

		private IColumnPropertyAccessor<Map<String, String>> getColumnPropertyAccessor() {
			return new IColumnPropertyAccessor<Map<String, String>>() {
				@Override
				public Object getDataValue(final Map<String, String> rowObject, final int columnIndex) {
					final Object value = rowObject.get(getColumnProperty(columnIndex));
					return (value == null) ? "" : value;
				}

				@Override
				public void setDataValue(final Map<String, String> rowObject, final int columnIndex,
						final Object newValue) {
					rowObject.put(getColumnProperty(columnIndex), newValue.toString());
				}

				@Override
				public String getColumnProperty(final int columnIndex) {
					return HistoryState.this.columns.get(columnIndex);
				}

				@Override
				public int getColumnIndex(final String propertyName) {
					return HistoryState.this.columns.indexOf(propertyName);
				}

				@Override
				public int getColumnCount() {
					return HistoryState.this.columns.size();
				}
			};
		}

		private IDataProvider getColumnHeaderDataProvider() {
			return new IDataProvider() {
				@Override
				public Object getDataValue(final int columnIndex, final int rowIndex) {
					return HistoryState.this.columns.get(columnIndex);
				}

				@Override
				public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
					throw new UnsupportedOperationException();
				}

				@Override
				public int getRowCount() {
					return 1;
				}

				@Override
				public int getColumnCount() {
					return HistoryState.this.columns.size();
				}
			};
		}

		private IDataProvider getRowHeaderDataProvider() {
			return new IDataProvider() {
				@Override
				public Object getDataValue(final int columnIndex, final int rowIndex) {
					for (final String label : HistoryState.this.index.keySet()) {
						if (HistoryState.this.index.get(label) == rowIndex) {
							return label;
						}
					}
					return "";
				}

				@Override
				public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
					throw new UnsupportedOperationException();
				}

				@Override
				public int getRowCount() {
					return HistoryState.this.rows.size();
				}

				@Override
				public int getColumnCount() {
					return 1;
				}
			};
		}
	}

	private Map<AgentId, HistoryState> states = new LinkedHashMap<>();
	private StackLayout layout;
	private Composite parent;
	private ScriptStackFrame context;

	@Override
	public void createPartControl(final Composite parent) {
		DebugUITools.addPartDebugContextListener(getSite(), this);
		this.layout = new StackLayout();
		parent.setLayout(this.layout);
		this.parent = parent;
	}

	@Override
	public void dispose() {
		DebugUITools.removePartDebugContextListener(getSite(), this);
		clear();
		this.layout = null;
		this.parent = null;
		super.dispose();
	}

	public void clear() {
		this.states.clear();
		this.context = null;
	}

	@Override
	public void setFocus() {
	}

	private HistoryState getHistoryState() {
		if (this.context == null) {
			return getDefaultState();
		} else {
			final IPath sourcePath = new Path(this.context.getSourceURI().getPath());
			final IFile sourceFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(sourcePath);
			final DebuggerCollection debuggerCollection = DebuggerCollection
					.getCollection(sourceFile.getProject().getName());
			final AgentId agent = (debuggerCollection == null) ? null
					: debuggerCollection.getAgentForThread(this.context.getScriptThread());
			HistoryState state = (this.states == null) ? null : this.states.get(agent);
			if (state == null && this.parent != null && !this.parent.isDisposed()) {
				try {
					state = new HistoryState(this.parent, debuggerCollection.getMainDebugger(), agent);
					this.states.put(agent, state);
				} catch (final Exception e) {
					state = getDefaultState();
				}
			}
			return state;
		}
	}

	private HistoryState getDefaultState() {
		HistoryState state = (this.states == null) ? null : this.states.get(null);
		if (state == null && this.parent != null && !this.parent.isDisposed()) {
			try {
				state = new HistoryState(this.parent, null, null);
				this.states.put(null, state);
			} catch (final Exception e) {
				DLTKCore.error(e);
			}
		}
		return state;
	}

	@Override
	public void debugContextChanged(final DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0
				&& event.getContext() instanceof IStructuredSelection) {
			final IStructuredSelection selection = (IStructuredSelection) event.getContext();
			if (selection.getFirstElement() instanceof ScriptStackFrame) {
				this.context = (ScriptStackFrame) selection.getFirstElement();
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						final HistoryState state = getHistoryState();
						if (state != null) {
							state.update();
							GoalHistoryView.this.layout.topControl = state;
							GoalHistoryView.this.parent.layout();
						}
					}
				});
			} else {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						final HistoryState state = getHistoryState();
						if (state != null) {
							GoalHistoryView.this.layout.topControl = null;
							GoalHistoryView.this.parent.layout();
						}
					}
				});
			}
		}
	}

	private static class GlazedListsGridLayer<T> extends GridLayer {
		private final DataLayer bodyDataLayer;
		private final DefaultBodyLayerStack bodyLayerStack;
		private final ListDataProvider<T> bodyDataProvider;
		private final GlazedListsColumnHeaderLayerStack<T> columnHeaderLayerStack;

		public GlazedListsGridLayer(final EventList<T> eventList,
				final IColumnPropertyAccessor<T> columnPropertyAccessor, final IDataProvider columnHeaderDataProvider,
				final IDataProvider rowHeaderDataProvider, final IConfigRegistry configRegistry) {
			super(true);
			// Body - with list event listener (sets width,height too)
			final SortedList<T> sortedList = new SortedList<>(eventList, null);
			this.bodyDataProvider = new ListDataProvider<>(sortedList, columnPropertyAccessor);
			this.bodyDataLayer = new DataLayer(this.bodyDataProvider, 20, 20);
			final GlazedListsEventLayer<T> glazedListsEventLayer = new GlazedListsEventLayer<>(this.bodyDataLayer,
					eventList);
			this.bodyLayerStack = new DefaultBodyLayerStack(glazedListsEventLayer);
			// Column header
			this.columnHeaderLayerStack = new GlazedListsColumnHeaderLayerStack<>(columnHeaderDataProvider, sortedList,
					columnPropertyAccessor, configRegistry, this.bodyLayerStack);
			// Row header
			final DataLayer rowHeaderDataLayer = new DataLayer(rowHeaderDataProvider, 150, 20);
			final RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, this.bodyLayerStack,
					this.bodyLayerStack.getSelectionLayer());
			// Corner
			final DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(
					this.columnHeaderLayerStack.getDataProvider(), rowHeaderDataProvider);
			final DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
			final CornerLayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer,
					this.columnHeaderLayerStack);
			// Grid
			setBodyLayer(this.bodyLayerStack);
			setColumnHeaderLayer(this.columnHeaderLayerStack);
			setRowHeaderLayer(rowHeaderLayer);
			setCornerLayer(cornerLayer);
		}
	}

	private static class GlazedListsColumnHeaderLayerStack<T> extends AbstractLayerTransform {
		private final IDataProvider dataProvider;
		private final DefaultColumnHeaderDataLayer dataLayer;
		private final ColumnHeaderLayer columnHeaderLayer;

		public GlazedListsColumnHeaderLayerStack(final IDataProvider dataProvider, final SortedList<T> sortedList,
				final IColumnPropertyAccessor<T> columnPropertyAccessor, final IConfigRegistry configRegistry,
				final DefaultBodyLayerStack bodyLayerStack) {
			this.dataProvider = dataProvider;
			this.dataLayer = new DefaultColumnHeaderDataLayer(dataProvider);
			this.columnHeaderLayer = new ColumnHeaderLayer(this.dataLayer, bodyLayerStack,
					bodyLayerStack.getSelectionLayer());
			final SortHeaderLayer<T> sortHeaderLayer = new SortHeaderLayer<>(this.columnHeaderLayer,
					new GlazedListsSortModel<>(sortedList, columnPropertyAccessor, configRegistry, this.dataLayer),
					false);
			setUnderlyingLayer(sortHeaderLayer);
		}

		public IDataProvider getDataProvider() {
			return this.dataProvider;
		}
	}
}