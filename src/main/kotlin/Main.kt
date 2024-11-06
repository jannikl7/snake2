
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlinx.coroutines.*
import kotlin.random.Random

/*
------------------ NOTES ---------------
create a world class which can be passed to obstruction objects so they
can have an effect on game parameters such as points, etc

when you have only one segment you can go back

Add nice GAME OVER sign

Slowly speed the game up

if you input several keys before next tick, only the last one will execute. Maybe they need to be cashed
and played sequentially in the following ticks. I could be annoying though.


 */

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

    val bgImg = Image(this::class.java.getResource("/img/backgrounds/river.jpg")?.toExternalForm())

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

        gContext.fill = Color.DIMGREY
        gContext.drawImage(bgImg, 0.0, 0.0)
       // gContext.fillRect(0.0, 0.0, canvas.width, canvas.height)

        //render head
        snake.renderHead(canvas)

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
                snake.move(nextAction, obstructions)?.let { obstructions.remove(it) }
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

                val combinedObstructions = snake.body + obstructions
                var obstructionToRemove = mutableListOf<Obstructing>()
                for( obstruction in combinedObstructions) {
                    if(isColliding(snake.head, obstruction)) {
                        val collisionEvent = obstruction.handleCollision()
                        when (collisionEvent) {
                            Obstructing.CollisionEvent.GROW -> {
                                nextAction = MoveAction.GROW
                                obstructionToRemove.add(obstruction)
                            }
                            Obstructing.CollisionEvent.KILL -> {
                                gameAction = GameAction.GAME_OVER
                            }
                            Obstructing.CollisionEvent.SHRINK -> {
                                nextAction = MoveAction.SHRINK
                                obstructionToRemove.add(obstruction)
                            }
                        }
                    }
                    if(obstruction is FoodItem && obstruction.removeTime < System.currentTimeMillis())
                        obstructionToRemove.add(obstruction)
                }
                //remove obstruction
                obstructionToRemove.forEach { item -> obstructions.remove(item) }


                //sometimes add food
                if(System.currentTimeMillis() >= placeNextItemTime) {
                    //find a random place on the canvas
                    var foodItem: FoodItem
                    do {
                        val posX = (Random.nextInt((canvas.width/itemWidth).toInt()) * itemWidth.toInt()) + (itemWidth.toInt()/2)
                        val posY = (Random.nextInt((canvas.height/itemHeight).toInt()) * itemHeight.toInt()) + (itemHeight.toInt()/2)
                        foodItem = FoodItem(posX.toDouble(), posY.toDouble(), editable = (Random.nextInt(3) == 1))
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
    SHRINK,
}

enum class Direction {
    NORTH,
    SOUTH,
    EAST,
    WEST
}

