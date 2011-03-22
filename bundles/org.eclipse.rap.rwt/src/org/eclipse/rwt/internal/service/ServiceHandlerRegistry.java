/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rwt.internal.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import javax.xml.parsers.*;

import org.eclipse.rwt.internal.resources.ResourceManager;
import org.eclipse.rwt.internal.util.ClassUtil;
import org.eclipse.rwt.resources.IResourceManager;
import org.eclipse.rwt.service.IServiceHandler;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

class ServiceHandlerRegistry {
  private static final String SERVICEHANDLER_XML = "servicehandler.xml";

  private final Map handlers;
  private boolean initialized;
  
  ServiceHandlerRegistry() {
    handlers = new HashMap();
  }

  boolean isCustomHandler( String customHandlerId ) {
    ensureInitialization();
    synchronized( handlers ) {
      return handlers.containsKey( customHandlerId );
    }
  }
  
  void put( String id, IServiceHandler handler ) {
    ensureInitialization();
    synchronized( handlers ) {
      handlers.put( id, handler );
    }
  }

  void remove( String id ) {
    ensureInitialization();
    synchronized( handlers ) {
      handlers.remove( id );
    }
  }

  IServiceHandler get( String customHandlerId ) {
    ensureInitialization();
    synchronized( handlers ) {
      return ( IServiceHandler )handlers.get( customHandlerId );
    }
  }
  
  /////////////////
  // helper methods

  private void ensureInitialization() {
    synchronized( handlers ) {
      if( !initialized ) {
        registerHandlerInstances();
        initialized = true;
      }
    }
  }
  
  private void registerHandlerInstances() {
    try {
      IResourceManager manager = ResourceManager.getInstance();
      if( manager != null ) {
        registerHandlerInstances( manager );
      }
    } catch( RuntimeException rte ) {
      throw rte;
    } catch( Exception exception ) {
      String msg = "Could not load custom service handlers.";
      throw new RuntimeException( msg, exception );
    }
  }

  private void registerHandlerInstances( IResourceManager manager )
    throws IOException, FactoryConfigurationError, ParserConfigurationException, SAXException
  {
    Enumeration resources = manager.getResources( SERVICEHANDLER_XML );
    while( hasServiceHandlerDeclarations( resources ) ) {
      Document document = parseDocument( ( URL )resources.nextElement() );
      registerHandlerInstances( document );
    }
  }

  private boolean hasServiceHandlerDeclarations( Enumeration resources ) {
    return resources != null && resources.hasMoreElements();
  }

  private Document parseDocument( URL url )
    throws FactoryConfigurationError, ParserConfigurationException, IOException, SAXException
  {
    DocumentBuilder builder = createBuilder();
    URLConnection connection = openConnection( url );
    return parseDocument( builder, connection );
  }

  private Document parseDocument( DocumentBuilder builder, URLConnection connection )
    throws IOException, SAXException
  {
    Document result;
    InputStream inputStream = connection.getInputStream();
    try {
      result = builder.parse( inputStream );
    } finally {
      inputStream.close();
    }
    return result;
  }

  private URLConnection openConnection( URL url ) throws IOException {
    URLConnection result = url.openConnection();
    result.setUseCaches( false );
    return result;
  }

  private DocumentBuilder createBuilder()
    throws FactoryConfigurationError, ParserConfigurationException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    return factory.newDocumentBuilder();
  }

  private void registerHandlerInstances( Document document ) {
    NodeList handlerList = getHandlerList( document );
    for( int i = 0; i < handlerList.getLength(); i++ ) {
      String name = getClassName( handlerList.item( i ) );
      String id = getHandlerId( handlerList.item( i ) );
      registerHandlerInstance( id, name );
    }
  }

  private void registerHandlerInstance( String id, String name ) {
    Object handlerInstance = ClassUtil.newInstance( getClass().getClassLoader(), name );
    this.handlers.put( id, handlerInstance );
  }

  private NodeList getHandlerList( Document document ) {
    return document.getElementsByTagName( "handler" );
  }

  private String getHandlerId( Node item ) {
    return getAttribute( item, "requestparameter" );
  }

  private String getClassName( Node item ) {
    return getAttribute( item, "class" );
  }

  private String getAttribute( Node item, String attrName ) {
    return item.getAttributes().getNamedItem( attrName ).getNodeValue();
  }
}