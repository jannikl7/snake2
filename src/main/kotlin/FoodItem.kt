import Obstructing.CollisionEvent.GROW
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color

class FoodItem(
   override val posX: Double,
   override val posY: Double,
   override val height: Double,
   override val width: Double,
   val editable: Boolean = true
) : Obstructing {

   val removeTime: Long = System.currentTimeMillis() + 10000
   private val centerX = posX+width/2
   private val centerY = posY+height/2

   override fun render(canvas: Canvas) {
      val gContext = canvas.graphicsContext2D
      gContext.fill = if (editable) Color.GREEN else Color.RED

      gContext.fillOval(
         centerX-width/4,
         centerY-height/4,
         width/2,
         height/2
      )
   }

   override fun handleCollision(): Obstructing.CollisionEvent {
      return if (editable) GROW else Obstructing.CollisionEvent.SHRINK
   }

}