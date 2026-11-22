package extetris.gui

import scalafx.animation.AnimationTimer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, Separator}
import scalafx.scene.layout.{HBox, StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import scalafx.scene.input.KeyCode
import scalafx.Includes._
import extetris.model.GameState

/** Shown once the game is over. Displays the final result and returns to the
  * main menu automatically after a short countdown (or immediately via the
  * Main Menu button / Esc / Enter).
  */
class GameOverScene(finalState: GameState, mainMenu: MainMenuScene):
  private val autoReturnSeconds = 6.0
  private var startedAt = 0L
  private var shownRemaining = autoReturnSeconds.toInt
  private var returned = false

  private val countdownLabel = new Label(s"Returning to main menu in $shownRemaining seconds..."):
    font = Font("Monospace", 13)
    textFill = Color.Gray

  private def statBlock(title: String, value: String): VBox =
    new VBox(4):
      alignment = Pos.Center
      children = Seq(
        new Label(title):
          font = Font("Monospace", 12)
          textFill = Color.Gray
        ,
        new Label(value):
          font = Font("Monospace", 26)
          textFill = Color.White
      )

  val scene: Scene = new Scene:
    root = new StackPane:
      style = "-fx-background-color: #0d0d15;"
      children = Seq(
        new VBox(24):
          alignment = Pos.Center
          padding = Insets(50)
          maxWidth = 520
          children = Seq(
            new Label("GAME OVER"):
              font = Font("Monospace", 56)
              textFill = Color.color(1.0, 0.3, 0.3)
              style = "-fx-effect: dropshadow(gaussian, #ff0000, 30, 0.5, 0, 0);"
            ,
            new HBox(40):
              alignment = Pos.Center
              children = Seq(
                statBlock("SCORE", f"${finalState.score}%,d"),
                statBlock("LEVEL", finalState.level.toString),
                statBlock("LINES", finalState.linesCleared.toString)
              )
            ,
            new Separator():
              maxWidth = 320
              style = "-fx-background-color: #333;"
            ,
            new Button("Main Menu"):
              prefWidth = 240
              prefHeight = 50
              font = Font("Monospace", 18)
              style = "-fx-background-color: #00ffff; -fx-text-fill: #0d0d15; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;"
              onAction = _ => returnToMenu()
            ,
            countdownLabel
          )
      )

    onKeyPressed = e =>
      e.code match
        case KeyCode.Escape | KeyCode.Enter => returnToMenu()
        case _ =>

  // Drive the countdown from the animation pulse (no blocking on the FX thread).
  private val countdownTimer = AnimationTimer { now =>
    if !returned then
      if startedAt == 0L then startedAt = now
      val elapsed = (now - startedAt) / 1_000_000_000.0
      val remaining = math.max(0, math.ceil(autoReturnSeconds - elapsed).toInt)
      if remaining != shownRemaining then
        shownRemaining = remaining
        countdownLabel.text = s"Returning to main menu in $remaining..."
      if elapsed >= autoReturnSeconds then returnToMenu()
  }

  countdownTimer.start()

  private def returnToMenu(): Unit =
    if !returned then
      returned = true
      countdownTimer.stop()
      MainApp.stage.scene = mainMenu.scene
