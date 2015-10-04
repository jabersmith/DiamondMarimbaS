package jaysmith.dms.drawing

import android.graphics.{Path, Region}


/** defines a square, centered in the View's display area,
  * in which the marimba and surrounding elements can be
  * drawn.  Exposes some simple geometrical values related
  * to the square that the View will use repeatedly.
  *
  * @param w the width of the enclosing View
  * @param h the height of the enclosing View
  */
class CentralSquare (w: Int, h: Int) {

  /** The four corners of the square */
  val (xMin, xMax, yMin, yMax) = {
    if (h >= w) {
      val yMin = (h - w) / 2
      (0, w, yMin, yMin + w)
    } else {
      val xMin = (w - h) / 2
      (xMin, xMin + h, 0, h)
    }
  }

  /** The x coordinate halfway between xMin and xMax */
  val xMid = (xMin + xMax) / 2

  /** The y coordinate halfway between yMin and yMax */
  val yMid = (yMin + yMax) / 2

  /** The android Region covering the entire square -- used for clipping */
  val totalRegion = new Region(xMin, yMin, xMax, yMax)

  /** The android Region covering the upper left corner of the
    * square -- defines a clickable button, currently used for
    * increasing the degree of the marimba
    */
  // (We could add regions for the other corners if needed,
  // but for now, only the upper left is clickable)
  val upperLeft: Region = {
    val path = new Path
    path.moveTo(xMin, yMin)
    path.lineTo(xMid, yMin)
    path.lineTo(xMin, yMid)
    path.lineTo(xMin, yMin)
    val r = new Region
    r.setPath(path, totalRegion)
    r
  }
}
