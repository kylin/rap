/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.lifecycle;

import java.io.IOException;

import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.swt.widgets.Display;


final class Render implements IPhase {

  public PhaseId getPhaseId() {
    return PhaseId.RENDER;
  }

  public PhaseId execute( Display display ) throws IOException {
    if( display != null ) {
      DisplayLifeCycleAdapter displayLCA = DisplayUtil.getLCA( display );
      displayLCA.render( display );
      displayLCA.clearPreserved( display );
    }
    return null;
  }

}
