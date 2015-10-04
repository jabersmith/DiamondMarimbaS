package jaysmith.dms.drawing

import android.graphics.{Canvas, Paint}
import android.util.SparseArray
import android.view.MotionEvent

import jaysmith.dms.Marimba

/** Contains the entire set of Diamonds in the marimba
  * currently displayed on screen, and tracks those
  * currently being touched by Pointers.
  *
  * @param marimba The marimba currently being displayed
  * @param square The square region of pixels in which the marimba is displayed
  */
class DiamondSet (marimba: Marimba, square: CentralSquare) {

  // a sequence of all the diamonds corresponding
  // to keys of the current marimba
  private val diamonds = for (i <- 0 until marimba.degree;
                              j <- 0 until marimba.degree;
                              distance = (square.xMax - square.xMin) / (marimba.degree * 2)) yield {
    val x = square.xMin + ((i + j) * distance)
    val y = square.yMid + ((i - j) * distance)
    new Diamond(marimba.keys(i)(j),
                x, y, distance, square.totalRegion)
  }

  // If any pointers (fingers) are currently pressed
  // down in the marimba area, this maps from Android-
  // assigned pointerIds to the Diamond the pointer is
  // currently within.
  private val pointerIdsToDiamonds = new SparseArray[Diamond]


  def draw(p: Paint, c: Canvas) = diamonds.foreach(_.draw(p, c))


  // Find out if a DOWN event happened within a Diamond. If it did,
  // press that diamond and save it in the pointerIds SparseArray.
  def press(event: MotionEvent, i: Int): Boolean = {
    pressIf(findDiamond(event.getX(i), event.getY(i)), event.getPointerId(i))
  }

  private def pressIf(d: Option[Diamond], pointerId: Int): Boolean = {
    for (diamond <- d){
      diamond.press()
      pointerIdsToDiamonds.put(pointerId, diamond)
    }
    d.isDefined
  }

  // Find out if a UP event happened within a Diamond. If it did,
  // release that diamond and clear the pointerId in question.
  def release(event: MotionEvent, i: Int): Boolean = {
    val pointerId = event.getPointerId(i)
    releaseIf(Option(pointerIdsToDiamonds.get(pointerId)), pointerId)
  }

  private def releaseIf(d: Option[Diamond], pointerId: Int): Boolean = {
    for (diamond <- d){
      diamond.release()
      pointerIdsToDiamonds.put(pointerId, null)
    }
    d.isDefined
  }

  // On a MOVE event, see if the move began or ended in a
  // Diamond.  If it did, press/release as appropriate.
  def move(event: MotionEvent, i: Int): Boolean = {
    val pointerId = event.getPointerId(i)
    val oldDiamond = Option(pointerIdsToDiamonds.get(pointerId))
    val newDiamond = findDiamond(event.getX(i), event.getY(i))

    // avoid churn and view-invalidation if the move didn't
    // cross the borders between diamonds
    if (oldDiamond == newDiamond) {
      false
    } else {
      releaseIf(oldDiamond, pointerId) ||
        pressIf(newDiamond, pointerId)
    }
  }

  // is a given (x,y) location within one of the marimba's
  // current diamonds?
  private def findDiamond(x: Float, y: Float): Option[Diamond] =
    diamonds.find(_.contains(x.toInt, y.toInt))

 }
