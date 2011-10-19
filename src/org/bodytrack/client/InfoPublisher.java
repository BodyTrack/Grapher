package org.bodytrack.client;

/**
 * Publishes information to the page outside of the Grapher widget.  This
 * information is available outside of GWT and offers a consistent interface
 * to the rest of the page, regardless of how GWT compiles this widget.
 *
 * <p>This class deals with the public API between the GWT application
 * and the rest of the webpage.</p>
 */
public final class InfoPublisher {
	/**
	 * Initializes the wrapper objects for the JavaScript API.
	 */
	public static native void setUpWrappers() /*-{
		/// Private function to create a new axis constructor
		///
		/// This is intended to be called only to create the NumberAxis and
		/// GraphAxis constructors.  It is declared as a var so that its
		/// scope will be local to the function that GWT creates to replace
		/// setUpWrappers.
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
							? placeholderElement.offsetHeight
							: DEFAULT_WIDTH;
						basis = @org.bodytrack.client.Basis::xDownYRight;
					} else {
						axisWidth = placeholderElement != null
							? placeholderElement.offsetWidth
							: DEFAULT_WIDTH;
						basis = @org.bodytrack.client.Basis::xRightYUp;
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
			};
		}

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

		/// Initializes a new SeriesPlot object
		///
		/// @param datasource
		///		A function to be used as the data source for the new
		///		plot.  This parameter must not be null
		/// @param horizontalAxis
		///		An non-null axis with horizontal orientation
		/// @param verticalAxis
		///		A non-null axis with vertical orientation
		/// @param style
		///		Optional parameter: a dictionary specifying the style of
		///		the new plot
		$wnd.SeriesPlot = function(datasource, horizontalAxis, verticalAxis, style) {
			if (datasource == null) {
				throw 'Must pass in datasource';
			}

			if (horizontalAxis == null || verticalAxis == null) {
				throw 'Must pass in both axes';
			}

			// TODO: The following four conditionals are gross hacks!
			// Right now the DataPlot requires a Channel object, which has
			// a device and channel.  Without that information, it's
			// pretty much impossible to initialize a new DataPlot object.
			// These conditionals attempt to paper over that problem
			// by ensuring that there is always some device name and
			// channel name in style.
			if (style == null) {
				style = {};
			}
			if (!('device_name' in style)) {
				style.device_name = 'device';
			}
			if (!('channel_name' in style)) {
				style.channel_name = 'channel';
			}
			if (!('color' in style)) {
				style.color = 'black';
			}

			this.getDatasource = function() { return datasource; }
			this.__backingPlot = (function() {
				var MIN_LEVEL = -20; // TODO: Offer control to the plot creator?

				// TODO: Get the channel type from the style (only getting
				// the device and channel names right now)
				var channel = @org.bodytrack.client.Channel::new(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(style.device_name, style.channel_name, null);

				var color = @org.bodytrack.client.ColorUtils::buildColor(Ljava/lang/String;)(style.color);

				return @org.bodytrack.client.DataPlot::new(Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Lorg/bodytrack/client/Channel;ILgwt/g2d/client/graphics/Color;)(datasource, horizontalAxis, verticalAxis, channel, MIN_LEVEL, color);
			})();
			this.getHorizontalAxis = function() {
				return this.__backingPlot.@org.bodytrack.client.DataPlot::getNativeXAxis()();
			};
			this.setHorizontalAxis = function(axis) {
				this.__backingPlot.@org.bodytrack.client.DataPlot::setXAxis(Lcom/google/gwt/core/client/JavaScriptObject;)(axis);
			};
			this.getVerticalAxis = function() {
				return this.__backingPlot.@org.bodytrack.client.DataPlot::getNativeYAxis()();
			};
			this.setVerticalAxis = function(axis) {
				this.__backingPlot.@org.bodytrack.client.DataPlot::setYAxis(Lcom/google/gwt/core/client/JavaScriptObject;)(axis);
			};
			this.style = style;
			this.getStyle = function() { return this.style; }
			this.setStyle = function(new_style) {
				// TODO: Support changing the plot type, which requires
				// surgically changing the backing plot
				this.style = new_style;
			}
		};

		// TODO: This is a hack to get around GWT's asynchronous loading,
		// which ensures that GWT scripts load last
		if (!!($wnd.grapherLoad)) {
			$wnd.grapherLoad();
		}
	}-*/;
}
