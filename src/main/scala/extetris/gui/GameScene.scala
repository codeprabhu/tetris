package extetris.gui

import scalafx.scene.Scene
import scalafx.scene.layout.{BorderPane, VBox, HBox, StackPane, GridPane, Pane}
import scalafx.scene.control.{Button, Label, MenuBar, Menu, MenuItem, SeparatorMenuItem}
import scalafx.scene.shape.Rectangle
import scalafx.scene.paint.Color
import scalafx.geometry.{Insets, Pos}
import scalafx.animation.AnimationTimer
import scalafx.stage.FileChooser
import scalafx.Includes._
import scalafx.scene.input.KeyCode
import extetris.model.{GameState, GameInput, GameStatus, Position}
import extetris.pieces.Piece
import extetris.board.Board
import extetris.runner.GameRunner
import extetris.config.VariantConfig
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutor

class GameScene(runner: GameRunner, variant: VariantConfig, mainMenu: MainMenuScene):
  given ExecutionContextExecutor = ExecutionContext.global
  
  private val cellSize = 30
  private val boardWidth = variant.boardWidth
  private val boardHeight = variant.boardHeight
  
  private val boardPane = new Pane():
    prefWidth = boardWidth * cellSize
    prefHeight = boardHeight * cellSize
    style = "-fx-background-color: #0f0f1a; -fx-border-color: #444; -fx-border-width: 2;"
  
  private val nextPiecePane = new Pane():
    prefWidth = 4 * cellSize
    prefHeight = 4 * cellSize
    style = "-fx-background-color: #0f0f1a; -fx-border-color: #444; -fx-border-width: 1;"
  
  private val heldPiecePane = new Pane():
    prefWidth = 4 * cellSize
    prefHeight = 4 * cellSize
    style = "-fx-background-color: #0f0f1a; -fx-border-color: #444; -fx-border-width: 1;"
  
  private val scoreLabel = new Label("Score: 0"):
    font = scalafx.scene.text.Font(18)
    textFill = Color.White
  
  private val levelLabel = new Label("Level: 1"):
    font = scalafx.scene.text.Font(18)
    textFill = Color.White
  
  private val linesLabel = new Label("Lines: 0"):
    font = scalafx.scene.text.Font(18)
    textFill = Color.White
  
  private val statusLabel = new Label(""):
    font = scalafx.scene.text.Font(24)
    textFill = Color.Red
  
  private val pauseOverlay = new StackPane():
    visible = false
    children = new Label("PAUSED"):
      font = scalafx.scene.text.Font(48)
      textFill = Color.White
    style = "-fx-background-color: rgba(0, 0, 0, 0.7);"
  
  private val gameOverOverlay = new StackPane():
    visible = false
    children = new VBox(20):
      alignment = Pos.Center
      children = Seq(
        new Label("GAME OVER"):
          font = scalafx.scene.text.Font(48)
          textFill = Color.Red
        ,
        new Label(""):
          font = scalafx.scene.text.Font(24)
          textFill = Color.White
        ,
        new Button("Restart"):
          prefWidth = 150
          onAction = _ => restart()
        ,
        new Button("Main Menu"):
          prefWidth = 150
          onAction = _ => returnToMenu()
      )
    style = "-fx-background-color: rgba(0, 0, 0, 0.8);"
  
  private var animationTimer: Option[AnimationTimer] = None
  
  val scene: Scene = new Scene:
    root = new BorderPane():
      padding = Insets(20)
      style = "-fx-background-color: #1a1a2e;"
      top = new MenuBar():
        menus = Seq(
          new Menu("Game"):
            items = Seq(
              new MenuItem("Restart"):
                onAction = _ => restart()
              ,
              new SeparatorMenuItem()
              ,
              new MenuItem("Save"):
                onAction = _ => saveGame()
              ,
              new MenuItem("Load"):
                onAction = _ => loadGame()
              ,
              new SeparatorMenuItem()
              ,
              new MenuItem("Main Menu"):
                onAction = _ => returnToMenu()
            )
          ,
          new Menu("View"):
            items = Seq(
              new MenuItem("Settings"):
                onAction = _ => showSettings()
            )
        )
      center = new HBox(20):
        alignment = Pos.TopCenter
        children = Seq(
          new VBox(10):
            alignment = Pos.TopCenter
            children = Seq(
              new Label("NEXT"):
                font = scalafx.scene.text.Font(16)
                textFill = Color.White
              ,
              nextPiecePane
              ,
              new Label("HOLD"):
                font = scalafx.scene.text.Font(16)
                textFill = Color.White
              ,
              heldPiecePane
            )
          ,
          new StackPane():
            children = Seq(boardPane, pauseOverlay, gameOverOverlay)
          ,
          new VBox(10):
            alignment = Pos.TopCenter
            children = Seq(scoreLabel, levelLabel, linesLabel, statusLabel)
        )
      bottom = new HBox(10):
        alignment = Pos.Center
        padding = Insets(10)
        children = Seq(
          new Label("← →: Move  ↑: Rotate  ↓: Soft Drop  Space: Hard Drop  C: Hold  P: Pause"):
            font = scalafx.scene.text.Font(12)
            textFill = Color.Gray
        )
    
    onKeyPressed = e =>
      if runner.state.isPlaying then
        e.code match
          case KeyCode.Left => runner.handleInput(GameInput.MoveLeft)
          case KeyCode.Right => runner.handleInput(GameInput.MoveRight)
          case KeyCode.Down => runner.handleInput(GameInput.MoveDown)
          case KeyCode.Up => runner.handleInput(GameInput.RotateCW)
          case KeyCode.Space => runner.handleInput(GameInput.HardDrop)
          case KeyCode.C => runner.handleInput(GameInput.Hold)
          case KeyCode.P => runner.pause()
          case _ =>
      else if runner.state.isPaused then
        e.code match
          case KeyCode.P => runner.resume()
          case _ =>
    
    onKeyReleased = e =>
      if runner.state.isPlaying then
        e.code match
          case KeyCode.Down => () // Soft drop release handled by tick
          case _ =>
  
  private def startAnimation(): Unit =
    animationTimer = Some(AnimationTimer(t => updateView()))
    animationTimer.foreach(_.start())
  
  private def stopAnimation(): Unit =
    animationTimer.foreach(_.stop())
    animationTimer = None
  
  private def updateView(): Unit =
    val state = runner.state
    renderBoard(state.board)
    renderCurrentPiece(state.currentPiece)
    renderNextPiece(state.nextPiece)
    renderHeldPiece(state.heldPiece)
    updateLabels(state)
    updateOverlays(state)
  
  private def renderBoard(board: Board): Unit =
    boardPane.children.clear()
    for
      row <- 0 until board.height
      col <- 0 until board.width
    do
      board.cellAt(col, row).foreach {
        case extetris.model.Cell.Filled(_, color) =>
          val rectX = col * cellSize
          val rectY = row * cellSize
          val rect = new Rectangle:
            layoutX = rectX
            layoutY = rectY
            width = cellSize - 1
            height = cellSize - 1
            fill = color.toScalafxColor
            stroke = Color.Black
            strokeWidth = 0.5
          boardPane.children.add(rect)
        case _ =>
      }
  
  private def renderCurrentPiece(piece: Option[Piece]): Unit =
    piece.foreach { p =>
      p.cells.foreach { pos =>
        if pos.y >= 0 then
          val rectX = pos.x * cellSize
          val rectY = pos.y * cellSize
          val rect = new Rectangle:
            layoutX = rectX
            layoutY = rectY
            width = cellSize - 1
            height = cellSize - 1
            fill = p.color.toScalafxColor
            stroke = Color.White
            strokeWidth = 1
          boardPane.children.add(rect)
      }
    }
  
  private def renderNextPiece(piece: Option[Piece]): Unit =
    nextPiecePane.children.clear()
    piece.foreach { p =>
      val (minX, minY, _, _) = p.rotation.boundingBox
      p.rotation.cells.foreach { pos =>
        val rectX = (pos.x - minX) * cellSize + cellSize
        val rectY = (pos.y - minY) * cellSize + cellSize
        val rect = new Rectangle:
          layoutX = rectX
          layoutY = rectY
          width = cellSize - 1
          height = cellSize - 1
          fill = p.color.toScalafxColor
          stroke = Color.White
          strokeWidth = 1
        nextPiecePane.children.add(rect)
      }
    }
  
  private def renderHeldPiece(definition: Option[extetris.pieces.PieceDefinition]): Unit =
    heldPiecePane.children.clear()
    definition.foreach { defn =>
      val rotation = defn.initialRotation
      val (minX, minY, _, _) = rotation.boundingBox
      rotation.cells.foreach { pos =>
        val rectX = (pos.x - minX) * cellSize + cellSize
        val rectY = (pos.y - minY) * cellSize + cellSize
        val rect = new Rectangle:
          layoutX = rectX
          layoutY = rectY
          width = cellSize - 1
          height = cellSize - 1
          fill = defn.color.toScalafxColor
          stroke = Color.White
          strokeWidth = 1
        heldPiecePane.children.add(rect)
      }
    }
  
  private def updateLabels(state: GameState): Unit =
    scoreLabel.text = f"Score: ${state.score}%,d"
    levelLabel.text = s"Level: ${state.level}"
    linesLabel.text = s"Lines: ${state.linesCleared}"
  
  private def updateOverlays(state: GameState): Unit =
    pauseOverlay.visible = state.isPaused
    gameOverOverlay.visible = state.isGameOver
    if state.isGameOver then
      val label = gameOverOverlay.children.head.asInstanceOf[scalafx.scene.layout.VBox].children(1).asInstanceOf[Label]
      label.text = f"Final Score: ${state.score}%,d\nLevel: ${state.level}\nLines: ${state.linesCleared}"
  
  def restart(): Unit =
    runner.restart()
    pauseOverlay.visible = false
    gameOverOverlay.visible = false
  
  def returnToMenu(): Unit =
    runner.stop()
    stopAnimation()
    MainApp.stage.scene = mainMenu.scene
  
  def saveGame(): Unit =
    val fileChooser = new FileChooser()
    fileChooser.setTitle("Save Game")
    fileChooser.getExtensionFilters.add(new FileChooser.ExtensionFilter("ExTetris Save", "*.json"))
    val file = fileChooser.showSaveDialog(MainApp.stage)
    if file != null then
      runner.save(file.getAbsolutePath) match
        case Right(_) => showStatus(s"Game saved to ${file.getName}")
        case Left(e) => showStatus(s"Save failed: ${e.getMessage}")
  
  def loadGame(): Unit =
    val fileChooser = new FileChooser()
    fileChooser.setTitle("Load Game")
    fileChooser.getExtensionFilters.add(new FileChooser.ExtensionFilter("ExTetris Save", "*.json"))
    val file = fileChooser.showOpenDialog(MainApp.stage)
    if file != null then
      runner.load(file.getAbsolutePath) match
        case Right(_) => showStatus(s"Game loaded from ${file.getName}")
        case Left(e) => showStatus(s"Load failed: ${e.getMessage}")
  
  def showSettings(): Unit =
    MainApp.stage.scene = SettingsScene(this).scene
  
  def showStatus(message: String): Unit =
    statusLabel.text = message
    // Clear after 3 seconds
    new Thread(() => {
      Thread.sleep(3000)
      scalafx.application.Platform.runLater(() => statusLabel.text = "")
    }).start()
  
  startAnimation()