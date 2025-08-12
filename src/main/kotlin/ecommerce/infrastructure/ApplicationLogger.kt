package ecommerce.infrastructure

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ApplicationLogger {
    val log: Logger = LoggerFactory.getLogger(ApplicationLogger::class.java)

    fun logError(message: String?) {
        log.error(message)
    }
}
