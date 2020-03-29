package com.ordy.backend.services

import com.ordy.backend.database.repositories.UserRepository
import com.ordy.backend.exceptions.GenericException
import com.ordy.backend.wrappers.AuthLoginWrapper
import com.ordy.backend.wrappers.AuthRegisterWrapper
import com.ordy.backend.wrappers.AuthTokenWrapper
import com.ordy.backend.database.models.User
import com.ordy.backend.exceptions.PropertyException
import com.ordy.backend.exceptions.ThrowableList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import java.util.*
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

@Service
class AuthService(@Autowired val userRepository: UserRepository, @Autowired val tokenService: TokenService) {
    private final val bCryptRounds = 12
    private val usernamePattern = Regex("^[^ ][A-Za-z0-9 \\-_]+[^ ]$")

    private fun hashPasswd(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(bCryptRounds))
    }

    /* returns True is password is correct */
    private fun checkPasswd(password: String, hash: String): Boolean {
        return BCrypt.checkpw(password, hash)
    }

    fun checkEmail(email: String): Boolean {
        try {
            InternetAddress(email).validate()
        } catch (e: AddressException) {
            return false
        }
        return true
    }

    fun login(loginWrapper: AuthLoginWrapper): AuthTokenWrapper {

        if (checkEmail(loginWrapper.email).not()) {
            val throwableList = ThrowableList()
            throwableList.addPropertyException("email", "Not a valid email address")
            throwableList.addGenericException("Could not login")
            throw throwableList.also { it.code = HttpStatus.UNPROCESSABLE_ENTITY }
        }

        val users = userRepository.findByEmail(loginWrapper.email)

        if (users.isNotEmpty()) {
            val user = users[0]
            if (checkPasswd(loginWrapper.password, user.password)) {
                return AuthTokenWrapper(tokenService.encrypt(user.id.toString()))
            } else {
                throw GenericException(HttpStatus.NOT_FOUND, "Incorrect email, password combination")
            }
        } else {
            throw GenericException(HttpStatus.NOT_FOUND, "Incorrect email, password combination")
        }
    }

    fun register(registerWrapper: AuthRegisterWrapper) {

        val throwableList = ThrowableList()
        if (checkEmail(registerWrapper.email).not()) {
            throwableList.addPropertyException("email", "Not a valid email address")
        }
        if (usernamePattern.matches(registerWrapper.username).not()) {
            throwableList.addPropertyException("username",
                    "Username can only contain letters, number, space dash and underscore")
        }
        throwableList.ifNotEmpty {
            throwableList.addGenericException("Could not register")
            throw throwableList.also { it.code = HttpStatus.UNPROCESSABLE_ENTITY }
        }

        val users = userRepository.findByEmail(registerWrapper.email)

        // Checks if email is in use
        if (users.isEmpty()) {
            val newUser = User(name = registerWrapper.username, email = registerWrapper.email, password = hashPasswd(registerWrapper.password))
            userRepository.saveAndFlush(newUser)
        } else {
            throwableList.addPropertyException("email", "Email alread in use")
            throwableList.addGenericException("Could not register")
            throw throwableList.also { it.code = HttpStatus.FORBIDDEN }
        }
    }
}