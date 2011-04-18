/*******************************************************************************
 * Copyright (c) 2008, 2011 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rwt.internal.textsize;

import java.io.IOException;

import org.eclipse.rwt.internal.textsize.TextSizeDeterminationFacade;
import org.eclipse.rwt.internal.textsize.TextSizeDetermination.ICalculationItem;
import org.eclipse.rwt.internal.textsize.TextSizeProbeStore.Probe;


public final class TextSizeDeterminationFacadeImpl extends TextSizeDeterminationFacade {

  public String createMeasureStringInternal( String string, boolean expandNewLines ) {
    return string;
  }

  public ICalculationItem[] writeStringMeasurementsInternal() throws IOException {
    return new ICalculationItem[ 0 ];
  }

  public Probe[] writeFontProbingInternal() throws IOException {
    return new Probe[ 0 ];
  }

  public String getStartupProbeCodeInternal() {
    return null;
  }
}