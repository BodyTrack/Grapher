package org.bodytrack.client;

/**
 * <p>
 * <code>SequenceNumber</code> produces an <code>int</code> sequence number, starting at 1.  The sequence number resets
 * to 1 after reaching {@link Integer#MAX_VALUE}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class SequenceNumber {
   private static final SequenceNumber INSTANCE = new SequenceNumber();

   public static int getNext() {
      if (INSTANCE.id == Integer.MAX_VALUE) {
         INSTANCE.id = 0;
      }
      return ++INSTANCE.id;
   }

   private int id = 0;

   private SequenceNumber() {
      // private to prevent instantiation
   }
}
