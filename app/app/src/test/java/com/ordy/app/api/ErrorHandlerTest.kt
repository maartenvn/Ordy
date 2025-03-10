package com.ordy.app.api

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.github.javafaker.Faker
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ordy.app.api.util.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import retrofit2.HttpException
import retrofit2.Response

class ErrorHandlerTest {

    private var faker = Faker()
    private val activity: AppCompatActivity = mock(AppCompatActivity::class.java)
    private val viewGroup: ViewGroup = mock(ViewGroup::class.java)

    @Before
    fun setup() {
        whenever(activity.findViewById<ViewGroup>(android.R.id.content)).thenReturn(viewGroup)
    }

    @Test
    fun `'parse' should use the error message when passed a Throwable`() {
        val errorMessage = faker.name().name()
        val error = Throwable(errorMessage)

        // Parse the error
        val queryError = ErrorHandler().parse(error)

        Assert.assertEquals(errorMessage, queryError.message)
    }

    @Test
    fun `'parse' should use a generic message when passed a Throwable without message`() {
        val error = Throwable()

        // Parse the error
        val queryError = ErrorHandler().parse(error)

        Assert.assertEquals("An unknown error has occurred", queryError.message)
    }

    @Test
    fun `'parse' should use a custom error code when passed a Throwable`() {
        val error = Throwable()

        // Parse the error
        val queryError = ErrorHandler().parse(error)

        Assert.assertEquals("unknown", queryError.code)
    }

    @Test
    fun `'parse' should use a custom error description when passed a Throwable`() {
        val error = Throwable()

        // Parse the error
        val queryError = ErrorHandler().parse(error)

        Assert.assertEquals(
            "Something went wrong, please contact a developer.",
            queryError.description
        )
    }

    @Test
    fun `'parse' should use the error message when passed a HTTPException`() {
        val errorMessage = faker.name().name()

        // Mock a response
        val response = mock(Response::class.java)
        whenever(response.message()).thenReturn(errorMessage)

        val error = HttpException(response)

        // Parse the error
        val queryError = ErrorHandler().parse(error)

        Assert.assertEquals(errorMessage, queryError.message)
    }

    @Test
    fun `'parse' should use a placeholder message when passed a HTTPException without message`() {

        // Mock a response
        val response = mock(Response::class.java)
        whenever(response.message()).thenReturn("")

        val error = HttpException(response)

        // Parse the error
        val queryError = ErrorHandler().parse(error)

        Assert.assertEquals("An unknown error has occurred", queryError.message)
    }

    @Test
    fun `'parse' should use the error code when passed a HTTPException`() {
        val errorCode = 418

        // Mock a response
        val response = mock(Response::class.java)
        whenever(response.message()).thenReturn(faker.name().name())
        whenever(response.code()).thenReturn(errorCode)

        val error = HttpException(response)

        // Parse the error
        val queryError = ErrorHandler().parse(error)

        Assert.assertEquals(errorCode.toString(), queryError.code)
    }

    @Test
    fun `'parse' should use the response when passed a HTTPException`() {
        val errorCode = 418

        // Mock a response
        val response = mock(Response::class.java)
        whenever(response.message()).thenReturn(faker.name().name())

        val error = HttpException(response)

        // Parse the error
        val queryError = ErrorHandler().parse(error)

        Assert.assertEquals(error.response(), queryError.response)
    }

    @Test
    fun `'parse' should use the generalErrors when present when passed HTTPException`() {
        val generalErrorMessage = faker.name().name()
        val errorJson = """{ generalErrors: [ { "message": "$generalErrorMessage" } ] }"""

        // Mock a response body
        val responseBody = errorJson.toResponseBody()

        // Mock a response
        val response = mock(Response::class.java)
        whenever(response.message()).thenReturn(faker.name().name())
        whenever(response.errorBody()).thenReturn(responseBody)

        val error = HttpException(response)

        // Parse the error
        val queryError = ErrorHandler().parse(error)

        Assert.assertFalse(queryError.generalErrors.isEmpty())
        Assert.assertEquals(generalErrorMessage, queryError.generalErrors.first().message)
    }

    @Test
    fun `'parse' should use an empty list for generalErrors when not present when passed HTTPException`() {
        val errorJson = """{}"""

        // Mock a response body
        val responseBody = errorJson.toResponseBody()

        // Mock a response
        val response = mock(Response::class.java)
        whenever(response.message()).thenReturn(faker.name().name())
        whenever(response.errorBody()).thenReturn(responseBody)

        val error = HttpException(response)

        // Parse the error
        val queryError = ErrorHandler().parse(error)

        Assert.assertEquals(emptyList<QueryGeneralError>(), queryError.generalErrors)
    }

    @Test
    fun `'parse' should use the inputErrors when present when passed HTTPException`() {
        val inputErrorMessage = faker.name().name()
        val errorJson = """{ inputErrors: [ { "message": "$inputErrorMessage" } ] }"""

        // Mock a response body
        val responseBody = errorJson.toResponseBody()

        // Mock a response
        val response = mock(Response::class.java)
        whenever(response.message()).thenReturn(faker.name().name())
        whenever(response.errorBody()).thenReturn(responseBody)

        val error = HttpException(response)

        // Parse the error
        val queryError = ErrorHandler().parse(error)

        Assert.assertFalse(queryError.inputErrors.isEmpty())
        Assert.assertEquals(inputErrorMessage, queryError.inputErrors.first().message)
    }

    @Test
    fun `'parse' should use an empty list for inputErrors when not present when passed HTTPException`() {
        val errorJson = """{}"""

        // Mock a response body
        val responseBody = errorJson.toResponseBody()

        // Mock a response
        val response = mock(Response::class.java)
        whenever(response.message()).thenReturn(faker.name().name())
        whenever(response.errorBody()).thenReturn(responseBody)

        val error = HttpException(response)

        // Parse the error
        val queryError = ErrorHandler().parse(error)

        Assert.assertEquals(emptyList<QueryInputError>(), queryError.inputErrors)
    }

    @Test
    fun `'handle' should handle the input errors`() {
        val errorMessage = faker.name().name()
        val errorHandler = spy(ErrorHandler::class.java)

        // Mock the data
        val queryError = mock(QueryError::class.java)
        whenever(queryError.message).thenReturn(errorMessage)
        val fields = emptyList<InputField>()

        // Prevent spawning a snackbar, since it is not mocked
        doNothing().whenever(errorHandler).handleRawGeneral(errorMessage, activity)

        // Call the handle function
        errorHandler.handle(queryError, activity, fields)

        verify(errorHandler, times(1)).handleInputs(
            queryError,
            activity.findViewById(android.R.id.content),
            fields
        )
    }

    @Test
    fun `'handle' should handle the general errors`() {
        val errorMessage = faker.name().name()
        val errorHandler = spy(ErrorHandler::class.java)

        // Mock the data
        val queryError = mock(QueryError::class.java)
        whenever(queryError.message).thenReturn(errorMessage)

        // Prevent spawning a snackbar, since it is not mocked
        doNothing().whenever(errorHandler).handleRawGeneral(errorMessage, activity)

        // Call the handle function
        errorHandler.handle(queryError, activity)

        verify(errorHandler, times(1)).handleGeneral(queryError, activity)
    }

    @Test
    fun `'handle' should put 'displayedError' on true when called`() {
        val errorMessage = faker.name().name()
        val errorHandler = spy(ErrorHandler::class.java)

        // Mock the data
        val queryError = QueryError()
        queryError.message = errorMessage

        // Prevent spawning a snackbar, since it is not mocked
        doNothing().whenever(errorHandler).handleRawGeneral(errorMessage, activity)

        // Call the handle function
        errorHandler.handle(queryError, activity)

        Assert.assertTrue(queryError.displayedError)
    }

    @Test
    fun `'handle' should do nothing when 'displayedError' is true`() {
        val errorMessage = faker.name().name()
        val errorHandler = spy(ErrorHandler::class.java)

        // Mock the data
        val queryError = QueryError()
        queryError.message = errorMessage
        queryError.displayedError = true

        // Prevent spawning a snackbar, since it is not mocked
        doNothing().whenever(errorHandler).handleRawGeneral(errorMessage, activity)

        // Call the handle function
        errorHandler.handle(queryError, activity)

        verify(errorHandler, times(1)).handle(queryError, activity)
        verifyNoMoreInteractions(errorHandler)
    }

    @Test
    fun `'handle' should call 'handleRawGeneral' with the error message when no input or general error was found`() {
        val errorMessage = faker.name().name()
        val errorHandler = spy(ErrorHandler::class.java)

        // Mock the data
        val queryError = QueryError()
        queryError.message = errorMessage
        queryError.inputErrors = emptyList()
        queryError.generalErrors = emptyList()

        // Prevent spawning a snackbar, since it is not mocked
        doNothing().whenever(errorHandler).handleRawGeneral(errorMessage, activity)

        // Call the handle function
        errorHandler.handle(queryError, activity)

        verify(errorHandler, times(1)).handleRawGeneral(queryError.message, activity)
    }
}