/*******************************************************************************
 * Copyright (c) 2011 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.osgi.internal;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.servlet.*;

class ServletContextWrapper implements ServletContext {
  private final ServletContext servletContext;
  private final String contextDirectory;
  private final Map<String, Object> attributes;

  ServletContextWrapper( ServletContext servletContext, String contextDirectory ) {
    this.servletContext = servletContext;
    this.contextDirectory = contextDirectory;
    this.attributes = new HashMap<String, Object>();
  }

  public ServletContext getContext( String uripath ) {
    return servletContext.getContext( uripath );
  }

  public String getContextPath() {
    return servletContext.getContextPath();
  }

  public int getMajorVersion() {
    return servletContext.getMajorVersion();
  }

  public int getMinorVersion() {
    return servletContext.getMinorVersion();
  }

  public String getMimeType( String file ) {
    return servletContext.getMimeType( file );
  }

  public Set getResourcePaths( String path ) {
    return servletContext.getResourcePaths( path );
  }

  public URL getResource( String path ) throws MalformedURLException {
    return servletContext.getResource( path );
  }

  public InputStream getResourceAsStream( String path ) {
    return servletContext.getResourceAsStream( path );
  }

  public RequestDispatcher getRequestDispatcher( String path ) {
    return servletContext.getRequestDispatcher( path );
  }

  public RequestDispatcher getNamedDispatcher( String name ) {
    return servletContext.getNamedDispatcher( name );
  }

  @Deprecated
  public Servlet getServlet( String name ) throws ServletException {
    return servletContext.getServlet( name );
  }

  @Deprecated
  public Enumeration getServlets() {
    return servletContext.getServlets();
  }

  @Deprecated
  public Enumeration getServletNames() {
    return servletContext.getServletNames();
  }

  public void log( String msg ) {
    servletContext.log( msg );
  }

  @Deprecated
  public void log( Exception exception, String msg ) {
    servletContext.log( exception, msg );
  }

  @Deprecated
  public void log( String message, Throwable throwable ) {
    servletContext.log( message, throwable );
  }

  public String getRealPath( String path ) {
    return contextDirectory + path;
  }

  public String getServerInfo() {
    return servletContext.getServerInfo();
  }

  public String getInitParameter( String name ) {
    return servletContext.getInitParameter( name );
  }

  public Enumeration getInitParameterNames() {
    return servletContext.getInitParameterNames();
  }

  public Object getAttribute( String name ) {
    Object result;
    synchronized( attributes ) {
      if( isAttributeInWrappedContext( name ) ) {
        result = servletContext.getAttribute( name );
      } else {
        result = attributes.get( name );
      }
    }
    return result;
  }

  public Enumeration getAttributeNames() {
    Enumeration result;
    synchronized( attributes ) {
      result = servletContext.getAttributeNames();
      if( needEnumerationFromLocalAttributeBuffer( result ) ) {
        result = createAttributeNamesEnumeration();
      }
    }
    return result;
  }

  private boolean needEnumerationFromLocalAttributeBuffer( Enumeration result ) {
    return ( result == null || !result.hasMoreElements() ) && !attributes.isEmpty();
  }

  private Enumeration createAttributeNamesEnumeration() {
    return new Enumeration< Object >() {
      Iterator< String > names = attributes.keySet().iterator();

      public boolean hasMoreElements() {
        return names.hasNext();
      }

      public Object nextElement() {
        return names.next();
      }
    };
  }

  public void setAttribute( String name, Object object ) {
    synchronized( attributes ) {
      servletContext.setAttribute( name, object );
      if( !isAttributeInWrappedContext( name ) ) {
        attributes.put( name, object );
      }
    }
  }

  public void removeAttribute( String name ) {
    synchronized( attributes ) {
      if( isAttributeInWrappedContext( name ) ) {
        servletContext.removeAttribute( name );
      } else {
        attributes.remove( name );
      }
    }
  }

  private boolean isAttributeInWrappedContext( String name ) {
    return null != servletContext.getAttribute( name );
  }
  
  public String getServletContextName() {
    return servletContext.getServletContextName();
  }
}