package extetris.gui

import scalafx.scene.Scene
import scalafx.scene.layout.{VBox, HBox, StackPane, Region}
import scalafx.scene.control.{Button, Label, ComboBox, Separator}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import scalafx.Includes._
import scalafx.stage.FileChooser
import scalafx.application.Platform
import extetris.config.{VariantConfig, ScoringConfig, LevelConfig, PieceGenerationConfig}
import extetris.pieces.PieceId
import extetris.runner.GameRunner
import extetris.examples.ExampleVariants
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutor

class MainMenuScene:
  given ExecutionContextExecutor = ExecutionContext.global
  
  private val variantOptions = Map(
    "Classic ExTetris" -> VariantConfig.classic,
    "Extended ExTetris" -> VariantConfig.extended,
    "Default" -> VariantConfig.default,
    "Mini ExTetris" -> ExampleVariants.mini,
    "Giant ExTetris" -> ExampleVariants.giant,
    "Challenge ExTetris" -> ExampleVariants.challenge
  )
  
  private var selectedVariant: VariantConfig = VariantConfig.classic
  
  val scene: Scene = new Scene:
    root = new StackPane:
      style = "-fx-background-color: #0d0d15;"
      pickOnBounds = false
      children = Seq(
        new VBox(32):
          alignment = Pos.Center
          padding = Insets(60)
          maxWidth = 480
          pickOnBounds = false
          style = "-fx-background-color: transparent;"
          children = Seq(
            new Label("ExTetris Platform"):
              font = Font("Monospace", 42)
              textFill = Color.White
              style = "-fx-text-fill: white; -fx-effect: dropshadow(gaussian, #00ffff, 30, 0.5, 0, 0);"
            ,
            new Label("An extensible Tetris platform"):
              font = Font("Monospace", 16)
              textFill = Color.Gray
            ,
            new Separator():
              style = "-fx-background-color: #333;"
              maxWidth = 300
            ,
            new VBox(16):
              alignment = Pos.Center
              spacing = 12
              children = Seq(
                new Label("Select Variant:"):
                  font = Font("Monospace", 18)
                  textFill = Color.White
                ,
                new ComboBox[String](variantOptions.keys.toSeq.sorted):
                  value = "Classic ExTetris"
                  prefWidth = 300
                  style = "-fx-background-color: #1a1a2e; -fx-text-fill: white; -fx-border-color: #333; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4; -fx-font-family: Monospace; -fx-font-size: 14px;"
                  onAction = _ => selectedVariant = variantOptions(value.value)
              )
            ,
            new VBox(16):
              alignment = Pos.Center
              spacing = 12
              children = Seq(
                new Button("Start Game"):
                  prefWidth = 280
                  prefHeight = 56
                  font = Font("Monospace", 20)
                  style = "-fx-background-color: #00ffff; -fx-text-fill: #0d0d15; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, #00ffff, 20, 0.3, 0, 0);"
                  onAction = _ => startGame()
                ,
                new Button("Settings"):
                  prefWidth = 280
                  prefHeight = 48
                  font = Font("Monospace", 16)
                  style = "-fx-background-color: #333; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-radius: 8;"
                  onAction = _ => showSettings()
                ,
                new Button("Load Game"):
                  prefWidth = 280
                  prefHeight = 48
                  font = Font("Monospace", 16)
                  style = "-fx-background-color: #333; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-radius: 8;"
                  onAction = _ => loadGame()
                ,
                new Button("Exit"):
                  prefWidth = 280
                  prefHeight = 48
                  font = Font("Monospace", 16)
                  style = "-fx-background-color: #1a1a2e; -fx-text-fill: #888; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #333; -fx-border-width: 1;"
                  onAction = _ => sys.exit(0)
              )
            ,
            new VBox(8):
              alignment = Pos.Center
              spacing = 4
              children = Seq(
                new Label("Controls: ← → Move  |  ↑ Rotate  |  ↓ Soft Drop  |  Space Hard Drop  |  C Hold  |  P Pause"):
                  font = Font("Monospace", 12)
                  textFill = Color.Gray
                ,
                new Label("Esc returns to menu at any time"):
                  font = Font("Monospace", 11)
                  textFill = Color.Gray
              )
          )
        ,
        new VBox:
          alignment = Pos.BottomCenter
          padding = Insets(30)
          children = Seq(
            new Label("ExTetris Platform • Principles of Programming Languages"):
              font = Font("Monospace", 11)
              textFill = Color.Gray
          )
      )
   
  private def startGame(): Unit =
    val runner = GameRunner(selectedVariant)
    val gameScene = GameScene(runner, selectedVariant, this)
    MainApp.stage.scene = gameScene.scene
    runner.start()
  
  private def showSettings(): Unit =
    MainApp.stage.scene = SettingsScene(this).scene
  
  private def loadGame(): Unit =
    val fileChooser = new FileChooser()
    fileChooser.setTitle("Load Game")
    fileChooser.getExtensionFilters.add(new FileChooser.ExtensionFilter("ExTetris Save", "*.json"))
    val file = fileChooser.showOpenDialog(MainApp.stage)
    if file != null then
      val loaded = extetris.persistence.GamePersistence.loadFromFile(file)
      loaded match
        case Right(state) =>
          val runner = GameRunner.fromState(state)
          val gameScene = GameScene(runner, selectedVariant, this)
          MainApp.stage.scene = gameScene.scene
          runner.start()
        case Left(e) =>
          println(s"Failed to load game: ${e.getMessage}")