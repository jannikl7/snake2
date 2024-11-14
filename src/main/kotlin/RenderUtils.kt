import javafx.geometry.Bounds
import javafx.geometry.VPos
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment


fun renderLevelComplete(canvas: Canvas) {
   val gContext: GraphicsContext = canvas.graphicsContext2D
   gContext.fill = Color.DARKGREEN
   gContext.font = Font.font("Verdana", FontWeight.BOLD, 30.0)
   val text = "LEVEL COMPLETED!!!"
   val textBounds = getTextBounds(text, gContext.font)
   val textX = (canvas.width-textBounds.width)/2
   val textY = (canvas.height-textBounds.height)/2
   val margin = 30

   gContext.fillRect(
      textX-margin,
      textY-margin,
      textBounds.width + (2*margin),
      textBounds.height + (2*margin)
   )
   gContext.stroke = Color.BLACK
   gContext.strokeRect(
      textX-margin,
      textY-margin,
      textBounds.width + (2*margin),
      textBounds.height + (2*margin)
   )

   gContext.fill = Color.DARKORANGE
   gContext.textAlign = TextAlignment.LEFT
   gContext.textBaseline = VPos.TOP
   gContext.fillText(text, textX, textY)
}

fun renderGameOver(canvas: Canvas) {

}

fun getTextBounds(text: String, font: Font): Bounds {
   val textNode = Text(text)
   textNode.font = font
   return textNode.layoutBounds
}

fun renderScene(canvas: Canvas, snake: SnakeController, pointsLabel: Label, points: Int, bgImg: Image, level: Levels.Level, gameAction: SnakeGame.GameAction) {
   val gContext: GraphicsContext = canvas.graphicsContext2D

   pointsLabel.text = "Points: $points"

   gContext.fill = Color.DIMGREY
   gContext.drawImage(bgImg, 0.0, 0.0)
   // gContext.fillRect(0.0, 0.0, canvas.width, canvas.height)

   //renderGrid()

   //render head
   snake.renderHead(canvas)

   //add snakes body
   snake.body.forEach { segment ->
      segment.render(canvas)
   }

   //add obstacles
   level.board.forEach { item ->
      item.render(canvas)
   }
   when (gameAction) {
      SnakeGame.GameAction.GAME_OVER -> {
         gContext.fillText("GAME OVER", 200.0, 200.0)
      }
      SnakeGame.GameAction.LEVEL_COMPLETED -> {
         renderLevelComplete(canvas)
      }
   else -> Unit
}

}