/*******************************************************************************
 * Copyright (c) 2002, 2013 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.tablekit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;
import static org.eclipse.rap.rwt.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.testfixture.internal.TestUtil.createImage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.LifeCycle;
import org.eclipse.rap.rwt.internal.protocol.ClientMessageConst;
import org.eclipse.rap.rwt.internal.protocol.JsonUtil;
import org.eclipse.rap.rwt.lifecycle.PhaseEvent;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.lifecycle.PhaseListener;
import org.eclipse.rap.rwt.lifecycle.WidgetAdapter;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.CellToolTipUtil;
import org.eclipse.swt.internal.widgets.ICellToolTipAdapter;
import org.eclipse.swt.internal.widgets.ICellToolTipProvider;
import org.eclipse.swt.internal.widgets.ITableAdapter;
import org.eclipse.swt.internal.widgets.Props;
import org.eclipse.swt.internal.widgets.buttonkit.ButtonOperationHandler;
import org.eclipse.swt.internal.widgets.controlkit.ControlLCATestUtil;
import org.eclipse.swt.internal.widgets.tablekit.TableLCA.ItemMetrics;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;


public class TableLCA_Test {

  private Display display;
  private Shell shell;
  private Table table;
  private TableLCA lca;

  @Before
  public void setUp() {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    table = new Table( shell, SWT.NONE );
    lca = new TableLCA();
    Fixture.fakeNewRequest();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testControlListeners() throws IOException {
    ControlLCATestUtil.testActivateListener( table );
    ControlLCATestUtil.testFocusListener( table );
    ControlLCATestUtil.testMouseListener( table );
    ControlLCATestUtil.testKeyListener( table );
    ControlLCATestUtil.testTraverseListener( table );
    ControlLCATestUtil.testMenuDetectListener( table );
    ControlLCATestUtil.testHelpListener( table );
  }

  @Test
  public void testPreserveValues() {
    Fixture.markInitialized( display );
    Fixture.preserveWidgets();
    WidgetAdapter adapter = WidgetUtil.getAdapter( table );
    // control: enabled
    assertEquals( Boolean.TRUE, adapter.getPreserved( Props.ENABLED ) );
    Fixture.clearPreserved();
    table.setEnabled( false );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( table );
    assertEquals( Boolean.FALSE, adapter.getPreserved( Props.ENABLED ) );
    Fixture.clearPreserved();
    table.setEnabled( true );
    // visible
    table.setSize( 10, 10 );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( table );
    assertEquals( Boolean.TRUE, adapter.getPreserved( Props.VISIBLE ) );
    Fixture.clearPreserved();
    table.setVisible( false );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( table );
    assertEquals( Boolean.FALSE, adapter.getPreserved( Props.VISIBLE ) );
    Fixture.clearPreserved();
    // menu
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( table );
    assertEquals( null, adapter.getPreserved( Props.MENU ) );
    Fixture.clearPreserved();
    Menu menu = new Menu( table );
    MenuItem item = new MenuItem( menu, SWT.NONE );
    item.setText( "1 Item" );
    table.setMenu( menu );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( table );
    assertEquals( menu, adapter.getPreserved( Props.MENU ) );
    Fixture.clearPreserved();
    // bounds
    Rectangle rectangle = new Rectangle( 10, 10, 30, 50 );
    table.setBounds( rectangle );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( table );
    assertEquals( rectangle, adapter.getPreserved( Props.BOUNDS ) );
    Fixture.clearPreserved();
    // foreground background font
    Color background = new Color( display, 122, 33, 203 );
    table.setBackground( background );
    Color foreground = new Color( display, 211, 178, 211 );
    table.setForeground( foreground );
    Font font = new Font( display, "font", 12, SWT.BOLD );
    table.setFont( font );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( table );
    assertEquals( background, adapter.getPreserved( Props.BACKGROUND ) );
    assertEquals( foreground, adapter.getPreserved( Props.FOREGROUND ) );
    assertEquals( font, adapter.getPreserved( Props.FONT ) );
    Fixture.clearPreserved();
    // tooltip text
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( table );
    assertEquals( null, table.getToolTipText() );
    Fixture.clearPreserved();
    table.setToolTipText( "some text" );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( table );
    assertEquals( "some text", table.getToolTipText() );
  }

  @Test
  public void testFireWidgetSelectedWithCheck() {
    table = new Table( shell, SWT.CHECK );
    TableItem item = new TableItem( table, SWT.NONE );
    SelectionListener listener = mock( SelectionListener.class );
    table.addSelectionListener( listener );

    fakeWidgetSelected( table, getId( item ), "check" );
    Fixture.readDataAndProcessAction( display );

    ArgumentCaptor<SelectionEvent> captor = ArgumentCaptor.forClass( SelectionEvent.class );
    verify( listener, times( 1 ) ).widgetSelected( captor.capture() );
    assertSame( item, captor.getValue().item );
    assertEquals( SWT.CHECK, captor.getValue().detail );
    verify( listener, times( 0 ) ).widgetDefaultSelected( any( SelectionEvent.class ) );
  }

  @Test
  public void testFireWidgetDefaultSelected() {
    table = new Table( shell, SWT.MULTI );
    TableItem item = new TableItem( table, SWT.NONE );
    SelectionListener listener = mock( SelectionListener.class );
    table.addSelectionListener( listener );

    fakeWidgetDefaultSelected( table, item );
    Fixture.readDataAndProcessAction( display );

    verify( listener, times( 0 ) ).widgetSelected( any( SelectionEvent.class ) );
    ArgumentCaptor<SelectionEvent> captor = ArgumentCaptor.forClass( SelectionEvent.class );
    verify( listener, times( 1 ) ).widgetDefaultSelected( captor.capture() );
    assertSame( item, captor.getValue().item );
  }

  @Test
  public void testFireWidgetDefaultSelected_WithoutFocusedItem() {
    table = new Table( shell, SWT.MULTI );
    new TableItem( table, SWT.NONE );
    TableItem disposedItem = new TableItem( table, SWT.NONE );
    disposedItem.dispose();
    SelectionListener listener = mock( SelectionListener.class );
    table.addSelectionListener( listener );

    fakeWidgetDefaultSelected( table, disposedItem );
    Fixture.readDataAndProcessAction( display );

    verify( listener, times( 0 ) ).widgetSelected( any( SelectionEvent.class ) );
    ArgumentCaptor<SelectionEvent> captor = ArgumentCaptor.forClass( SelectionEvent.class );
    verify( listener, times( 1 ) ).widgetDefaultSelected( captor.capture() );
    assertNull( captor.getValue().item );
  }

  @Test
  public void testFireWidgetDefaultSelected_WithFocusedItem() {
    table = new Table( shell, SWT.MULTI );
    TableItem item = new TableItem( table, SWT.NONE );
    TableItem disposedItem = new TableItem( table, SWT.NONE );
    disposedItem.dispose();
    table.setSelection( 0 ); // set focus item
    SelectionListener listener = mock( SelectionListener.class );
    table.addSelectionListener( listener );

    fakeWidgetDefaultSelected( table, disposedItem );
    Fixture.readDataAndProcessAction( display );

    verify( listener, times( 0 ) ).widgetSelected( any( SelectionEvent.class ) );
    ArgumentCaptor<SelectionEvent> captor = ArgumentCaptor.forClass( SelectionEvent.class );
    verify( listener, times( 1 ) ).widgetDefaultSelected( captor.capture() );
    assertSame( item, captor.getValue().item );
  }

  @Test
  public void testRedraw() {
    final Table[] table = { null };
    shell.setSize( 100, 100 );
    Button button = new Button( shell, SWT.PUSH );
    getRemoteObject( button ).setHandler( new ButtonOperationHandler( button ) );
    button.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        table[ 0 ] = new Table( shell, SWT.VIRTUAL );
        table[ 0 ].setItemCount( 500 );
        table[ 0 ].setSize( 90, 90 );
        assertFalse( isItemVirtual( table[ 0 ], 0 ) );
        table[ 0 ].clearAll();
        table[ 0 ].redraw();
      }
    } );
    shell.open();

    Fixture.fakeNotifyOperation( getId( button ), ClientMessageConst.EVENT_SELECTION, null );
    Fixture.readDataAndProcessAction( display );

    assertFalse( isItemVirtual( table[ 0 ], 0  ) );
  }

  @Test
  public void testNoUnwantedResolveItems() {
    shell.setSize( 100, 100 );
    table = new Table( shell, SWT.VIRTUAL );
    table.setSize( 90, 90 );
    table.setItemCount( 1000 );
    shell.open();
    fakeSetTopItemIndex( table, 500 );

    Fixture.executeLifeCycleFromServerThread();

    assertTrue( isItemVirtual( table, 499 ) );
    assertTrue( isItemVirtual( table, 800 ) );
    assertTrue( isItemVirtual( table, 999 ) );
  }

  @Test
  public void testClearVirtual() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    shell.setSize( 100, 100 );
    shell.setLayout( new FillLayout() );
    table = new Table( shell, SWT.VIRTUAL );
    table.setItemCount( 100 );
    shell.layout();
    shell.open();
    ITableAdapter adapter = table.getAdapter( ITableAdapter.class );
    // precondition: all items are resolved (TableItem#cached == true)
    // resolve all items and ensure
    for( int i = 0; i < table.getItemCount(); i++ ) {
      table.getItem( i ).getText();
    }
    assertFalse( adapter.isItemVirtual( table.getItemCount() - 1 ) );
    //
    final int lastItemIndex = table.getItemCount() - 1;
    // fake one request that would initialize the UI
    Fixture.fakeNewRequest();
    Fixture.executeLifeCycleFromServerThread();
    // run actual request
    Fixture.fakeNewRequest();
    LifeCycle lifeCycle = getApplicationContext().getLifeCycleFactory().getLifeCycle();
    lifeCycle.addPhaseListener( new PhaseListener() {
      private static final long serialVersionUID = 1L;
      public void beforePhase( PhaseEvent event ) {
        table.clear( lastItemIndex );
      }
      public void afterPhase( PhaseEvent event ) {
      }
      public PhaseId getPhaseId() {
        return PhaseId.PROCESS_ACTION;
      }
    } );
    Fixture.executeLifeCycleFromServerThread();
    Message message = Fixture.getProtocolMessage();
    assertNotNull( message.findCallOperation( table.getItem( lastItemIndex ), "clear" ) );
  }

  @Test
  public void testSetDataEvent() {
    shell.setSize( 100, 100 );
    table = new Table( shell, SWT.VIRTUAL );
    Listener listener = new Listener() {
      public void handleEvent( Event event ) {
        Item item = ( Item )event.item;
        item.setText( "Item " + event.index );
      }
    };
    table.addListener( SWT.SetData, listener );
    table.setSize( 90, 90 );
    table.setItemCount( 1000 );
    shell.layout();
    shell.open();
    assertTrue( isItemVirtual( table, 500 ) ); // ensure precondition

    fakeSetTopItemIndex( table, 500 );
    Fixture.executeLifeCycleFromServerThread();

    // Remove SetData listener to not accidentially resolve item with asserts
    table.removeListener( SWT.SetData, listener );
    assertFalse( isItemVirtual( table, 500 ) );
    assertFalse( isItemVirtual( table, 502 ) );
    assertTrue( isItemVirtual( table, 510 ) );
    assertEquals( "Item 500", table.getItem( 500 ).getText() );
    assertEquals( "Item 502", table.getItem( 502 ).getText() );
  }

  @Test
  public void testReadSelection() {
    table = new Table( shell, SWT.MULTI );
    TableItem item1 = new TableItem( table, SWT.NONE );
    TableItem item2 = new TableItem( table, SWT.NONE );

    JsonValue selection = createJsonArray( getId( item1 ), getId( item2 ) );
    Fixture.fakeSetProperty( getId( table ), "selection", selection );
    Fixture.executeLifeCycleFromServerThread();

    TableItem[] selectedItems = table.getSelection();
    assertEquals( 2, selectedItems.length );
    assertSame( item1, selectedItems[ 1 ] );
    assertSame( item2, selectedItems[ 0 ] );
  }

  @Test
  public void testReadSelection_UnresolvedItem() {
    table = new Table( shell, SWT.MULTI | SWT.VIRTUAL );
    table.setItemCount( 3 );
    TableItem item = table.getItem( 0 );
    item.setText( "Item 1" );

    Fixture.fakeNewRequest();
    JsonValue selection = createJsonArray( getId( item ), getId( table ) + "#2" );
    Fixture.fakeSetProperty( getId( table ), "selection", selection );
    Fixture.executeLifeCycleFromServerThread();

    int[] selectedIndices = table.getSelectionIndices();
    assertEquals( 2, selectedIndices.length );
    assertEquals( 0, selectedIndices[ 1 ] );
    assertEquals( 2, selectedIndices[ 0 ] );
    assertTrue( isItemVirtual( table, 2 ) );
  }

  @Test
  public void testReadSelectionDisposedItem() {
    table = new Table( shell, SWT.MULTI );
    TableItem item = new TableItem( table, SWT.NONE );
    new TableItem( table, SWT.NONE );
    item.dispose();

    Fixture.fakeNewRequest();
    Fixture.fakeSetProperty( getId( table ), "selection", createJsonArray( getId( item ) ) );
    Fixture.executeLifeCycleFromServerThread();

    TableItem[] selectedItems = table.getSelection();
    assertEquals( 0, selectedItems.length );
  }

  /*
   * Ensures that checkData calls with an invalid index are silently ignored.
   * This may happen, when the itemCount is reduced during a SetData event.
   * Queued SetData events may then have stale (out-of-bounds) indices.
   * See 235368: [table] [table] ArrayIndexOutOfBoundsException in virtual
   *     TableViewer
   *     https://bugs.eclipse.org/bugs/show_bug.cgi?id=235368
   */
  @Test
  public void testReduceItemCountInSetData() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    shell.setSize( 100, 100 );
    table = new Table( shell, SWT.VIRTUAL );
    Listener setDataListener = mock( Listener.class );
    table.addListener( SWT.SetData, setDataListener );

    Fixture.fakePhase( PhaseId.READ_DATA );
    table.setItemCount( 1 );
    ITableAdapter adapter = table.getAdapter( ITableAdapter.class );
    adapter.checkData( 0 );

    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    table.setItemCount( 0 );
    while( display.readAndDispatch() ) {
    }
    verifyZeroInteractions( setDataListener );
  }

  @Test
  public void testGetItemMetrics() throws IOException {
    Image image = createImage( display, Fixture.IMAGE1 );
    shell.setBounds( 0, 0, 800, 600 );
    shell.setLayout( new FillLayout() );
    table.setHeaderVisible( true );
    TableColumn column = new TableColumn( table, SWT.NONE );
    column.setText( "column1" );
    column.setWidth( 200 );
    TableItem item1 = new TableItem( table, SWT.NONE );
    item1.setText( "item1" );
    TableItem item2 = new TableItem( table, SWT.NONE );
    item2.setText( "item2" );
    TableItem item3 = new TableItem( table, SWT.NONE );
    item3.setText( "item3" );

    item2.setImage( image );
    ItemMetrics[] metrics = TableLCA.getItemMetrics( table );
    assertTrue( metrics[ 0 ].imageWidth > 0 );

    item1.setImage( image );
    metrics = TableLCA.getItemMetrics( table );
    int defaultLeftPadding = 3;
    assertEquals( defaultLeftPadding, metrics[ 0 ].imageLeft );
    assertTrue( metrics[ 0 ].imageWidth > 0 );

    // spacing must be respected
    int defaultSpacing = 3;
    int expected =   metrics[ 0 ].imageLeft
                   + metrics[ 0 ].imageWidth
                   + defaultSpacing;
    assertEquals( expected, metrics[ 0 ].textLeft );

    // left offset must be compensated
    ITableAdapter adapter
      = table.getAdapter( ITableAdapter.class );
    adapter.setLeftOffset( 10 );
    metrics = TableLCA.getItemMetrics( table );
    assertEquals( 0, metrics[ 0 ].left );
    assertEquals( defaultLeftPadding, metrics[ 0 ].imageLeft );
    expected =   metrics[ 0 ].imageLeft
               + metrics[ 0 ].imageWidth
               + defaultSpacing;
    assertEquals( expected, metrics[ 0 ].textLeft );

    // image must not exceed right column border
    column.setWidth( 12 );
    metrics = TableLCA.getItemMetrics( table );
    assertEquals( 9, metrics[ 0 ].imageWidth );
  }

  @Test
  public void testGetItemMetricsWithCheckBox() throws IOException {
    Image image = createImage( display, Fixture.IMAGE1 );
    shell.setBounds( 0, 0, 200, 200 );
    shell.setLayout( new FillLayout() );
    table = new Table( shell, SWT.CHECK );
    table.setHeaderVisible( true );
    TableColumn column = new TableColumn( table, SWT.NONE );
    column.setText( "column1" );
    column.setWidth( 30 );
    TableColumn column2 = new TableColumn( table, SWT.NONE );
    column2.setText( "column2" );
    column2.setWidth( 400 );
    ITableAdapter adapter = table.getAdapter( ITableAdapter.class );
    adapter.setLeftOffset( 5 );
    TableItem item1 = new TableItem( table, SWT.NONE );
    item1.setText( "item1" );
    TableItem item2 = new TableItem( table, SWT.NONE );
    item2.setText( "item2" );
    TableItem item3 = new TableItem( table, SWT.NONE );
    item3.setText( "item3" );
    item2.setImage( image );
    ItemMetrics[] metrics = TableLCA.getItemMetrics( table );
    assertEquals( 28, metrics[ 0 ].imageLeft );
    assertEquals( 2, metrics[ 0 ].imageWidth );
  }

  @Test
  public void testGetItemMetricsImageCutOffInSecondColumn() throws IOException {
    Image image = createImage( display, Fixture.IMAGE1 );
    shell.setBounds( 0, 0, 200, 200 );
    shell.setLayout( new FillLayout() );
    table.setHeaderVisible( true );
    TableColumn column = new TableColumn( table, SWT.NONE );
    column.setText( "column1" );
    column.setWidth( 400 );
    TableColumn column2 = new TableColumn( table, SWT.NONE );
    column2.setText( "column2" );
    column2.setWidth( 30 );
    ITableAdapter adapter = table.getAdapter( ITableAdapter.class );
    adapter.setLeftOffset( 5 );
    TableItem item1 = new TableItem( table, SWT.NONE );
    item1.setText( "item1" );
    TableItem item2 = new TableItem( table, SWT.NONE );
    item2.setText( "item2" );
    TableItem item3 = new TableItem( table, SWT.NONE );
    item3.setText( "item3" );
    item2.setImage( 1, image );
    ItemMetrics[] metrics = TableLCA.getItemMetrics( table );
    assertEquals( 403, metrics[ 1 ].imageLeft );
    assertEquals( 27, metrics[ 1 ].imageWidth );
  }

  @Test
  public void testGetItemMetricsWithoutColumns() throws IOException {
    Image image = createImage( display, Fixture.IMAGE1 );
    shell.setBounds( 0, 0, 800, 600 );
    shell.setLayout( new FillLayout() );
    table.setHeaderVisible( true );
    TableItem item1 = new TableItem( table, SWT.NONE );
    item1.setText( "item1" );
    TableItem item2 = new TableItem( table, SWT.NONE );
    item2.setText( "item2" );
    TableItem item3 = new TableItem( table, SWT.NONE );
    item3.setText( "item3" );

    ItemMetrics[] metrics = TableLCA.getItemMetrics( table );
    assertEquals( 0, metrics[ 0 ].imageWidth );

    item2.setImage( image );
    metrics = TableLCA.getItemMetrics( table );
    assertTrue( metrics[ 0 ].imageWidth > 0 );
    int defaultLeftPadding = 3;
    assertEquals( defaultLeftPadding, metrics[ 0 ].imageLeft );
    assertTrue( metrics[ 0 ].imageWidth > 0 );

    // spacing must be respected
    int defaultSpacing = 3;
    int expected =   metrics[ 0 ].imageLeft
                   + metrics[ 0 ].imageWidth
                   + defaultSpacing;
    assertEquals( expected, metrics[ 0 ].textLeft );

    // left offset must be compensated
    ITableAdapter adapter = table.getAdapter( ITableAdapter.class );
    adapter.setLeftOffset( 10 );
    metrics = TableLCA.getItemMetrics( table );
    assertEquals( 0, metrics[ 0 ].left );
    assertEquals( defaultLeftPadding, metrics[ 0 ].imageLeft );
    expected =   metrics[ 0 ].imageLeft
               + metrics[ 0 ].imageWidth
               + defaultSpacing;
    assertEquals( expected, metrics[ 0 ].textLeft );
  }

  @Test
  public void testGetItemMetricsWithEmptyTable() {
    table.setHeaderVisible( true );
    for( int i = 0; i < 3; i++ ) {
      TableColumn column = new TableColumn( table, SWT.NONE );
      column.setText( "column" + i );
      column.setWidth( 100 );
    }

    ItemMetrics[] metrics = TableLCA.getItemMetrics( table );

    assertEquals( 100, metrics[ 1 ].left );
    assertEquals( 100, metrics[ 1 ].width );
  }

  @Test
  public void testReadFocusItem() {
    // ensure that reading selection parameter does not override focusIndex
    table = new Table( shell, SWT.MULTI );
    for( int i = 0; i < 5; i++ ) {
      new TableItem( table, SWT.NONE );
    }

    Fixture.fakeSetProperty( getId( table ), "focusItem", indexToId( table, 4 ) );
    JsonValue items = JsonUtil.createJsonArray( indicesToIds( table, new int[]{ 0, 1, 2, 3, 4 } ) );
    Fixture.fakeSetProperty( getId( table ), "selection", items );
    TableLCA tableLCA = new TableLCA();
    tableLCA.readData( table );

    assertEquals( 4, table.getAdapter( ITableAdapter.class ).getFocusIndex() );
  }

  @Test
  public void testReadUnresolvedFocusItem() {
    // ensure that reading selection parameter does not override focusIndex
    table = new Table( shell, SWT.MULTI );
    createTableItems( table, 5 );

    Fixture.fakeSetProperty( getId( table ), "focusItem", getId( table ) + "#4" );
    JsonValue items = createJsonArray( indicesToIds( table, new int[]{ 0, 1, 2, 3, 4 } ) );
    Fixture.fakeSetProperty( getId( table ), "selection", items );
    TableLCA tableLCA = new TableLCA();
    tableLCA.readData( table );

    assertEquals( 4, table.getAdapter( ITableAdapter.class ).getFocusIndex() );
  }

  @Test
  public void testReadDisposedFocusItem() {
    // ensure that reading selection parameter does not override focusIndex
    table = new Table( shell, SWT.MULTI );
    createTableItems( table, 5 );

    JsonValue items = createJsonArray( indicesToIds( table, new int[]{ 0, 1, 2, 3, 4 } ) );
    Fixture.fakeSetProperty( getId( table ), "selection", items );
    Fixture.fakeSetProperty( getId( table ), "focusItem", indexToId( table, 4 ) );
    table.getItem( 4 ).dispose();
    TableLCA tableLCA = new TableLCA();
    tableLCA.readData( table );

    assertEquals( -1, table.getAdapter( ITableAdapter.class ).getFocusIndex() );
  }

  @Test
  public void testReadTopIndex() {
    table = new Table( shell, SWT.MULTI );
    table.setSize( 485, 485 );
    createTableItems( table, 115 );

    int[] indices = new int[]{
      114,70,71,72,73,74,75,76,77,78,79,80,81,82,83,
      84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,
      99,100,101,102,103,104,105,106,107,108,109,
      110,111,112,113,0
    };
    JsonValue items = createJsonArray( indicesToIds( table, indices ) );
    Fixture.fakeSetProperty( getId( table ), "selection", items );
    fakeSetTopItemIndex( table, 0 );
    TableLCA tableLCA = new TableLCA();
    tableLCA.readData( table );

    assertEquals( 0, table.getTopIndex() );
  }

  @Test
  public void testCellTooltipRequestForMissingCells() {
    createTableItems( table, 3 );
    final StringBuilder log = new StringBuilder();
    ICellToolTipAdapter tableAdapter = table.getAdapter( ICellToolTipAdapter.class );
    tableAdapter.setCellToolTipProvider( new ICellToolTipProvider() {
      public void getToolTipText( Item item, int columnIndex ) {
        StringBuilder buffer = new StringBuilder();
        buffer.append( "[" );
        buffer.append( WidgetUtil.getId( item ) );
        buffer.append( "," );
        buffer.append( columnIndex );
        buffer.append( "]" );
        log.append( buffer.toString() );
      }
    } );
    String itemId = WidgetUtil.getId( table.getItem( 0 ) );
    fakeCellToolTipRequest( table, itemId, 0 );
    Fixture.executeLifeCycleFromServerThread();
    String expected = "[" + itemId + ",0]";
    assertEquals( expected, log.toString() );
    log.setLength( 0 );
    itemId = WidgetUtil.getId( table.getItem( 2 ) );
    fakeCellToolTipRequest( table, itemId, 0 );
    Fixture.executeLifeCycleFromServerThread();
    expected = "[" + itemId + ",0]";
    assertEquals( expected, log.toString() );
    log.setLength( 0 );
    fakeCellToolTipRequest( table, "xyz", 0 );
    Fixture.executeLifeCycleFromServerThread();
    assertEquals( "", log.toString() );
    fakeCellToolTipRequest( table, itemId, 1 );
    Fixture.executeLifeCycleFromServerThread();
    assertEquals( "", log.toString() );
    createTableColumns( table, 2 );
    fakeCellToolTipRequest( table, itemId, 1 );
    Fixture.executeLifeCycleFromServerThread();
    expected = "[" + itemId + ",1]";
    assertEquals( expected, log.toString() );
    log.setLength( 0 );
    fakeCellToolTipRequest( table, itemId, 2 );
    Fixture.executeLifeCycleFromServerThread();
    assertEquals( "", log.toString() );
  }

  @Test
  public void testHorizontalScrollbarsSelectionEvent() {
    createTableItems( table, 20 );
    SelectionListener listener = mock( SelectionListener.class );
    table.getHorizontalBar().addSelectionListener( listener );

    Fixture.fakeNotifyOperation( getId( table.getHorizontalBar() ), "Selection", null );
    Fixture.readDataAndProcessAction( table );

    verify( listener, times( 1 ) ).widgetSelected( any( SelectionEvent.class ) );
  }

  @Test
  public void testVerticalScrollbarsSelectionEvent() {
    createTableItems( table, 20 );
    SelectionListener listener = mock( SelectionListener.class );
    table.getVerticalBar().addSelectionListener( listener );

    Fixture.fakeNotifyOperation( getId( table.getVerticalBar() ), "Selection", null );
    Fixture.readDataAndProcessAction( table );

    verify( listener, times( 1 ) ).widgetSelected( any( SelectionEvent.class ) );
  }

  @Test
  public void testSelectionEvent_UnresolvedItem() {
    table = new Table( shell, SWT.VIRTUAL );
    table.setItemCount( 3 );
    SelectionListener listener = mock( SelectionListener.class );
    table.addSelectionListener( listener );

    fakeWidgetSelected( table, getId( table ) + "#2" );
    Fixture.readDataAndProcessAction( table );

    ArgumentCaptor<SelectionEvent> captor = ArgumentCaptor.forClass( SelectionEvent.class );
    verify( listener ).widgetSelected( captor.capture() );
    assertSame( table.getItem( 2 ), captor.getValue().item );
  }

  @Test
  public void testRenderNonNegativeImageWidth() throws IOException {
    TableColumn column = new TableColumn( table, SWT.NONE );
    TableItem item = new TableItem( table, SWT.NONE );
    Image image = createImage( display, Fixture.IMAGE1 );
    item.setImage( image );
    column.setWidth( 2 );
    ItemMetrics[] metrics = TableLCA.getItemMetrics( table );
    assertEquals( 1, metrics.length );
    assertEquals( 0, metrics[ 0 ].imageWidth );
  }

  // bug 360152
  @Test
  public void testReadItemToolTipDoesNotResolveVirtualItems() {
    table = new Table( shell, SWT.VIRTUAL );
    table.setData( ICellToolTipProvider.ENABLE_CELL_TOOLTIP, Boolean.TRUE );
    ICellToolTipAdapter toolTipAdapter = CellToolTipUtil.getAdapter( table );
    ITableAdapter tableAdapter = table.getAdapter( ITableAdapter.class );
    ICellToolTipProvider toolTipProvider = mock( ICellToolTipProvider.class );
    toolTipAdapter.setCellToolTipProvider( toolTipProvider );
    table.setItemCount( 2 );
    TableItem item = table.getItem( 1 );

    fakeCellToolTipRequest( table, getId( item ), 0 );
    Fixture.readDataAndProcessAction( table );

    verify( toolTipProvider ).getToolTipText( item, 0 );
    assertEquals( 1, tableAdapter.getCreatedItems().length );
  }

  @Test
  public void testRenderCreate() throws IOException {
    lca.renderInitialization( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertEquals( "rwt.widgets.Grid", operation.getType() );
    assertEquals( "table", operation.getProperty( "appearance" ).asString() );
    assertEquals( 0, operation.getProperty( "indentionWidth" ).asInt() );
    assertEquals( -1, operation.getProperty( "treeColumn" ).asInt() );
    assertFalse( operation.getPropertyNames().contains( "checkBoxMetrics" ) );
    assertEquals( JsonValue.FALSE, operation.getProperty( "markupEnabled" ) );
  }

  @Test
  public void testRenderCreateWithFixedColumns() throws IOException {
    table.setData( RWT.FIXED_COLUMNS, Integer.valueOf( 1 ) );

    lca.renderInitialization( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertEquals( JsonValue.TRUE, operation.getProperty( "splitContainer" ) );
  }

  @Test
  public void testRenderParent() throws IOException {
    lca.renderInitialization( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertEquals( WidgetUtil.getId( table.getParent() ), operation.getParent() );
  }

  @Test
  public void testRenderCreateWithVirtualNoScrollMulti() throws IOException {
    table = new Table( shell, SWT.VIRTUAL | SWT.NO_SCROLL | SWT.MULTI );

    lca.renderInitialization( table );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation create = message.findCreateOperation( table );
    Object[] styles = create.getStyles();
    assertTrue( Arrays.asList( styles ).contains( "VIRTUAL" ) );
    assertTrue( Arrays.asList( styles ).contains( "NO_SCROLL" ) );
    assertTrue( Arrays.asList( styles ).contains( "MULTI" ) );
    assertEquals( JsonValue.TRUE, message.findListenProperty( table, "SetData" ) );
  }

  @Test
  public void testDontRenderSetDataListenerTwice() throws Exception {
    table = new Table( shell, SWT.VIRTUAL | SWT.NO_SCROLL | SWT.MULTI );
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );
    Fixture.preserveWidgets();

    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( table, "SetData" ) );
  }

  @Test
  public void testDontRenderSetDataWithoutVirtual() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );
    Fixture.preserveWidgets();

    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( table, "SetData" ) );
  }

  @Test
  public void testRenderCreateWithHideSelection() throws IOException {
    table = new Table( shell, SWT.HIDE_SELECTION );

    lca.renderInitialization( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    Object[] styles = operation.getStyles();
    assertTrue( Arrays.asList( styles ).contains( "HIDE_SELECTION" ) );
  }

  @Test
  public void testRenderCreateWithCheck() throws IOException {
    table = new Table( shell, SWT.CHECK );

    lca.renderInitialization( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    Object[] styles = operation.getStyles();
    assertTrue( Arrays.asList( styles ).contains( "CHECK" ) );
    JsonArray expected = JsonArray.readFrom( "[4, 21]" );
    assertEquals( expected, operation.getProperty( "checkBoxMetrics" ) );
  }

  @Test
  public void testRenderInitialItemCount() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertTrue( operation.getPropertyNames().indexOf( "itemCount" ) == -1 );
  }

  @Test
  public void testRenderItemCount() throws IOException {
    table.setItemCount( 10 );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 10, message.findSetProperty( table, "itemCount" ).asInt() );
  }

  @Test
  public void testRenderItemCountUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    table.setItemCount( 10 );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "itemCount" ) );
  }

  @Test
  public void testRenderInitialItemHeight() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    assertNotNull( message.findSetOperation( table, "itemHeight" ) );
  }

  @Test
  public void testRenderItemHeight() throws IOException {
    Font font = new Font( display, "Arial", 26, SWT.NONE );

    table.setFont( font );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNotNull( message.findSetOperation( table, "itemHeight" ) );
  }

  @Test
  public void testRenderItemHeightUnchanged() throws IOException {
    Font font = new Font( display, "Arial", 26, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    table.setFont( font );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "itemHeight" ) );
  }

  @Test
  public void testRenderInitialItemMetrics() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    assertNotNull( message.findSetOperation( table, "itemMetrics" ) );
  }

  @Test
  public void testRenderItemMetrics() throws IOException {
    TableItem item = new TableItem( table, SWT.NONE );
    item.setText( "foo" );

    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    JsonArray expected = JsonArray.readFrom( "[[0, 0, 26, 3, 0, 3, 20]]" );
    assertEquals( expected, message.findSetProperty( table, "itemMetrics" ) );
  }

  @Test
  public void testRenderItemMetricsUnchanged() throws IOException {
    TableItem item = new TableItem( table, SWT.NONE );
    item.setText( "foo" );
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "itemMetrics" ) );
  }

  @Test
  public void testRenderInitialColumnCount() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertTrue( operation.getPropertyNames().indexOf( "columnCount" ) == -1 );
  }

  @Test
  public void testRenderColumnCount() throws IOException {
    new TableColumn( table, SWT.NONE );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 1, message.findSetProperty( table, "columnCount" ).asInt() );
  }

  @Test
  public void testRenderColumnCountUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    new TableColumn( table, SWT.NONE );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "columnCount" ) );
  }

  @Test
  public void testRenderInitialFixedColumns() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertTrue( operation.getPropertyNames().indexOf( "fixedColumns" ) == -1 );
  }

  @Test
  public void testRenderFixedColumns() throws IOException {
    new TableColumn( table, SWT.NONE );

    table.setData( RWT.FIXED_COLUMNS, Integer.valueOf( 1 ) );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 1, message.findSetProperty( table, "fixedColumns" ).asInt() );
  }

  @Test
  public void testRenderFixedColumnsUnchanged() throws IOException {
    new TableColumn( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    table.setData( "fixedColumns", Integer.valueOf( 1 ) );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "fixedColumns" ) );
  }

  @Test
  public void testRenderInitialHeaderHeight() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertTrue( operation.getPropertyNames().indexOf( "headerHeight" ) == -1 );
  }

  @Test
  public void testRenderHeaderHeight() throws IOException {
    table.setHeaderVisible( true );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 31, message.findSetProperty( table, "headerHeight" ).asInt() );
  }

  @Test
  public void testRenderHeaderHeightUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    table.setHeaderVisible( true );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "headerHeight" ) );
  }

  @Test
  public void testRenderInitialHeaderVisible() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertTrue( operation.getPropertyNames().indexOf( "headerVisible" ) == -1 );
  }

  @Test
  public void testRenderHeaderVisible() throws IOException {
    table.setHeaderVisible( true );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findSetProperty( table, "headerVisible" ) );
  }

  @Test
  public void testRenderHeaderVisibleUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    table.setHeaderVisible( true );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "headerVisible" ) );
  }

  @Test
  public void testRenderInitialLinesVisible() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertTrue( operation.getPropertyNames().indexOf( "linesVisible" ) == -1 );
  }

  @Test
  public void testRenderLinesVisible() throws IOException {
    table.setLinesVisible( true );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findSetProperty( table, "linesVisible" ) );
  }

  @Test
  public void testRenderLinesVisibleUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    table.setLinesVisible( true );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "linesVisible" ) );
  }

  @Test
  public void testRenderInitialTopItemIndex() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertTrue( operation.getPropertyNames().indexOf( "topItemIndex" ) == -1 );
  }

  @Test
  public void testRenderTopItemIndex() throws IOException {
    createTableItems( table, 3 );

    table.setTopIndex( 2 );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 2, message.findSetProperty( table, "topItemIndex" ).asInt() );
  }

  @Test
  public void testRenderTopItemIndexUnchanged() throws IOException {
    createTableItems( table, 3 );
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    table.setTopIndex( 2 );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "topItemIndex" ) );
  }

  @Test
  public void testRenderInitialFocusItem() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertTrue( operation.getPropertyNames().indexOf( "focusItem" ) == -1 );
  }

  @Test
  public void testRenderFocusItem() throws IOException {
    createTableItems( table, 2 );
    TableItem item = new TableItem( table, SWT.NONE );

    table.getAdapter( ITableAdapter.class ).setFocusIndex( 2 );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( getId( item ), message.findSetProperty( table, "focusItem" ).asString() );
  }

  @Test
  public void testRenderFocusItemUnchanged() throws IOException {
    createTableItems( table, 3 );
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    table.getAdapter( ITableAdapter.class ).setFocusIndex( 2 );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "focusItem" ) );
  }

  @Test
  public void testRenderInitialScrollLeft() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertTrue( operation.getPropertyNames().indexOf( "scrollLeft" ) == -1 );
  }

  @Test
  public void testRenderScrollLeft() throws IOException {
    table.getAdapter( ITableAdapter.class ).setLeftOffset( 10 );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 10, message.findSetProperty( table, "scrollLeft" ).asInt() );
  }

  @Test
  public void testRenderScrollLeftUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    table.getAdapter( ITableAdapter.class ).setLeftOffset( 10 );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "scrollLeft" ) );
  }

  @Test
  public void testRenderInitialSelection() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertTrue( operation.getPropertyNames().indexOf( "selection" ) == -1 );
  }

  @Test
  public void testRenderSelection() throws IOException {
    table = new Table( shell, SWT.MULTI );
    createTableItems( table, 3 );

    table.setSelection( new int[] { 0, 2 } );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    JsonArray expected = new JsonArray();
    expected.add( getId( table.getItem( 2 ) ) );
    expected.add( getId( table.getItem( 0 ) ) );
    assertEquals( expected, message.findSetProperty( table, "selection" ) );
  }

  @Test
  public void testRenderSelectionUnchanged() throws IOException {
    table = new Table( shell, SWT.MULTI );
    createTableItems( table, 3 );
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    table.setSelection( new int[] { 0, 2 } );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "selection" ) );
  }

  @Test
  public void testRenderInitialSortDirection() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertTrue( operation.getPropertyNames().indexOf( "sortDirection" ) == -1 );
  }

  @Test
  public void testRenderSortDirection() throws IOException {
    TableColumn column = new TableColumn( table, SWT.NONE );

    table.setSortColumn( column );
    table.setSortDirection( SWT.UP );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "up", message.findSetProperty( table, "sortDirection" ).asString() );
  }

  @Test
  public void testRenderSortDirectionUnchanged() throws IOException {
    TableColumn column = new TableColumn( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    table.setSortColumn( column );
    table.setSortDirection( SWT.UP );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "sortDirection" ) );
  }

  @Test
  public void testRenderInitialSortColumn() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertTrue( operation.getPropertyNames().indexOf( "sortColumn" ) == -1 );
  }

  @Test
  public void testRenderSortColumn() throws IOException {
    TableColumn column = new TableColumn( table, SWT.NONE );

    table.setSortColumn( column );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( getId( column ), message.findSetProperty( table, "sortColumn" ).asString() );
  }

  @Test
  public void testRenderSortColumnUnchanged() throws IOException {
    TableColumn column = new TableColumn( table, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    table.setSortColumn( column );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "sortColumn" ) );
  }

  @Test
  public void testRenderAddScrollBarsSelectionListener_Horizontal() throws Exception {
    table = new Table( shell, SWT.H_SCROLL | SWT.V_SCROLL );
    ScrollBar hScroll = table.getHorizontalBar();
    lca.renderInitialization( table );
    Fixture.preserveWidgets();

    hScroll.addSelectionListener( new SelectionAdapter() { } );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findListenProperty( hScroll, "Selection" ) );
  }

  @Test
  public void testRenderRemoveScrollBarsSelectionListener_Horizontal() throws Exception {
    table = new Table( shell, SWT.H_SCROLL | SWT.V_SCROLL );
    ScrollBar hScroll = table.getHorizontalBar();
    SelectionListener listener = new SelectionAdapter() { };
    hScroll.addSelectionListener( listener );
    lca.render( table );
    Fixture.preserveWidgets();

    hScroll.removeSelectionListener( listener );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findListenProperty( hScroll, "Selection" ) );
  }

  @Test
  public void testRenderScrollBarsSelectionListenerUnchanged_Horizontal() throws Exception {
    table = new Table( shell, SWT.H_SCROLL | SWT.V_SCROLL );
    ScrollBar hScroll = table.getHorizontalBar();
    Fixture.markInitialized( display );
    lca.render( table );

    hScroll.addSelectionListener( new SelectionAdapter() { } );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( hScroll, "Selection" ) );
  }

  @Test
  public void testRenderAddScrollBarsSelectionListener_Vertical() throws Exception {
    table = new Table( shell, SWT.H_SCROLL | SWT.V_SCROLL );
    ScrollBar vScroll = table.getVerticalBar();
    lca.renderInitialization( table );
    Fixture.preserveWidgets();

    vScroll.addSelectionListener( new SelectionAdapter() { } );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findListenProperty( vScroll, "Selection" ) );
  }

  @Test
  public void testRenderRemoveScrollBarsSelectionListener_Vertical() throws Exception {
    table = new Table( shell, SWT.H_SCROLL | SWT.V_SCROLL );
    ScrollBar vScroll = table.getVerticalBar();
    SelectionListener listener = new SelectionAdapter() { };
    vScroll.addSelectionListener( listener );
    lca.render( table );
    Fixture.preserveWidgets();

    vScroll.removeSelectionListener( listener );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findListenProperty( vScroll, "Selection" ) );
  }

  @Test
  public void testRenderScrollBarsSelectionListenerUnchanged_Vertical() throws Exception {
    table = new Table( shell, SWT.H_SCROLL | SWT.V_SCROLL );
    ScrollBar vScroll = table.getVerticalBar();
    Fixture.markInitialized( display );
    lca.render( table );

    vScroll.addSelectionListener( new SelectionAdapter() { } );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( vScroll, "Selection" ) );
  }

  @Test
  public void testRenderInitialScrollBarsVisible() throws IOException {
    table = new Table( shell, SWT.H_SCROLL | SWT.V_SCROLL );

    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table.getHorizontalBar(), "visibility" ) );
    assertNull( message.findSetOperation( table.getVerticalBar(), "visibility" ) );
  }

  @Test
  public void testRenderScrollBarsVisible_Horizontal() throws IOException {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    table = new Table( shell, SWT.H_SCROLL | SWT.V_SCROLL );
    TableColumn column = new TableColumn( table, SWT.NONE );

    column.setWidth( 25 );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findSetProperty( table.getHorizontalBar(), "visibility" ) );
    assertNull( message.findSetOperation( table.getVerticalBar(), "visibility" ) );
  }

  @Test
  public void testRenderScrollBarsVisible_Vertical() throws IOException {
    table = new Table( shell, SWT.H_SCROLL | SWT.V_SCROLL );
    new TableColumn( table, SWT.NONE );

    table.setHeaderVisible( true );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table.getHorizontalBar(), "visibility" ) );
    assertEquals( JsonValue.TRUE, message.findSetProperty( table.getVerticalBar(), "visibility" ) );
  }

  @Test
  public void testRenderScrollBarsVisibleUnchanged() throws IOException {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    table = new Table( shell, SWT.H_SCROLL | SWT.V_SCROLL );
    TableColumn column = new TableColumn( table, SWT.NONE );
    Fixture.markInitialized( display );
    lca.render( table );
    Fixture.fakeNewRequest();

    column.setWidth( 25 );
    table.setHeaderVisible( true );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table.getHorizontalBar(), "visibility" ) );
    assertNull( message.findSetOperation( table.getVerticalBar(), "visibility" ) );
  }

  @Test
  public void testRenderAddSelectionListener() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );
    Fixture.preserveWidgets();

    table.addListener( SWT.Selection, mock( Listener.class ) );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findListenProperty( table, "Selection" ) );
    assertNull( message.findListenOperation( table, "DefaultSelection" ) );
  }

  @Test
  public void testRenderRemoveSelectionListener() throws Exception {
    Listener listener = mock( Listener.class );
    table.addListener( SWT.Selection, listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );
    Fixture.preserveWidgets();

    table.removeListener( SWT.Selection, listener );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findListenProperty( table, "Selection" ) );
    assertNull( message.findListenOperation( table, "DefaultSelection" ) );
  }

  @Test
  public void testRenderAddDefaultSelectionListener() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );
    Fixture.preserveWidgets();

    table.addListener( SWT.DefaultSelection, mock( Listener.class ) );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findListenProperty( table, "DefaultSelection" ) );
    assertNull( message.findListenOperation( table, "Selection" ) );
  }

  @Test
  public void testRenderRemoveDefaultSelectionListener() throws Exception {
    Listener listener = mock( Listener.class );
    table.addListener( SWT.DefaultSelection, listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );
    Fixture.preserveWidgets();

    table.removeListener( SWT.DefaultSelection, listener );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findListenProperty( table, "DefaultSelection" ) );
    assertNull( message.findListenOperation( table, "Selection" ) );
  }

  @Test
  public void testRenderSelectionListenerUnchanged() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );
    Fixture.preserveWidgets();

    table.addSelectionListener( new SelectionAdapter() { } );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( table, "Selection" ) );
  }

  @Test
  public void testRenderInitialAlwaysHideSelection() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertTrue( operation.getPropertyNames().indexOf( "alwaysHideSelection" ) == -1 );
  }

  @Test
  public void testRenderAlwaysHideSelection() throws IOException {
    table.setData( Table.ALWAYS_HIDE_SELECTION, Boolean.TRUE );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findSetProperty( table, "alwaysHideSelection" ) );
  }

  @Test
  public void testRenderAlwaysHideSelectionUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    table.setData( Table.ALWAYS_HIDE_SELECTION, Boolean.TRUE );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "alwaysHideSelection" ) );
  }

  @Test
  public void testRenderInitialEnableCellToolTip() throws IOException {
    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( table );
    assertTrue( operation.getPropertyNames().indexOf( "enableCellToolTip" ) == -1 );
  }

  @Test
  public void testRenderEnableCellToolTip() throws IOException {
    table.setData( ICellToolTipProvider.ENABLE_CELL_TOOLTIP, Boolean.TRUE );
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findSetProperty( table, "enableCellToolTip" ) );
  }

  @Test
  public void testRenderEnableCellToolTipUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );

    table.setData( ICellToolTipProvider.ENABLE_CELL_TOOLTIP, Boolean.TRUE );
    Fixture.preserveWidgets();
    lca.renderChanges( table );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "enableCellToolTip" ) );
  }

  @Test
  public void testRenderCellToolTipText() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );
    createTableItems( table, 5 );
    final ICellToolTipAdapter adapter = CellToolTipUtil.getAdapter( table );
    adapter.setCellToolTipProvider( new ICellToolTipProvider() {
      public void getToolTipText( Item item, int columnIndex ) {
        StringBuilder buffer = new StringBuilder();
        buffer.append( "[" );
        buffer.append( WidgetUtil.getId( item ) );
        buffer.append( "," );
        buffer.append( columnIndex );
        buffer.append( "]" );
        adapter.setCellToolTipText( buffer.toString() );
      }
    } );

    String itemId = WidgetUtil.getId( table.getItem( 2 ) );
    fakeCellToolTipRequest( table, itemId, 0 );
    Fixture.executeLifeCycleFromServerThread();

    Message message = Fixture.getProtocolMessage();
    String expected = "[" + itemId + ",0]";
    assertEquals( expected, message.findSetProperty( table, "cellToolTipText" ).asString() );
  }

  @Test
  public void testRenderCellToolTipTextNull() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( table );
    createTableItems( table, 5 );
    final ICellToolTipAdapter adapter = CellToolTipUtil.getAdapter( table );
    adapter.setCellToolTipProvider( new ICellToolTipProvider() {
      public void getToolTipText( Item item, int columnIndex ) {
        adapter.setCellToolTipText( null );
      }
    } );

    String itemId = WidgetUtil.getId( table.getItem( 2 ) );
    fakeCellToolTipRequest( table, itemId, 0 );
    Fixture.executeLifeCycleFromServerThread();

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( table, "cellToolTipText" ) );
  }

  @Test
  public void testRenderMarkupEnabled() throws IOException {
    table.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );

    lca.render( table );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findCreateProperty( table, "markupEnabled" ) );
  }

  private static void createTableColumns( Table table, int count ) {
    for( int i = 0; i < count; i++ ) {
      new TableColumn( table, SWT.NONE );
    }
  }

  private static void createTableItems( Table table, int count ) {
    for( int i = 0; i < count; i++ ) {
      new TableItem( table, SWT.NONE );
    }
  }

  private static void fakeCellToolTipRequest( Table table, String itemId, int column ) {
    Fixture.fakeNewRequest();
    JsonObject parameters = new JsonObject()
      .add( "item", itemId )
      .add( "column", column );
    Fixture.fakeCallOperation( getId( table ), "renderToolTipText", parameters );
  }

  private static boolean isItemVirtual( Table table, int index ) {
    return table.getAdapter( ITableAdapter.class ).isItemVirtual( index );
  }

  private static String[] indicesToIds( Table table, int[] indices ) {
    String[] items = new String[ indices.length ];
    for( int i = 0; i < indices.length; i++ ) {
      items[ i ] = indexToId( table, indices[ i ] );
    }
    return items;
  }

  private static String indexToId( Table table, int index ) {
    return WidgetUtil.getId( table.getItem( index ) );
  }

  private void fakeWidgetSelected( Table table, String itemId ) {
    fakeWidgetSelected( table, itemId, null );
  }

  private void fakeWidgetSelected( Table table, String itemId, String detail ) {
    JsonObject parameters = new JsonObject()
      .add( ClientMessageConst.EVENT_PARAM_ITEM, itemId );
    if( detail != null ) {
      parameters.add( ClientMessageConst.EVENT_PARAM_DETAIL, detail );
    }
    Fixture.fakeNotifyOperation( getId( table ),
                                 ClientMessageConst.EVENT_SELECTION,
                                 parameters );
  }

  private void fakeWidgetDefaultSelected( Table table, TableItem item ) {
    Fixture.fakeNewRequest();
    JsonObject parameters = new JsonObject()
      .add( ClientMessageConst.EVENT_PARAM_ITEM, getId( item ) );
    Fixture.fakeNotifyOperation( getId( table ),
                                 ClientMessageConst.EVENT_DEFAULT_SELECTION,
                                 parameters );
  }

  private void fakeSetTopItemIndex( Table table, int index ) {
    Fixture.fakeSetProperty( getId( table ), "topItemIndex", index );
  }

}
