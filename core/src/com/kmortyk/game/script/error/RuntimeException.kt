package com.kmortyk.game.script.error

import com.kmortyk.game.script.Token
import java.lang.RuntimeException

class RuntimeError(val token: Token, message: String?) : RuntimeException(message)