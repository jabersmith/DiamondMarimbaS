package jaysmith.dms

/** The fundamental building block of Partch's music theory --
  * every pitch of a scale is expressed as a ratio of two small(-ish)
  * integers.  Each ratio used in the tonality-diamond/
  * diamond-marimba is then "reduced to the octave" -- that is,
  * multiplied or divided by two until the value fits in the
  * range 1 <= n < 2.  (Since a pitch of frequency 2n sounds
  * one octave higher than a pitch of frequency n, this halving
  * and doubling of the ratio is effectively just shifting the
  * corresponding note an octave higher or lower, until it fits
  * into the octave beginning with the diamond's tonic note.)
  *
  * The ratios of the tonality diamond are constructed in two
  * different ways.  Two sides of the diamond are built from
  * simple ratios of the diamond's "identities" (the odd integers
  * from 1 to the "limit" of the diamond), and the rest is built
  * from the products of two of those identity-ratios.  So there
  * are two constructors here -- one takes two integers, and
  * reduces the ratio between them, and the other takes two
  * ReducedRatios, computes their product, and then reduces
  * the resulting ratio.
  *
  * @param n The (original) numerator of the ratio
  * @param d The (original) denominator of the ratio
  */
class ReducedRatio (n: Int, d: Int) extends Ordered[ReducedRatio] {

  /** The reduced numerator and denominator values  */
  val (numerator, denominator) = reduce(n, d)

  /** a floating-point representation of the ratio */
  val value = numerator.toDouble / denominator.toDouble

  /** the secondary constructor, as described above --
    * takes the product of two ratios, and then reduces
    * the result.
    *
    * @param r1 one ratio
    * @param r2 the other ratio
    */
  def this(r1: ReducedRatio, r2: ReducedRatio) {
    this(r1.numerator * r2.numerator, r1.denominator * r2.denominator)
  }


  private def reduce(n: Int, d: Int): (Int, Int) = {
    var (num, denom) = (n, d)

    // First, "reduce" the ratio to the octave (that is, multiply
    // or divide by two until it falls in the range [1, 2) -- that
    // is, between our tonic and its octave-higher counterpart).
    while (num < denom)
      num *= 2

    while (num >= (2 * denom))
      denom *= 2

    // Then, do traditional fractional reduction
    val g = gcd(num,denom)
    (num / g, denom / g)
  }

  /** compute the greatest common denominator of two
    * integers using the modulo version of Euclid's
    * algorithm.
    *
    * @param p an integer
    * @param q another integer
    * @return their gcd
    */
  private def gcd(p: Int, q: Int): Int = {
    if (q == 0)
      p
    else
      gcd(q, p % q)
  }

  /** standard compare() method for the Ordered trait.
    *
    * @param that The ReducedRatio to which this ReducedRatio should be compared
    * @return x where x < 0 iff this < that x == 0 iff this == that x > 0 iff this > that
    */
  def compare(that: ReducedRatio): Int = value.compare(that.value)

  /** a string representation of the ratio */
  override def toString : String = numerator + "/" + denominator
}
