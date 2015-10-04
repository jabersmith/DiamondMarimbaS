package jaysmith.dms

/** Implements Partch's tonal diamond as a "marimba" of "keys".
  *
  * @param degree the number of keys along each side of the diamond
  */
class Marimba (var degree: Int) {

  /** the keys, as a two-dimensional vector.  First index indicates
    * what Partch called the "otonality", or "over-tonality", of
    * the key's ratio -- this is essentially the set of ratios in
    * the diamond whose numerators share the same "identity"/"odentity".
    * The second index indicates the "utonality" ("under-tonality")
    * of the ratio -- the set whose denominators share an "identity"/
    * "udentity".
    */
  var keys : Vector[Vector[Key]] = computeRatios

  /** In Partch's terminology, the "limit" is the highest value
    * in the "identity" of the diamond -- that is, the set of
    * consecutive odd integers {1, 3, ..., n} from which the
    * diamond's ratios will be constructed.
    *
    * @return the limit
    */
  def limit: Int = (degree * 2) - 1

  /** Increase the size of the Marimba by one.
    *
    * I've made the Marimba itself a mutable object, with an
    * collection of immutable keys, for ease in communicating
    * between the View and the AudioPlayer thread.
    */
  def incrementDegree ()= {
    degree = if (degree < 10) (degree + 1) else 2
    keys = computeRatios
  }

  // Do the actual work of constructing the keys.
  // As described above, the odentities and udentities are
  // the axes of the diamond; the other keys are reduced
  // from the products of their corresponding odentities
  // and udentities.
  private def computeRatios : Vector[Vector[Key]] = {
    val odentities = (for (odd <- 1 to (limit, 2)) yield new ReducedRatio(odd, 1)).sorted
    val udentities = (for (odd <- 3 to (limit, 2)) yield new ReducedRatio(1, odd)).sorted.reverse

    Vector.tabulate(degree, degree) ((m: Int, n: Int) => (m, n) match {
      case (0, j) => new Key(odentities(j))
      case (i, 0) => new Key(udentities(i - 1))
      case (i, j) => new Key(new ReducedRatio(odentities(j), udentities(i - 1)))
    })
  }

  /** A Vector of the keys of the marimba that are currently
    * "active" -- that is, that should be making sound and
    * drawn as on.
    * @return the vector
    */
  def currentlyActive = keys.flatMap(_.filter(_.active))
}
