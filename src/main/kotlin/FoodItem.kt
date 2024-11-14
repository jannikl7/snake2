import Obstructing.CollisionEvent.GROW
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color

class FoodItem(
   override val posX: Double, override val posY: Double, override val height: Double = 10.0,
   override val width: Double = 10.0, val editable: Boolean = true
) : Obstructing {

   val removeTime: Long = System.currentTimeMillis() + 10000
   val centerX = posX+SnakeGame.itemWidth/2
   val centerY = posY+SnakeGame.itemHeight/2

   override fun render(canvas: Canvas) {
      val gContext = canvas.graphicsContext2D
      gContext.fill = if (editable) Color.GREEN else Color.RED

      gContext.fillOval(
         centerX-width/2,
         centerY-height/2,
         width,
         height
      )
   }

   override fun handleCollision(): Obstructing.CollisionEvent {
      return if (editable) GROW else Obstructing.CollisionEvent.SHRINK
   }

}