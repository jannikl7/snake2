
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.scene.paint.Color.MEDIUMPURPLE
import javafx.stage.Stage
import kotlinx.coroutines.*
import kotlin.random.Random


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    Application.launch(SnakeGame::class.java)
}

class SnakeGame() : Application() {
    val canvasHeight = 400.0
    val canvasWidth = 400.0
    val canvas: Canvas = Canvas(canvasWidth, canvasHeight)
    val layout = BorderPane()

    val directions = Direction.entries // Get all enum constants
    val randomIndex = Random.nextInt(directions.size) // Generate a random index

    val obstructions: MutableList<Obstructing> = mutableListOf()
    val snake: Snake = Snake(canvas.width / 2, canvas.height / 2, directions[randomIndex]) { obstruction ->
        obstructions.add(obstruction)
    }
    var nextAction: MoveAction = MoveAction.NONE
    var gameAction: GameAction = GameAction.NOT_STARTED

    enum class GameAction {
        NOT_STARTED,
        PLAYING,
        GAME_OVER,
    }

    private val job = SupervisorJob() // Manage coroutine lifecycle
    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun start(stage: Stage) {
        val start = Button("Snake")
        start.setOnMouseClicked { event ->
            initializeNewGame( canvasWidth, canvasHeight)

        }
        canvas.setOnKeyPressed { event ->
            when (event.code) {
                KeyCode.UP -> snake.turn(Direction.NORTH)
                KeyCode.DOWN -> snake.turn( Direction.SOUTH)
                KeyCode.LEFT -> snake.turn(Direction.WEST)
                KeyCode.RIGHT -> snake.turn(Direction.EAST)
                KeyCode.SPACE -> nextAction = MoveAction.GROW
                else -> Unit //no action
            }
        }
        layout.center = start


        val scene = Scene(layout, 400.0, 450.0)
        stage.scene = scene
        stage.title = "Snake Game"
        stage.show()
    }

    fun initializeNewGame(width: Double, height: Double) {
        layout.center = canvas
        canvas.isFocusTraversable = true // Allow canvas to receive focus
        canvas.requestFocus() // Request focus for the canvas
        gameAction = GameAction.PLAYING
        // start game loop
        scope.launch {
            while(true) {
                updateGameState()
                render()
                delay(500L)
            }
        }
    }

    fun render() {
        val gContext: GraphicsContext = canvas.graphicsContext2D
        val segmentOffsetX = Snake.WIDTH/2
        val segmentOffsetY = Snake.HEIGHT/2

        gContext.fill = Color.DIMGREY
        gContext.fillRect(0.0, 0.0, canvas.width, canvas.height)

        //draw head
        gContext.save()
        gContext.translate(snake.head.posX, snake.head.posY)
        when(snake.head.direction) {
            Direction.NORTH -> gContext.rotate(270.0)
            Direction.SOUTH -> gContext.rotate(90.0)
            Direction.EAST -> gContext.rotate(0.0)
            Direction.WEST -> gContext.scale(-1.0, 1.0)
        }
        when(snake.mouthOpen) {
            true -> {
                val xPoints = doubleArrayOf(
                    -segmentOffsetX,
                    -segmentOffsetX+Snake.WIDTH,
                    0.0,
                    -segmentOffsetX+Snake.WIDTH,
                    -segmentOffsetX)
                val yPoints = doubleArrayOf(
                    -segmentOffsetY,
                    -segmentOffsetY,
                    0.0,
                    -segmentOffsetY+Snake.HEIGHT,
                    -segmentOffsetY+Snake.HEIGHT)
                gContext.fill = MEDIUMPURPLE
                gContext.fillPolygon(xPoints, yPoints, xPoints.size)
                gContext.fill = Color.GREEN
                gContext.fillOval(0.0, -(Snake.HEIGHT/2), 4.0, 4.0)
            }
            false -> {

                val xPoints = doubleArrayOf(
                    -segmentOffsetX,
                    -segmentOffsetX+Snake.WIDTH,
                    -segmentOffsetX+Snake.WIDTH,
                    0.0,
                    -segmentOffsetX+Snake.WIDTH,
                    -segmentOffsetX+Snake.WIDTH,
                    -segmentOffsetX)
                val yPoints = doubleArrayOf(
                    -segmentOffsetY,
                    -segmentOffsetY,
                    0.0,
                    0.0,
                    0.0,
                    -segmentOffsetY+Snake.HEIGHT,
                    -segmentOffsetY+Snake.HEIGHT)
                gContext.fill = MEDIUMPURPLE
                gContext.fillPolygon(xPoints, yPoints, xPoints.size)
                gContext.fill = Color.GREEN
                gContext.fillOval(0.0, -(Snake.HEIGHT/2), 4.0, 4.0)
                gContext.fill = Color.DIMGREY
                gContext.strokeLine(0.0, 0.0, Snake.WIDTH/2, 0.0)
            }
        }
        gContext.restore()


        gContext.fill = MEDIUMPURPLE
        snake.body.forEach { segment ->
            gContext.fillRoundRect(
                segment.posX - segmentOffsetX,
                segment.posY - segmentOffsetY,
                Snake.WIDTH,
                Snake.HEIGHT,
                Snake.WIDTH/2,
                Snake.HEIGHT/2
            )
        }
        if(gameAction == GameAction.GAME_OVER) {
            gContext.fillText("GAME OVER", 200.0, 200.0)
        }
    }

    fun updateGameState() {
        when (gameAction) {
            GameAction.GAME_OVER ->
                endGame() //game is over
            GameAction.PLAYING -> {
                //move snake head
                snake.move(nextAction);
                nextAction = MoveAction.NONE

                //check collition
                //check if head outside canvas
                if (
                    snake.head.posY - Snake.HEIGHT / 2 < 0 ||
                    snake.head.posY + Snake.HEIGHT / 2 > canvas.height ||
                    snake.head.posX - Snake.WIDTH / 2 < 0 ||
                    snake.head.posX + Snake.WIDTH / 2 > canvas.width
                ) {
                    gameAction = GameAction.GAME_OVER
                }

                obstructions.forEach { obstruction ->
                    if(isColliding(snake.head, obstruction))
                        gameAction = GameAction.GAME_OVER;
                }

            }
            else -> Unit
        }
    }

    fun isColliding(objA: Snake.SnakeSegment, objB: Obstructing): Boolean {
        return objA.posX  <= objB.posX + Snake.WIDTH/2 &&
                objA.posX >= objB.posX-Snake.WIDTH/2 &&
                objA.posY <= objB.posY + Snake.WIDTH/2 &&
                objA.posY >= objB.posY - Snake.WIDTH/2
    }

    fun endGame() {
        //show gameover
        job.cancel()
    }

}

class Snake(var posX: Double, var posY: Double, var direction: Direction, private val onAddSegment: (Obstructing) -> Unit) {
    val head: SnakeSegment = SnakeSegment(posX, posY, direction)
    private var _mouthOpen = false // Backing field to hold the actual state
    val mouthOpen: Boolean
        get() {
            _mouthOpen = !_mouthOpen // Toggle the value
            return _mouthOpen
        }

    val innerBody: MutableList<SnakeSegment> = mutableListOf()
    val body: MutableList<SnakeSegment>
        get() = innerBody
    companion object {
       const val WIDTH: Double = 20.0
       const val HEIGHT: Double = 20.0
    }

    fun move(action: MoveAction) {
        if(action == MoveAction.GROW) {
            //push new element with head pos
            innerBody.addFirst(SnakeSegment(head.posX, head.posY, head.direction))
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

    data class SnakeSegment(
        override var posX: Double,
        override var posY: Double,
        var direction: Direction
    ) : Obstructing
}

enum class MoveAction {
    NONE,
    GROW,
}

enum class Direction {
    NORTH,
    SOUTH,
    EAST,
    WEST
}

interface Obstructing {
    val posX: Double
    val posY: Double
}
