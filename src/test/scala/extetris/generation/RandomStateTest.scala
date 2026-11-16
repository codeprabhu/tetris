package extetris.generation

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class RandomStateTest extends AnyFunSuite with Matchers:
  
  test("RandomState from seed"):
    val rs = RandomState.fromSeed(12345L)
    rs.currentSeed shouldBe 12345L
    rs.currentState shouldBe 12345L
  
  test("RandomState random creates different seeds"):
    val rs1 = RandomState.random()
    val rs2 = RandomState.random()
    rs1.currentSeed should not equal rs2.currentSeed
  
  test("NextInt produces values in range"):
    var rs = RandomState.fromSeed(12345L)
    for _ <- 1 to 100 do
      val (value, newRs) = rs.nextInt(10)
      value should be >= 0
      value should be < 10
      rs = newRs
  
  test("NextInt with bound 1 always returns 0"):
    var rs = RandomState.fromSeed(12345L)
    for _ <- 1 to 10 do
      val (value, newRs) = rs.nextInt(1)
      value shouldBe 0
      rs = newRs
  
  test("Deterministic sequence with same seed"):
    var rs1 = RandomState.fromSeed(12345L)
    var rs2 = RandomState.fromSeed(12345L)
    
    val seq1 = (1 to 10).map { _ =>
      val (v, n) = rs1.nextInt(100)
      rs1 = n
      v
    }
    
    val seq2 = (1 to 10).map { _ =>
      val (v, n) = rs2.nextInt(100)
      rs2 = n
      v
    }
    
    seq1 shouldBe seq2
  
  test("Different seeds produce different sequences"):
    var rs1 = RandomState.fromSeed(11111L)
    var rs2 = RandomState.fromSeed(22222L)
    
    val seq1 = (1 to 10).map { _ =>
      val (v, n) = rs1.nextInt(100)
      rs1 = n
      v
    }
    
    val seq2 = (1 to 10).map { _ =>
      val (v, n) = rs2.nextInt(100)
      rs2 = n
      v
    }
    
    seq1 should not equal seq2
  
  test("NextLong produces values"):
    var rs = RandomState.fromSeed(12345L)
    for _ <- 1 to 10 do
      val (value, newRs) = rs.nextLong()
      rs = newRs
      // Just verify it runs without error
  
  test("NextDouble produces values in [0, 1)"):
    var rs = RandomState.fromSeed(12345L)
    for _ <- 1 to 100 do
      val (value, newRs) = rs.nextDouble()
      value should be >= 0.0
      value should be < 1.0
      rs = newRs
  
  test("NextBoolean produces both values"):
    var rs = RandomState.fromSeed(12345L)
    var hasTrue = false
    var hasFalse = false
    for _ <- 1 to 100 do
      val (value, newRs) = rs.nextBoolean()
      if value then hasTrue = true else hasFalse = true
      rs = newRs
    hasTrue shouldBe true
    hasFalse shouldBe true
  
  test("SetSeed resets state"):
    var rs = RandomState.fromSeed(12345L)
    val (v1, n1) = rs.nextInt(100)
    rs = n1.setSeed(12345L)
    val (v2, _) = rs.nextInt(100)
    v1 shouldBe v2