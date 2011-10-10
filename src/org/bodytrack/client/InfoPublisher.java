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
		function NumberAxis(placeholder, orientation, range) {
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
			this.__backingAxis = function() {
				// Call an anonymous function to avoid later closures
				// keeping around any objects longer than necessary

				// Setup code
				var DEFAULT_WIDTH = 10;
				var placeholderElement = placeholder !== null
					? $doc.getElementById(placeholder)
					: null;

				var axisMin = range.min;
				var axisMax = range.max;
				var isXAxis = orientation == 'horizontal';

				var axisWidth = null;
				var basis = null;
				if (isXAxis) {
					axisWidth = placeholderElement !== null
						? placeholderElement.offsetHeight
						: DEFAULT_WIDTH;
					basis = @org.bodytrack.client.Basis::xDownYRight;
				} else {
					axisWidth = placeholderElement !== null
						? placeholderElement.offsetWidth
						: DEFAULT_WIDTH;
					basis = @org.bodytrack.client.Basis::xRightYUp;
				}

				return @org.bodytrack.client.GraphAxis::new(Ljava/lang/String;DDLorg/bodytrack/client/Basis;DZ)(placeholder, axisMin, axisMax, basis, axisWidth, isXAxis);
			}();
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
		}
	}-*/;
}
