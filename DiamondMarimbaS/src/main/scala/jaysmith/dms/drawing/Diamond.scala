package jaysmith.dms.drawing

import android.graphics.{Canvas, Color, Paint, Path, Region}
import android.os.SystemClock

import jaysmith.dms.Key

/** Represents an individual key of the marimba as drawn onscreen.
  * Reponsible for tracking the geometry of the key, its visual
  * appearance, and detecting presses and long-presses of the
  * key.
  *
  * @param key the abstract object for this key
  * @param x horizontal position of the leftmost corner of the diamond
  * @param y vertical position of the leftmost corner of the diamond
  * @param distance vertical/horizontal distance (in pixels) from any
  *                 corner of the diamond to its center
  * @param clip region of the entire square in which the marimba is drawn;
  *             needed for creating sub-regions
  */
class Diamond (val key: Key, x: Int, y: Int, distance: Int, clip: Region) {

  // the Path that forms the borders of the diamond
  private val path = {
    val path = new Path
    path.moveTo(x, y)
    path.lineTo(x + distance, y - distance)
    path.lineTo(x + (2 * distance), y)
    path.lineTo(x + distance, y + distance)
    path.lineTo(x, y)
    path
  }

  /** the Region that covers the diamond's area -- used
    * for detecting pointer actions in the diamond.
    */
  val region = {
    val region = new Region()
    region.setPath(path, clip)
    region
  }

  /** Is a given pixel-location on the screen contained
    * within the diamond?
    * 
    * @param x the horizontal co-ordinate of the point to be tested
    * @param y the vertical co-ordinate of the point to be tested
    * @return true if the point is contained in this diamond,
   *          false if it is not
    */
  def contains(x: Int, y: Int) = region.contains(x, y)

  /** draw the diamond on the supplied Canvas
    * 
    * @param paint the android Paint object
    * @param canvas the android Canvas on which to draw
    */
  def draw(paint: Paint, canvas: Canvas) = {
    // "active" diamonds are filled with the color
    // associated with their ratio
    if (key.active) {
      paint.setColor(Diamond.colorMap(key.ratio.value))
      paint.setStyle(Paint.Style.FILL)
      canvas.drawPath(path, paint)
    }

    // and all diamonds are outlined in white
    paint.setColor(Color.WHITE)
    paint.setStyle(Paint.Style.STROKE)
    canvas.drawPath(path, paint)
  }



  // multiple pointers/fingers can be in a Diamond
  // at the same time, so we increment/decrement this
  // counter to keep track, and turn off the key only
  // when the counter goes to zero.
  private var counter: Int = 0

  // Because our "buttons" are not rectangular, they
  // can't be actual Android buttons, or any other
  // View subclass.  So we have to provide our own
  // long-press detection, rather than using Android's
  // gesture code.  Fair enough; we consider any
  // uninterrupted period of longer than a second
  // (LongTouchInterval milliseconds) in which at
  // least one pointer is within the diamond to be
  // a long-press.  (It's possible that during that
  // period, there's not a single long press, but an
  // overlapping series of short presses by different
  // pointers/fingers, but we'll treat that as a
  // long press for these purposes.)
  // After a long-press is released, the diamond's
  // note continues to play until the diamond is
  // pressed again (and released in less than a
  // second).
  private var lastPressed: Long = 0L
  private val LongTouchInterval: Long = 1000L

  /** process a touch event (down or move) that enters the diamond */
  def press() = {
    if (counter == 0) {
      key.active = true
      lastPressed = SystemClock.uptimeMillis
    }
    counter += 1
  }

  /** process a touch event (up or move) that leaves the diamond */
  def release() = {
    counter -= 1
    if (counter == 0) {
      if ((SystemClock.uptimeMillis - lastPressed) < LongTouchInterval) {
        key.active = false
        lastPressed = 0
      }
    }
  }
}

/** Companion object for the Diamond UI object.  Contains a utility
  * method for mapping ratios into colors for display.
  */
object Diamond {

  /** Map a ratio, in the 1-to-2 single-octave range, into a
    * hue in the Android HSV model, and then convert that into
    * a displayable Color value.  This color will be used both
    * to paint the corresponding diamond when it's active, and
    * to display numerical information about the ratio below
    * the marimba; the colors are pretty arbitrary, beyond
    * providing a way to associate each diamond with its
    * corresponding data line.
    *
    * I suspect that there are mappings that would provide a
    * nicer mapping here -- the hue range seems to be an
    * essentially linear spread of the colorspace across
    * 0-360, while the pitch-ratios are geometric.  But
    * this works for now.
    *
    * @param ratio a ratio value, as computed by ReducedRatio, in the range [1,2)
    * @return an integer in the
    */
  def colorMap(ratio: Double): Int = {
    val hue: Float = ((ratio - 1) * 360).toFloat
    Color.HSVToColor(Array(hue, 1.0f, 1.0f))
  }
}

