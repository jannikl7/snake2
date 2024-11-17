import javafx.scene.canvas.Canvas
import kotlin.random.Random

class WormHole(
   override val posX: Double,
   override val posY: Double,
   override val width: Double,
   override val height: Double,
   private val wormHoleGroup: MutableList<WormHole>
) : Obstructing {
   override fun handleCollision(): Obstructing.CollisionEvent {
      return Obstructing.CollisionEvent.JUMP
   }

   override fun render(canvas: Canvas) {
      TODO("Not yet implemented")
   }

   fun getExit(): WormHole{
      var exit: WormHole? = null
      while(exit == null || exit == this) {
         exit = wormHoleGroup[Random.nextInt(wormHoleGroup.size)]
      }
      return exit
   }

}