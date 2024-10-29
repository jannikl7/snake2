
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color.MEDIUMPURPLE
import javafx.stage.Stage
import kotlinx.coroutines.*

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    Application.launch(SnakeGame::class.java)
}

class SnakeGame() : Application() {
    val canvasHeight = 400.0
    val canvasWidth = 400.0
    val canvas: Canvas = Canvas(canvasWidth, canvasHeight)
    val snakeHead: SnakeSegment = SnakeSegment(
        true, canvas.width / 2, canvas.height / 2, Direction.EAST)


    private val job = SupervisorJob() // Manage coroutine lifecycle
    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun start(stage: Stage) {
        val layout = BorderPane()
        val start = Button("Snake")
        start.setOnMouseClicked { event ->
            initializeNewGame(canvasWidth, canvasHeight)

        }
        layout.center = start


        val scene = Scene(layout, 400.0, 450.0)
        stage.scene = scene
        stage.title = "Snake Game"
        stage.show()
    }

    fun initializeNewGame(width: Double, height: Double) {
        // start game loop
        scope.launch {
            updateGameState()
            render()
            delay(100L)
        }
    }

    fun render() {
        var gContext: GraphicsContext = canvas.graphicsContext2D
        var currentSegment: SnakeSegment? = snakeHead
        var segmentOffsetX = SnakeSegment.WIDTH/2
        var segmentOffsetY = SnakeSegment.HEIGHT/2

        while(currentSegment != null) {
            //draw currentSegment
            gContext.fill = MEDIUMPURPLE
            gContext.fillRect(currentSegment.posX-segmentOffsetX, currentSegment.posY-segmentOffsetY, currentSegment.posX+segmentOffsetX, currentSegment.posY+segmentOffsetY)
            currentSegment = currentSegment.nextSegment
        }
    }

    fun updateGameState() {
        if(false) endGame() //game is over
    }

    fun endGame() {
        //show gameover
        job.cancel()
    }

}

data class SnakeSegment(
    val isHead: Boolean,
    val posX: Double,
    val posY: Double,
    val direction: Direction,
    val nextSegment: SnakeSegment? = null,
) {
    companion object {
        const val WIDTH: Double = 20.0
        const val HEIGHT: Double = 20.0
    }
}

enum class Direction {
    NORTH,
    SOUTH,
    EAST,
    WEST
}
