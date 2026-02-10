import requests
import pickle
import hashlib

def insecure_function():
    # ❌ Hardcoded URL + SSL verification disabled
    response = requests.get(
        "https://api.example.com/data",
        verify=False
    )

    # ❌ Weak cryptographic algorithm
    password_hash = hashlib.md5(b"admin123").hexdigest()

    # ❌ Unsafe pickle deserialization
    data = pickle.loads(response.content)

    return data, password_hash
