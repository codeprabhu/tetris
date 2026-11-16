package extetris.generation

import scala.util.Random

final case class RandomState(private val seed: Long, private val state: Long):
  def nextInt(bound: Int): (Int, RandomState) =
    val rng = new Random(state)
    val value = rng.nextInt(bound)
    (value, copy(state = rng.nextLong()))
  
  def nextLong(): (Long, RandomState) =
    val rng = new Random(state)
    val value = rng.nextLong()
    (value, copy(state = rng.nextLong()))
  
  def nextDouble(): (Double, RandomState) =
    val rng = new Random(state)
    val value = rng.nextDouble()
    (value, copy(state = rng.nextLong()))
  
  def nextBoolean(): (Boolean, RandomState) =
    val rng = new Random(state)
    val value = rng.nextBoolean()
    (value, copy(state = rng.nextLong()))
  
  def setSeed(newSeed: Long): RandomState = copy(seed = newSeed, state = newSeed)
  def currentSeed: Long = seed
  def currentState: Long = state

object RandomState:
  def fromSeed(seed: Long): RandomState = RandomState(seed, seed)
  def random(): RandomState = fromSeed(System.nanoTime())
  
  given Ordering[RandomState] = Ordering.by(_.seed)