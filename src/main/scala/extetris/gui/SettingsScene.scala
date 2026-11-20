package extetris.gui

import scalafx.scene.Scene
import scalafx.scene.layout.{VBox, HBox, GridPane, StackPane, Region}
import scalafx.scene.control.{Button, Label, CheckBox, ComboBox, Separator, Spinner, TextField, ToggleButton, ScrollPane}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import scalafx.Includes._
import scalafx.application.Platform
import scala.reflect.Selectable.reflectiveSelectable
import extetris.config.{VariantConfig, ScoringConfig, LevelConfig, PieceGenerationConfig}
import extetris.pieces.PieceId

class SettingsScene(previousScene: { def scene: Scene }):
  
  val scene: Scene = new Scene:
    root = new StackPane:
      style = "-fx-background-color: #0d0d15;"
      pickOnBounds = false
      children = Seq(
        new ScrollPane():
          fitToWidth = true
          style = "-fx-background: transparent; -fx-background-color: transparent;"
          content = new VBox(24):
            padding = Insets(40)
            alignment = Pos.TopCenter
            maxWidth = 500
            style = "-fx-background-color: transparent;"
            children = Seq(
              new Label("Settings"):
                font = Font("Monospace", 32)
                textFill = Color.White
                style = "-fx-text-fill: white;"
              ,
              new Separator():
                style = "-fx-background-color: #333;"
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
                spacing = 16
                children = Seq(
                  new Button("Back"):
                    prefWidth = 160
                    prefHeight = 44
                    font = Font("Monospace", 16)
                    style = "-fx-background-color: #333; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;"
                    onAction = _ => MainApp.stage.scene = previousScene.scene
                  ,
                  new Button("Reset to Defaults"):
                    prefWidth = 160
                    prefHeight = 44
                    font = Font("Monospace", 16)
                    style = "-fx-background-color: #00ffff; -fx-text-fill: #0d0d15; -fx-font-weight: bold; -fx-background-radius: 4;"
                    onAction = _ => resetDefaults()
                )
            )
        ,
        new VBox:
          alignment = Pos.BottomCenter
          padding = Insets(20)
          // Decorative footer: must never intercept mouse events from the settings controls.
          mouseTransparent = true
          pickOnBounds = false
          children = Seq(
            new Label("ExTetris Platform • Settings"):
              font = Font("Monospace", 10)
              textFill = Color.Gray
          )
      )
    
    // Request focus after scene is set on stage
    Platform.runLater { () =>
      scene.root.value.requestFocus()
    }
   
  private def createBoardSection(): VBox =
    new VBox(12):
      style = "-fx-background-color: #1a1a2e; -fx-background-radius: 8; -fx-border-color: #333; -fx-border-width: 1; -fx-padding: 20;"
      children = Seq(
        new Label("Board Settings"):
          font = Font("Monospace", 18)
          textFill = Color.color(0.5, 0.8, 1.0)
        ,
        new GridPane():
          hgap = 16
          vgap = 12
          addRow(0, new Label("Width:"), createSpinner(4, 50, 10, "boardWidth"))
          addRow(1, new Label("Height:"), createSpinner(4, 100, 20, "boardHeight"))
      )
  
  private def createPieceSection(): VBox =
    new VBox(12):
      style = "-fx-background-color: #1a1a2e; -fx-background-radius: 8; -fx-border-color: #333; -fx-border-width: 1; -fx-padding: 20;"
      children = Seq(
        new Label("Enabled Pieces"):
          font = Font("Monospace", 18)
          textFill = Color.color(0.5, 0.8, 1.0)
        ,
        new VBox(8):
          children = PieceId.allBuiltInIds.toSeq.sortBy(_.value).map { pid =>
            new CheckBox:
              text = pid.value
              selected = true
              font = Font("Monospace", 14)
              textFill = Color.White
              style = "-fx-text-fill: white;"
          }
      )
  
  private def createGameplaySection(): VBox =
    new VBox(12):
      style = "-fx-background-color: #1a1a2e; -fx-background-radius: 8; -fx-border-color: #333; -fx-border-width: 1; -fx-padding: 20;"
      children = Seq(
        new Label("Gameplay"):
          font = Font("Monospace", 18)
          textFill = Color.color(0.5, 0.8, 1.0)
        ,
        new GridPane():
          hgap = 16
          vgap = 12
          addRow(0, new Label("Initial Level:"), createSpinner(1, 20, 1, "initialLevel"))
          addRow(1, new Label("Preview Count:"), createSpinner(0, 5, 1, "previewCount"))
          addRow(2, new Label("Allow Hold:"), new CheckBox { selected = true; textFill = Color.White })
          addRow(3, new Label("Allow Hard Drop:"), new CheckBox { selected = true; textFill = Color.White })
          addRow(4, new Label("Gravity Enabled:"), new CheckBox { selected = true; textFill = Color.White })
          addRow(5, new Label("Generation Strategy:"), new ComboBox[String](Seq("Uniform", "Weighted", "Bag")) { value = "Weighted"; style = "-fx-background-color: #1a1a2e; -fx-text-fill: white;" })
      )
  
  private def createScoringSection(): VBox =
    new VBox(12):
      style = "-fx-background-color: #1a1a2e; -fx-background-radius: 8; -fx-border-color: #333; -fx-border-width: 1; -fx-padding: 20;"
      children = Seq(
        new Label("Scoring"):
          font = Font("Monospace", 18)
          textFill = Color.color(0.5, 0.8, 1.0)
        ,
        new GridPane():
          hgap = 16
          vgap = 12
          addRow(0, new Label("Single Line:"), createTextField("100", "singleLine"))
          addRow(1, new Label("Double Line:"), createTextField("300", "doubleLine"))
          addRow(2, new Label("Triple Line:"), createTextField("500", "tripleLine"))
          addRow(3, new Label("Tetris:"), createTextField("800", "tetrisLine"))
          addRow(4, new Label("Soft Drop/Cell:"), createTextField("1", "softDrop"))
          addRow(5, new Label("Hard Drop/Cell:"), createTextField("2", "hardDrop"))
          addRow(6, new Label("Level Multiplier:"), new CheckBox { selected = true; textFill = Color.White })
      )
  
  private def createSpinner(min: Int, max: Int, default: Int, name: String): Spinner[Int] =
    new Spinner[Int](min, max, default):
      editable = true
      prefWidth = 100
      style = "-fx-background-color: #1a1a2e; -fx-text-fill: white; -fx-border-color: #333;"
  
  private def createTextField(default: String, name: String): TextField =
    new TextField():
      text = default
      prefWidth = 100
      style = "-fx-background-color: #1a1a2e; -fx-text-fill: white; -fx-border-color: #333;"
  
  private def resetDefaults(): Unit =
    println("Reset to defaults")