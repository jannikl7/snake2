import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop

class WormHole(
   override val posX: Double,
   override val posY: Double,
   secondPosX: Double,
   secondPosY: Double,
   override val width: Double,
   override val height: Double,
   val color: Color
) : Obstructing {

   private var occupied = false

   private var pairObstruct: Obstruction = Obstruction(
      posX = secondPosX,
      posY = secondPosY,
      height = height,
      width = width
   )

   override fun handleCollision(): Obstructing.CollisionEvent {
      val event: Obstructing.CollisionEvent
      if(occupied) {
         occupied = false
         event = Obstructing.CollisionEvent.WORMHOLE_EXIT
      }else {
         occupied = true
         event = Obstructing.CollisionEvent.WORMHOLE_ENTER
      }
      return event
      }

   override fun render(canvas: Canvas) {
      val gContext = canvas.graphicsContext2D
      val gradient = RadialGradient(
         0.0, 0.0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
         Stop(0.0, color), Stop(1.0, javafx.scene.paint.Color.BLACK)
      )

      gContext.fill = gradient
      gContext.fillRect(posX, posY, width, height)
      gContext.fillRect(pairObstruct.posX, pairObstruct.posY, width, height)
   }

   override fun isColliding(objB: Obstructing): Boolean {
      return super.isColliding(objB) || pairObstruct.isColliding(objB)
   }

   fun getExitPoint(entryX: Double, entryY: Double): Pair<Double, Double>? {
      return when {
         entryX == posX && entryY == posY -> Pair(pairObstruct.posX, pairObstruct.posY)
         entryX == pairObstruct.posX && entryY == pairObstruct.posY -> Pair(posX, posY)
         else -> null
      }
   }

}