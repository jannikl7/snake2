import javafx.scene.paint.Color

class Levels(itemWidth: Double, itemHeight: Double, canvasWidth: Double, canvasHeight: Double, scaleFactor: Int) {
   init {
      val centerCol = (canvasWidth/itemWidth)/2
      val centerRow = (canvasHeight/itemHeight)/2

      val snakeStartX = (canvasWidth / itemWidth) * (itemWidth / 2) - (itemWidth)
      val snakeStartY = (canvasHeight / itemHeight) * (itemHeight / 2) - (itemHeight)

      levels.add(Level(0,"Level 1", mutableListOf(), Direction.EAST, 25, false, snakeStartX, snakeStartY))

      //------------- Level 2 ----------------
      val level2 = mutableListOf<Obstructing>()
      //find 1/3 down
      val thirdHeight = ((canvasHeight/itemHeight)/3).toInt()
      val startX = centerCol - (6/2)
      for(i in 0..5*scaleFactor) {
         level2.add(SolidObstruction((startX + i) * itemWidth, thirdHeight * itemHeight, itemWidth, itemHeight))
         level2.add(SolidObstruction((startX + i) * itemWidth, thirdHeight * itemHeight * 2, itemWidth, itemHeight))
      }
      levels.add(Level(1, "Level2", level2, Direction.EAST, 25, false, snakeStartX, snakeStartY))


      //------------- Level 3 ----------------
      val level3 = mutableListOf<Obstructing>()
      (3 .. 4 * scaleFactor).forEach {
         level3.add(SolidObstruction(it*itemWidth, itemHeight*3, itemWidth, itemHeight))
         level3.add(SolidObstruction(it*itemWidth, itemHeight*4, itemWidth, itemHeight))

         level3.add(SolidObstruction((canvasWidth-itemWidth)-(itemWidth*it), itemHeight*3, itemWidth, itemHeight))
         level3.add(SolidObstruction((canvasWidth-itemWidth)-(itemWidth*it), itemHeight*4, itemWidth, itemHeight))

         level3.add(SolidObstruction(it*itemWidth,canvasHeight-(itemHeight*3), itemWidth, itemHeight))
         level3.add(SolidObstruction(it*itemWidth,canvasHeight-(itemHeight*4), itemWidth, itemHeight))

         level3.add(SolidObstruction((canvasHeight-itemWidth)-(itemWidth*it), canvasHeight-(itemHeight*3), itemWidth, itemHeight))
         level3.add(SolidObstruction((canvasWidth-itemWidth)-(itemWidth*it), canvasHeight-(itemHeight*4), itemWidth, itemHeight))
      }
       levels.add(Level(2, "Level3", level3, Direction.EAST, 25, false, snakeStartX, snakeStartY))

      //------------- Level 4 ----------------
       val level4 = mutableListOf<Obstructing>()
      (3 .. 6).forEach {
         level4.add(SolidObstruction(it*itemWidth, posY = itemHeight*it, itemWidth, itemHeight))
         level4.add(SolidObstruction((canvasWidth-itemWidth)-(itemWidth*it), itemHeight*it, itemWidth, itemHeight))
         level4.add(SolidObstruction(it*itemWidth,canvasHeight-(itemHeight*it), itemWidth, itemHeight))
         level4.add(SolidObstruction((canvasHeight-itemWidth)-(itemWidth*it), canvasHeight-(itemHeight*it), itemWidth, itemHeight))
      }
      levels.add(Level(3, "Level4", level4, Direction.EAST, 25, true, snakeStartX, snakeStartY))

      //------------- Level 5 & 6 ----------------
      val level5 = mutableListOf<Obstructing>()
      val level6 = mutableListOf<Obstructing>()
      val wormhole1 = WormHole(itemWidth*3, itemHeight*3, canvasWidth-(itemWidth*3), canvasHeight-(itemHeight*3), itemWidth, itemHeight, color = Color.GREEN )
      val wormhole2 = WormHole(canvasWidth-(itemWidth*3), itemHeight*3, itemWidth*3, canvasHeight-(itemHeight*3), itemWidth, itemHeight, color = Color.YELLOW )
      level5.add(wormhole1)
      level5.add(wormhole2)
      level6.add(wormhole1)
      level6.add(wormhole2)
      for(i in 0..(canvasHeight-itemHeight).toInt() step itemHeight.toInt()) {
         val obstr = SolidObstruction(((canvasWidth/itemWidth)/2)*itemWidth, i.toDouble(), itemWidth, itemHeight)
         level5.add(obstr)
         level6.add(obstr)
      }

      val y = (canvasHeight/itemHeight)/2 * itemHeight
      val xThird = ((canvasWidth/itemWidth)/4).toInt() * itemWidth
      val xTwoThirds = xThird * 3
      level6.add(
         SolidObstruction(xThird-(2*itemWidth), y, itemWidth, itemHeight)
      )
      level6.add(
         SolidObstruction(xThird, y, itemWidth, itemHeight)
      )
      level6.add(
         SolidObstruction(xThird+(itemWidth*2), y, itemWidth, itemHeight)
      )
      level6.add(
         SolidObstruction(xTwoThirds-(2*itemWidth), y, itemWidth, itemHeight)
      )
      level6.add(
         SolidObstruction(xTwoThirds, y, itemWidth, itemHeight)
      )
      level6.add(
         SolidObstruction(xTwoThirds+(itemWidth*2), y, itemWidth, itemHeight)
      )

      levels.add(Level(4, "Level5", level5, Direction.EAST, 25, false, snakeStartX + (itemWidth*2), snakeStartY))
      levels.add(Level(4, "Level6", level6, Direction.EAST, 25, false, snakeStartX + (itemWidth*2), (canvasHeight/itemHeight)*3))



      levels.lastOrNull()?.let { it.finalLevel = true }
   }

   fun getLevel(idx: Int): Level {
      return levels[idx-1].copy(board = levels[idx-1].board.toMutableList())
   }

   data class Level(val idx: Int, val name: String, val board: MutableList<Obstructing>, val startDirection: Direction, val goalPoints: Int, var finalLevel: Boolean, val startPosX: Double, val startPosY: Double)

   companion object {
      val levels: MutableList<Level> = mutableListOf()
      fun getNextLevel(currLevel: Level): Levels.Level? {
         return levels.getOrNull(currLevel.idx+1)
      }

      fun getFirstLevel(): Levels.Level? {
        // return levels.firstOrNull()
         return levels.lastOrNull()
      }
   }
}