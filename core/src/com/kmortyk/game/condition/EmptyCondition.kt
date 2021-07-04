package com.kmortyk.game.condition

import com.kmortyk.game.log

class EmptyCondition : Condition {
    override fun isTruly(): Boolean {
        log.info("empty condition called")
        return true
    }
}