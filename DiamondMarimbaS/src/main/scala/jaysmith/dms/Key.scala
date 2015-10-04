package jaysmith.dms

/** Represents an individual "key" of the virtual Marimba.
  * Contains a ReducedRatio indicating the pitch of the key,
  * and a boolean flag indicating whether the key is currently
  * playing.
  *
  * @param ratio the ratio that defines the pitch of the key
  */
class Key (val ratio: ReducedRatio) {

  /** should this key currently be making sound? */
  var active = false

  /** a string representation of the key's ratio */
  override def toString : String = ratio.toString


}
