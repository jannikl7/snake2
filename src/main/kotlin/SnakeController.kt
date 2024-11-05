import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.paint.Color.MEDIUMPURPLE

class SnakeController(var posX: Double, var posY: Double, var direction: Direction, private val onAddSegment: (Obstructing) -> Unit) {
    companion object {
        const val WIDTH: Double = 20.0
        const val HEIGHT: Double = 20.0
    }
    val head: SnakeSegment = SnakeSegment(posX, posY, WIDTH, HEIGHT, direction)
    private var _mouthOpen = false // Backing field to hold the actual state
    val mouthOpen: Boolean
        get() {
            _mouthOpen = !_mouthOpen // Toggle the value
            return _mouthOpen
        }

    val innerBody: MutableList<SnakeSegment> = mutableListOf()
    val body: MutableList<SnakeSegment>
        get() = innerBody

    fun move(action: MoveAction) {
        if(action == MoveAction.GROW) {
            //push new element with head pos
            grow(1)
            onAddSegment(innerBody.first())
        } else  {
            innerBody.asReversed().forEachIndexed { reversedIndex, segment ->
                val originalIndex = innerBody.size - 1 - reversedIndex
                if (originalIndex == 0) {
                    segment.posY = head.posY
                    segment.posX = head.posX
                } else {
                    segment.posY = innerBody.elementAt(originalIndex - 1).posY
                    segment.posX = innerBody.elementAt(originalIndex - 1).posX
                }
            }
        }
        when(head.direction) {
            Direction.NORTH ->
                head.posY -= HEIGHT
            Direction.SOUTH ->
                head.posY += HEIGHT
            Direction.EAST ->
                head.posX += WIDTH
            Direction.WEST ->
                head.posX -= WIDTH
        }
    }

    fun turn(direction: Direction) {
        head.direction = direction
    }

    fun grow(segments: Int) {
        innerBody.addFirst(SnakeSegment(head.posX, head.posY, WIDTH, HEIGHT, head.direction))
    }

    fun renderHead(canvas: Canvas) {
        //draw head
        val gContext: GraphicsContext = canvas.graphicsContext2D
        val segmentOffsetX = SnakeController.WIDTH/2
        val segmentOffsetY = SnakeController.HEIGHT/2
        gContext.save()
        gContext.translate(head.posX, head.posY)
        when(head.direction) {
            Direction.NORTH -> gContext.rotate(270.0)
            Direction.SOUTH -> gContext.rotate(90.0)
            Direction.EAST -> gContext.rotate(0.0)
            Direction.WEST -> gContext.scale(-1.0, 1.0)
        }
        when(mouthOpen) {
            true -> {
                val xPoints = doubleArrayOf(
                    -segmentOffsetX,
                    -segmentOffsetX+SnakeController.WIDTH,
                    0.0,
                    -segmentOffsetX+SnakeController.WIDTH,
                    -segmentOffsetX)
                val yPoints = doubleArrayOf(
                    -segmentOffsetY,
                    -segmentOffsetY,
                    0.0,
                    -segmentOffsetY+SnakeController.HEIGHT,
                    -segmentOffsetY+SnakeController.HEIGHT)
                gContext.fill = MEDIUMPURPLE
                gContext.fillPolygon(xPoints, yPoints, xPoints.size)
                gContext.fill = Color.GREEN
                gContext.fillOval(0.0, -(SnakeController.HEIGHT/2), 4.0, 4.0)
            }
            false -> {

                val xPoints = doubleArrayOf(
                    -segmentOffsetX,
                    -segmentOffsetX+SnakeController.WIDTH,
                    -segmentOffsetX+SnakeController.WIDTH,
                    0.0,
                    -segmentOffsetX+SnakeController.WIDTH,
                    -segmentOffsetX+SnakeController.WIDTH,
                    -segmentOffsetX)
                val yPoints = doubleArrayOf(
                    -segmentOffsetY,
                    -segmentOffsetY,
                    0.0,
                    0.0,
                    0.0,
                    -segmentOffsetY+SnakeController.HEIGHT,
                    -segmentOffsetY+SnakeController.HEIGHT)
                gContext.fill = MEDIUMPURPLE
                gContext.fillPolygon(xPoints, yPoints, xPoints.size)
                gContext.fill = Color.GREEN
                gContext.fillOval(0.0, -(SnakeController.HEIGHT/2), 4.0, 4.0)
                gContext.fill = Color.DIMGREY
                gContext.strokeLine(0.0, 0.0, SnakeController.WIDTH/2, 0.0)
            }
        }
        gContext.restore()
    }

    data class SnakeSegment(
        override var posX: Double,
        override var posY: Double,
        override val height: Double,
        override val width: Double,
        var direction: Direction
    ) : Obstructing {
        override fun handleCollision(): Obstructing.CollisionEvent {
            return Obstructing.CollisionEvent.KILL
        }

        override fun render(canvas: Canvas) {
            val segmentOffsetX = WIDTH /2
            val segmentOffsetY = HEIGHT /2
            val gContext = canvas.graphicsContext2D
            gContext.fill = MEDIUMPURPLE
            gContext.fillRoundRect(
                this.posX - segmentOffsetX,
                this.posY - segmentOffsetY,
                WIDTH,
                HEIGHT,
                WIDTH /2,
                HEIGHT /2
            )
        }
    }
}

