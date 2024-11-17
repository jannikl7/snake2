import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import java.util.logging.Logger
import kotlin.random.Random
import kotlin.system.exitProcess

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
   companion object {
      val staticItemWidth = 20.0
      val staticItemHeight = 20.0
   }
   val logger = Logger.getLogger("SnakeLogger")

   var scaleFactor:Int = 1
   val itemWidth = staticItemWidth * scaleFactor
   val itemHeight = staticItemHeight * scaleFactor
   val canvasHeight = itemHeight * 20
   val canvasWidth = itemWidth * 20

   val canvas: Canvas = Canvas(canvasWidth, canvasHeight)
   val pointsLabel = Label()
   val layout = BorderPane()

   val levels = Levels(itemHeight, itemWidth, canvasWidth, canvasHeight, scaleFactor)
   lateinit var level: Levels.Level
   val levelPauseLength = 5000
   var levelPauseEnd: Long? = null

   lateinit var obstructions: MutableList<Obstructing>
   val snakeStartX = (canvas.width / itemWidth) * (itemWidth / 2) - (itemWidth)
   val snakeStartY = (canvas.height / itemHeight) * (itemHeight / 2) - (itemHeight)
   lateinit var snake: SnakeController
   var nextAction: MoveAction = MoveAction.NONE
   var gameState: GameState = GameState.MENU
   var points: Int = 0

   enum class GameState {
      MENU,
      PLAYING,
      GAME_OVER,
      LEVEL_COMPLETED,
      GAME_WON,
   }

   private val job = SupervisorJob() // Manage coroutine lifecycle
   private val scope = CoroutineScope(Dispatchers.Default + job)
   var placeNextItemTime = System.currentTimeMillis() + Random.nextLong(10_000L)

   val bgImg = Image(this::class.java.getResource("/img/backgrounds/river.jpg")?.toExternalForm())

   fun initMenu() {
      renderMenu(canvas, layout) {initializeNewGame(canvasWidth, canvasHeight)}
   }

   override fun start(stage: Stage) {
      initMenu()

      val scene = Scene(layout, 400.0, 450.0)
      stage.scene = scene
      stage.title = ".Snake Game"
      stage.show()
   }

   fun initializeNewGame(width: Double, height: Double) {
      level = levels.getLevel(1)
      val firstLevel = Levels.getFirstLevel()
      if(firstLevel == null) {
         logger.severe("There are no levels available!!!")
         exitProcess(1)
      } else {
         snake = SnakeController(snakeStartX, snakeStartY, level.startDirection) { obstruction ->
            obstructions.add(obstruction)
         }
         initializeLevel(firstLevel)
      }
      nextAction = MoveAction.NONE
      pointsLabel.text = "Points: $points"
      canvas.setOnKeyPressed { event ->
         if(gameState == GameState.PLAYING) {
            when (event.code) {
               KeyCode.UP -> snake.turn(Direction.NORTH)
               KeyCode.DOWN -> snake.turn(Direction.SOUTH)
               KeyCode.LEFT -> snake.turn(Direction.WEST)
               KeyCode.RIGHT -> snake.turn(Direction.EAST)
               KeyCode.SPACE -> nextAction = MoveAction.GROW
               else -> Unit //no action
            }
         }
      }
      layout.center = canvas
      layout.bottom = pointsLabel
      canvas.isFocusTraversable = true // Allow canvas to receive focus
      canvas.requestFocus() // Request focus for the canvas
      gameState = GameState.PLAYING
      // start game loop
      scope.launch {
         while (true) {
            updateGameState()
            withContext(Dispatchers.JavaFx) {
               render()
            }
            delay(300L)
         }
      }
   }

   fun initializeLevel(level: Levels.Level) {
      this.level = level
      obstructions = level.board
      snake.initAt(snakeStartX, snakeStartY, level.startDirection)
      points = 0
      nextAction = MoveAction.NONE
   }

   fun render() {
      when(gameState) {
         GameState.GAME_OVER ->
            renderGameOver(canvas)
         GameState.LEVEL_COMPLETED ->
            renderLevelComplete(canvas)
         GameState.PLAYING ->
            renderScene(
               canvas,
               snake,
               pointsLabel,
               points,
               bgImg,
               level,
               gameState
            )
         GameState.MENU ->
            initMenu()
         GameState.GAME_WON ->
            renderGameWon(canvas)
      }
   }

   var currObstructing: Obstructing? = null
   fun updateGameState() {
      when (gameState) {
         GameState.GAME_OVER ->
            endGame() //game is over
         GameState.LEVEL_COMPLETED -> {
            levelPauseEnd?.let { pauseEnd ->
               if (pauseEnd <= System.currentTimeMillis()) {
                  val nextLevel = Levels.getNextLevel(level)
                  if (nextLevel != null) {
                     level = nextLevel
                     levelPauseEnd = null
                     initializeLevel(level)
                     gameState = GameState.PLAYING
                  } else {
                     logger.severe("No more levels and this one is not final. Level.idx: ${level.idx}")
                  }
               }
            } ?: run {
               levelPauseEnd = System.currentTimeMillis() + levelPauseLength
            }

         }
         GameState.PLAYING -> {
            if(currObstructing == null) {
               snake.move(nextAction)?.let { obstructions.remove(it) }
            } else if(nextAction == MoveAction.EXIT_WORMHOLE && currObstructing is WormHole){
               val obstruction = currObstructing as WormHole
               obstruction.let {
                  val exit = obstruction.getExit()
                  snake.moveToPos(exit.posX, exit.posY)
               }

            }
            nextAction = MoveAction.NONE
            //check collision
            //check if head outside canvas
            if (
               snake.head.posY < 0 ||
               snake.head.posY + SnakeController.HEIGHT> canvas.height ||
               snake.head.posX  < 0 ||
               snake.head.posX + SnakeController.WIDTH> canvas.width
            ) {
               gameState = GameState.GAME_OVER
            }

            val combinedObstructions = snake.body + obstructions
            val obstructionToRemove = mutableListOf<Obstructing>()
            for (obstruction in combinedObstructions) {
               if (isColliding(snake.head, obstruction)) {
                  val collisionEvent = obstruction.handleCollision()
                  when (collisionEvent) {
                     Obstructing.CollisionEvent.GROW -> {
                        nextAction = MoveAction.GROW
                        obstructionToRemove.add(obstruction)
                        points++
                        if(points == level.goalPoints) {
                           //check if player won
                              if(level.finalLevel) gameState = GameState.GAME_WON
                              else gameState = GameState.LEVEL_COMPLETED
                        }
                     }

                     Obstructing.CollisionEvent.KILL -> {
                        gameState = GameState.GAME_OVER
                     }

                     Obstructing.CollisionEvent.SHRINK -> {
                        nextAction = MoveAction.SHRINK
                        obstructionToRemove.add(obstruction)
                        points--
                     }

                     Obstructing.CollisionEvent.JUMP -> {
                        if(obstruction is WormHole) {
                           nextAction = MoveAction.EXIT_WORMHOLE
                        }
                     }
                  }
               }
               //find FoodItems ready to be removed
               if (obstruction is FoodItem && obstruction.removeTime < System.currentTimeMillis())
                  obstructionToRemove.add(obstruction)
            }
            //remove obstruction set for removal (e.g. fooditems)
            obstructionToRemove.forEach { item -> obstructions.remove(item) }


            //sometimes add food
            if (System.currentTimeMillis() >= placeNextItemTime) {
               //find a random place on the canvas
               var foodItem: FoodItem
               do {
                  val posX =
                     Random.nextInt((canvas.width / itemWidth).toInt()) * itemWidth.toInt()
                  val posY =
                     Random.nextInt((canvas.height / itemHeight).toInt()) * itemHeight.toInt()
                  foodItem = FoodItem(posX.toDouble(), posY.toDouble(), editable = (Random.nextInt(2) == 1), width = itemWidth, height = itemHeight)
               } while (obstructions.any { canvasItem -> isColliding(foodItem, canvasItem) })
               //add obstruction
               obstructions.add(foodItem)
               placeNextItemTime = System.currentTimeMillis() + Random.nextLong(10_000L)
            }
         }
         else -> Unit
      }
   }

   fun renderGrid() {
      val gContext = canvas.graphicsContext2D
      gContext.stroke = Color.LIGHTGREY
      gContext.lineWidth = 1.0
      for(y:Int in itemHeight.toInt() .. (canvasHeight-itemHeight).toInt() step itemHeight.toInt()) {
         for (x: Int in itemWidth.toInt()..(canvasWidth - itemWidth).toInt() step itemWidth.toInt()) {
            gContext.strokeLine(x.toDouble(), y.toDouble(), x.toDouble(), y+itemHeight)
         }
      }
   }

   fun isColliding(objA: Obstructing, objB: Obstructing): Boolean {
      val halfWidthA = objA.width / 2
      val halfHeightA = objA.height / 2
      val halfWidthB = objB.width / 2
      val halfHeightB = objB.height / 2

      return objA.posX + halfWidthA > objB.posX - halfWidthB && // Right edge of A reaches left edge of B
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
   EXIT_WORMHOLE,
}

enum class Direction {
   NORTH,
   SOUTH,
   EAST,
   WEST
}

