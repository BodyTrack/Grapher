package org.bodytrack.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.canvas.dom.client.Context2d.TextAlign;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.dom.client.Touch;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
import com.googlecode.mgwt.dom.client.recognizer.pinch.PinchEvent;
import com.googlecode.mgwt.dom.client.recognizer.pinch.PinchHandler;
import com.google.gwt.user.client.Window.Navigator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SeriesPlotContainer extends BasePlotContainer {
   /**
    * The default loading message for this widget to show.  This class
    * never actually uses this value, but makes it available for classes
    * that use the loading API.
    */
   public static final String DEFAULT_LOADING_MESSAGE = "Loading...";

   /**
    * The maximum number of value messages that is <em>guaranteed</em>
    * to be displayed.  It is possible that, if there are more than
    * this many value messages, all will be displayed, but no guarantee
    * is made.  Also, no guarantee is made as to which value messages
    * will be displayed.
    *
    * <p>In order to guarantee display of a message, then, a caller of
    * {@link #addValueMessage(String)} should ensure that the number of
    * value messages, as returned by {@link #countValueMessages()}, is
    * less than this.  If this is not so, messages can always be removed
    * using {@link #removeValueMessage(int)}.</p>
    */
   public static final int VALUE_MESSAGES_CAPACITY = 4;

   private static final int INITIAL_MESSAGE_ID = 1;
   private static final CssColor LOADING_MSG_COLOR = ColorUtils.DARK_GRAY;
   private static final double LOADING_MSG_X_MARGIN = 5;
   private static final double LOADING_MSG_Y_MARGIN = 3;
   private static final double VALUE_MSG_X_MARGIN = 5;
   private static final double VALUE_MSG_Y_MARGIN = 3;
   private static final double VALUE_MSG_GAP = 2;
   private static final double TEXT_HEIGHT = 12;
   private static final double TEXT_LINE_WIDTH = 0.75;
   private static final int MAX_DRAG_CLICK_EVENT = 3;

   private final Canvas drawing;
   private TouchDelegate touchDelegateCanvas;
   private TouchDelegate touchDelegateRootPanel;

   // For the loading message API, which shows one message at a time
   // on the bottom left, without regard to width
   // Invariants: all IDs in loadingMessages conform to
   // INITIAL_MESSAGE_ID <= id < nextLoadingMessageId, with all IDs
   // unique, and there is never a null message string in loadingMessages
   private int nextLoadingMessageId;
   private final List<DisplayMessage> loadingMessages;

   // For the value message API, which shows multiple messages at a time
   // on the bottom right, with strict controls on size
   // Invariants: all IDs in valueMessages conform to
   // INITIAL_MESSAGE_ID <= id < nextValueMessageId, with all IDs
   // unique, and there is never a null message string in valueMessages
   private int nextValueMessageId;
   private final List<DisplayMessage> valueMessages;

   private int width;
   private int height;

   private Vector2 mouseDragLastPos;
   private Vector2 mouseDragStartPos;
   
   private Vector2 touchDragLastPos;
   
   private boolean mouseDownInside;
   private boolean firstPinch;
   
   private int previousPaintEventId = 0;

   @SuppressWarnings("unused")
   private final String placeholderElementId;
   
   public SeriesPlotContainer(final String placeholderElementId, boolean ignoreClickEvents) {
      //Log.debug("isMobile: " + Boolean.toString(isMobile()) + " - " + Navigator.getUserAgent());
      if (placeholderElementId == null) {
         throw new NullPointerException("The placeholder element ID cannot be null");
      }
      this.placeholderElementId = placeholderElementId;
      
      final RootPanel placeholderElement = RootPanel.get(placeholderElementId);
      this.width = placeholderElement.getElement().getClientWidth();
      this.height = placeholderElement.getElement().getClientHeight();
      drawing = Canvas.createIfSupported();
      
      touchDelegateCanvas = new TouchDelegate(drawing);
              
      placeholderElement.add(drawing);

      nextLoadingMessageId = INITIAL_MESSAGE_ID;
      loadingMessages = new ArrayList<DisplayMessage>();

      nextValueMessageId = INITIAL_MESSAGE_ID;
      valueMessages = new ArrayList<DisplayMessage>();
      mouseDownInside = false;

      if (!ignoreClickEvents) {
    	  drawing.addMouseDownHandler(new MouseDownHandler() {
    	         @Override
    	         public void onMouseDown(final MouseDownEvent event) {
    	            handleMouseDownEvent(event);
    	         }
    	  });
          
    	  drawing.addMouseUpHandler(new MouseUpHandler() {
    	         @Override
    	         public void onMouseUp(final MouseUpEvent event) {
    	            handleMouseUpEvent(event);
    	         }
    	  });

    	  RootPanel.get().addDomHandler(new MouseUpHandler(){
    		 @Override
    		 public void onMouseUp(MouseUpEvent event) {
                    handleWindowMouseUpEvent(event);
    				
    		 }
    	  }, MouseUpEvent.getType());
          
    	  RootPanel.get().addDomHandler(new MouseMoveHandler(){
        	 public void onMouseMove(final MouseMoveEvent event){
        	    handleWindowMouseMoveEvent(event);
        	 }
        	  
          }, MouseMoveEvent.getType());
          
          RootPanel.get().addDomHandler(new MouseOutHandler(){
    		 @Override
    		 public void onMouseOut(MouseOutEvent event) {
    		    handleWindowMouseOutHandler(event);		
    		 }	  
          }, MouseOutEvent.getType());
          
          RootPanel.get().addDomHandler(new MouseDownHandler(){
    		 @Override
    		 public void onMouseDown(MouseDownEvent event) {
    		    event.preventDefault();	
    		 }	  
          }, MouseDownEvent.getType());
      }
      
      drawing.addMouseMoveHandler(new MouseMoveHandler() {
         @Override
         public void onMouseMove(final MouseMoveEvent event) {
           handleMouseMoveEvent(event);
         }
      });
  
      drawing.addMouseOutHandler(new MouseOutHandler() {
         @Override
         public void onMouseOut(final MouseOutEvent event) {
            handleMouseOutEvent(event);
         }
      });
      
      drawing.addTouchStartHandler(new TouchStartHandler() {
         @Override
         public void onTouchStart(final TouchStartEvent event) {
            handleTouchStartEvent(event);
         }
      });

      drawing.addTouchEndHandler(new TouchEndHandler(){
         @Override
         public void onTouchEnd(final TouchEndEvent event) {
            handleTouchEndEvent(event);
         }
      });

      drawing.addTouchMoveHandler(new TouchMoveHandler(){
         @Override
         public void onTouchMove(final TouchMoveEvent event) {
            handleTouchMoveEvent(event);
         }
      });

      drawing.addTouchCancelHandler(new TouchCancelHandler(){
         @Override
         public void onTouchCancel(final TouchCancelEvent event) {
            handleTouchCancelEvent(event);
         }
      });

      touchDelegateCanvas.addPinchHandler(new PinchHandler(){
         @Override
         public void onPinch(final PinchEvent event) {
            handlePinchEvent(event);
         }
      });
   } 

   private boolean isMobile() {
    String userAgent = Navigator.getUserAgent();
    return (userAgent.contains("Android") 
            || userAgent.contains("webOS") 
            || userAgent.contains("iPhone")
            || userAgent.contains("iPad")
            || userAgent.contains("iPod")
            || userAgent.contains("BlackBerry")
            || userAgent.contains("Windows Phone"));
  };

   private void handlePinchEvent(final PinchEvent event) {
      if(firstPinch) {
         // The scale of the first pinch event is always wrong.
         // So we need to ignore the first one with a threshold.
         if(event.getScaleFactor() > 1.01 || event.getScaleFactor() < 0.99) {
            firstPinch = false;
         }
      } else {
         if(event.getScaleFactor() > 1.005 || event.getScaleFactor() < 0.995) {
            //Log.debug("handlePinchEvent: " + Double.toString(event.getScaleFactor()));
            final Vector2 pos = new Vector2(event.getX(), event.getY());
            for (final Plot plot : containedPlots) {
               //double panAmmount = (plot.getXAxis().getMax() - plot.getXAxis().getMin()) / 60 * event.getScaleFactor();
               plot.getXAxis().zoom(event.getScaleFactor(), plot.getXAxis().unproject(pos), SequenceNumber.getNextThrottled());
               //plot.getXAxis().uncheckedDrag(panAmmount, SequenceNumber.getNextThrottled());
               //plot.getXAxis().paint(SequenceNumber.getNextThrottled());
            }
         }
      }
   }

   private void handleTouchStartEvent(final TouchStartEvent event) {
      //Log.debug("handleTouchStartEvent: " + Integer.toString(event.getTouches().length()));
      firstPinch = true;
      touchDragLastPos = null;
      event.preventDefault();
   }
 
   private void handleTouchMoveEvent(final TouchMoveEvent event){
      //Log.debug("handleTouchMoveEvent: " + Integer.toString(event.getTouches().length()));
      Touch touch1 = event.getTouches().get(0);
      final Vector2 pos;
      if(event.getTouches().length() == 1) {
         pos = new Vector2(touch1.getScreenX(), touch1.getScreenY());
      } else {
         Touch touch2 = event.getTouches().get(1);
         pos = new Vector2((touch1.getScreenX()+touch2.getScreenX())/2, (touch1.getScreenY()+touch2.getScreenY())/2);
      }
      if (touchDragLastPos != null && touchDragLastPos.distanceSquared(pos) > 1) {
         for (final Plot plot : containedPlots) {
            plot.getXAxis().drag(touchDragLastPos, pos, false, SequenceNumber.getNextThrottled());
            plot.getYAxis().drag(touchDragLastPos, pos, false, SequenceNumber.getNextThrottled());
         }  
      }
      touchDragLastPos = pos;
      event.preventDefault();
   }
   
   private void handleTouchEndEvent(final TouchEndEvent event){
      //Log.debug("handleTouchEndEvent: " + Integer.toString(event.getTouches().length()));
      touchDragLastPos = null;
      event.preventDefault();
   }
   
   private void handleTouchCancelEvent(final TouchCancelEvent event){
      //Log.debug("handleTouchCancelEvent: " + Integer.toString(event.getTouches().length()));
      touchDragLastPos = null;
      event.preventDefault();
   }
   
   private void handleMouseDownEvent(final MouseDownEvent event) {
      mouseDragStartPos = new Vector2(event.getScreenX(), event.getScreenY());
      mouseDragLastPos = new Vector2(event.getScreenX(), event.getScreenY());
      mouseDownInside = true;
      event.preventDefault();
   }
   
   private void handleWindowMouseMoveEvent(final MouseMoveEvent event){
      final Vector2 pos = new Vector2(event.getScreenX(), event.getScreenY());
      // We can be dragging exactly one of: one or
      // more plots, the whole viewing window, and nothing
      if (mouseDragLastPos != null) {
         // We are either dragging either one or more plots,
         // or the whole viewing window. If there's one or more
         // highlighted plot, then just drag the axes
         // for those plots.  Otherwise, drag all axes.

         // build a set of the highlighted plots
         final Set<Plot> highlightedPlots = new HashSet<Plot>();
         for (final Plot plot : containedPlots) {
            if (plot.isHighlighted()) {
               highlightedPlots.add(plot);
            }
         }

         // determine whether we're dragging only the highlighted plots
         final Set<Plot> plots = (highlightedPlots.size() > 0) ? highlightedPlots : containedPlots;

         // build a Set of axes to eliminate dupes
         final Set<GraphAxis> axes = new HashSet<GraphAxis>();
         for (final Plot plot : plots) {
            axes.add(plot.getXAxis());
            axes.add(plot.getYAxis());
         }

         // drag the axes
         for (final GraphAxis axis : axes) {
            axis.drag(mouseDragLastPos, pos, false, SequenceNumber.getNextThrottled());
         }
         event.preventDefault();

         mouseDragLastPos = pos;
      }
   }
   
   private void handleMouseMoveEvent(final MouseMoveEvent event) {
      final Vector2 pos = new Vector2(event.getX(), event.getY());
      if (mouseDragLastPos == null) {
         // We are not dragging anything, so we just update the
         // highlighting on the plots and axes

         final Set<Plot> highlightedPlots = new HashSet<Plot>();
         for (final Plot plot : containedPlots) {
            if (plot.highlightIfNear(pos)) {
               highlightedPlots.add(plot);
            } else {
               plot.unhighlight();
            }
         }

         // Now we handle highlighting of the axes--first build a set of the unhighlighted plots
         final Set<Plot> unhighlightedPlots = new HashSet<Plot>(containedPlots);
         unhighlightedPlots.removeAll(highlightedPlots);

         // unhighlight the axes of the unhighlighted plots
         final Set<GraphAxis> unhighlightedAxes = new HashSet<GraphAxis>();
         for (final Plot plot : unhighlightedPlots) {
            unhighlightedAxes.add(plot.getXAxis());
            unhighlightedAxes.add(plot.getYAxis());
         }
         for (final GraphAxis axis : unhighlightedAxes) {
            axis.unhighlight();
         }

         // now highlight the axes of the highlighted plots
         for (final Plot plot : containedPlots) {
            final PlottablePoint highlightedPoint = plot.getHighlightedPoint();
            plot.getXAxis().highlight(highlightedPoint);
            plot.getYAxis().highlight(highlightedPoint);
         }
         event.preventDefault();

         paint(SequenceNumber.getNextThrottled());
      }
   }
   
   private void handleWindowMouseUpEvent(final MouseUpEvent event){
         mouseDownInside = false;
         if (mouseDragLastPos != null) {
            // We are either dragging either one or more plots,
	    // or the whole viewing window. If there's one or more
	    // highlighted plot, then just drag the axes
	    // for those plots.  Otherwise, drag all axes.
		
	    // build a set of the highlighted plots
	    final Set<Plot> highlightedPlots = new HashSet<Plot>();
	    for (final Plot plot : containedPlots) {
	    if (plot.isHighlighted()) {
               highlightedPlots.add(plot);
	    }
	 }
		
	 // determine whether we're dragging only the highlighted plots
	 final Set<Plot> plots = (highlightedPlots.size() > 0) ? highlightedPlots : containedPlots;
		
	 // build a Set of axes to eliminate dupes
	 final Set<GraphAxis> axes = new HashSet<GraphAxis>();
	 for (final Plot plot : plots) {
	    axes.add(plot.getXAxis());
	    axes.add(plot.getYAxis());
	 }
		
	 // drag the axes
	 for (final GraphAxis axis : axes) {
	    axis.drag(mouseDragLastPos, mouseDragLastPos, false, SequenceNumber.getNextThrottled());
	 }
         event.preventDefault();
      }
      mouseDragStartPos = null;
      mouseDragLastPos = null;   
   }
   
   private void handleMouseUpEvent(final MouseUpEvent event) {
      if (mouseDownInside)
         event.preventDefault();
      final Vector2 pos = new Vector2(event.getX(), event.getY());
      final Vector2 screenPos = new Vector2(event.getScreenX(),event.getScreenY());
      final boolean isClickEvent = mouseDownInside && ((mouseDragStartPos == null)
         || (screenPos.distanceSquared(mouseDragStartPos) <
               (MAX_DRAG_CLICK_EVENT * MAX_DRAG_CLICK_EVENT)));

      // Alert all the plots to the click event
      if (isClickEvent) {
         for (final Plot plot: containedPlots)
            plot.onClick(pos);
      }

      // Want guaranteed update for the axes
      paint(SequenceNumber.getNext());
   }
   
   private void handleWindowMouseOutHandler(final MouseOutEvent event){
      if (mouseDragLastPos != null) {
         // We are either dragging either one or more plots,
	 // or the whole viewing window. If there's one or more
	 // highlighted plot, then just drag the axes
	 // for those plots.  Otherwise, drag all axes.
		
	 // build a set of the highlighted plots
	 final Set<Plot> highlightedPlots = new HashSet<Plot>();
	 for (final Plot plot : containedPlots) {
	    if (plot.isHighlighted()) {
	       highlightedPlots.add(plot);
	    }
	 }
		
	 // determine whether we're dragging only the highlighted plots
	 final Set<Plot> plots = (highlightedPlots.size() > 0) ? highlightedPlots : containedPlots;
		
	 // build a Set of axes to eliminate dupes
	 final Set<GraphAxis> axes = new HashSet<GraphAxis>();
	 for (final Plot plot : plots) {
	    axes.add(plot.getXAxis());
	    axes.add(plot.getYAxis());
	 }
		
	 // drag the axes
	 for (final GraphAxis axis : axes) {
	    axis.drag(mouseDragLastPos, mouseDragLastPos, false, SequenceNumber.getNextThrottled());
	 }
	 event.preventDefault();
      }
      mouseDownInside = false;
      mouseDragLastPos = null;
      mouseDragStartPos = null;
   }
 
   private void handleMouseOutEvent(final MouseOutEvent event) {
      // Ensure that all plots are unhighlighted, as are all axes
      for (final Plot plot : containedPlots) {
         plot.unhighlight();
         plot.getXAxis().unhighlight();
         plot.getYAxis().unhighlight();
      }

      // Want guaranteed update for the axes
      paint(SequenceNumber.getNext());
   }
   
   private void layout() {
      final Set<GraphAxis> axes = new HashSet<GraphAxis>();
      for (final Plot plot : containedPlots) {
         axes.add(plot.getXAxis());
         axes.add(plot.getYAxis());
      }
      for (final GraphAxis axis : axes) {
         axis.layout();
      }
   }

   @Override
   public void setSize(final int widthInPixels, final int heightInPixels, final int newPaintEventId) {
      final Element canvas = drawing.getElement();

      if ((canvas.getClientWidth() != widthInPixels) ||
          (canvas.getClientHeight() != heightInPixels)) {
    	  height = heightInPixels;
    	  width = widthInPixels;
    	  drawing.setCoordinateSpaceHeight(height);
    	  drawing.setCoordinateSpaceWidth(width);
         paint(newPaintEventId);
      }
   }

   public void paint(final int newPaintEventId) {
      // guard against redundant paints
      if (previousPaintEventId != newPaintEventId) {
         previousPaintEventId = newPaintEventId;

         layout();
         Context2d context = drawing.getContext2d();
         context.clearRect(0,0,drawing.getCoordinateSpaceWidth(),drawing.getCoordinateSpaceHeight());
         context.save();
         context.translate(.5, .5);

         // Draw any Loading... messages that might be requested
         if (loadingMessages.size() > 0) {
            showLoadingMessage(loadingMessages.get(0));
         }

         // Draw any value messages that might be requested
         if (valueMessages.size() > 0) {
            // We use the first (oldest) VALUE_MESSAGES_CAPACITY
            // messages in valueMessages, at least for now
            final int numMessages = Math.min(VALUE_MESSAGES_CAPACITY,
                                             valueMessages.size());

            showValueMessages(valueMessages.subList(0, numMessages));
         }

         // Now draw the data
         final GrapherCanvas canvas = GrapherCanvas.buildCanvas(drawing);
         for (final Plot plot : containedPlots) {
            plot.paint(canvas, newPaintEventId);
         }

         // Draw the axes
         // This code freezes Chrome on mobile devices while zooming 
         //for (final Plot plot : containedPlots) {
            //plot.getYAxis().paint(newPaintEventId);
            //plot.getXAxis().paint(newPaintEventId);
         //}

         context.restore();
      }
   }

   /**
    * Shows the specified loading message.
    *
    * @param msg
    * 		a message to be shown at the bottom left corner of
    * 		the grapher.  Note that we require that msg is not
    * 		<tt>null</tt>
    */
   private void showLoadingMessage(DisplayMessage msg) {
      // Save old data to be restored later
	  Context2d context = drawing.getContext2d();
      TextAlign oldTextAlign = TextAlign.valueOf(context.getTextAlign());
      double oldLineWidth = context.getLineWidth();

      // Change settings
      context.setTextAlign(TextAlign.LEFT);
      context.setLineWidth(TEXT_LINE_WIDTH);
      context.setStrokeStyle(msg.getColor());

      // Actually write the text
      double bottom = height - LOADING_MSG_Y_MARGIN;
      double textTop = bottom - TEXT_HEIGHT;
      context.strokeText(msg.getText(), LOADING_MSG_X_MARGIN, textTop);

      // Restore old settings
      context.setTextAlign(oldTextAlign);
      context.setLineWidth(oldLineWidth);
      context.setStrokeStyle(GrapherCanvas.DEFAULT_COLOR);
   }

   /**
    * Shows the specified value messages.
    *
    * @param messages
    * 		a list of messages to be shown at the bottom right
    * 		of the grapher.  Note that we require that messages
    * 		is neither <tt>null</tt> nor empty, contains no
    * 		<tt>null</tt> elements, and has length less than or
    * 		equal to {@link #VALUE_MESSAGES_CAPACITY}.
    */
   private void showValueMessages(List<DisplayMessage> messages) {
      // Save old data to be restored later
	   Context2d context = drawing.getContext2d();
      TextAlign oldTextAlign = TextAlign.valueOf(context.getTextAlign());
      double oldLineWidth = context.getLineWidth();

      // Change settings
      context.setTextAlign(TextAlign.RIGHT);
      context.setLineWidth(TEXT_LINE_WIDTH);

      // Actually write the text
      double bottom = height - VALUE_MSG_Y_MARGIN;
      double x = width - VALUE_MSG_X_MARGIN;
      // Right edge X-value with right text alignment

      for (DisplayMessage msg : messages) {
    	  context.setStrokeStyle(msg.getColor());

         double textTop = bottom - TEXT_HEIGHT;
         String text = msg.getText();

         // Find left edge, given that we know right edge
         context.strokeText(text, x, textTop);

         // Move upwards for next loop iteration
         bottom = textTop - VALUE_MSG_GAP;
         textTop = bottom - TEXT_HEIGHT;
      }

      // Restore old settings
      context.setTextAlign(oldTextAlign);
      context.setLineWidth(oldLineWidth);
      context.setStrokeStyle(GrapherCanvas.DEFAULT_COLOR);
   }

   /**
    * Adds a loading message
    *
    * <p>Note that we implement a FIFO policy for displaying loading
    * messages.  This means that messages that are posted early will
    * display until removed.</p>
    *
    * <p>This system of assigning integer IDs to messages and then
    * removing messages by ID is necessitated by the fact that data
    * is not required to come into the page in the same order
    * that it is requested.  A simple queue system thus doesn't
    * work for removal purposes.</p>
    *
    * @param message
    * 		the message to display whenever there are no older
    * 		messages left in the queue
    * @return
    * 		an integer ID that can be used to remove message whenever
    * 		it should no longer appear
    * @throws NullPointerException
    * 		if message is <tt>null</tt>
    * @see #removeLoadingMessage(int)
    */
   public final int addLoadingMessage(String message) {
      if (message == null) {
         throw new NullPointerException(
               "Null loading message not allowed");
      }

      int id = nextLoadingMessageId;
      nextLoadingMessageId++;

      loadingMessages.add(
            new DisplayMessage(id, message, LOADING_MSG_COLOR));

      return id;
   }

   /**
    * Removes the specified message from the queue of loading messages
    * to show.
    *
    * <p>It is guaranteed that this method will remove either 0
    * or 1 messages from the set of messages that could be shown.</p>
    *
    * @param messageId
    * 		the ID of the message to remove
    * @return
    * 		the string that was previously associated with messageId,
    * 		at least if messageId is in the list.  If messageId is
    * 		not in the list, returns <tt>null</tt>.
    * @see #addLoadingMessage(String)
    */
   public final String removeLoadingMessage(int messageId) {
      // Since IDs are assigned sequentially, starting at
      // INITIAL_MESSAGE_ID, this allows us to avoid a search
      // in some cases
      if (messageId < INITIAL_MESSAGE_ID ||
          messageId >= nextLoadingMessageId) {
         return null;
      }

      return removeMessage(loadingMessages, messageId);
   }

   /**
    * Removes the message with the specified ID from messages, if
    * such a message exists.  Otherwise, does nothing
    *
    * @param messages
    * 		the list of messages from which to remove a message
    * @param messageId
    * 		the ID of the message to remove from messages
    * @return
    * 		the string message that was removed from messages, or
    * 		<tt>null</tt> if there is no message in messages with
    * 		the messageId
    */
   private static String removeMessage(List<DisplayMessage> messages,
                                       int messageId) {
      Iterator<DisplayMessage> it = messages.iterator();

      String result = null;

      while (it.hasNext()) {
         DisplayMessage curr = it.next();

         if (curr.getId() == messageId) {
            result = curr.getText();
            it.remove();
         }
      }

      return result;
   }

   /**
    * Adds a value message
    *
    * <p>Note that we try to display all value messages, and indeed
    * guarantee that we will display all of them, as long as there
    * are less than or equal to {@link #VALUE_MESSAGES_CAPACITY}.
    * If there are more than this many messages, it is still possible
    * that all the messages will be drawn, but no guarantees are made
    * to which value messages will be drawn, whether some or all.
    * The count of value messages is always available through a call
    * to {@link #countValueMessages()}.</p>
    *
    * @param message
    * 		the value message to display if possible
    * @return
    * 		an integer ID that can be used to remove message whenever
    * 		it should no longer appear
    * @throws NullPointerException
    * 		if message or color is <tt>null</tt>
    * @see #removeValueMessage(int)
    */
   public final int addValueMessage(String message, CssColor color) {
      if (message == null) {
         throw new NullPointerException(
               "Null value message not allowed");
      }

      if (color == null) {
         throw new NullPointerException("Null color not allowed");
      }

      int id = nextValueMessageId;
      nextValueMessageId++;

      valueMessages.add(new DisplayMessage(id, message, color));

      return id;
   }

   /**
    * Removes the specified message from the list of value messages
    * to show.
    *
    * <p>It is guaranteed that this method will remove either 0
    * or 1 messages from the set of messages that could be shown.</p>
    *
    * @param messageId
    * 		the ID of the message to remove
    * @return
    * 		the string that was previously associated with messageId,
    * 		at least if messageId is in the list.  If messageId is
    * 		not in the list, returns <tt>null</tt>.
    * @see #addValueMessage(String)
    */
   public final String removeValueMessage(int messageId) {
      // Since IDs are assigned sequentially, starting at
      // INITIAL_MESSAGE_ID, this allows us to avoid a search
      // in some cases
      if (messageId < INITIAL_MESSAGE_ID ||
          messageId >= nextValueMessageId) {
         return null;
      }

      return removeMessage(valueMessages, messageId);
   }

   /**
    * Returns the number of value messages currently held in this
    * <tt>PlotContainer</tt>.
    *
    * @return
    * 		the number of messages that have been added using
    * 		{@link #addValueMessage(String)} but have not been
    * 		removed using {@link #removeValueMessage(int)}
    */
   public final int countValueMessages() {
      return valueMessages.size();
   }
   
   public int getHeight(){
	   return height;
   }
   
   public int getWidth(){
	   return width;
   }

   /**
    * A class representing a message ID and loading message.
    *
    * <p>Note that objects of this class are immutable and therefore
    * unconditionally thread-safe.  Or at least, in Java this holds.
    * In JavaScript every object is mutable, and there is only one
    * thread, so that doesn't really hold anymore.</p>
    */
   private static final class DisplayMessage
         implements Comparable<DisplayMessage> {
      private final int id;
      private final String text;
      private final CssColor color;

      /**
       * Creates a new {@link DisplayMessage DisplayMessage} object
       *
       * @param id
       * 		the ID this object should hold
       * @param text
       * 		the message this object should hold
       * @param color
       * 		the color at which the message should be drawn
       * @throws NullPointerException
       * 		if either text or color is <tt>null</tt>
       */
      public DisplayMessage(int id, String text, CssColor color) {
         if (text == null || color == null) {
            throw new NullPointerException(
                  "Null constructor parameter not allowed");
         }

         this.id = id;
         this.text = text;
         this.color = color;
      }

      /**
       * Returns the ID passed to this object's constructor when it
       * was created.
       *
       * @return
       * 		the ID this object holds
       */
      public int getId() {
         return id;
      }

      /**
       * Returns the message parameter passed to this object's
       * constructor when it was created.
       *
       * @return
       * 		the message this object holds
       */
      public String getText() {
         return text;
      }

      /**
       * Returns the color parameter passed to this object's
       * constructor when it was created.
       *
       * @return
       * 		the color this object holds
       */
      public CssColor getColor() {
         return color;
      }

      /**
       * Computes a hash code for this object.
       */
      @Override
      public int hashCode() {
         return id;
      }

      /**
       * Tells if this is logically equal to o.  Note that, if o
       * is not a DisplayMessage, then this and o must be unequal.
       * Note that equality is determined by ID.
       */
      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         }
         if (!(o instanceof DisplayMessage)) {
            return false;
         }
         DisplayMessage other = (DisplayMessage)o;
         return compareTo(other) == 0;
      }

      /**
       * Compares this to other.  Note that comparison is by ID.
       */
      @Override
      public int compareTo(DisplayMessage other) {
         if (id > other.id) {
            return 1;
         }
         if (id < other.id) {
            return -1;
         }
         return 0;
      }
   }
}
