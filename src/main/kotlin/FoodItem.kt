import Obstructing.CollisionEvent.GROW
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color

class FoodItem(override val posX: Double, override val posY: Double, override val height: Double = 10.0,
               override val width:Double = 10.0): Obstructing {

    override fun render(canvas: Canvas) {
        val gContext = canvas.graphicsContext2D
        gContext.fill = Color.GREEN

        gContext.fillOval(posX-width/2, posY-height/2, width, height)
    }

    override fun handleCollision(): Obstructing.CollisionEvent {
        return GROW
    }

}