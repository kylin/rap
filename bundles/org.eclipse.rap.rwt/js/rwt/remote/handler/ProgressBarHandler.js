/*******************************************************************************
 * Copyright (c) 2011, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.remote.HandlerRegistry.add( "rwt.widgets.ProgressBar", {

  factory : function( properties ) {
    var result = new rwt.widgets.ProgressBar();
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    result.setUserData( "isControl", true );
    rwt.remote.HandlerUtil.setParent( result, properties.parent );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getControlDestructor(),

  getDestroyableChildren : rwt.remote.HandlerUtil.getDestroyableChildrenFinder(),

  properties : rwt.remote.HandlerUtil.extendControlProperties( [
    // [if] Important: Order matters - minimum, maximum, selection
    "minimum",
    "maximum",
    "selection",
    "state"
  ] ),

  propertyHandler : rwt.remote.HandlerUtil.extendControlPropertyHandler( {
    // Overrides original backgroundImage handler
    "backgroundImage" : function( widget, value ) {
      if( value === null ) {
        widget.resetBackgroundImageSized();
      } else {
        widget.setBackgroundImageSized( value );
      }
    }
  } ),

  listeners : rwt.remote.HandlerUtil.extendControlListeners( [] ),

  listenerHandler : rwt.remote.HandlerUtil.extendControlListenerHandler( {} ),

  methods: [ "addListener", "removeListener" ],

  methodHandler : rwt.remote.HandlerUtil.extendListenerMethodHandler( {} ),

  /**
   * @class RWT Scripting analoge to org.eclipse.swt.widgets.Progressbar
   * @name ProgressBar
   * @extends Control
   * @description The constructor is not public.
   * @since 2.2
   */
  scriptingMethods : rwt.remote.HandlerUtil.extendControlScriptingMethods(
    /** @lends Scale.prototype */
  {
     /**
      * @description Returns the 'selection', which is the receiver's position.
      * @return {int} the selection
      */
     getSelection : function() {
      return this._selection;
    },
     /**
      * @description Returns the maximum value which the receiver will allow.
      * @return {int} the maximum
      */
     getMaximum : function() {
      return this._maximum;
    },
     /**
      * @description Returns the minimum value which the receiver will allow.
      * @return {int} the minimum
      */
     getMinimum : function() {
      return this._minimum;
    }
  } )

} );
