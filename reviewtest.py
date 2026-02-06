# ❌ INSECURE CODE – DO NOT USE IN REAL PROJECTS

OPENAI_API_KEY = "sk-1234567890abcdef"
DB_PASSWORD = "mySuperSecretPassword"

import openai

openai.api_key = OPENAI_API_KEY

def get_response(prompt):
    return openai.ChatCompletion.create(
        model="gpt-3.5-turbo",
        messages=[{"role": "user", "content": prompt}]
    )

print("Connected with password:", DB_PASSWORD)
