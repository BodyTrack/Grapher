package org.bodytrack.client;

import junit.framework.TestCase;

import org.junit.Test;

public class GraphAxisTest extends TestCase {
   @Test
   public void test_ticks() {
      /* TODO: Re-enable tests
      final GraphAxis g = new GraphAxis(null,
            0, 1, // min, max
            Basis.xDownYRight,
            5, // width
            true
      );

      g.layout();

      final double epsilon = 1e-10;

      assertEquals(0.1, g.computeTickSize(9), epsilon);
      assertEquals(0.1, g.computeTickSize(10), epsilon);
      assertEquals(0.2, g.computeTickSize(11), epsilon);

      assertEquals(0.2, g.computeTickSize(19), epsilon);
      assertEquals(0.2, g.computeTickSize(20), epsilon);
      assertEquals(0.5, g.computeTickSize(21), epsilon);

      assertEquals(0.5, g.computeTickSize(49), epsilon);
      assertEquals(0.5, g.computeTickSize(50), epsilon);
      assertEquals(1.0, g.computeTickSize(51), epsilon);

      assertEquals(1.0, g.computeTickSize(99), epsilon);
      assertEquals(1.0, g.computeTickSize(100), epsilon);
      */
   }
}
