package com.kmortyk.game.person

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.kmortyk.game.Assets
import com.kmortyk.game.GridPosition
import com.kmortyk.game.Position
import com.kmortyk.game.PrettyCoolGame
import com.kmortyk.game.animation.AnimationManager
import com.kmortyk.game.effect.MovePersonToCurPositionEffect
import com.kmortyk.game.effect.SurpriseEffect
import com.kmortyk.game.hexagon.Hexagon
import com.kmortyk.game.item.Item
import com.kmortyk.game.state.GameState
import com.kmortyk.game.state.GameStatistic
import com.kmortyk.game.ui.game.PersonHPBar

enum class PersonState { Stand, Walking, Aggressive }
enum class Direction { Right, Left }

open class Person(public val name: String, row: Int, col: Int) {
    companion object {
        fun oppositeDirection(direction: Direction): Direction {
            if (direction == Direction.Left)
                return Direction.Right
            else if(direction == Direction.Right)
                return Direction.Left
            return Direction.Right
        }

        const val StartHealthPoints: Int = 10
        const val StartMaxHealthPoints: Int = 10
    }

    /// MAIN STATE
    // position of the person at map and the screen
    val position: Position = Position()
    // direction of this person
    var direction: Direction = Direction.Right
    // current person's state
    var state: PersonState = PersonState.Stand

    /// PROPERTIES
    var attackPower = 0
    // healthPoints represents level of health for the person
    var healthPoints: Int = StartHealthPoints
    // maximum amount of health
    var maxHealthPoints: Int = StartMaxHealthPoints

    /// GRAPHICS
    // hp bar ui element, that belongs to this person
    val hpBar: PersonHPBar = PersonHPBar()
    // animation manager controls current frame to draw
    val animationManager: AnimationManager = AnimationManager()
    // default sprite width
    val defaultWidth: Int
    // default sprite height
    val defaultHeight: Int
    // effects for this person
    val movePersonEffect = MovePersonToCurPositionEffect(this)

    /// MECHANICS
    // hexes that this person is observes
    val observableHexes: MutableList<Hexagon> = mutableListOf()
    // persons that seen at the moment by the person
    val seenPersons: MutableSet<Person> = mutableSetOf()
    val seenEnemies: MutableSet<Person> = mutableSetOf()
    // dialog represents text dialog that will be showed when interacting with think person
    var dialog: Dialog? = null

    init {
        val serialized = Assets.Persons[name]!!
        position.setGrid(row, col)
        animationManager.switchTo(serialized.drawable)

        if(serialized.dialog != null) {
            dialog = Dialog(serialized.dialog)
        }

        when(serialized.state) {
            "aggressive" -> { this.state = PersonState.Aggressive }
            "stand"      -> { this.state = PersonState.Stand }
            "walking"    -> { this.state = PersonState.Walking }
        }


        /// DEBUG disable ai
        if(PrettyCoolGame.Debug && PrettyCoolGame.DisableAI) {
            this.state = PersonState.Stand
        }

        this.attackPower = serialized.barehands_power
        this.maxHealthPoints = serialized.init_hp
        this.healthPoints = serialized.init_hp

        val anim = Assets.Animations[serialized.drawable]!!
        val defaultTexture = Assets.atlas(anim.atlas).findRegion("default")

        defaultWidth = defaultTexture.originalWidth
        defaultHeight = defaultTexture.originalHeight
    }

    fun gridPosition(): GridPosition = position.grid

    // set destination for the person to move at current step
    fun setDestination(pos: GridPosition)  {
        val dir = Position.projectGridToActual(pos.row, pos.col)
        direction = when {
            dir.x > position.actualX() -> { Direction.Right }
            dir.x < position.actualX() -> { Direction.Left }
            else -> direction // save last
        }
    }

    fun focusOnTarget(gameState: GameState, p: Person) {
        setDestination(p.gridPosition())
        updateObservableHexes(gameState)
        updateSeenPersons(gameState)
    }

    // person draw
    fun draw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        val frame = animationManager.frame(Gdx.graphics.deltaTime)

        val flip = direction == Direction.Left
        val spriteX = if (flip) { position.actualX() + (Assets.HexWidth - defaultWidth) * 0.5f + defaultWidth }
                      else      { position.actualX() + (Assets.HexWidth - defaultWidth) * 0.5f }
        val spriteWidth = if (flip) { -frame.regionWidth.toFloat() }
                          else      {  frame.regionWidth.toFloat() }

        spriteBatch.draw(frame, spriteX, position.actualY() + Assets.HexHeight * 0.35f, spriteWidth, defaultHeight.toFloat())

        if(hasDialog()) {
            spriteBatch.draw(assetManager[Assets["ui_dialog_icon"]],
                    position.actualX() + Assets.HexWidth * 0.5f - defaultWidth * 0.2f,
                    position.actualY() + Assets.HexHeight * 0.9f)
        } else if(isAggressive()) {
            hpBar.updatePosition(
                    position.actualX() + Assets.HexWidth * 0.5f - PersonHPBar.barWidth * 0.5f,
                    position.actualY() + defaultHeight + Assets.HexHeight * 0.35f + PersonHPBar.barHeight
            )
            hpBar.updateValue(healthPoints, maxHealthPoints)
            hpBar.draw(assetManager, spriteBatch)
        }
    }

    fun addHealthPoints(amount: Int) {
        if(healthPoints + amount <= maxHealthPoints) {
            healthPoints += amount
        } else {
            healthPoints = maxHealthPoints
        }
    }

    // returns true if we can talk with that person
    private fun hasDialog() = dialog != null

    private fun isAggressive() = state == PersonState.Aggressive

    open fun canAttack(gameState: GameState, target: Person): Boolean {
        return isNearPos(target.gridPosition())
    }

    // TODO run animation
    open fun attack(gameState: GameState, target: Person, power: Int = attackPower) {
        target.healthPoints -= power
        if(target.healthPoints <= 0)
            gameState.removePerson(this, target)
    }

    fun isNearPos(t: GridPosition) : Boolean {
        val p = gridPosition()

        val possibleOffsets: Array<Pair<Int, Int>> =
        if (p.row % 2 == 0) arrayOf(Pair(0, 1), Pair(-1, 0), Pair(1, 0), Pair(-1, -1), Pair(0, -1), Pair(1, -1))
        else arrayOf(Pair(-1, 1), Pair(0, 1), Pair(1, 1), Pair(-1, 0), Pair(1, 0), Pair(0, -1))

        for((rowOffset, colOffset) in possibleOffsets) {
            val row = p.row + rowOffset
            val col = p.col + colOffset

            if(row == t.row && col == t.col)
                return true
        }
        return false
    }

    fun randomNearPos(gameState: GameState, stat: GameStatistic) : GridPosition {
        val p = gridPosition()

        val possibleOffsets: Array<Pair<Int, Int>> =
        if (p.row % 2 == 0)
            arrayOf(Pair(0, 0), Pair(0, 1), Pair(-1, 0), Pair(1, 0), Pair(-1, -1), Pair(0, -1), Pair(1, -1))
        else
            arrayOf(Pair(0, 0), Pair(-1, 1), Pair(0, 1), Pair(1, 1), Pair(-1, 0), Pair(1, 0), Pair(0, -1))

        val randOff = possibleOffsets[stat.randomNearPositionIndex(possibleOffsets.size)]
        val newP = GridPosition(p.row + randOff.first, p.col + randOff.second)

        return if(gameState.gameMap.isHexBlocked(newP)) p else newP
    }

    fun getDrawPosition(): Vector2 {
        return getDrawPosition(Vector2())
    }

    fun getDrawPosition(out: Vector2) : Vector2 {
        val frame = animationManager.frame(Gdx.graphics.deltaTime)
        val width = frame.regionWidth

        val flip = direction == Direction.Left
        val spriteX = if (flip) {
            position.actualX() + (Assets.HexWidth - width) * 0.5f
        }
        else { position.actualX() + (Assets.HexWidth - width) * 0.5f }

        out.set(spriteX, position.actualY() + Assets.HexHeight * 0.35f)

        return out
    }

    fun updatePosition(gameState: GameState, newPos: GridPosition, updateActual: Boolean) {
        position.setGrid(newPos, updateActual)
        updateObservableHexes(gameState)
    }

    fun sawPerson(p: Person) = seenPersons.contains(p)

    fun updateSeenPersons(gameState: GameState) {
        // update seen persons
        val prevSeenEnemiesSize = seenEnemies.size
        for(seenHex in observableHexes) {
            if(seenHex.hasPerson()) {
                val person = seenHex.personSlot!!
                // skip is seen self
                if(person == this)
                    continue
                // add surprise effect if this is first person that seen
                if(state == PersonState.Aggressive && prevSeenEnemiesSize == 0 && person is Player) {
                    movePersonEffect.disable()
                    gameState.game.addEffect(SurpriseEffect(gameState.game.assetManager, this))
                    seenEnemies.add(person)
                }
                // mark person as seen
                seenPersons.add(person)
            }
        }
    }

    fun updateObservableHexes(gameState: GameState) {
        observableHexes.clear()
        val radius = 200
        val pHex = gameState.gameMap[position]

        for(row in gameState.gameMap.hexes) {
            for(hex in row) {
                val cx = hex.centerX() - pHex.centerX()
                val cy = hex.centerY() - pHex.centerY()
                if(((direction==Direction.Right && cx >= 0)
                        || (direction==Direction.Left && cx <= 0)) && cx*cx + cy*cy <= radius*radius) {
                    observableHexes.add(hex)
                }
            }
        }
    }

    fun generateLoot(gameState: GameState, stat: GameStatistic): Collection<Item> {
        val res = mutableListOf<Item>()

        if(stat.eventPossible("15%")) {
            res.add(Item(gameState, "hp_sphere_small"))
        } else if(stat.eventPossible("5%")) {
            res.add(Item(gameState, "hp_sphere_medium"))
        }

        return res
    }

    override fun toString(): String {
        return "Person[$name]"
    }
}