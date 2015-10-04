package jaysmith.dms

import android.media.{AudioManager, AudioFormat, AudioTrack}
import android.util.Log

/** Simple polyphonic synth for playing the currently-active
  * keys of the diamond marimba.  Spawns a thread that repeatedly
  * fills an android AudioTrack
 *
 * @param marimba The marimba for which the AudioPlayer will generate audio
 */
class AudioPlayer(private val marimba: Marimba) extends Runnable {

  @volatile private var stopFlag = false
  private var soundThread = new Thread(this)

  // if logging is enabled, log timing information every 1000 buffers
  private val logEnabled = false
  private val logName = "DiamondMarimbaS:Audio"
  private val logInterval = 1000
  private var startOuterLoop = 0l
  private var startInnerLoop = 0l
  private var endLoops = 0l


  def start() = {
    soundThread.setPriority(Thread.MAX_PRIORITY)
    soundThread.start()
  }

  def stop() = {
    stopFlag = true
    soundThread.join()
    soundThread = null
  }

  def run() = {

    // create our AudioTrack -- 44KHz, 16-bit mono
    val minBuffSize: Int = AudioTrack.getMinBufferSize(AudioPlayer.SamplingFrequency,
                                                       AudioFormat.CHANNEL_OUT_MONO,
                                                       AudioFormat.ENCODING_PCM_16BIT) / 2

    val audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                                    AudioPlayer.SamplingFrequency,
                                    AudioFormat.CHANNEL_OUT_MONO,
                                    AudioFormat.ENCODING_PCM_16BIT,
                                    minBuffSize,
                                    AudioTrack.MODE_STREAM)
    audioTrack.play()

    val buffsize = minBuffSize
    val samples = new Array[Short](buffsize)

    var voices: Map[Double, SingleVoice] = Map()

    var logCounter = 0
    while (!stopFlag) {

      val log = logEnabled && ((logCounter % logInterval) == 0)

      if (log) {
        startOuterLoop = System.nanoTime
      }

      // The ratios of the currently-active keys of the marimba -- this is a
      // set since a marimba will normally have multiple keys with a 1::1 ratio,
      // and there's no need to add them into the mix separately
      val currentRatios = marimba.currentlyActive.map(k => k.ratio.value).toSet

      // Since voices still contains the voices from last pass,
      // these are the sets of ratios that are new to the current
      // set, and those that have been dropped since last pass
      val addRatios = currentRatios diff voices.keySet
      val removeRatios = voices.keySet diff currentRatios

      // We continue to track the ratios that have just left the
      // current set so that we can fade them over the first n
      // samples of the new pass, instead of abruptly dropping
      // them, potentially causing audible clicks.
      val fadeVoices = voices.filterKeys(removeRatios.contains)

      // update the voices Map, removing no-longer-active voices
      // and adding newly-active ones
      voices = voices -- removeRatios ++
        addRatios.map(r => (r, new SingleVoice(AudioPlayer.Tonic * r,
                                               AudioPlayer.Amplitude)))

      // The inner loop is by far our bottleneck -- it has to run
      // 44K times per second, or we fall behind the audioTrack.
      // I'd originally written it as a cute Scala for comprehension,
      // but the object-creation involved in that implementation led
      // to frequent, audible pauses.  This simple while loop did much
      // better.
      val voiceArray = voices.values.toArray
      val fadeArray = fadeVoices.values.toArray

      if (log) {
        startInnerLoop = System.nanoTime
      }

      var i = 0
      while (i < buffsize)
      {
        samples(i) = sumVoices(voiceArray, fadeArray, i)
        i += 1
      }

      if (log){
        endLoops = System.nanoTime
      }

      audioTrack.write(samples, 0, buffsize)

      if (log) {
        val endWrite = System.nanoTime
        Log.d(logName, "outerLoop %d, innerLoop %d, writeTime %d".format(endLoops - startOuterLoop,
                                                                         endLoops - startInnerLoop,
                                                                         endWrite - endLoops))
      }
      logCounter += 1
    }
    audioTrack.stop()
    audioTrack.release()
  }


  // Given a collection of voices (and a separate collection of voices
  // we're meant to be fading), generate the next sample by averaging
  // the next value from each voice's waveform
  private def sumVoices(currentVoices: Array[SingleVoice],
                        fadeVoices: Array[SingleVoice],
                        position: Int): Short = {
    var total: Int = 0
    var nVoices: Int = 0

    nVoices = currentVoices.length
    var i = 0
    while (i < nVoices) {
      total += currentVoices(i).getNextShort
      i += 1
    }

    // Fade the old voices over the first 100 samples of
    // each pass
    var nFade = fadeVoices.length
    if ((nFade > 0) && (position < 100)) {
      val fade: Float = (100f - position) / 100f

      i = 0
      while (i < nFade) {
        total += (fadeVoices(i).getNextShort * fade).toInt
        i += 1
      }
      nVoices += nFade
    }

    if (nVoices == 0) 0 else (total / nVoices).toShort
  }

  // Compute the (sinusoidal, for now) waveform for a single
  // frequency.
  private class SingleVoice(private val frequency: Double, private val scale: Short) {

    private val increment: Double = (AudioPlayer.TwoPi / AudioPlayer.SamplingFrequency) * frequency
    private var phaseIndex: Double = 0.0

    def getNextShort: Short = {
      val sine: Double = Math.sin(phaseIndex)
      phaseIndex = (phaseIndex + increment) % AudioPlayer.TwoPi
      (scale * sine).toShort
    }
  }
}

// A bunch of simple constants we need to generate audio
private object AudioPlayer {
  private val TwoPi: Double = 8.0D * Math.atan(1.0)
  private val SamplingFrequency: Int = 44100
  private val Tonic: Double = 440.0D
  private val Amplitude: Short = 10000
}
