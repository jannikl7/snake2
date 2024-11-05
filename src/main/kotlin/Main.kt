
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
    val itemWidth = 20.0
    val itemHeight = 20.0
    val canvasHeight = itemHeight*20
    val canvasWidth = itemWidth*20
    val canvas: Canvas = Canvas(canvasWidth, canvasHeight)
    val layout = BorderPane()

    val directions = Direction.entries // Get all enum constants
    val randomIndex = Random.nextInt(directions.size) // Generate a random index

    val obstructions: MutableList<Obstructing> = mutableListOf()
    val snakeStartX = (canvas.width/itemWidth)*(itemWidth/2)-(itemWidth/2)
    val snakeStartY = (canvas.height/itemHeight)*(itemHeight/2)-(itemHeight/2)
    val snake: SnakeController = SnakeController(snakeStartX, snakeStartY, directions[randomIndex]) { obstruction ->
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
    var placeNextItemTime = System.currentTimeMillis() + Random.nextLong(10_000L)

    override fun start(stage: Stage) {
        val start = Button(".Snake")
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
        stage.title = ".Snake Game"
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
        val segmentOffsetX = SnakeController.WIDTH/2
        val segmentOffsetY = SnakeController.HEIGHT/2

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

        //add snakes body
        snake.body.forEach { segment ->
            segment.render(canvas)
        }

        //add obstacles
        obstructions.forEach {item ->
            item.render(canvas)
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

                //check collision
                //check if head outside canvas
                if (
                    snake.head.posY - SnakeController.HEIGHT / 2 < 0 ||
                    snake.head.posY + SnakeController.HEIGHT / 2 > canvas.height ||
                    snake.head.posX - SnakeController.WIDTH / 2 < 0 ||
                    snake.head.posX + SnakeController.WIDTH / 2 > canvas.width
                ) {
                    gameAction = GameAction.GAME_OVER
                }

                val combinedObjstructions = snake.body + obstructions
                var obstructionToRemove: Obstructing? = null
                for( obstruction in combinedObjstructions) {
                    if(isColliding(snake.head, obstruction)) {
                        val collisionEvent = obstruction.handleCollision()
                        when (collisionEvent) {
                            Obstructing.CollisionEvent.GROW -> {
                                nextAction = MoveAction.GROW
                                obstructionToRemove = obstruction
                            }
                            Obstructing.CollisionEvent.KILL -> {
                                gameAction = GameAction.GAME_OVER
                            }
                        }
                        break
                    }
                }
                //remove obstruction
                obstructionToRemove?.let { obstructions.remove(it) }


                //sometimes add food
                if(System.currentTimeMillis() >= placeNextItemTime) {
                    //find a random place on the canvas
                    var foodItem: FoodItem?
                    do {
                        val posX = (Random.nextInt((canvas.width/itemWidth).toInt()) * itemWidth.toInt()) + (itemWidth.toInt()/2)
                        val posY = (Random.nextInt((canvas.height/itemHeight).toInt()) * itemHeight.toInt()) + (itemHeight.toInt()/2)
                        foodItem = FoodItem(posX.toDouble(), posY.toDouble())
                    } while(obstructions.any {canvasItem -> isColliding(foodItem, canvasItem)})
                    //add obstruction
                    obstructions.add(foodItem)
                    placeNextItemTime = System.currentTimeMillis() + Random.nextLong(10_000L)
                }
            }
            else -> Unit
        }
    }

    fun isColliding(objA: Obstructing, objB: Obstructing): Boolean {
        val halfWidthA = objA.width / 2
        val halfHeightA = objA.height / 2
        val halfWidthB = objB.width / 2
        val halfHeightB = objB.height / 2

        return  objA.posX + halfWidthA > objB.posX - halfWidthB && // Right edge of A reaches left edge of B
                objA.posX - halfWidthA < objB.posX + halfWidthB && // Left edge of A reaches right edge of B
                objA.posY + halfHeightA > objB.posY - halfHeightB && // Bottom edge of A reaches top edge of B
                objA.posY - halfHeightA < objB.posY + halfHeightB    // Top edge of A reaches bottom edge of B
    }

    fun endGame() {
        //show gameover
        job.cancel()
    }

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

