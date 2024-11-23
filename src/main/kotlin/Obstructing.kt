import javafx.scene.canvas.Canvas

interface Obstructing {
   val posX: Double
   val posY: Double
   val width: Double
   val height: Double

   fun handleCollision(): CollisionEvent

   fun render(canvas: Canvas)

   fun isColliding(objB: Obstructing): Boolean {
      val halfWidthA = width / 2
      val halfHeightA = height / 2
      val halfWidthB = objB.width / 2
      val halfHeightB = objB.height / 2

      return posX + halfWidthA > objB.posX - halfWidthB && // Right edge of A reaches left edge of B
              posX - halfWidthA < objB.posX + halfWidthB && // Left edge of A reaches right edge of B
              posY + halfHeightA > objB.posY - halfHeightB && // Bottom edge of A reaches top edge of B
              posY - halfHeightA < objB.posY + halfHeightB    // Top edge of A reaches bottom edge of B
   }

   enum class CollisionEvent {
      KILL,
      GROW,
      SHRINK,
      WORMHOLE_ENTER,
      WORMHOLE_EXIT,
   }
}

class Obstruction(
   override val posX: Double,
   override val posY: Double,
   override val width: Double,
   override val height: Double
): Obstructing {
   override fun handleCollision(): Obstructing.CollisionEvent {
      TODO("Not yet implemented")
   }

   override fun render(canvas: Canvas) {
      TODO("Not yet implemented")
   }
}
