package extetris.gui

import scalafx.scene.Scene
import scalafx.scene.layout.{VBox, HBox, StackPane}
import scalafx.scene.control.{Button, Label, ComboBox, CheckBox}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import scalafx.Includes._
import extetris.config.{VariantConfig, ScoringConfig, LevelConfig, PieceGenerationConfig}
import extetris.pieces.PieceId
import extetris.runner.GameRunner
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutor

class MainMenuScene:
  given ExecutionContextExecutor = ExecutionContext.global
  
  private val variantOptions = Map(
    "Classic ExTetris" -> VariantConfig.classic,
    "Extended ExTetris" -> VariantConfig.extended,
    "Default" -> VariantConfig.default
  )
  
  private var selectedVariant: VariantConfig = VariantConfig.classic
  
  val scene: Scene = new Scene:
    root = new VBox(20):
      padding = Insets(40)
      alignment = Pos.Center
      children = Seq(
        new Label("ExTetris Platform"):
          font = Font(36)
          textFill = Color.White
        ,
        new VBox(10):
          alignment = Pos.Center
          children = Seq(
            new Label("Select Variant:"):
              font = Font(18)
              textFill = Color.White
            ,
            new ComboBox[String](variantOptions.keys.toSeq):
              value = "Classic ExTetris"
              onAction = _ => selectedVariant = variantOptions(value.value)
              prefWidth = 250
          )
        ,
        new VBox(15):
          alignment = Pos.Center
          children = Seq(
            new Button("Start Game"):
              prefWidth = 200
              prefHeight = 50
              font = Font(18)
              onAction = _ => startGame()
            ,
            new Button("Settings"):
              prefWidth = 200
              prefHeight = 40
              font = Font(16)
              onAction = _ => showSettings()
            ,
            new Button("Load Game"):
              prefWidth = 200
              prefHeight = 40
              font = Font(16)
              onAction = _ => loadGame()
            ,
            new Button("Exit"):
              prefWidth = 200
              prefHeight = 40
              font = Font(16)
              onAction = _ => sys.exit(0)
          )
      )
      style = "-fx-background-color: #1a1a2e;"
  
  private def startGame(): Unit =
    val runner = GameRunner(selectedVariant)
    val gameScene = GameScene(runner, selectedVariant, this)
    MainApp.stage.scene = gameScene.scene
    runner.start()
  
  private def showSettings(): Unit =
    MainApp.stage.scene = SettingsScene(this).scene
  
  private def loadGame(): Unit =
    val fileChooser = new javafx.stage.FileChooser()
    fileChooser.setTitle("Load Game")
    fileChooser.getExtensionFilters.add(new javafx.stage.FileChooser.ExtensionFilter("ExTetris Save", "*.json"))
    val file = fileChooser.showOpenDialog(MainApp.stage)
    if file != null then
      val runner = GameRunner.fromState(extetris.persistence.GamePersistence.loadFromFile(file).getOrElse(throw new Exception("Failed to load game")))
      val gameScene = GameScene(runner, selectedVariant, this)
      MainApp.stage.scene = gameScene.scene
      runner.start()