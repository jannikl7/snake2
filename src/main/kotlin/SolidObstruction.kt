import javafx.scene.canvas.Canvas
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import java.awt.Color

class SolidObstruction(
   override val posX: Double,
   override val posY: Double,
   override val width: Double,
   override val height: Double
) : Obstructing {
   override fun handleCollision(): Obstructing.CollisionEvent {
      return Obstructing.CollisionEvent.KILL
   }

   override fun render(canvas: Canvas) {
      val gContext = canvas.graphicsContext2D
      val gradient = RadialGradient(
         0.0, 0.0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
         Stop(0.0, javafx.scene.paint.Color.RED), Stop(1.0, javafx.scene.paint.Color.BLACK)
      )

      gContext.fill = gradient
      gContext.fillRect(posX, posY, width, height)

   }

}