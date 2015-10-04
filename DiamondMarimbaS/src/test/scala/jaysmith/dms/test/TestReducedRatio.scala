package jaysmith.dms.test

import org.junit.Test
import org.junit.Assert.{assertEquals,assertTrue}

import jaysmith.dms.ReducedRatio

object TestReducedRatio {
  def assertRatio (expected: (Int, Int), ratio: ReducedRatio) =
  {
    assertEquals(expected, (ratio.numerator, ratio.denominator))
    assertTrue((ratio.value >= 1) && (ratio.value < 2))
  }
}

class TestReducedRatio {
  import TestReducedRatio.assertRatio

  val intCases = List(((1, 1), (1, 1)),
                      ((3, 1), (3, 2)),
                      ((1, 3), (4, 3)),
                      ((5, 1), (5, 4)),
                      ((1, 5), (8, 5)),
                      ((7, 1), (7, 4)),
                      ((1, 7), (8, 7)),
                      ((2, 2), (1, 1)),
                      ((2, 1), (1, 1)))

  @Test
  def testIntRatios() {
    for (c <- intCases) {
      val input = c._1
      val output = c._2

      val ratio = new ReducedRatio(input._1, input._2)
      assertRatio(output, ratio)
    }
  }


  val compoundCases = List((((3, 1), (1, 3)), (1, 1)),
                           (((3, 1), (1, 5)), (6, 5)),
                           (((5, 1), (1, 3)), (5, 3)))

  def testCompoundRatios() {
    for (c <- compoundCases) {
      val input = c._1
      val output = c._2

      val numerator = new ReducedRatio(input._1._1, input._1._2)
      val denominator = new ReducedRatio(input._2._1, input._2._2)
      val ratio = new ReducedRatio(numerator, denominator)
      assertRatio(output, ratio)
    }
  }
}
