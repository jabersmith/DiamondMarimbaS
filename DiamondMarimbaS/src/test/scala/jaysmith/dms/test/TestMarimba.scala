package jaysmith.dms.test

import org.junit.Test

import jaysmith.dms.Marimba
import jaysmith.dms.test.TestReducedRatio.assertRatio

class TestMarimba{

  def testMarimba(degree: Int, expected: Vector[Vector[(Int, Int)]]) = {
      val keys = new Marimba(degree).keys

      var i = 0
      while (i < degree) {
        var j = 0
        while (j < degree) {
          assertRatio(expected(i)(j), keys(i)(j).ratio)
          j += 1
        }
        i += 1
      }
  }

  @Test
  def testTwoDegreeMarimba() = {
    val expected = Vector(Vector((1, 1), (3, 2)),
                          Vector((4, 3), (1, 1)))

    testMarimba(2, expected)
  }

  @Test
  def testThreeDegreeMarimba() = {
    val expected = Vector(Vector((1, 1), (5, 4), (3, 2)),
                          Vector((8, 5), (1, 1), (6, 5)),
                          Vector((4, 3), (5, 3), (1, 1)))

    testMarimba(3, expected)
  }

  @Test
  def testFourDegreeMarimba() = {
    val expected = Vector(Vector((1, 1), (5, 4), (3, 2), (7, 4)),
                          Vector((8, 5), (1, 1), (6, 5), (7, 5)),
                          Vector((4, 3), (5, 3), (1, 1), (7, 6)),
                          Vector((8, 7),(10, 7),(12, 7), (1, 1)))

    testMarimba(4, expected)
  }

  @Test
  def testSixDegreeMarimba() = {
    val expected = Vector(Vector(( 1,  1), ( 9,  8), ( 5,  4), (11,  8), ( 3,  2), ( 7,  4)),
                          Vector((16,  9), ( 1,  1), (10,  9), (11,  9), ( 4,  3), (14,  9)),
                          Vector(( 8,  5), ( 9,  5), ( 1,  1), (11, 10), ( 6,  5), ( 7,  5)),
                          Vector((16, 11), (18, 11), (20, 11), ( 1,  1), (12, 11), (14, 11)),
                          Vector(( 4,  3), ( 3,  2), ( 5,  3), (11,  6), ( 1,  1), ( 7,  6)),
                          Vector(( 8,  7), ( 9,  7), (10,  7), (11,  7), (12,  7), ( 1,  1)))

    testMarimba(6, expected)
  }
}
