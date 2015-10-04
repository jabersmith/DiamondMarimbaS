package jaysmith.dms

import android.app.Activity
import android.os.Bundle

import jaysmith.dms.drawing.MarimbaView

/** The main (only, at least for now) Activity for the
  * Diamond Marimba.  Totally boring app lifecycle stuff.
  */
class Play extends Activity {


  private var player: AudioPlayer = null

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_play)
  }

  protected override def onStart() = {
    super.onStart()
    val marimba = findViewById(R.id.game_board).asInstanceOf[MarimbaView].marimba
    player = new AudioPlayer(marimba)
    player.start()
  }

  protected override def onStop() = {
    super.onStop()
    player.stop()
    player = null
  }
}