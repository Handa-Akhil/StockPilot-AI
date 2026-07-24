from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.exceptions import register_exception_handlers
from app.routes.market import router as market_router
from app.routes.analysis import router as analysis_router

def create_app() -> FastAPI:
    app = FastAPI(
        title=settings.APP_NAME,
        version="1.0",
        description="StockPilot AI Market Data & Analysis Provider API"
    )

    # CORS configuration
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.CORS_ORIGINS,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    # Register custom exception filters
    register_exception_handlers(app)

    # Register API routers
    app.include_router(market_router)
    app.include_router(analysis_router)

    # Base routing check
    @app.get("/")
    def read_root():
        return {"message": "StockPilot Market Data Service is running"}

    @app.get("/health")
    def health_check():
        return {"status": "UP"}

    return app
