package extetris.gui

import scalafx.scene.Scene
import scalafx.scene.layout.{BorderPane, VBox, HBox, StackPane, Pane}
import scalafx.scene.control.{Label, MenuBar, Menu, MenuItem, SeparatorMenuItem, Separator}
import scalafx.scene.shape.{Rectangle, StrokeType}
import scalafx.scene.paint.Color
import scalafx.geometry.{Insets, Pos}
import scalafx.animation.{AnimationTimer, PauseTransition}
import scalafx.util.Duration
import scalafx.stage.FileChooser
import scalafx.Includes._
import scalafx.scene.input.KeyCode
import scalafx.application.Platform
import extetris.model.{GameState, GameInput}
import extetris.pieces.Piece
import extetris.board.Board
import extetris.runner.GameRunner
import extetris.config.VariantConfig

class GameScene(runner: GameRunner, variant: VariantConfig, mainMenu: MainMenuScene):
  private val cellSize = 28
  private val boardWidth = variant.boardWidth
  private val boardHeight = variant.boardHeight
  private val previewSize = 4
  
  private val boardPane = new Pane():
    prefWidth = boardWidth * cellSize
    prefHeight = boardHeight * cellSize
    minWidth = boardWidth * cellSize
    minHeight = boardHeight * cellSize
    maxWidth = boardWidth * cellSize
    maxHeight = boardHeight * cellSize
    style = "-fx-background-color: #0d0d15; -fx-border-color: #333; -fx-border-width: 2; -fx-background-radius: 4; -fx-border-radius: 4;"
  
  private val nextPiecePane = new Pane():
    prefWidth = previewSize * cellSize
    prefHeight = previewSize * cellSize
    minWidth = previewSize * cellSize
    minHeight = previewSize * cellSize
    maxWidth = previewSize * cellSize
    maxHeight = previewSize * cellSize
    style = "-fx-background-color: #0d0d15; -fx-border-color: #333; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;"
  
  private val heldPiecePane = new Pane():
    prefWidth = previewSize * cellSize
    prefHeight = previewSize * cellSize
    minWidth = previewSize * cellSize
    minHeight = previewSize * cellSize
    maxWidth = previewSize * cellSize
    maxHeight = previewSize * cellSize
    style = "-fx-background-color: #0d0d15; -fx-border-color: #333; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;"
  
  private val scoreLabel = new Label("Score: 0"):
    font = scalafx.scene.text.Font("Monospace", 18)
    textFill = Color.White
    style = "-fx-padding: 8 12; -fx-background-color: #1a1a2e; -fx-background-radius: 4; -fx-border-color: #333; -fx-border-width: 1;"
  
  private val levelLabel = new Label("Level: 1"):
    font = scalafx.scene.text.Font("Monospace", 18)
    textFill = Color.White
    style = "-fx-padding: 8 12; -fx-background-color: #1a1a2e; -fx-background-radius: 4; -fx-border-color: #333; -fx-border-width: 1;"
  
  private val linesLabel = new Label("Lines: 0"):
    font = scalafx.scene.text.Font("Monospace", 18)
    textFill = Color.White
    style = "-fx-padding: 8 12; -fx-background-color: #1a1a2e; -fx-background-radius: 4; -fx-border-color: #333; -fx-border-width: 1;"
  
  private val statusLabel = new Label(""):
    font = scalafx.scene.text.Font("Monospace", 16)
    textFill = Color.Red
  
  private val pauseOverlay = new StackPane():
    visible = false
    pickOnBounds = false
    mouseTransparent = true
    children = new VBox(16):
      alignment = Pos.Center
      pickOnBounds = false
      mouseTransparent = true
      style = "-fx-background-color: rgba(13, 13, 21, 0.95); -fx-background-radius: 8;"
      children = Seq(
        new Label("PAUSED"):
          font = scalafx.scene.text.Font("Monospace", 48)
          textFill = Color.White
          style = "-fx-effect: dropshadow(gaussian, #00ffff, 20, 0.5, 0, 0);"
        ,
        new Label("Press P to resume"):
          font = scalafx.scene.text.Font("Monospace", 16)
          textFill = Color.Gray
      )
  
  private var lastRenderedState: Option[GameState] = None
  private var gameOverShown = false
  
  val scene: Scene = new Scene:
    root = new BorderPane():
      padding = Insets(20)
      style = "-fx-background-color: #0d0d15;"
      top = new MenuBar():
        style = "-fx-background-color: #1a1a2e; -fx-border-color: #333; -fx-border-width: 0 0 1 0;"
        menus = Seq(
          new Menu("Game"):
            style = "-fx-text-fill: white;"
            items = Seq(
              new MenuItem("Restart"):
                style = "-fx-text-fill: white;"
                onAction = _ => restart()
              ,
              new SeparatorMenuItem()
              ,
              new MenuItem("Save"):
                style = "-fx-text-fill: white;"
                onAction = _ => saveGame()
              ,
              new MenuItem("Load"):
                style = "-fx-text-fill: white;"
                onAction = _ => loadGame()
              ,
              new SeparatorMenuItem()
              ,
              new MenuItem("Main Menu"):
                style = "-fx-text-fill: white;"
                onAction = _ => returnToMenu()
            )
          ,
          new Menu("View"):
            style = "-fx-text-fill: white;"
            items = Seq(
              new MenuItem("Settings"):
                style = "-fx-text-fill: white;"
                onAction = _ => showSettings()
            )
        )
      center = new HBox(24):
        alignment = Pos.Center
        padding = Insets(0, 0, 0, 0)
        children = Seq(
          new VBox(16):
            alignment = Pos.TopCenter
            spacing = 16
            minWidth = previewSize * cellSize + 32
            maxWidth = previewSize * cellSize + 32
            children = Seq(
              new VBox(8):
                alignment = Pos.Center
                children = Seq(
                  new Label("NEXT"):
                    font = scalafx.scene.text.Font("Monospace", 14)
                    textFill = Color.color(0.5, 0.8, 1.0)
                    style = "-fx-text-fill: #80cfff;"
                  ,
                  new StackPane():
                    minWidth = previewSize * cellSize + 16
                    minHeight = previewSize * cellSize + 16
                    maxWidth = previewSize * cellSize + 16
                    maxHeight = previewSize * cellSize + 16
                    style = "-fx-background-color: #0d0d15; -fx-border-color: #333; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4; -fx-padding: 8;"
                    children = Seq(nextPiecePane)
                )
              ,
              new VBox(8):
                alignment = Pos.Center
                children = Seq(
                  new Label("HOLD"):
                    font = scalafx.scene.text.Font("Monospace", 14)
                    textFill = Color.color(0.5, 0.8, 1.0)
                  ,
                  new StackPane():
                    minWidth = previewSize * cellSize + 16
                    minHeight = previewSize * cellSize + 16
                    maxWidth = previewSize * cellSize + 16
                    maxHeight = previewSize * cellSize + 16
                    style = "-fx-background-color: #0d0d15; -fx-border-color: #333; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4; -fx-padding: 8;"
                    children = Seq(heldPiecePane)
                )
              ,
              new VBox(8):
                alignment = Pos.Center
                spacing = 8
                children = Seq(
                  new Label("CONTROLS"):
                    font = scalafx.scene.text.Font("Monospace", 12)
                    textFill = Color.Gray
                  ,
                  new Label("← / →  Move"):
                    font = scalafx.scene.text.Font("Monospace", 11)
                    textFill = Color.Gray
                  ,
                  new Label("↑  Rotate"):
                    font = scalafx.scene.text.Font("Monospace", 11)
                    textFill = Color.Gray
                  ,
                  new Label("↓  Soft Drop"):
                    font = scalafx.scene.text.Font("Monospace", 11)
                    textFill = Color.Gray
                  ,
                  new Label("Space  Hard Drop"):
                    font = scalafx.scene.text.Font("Monospace", 11)
                    textFill = Color.Gray
                  ,
                  new Label("C  Hold"):
                    font = scalafx.scene.text.Font("Monospace", 11)
                    textFill = Color.Gray
                  ,
                  new Label("P  Pause"):
                    font = scalafx.scene.text.Font("Monospace", 11)
                    textFill = Color.Gray
                  ,
                  new Label("Esc  Menu"):
                    font = scalafx.scene.text.Font("Monospace", 11)
                    textFill = Color.Gray
                )
            )
          ,
          new StackPane():
            style = "-fx-background-color: transparent;"
            children = Seq(boardPane, pauseOverlay)
          ,
          new VBox(16):
            alignment = Pos.TopCenter
            spacing = 16
            minWidth = 160
            maxWidth = 160
            children = Seq[scalafx.scene.Node](
              new VBox(12):
                alignment = Pos.Center
                spacing = 8
                style = "-fx-background-color: #1a1a2e; -fx-background-radius: 8; -fx-border-color: #333; -fx-border-width: 1; -fx-padding: 20;"
                children = Seq[scalafx.scene.Node](
                  new Label("STATS"):
                    font = scalafx.scene.text.Font("Monospace", 14)
                    textFill = Color.color(0.5, 0.8, 1.0)
                  ,
                  scoreLabel
                  ,
                  levelLabel
                  ,
                  linesLabel
                  ,
                  new Separator():
                    style = "-fx-background-color: #333;"
                  ,
                  new Label("ExTetris Platform"):
                    font = scalafx.scene.text.Font("Monospace", 10)
                    textFill = Color.Gray
                )
            )
        )
      bottom = new VBox(4):
        alignment = Pos.Center
        padding = Insets(16, 0, 0, 0)
        children = Seq(
          new Label("← → Move  |  ↑ Rotate  |  ↓ Soft Drop  |  Space Hard Drop  |  C Hold  |  P Pause  |  Esc Menu"):
            font = scalafx.scene.text.Font("Monospace", 11)
            textFill = Color.Gray
          ,
          statusLabel
        )
    
    // Keyboard handling
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
          case KeyCode.Escape => returnToMenu()
          case _ =>
      else if runner.state.isPaused then
        e.code match
          case KeyCode.P => runner.resume()
          case KeyCode.Escape => returnToMenu()
          case _ =>
      else if runner.state.isGameOver then
        e.code match
          case KeyCode.Escape => returnToMenu()
          case _ =>
  
    // Request focus after scene is set on stage
    Platform.runLater { () =>
      scene.root.value.requestFocus()
    }
  
  private def updateView(): Unit =
    val state = runner.state
    // Redraw only when something actually changed (input, gravity, pause...).
    if !lastRenderedState.contains(state) then
      lastRenderedState = Some(state)
      renderBoard(state.board)
      renderGhostPiece(state)
      renderCurrentPiece(state.currentPiece)
      renderNextPiece(state.nextPiece)
      renderHeldPiece(state.heldPiece)
      updateLabels(state)
      updateOverlays(state)
  
  private def renderBoard(board: Board): Unit =
    boardPane.children.clear()
    // Draw subtle grid first
    for
      row <- 0 until board.height
      col <- 0 until board.width
    do
      val gridRect = new Rectangle:
        layoutX = col * cellSize
        layoutY = row * cellSize
        width = cellSize
        height = cellSize
        fill = Color.Transparent
        stroke = Color.color(0.2, 0.2, 0.3, 0.3)
        strokeWidth = 0.5
      boardPane.children.add(gridRect)
    
    // Draw occupied cells
    for
      row <- 0 until board.height
      col <- 0 until board.width
    do
      board.cellAt(col, row).foreach {
        case extetris.model.Cell.Filled(_, color) =>
          val rect = new Rectangle:
            layoutX = col * cellSize
            layoutY = row * cellSize
            width = cellSize - 1
            height = cellSize - 1
            fill = color.toScalafxColor
            stroke = Color.Black
            strokeWidth = 0.5
            style = "-fx-effect: dropshadow(gaussian, black, 2, 0.3, 0, 1);"
          boardPane.children.add(rect)
        case _ =>
      }
  
  private def renderGhostPiece(state: GameState): Unit =
    state.currentPiece.foreach { piece =>
      val ghost = getGhostPosition(piece, state.board)
      ghost.cells.foreach { pos =>
        if pos.y >= 0 then
          val rect = new Rectangle:
            layoutX = pos.x * cellSize
            layoutY = pos.y * cellSize
            width = cellSize - 1
            height = cellSize - 1
            fill = piece.color.toScalafxColor.deriveColor(0, 1, 1, 0.25)
            stroke = piece.color.toScalafxColor.deriveColor(0, 1, 1, 0.5)
            strokeWidth = 1.5
            strokeType = StrokeType.Outside
            strokeDashOffset = 0
            strokeDashArray.addAll(6d, 4d)
          boardPane.children.add(rect)
      }
    }
  
  private def getGhostPosition(piece: Piece, board: Board): Piece =
    var ghost = piece
    while board.canPlace(ghost.move(0, 1)) do
      ghost = ghost.move(0, 1)
    ghost
  
  private def renderCurrentPiece(piece: Option[Piece]): Unit =
    piece.foreach { p =>
      p.cells.foreach { pos =>
        if pos.y >= 0 then
          val rect = new Rectangle:
            layoutX = pos.x * cellSize
            layoutY = pos.y * cellSize
            width = cellSize - 1
            height = cellSize - 1
            fill = p.color.toScalafxColor
            stroke = Color.White
            strokeWidth = 1.5
            style = "-fx-effect: dropshadow(gaussian, black, 3, 0.4, 0, 1);"
          boardPane.children.add(rect)
      }
    }
  
  private def renderNextPiece(piece: Option[Piece]): Unit =
    nextPiecePane.children.clear()
    piece.foreach { p =>
      val (minX, minY, _, _) = p.rotation.boundingBox
      p.rotation.cells.foreach { pos =>
        val rect = new Rectangle:
          layoutX = (pos.x - minX) * cellSize + cellSize
          layoutY = (pos.y - minY) * cellSize + cellSize
          width = cellSize - 1
          height = cellSize - 1
          fill = p.color.toScalafxColor
          stroke = Color.White
          strokeWidth = 1
          style = "-fx-effect: dropshadow(gaussian, black, 2, 0.3, 0, 1);"
        nextPiecePane.children.add(rect)
      }
    }
  
  private def renderHeldPiece(definition: Option[extetris.pieces.PieceDefinition]): Unit =
    heldPiecePane.children.clear()
    definition.foreach { defn =>
      val rotation = defn.initialRotation
      val (minX, minY, _, _) = rotation.boundingBox
      rotation.cells.foreach { pos =>
        val rect = new Rectangle:
          layoutX = (pos.x - minX) * cellSize + cellSize
          layoutY = (pos.y - minY) * cellSize + cellSize
          width = cellSize - 1
          height = cellSize - 1
          fill = defn.color.toScalafxColor
          stroke = Color.White
          strokeWidth = 1
          style = "-fx-effect: dropshadow(gaussian, black, 2, 0.3, 0, 1);"
        heldPiecePane.children.add(rect)
      }
    }
  
  private def updateLabels(state: GameState): Unit =
    scoreLabel.text = f"Score: ${state.score}%,d"
    levelLabel.text = s"Level: ${state.level}"
    linesLabel.text = s"Lines: ${state.linesCleared}"
  
  private def updateOverlays(state: GameState): Unit =
    val paused = state.isPaused
    pauseOverlay.visible = paused
    pauseOverlay.mouseTransparent = !paused
    if state.isGameOver && !gameOverShown then
      gameOverShown = true
      showGameOver(state)
  
  private def showGameOver(state: GameState): Unit =
    // Stop both loops before leaving the gameplay scene.
    renderTimer.stop()
    runner.stop()
    MainApp.stage.scene = GameOverScene(state, mainMenu).scene
  
  def restart(): Unit =
    runner.restart()
    pauseOverlay.visible = false
    pauseOverlay.mouseTransparent = true
  
  def returnToMenu(): Unit =
    runner.stop()
    renderTimer.stop()
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
  
  private val statusTimer = new PauseTransition(Duration(3000)):
    onFinished = _ => statusLabel.text = ""

  def showStatus(message: String): Unit =
    statusLabel.text = message
    statusTimer.stop()
    statusTimer.playFromStart()
  
  // Start the animation timer for rendering (60 FPS)
  private val renderTimer = AnimationTimer(t => updateView())
  renderTimer.start()
  
  // Cleanup on window close. The scene may not be attached to a window yet,
  // so register as soon as a window becomes available instead of dereferencing null.
  scene.window.onChange { (_, _, window) =>
    if window != null then
      window.onCloseRequest = _ =>
        renderTimer.stop()
        runner.stop()
  }