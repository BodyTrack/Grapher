package org.bodytrack.client;

/**
 * Deals with the public API between the GWT application and the rest
 * of the webpage.
 */
public final class NativeInterface {

	// Set to true so that setUpWrappers is called only once.
	// With threading, this would be unsafe, but there is no issue
	// here because JavaScript is single-threaded.
	private static boolean wrappersSet = false;

	// Used for the monotonically increasing ID on each object built
	// from the API
	@SuppressWarnings("unused") // Actually used in JavaScript
	private static int nextID;

	public static void setUpWrappers() {
		if (wrappersSet)
			return;
		wrappersSet = true;
		nextID = 1;

		setUpJavaScriptWrappers();
	}

	/**
	 * Initializes the wrapper objects for the JavaScript API.
	 */
	private static native void setUpJavaScriptWrappers() /*-{
		/// Private function to update and return the next ID
		///
		/// @return
		///		The next ID for an object
		var __getNextID = function() {
			var id = @org.bodytrack.client.NativeInterface::nextID;
			@org.bodytrack.client.NativeInterface::nextID = id + 1;
			return id;
		};

		/// Private function to create a new axis constructor
		///
		/// This is intended to be called only to create the NumberAxis and
		/// GraphAxis constructors.  It is declared as a var so that its
		/// scope will be local to the function that GWT creates to replace
		/// setUpWrappers.
		///
		/// @param axisConstructor
		///		The Java constructor that will be used for the backing axis
		/// @return
		///		A constructor that will build an axis when called, with the
		///		backing axis built using axisConstructor
		var __createAxisConstructor = function(axisConstructor) {
			return function(placeholder, orientation, range) {
				if (placeholder === undefined || orientation === undefined) {
					throw 'Must pass in placeholder and orientation';
				}

				if (orientation != 'horizontal' && orientation != 'vertical') {
					throw 'Orientation must be either "horizontal" or "vertical"';
				}

				if (!range) {
					range = {
						'min': -10,
						'max': 10
					};
				} else {
					if (!('min' in range)) {
						range.min = -10;
					}
					if (!('max' in range)) {
						range.max = 10;
					}
				}

				this.getPlaceholder = function() { return placeholder; };
				this.getOrientation = function() { return orientation; };
				this.__backingAxis = (function() {
					// Call an anonymous function to avoid later closures
					// keeping around any objects longer than necessary

					// Setup code
					var DEFAULT_WIDTH = 10;
					var placeholderElement = placeholder != null
						? $doc.getElementById(placeholder)
						: null;

					var axisMin = range.min;
					var axisMax = range.max;
					var isXAxis = orientation == 'horizontal';

					var axisWidth = null;
					var basis = null;
					if (isXAxis) {
						axisWidth = placeholderElement != null
							? placeholderElement.clientHeight
							: DEFAULT_WIDTH;
						basis = @org.bodytrack.client.Basis::xDownYRight;
					} else {
						axisWidth = placeholderElement != null
							? placeholderElement.clientWidth
							: DEFAULT_WIDTH;
						basis = @org.bodytrack.client.Basis::xRightYUp;
					}

					if (axisWidth == 0) {
						axisWidth = DEFAULT_WIDTH;
					}

					return axisConstructor(placeholder, axisMin, axisMax,
						basis, axisWidth, isXAxis);
				})();
				this.getMin = function() {
					return this.__backingAxis.@org.bodytrack.client.GraphAxis::getMin()();
				};
				this.getMax = function() {
					return this.__backingAxis.@org.bodytrack.client.GraphAxis::getMax()();
				};
				this.setRange = function(min, max) {
					// Exceptions for illegal min and max values are handled in Java
					this.__backingAxis.@org.bodytrack.client.GraphAxis::replaceBounds(DD)(min, max);
				};
				this.setSize = function (widthInPixels, heightInPixels, eventId) {
					this.__backingAxis.@org.bodytrack.client.GraphAxis::setSize(III)(widthInPixels, heightInPixels, eventId);
				};
				this.addAxisChangeListener = function (listenerFunction) {
					this.__backingAxis.@org.bodytrack.client.GraphAxis::addEventListener(Lcom/google/gwt/core/client/JavaScriptObject;)(listenerFunction);
				}
				this.removeAxisChangeListener = function (listenerFunction) {
					this.__backingAxis.@org.bodytrack.client.GraphAxis::removeEventListener(Lcom/google/gwt/core/client/JavaScriptObject;)(listenerFunction);
				}
				this.setTimeZoneMapping = function (mapping) {
					// TODO: Not currently implemented
				};
				this.localTimeToUTC = function(value){
					var timeZoneMap;
					if (this.__backingAxis.@org.bodytrack.client.GraphAxis::getTimeZoneMap()() == null)
						timeZoneMap = @org.bodytrack.client.ModalTimeZoneMap::new(Lorg/bodytrack/client/TimeZoneMap;Z)(@org.bodytrack.client.TimeZoneMap::IDENTITY_MAP, false);
					else
						timeZoneMap = @org.bodytrack.client.ModalTimeZoneMap::new(Lorg/bodytrack/client/TimeZoneMap;Z)(this.__backingAxis.@org.bodytrack.client.GraphAxis::getTimeZoneMap()(), false);
					return timeZoneMap.@org.bodytrack.client.ModalTimeZoneMap::reverseConvert(D)(value);
				}
				this.setCursorPosition = function(position){
					if (position == null)
						this.__backingAxis.@org.bodytrack.client.GraphAxis::setCursorPosition(Ljava/lang/Double;Ljava/lang/Integer;)(position,null);
					else
						this.__backingAxis.@org.bodytrack.client.GraphAxis::setCursorPosition(D)(position);
				}
				this.getCursorPosition = function(){
					var value = this.__backingAxis.@org.bodytrack.client.GraphAxis::getCursorPosition()();
					if (value == null)
						return null;
					else
						return value.@java.lang.Double::doubleValue()();
				}
				this.setMaxRange = function(min,max){
					this.__backingAxis.@org.bodytrack.client.GraphAxis::setMaxRange(DD)(min,max);
				}
				this.getScale = function(){
					return 1 / this.__backingAxis.@org.bodytrack.client.GraphAxis::getScale()();
				}
				this.id = __getNextID();
			};
		};

		/// Initializes a new NumberAxis object
		///
		/// @param placeholder
		///		The ID of a div in which this axis should go, or null
		/// @param orientation
		///		Either 'horizontal', for an X-axis, or 'vertical', for a Y-axis
		/// @param range
		///		Optional parameter: a dictionary with keys 'min' and 'max',
		///		representing the bounds on the axis.  The values on these keys
		///		must be numbers, with the max value greater than the min value.
		///		If this key is not present, a default range will be assigned
		$wnd.NumberAxis = __createAxisConstructor(@org.bodytrack.client.GraphAxis::new(Ljava/lang/String;DDLorg/bodytrack/client/Basis;DZ));

		/// Initializes a new DateAxis object
		///
		/// @param placeholder
		///		The ID of a div in which this axis should go, or null
		/// @param orientation
		///		Either 'horizontal', for an X-axis, or 'vertical', for a Y-axis
		/// @param range
		///		Optional parameter: a dictionary with keys 'min' and 'max',
		///		representing the bounds on the axis.  The values on these keys
		///		must be numbers, with the max value greater than the min value.
		///		If this key is not present, a default range will be assigned
		$wnd.DateAxis = __createAxisConstructor(@org.bodytrack.client.TimeGraphAxis::new(Ljava/lang/String;DDLorg/bodytrack/client/Basis;DZ));

		/// Initializes a new PhotoAxis object
		///
		/// @param placeholder
		///		The ID of a div in which this axis should go, or null
		/// @param orientation
		///		Either 'horizontal', for an X-axis, or 'vertical', for a Y-axis
		/// @param range
		///		Optional parameter: a dictionary with keys 'min' and 'max',
		///		representing the bounds on the axis.  The values on these keys
		///		must be numbers, with the max value greater than the min value.
		///		If this key is not present, a default range will be assigned
		$wnd.PhotoAxis = __createAxisConstructor(@org.bodytrack.client.PhotoGraphAxis::new(Ljava/lang/String;DDLorg/bodytrack/client/Basis;DZ));

		/// Initializes a new DataSeriesPlot object
		///
		/// @param datasource
		///		A function to be used as the data source for the new
		///		plot.  This parameter must not be null
		/// @param horizontalAxis
		///		An non-null axis with horizontal orientation
		/// @param verticalAxis
		///		A non-null axis with vertical orientation
		/// @param optionalParams
		///		Optional parameter: a dictionary of optional parameters.  Supported keys:
		///			localDisplay
		///				True to display all data as though datastore time were equal to local
		///				time, or false (default) to display data as the server presents it
		///			style
		///				A dictionary specifying the initial style of the new plot
		$wnd.DataSeriesPlot = function(datasource, horizontalAxis, verticalAxis, optionalParams) {
			if (datasource == null) {
				throw 'Must pass in datasource';
			}

			if (horizontalAxis == null || verticalAxis == null) {
				throw 'Must pass in both axes';
			}

			// Use the default localDisplay value of false if necessary
			var localDisplay = !!(optionalParams['localDisplay']); // !!undefined is false

			// create a default style if necessary
			var style = optionalParams['style'] || {}; // undefined || {} is {}

			this.datasource = datasource;
			this.getDatasource = function() {
				return this.datasource;
			};
			this.setDatasource = function(datasource) {
				this.datasource = datasource;
				this.__backingPlot.@org.bodytrack.client.DataSeriesPlot::setDatasource(Lcom/google/gwt/core/client/JavaScriptObject;)(datasource);
			};
			this.__backingPlot = @org.bodytrack.client.DataSeriesPlot::new(Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Z)(datasource, horizontalAxis, verticalAxis, style, localDisplay);
			this.getHorizontalAxis = function() {
				return this.__backingPlot.@org.bodytrack.client.DataSeriesPlot::getNativeXAxis()();
			};
			this.getVerticalAxis = function() {
				return this.__backingPlot.@org.bodytrack.client.DataSeriesPlot::getNativeYAxis()();
			};
			this.style = style;
			this.getStyle = function() {
				// stringify and then parse the style so that we return
				// a COPY of the style, so the user can't mutate the one we store
				return JSON.parse(JSON.stringify(this.style));
			};
			this.setStyle = function(new_style) {
				this.style = new_style;
				this.__backingPlot.@org.bodytrack.client.DataSeriesPlot::setStyle(Lcom/google/gwt/core/client/JavaScriptObject;)(new_style);
			};
			this.addDataPointListener = function(listenerFunction) {
				return this.__backingPlot.@org.bodytrack.client.DataSeriesPlot::addDataPointListener(Lcom/google/gwt/core/client/JavaScriptObject;)(listenerFunction);
			};
			this.removeDataPointListener = function(listenerFunction) {
				return this.__backingPlot.@org.bodytrack.client.DataSeriesPlot::removeDataPointListener(Lcom/google/gwt/core/client/JavaScriptObject;)(listenerFunction);
			};
			this.getSimpleStatistics = function(xMin, xMax) {
				return this.__backingPlot.@org.bodytrack.client.DataSeriesPlot::getSimpleStatistics(DD)(xMin, xMax);
			};
			this.getStatistics = function(xMin, xMax, fieldnames, afterload) {
				return this.__backingPlot.@org.bodytrack.client.DataSeriesPlot::getStatistics(DDLcom/google/gwt/core/client/JsArrayString;Lcom/google/gwt/core/client/JavaScriptObject;)(xMin, xMax, fieldnames, afterload);
			};
			this.doCursorClick = function(){
				this.__backingPlot.@org.bodytrack.client.Plot::doCursorClick()();
			}
			this.invalidateTiles = function(start,end){
				this.__backingPlot.@org.bodytrack.client.Plot::invalidateTilesForRange(DD)(start,end);
			}
			this.id = __getNextID();
		};

		/// Initializes a new PhotoSeriesPlot object
		///
		/// @param datasource
		///		A function to be used as the data source for the new
		///		plot.  This parameter must not be null
		/// @param horizontalAxis
		///		An non-null axis with horizontal orientation
		/// @param verticalAxis
		///		A non-null axis with vertical orientation
		/// @param userId
		///		The ID of the current logged-in user
		/// @param optionalParams
		///		Optional parameter: a dictionary of optional parameters.  Supported keys:
		///			localDisplay
		///				True to display all data as though datastore time were equal to local
		///				time, or false (default) to display data as the server presents it
		///			style
		///				The style that determines how the photos should be drawn
		$wnd.PhotoSeriesPlot = function(datasource, horizontalAxis, verticalAxis, userId, optionalParams) {
			if (datasource == null) {
				throw 'Must pass in datasource';
			}

			if (horizontalAxis == null || verticalAxis == null) {
				throw 'Must pass in both axes';
			}

			// Use the default localDisplay value of false if necessary
			var localDisplay = !!(optionalParams['localDisplay']); // !!undefined is false

			// create a default style if necessary
			var style = optionalParams['style'];
			if (style == null) {
				style = {
					"styles": [
						{"type": "photo"}
					],
					"comments": {"show": true}
				};
			}

			this.datasource = datasource;
			this.getDatasource = function() {
				return this.datasource;
			};
			this.setDatasource = function(datasource) {
				this.datasource = datasource;
				this.__backingPlot.@org.bodytrack.client.PhotoSeriesPlot::setDatasource(Lcom/google/gwt/core/client/JavaScriptObject;)(datasource);
			};
			this.__backingPlot = @org.bodytrack.client.PhotoSeriesPlot::new(Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;ILcom/google/gwt/core/client/JavaScriptObject;Z)(datasource, horizontalAxis, verticalAxis, userId, style, localDisplay);
			this.getHorizontalAxis = function() {
				return this.__backingPlot.@org.bodytrack.client.PhotoSeriesPlot::getNativeXAxis()();
			};
			this.getVerticalAxis = function() {
				return this.__backingPlot.@org.bodytrack.client.PhotoSeriesPlot::getNativeYAxis()();
			};
			this.style = style;
			this.getStyle = function () {
				// stringify and then parse the style so that we return
				// a COPY of the style, so the user can't mutate the one we store
				return JSON.parse(JSON.stringify(this.style));
			};
			this.setStyle = function (new_style) {
				this.style = new_style;
				this.__backingPlot.@org.bodytrack.client.PhotoSeriesPlot::setStyle(Lcom/google/gwt/core/client/JavaScriptObject;)(new_style);
			};
			this.addDataPointListener = function(listenerFunction) {
				return this.__backingPlot.@org.bodytrack.client.PhotoSeriesPlot::addDataPointListener(Lcom/google/gwt/core/client/JavaScriptObject;)(listenerFunction);
			};
			this.removeDataPointListener = function(listenerFunction) {
				return this.__backingPlot.@org.bodytrack.client.PhotoSeriesPlot::removeDataPointListener(Lcom/google/gwt/core/client/JavaScriptObject;)(listenerFunction);
			};
			this.doCursorClick = function(){
				this.__backingPlot.@org.bodytrack.client.Plot::doCursorClick()();
			}
			this.id = __getNextID();
		};

		$wnd.TimespanSeriesPlot = function(datasource, horizontalAxis, verticalAxis, optionalParams){
			if (datasource == null) {
				throw 'Must pass in datasource';
			}

			if (horizontalAxis == null || verticalAxis == null) {
				throw 'Must pass in both axes';
			}

			// Use the default localDisplay value of false if necessary
			var localDisplay = !!(optionalParams['localDisplay']); // !!undefined is false

			// create a default style if necessary
			var style = optionalParams['style'] || {}; // undefined || {} is {}

			this.datasource = datasource;
			this.getDatasource = function() {
				return this.datasource;
			};
			this.setDatasource = function(datasource) {
				this.datasource = datasource;
				this.__backingPlot.@org.bodytrack.client.TimespanSeriesPlot::setDatasource(Lcom/google/gwt/core/client/JavaScriptObject;)(datasource);
			};
			this.__backingPlot = @org.bodytrack.client.TimespanSeriesPlot::new(Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Z)(datasource, horizontalAxis, verticalAxis, style, localDisplay);
			this.getHorizontalAxis = function() {
				return this.__backingPlot.@org.bodytrack.client.TimespanSeriesPlot::getNativeXAxis()();
			};
			this.getVerticalAxis = function() {
				return this.__backingPlot.@org.bodytrack.client.TimespanSeriesPlot::getNativeYAxis()();
			};
			this.style = style;
			this.getStyle = function() {
				// stringify and then parse the style so that we return
				// a COPY of the style, so the user can't mutate the one we store
				return JSON.parse(JSON.stringify(this.style));
			};
			this.setStyle = function(new_style) {
				this.style = new_style;
				this.__backingPlot.@org.bodytrack.client.TimespanSeriesPlot::setStyle(Lcom/google/gwt/core/client/JavaScriptObject;)(new_style);
			};
			this.addDataPointListener = function(listenerFunction) {
				return this.__backingPlot.@org.bodytrack.client.TimespanSeriesPlot::addDataPointListener(Lcom/google/gwt/core/client/JavaScriptObject;)(listenerFunction);
			};
			this.removeDataPointListener = function(listenerFunction) {
				return this.__backingPlot.@org.bodytrack.client.TimespanSeriesPlot::removeDataPointListener(Lcom/google/gwt/core/client/JavaScriptObject;)(listenerFunction);
			};
			this.doCursorClick = function(){
				this.__backingPlot.@org.bodytrack.client.Plot::doCursorClick()();
			}
			this.id = __getNextID();
		}

		/// Defines the SequenceNumber object
		$wnd.SequenceNumber = function() {};
		$wnd.SequenceNumber.getNext = function() { return @org.bodytrack.client.SequenceNumber::getNext()(); };

		/// Initializes a new PlotContainer
		///
		/// @param placeholder
		///		The ID of a div in which this container should go, or null
		/// @param plots
		///		Optional parameter: an array of plots that act as the initial
		///		list of plots for this container
		$wnd.PlotContainer = function(placeholder, ignoreClickEvents, plots) {
			if (placeholder === undefined) {
				// It's OK to pass in a null placeholder, but calling
				// new PlotContainer() with no arguments isn't OK
				throw 'Must pass in placeholder';
			}

			if (plots === undefined || plots == null) {
				plots = [];
			}

			this.getPlaceholder = function() { return placeholder; };
			this.__backingPlotContainer = (function() {
				var widget = @org.bodytrack.client.SeriesPlotContainer::new(Ljava/lang/String;Z)(placeholder,ignoreClickEvents);
				for (var i = 0; i < plots.length; i++) {
					widget.@org.bodytrack.client.SeriesPlotContainer::addPlot(Lorg/bodytrack/client/Plot;)(plots[i].__backingPlot);
				}
				return widget;
			})();
			this.addPlot = function(plot) {
				if (plot === undefined) {
					throw 'The addPlot function requires one argument';
				}
				this.__backingPlotContainer.@org.bodytrack.client.SeriesPlotContainer::addPlot(Lorg/bodytrack/client/Plot;)(plot.__backingPlot);
			};
			this.removePlot = function(plot) {
				if (plot === undefined) {
					throw 'The removePlot function requires one argument';
				}
				this.__backingPlotContainer.@org.bodytrack.client.SeriesPlotContainer::removePlot(Lorg/bodytrack/client/Plot;)(plot.__backingPlot);
			};
			this.setSize = function(widthInPixels, heightInPixels, eventId) {
				this.__backingPlotContainer.@org.bodytrack.client.SeriesPlotContainer::setSize(III)(widthInPixels, heightInPixels, eventId);
			};
			this.id = __getNextID();
		};

		// This is a hack to get around GWT's asynchronous loading, which
		// ensures that GWT scripts load last
		if (!!($wnd.grapherLoad)) {
			$wnd.grapherLoad();
		}
	}-*/;
}
