package extetris.gui

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.stage.Stage

object MainApp extends JFXApp3:
  override def start(): Unit =
    stage = new JFXApp3.PrimaryStage:
      title = "ExTetris Platform"
      width = 800
      height = 600
      minWidth = 600
      minHeight = 500
      scene = MainMenuScene().scene
    
    stage.onCloseRequest = _ => System.exit(0)