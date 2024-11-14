class Levels(itemWidth: Double, itemHeight: Double, canvasWidth: Double, canvasHeight: Double) {
   val levels: MutableList<Level> = mutableListOf()

   init {
      levels.add(Level("Level 1", mutableListOf(), Direction.EAST, 1))

      //------------- Level 2 ----------------
      val level2 = mutableListOf<Obstructing>()
      //find 1/3 down
      val centerCell = (canvasWidth/itemWidth)/2
      val thirdHeight = ((canvasHeight/itemHeight)/3).toInt()
      val twoThirdHeight = thirdHeight
      val startX = centerCell - (6/2)
      for(i in 0..5) {
         level2.add(SolidObstruction((startX + i) * itemWidth, thirdHeight * itemHeight, itemWidth, itemHeight))
         level2.add(SolidObstruction((startX + i) * itemWidth, thirdHeight * itemHeight * 2, itemWidth, itemHeight))
      }
      levels.add(Level("Level2", level2, Direction.EAST, 25))

      // level3 = mutableListOf<Obstructing>()
      // level4 = mutableListOf<Obstructing>()
   }

   fun getLevel(idx: Int): Level {
      return levels[idx-1].copy(board = levels[idx-1].board.toMutableList())
   }

   data class Level(val name: String, val board: MutableList<Obstructing>, val startDirection: Direction, val goalPoints: Int)
}