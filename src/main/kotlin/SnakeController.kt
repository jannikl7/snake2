import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop

class SnakeController(

   private val onAddSegment: (Obstructing) -> Unit
) {
   companion object {
      const val WIDTH: Double = 20.0
      const val HEIGHT: Double = 20.0
      val STROKE: Color = Color.DARKGRAY
   }

   val head: SnakeSegment = SnakeSegment(WIDTH, HEIGHT, WIDTH, HEIGHT, Direction.EAST)
   private var _mouthOpen = false // Backing field to hold the actual state
   val mouthOpen: Boolean
      get() {
         _mouthOpen = !_mouthOpen // Toggle the value
         return _mouthOpen
      }

   private val innerBody: MutableList<SnakeSegment> = mutableListOf()
   val body: MutableList<SnakeSegment>
      get() = innerBody

   val gradient = RadialGradient(
      0.0, 0.0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
      Stop(0.0, Color.GREEN), Stop(1.0, Color.DARKGREEN)
   )

   fun initAt(posX: Double, posY: Double, direction: Direction) {
      innerBody.clear()
      _mouthOpen = false
      head.posX = posX
      head.posY = posY
      head.direction = direction
   }

   fun moveToPos(posX: Double, posY: Double) {
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
      head.posX = posX
      head.posY = posY
   }

   fun move(action: MoveAction): SnakeSegment? {
      var element: SnakeSegment? = null
      if (action == MoveAction.GROW) {
         //push new element with head pos
         grow(1)
         onAddSegment(innerBody.first())
      } else if (action == MoveAction.SHRINK) {
         element = innerBody.removeLast()
      }
      var _posX = head.posX
      var _posY = head.posY
      when (head.direction) {
         Direction.NORTH ->
            _posY -= HEIGHT

         Direction.SOUTH ->
            _posY += HEIGHT

         Direction.EAST ->
            _posX += WIDTH

         Direction.WEST ->
            _posX -= WIDTH
      }
      moveToPos(_posX, _posY)
      return element
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
      val segmentOffsetX = SnakeController.WIDTH / 2
      val segmentOffsetY = SnakeController.HEIGHT / 2
      gContext.save()
      gContext.translate(head.posX + segmentOffsetX, head.posY + segmentOffsetY)
      when (head.direction) {
         Direction.NORTH -> gContext.rotate(270.0)
         Direction.SOUTH -> gContext.rotate(90.0)
         Direction.EAST -> gContext.rotate(0.0)
         Direction.WEST -> gContext.scale(-1.0, 1.0)
      }
      when (mouthOpen) {
         true -> {
            val xPoints = doubleArrayOf(
               -segmentOffsetX,
               -segmentOffsetX + SnakeController.WIDTH,
               0.0,
               -segmentOffsetX + SnakeController.WIDTH,
               -segmentOffsetX
            )
            val yPoints = doubleArrayOf(
               -segmentOffsetY,
               -segmentOffsetY,
               0.0,
               -segmentOffsetY + SnakeController.HEIGHT,
               -segmentOffsetY + SnakeController.HEIGHT
            )
            gContext.fill = gradient
            gContext.fillPolygon(xPoints, yPoints, xPoints.size)
            gContext.stroke = SnakeController.STROKE
            gContext.strokePolygon(xPoints, yPoints, xPoints.size)
            gContext.fill = Color.GREEN
            gContext.fillOval(0.0, -(SnakeController.HEIGHT / 2), 4.0, 4.0)
         }

         false -> {

            val xPoints = doubleArrayOf(
               -segmentOffsetX,
               -segmentOffsetX + SnakeController.WIDTH,
               -segmentOffsetX + SnakeController.WIDTH,
               0.0,
               -segmentOffsetX + SnakeController.WIDTH,
               -segmentOffsetX + SnakeController.WIDTH,
               -segmentOffsetX
            )
            val yPoints = doubleArrayOf(
               -segmentOffsetY,
               -segmentOffsetY,
               0.0,
               0.0,
               0.0,
               -segmentOffsetY + SnakeController.HEIGHT,
               -segmentOffsetY + SnakeController.HEIGHT
            )
            gContext.fill = gradient
            gContext.fillPolygon(xPoints, yPoints, xPoints.size)
            gContext.fill = Color.GREEN
            gContext.fillOval(0.0, -(SnakeController.HEIGHT / 2), 4.0, 4.0)
            gContext.stroke = SnakeController.STROKE
            gContext.strokePolygon(xPoints, yPoints, xPoints.size)
            gContext.strokeLine(0.0, 0.0, SnakeController.WIDTH / 2, 0.0)
         }
      }
      gContext.restore()
   }

   data class SnakeSegment(
      override var posX: Double,
      override var posY: Double,
      override val width: Double,
      override val height: Double,
      var direction: Direction
   ) : Obstructing {
      override fun handleCollision(): Obstructing.CollisionEvent {
         return Obstructing.CollisionEvent.KILL
      }

      override fun render(canvas: Canvas) {
         val gContext = canvas.graphicsContext2D
         val gradient = RadialGradient(
            0.0, 0.0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            Stop(0.0, Color.GREEN), Stop(1.0, Color.DARKGREEN)
         )

         gContext.fill = gradient
         gContext.fillRoundRect(
            this.posX,
            this.posY,
            WIDTH,
            HEIGHT,
            WIDTH / 2,
            HEIGHT / 2
         )

         gContext.stroke = SnakeController.STROKE
         gContext.strokeRoundRect(
            this.posX,
            this.posY,
            WIDTH,
            HEIGHT,
            WIDTH / 2,
            HEIGHT / 2
         )
      }
   }
}

