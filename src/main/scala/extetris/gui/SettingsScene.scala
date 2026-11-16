package extetris.gui

import scalafx.scene.Scene
import scalafx.scene.layout.{VBox, HBox, GridPane}
import scalafx.scene.control.{Button, Label, CheckBox, ComboBox, ScrollPane, Separator, Spinner, TextField}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import scalafx.Includes._
import scala.reflect.Selectable.reflectiveSelectable
import extetris.config.{VariantConfig, ScoringConfig, LevelConfig, PieceGenerationConfig}
import extetris.pieces.PieceId

class SettingsScene(previousScene: { def scene: Scene }):
  
  val scene: Scene = new Scene:
    root = new ScrollPane:
      fitToWidth = true
      content = new VBox(20):
        padding = Insets(30)
        alignment = Pos.TopCenter
        style = "-fx-background-color: #1a1a2e;"
        children = Seq(
          new Label("Settings"):
            font = Font(32)
            textFill = Color.White
          ,
          new Separator():
            style = "-fx-background-color: #444;"
          ,
          createBoardSection()
          ,
          createPieceSection()
          ,
          createGameplaySection()
          ,
          createScoringSection()
          ,
          new HBox(20):
            alignment = Pos.Center
            children = Seq(
              new Button("Back"):
                prefWidth = 150
                onAction = _ => MainApp.stage.scene = previousScene.scene
              ,
              new Button("Reset to Defaults"):
                prefWidth = 150
                onAction = _ => resetDefaults()
            )
        )
  
  private def createBoardSection(): VBox =
    new VBox(10):
      children = Seq(
        new Label("Board Settings"):
          font = Font(20)
          textFill = Color.LightBlue
        ,
        new GridPane():
          hgap = 10
          vgap = 10
          addRow(0, new Label("Width:"), createSpinner(4, 50, 10))
          addRow(1, new Label("Height:"), createSpinner(4, 100, 20))
      )
  
  private def createPieceSection(): VBox =
    new VBox(10):
      children = Seq(
        new Label("Enabled Pieces"):
          font = Font(20)
          textFill = Color.LightBlue
        ,
        new VBox(5):
          children = PieceId.allBuiltInIds.toSeq.sortBy(_.value).map { pid =>
            new CheckBox:
              text = pid.value
              selected = true
              textFill = Color.White
              font = Font(14)
          }
      )
  
  private def createGameplaySection(): VBox =
    new VBox(10):
      children = Seq(
        new Label("Gameplay"):
          font = Font(20)
          textFill = Color.LightBlue
        ,
        new GridPane():
          hgap = 10
          vgap = 10
          addRow(0, new Label("Initial Level:"), createSpinner(1, 20, 1))
          addRow(1, new Label("Preview Count:"), createSpinner(0, 5, 1))
          addRow(2, new Label("Allow Hold:"), new CheckBox { selected = true })
          addRow(3, new Label("Allow Hard Drop:"), new CheckBox { selected = true })
          addRow(4, new Label("Gravity Enabled:"), new CheckBox { selected = true })
          addRow(5, new Label("Generation Strategy:"), new ComboBox[String](Seq("Uniform", "Weighted", "Bag")) { value = "Weighted" })
      )
  
  private def createScoringSection(): VBox =
    new VBox(10):
      children = Seq(
        new Label("Scoring"):
          font = Font(20)
          textFill = Color.LightBlue
        ,
        new GridPane():
          hgap = 10
          vgap = 10
          addRow(0, new Label("Single Line:"), createTextField("100"))
          addRow(1, new Label("Double Line:"), createTextField("300"))
          addRow(2, new Label("Triple Line:"), createTextField("500"))
          addRow(3, new Label("Tetris:"), createTextField("800"))
          addRow(4, new Label("Soft Drop/Cell:"), createTextField("1"))
          addRow(5, new Label("Hard Drop/Cell:"), createTextField("2"))
          addRow(6, new Label("Level Multiplier:"), new CheckBox { selected = true })
      )
  
  private def createSpinner(min: Int, max: Int, default: Int): Spinner[Int] =
    new Spinner[Int](min, max, default):
      editable = true
      prefWidth = 80
  
  private def createTextField(default: String): TextField =
    new TextField:
      text = default
      prefWidth = 80
  
  private def resetDefaults(): Unit =
    println("Reset to defaults")