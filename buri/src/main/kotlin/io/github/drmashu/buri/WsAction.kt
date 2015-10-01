package io.github.drmashu.buri

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * WebSocket Action
 */
public class WsAction(request: HttpServletRequest, response: HttpServletResponse ): Action(request, response) {
    override fun encode(str: String): String = str

}