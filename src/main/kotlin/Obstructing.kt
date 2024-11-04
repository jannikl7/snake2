import javafx.scene.canvas.Canvas

interface Obstructing {
    val posX: Double
    val posY: Double
    val height: Double
    val width: Double

    fun handleCollision(): CollisionEvent

    fun render(canvas: Canvas)

    enum class CollisionEvent {
        KILL,
        GROW,
    }
}
