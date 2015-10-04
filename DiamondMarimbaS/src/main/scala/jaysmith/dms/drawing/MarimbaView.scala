package jaysmith.dms.drawing

import android.content.Context
import android.graphics._
import android.util.{Log, AttributeSet}
import android.view.{MotionEvent, View}

import jaysmith.dms.{R, Marimba}

/** A custom view for drawing and controlling the diamond marimba.
  *
  * It's more complicated than I'd really like it to be, because
  * Android Views are, at least for now, always rectangular.  So
  * the diamond-shaped keys of the marimba can't be any sort of
  * sub-Views, and we have to handle low-level details here.
  *
  * @param context The View's Context
  * @param attrs The attributes of the XML tag that inflates the view
  * @param initialDegree The size of the Marimba we should start with
  */
class MarimbaView(context: Context, attrs: AttributeSet, val initialDegree: Int)
  extends View(context, attrs) {

  /** XML constructor, required by Android
    *
    * @param context The View's Context
    * @param attrs The attributes of the XML tag that inflates the view
    */
  def this(context: Context, attrs: AttributeSet) = this(context, attrs, {
    val a = context.getTheme.obtainStyledAttributes(attrs, R.styleable.MarimbaView, 0, 0)
    val initialDegree = a.getInteger(R.styleable.MarimbaView_initialDegree, 0)
    a.recycle()
    initialDegree
  })


  /** The marimba, created by the View and accessible to the Activity */
  var marimba: Marimba = new Marimba(initialDegree)

  // the corners of the square in which we'll draw the Marimba
  private var square = new CentralSquare(0,0)

  // The set of diamonds, the onscreen representation of each marimba Key
  private var diamonds = new DiamondSet(marimba, square)

  // Logging, mostly for when I don't grok the android MotionEvent
  private val logEnabled = false
  private val logName = "DiamondMarimbaS:View"

  /** Called during layout when the size of the view changes.
    *
    * @param w new width
    * @param h new height
    * @param oldw old width
    * @param oldh old height
    */
  protected override def onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    if ((w == 0) || (h == 0)) return

    square = new CentralSquare(w, h)

    diamonds = new DiamondSet(marimba, square)

  }

  /** Called when the system needs the Marimba to be redrawn
    *
    * @param canvas the canvas on which the background will be drawn
    */
  override def onDraw(canvas: Canvas) {
    val p = new Paint

    // Fill in a black background
    p.setColor(Color.BLACK)
    p.setStyle(Paint.Style.FILL)
    p.setStrokeWidth(1)
    canvas.drawRect(0, 0, getWidth, getHeight, p)

    // Draw the diamonds
    diamonds.draw(p, canvas)

    // Upper left corner displays the limit
    // (and is also the increment-degree button)
    p.setColor(Color.WHITE)
    p.setTextSize(24)
    canvas.drawText("Limit: " + marimba.limit,
                    square.xMin, square.yMin, p)

    // Lower left corner has details on the
    // currently-playing ratios
    drawCurrentlyActiveInfo(p, canvas)
  }

  // write the list of currently-active ratios to
  // the lower left corner of the screen
  private def drawCurrentlyActiveInfo(p: Paint, canvas: Canvas) = {
    var y = square.yMax
    for (key <- marimba.currentlyActive) {
      p.setColor(Diamond.colorMap(key.ratio.value))
      canvas.drawText("ratio: %s, %.3f".format(key.toString, key.ratio.value), square.xMin, y, p)
      y -= 24
    }
  }


  /** handle a touch event
    *
    * @param event the touch event
    * @return true if we've handled the event; false otherwise
    */
  override def onTouchEvent(event: MotionEvent): Boolean = {
    if (marimba == null) return false

    val i = event.getActionIndex
    val action = event.getActionMasked

    if (logEnabled) {
      Log.d(logName, "received touch event %d at (%d, %d)".format(action, event.getX(i).toInt, event.getY(i).toInt))
    }

    val changed = action match {
      case MotionEvent.ACTION_DOWN | MotionEvent.ACTION_POINTER_DOWN =>
        diamonds.press(event, i) ||
          checkCornerButtons(event, i)
      case MotionEvent.ACTION_UP | MotionEvent.ACTION_POINTER_UP =>
        diamonds.release(event, i)
      case MotionEvent.ACTION_MOVE =>
        val flags = for (i <- 0 until event.getPointerCount) yield {
          diamonds.move(event, i)
        }
        flags.reduceLeft(_ || _)
      case _ =>
        false
    }
    if (changed) this.invalidate()
    changed
  }

  // Currently, only the upper left corner of the screen is
  // an active button, to increase the degree of the marimba
  private def checkCornerButtons(event: MotionEvent, i: Int) : Boolean = {
    val pressed = square.upperLeft.contains(event.getX(i).toInt, event.getY(i).toInt)
    if (pressed) {
      marimba.incrementDegree()
      diamonds = new DiamondSet(marimba, square)
    }
    pressed
  }
}
