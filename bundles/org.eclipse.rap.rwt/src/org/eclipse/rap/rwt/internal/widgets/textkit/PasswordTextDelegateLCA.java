/*******************************************************************************
 * Copyright (c) 2002-2006 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 ******************************************************************************/

package org.eclipse.rap.rwt.internal.widgets.textkit;

import java.io.IOException;
import org.eclipse.rap.rwt.internal.widgets.ControlLCAUtil;
import org.eclipse.rap.rwt.lifecycle.JSWriter;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.widgets.Text;

final class PasswordTextDelegateLCA extends TextDelegateLCA {

  void preserveValues( final Text text ) {
    ControlLCAUtil.preserveValues( text );
    TextLCAUtil.preserveText( text );
  }

  void readData( final Text text ) {
    TextLCAUtil.readText( text );
  }

  void renderInitialization( final Text text ) throws IOException {
    JSWriter writer = JSWriter.getWriterFor( text );
    writer.newWidget( "qx.ui.form.PasswordField" );
    ControlLCAUtil.writeStyleFlags( text );
    TextLCAUtil.writeNoSpellCheck( text );
    TextLCAUtil.writeModifyListeners( text );
    TextLCAUtil.writeReadOnly( text );
  }

  void renderChanges( final Text text ) throws IOException {
    JSWriter writer = JSWriter.getWriterFor( text );
    ControlLCAUtil.writeChanges( text );
    String newValue = text.getText();
    if( WidgetUtil.hasChanged( text, TextLCAUtil.PROP_TEXT, newValue, "" ) ) {
      writer.set( "value", TextLCAUtil.getRenderText( newValue ) );
    }
//    writer.set( Props.TEXT, JSConst.QX_FIELD_VALUE, getRenderText( text ), "" );
  }
  
  void renderDispose( final Text text ) throws IOException {
    JSWriter writer = JSWriter.getWriterFor( text );
    writer.dispose();
  }
}
