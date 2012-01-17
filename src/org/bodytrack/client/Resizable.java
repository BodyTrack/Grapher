package org.bodytrack.client;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface Resizable {
   void setSize(int widthInPixels, int heightInPixels, int newPaintEventId);
}