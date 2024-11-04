import Obstructing.CollisionEvent.GROW
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color

class FoodItem(override val posX: Double, override val posY: Double, override val height: Double = 20.0,
               override val width:Double = 20.0): Obstructing {

    override fun render(canvas: Canvas) {
        val gcontext = canvas.graphicsContext2D
        gcontext.fill = Color.GREEN

        gcontext.fillOval(posX, posY, width, height)
    }

    override fun handleCollision(): Obstructing.CollisionEvent {
        return GROW
    }

}