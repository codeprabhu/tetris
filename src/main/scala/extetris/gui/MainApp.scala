package extetris.gui

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.stage.Stage
import scalafx.application.Platform

object MainApp extends JFXApp3:
  override def start(): Unit =
    val mainMenu = MainMenuScene()
    stage = new JFXApp3.PrimaryStage:
      title = "ExTetris Platform"
      width = 800
      height = 600
      minWidth = 600
      minHeight = 500
      scene = mainMenu.scene
    
    stage.onCloseRequest = _ => System.exit(0)
    
    // Request focus after scene is set on stage
    Platform.runLater { () =>
      mainMenu.scene.root.value.requestFocus()
    }