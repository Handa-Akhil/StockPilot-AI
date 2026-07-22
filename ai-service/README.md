# StockPilot AI — AI Service

This is the Python FastAPI AI service for StockPilot AI. It wraps the Google Gemini API to deliver explainable AI insights (technical chart interpretations, fundamental summaries, sentiment analysis, and conversational market Q&A).

## Setup Instructions

### Prerequisites
- **Python 3.9+** (Must be installed and added to your system PATH)

### Install Dependencies & Run

1. **Create a virtual environment**:
   ```bash
   # In the ai-service directory
   python -m venv venv
   ```

2. **Activate the virtual environment**:
   - **Windows (PowerShell)**:
     ```powershell
     .\venv\Scripts\Activate.ps1
     ```
   - **macOS/Linux**:
     ```bash
     source venv/bin/activate
     ```

3. **Install requirements**:
   ```bash
   pip install -r requirements.txt
   ```

4. **Run the development server**:
   ```bash
   uvicorn main:app --reload --port 8000
   ```

The service will run locally at `http://localhost:8000`. You can inspect the interactive OpenAPI documentation at `http://localhost:8000/docs`.
