package com.ordy.backend.interceptors

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ordy.backend.database.repositories.UserRepository
import com.ordy.backend.exceptions.GenericException
import com.ordy.backend.services.TokenService
import com.ordy.backend.wrappers.TokenWrapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.lang.Exception
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthInterceptor(
        val tokenService: TokenService,
        val userRepo: UserRepository
): HandlerInterceptor{

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, dataObject: Any) : Boolean {

        val token = request.getHeader("Authorization") ?: throw GenericException(HttpStatus.UNAUTHORIZED, "Invalid token")

        try {
            val tokenDecrypt = jacksonObjectMapper().readValue(tokenService.decrypt(token), TokenWrapper::class.java)
            val userId = tokenDecrypt.userId

            // Check if decrypted id is numerical
            if (userId != null) {
                val optionalUser = userRepo.findById(userId)

                if (optionalUser.isPresent) {
                    request.setAttribute("userId", optionalUser.get().id)
                    return true
                } else {
                    throw GenericException(HttpStatus.UNAUTHORIZED, "Invalid token")
                }
            } else {
                throw GenericException(HttpStatus.UNAUTHORIZED, "Invalid token")
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            throw GenericException(HttpStatus.UNAUTHORIZED, "Invalid token")
        }
    }
}
