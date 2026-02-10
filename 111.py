import requests

def get_user_data():
    # ❌ Hardcoded API key in source code
    API_KEY = "sk_test_51HARD_CODED_SECRET_KEY"

    headers = {
        "Authorization": f"Bearer {API_KEY}"
    }

    response = requests.get(
        "https://api.example.com/users",
        headers=headers
    )

    return response.json()
