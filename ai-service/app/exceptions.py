from fastapi import Request, FastAPI
from fastapi.responses import JSONResponse
import logging

logger = logging.getLogger("stockpilot-market-service.exceptions")

class MarketDataException(Exception):
    def __init__(self, message: str, status_code: int = 500):
        super().__init__(message)
        self.message = message
        self.status_code = status_code

def register_exception_handlers(app: FastAPI):
    @app.exception_handler(MarketDataException)
    async def market_data_exception_handler(request: Request, exc: MarketDataException):
        logger.error(f"MarketDataException on {request.url.path}: {exc.message}")
        return JSONResponse(
            status_code=exc.status_code,
            content={"status": "ERROR", "message": exc.message}
        )

    @app.exception_handler(Exception)
    async def global_exception_handler(request: Request, exc: Exception):
        logger.error(f"Unhandled Exception on {request.url.path}: {str(exc)}", exc_info=True)
        return JSONResponse(
            status_code=500,
            content={"status": "ERROR", "message": "An unexpected error occurred."}
        )
